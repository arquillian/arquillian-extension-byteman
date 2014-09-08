package org.jboss.arquillian.extension.byteman.impl.common;

import java.util.EnumSet;
import java.util.Map;
import java.util.logging.Logger;

import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.extension.byteman.api.ExecType;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jboss.arquillian.test.spi.event.suite.TestEvent;

/**
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class AbstractRuleInstaller {
    private static final Logger log = Logger.getLogger(AbstractRuleInstaller.class.getName());

    public static final String CLASS_KEY_PREFIX = "Class:";
    public static final String METHOD_KEY_PREFIX = "Method:";

    protected abstract Map<Integer, EnumSet<ExecType>> getExecMap();

    private static void install(String prefix, String script, int port, EnumSet<ExecType> match) {
        if (script != null) {
            try {
                SubmitUtil.install(generateKey(prefix), script, port);
            } catch (RuntimeException e) {
                log.severe(String.format("Error installing '%s' script to port %s, exec %s, msg: %s", prefix, port, match, e.getMessage()));
                throw e;
            }
        }
    }

    private static void uninstall(String prefix, String script, int port, EnumSet<ExecType> match) {
        if (script != null) {
            try {
                SubmitUtil.uninstall(generateKey(prefix), script, port);
            } catch (RuntimeException e) {
                log.severe(String.format("Error uninstalling '%s' script to port %s, exec %s, msg: %s", prefix, port, match, e.getMessage()));
                throw e;
            }
        }
    }

    public void installClass(@Observes BeforeClass event) {
        for (Map.Entry<Integer, EnumSet<ExecType>> entry : getExecMap().entrySet()) {
            String script = ExtractScriptUtil.extract(entry.getValue(), event);
            install(CLASS_KEY_PREFIX, script, entry.getKey(), entry.getValue());
        }
    }

    public void uninstallClass(@Observes AfterClass event) {
        for (Map.Entry<Integer, EnumSet<ExecType>> entry : getExecMap().entrySet()) {
            String script = ExtractScriptUtil.extract(entry.getValue(), event);
            uninstall(generateKey(CLASS_KEY_PREFIX), script, entry.getKey(), entry.getValue());
        }
    }

    protected abstract boolean shouldRun(TestEvent event);

    public void installMethod(@Observes Before event) {
        if (shouldRun(event) == false) {
            return;
        }

        for (Map.Entry<Integer, EnumSet<ExecType>> entry : getExecMap().entrySet()) {
            String script = ExtractScriptUtil.extract(entry.getValue(), event);
            install(METHOD_KEY_PREFIX, script, entry.getKey(), entry.getValue());
        }
    }

    public void uninstallMethod(@Observes After event) {
        if (shouldRun(event) == false) {
            return;
        }

        for (Map.Entry<Integer, EnumSet<ExecType>> entry : getExecMap().entrySet()) {
            String script = ExtractScriptUtil.extract(entry.getValue(), event);
            uninstall(METHOD_KEY_PREFIX, script, entry.getKey(), entry.getValue());
        }
    }

    private static String generateKey(String prefix) {
        return prefix + Thread.currentThread().getName();
    }
}
