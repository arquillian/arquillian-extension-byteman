/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.extension.byteman.impl.container;

import java.util.EnumSet;

import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.event.Event;
import org.jboss.arquillian.extension.byteman.api.ExecType;
import org.jboss.arquillian.extension.byteman.impl.common.AbstractRuleInstaller;
import org.jboss.arquillian.extension.byteman.impl.common.BytemanConfiguration;
import org.jboss.arquillian.extension.byteman.impl.common.ExecContext;
import org.jboss.arquillian.extension.byteman.impl.common.ExtractScriptUtil;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jboss.arquillian.test.spi.event.suite.TestEvent;

/**
 * MethodRuleInstaller
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @version $Revision: $
 */
public class RuleInstaller extends AbstractRuleInstaller {
    private static BytemanConfiguration getConfiguration() {
        return BytemanConfiguration.from(
            Thread.currentThread().getContextClassLoader().getResourceAsStream(BytemanConfiguration.BYTEMAN_CONFIG)
        );
    }

    public void installClassClient(@Observes BeforeClass event) {
        ExecContext context = getExecContextContainer(event);
        if(!isInstalled(CLASS_KEY_PREFIX, context)) {
	        String script = ExtractScriptUtil.extract(context, event);
	        install(CLASS_KEY_PREFIX, script, context);
        }
    }

    public void installMethodClient(@Observes Before event) {
        ExecContext context = getExecContextContainer(event);
        String script = ExtractScriptUtil.extract(context, event);
        install(METHOD_KEY_PREFIX, script, context);
    }

    public void uninstallMethodClient(@Observes After event) {
        ExecContext context = getExecContextContainer(event);
        String script = ExtractScriptUtil.extract(context, event);
        uninstall(METHOD_KEY_PREFIX, script, context);
    }

    protected ExecContext getExecContextContainer(Event event) {
        BytemanConfiguration configuration = getConfiguration();
        return new ExecContext(configuration.containerAgentPort(), EnumSet.of(ExecType.ALL, ExecType.CONTAINER),
                configuration);
    }

    protected boolean shouldRun(TestEvent event) {
        return true;
    }
}
