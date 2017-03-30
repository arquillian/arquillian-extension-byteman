package org.jboss.arquillian.extension.byteman.impl.common;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.extension.byteman.api.ExecType;
import org.jboss.arquillian.extension.byteman.impl.client.AddressProvider;
import org.jboss.arquillian.test.spi.event.suite.ClassLifecycleEvent;
import org.jboss.arquillian.test.spi.event.suite.TestLifecycleEvent;
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

    private final Set<ExecType> matched = new HashSet<>();

    public ExecContext(int port, EnumSet<ExecType> exec, BytemanConfiguration configuration) {
        this(Submit.DEFAULT_ADDRESS, port, exec, configuration);
    }

    public ExecContext(String address, int port, EnumSet<ExecType> exec, BytemanConfiguration configuration) {
        this.address = address;
        this.port = port;
        this.exec = exec;
        this.configuration = configuration;
    }

    public boolean match(ExecType execType) {
        if (getExec().contains(execType)) {
            matched.add(execType);
            return true;
        } else {
            return false;
        }
    }

    @SuppressWarnings("UnusedParameters")
    public void validate(ClassLifecycleEvent event) {
        if (matched.contains(ExecType.CLIENT_CONTAINER) && (AddressProvider.provideAddress(event) == null)) {
            log.warning(
                String.format("Can only handle %s container agent address; no %s available.", Submit.DEFAULT_ADDRESS,
                    ProtocolMetaData.class.getSimpleName()));
        }
    }

    @SuppressWarnings("UnusedParameters")
    public void validate(TestLifecycleEvent event) {
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
