package com.cardioflex.bioreactor.opc;

import io.okandroid.exception.OkOPCException;
import io.okandroid.opcua.OpcClient;

public class OPC {

    private static final String endpointUrl = "opc.tcp://10.168.1.9:4840";

    private static volatile OpcClient client;

    synchronized public static OpcClient getClient() throws OkOPCException {
        if (client == null || !client.isConnected()) {
            client = new OpcClient();
            client.connect(endpointUrl);
        }
        return client;
    }
}
