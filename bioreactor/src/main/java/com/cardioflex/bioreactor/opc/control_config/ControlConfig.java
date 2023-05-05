package com.cardioflex.bioreactor.opc.control_config;

import com.cardioflex.bioreactor.opc.OPC;

import io.okandroid.exception.OkOPCException;
import io.okandroid.opcua.OpcClient;

public abstract class ControlConfig<T> {

    protected OpcClient client() throws OkOPCException {
        return OPC.getClient();
    }


    public abstract T readConfig() throws OkOPCException;

    public abstract void writeConfig(T payload) throws OkOPCException;
}
