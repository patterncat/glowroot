/**
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.informantproject.core.weaving.preinit;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * @author Trask Stalnaker
 * @since 0.5
 */
public class GlobalCollector {

    private static final Logger logger = LoggerFactory.getLogger(GlobalCollector.class);

    // caches
    private final Set<ReferencedMethod> referencedMethods = Sets.newHashSet();
    private final Map<String, Optional<TypeCollector>> typeCollectors = Maps.newHashMap();

    private final Set<ReferencedMethod> overrides = Sets.newTreeSet();

    private String indent = "";

    public void processMethod(ReferencedMethod rootMethod) throws IOException {
        if (referencedMethods.contains(rootMethod)) {
            return;
        }
        if (rootMethod.getOwner().startsWith("[")) {
            // method on an Array, e.g. new String[] {}.clone()
            return;
        }
        // add the containing type and its super types if not already added
        Optional<TypeCollector> optional = typeCollectors.get(rootMethod.getOwner());
        if (optional == null) {
            optional = addType(rootMethod.getOwner());
        }
        if (!optional.isPresent()) {
            // couldn't find type
            return;
        }
        logger.debug(indent + rootMethod);
        referencedMethods.add(rootMethod);
        if (Types.inBootstrapClassLoader(rootMethod.getOwner())) {
            return;
        }
        TypeCollector typeCollector = optional.get();
        String methodId = rootMethod.getName() + ":" + rootMethod.getDesc();
        MethodCollector methodCollector = typeCollector.getMethodCollector(methodId);
        if (methodCollector == null && !rootMethod.getName().equals("<clinit>")
                && typeCollector.getSuperType() != null) {
            // can't find method in type, so go up to super type
            processMethod(ReferencedMethod.from(typeCollector.getSuperType(), methodId));
        }
        // methodCollector can be null, e.g. unimplemented interface method in an abstract class
        if (methodCollector != null && !Types.inBootstrapClassLoader(rootMethod.getOwner())) {
            processMethod(methodCollector);
        }
    }

    public void processOverrides() throws IOException {
        while (true) {
            for (String type : typeCollectors.keySet()) {
                addOverrideReferencedMethods(type);
                addOverrideBootstrapMethods(type);
            }
            if (overrides.isEmpty()) {
                return;
            }
            for (ReferencedMethod override : overrides) {
                processMethod(override);
            }
            overrides.clear();
        }
    }

    public List<String> usedTypes() {
        List<String> types = Lists.newArrayList();
        for (String type : Sets.newTreeSet(typeCollectors.keySet())) {
            if (!Types.inBootstrapClassLoader(type) && Types.exists(type)) {
                types.add(type.replace('/', '.'));
            }
        }
        return types;
    }

    private void processMethod(MethodCollector methodCollector) throws IOException {
        // add types referenced from inside the method
        for (String referencedType : methodCollector.getReferencedTypes()) {
            addType(referencedType);
        }
        // recurse into other methods called from inside the method
        for (ReferencedMethod referencedMethod : methodCollector.getReferencedMethods()) {
            String prevIndent = indent;
            indent = indent + "  ";
            processMethod(referencedMethod);
            indent = prevIndent;
        }
    }

    private Optional<TypeCollector> addType(String type) throws IOException {
        Optional<TypeCollector> optional = typeCollectors.get(type);
        if (optional != null) {
            return optional;
        }
        ImmutableSet.Builder<String> allSuperTypes = ImmutableSet.builder();
        TypeCollector typeCollector = createTypeCollector(type);
        if (typeCollector == null) {
            optional = Optional.absent();
            typeCollectors.put(type, optional);
            return optional;
        }
        // don't return or recurse without typeCollector being fully built
        typeCollectors.put(type, Optional.of(typeCollector));
        if (typeCollector.getSuperType() != null) {
            // it's a major problem if super type is not present, ok to call Optional.get()
            TypeCollector superTypeCollector = addType(typeCollector.getSuperType()).get();
            allSuperTypes.addAll(superTypeCollector.getAllSuperTypes());
            allSuperTypes.add(typeCollector.getSuperType());
        }
        for (String interfaceType : typeCollector.getInterfaceTypes()) {
            Optional<TypeCollector> itype = addType(interfaceType);
            if (itype.isPresent()) {
                allSuperTypes.addAll(itype.get().getAllSuperTypes());
                allSuperTypes.add(interfaceType);
            } else {
                logger.debug("Could not find type: {}", interfaceType);
                typeCollector.setAllSuperTypes(allSuperTypes.build());
                return Optional.absent();
            }
        }
        typeCollector.setAllSuperTypes(allSuperTypes.build());
        // add static initializer (if it exists)
        processMethod(ReferencedMethod.from(type, "<clinit>", "()V"));
        // always add default constructor (if it exists)
        processMethod(ReferencedMethod.from(type, "<init>", "()V"));
        return Optional.of(typeCollector);
    }

    @Nullable
    private TypeCollector createTypeCollector(String type) {
        if (ClassLoader.getSystemResource(type + ".class") == null) {
            logger.debug("Could not find class: {}", type);
            return null;
        }
        TypeCollector typeCollector = new TypeCollector();
        try {
            ClassReader cr = new ClassReader(type);
            MyRemappingClassAdapter visitor = new MyRemappingClassAdapter(typeCollector);
            cr.accept(visitor, 0);
            return typeCollector;
        } catch (IOException e) {
            logger.error("Error parsing class: {}", type);
            return null;
        }
    }

    private void addOverrideReferencedMethods(String type) {
        Optional<TypeCollector> optional = typeCollectors.get(type);
        if (!optional.isPresent()) {
            return;
        }
        TypeCollector typeCollector = optional.get();
        for (String methodId : typeCollector.getMethodIds()) {
            if (methodId.startsWith("<clinit>:") || methodId.startsWith("<init>:")) {
                // TODO also skip private methods
                continue;
            }
            for (String superType : typeCollector.getAllSuperTypes()) {
                if (referencedMethods.contains(ReferencedMethod.from(superType, methodId))) {
                    addOverrideMethod(type, methodId);
                    // break inner loop
                    break;
                }
            }
        }
    }

    private void addOverrideBootstrapMethods(String type) {
        if (Types.inBootstrapClassLoader(type)) {
            return;
        }
        Optional<TypeCollector> optional = typeCollectors.get(type);
        if (!optional.isPresent()) {
            return;
        }
        TypeCollector typeCollector = optional.get();
        // add overridden bootstrap methods in type, e.g. hashCode(), toString()
        for (String methodId : typeCollector.getMethodIds()) {
            if (methodId.startsWith("<clinit>:") || methodId.startsWith("<init>:")) {
                // TODO also skip private methods
                continue;
            }
            for (String superType : typeCollector.getAllSuperTypes()) {
                if (Types.inBootstrapClassLoader(superType)) {
                    TypeCollector superTypeCollector = typeCollectors.get(superType).get();
                    if (superTypeCollector.getMethodCollector(methodId) != null) {
                        addOverrideMethod(type, methodId);
                    }
                }
            }
        }
    }

    private void addOverrideMethod(String type, String methodId) {
        ReferencedMethod referencedMethod = ReferencedMethod.from(type, methodId);
        if (!referencedMethods.contains(referencedMethod)) {
            overrides.add(referencedMethod);
        }
    }
}