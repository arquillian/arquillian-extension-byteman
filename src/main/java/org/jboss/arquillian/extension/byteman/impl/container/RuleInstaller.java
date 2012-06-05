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

import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.extension.byteman.impl.common.BytemanConfiguration;
import org.jboss.arquillian.extension.byteman.impl.common.ExtractScriptUtil;
import org.jboss.arquillian.extension.byteman.impl.common.SubmitUtil;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;

/**
 * MethodRuleInstaller
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class RuleInstaller
{
    public static final String CLASS_KEY_PREFIX = "Class:";
    public static final String METHOD_KEY_PREFIX = "Method:";

    public void installClass(@Observes BeforeClass event)
    {
        BytemanConfiguration config = BytemanConfiguration.from(
                Thread.currentThread().getContextClassLoader().getResourceAsStream(BytemanConfiguration.BYTEMAN_CONFIG)
        );

        String script = ExtractScriptUtil.extract(event);
        if(script != null)
        {
            SubmitUtil.install(generateKey(CLASS_KEY_PREFIX), script, config.containerAgentPort());
        }
    }

    public void uninstallClass(@Observes AfterClass event)
    {
        BytemanConfiguration config = BytemanConfiguration.from(
                Thread.currentThread().getContextClassLoader().getResourceAsStream(BytemanConfiguration.BYTEMAN_CONFIG)
        );

        String script = ExtractScriptUtil.extract(event);
        if(script != null)
        {
            SubmitUtil.uninstall(generateKey(CLASS_KEY_PREFIX), script, config.containerAgentPort());
        }
    }

    public void installMethod(@Observes Before event)
    {
        BytemanConfiguration config = BytemanConfiguration.from(
                Thread.currentThread().getContextClassLoader().getResourceAsStream(BytemanConfiguration.BYTEMAN_CONFIG)
        );

        String script = ExtractScriptUtil.extract(event);
        if(script != null)
        {
            SubmitUtil.install(generateKey(METHOD_KEY_PREFIX), script, config.containerAgentPort());
        }
    }

    public void uninstallMethod(@Observes After event)
    {
        BytemanConfiguration config = BytemanConfiguration.from(
                Thread.currentThread().getContextClassLoader().getResourceAsStream(BytemanConfiguration.BYTEMAN_CONFIG)
        );

        String script = ExtractScriptUtil.extract(event);
        if(script != null)
        {
            SubmitUtil.uninstall(generateKey(METHOD_KEY_PREFIX), script, config.containerAgentPort());
        }
    }

    private String generateKey(String prefix)
    {
        return prefix + Thread.currentThread().getName();
    }
}
