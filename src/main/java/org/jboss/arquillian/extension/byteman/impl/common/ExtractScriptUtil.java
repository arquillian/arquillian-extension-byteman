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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.jboss.arquillian.extension.byteman.api.BMRule;
import org.jboss.arquillian.extension.byteman.api.BMRules;
import org.jboss.arquillian.extension.byteman.api.ExecType;
import org.jboss.arquillian.test.spi.event.suite.ClassLifecycleEvent;
import org.jboss.arquillian.test.spi.event.suite.TestLifecycleEvent;

/**
 * ExtractScriptUtil
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @version $Revision: $
 */
public final class ExtractScriptUtil {
    public static String extract(EnumSet<ExecType> match, ClassLifecycleEvent event) {
        BMRule rule = event.getTestClass().getAnnotation(BMRule.class);
        BMRules rules = event.getTestClass().getAnnotation(BMRules.class);

        return createRules(match, rule, rules);
    }

    public static String extract(EnumSet<ExecType> match, TestLifecycleEvent event) {
        BMRule rule = event.getTestMethod().getAnnotation(BMRule.class);
        BMRules rules = event.getTestMethod().getAnnotation(BMRules.class);

        return createRules(match, rule, rules);
    }

    private static String createRules(EnumSet<ExecType> match, BMRule rule, BMRules rules) {
        if (rule != null || rules != null) {
            List<BMRule> bmRules = toRuleList(match, rule, rules);
            if (bmRules.size() > 0) {
                return GenerateScriptUtil.constructScriptText(bmRules.toArray(new BMRule[bmRules.size()]));
            }
        }
        return null;
    }

    private static List<BMRule> toRuleList(EnumSet<ExecType> match, BMRule rule, BMRules rules) {
        List<BMRule> bmRules = new ArrayList<BMRule>();
        if (rule != null) {
            checkRule(match, bmRules, rule);
        } else {
            for (BMRule bmr : rules.value()) {
                checkRule(match, bmRules, bmr);
            }
        }
        return bmRules;
    }

    private static void checkRule(EnumSet<ExecType> match, List<BMRule> bmRules, BMRule rule) {
        if (match.contains(rule.exec())) {
            bmRules.add(rule);
        }
    }
}
