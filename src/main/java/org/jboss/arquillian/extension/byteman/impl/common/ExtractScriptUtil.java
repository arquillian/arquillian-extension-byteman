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
    public static String extract(ExecContext context, ClassLifecycleEvent event) {
        BMRule rule = event.getTestClass().getAnnotation(BMRule.class);
        BMRules rules = event.getTestClass().getAnnotation(BMRules.class);

        return createRules(context, rule, rules);
    }

    public static String extract(ExecContext context, TestLifecycleEvent event) {
        BMRule rule = event.getTestMethod().getAnnotation(BMRule.class);
        BMRules rules = event.getTestMethod().getAnnotation(BMRules.class);

        return createRules(context, rule, rules);
    }

    private static String createRules(ExecContext context, BMRule rule, BMRules rules) {
        if (rule != null || rules != null) {
            List<BMRule> bmRules = toRuleList(context, rule, rules);
            if (bmRules.size() > 0) {
                return GenerateScriptUtil.constructScriptText(bmRules.toArray(new BMRule[bmRules.size()]));
            }
        }
        return null;
    }

    private static List<BMRule> toRuleList(ExecContext context, BMRule rule, BMRules rules) {
        List<BMRule> bmRules = new ArrayList<BMRule>();
        if (rule != null) {
            checkRule(context, bmRules, rule);
        } else {
            for (BMRule bmr : rules.value()) {
                checkRule(context, bmRules, bmr);
            }
        }
        return bmRules;
    }

    private static void checkRule(ExecContext context, List<BMRule> bmRules, BMRule rule) {
        EnumSet<ExecType> match = context.getExec();
        ExecType type = rule.exec();

        if (match.contains(type)) {
            context.validate(type);
            bmRules.add(rule);
        }
    }
}
