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
package org.jboss.arquillian.extension.byteman.test.container;

import javax.ejb.EJB;
import javax.ejb.EJBException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OverProtocol;
import org.jboss.arquillian.extension.byteman.api.BMRule;
import org.jboss.arquillian.extension.byteman.api.BMRules;
import org.jboss.arquillian.extension.byteman.test.model.StatelessManager;
import org.jboss.arquillian.extension.byteman.test.model.StatelessManagerBean;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test Case for {@link BMRule} with binding
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @version $Revision: $
 */
@RunWith(Arquillian.class)
@BMRules({
    @BMRule(name = "Create counter for StatelessManager", targetClass = "StatelessManagerBean", targetMethod = "<init>", binding = "bean = $this", action = "createCountDown(bean,1)"),
    @BMRule(name = "Fail on second call", targetClass = "StatelessManagerBean", targetMethod = "forcedClassLevelFailure", binding = "bean = $this", condition = "countDown(bean)", action = "throw new RuntimeException(\"Second call throws exception\")")})
public class BindingTestCase {

    @Deployment
    @OverProtocol("Servlet 3.0")
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class).addClasses(StatelessManager.class, StatelessManagerBean.class);
    }

    @EJB(mappedName = "java:module/StatelessManagerBean")
    private StatelessManager bean;

    @Test
    @InSequence(1)
    public void shouldNotFailForFirstInvocation() {
        bean.forcedClassLevelFailure();
    }

    @Test(expected = EJBException.class)
    @InSequence(2)
    public void shouldFailForSecondInvocation() throws Throwable {
        bean.forcedClassLevelFailure();
    }
}
