/**
 * Copyright 2012-2013 the original author or authors.
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
package io.informant.config;

import io.informant.api.weaving.Mixin;
import io.informant.api.weaving.Pointcut;
import io.informant.util.ObjectMappers;
import io.informant.util.Resources2;
import io.informant.util.Singleton;
import io.informant.weaving.Advice;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import checkers.igj.quals.Immutable;
import checkers.igj.quals.ReadOnly;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;

/**
 * @author Trask Stalnaker
 * @since 0.5
 */
@Singleton
public class PluginDescriptorCache {

    private static final Logger logger = LoggerFactory.getLogger(PluginDescriptorCache.class);
    private static final ObjectMapper mapper = ObjectMappers.create();

    private final ImmutableList<PluginDescriptor> pluginDescriptors;
    private final ImmutableList<Mixin> mixins;
    private final ImmutableList<Advice> advisors;

    public PluginDescriptorCache() {
        ImmutableList.Builder<PluginDescriptor> pluginDescriptors = ImmutableList.builder();
        try {
            pluginDescriptors.addAll(readPackagedPlugins());
            pluginDescriptors.addAll(readInstalledPlugins());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        this.pluginDescriptors = pluginDescriptors.build();

        ImmutableList.Builder<Mixin> mixins = ImmutableList.builder();
        ImmutableList.Builder<Advice> advisors = ImmutableList.builder();
        for (PluginDescriptor pluginDescriptor : this.pluginDescriptors) {
            for (String aspect : pluginDescriptor.getAspects()) {
                try {
                    // don't initialize the aspect since that will trigger static initializers which
                    // will probably call PluginServices.get()
                    Class<?> aspectClass = Class.forName(aspect, false,
                            PluginDescriptor.class.getClassLoader());
                    advisors.addAll(getAdvisors(aspectClass));
                    mixins.addAll(getMixins(aspectClass));
                } catch (ClassNotFoundException e) {
                    continue;
                }
            }
        }
        this.mixins = mixins.build();
        this.advisors = advisors.build();
    }

    // don't return ImmutableList since this method is used by UiTestingMain and when UiTestingMain
    // is compiled by maven, it is compiled against shaded informant, but then if it is run inside
    // an IDE without rebuilding UiTestingMain it will fail since informant is then unshaded
    @Immutable
    public List<PluginDescriptor> getPluginDescriptors() {
        return pluginDescriptors;
    }

    // don't return ImmutableList since this method is used by SameJvmExecutionAdapter and when
    // SameJvmExecutionAdapter is compiled by maven, it is compiled against shaded informant,
    // but then if a unit test is run inside an IDE without rebuilding SameJvmExecutionAdapter it
    // will fail since informant is then unshaded
    @Immutable
    public List<Mixin> getMixins() {
        return mixins;
    }

    // don't return ImmutableList, see comment above
    @Immutable
    public List<Advice> getAdvisors() {
        return advisors;
    }

    private static List<PluginDescriptor> readInstalledPlugins() throws IOException {
        List<PluginDescriptor> plugins = Lists.newArrayList();
        List<URL> urls = Resources2.getResources("META-INF/io.informant.plugin.json");
        for (URL url : urls) {
            try {
                String content = Resources.toString(url, Charsets.UTF_8);
                PluginDescriptor pluginDescriptor =
                        mapper.readValue(content, PluginDescriptor.class);
                plugins.add(pluginDescriptor);
            } catch (JsonProcessingException e) {
                // no need to log stack trace
                logger.error("syntax error in file {}: {}", url.toExternalForm(), e.getMessage());
                continue;
            }
        }
        return plugins;
    }

    @ReadOnly
    private static List<PluginDescriptor> readPackagedPlugins() throws IOException {
        URL url = Resources2.getResource("META-INF/io.informant.package.json");
        if (url == null) {
            return ImmutableList.of();
        }
        String content = Resources.toString(url, Charsets.UTF_8);
        try {
            return mapper.readValue(content, PackageDescriptor.class).getPlugins();
        } catch (JsonProcessingException e) {
            // no need to log stack trace
            logger.error("error in file {}: {}", url.toExternalForm(), e.getMessage());
            return ImmutableList.of();
        }
    }

    private static List<Advice> getAdvisors(Class<?> aspectClass) {
        List<Advice> advisors = Lists.newArrayList();
        for (Class<?> memberClass : aspectClass.getClasses()) {
            Pointcut pointcut = memberClass.getAnnotation(Pointcut.class);
            if (pointcut != null) {
                advisors.add(Advice.from(pointcut, memberClass));
            }
        }
        return advisors;
    }

    private static List<Mixin> getMixins(Class<?> aspectClass) {
        List<Mixin> mixins = Lists.newArrayList();
        for (Class<?> memberClass : aspectClass.getClasses()) {
            Mixin mixin = memberClass.getAnnotation(Mixin.class);
            if (mixin != null) {
                mixins.add(mixin);
            }
        }
        return mixins;
    }
}