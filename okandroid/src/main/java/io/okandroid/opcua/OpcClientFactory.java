package io.okandroid.opcua;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.UsernameProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.X509IdentityProvider;
import org.eclipse.milo.opcua.stack.client.security.DefaultClientCertificateValidator;
import org.eclipse.milo.opcua.stack.core.Stack;
import org.eclipse.milo.opcua.stack.core.security.DefaultTrustListManager;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;

import java.io.File;
import java.security.Security;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RequiresApi(api = Build.VERSION_CODES.N)
public class OpcClientFactory {
    static {
        // Required for SecurityPolicy.Aes256_Sha256_RsaPss
        Security.addProvider(new BouncyCastleProvider());
    }

    private final static String TAG = "OpcClient";

    private final CompletableFuture<OpcUaClient> future = new CompletableFuture<>();
    private static DefaultTrustListManager trustListManager;
    private Runner runner;

    public OpcClientFactory(Runner runner) {
        this.runner = runner;
    }

    public void start() {
        runClient();
    }

    /**
     * @param url "opc.tcp://10.168.1.9:4840"
     * @return
     * @throws Exception
     */
    public static OpcUaClient createClient(String url) throws Exception {
        String securityTempDir = System.getProperty("java.io.tmpdir") + "/client/security";
        File dir = new File(securityTempDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (!dir.exists()) {
            throw new Exception("unable to create security dir: " + securityTempDir);
        }

        File pkiDir = new File(securityTempDir + "/pki");

        Log.i(TAG, String.format("security dir: %s", securityTempDir));
        Log.i(TAG, String.format("security pki dir: %s", pkiDir.getAbsolutePath()));

        KeyStoreLoader loader = new KeyStoreLoader().load(securityTempDir);

        trustListManager = new DefaultTrustListManager(pkiDir);

        DefaultClientCertificateValidator certificateValidator =
                new DefaultClientCertificateValidator(trustListManager);

        return OpcUaClient.create(
                url,
                endpoints -> endpoints.stream().findFirst(),
                configBuilder -> configBuilder
                        .setApplicationName(LocalizedText.english("eclipse milo opc-ua client"))
                        .setApplicationUri("urn:eclipse:milo:examples:client")
                        .setKeyPair(loader.getClientKeyPair())
                        .setCertificate(loader.getClientCertificate())
                        .setCertificateChain(loader.getClientCertificateChain())
                        .setCertificateValidator(certificateValidator)
                        .setIdentityProvider(new AnonymousProvider()) // 匿名用户
                        .setRequestTimeout(uint(5000))
                        .build()
        );
    }

    private void runClient() {
        try {
            OpcUaClient client = createClient("opc.tcp://10.168.1.9:4840");
            future.whenCompleteAsync((c, ex) -> {
                if (ex != null) {
                    ex.printStackTrace();
                    Log.e(TAG, String.format("Error running example: %s", ex.getMessage()));
                }

                try {
                    client.disconnect().get();
                    Stack.releaseSharedResources();
                } catch (InterruptedException | ExecutionException e) {
                    Log.e(TAG, String.format("Error disconnecting: %s", e.getMessage()));
                }

                try {
                    Thread.sleep(1000);
                    Log.i(TAG, "Exit....");
                    // System.exit(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            try {
                runner.run(client, future);
                // future.get(5, TimeUnit.SECONDS);
            } catch (Throwable t) {
                t.printStackTrace();
                Log.e(TAG, String.format("Error running client: %s", t.getMessage()));
                future.completeExceptionally(t);
            }
        } catch (Throwable t) {
            t.printStackTrace();
            Log.e(TAG, String.format("Error getting client: %s", t.getMessage()));
            future.completeExceptionally(t);

            try {
                Thread.sleep(1000);
                Log.i(TAG, "System Exit.");
                // System.exit(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            Thread.sleep(999_999_999);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public interface Runner {
        void run(OpcUaClient client, CompletableFuture<OpcUaClient> future);
    }
}

