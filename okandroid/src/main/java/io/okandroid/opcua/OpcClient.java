package io.okandroid.opcua;

import static com.google.common.collect.Lists.newArrayList;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

import android.os.Build;

import androidx.annotation.RequiresApi;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.UaClient;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoredItemCreateRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoringParameters;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import io.okandroid.exception.OkOPCException;
import io.reactivex.rxjava3.core.Observable;

public class OpcClient {
    private OpcUaClient client;
    private UaClient uaClient;

    private boolean isConnected = false;

    public synchronized void connect(String url) throws OkOPCException {
        System.out.println("Connecting... " + url);
        try {
            if (client == null) {
                client = OpcClientFactory.createClient(url);
            }
            client.disconnect().get();
            uaClient = client.connect().get(); // wait until connected.
            isConnected = true;
        } catch (Exception e) {
            isConnected = false;
            throw new OkOPCException(e.getMessage());
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void disconnect() throws ExecutionException, InterruptedException {
        if (uaClient != null) {
            uaClient.disconnect().get();
        }
        if (client != null) {
            client.disconnect().get();
        }
    }

    public CompletableFuture<DataValue> read(NodeId nodeId) {
        return uaClient.readValue(0.0, TimestampsToReturn.Both, nodeId);
    }

    public CompletableFuture<List<DataValue>> read(List<NodeId> nodeIds) {
        return uaClient.readValues(0.0, TimestampsToReturn.Both, nodeIds);
    }

    public CompletableFuture<StatusCode> write(NodeId nodeId, DataValue value) {
        return uaClient.writeValue(nodeId, value);
    }

    public CompletableFuture<List<StatusCode>> write(List<NodeId> nodeIds, List<DataValue> values) {
        return uaClient.writeValues(nodeIds, values);
    }

    public Observable<NodeValue> observeValue(NodeId nodeId) {
        return Observable.create(emitter -> {
            try {
                // 1000.0 ms
                UaSubscription subscription = uaClient.getSubscriptionManager().createSubscription(1000.0).get();
                ReadValueId readValueId = new ReadValueId(nodeId, AttributeId.Value.uid(), null, QualifiedName.NULL_VALUE);
                UInteger clientHandle = subscription.nextClientHandle();
                MonitoringParameters parameters = new MonitoringParameters(
                        clientHandle,
                        1000.0,     // sampling interval
                        null,       // filter, null means use default
                        uint(10),   // queue size
                        true        // discard oldest // TODO! 确认参数是否会过滤未改变的值
                );

                MonitoredItemCreateRequest request = new MonitoredItemCreateRequest(
                        readValueId,
                        MonitoringMode.Reporting,
                        parameters
                );
                List<UaMonitoredItem> items = subscription.createMonitoredItems(
                        TimestampsToReturn.Both,
                        newArrayList(request),
                        (item, index) -> item.setValueConsumer((item1, value) -> {
                            if (!emitter.isDisposed()) {
                                emitter.onNext(new NodeValue(item1, value, index));
                            }
                        })
                ).get();
                for (UaMonitoredItem item : items) {
                    if (item.getStatusCode().isGood()) {
                        // logger.info("item created for nodeId={}", item.getReadValueId().getNodeId());
                    } else {
                        // logger.warn("failed to create item for nodeId={} (status={})", item.getReadValueId().getNodeId(), item.getStatusCode());
                    }
                }
                Thread.sleep(Long.MAX_VALUE); // listen forever.
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                if (!emitter.isDisposed()) {
                    emitter.onError(e);
                }
            }
        });
    }

    public static class NodeValue {
        public UaMonitoredItem item;
        public DataValue value;
        public int index;

        public NodeValue(UaMonitoredItem item, DataValue value, int index) {
            this.item = item;
            this.value = value;
            this.index = index;
        }

        @Override
        public String toString() {
            return "NodeValue{" +
                    "item=" + item +
                    ", value=" + value +
                    ", index=" + index +
                    '}';
        }
    }
}
