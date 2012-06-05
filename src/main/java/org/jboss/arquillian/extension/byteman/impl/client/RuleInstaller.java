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
package org.jboss.arquillian.extension.byteman.impl.client;

import java.lang.reflect.Method;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.client.deployment.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.extension.byteman.impl.common.BytemanConfiguration;
import org.jboss.arquillian.extension.byteman.impl.common.ExtractScriptUtil;
import org.jboss.arquillian.extension.byteman.impl.common.SubmitUtil;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jboss.arquillian.test.spi.event.suite.TestEvent;

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

    @Inject
    private Instance<Deployment> deploymentInstance;

    @Inject
    private Instance<Container> containerInstance;

    @Inject
    private Instance<ArquillianDescriptor> descriptorInst;

    public void installClass(@Observes BeforeClass event)
    {
        BytemanConfiguration config = BytemanConfiguration.from(descriptorInst.get());
        
        String script = ExtractScriptUtil.extract(event);
        if(script != null)
        {
            SubmitUtil.install(generateKey(CLASS_KEY_PREFIX), script, config.clientAgentPort());
        }
    }

    public void uninstallClass(@Observes AfterClass event)
    {
        BytemanConfiguration config = BytemanConfiguration.from(descriptorInst.get());
        
        String script = ExtractScriptUtil.extract(event);
        if(script != null)
        {
            SubmitUtil.uninstall(generateKey(CLASS_KEY_PREFIX), script, config.clientAgentPort());
        }
    }

    public void installMethod(@Observes Before event)
    {
        if(!shouldRun(deploymentInstance.get(), containerInstance.get(), event))
        {
            return;
        }

        BytemanConfiguration config = BytemanConfiguration.from(descriptorInst.get());
        String script = ExtractScriptUtil.extract(event);
        if(script != null)
        {
            SubmitUtil.install(generateKey(METHOD_KEY_PREFIX), script, config.clientAgentPort());
        }
    }

    public void uninstallMethod(@Observes After event)
    {
        if(!shouldRun(deploymentInstance.get(), containerInstance.get(), event))
        {
            return;
        }

        BytemanConfiguration config = BytemanConfiguration.from(descriptorInst.get());
        String script = ExtractScriptUtil.extract(event);
        if(script != null)
        {
            SubmitUtil.uninstall(generateKey(METHOD_KEY_PREFIX), script, config.clientAgentPort());
        }
    }

    private String generateKey(String prefix)
    {
        return prefix + Thread.currentThread().getName();
    }

    private static boolean shouldRun(Deployment deployment, Container container, TestEvent event)
    {
        if(isRunAsClient(deployment, event.getTestClass().getJavaClass(), event.getTestMethod())) {
            return true; 
        } 
        else if(isLocalContainer(container))
        {
            return true;
        }
        return false;
    }

    /**
     * Check is this should run as client.
     * 
     * Verify @Deployment.testable vs @RunAsClient on Class or Method level 
     * 
     * @param deployment
     * @param testClass
     * @param testMethod
     * @return
     */
    private static boolean isRunAsClient(Deployment deployment, Class<?> testClass, Method testMethod)
    {
       boolean runAsClient = true;
       if(deployment != null)
       {
          runAsClient =  deployment.getDescription().testable() ? false:true;
          runAsClient =  deployment.isDeployed() ? runAsClient:true;
          
          if(testMethod.isAnnotationPresent(RunAsClient.class))
          {
             runAsClient = true;
          }
          else if(testClass.isAnnotationPresent(RunAsClient.class))
          {
             runAsClient = true;
          }
       }
       return runAsClient;
    }

    /**
     * Check if this Container DEFAULTs to the Local protocol. 
     * 
     * Hack to get around ARQ-391
     * 
     * @param container
     * @return true if DeployableContianer.getDefaultProtocol == Local
     */
    private static boolean isLocalContainer(Container container)
    {
       if(
             container == null || 
             container.getDeployableContainer() == null || 
             container.getDeployableContainer().getDefaultProtocol() == null)
       {
          return false;
       }
       if("Local".equals(container.getDeployableContainer().getDefaultProtocol().getName()))
       {
          return true;
       }
       return false;
    }
}
