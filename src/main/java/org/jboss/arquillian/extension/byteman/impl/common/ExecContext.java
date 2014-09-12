package org.jboss.arquillian.extension.byteman.impl.common;

import java.util.EnumSet;
import java.util.logging.Logger;

import org.jboss.arquillian.extension.byteman.api.ExecType;
import org.jboss.byteman.agent.submit.Submit;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class ExecContext {
    private static final Logger log = Logger.getLogger(ExecContext.class.getName());

    private final String address;
    private final int port;
    private final EnumSet<ExecType> exec;

    private final BytemanConfiguration configuration;

    public ExecContext(int port, EnumSet<ExecType> exec, BytemanConfiguration configuration) {
        this(Submit.DEFAULT_ADDRESS, port, exec, configuration);
    }

    public ExecContext(String address, int port, EnumSet<ExecType> exec, BytemanConfiguration configuration) {
        this.address = address;
        this.port = port;
        this.exec = exec;
        this.configuration = configuration;
    }

    public void validate(ExecType execType) {
        if (execType == ExecType.CLIENT_CONTAINER && (configuration != null && configuration.autoInstallAgent())) {
            log.warning(String.format("Using %s with autoInstallAgent -- is container agent running?", execType));
        }
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public EnumSet<ExecType> getExec() {
        return exec;
    }

    public BytemanConfiguration getConfiguration() {
        return configuration;
    }
}
