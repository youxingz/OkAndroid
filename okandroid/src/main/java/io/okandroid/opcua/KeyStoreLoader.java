package io.okandroid.opcua;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateBuilder;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.regex.Pattern;

public class KeyStoreLoader {
    private static final Pattern IP_ADDR_PATTERN = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    private static final String CLIENT_ALIAS = "client-ai";
    private static final char[] PASSWORD = "password".toCharArray();

    private final static String TAG = "io.opcua_c.KeyStoreLoader";

    private X509Certificate[] clientCertificateChain;
    private X509Certificate clientCertificate;
    private KeyPair clientKeyPair;

    KeyStoreLoader load(String baseDir) throws Exception {
        java.security.KeyStore keyStore = java.security.KeyStore.getInstance("PKCS12");

        String serverKeyStore = baseDir + "/opcua-client.pfx";

        //        Log.i(TAG, String.format("Loading KeyStore at %s", serverKeyStore));
        File keyFile = new File(serverKeyStore);
        if (!keyFile.exists()) {
            keyStore.load(null, PASSWORD);

            KeyPair keyPair = SelfSignedCertificateGenerator.generateRsaKeyPair(2048);

            SelfSignedCertificateBuilder builder = new SelfSignedCertificateBuilder(keyPair).setCommonName("Eclipse Milo Example Client").setOrganization("digitalpetri").setOrganizationalUnit("dev").setLocalityName("Folsom").setStateName("CA").setCountryCode("US").setApplicationUri("urn:eclipse:milo:examples:client").addDnsName("localhost").addIpAddress("127.0.0.1");

            // Get as many hostnames and IP addresses as we can listed in the certificate.
            // for (String hostname : HostnameUtil.getHostnames("0.0.0.0")) {
            //     if (IP_ADDR_PATTERN.matcher(hostname).matches()) {
            //         builder.addIpAddress(hostname);
            //     } else {
            //         builder.addDnsName(hostname);
            //     }
            // }
            builder.addIpAddress("10.168.1.9");

            X509Certificate certificate = builder.build();

            keyStore.setKeyEntry(CLIENT_ALIAS, keyPair.getPrivate(), PASSWORD, new X509Certificate[]{certificate});
            try (OutputStream out = new FileOutputStream(keyFile)) {
                keyStore.store(out, PASSWORD);
            }
        } else {
            try (InputStream in = new FileInputStream(keyFile)) {
                keyStore.load(in, PASSWORD);
            }
        }

        Key clientPrivateKey = keyStore.getKey(CLIENT_ALIAS, PASSWORD);
        if (clientPrivateKey instanceof PrivateKey) {
            clientCertificate = (X509Certificate) keyStore.getCertificate(CLIENT_ALIAS);

            clientCertificateChain = Arrays.stream(keyStore.getCertificateChain(CLIENT_ALIAS)).map(X509Certificate.class::cast).toArray(X509Certificate[]::new);

            PublicKey serverPublicKey = clientCertificate.getPublicKey();
            clientKeyPair = new KeyPair(serverPublicKey, (PrivateKey) clientPrivateKey);
        }

        return this;
    }

    X509Certificate getClientCertificate() {
        return clientCertificate;
    }

    public X509Certificate[] getClientCertificateChain() {
        return clientCertificateChain;
    }

    KeyPair getClientKeyPair() {
        return clientKeyPair;
    }
}
