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
package org.jboss.arquillian.extension.byteman.impl.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.api.ExtensionDef;

/**
 * BytemanConfiguration
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class BytemanConfiguration
{
    public static String BYTEMAN_JAR = "byteman.jar";
    public static String BYTEMAN_SCRIPT = "byteman.script";
    public static String BYTEMAN_CONFIG = "byteman-arquillian.properties";
    public static String BYTEMAN_EXTENSION_NAME = "byteman";
    public static String BYTEMAN_AUTO_INSTALL_AGENT = "autoInstallAgent";
    public static String BYTEMAN_AGENT_PROPERTIES = "agentProperties";


    private Map<String, String> properties;

    public BytemanConfiguration(Map<String, String> properties)
    {
        this.properties = properties;
    }

    public boolean autoInstallAgent()
    {
        return Boolean.parseBoolean(properties.get(BYTEMAN_AUTO_INSTALL_AGENT));
    }

    public String agentProperties()
    {
        return properties.get(BYTEMAN_AGENT_PROPERTIES);
    }

    @Override
    public String toString()
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try
        {
            Properties tmp = new Properties();
            tmp.putAll(properties);
            tmp.store(output, "byteman-auto-added");
        }
        catch (IOException e)
        {
            // no-op, what could possible go wrong ? ;)
        }
        return output.toString();
    }

    public static BytemanConfiguration from(ArquillianDescriptor descriptor)
    {
        return new BytemanConfiguration(locateBytemanExtension(descriptor));
    }

    public static BytemanConfiguration from(InputStream inputStream)
    {
        return from(IOUtil.asUTF8String(inputStream));
    }

    public static BytemanConfiguration from(String properties)
    {
        return new BytemanConfiguration(loadPropertiesString(properties));
    }

    private static Map<String, String> locateBytemanExtension(ArquillianDescriptor descriptor)
    {
        if(descriptor != null)
        {
            for(ExtensionDef extension : descriptor.getExtensions())
            {
                if(BYTEMAN_EXTENSION_NAME.equalsIgnoreCase(extension.getExtensionName()))
                {
                    return extension.getExtensionProperties();
                }
            }
        }
        return new HashMap<String, String>();
    }

    private static Map<String, String> loadPropertiesString(String properties)
    {
        Map<String, String> result = new HashMap<String, String>();

        Properties props = new Properties();
        try
        {
            props.load(new StringReader(properties));
        }
        catch (IOException e)
        {
            // no-op, no IOException in StringReader
        }
        for(Map.Entry<Object, Object> entry : props.entrySet())
        {
            result.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
        }
        return result;
    }
}
