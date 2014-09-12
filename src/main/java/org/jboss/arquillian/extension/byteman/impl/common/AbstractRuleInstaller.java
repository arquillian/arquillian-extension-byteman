package org.jboss.arquillian.extension.byteman.impl.common;

import java.util.List;
import java.util.logging.Logger;

import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.event.Event;
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

    protected abstract List<ExecContext> getExecContexts(Event event);

    private static void install(String prefix, String script, ExecContext context) {
        if (script != null) {
            try {
                SubmitUtil.install(generateKey(prefix), script, context);
            } catch (RuntimeException e) {
                log.severe(String.format("Error installing '%s' script to %s:%s, exec %s, msg: %s", prefix, context.getAddress(), context.getPort(), context.getExec(), e.getMessage()));
                throw e;
            }
        }
    }

    private static void uninstall(String prefix, String script, ExecContext context) {
        if (script != null) {
            try {
                SubmitUtil.uninstall(generateKey(prefix), script, context);
            } catch (RuntimeException e) {
                log.severe(String.format("Error uninstalling '%s' script to  %s:%s, exec %s, msg: %s", prefix, context.getAddress(), context.getPort(), context.getExec(), e.getMessage()));
                throw e;
            }
        }
    }

    public void installClass(@Observes BeforeClass event) {
        for (ExecContext context : getExecContexts(event)) {
            String script = ExtractScriptUtil.extract(context, event);
            install(CLASS_KEY_PREFIX, script, context);
        }
    }

    public void uninstallClass(@Observes AfterClass event) {
        for (ExecContext context : getExecContexts(event)) {
            String script = ExtractScriptUtil.extract(context, event);
            uninstall(generateKey(CLASS_KEY_PREFIX), script, context);
        }
    }

    protected abstract boolean shouldRun(TestEvent event);

    public void installMethod(@Observes Before event) {
        if (shouldRun(event) == false) {
            return;
        }

        for (ExecContext context : getExecContexts(event)) {
            String script = ExtractScriptUtil.extract(context, event);
            install(METHOD_KEY_PREFIX, script, context);
        }
    }

    public void uninstallMethod(@Observes After event) {
        if (shouldRun(event) == false) {
            return;
        }

        for (ExecContext context : getExecContexts(event)) {
            String script = ExtractScriptUtil.extract(context, event);
            uninstall(METHOD_KEY_PREFIX, script, context);
        }
    }

    private static String generateKey(String prefix) {
        return prefix + Thread.currentThread().getName();
    }
}
