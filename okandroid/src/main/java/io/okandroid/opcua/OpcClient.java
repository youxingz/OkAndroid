package io.okandroid.opcua;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.stack.client.security.DefaultClientCertificateValidator;
import org.eclipse.milo.opcua.stack.core.Stack;
import org.eclipse.milo.opcua.stack.core.security.DefaultTrustListManager;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Security;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@RequiresApi(api = Build.VERSION_CODES.O)
public class OpcClient {
    static {
        // Required for SecurityPolicy.Aes256_Sha256_RsaPss
        Security.addProvider(new BouncyCastleProvider());
    }

    private final static String TAG = "OpcClient";

    private final CompletableFuture<OpcUaClient> future = new CompletableFuture<>();
    private DefaultTrustListManager trustListManager;
    private Runner runner;

    public OpcClient(Runner runner) {
        this.runner = runner;
    }

    public void start() {
        runClient();
    }

    private OpcUaClient createClient() throws Exception {
        Path securityTempDir = Paths.get(System.getProperty("java.io.tmpdir"), "client", "security");
        Files.createDirectories(securityTempDir);
        if (!Files.exists(securityTempDir)) {
            throw new Exception("unable to create security dir: " + securityTempDir);
        }

        File pkiDir = securityTempDir.resolve("pki").toFile();

        Log.i(TAG, String.format("security dir: %s", securityTempDir.toAbsolutePath()));
        Log.i(TAG, String.format("security pki dir: %s", pkiDir.getAbsolutePath()));

        KeyStoreLoader loader = new KeyStoreLoader().load(securityTempDir);

        trustListManager = new DefaultTrustListManager(pkiDir);

        DefaultClientCertificateValidator certificateValidator =
                new DefaultClientCertificateValidator(trustListManager);

        return OpcUaClient.create(
                "opc.tcp://10.168.1.9:4840",
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
            OpcUaClient client = createClient();
            future.whenCompleteAsync((c, ex) -> {
                if (ex != null) {
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
                future.get(15, TimeUnit.SECONDS);
            } catch (Throwable t) {
                Log.e(TAG, String.format("Error running client: %s", t.getMessage()));
                future.completeExceptionally(t);
            }
        } catch (Throwable t) {
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
