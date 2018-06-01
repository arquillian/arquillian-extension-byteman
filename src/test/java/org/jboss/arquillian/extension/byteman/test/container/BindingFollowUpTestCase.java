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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OverProtocol;
import org.jboss.arquillian.extension.byteman.api.BMRule;
import org.jboss.arquillian.extension.byteman.api.BMRules;
import org.jboss.arquillian.extension.byteman.test.model.StatelessManager;
import org.jboss.arquillian.extension.byteman.test.model.StatelessManagerBean;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * <p>
 * This testcase is expected to be run before {@link BindingTestCase}.
 * <p>
 * It checks that rules are removed after class finishes.<br>
 * Before the rules were removed each time test method finished.
 * The fixed behavior uses client call to remove class level BMRules
 * instead of the container one.
 * <p>
 * That's why it's important to use the same names in the rules as
 * the following testcase uses.
 *
 * @author <a href="mailto:ochaloup@redhat.com">Ondra Chaloupka</a>
 */
@RunWith(Arquillian.class)
@BMRules({
    @BMRule(name = BindingTestCase.CREATE_COUNTER_RULE, targetClass = "StatelessManagerBean", targetMethod = "<init>",
        action = "createCountDown(\"" + BindingTestCase.COUNTDOWN_NAME + "\",1)"),
    @BMRule(name = BindingTestCase.THROW_EXCEPTION_RULE, targetClass = "StatelessManagerBean", targetMethod = "bindingCountdownFailure",
        condition = "countDown(\"" + BindingTestCase.COUNTDOWN_NAME + "\")",
        action = "throw new RuntimeException(\"Second call throws exception\")")})
public class BindingFollowUpTestCase {

    @Deployment
    @OverProtocol("Servlet 3.0")
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class).addClasses(StatelessManager.class, StatelessManagerBean.class);
    }

    @EJB(mappedName = "java:module/StatelessManagerBean")
    private StatelessManager bean;

    @Test
    public void shouldNotFailForFirstInvocation() {
        bean.bindingCountdownFailure();
    }
}
