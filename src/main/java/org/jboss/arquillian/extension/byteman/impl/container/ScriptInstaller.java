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
package org.jboss.arquillian.extension.byteman.impl.container;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.extension.byteman.impl.common.BytemanConfiguration;
import org.jboss.arquillian.extension.byteman.impl.common.GenerateScriptUtil;
import org.jboss.arquillian.extension.byteman.impl.common.SubmitException;
import org.jboss.arquillian.test.spi.event.suite.AfterSuite;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;
import org.jboss.byteman.agent.submit.ScriptText;
import org.jboss.byteman.agent.submit.Submit;

/**
 * ScriptInstaller
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ScriptInstaller
{
   public void install(@Observes BeforeSuite event)
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      InputStream scriptStream = cl.getResourceAsStream(BytemanConfiguration.BYTEMAN_SCRIPT);

      try
      {
         if(scriptStream != null && scriptStream.available() > 0)
         {
            String ruleKey = Thread.currentThread().getName();
            String ruleScript = GenerateScriptUtil.toString(scriptStream);
            try
            {
               Submit submit = new Submit();
               submit.addScripts(Arrays.asList(new ScriptText(ruleKey, ruleScript)));
            }
            catch (Exception e)
            {
               throw new SubmitException("Could not install script from file", e);
            }
         }
      }
      catch (IOException e)
      {
         throw new RuntimeException("Could not check stream", e);
      }
   }

   public void uninstall(@Observes AfterSuite event)
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      InputStream scriptStream = cl.getResourceAsStream(BytemanConfiguration.BYTEMAN_SCRIPT);

      if(scriptStream != null)
      {
         String ruleKey = Thread.currentThread().getName();
         String ruleScript = GenerateScriptUtil.toString(scriptStream);
         try
         {
            Submit submit = new Submit();
            submit.deleteScripts(Arrays.asList(new ScriptText(ruleKey, ruleScript)));
         }
         catch (Exception e)
         {
            throw new SubmitException("Could not uninstall script from file", e);
         }
      }
   }
}

