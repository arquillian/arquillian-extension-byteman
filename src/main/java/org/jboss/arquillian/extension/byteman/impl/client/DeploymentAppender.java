/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
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

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.extension.byteman.api.BMRule;
import org.jboss.arquillian.extension.byteman.api.BMRules;
import org.jboss.arquillian.extension.byteman.api.ExecType;
import org.jboss.arquillian.extension.byteman.impl.BytemanRemoteExtension;
import org.jboss.arquillian.extension.byteman.impl.common.BytemanConfiguration;
import org.jboss.arquillian.extension.byteman.impl.common.GenerateScriptUtil;
import org.jboss.arquillian.extension.byteman.impl.common.SubmitException;
import org.jboss.byteman.agent.submit.ScriptText;
import org.jboss.byteman.agent.submit.Submit;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ArchiveAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * BytemanDeploymentAppender
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class DeploymentAppender implements AuxiliaryArchiveAppender {
    @Inject
    private Instance<ArquillianDescriptor> descriptorInst;

    @Override
    public Archive<?> createAuxiliaryArchive() {
        BytemanConfiguration config = BytemanConfiguration.from(descriptorInst.get());

        JavaArchive jar = ShrinkWrap.create(JavaArchive.class, "arquillian-byteman.jar")
            .addClasses(Submit.class, ScriptText.class, BMRule.class, BMRules.class, ExecType.class,
                BytemanRemoteExtension.class, GenerateScriptUtil.class, SubmitException.class, BytemanConfiguration.class)
            .addPackages(false,
                org.jboss.arquillian.extension.byteman.impl.container.ScriptInstaller.class.getPackage(),
                org.jboss.arquillian.extension.byteman.impl.common.BytemanConfiguration.class.getPackage())
            .addAsServiceProvider(RemoteLoadableExtension.class, BytemanRemoteExtension.class);

        jar.addAsResource(new StringAsset(config.toString()), BytemanConfiguration.BYTEMAN_CONFIG);

        if (config.autoInstallAgent()) {
            JavaArchive agentJar = ShrinkWrap.create(JavaArchive.class)
                .addPackages(true, "org.jboss.byteman")
                .setManifest(
                    new StringAsset("Manifest-Version: 1.0\n"
                        + "Created-By: Arquillian\n"
                        + "Implementation-Version: 0.0.0.Arq\n"
                        + "Premain-Class: org.jboss.byteman.agent.Main\n"
                        + "Agent-Class: org.jboss.byteman.agent.Main\n"
                        + "Can-Redefine-Classes: true\n"
                        + "Can-Retransform-Classes: true\n"));

            // add byteman archive as a resource in the jar, needed to install
            jar.add(new ArchiveAsset(agentJar, ZipExporter.class), BytemanConfiguration.BYTEMAN_JAR);
        }
        return jar;
    }
}