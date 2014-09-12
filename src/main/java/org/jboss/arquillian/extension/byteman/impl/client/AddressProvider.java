package org.jboss.arquillian.extension.byteman.impl.client;

import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;

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
    protected abstract String provide();

    /**
     * Extract address from ProtocolMetaData, if possible.
     *
     * @return address or null
     */
    protected abstract String extract(ProtocolMetaData pmd);

    public static void setExtractor(AddressProvider extractor) {
        tl.set(extractor);
    }

    public static String provideAddress() {
        AddressProvider provider = tl.get();
        return (provider != null) ? provider.provide() : null;
    }

    public static String extractAddress(ProtocolMetaData pmd) {
        AddressProvider provider = tl.get();
        return (provider != null) ? provider.extract(pmd) : null;
    }

    public static void removeExtractor() {
        tl.remove();
    }
}
