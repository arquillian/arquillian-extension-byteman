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
package org.jboss.arquillian.extension.byteman.test;

import javax.ejb.EJB;
import javax.ejb.EJBException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OverProtocol;
import org.jboss.arquillian.extension.byteman.api.BMRule;
import org.jboss.arquillian.extension.byteman.api.BMRules;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test Case for {@link BMRule} on Method level
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(Arquillian.class)
@BMRules(
        @BMRule(
                name = "Throw exception on success", targetClass = "StatelessManagerBean", targetMethod = "forcedClassLevelFailure", 
                action = "throw new java.lang.RuntimeException()")
)
public class BytemanFaultInjectionTestCase {

    @Deployment @OverProtocol("Servlet 3.0")
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(StatelessManager.class, StatelessManagerBean.class);
    }

    @EJB(mappedName = "java:module/StatelessManagerBean")
    private StatelessManager bean;

    @Test(expected = EJBException.class)
    @BMRule(
            name = "Throw exception on success", targetClass = "StatelessManagerBean", targetMethod = "forcedMethodLevelFailure", 
            action = "throw new java.lang.RuntimeException()")
    public void shouldBeAbleToInjectMethodLevelThrowRule()
    {
        Assert.assertNotNull("Verify bean was injected", bean);
        bean.forcedMethodLevelFailure();
    }

    @Test(expected = EJBException.class) @InSequence(2)
    public void shouldBeAbleToInjectClassLevelThrowRule()
    {
        Assert.assertNotNull("Verify bean was injected", bean);

        try
        {
            bean.forcedMethodLevelFailure();
        }
        catch (Exception e)
        {
            Assert.fail("No method level rule should be active");
        }

        bean.forcedClassLevelFailure();
    }
}