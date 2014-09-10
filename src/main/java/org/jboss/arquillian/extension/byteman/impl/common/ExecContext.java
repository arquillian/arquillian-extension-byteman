package org.jboss.arquillian.extension.byteman.impl.common;

import java.util.EnumSet;

import org.jboss.arquillian.extension.byteman.api.ExecType;
import org.jboss.byteman.agent.submit.Submit;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class ExecContext {
    private String address = Submit.DEFAULT_ADDRESS;
    private int port;
    private EnumSet<ExecType> exec;

    public ExecContext(int port, EnumSet<ExecType> exec) {
        this.port = port;
        this.exec = exec;
    }

    public ExecContext(String address, int port, EnumSet<ExecType> exec) {
        this.address = address;
        this.port = port;
        this.exec = exec;
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
}
