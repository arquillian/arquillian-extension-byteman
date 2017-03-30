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

import java.net.HttpURLConnection;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.extension.byteman.api.BMRule;
import org.jboss.arquillian.extension.byteman.api.ExecType;
import org.jboss.arquillian.extension.byteman.test.model.AccountService;
import org.jboss.arquillian.extension.byteman.test.model.AccountServlet;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/*
 * This tests depends on some other in-container test running first to install the agent.
 * See pom.xml surefire runOrder.
 */
@RunWith(Arquillian.class)
public class ContainerFromClientTestCase {

    @Deployment(testable = false)
    private static WebArchive deploy() {
        return ShrinkWrap.create(WebArchive.class)
            .addClasses(AccountService.class, AccountServlet.class)
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @ArquillianResource
    private URL baseURL;

    @Test
    public void shouldBeAllOk() throws Exception {
        HttpURLConnection con = (HttpURLConnection) new URL(baseURL, "account").openConnection();
        Assert.assertEquals(200, con.getResponseCode());
    }

    @Test
    @BMRule(
        name = "Throw exception on success", targetClass = "AccountService", targetMethod = "forcedMethodLevelFailure",
        action = "throw new java.lang.RuntimeException()",
        exec = ExecType.CLIENT_CONTAINER)
    public void shouldForceFailure() throws Exception {
        HttpURLConnection con = (HttpURLConnection) new URL(baseURL, "account").openConnection();
        Assert.assertEquals(500, con.getResponseCode());
    }
}
