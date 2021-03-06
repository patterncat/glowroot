/*
 * Copyright 2016 the original author or authors.
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
package org.glowroot.agent.plugin.grails;

import org.glowroot.agent.plugin.api.Agent;
import org.glowroot.agent.plugin.api.MessageSupplier;
import org.glowroot.agent.plugin.api.ThreadContext;
import org.glowroot.agent.plugin.api.ThreadContext.Priority;
import org.glowroot.agent.plugin.api.TimerName;
import org.glowroot.agent.plugin.api.TraceEntry;
import org.glowroot.agent.plugin.api.weaving.BindParameter;
import org.glowroot.agent.plugin.api.weaving.BindReceiver;
import org.glowroot.agent.plugin.api.weaving.BindThrowable;
import org.glowroot.agent.plugin.api.weaving.BindTraveler;
import org.glowroot.agent.plugin.api.weaving.OnBefore;
import org.glowroot.agent.plugin.api.weaving.OnReturn;
import org.glowroot.agent.plugin.api.weaving.OnThrow;
import org.glowroot.agent.plugin.api.weaving.Pointcut;
import org.glowroot.agent.plugin.api.weaving.Shim;

public class GrailsControllerClassAspect {

    @Shim("grails.core.GrailsControllerClass")
    public interface GrailsControllerClass {
        String getDefaultAction();
        String getName();
        String getFullName();
    }

    @Pointcut(className = "grails.core.GrailsControllerClass", methodName = "invoke",
            methodParameterTypes = {"java.lang.Object", "java.lang.String"},
            timerName = "grails controller")
    public static class ControllerAdvice {
        private static final TimerName timerName = Agent.getTimerName(ControllerAdvice.class);

        @OnBefore
        public static TraceEntry onBefore(ThreadContext context,
                @BindReceiver GrailsControllerClass grailsController,
                @SuppressWarnings("unused") @BindParameter Object controller,
                @BindParameter String action) {
            String actionName = action == null ? grailsController.getDefaultAction() : action;
            context.setTransactionName(grailsController.getName() + "#" + actionName,
                    Priority.CORE_PLUGIN);
            return context.startTraceEntry(MessageSupplier.from("grails controller: {}.{}()",
                    grailsController.getFullName(), actionName), timerName);
        }

        @OnReturn
        public static void onReturn(@BindTraveler TraceEntry traceEntry) {
            traceEntry.end();
        }

        @OnThrow
        public static void onThrow(@BindThrowable Throwable throwable,
                @BindTraveler TraceEntry traceEntry) {
            traceEntry.endWithError(throwable);
        }
    }
}
