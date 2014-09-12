package org.jboss.arquillian.extension.byteman.impl.client;

import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.core.spi.event.Event;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class AddressProvider {
    private static final ThreadLocal<AddressProvider> tl = new ThreadLocal<>();

    /**
     * Provide address, if possible.
     *
     * @return address or null
     */
    protected abstract String provide(Event event);

    /**
     * Extract address from ProtocolMetaData, if possible.
     *
     * @return address or null
     */
    protected abstract String extract(Event event, ProtocolMetaData pmd);

    public static void setExtractor(AddressProvider extractor) {
        tl.set(extractor);
    }

    public static String provideAddress(Event event) {
        AddressProvider provider = tl.get();
        return (provider != null) ? provider.provide(event) : null;
    }

    public static String extractAddress(Event event, ProtocolMetaData pmd) {
        AddressProvider provider = tl.get();
        return (provider != null) ? provider.extract(event, pmd) : null;
    }

    public static void removeExtractor() {
        tl.remove();
    }
}
