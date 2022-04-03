package org.gephi.branding.desktop;

import java.io.InputStream;
import java.security.KeyStore;
import org.netbeans.spi.autoupdate.KeyStoreProvider;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = KeyStoreProvider.class)
public class GephiKeyStoreProvider implements KeyStoreProvider {

    private static final String KS_RESOURCE_PATH = "/keystore/truststore.ks";
    private static final String KS_DEFAULT_PASSWORD = "open4all";

    @Override
    public KeyStore getKeyStore() {
        try (InputStream inputStream = getClass().getResourceAsStream(KS_RESOURCE_PATH)) {
            KeyStore keyStore;
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(inputStream, KS_DEFAULT_PASSWORD.toCharArray());
            return keyStore;
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

}