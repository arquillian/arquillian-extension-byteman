package org.jboss.arquillian.extension.byteman.impl.common;

import java.util.List;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class AbstractRuleInstaller {
    private static final Logger log = Logger.getLogger(AbstractRuleInstaller.class.getName());

    public static final String CLASS_KEY_PREFIX = "Class:";
    public static final String METHOD_KEY_PREFIX = "Method:";

    protected static void install(String prefix, String script, ExecContext context) {
        if (script != null) {
            try {
                SubmitUtil.install(generateKey(prefix), script, context);
            } catch (RuntimeException e) {
                log.severe(
                    String.format("Error installing '%s' script to %s:%s, exec %s, msg: %s", prefix, context.getAddress(),
                        context.getPort(), context.getExec(), e.getMessage()));
                throw e;
            }
        }
    }

    protected static void uninstall(String prefix, String script, ExecContext context) {
        if (script != null) {
            try {
                SubmitUtil.uninstall(generateKey(prefix), script, context);
            } catch (RuntimeException e) {
                log.severe(String.format("Error uninstalling '%s' script to  %s:%s, exec %s, msg: %s", prefix,
                    context.getAddress(), context.getPort(), context.getExec(), e.getMessage()));
                throw e;
            }
        }
    }

    protected static boolean isInstalled(String scriptName, ExecContext context) {
        List<String> scriptNames = SubmitUtil.listInstalled(context);
        return scriptNames.contains(generateKey(scriptName));
    }

    private static String generateKey(String prefix) {
        return prefix + Thread.currentThread().getName();
    }
}
