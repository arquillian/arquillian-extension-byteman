/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.extension.byteman.impl.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generic input/output utilities
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public final class IOUtil {

    // -------------------------------------------------------------------------------------||
    // Class Members ----------------------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    /**
     * Logger
     */
    private static final Logger log = Logger.getLogger(IOUtil.class.getName());

    /**
     * Name of UTF-8 Charset
     */
    private static final String CHARSET_UTF8 = "UTF-8";

    // -------------------------------------------------------------------------------------||
    // Constructor ------------------------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    /**
     * Internal constructor; should not be called
     */
    private IOUtil() {
        throw new UnsupportedOperationException("No instances should be created; stateless class");
    }

    // -------------------------------------------------------------------------------------||
    // Required Implementations -----------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    /**
     * Obtains the contents of the specified stream
     * as a String in UTF-8 charset.
     *
     * @throws IllegalArgumentException
     *     If the stream was not specified
     */
    public static String asUTF8String(InputStream in) {
        StringBuilder buffer = new StringBuilder();
        String line;

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, CHARSET_UTF8));
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append('\n');
            }
        } catch (IOException ioe) {
            throw new RuntimeException("Error in obtaining string from " + in, ioe);
        } finally {
            try {
                in.close();
            } catch (IOException ignore) {
                if (log.isLoggable(Level.FINER)) {
                    log.finer("Could not close stream due to: " + ignore.getMessage() + "; ignoring");
                }
            }
        }

        return buffer.toString();
    }
}