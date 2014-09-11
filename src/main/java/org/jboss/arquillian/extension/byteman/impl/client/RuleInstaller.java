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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.client.deployment.Deployment;
import org.jboss.arquillian.container.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.container.spi.client.protocol.metadata.JMXContext;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.spi.client.protocol.metadata.RMIContext;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.extension.byteman.api.ExecType;
import org.jboss.arquillian.extension.byteman.impl.common.AbstractRuleInstaller;
import org.jboss.arquillian.extension.byteman.impl.common.BytemanConfiguration;
import org.jboss.arquillian.extension.byteman.impl.common.ExecContext;
import org.jboss.arquillian.test.spi.event.suite.TestEvent;

/**
 * MethodRuleInstaller
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @version $Revision: $
 */
public class RuleInstaller extends AbstractRuleInstaller {
    @Inject
    private Instance<Deployment> deploymentInstance;

    @Inject
    private Instance<Container> containerInstance;

    @Inject
    private Instance<ArquillianDescriptor> descriptorInst;

    @Inject
    private Instance<ProtocolMetaData> protocolMetaDataInstance;

    @SuppressWarnings("deprecation")
    protected String readAddress() {
        ProtocolMetaData pmd = protocolMetaDataInstance.get();

        if (pmd == null) {
            // TODO -- bug in ARQ?
            return null;
        }

        HTTPContext httpContext = pmd.getContext(HTTPContext.class);
        if (httpContext != null) {
            return httpContext.getHost();
        }

        RMIContext rmiContext = pmd.getContext(RMIContext.class);
        if (rmiContext != null) {
            return rmiContext.getIp();
        }

        JMXContext jmxContext = pmd.getContext(JMXContext.class);
        if (jmxContext != null) {
            try {
                Field hostField = JMXContext.class.getDeclaredField("host");
                hostField.setAccessible(true);
                return (String) hostField.get(jmxContext);
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    protected List<ExecContext> getExecContexts() {
        BytemanConfiguration config = BytemanConfiguration.from(descriptorInst.get());
        List<ExecContext> list = new ArrayList<ExecContext>();
        if (config.clientAgentPort() == config.containerAgentPort()) {
            list.add(new ExecContext(config.clientAgentPort(), EnumSet.complementOf(EnumSet.of(ExecType.CONTAINER))));
        } else {
            list.add(new ExecContext(config.clientAgentPort(), EnumSet.complementOf(EnumSet.of(ExecType.CONTAINER, ExecType.CLIENT_CONTAINER))));
            String address = readAddress();
            ExecContext remote = (address != null) ? new ExecContext(address, config.containerAgentPort(), EnumSet.of(ExecType.CLIENT_CONTAINER)) : new ExecContext(config.containerAgentPort(), EnumSet.of(ExecType.CLIENT_CONTAINER));
            list.add(remote);
        }
        return list;
    }

    protected boolean shouldRun(TestEvent event) {
        return shouldRun(deploymentInstance.get(), containerInstance.get(), event);
    }

    private static boolean shouldRun(Deployment deployment, Container container, TestEvent event) {
        if (isRunAsClient(deployment, event.getTestClass().getJavaClass(), event.getTestMethod())) {
            return true;
        } else if (isLocalContainer(container)) {
            return true;
        }
        return false;
    }

    /**
     * Check is this should run as client.
     * <p/>
     * Verify @Deployment.testable vs @RunAsClient on Class or Method level
     */
    private static boolean isRunAsClient(Deployment deployment, Class<?> testClass, Method testMethod) {
        boolean runAsClient = true;
        if (deployment != null) {
            runAsClient = !deployment.getDescription().testable();
            runAsClient = !deployment.isDeployed() || runAsClient;

            if (testMethod.isAnnotationPresent(RunAsClient.class)) {
                runAsClient = true;
            } else if (testClass.isAnnotationPresent(RunAsClient.class)) {
                runAsClient = true;
            }
        }
        return runAsClient;
    }

    /**
     * Check if this Container DEFAULTs to the Local protocol.
     * <p/>
     * Hack to get around ARQ-391
     *
     * @param container the container
     * @return true if DeployableContianer.getDefaultProtocol == Local
     */
    private static boolean isLocalContainer(Container container) {
        if (
            container == null ||
                container.getDeployableContainer() == null ||
                container.getDeployableContainer().getDefaultProtocol() == null) {
            return false;
        }
        return "Local".equals(container.getDeployableContainer().getDefaultProtocol().getName());
    }
}
