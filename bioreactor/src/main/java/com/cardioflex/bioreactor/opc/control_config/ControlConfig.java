package com.cardioflex.bioreactor.opc.control_config;

import com.cardioflex.bioreactor.opc.OPC;

import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;

import java.util.List;

import io.okandroid.exception.OkOPCException;
import io.okandroid.opcua.OpcClient;

public abstract class ControlConfig<T> {
    protected List<NodeId> configNodeIdsRead;
    protected List<NodeId> configNodeIdsWrite;

    protected OpcClient client() throws OkOPCException {
        return OPC.getClient();
    }

    public abstract T readConfig() throws OkOPCException;

    public abstract void writeConfig(T payload) throws OkOPCException;
}
