package com.cardioflex.bioreactor.opc.control_config;

import com.cardioflex.bioreactor.opc.BioreactorNodeId;
import com.cardioflex.bioreactor.opc.OPCUtils;

import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import io.okandroid.exception.OkOPCException;

public class SensorTemp extends ControlConfig<SensorTemp.TempPayload> {

    private static SensorTemp instance;

    private SensorTemp() {
        configNodeIdsRead = new ArrayList<>();
        configNodeIdsRead.add(BioreactorNodeId.Temp_TempControl);
        configNodeIdsRead.add(BioreactorNodeId.Temp_Kp);
        configNodeIdsRead.add(BioreactorNodeId.Temp_Ti);
        configNodeIdsRead.add(BioreactorNodeId.Temp_Td);
        configNodeIdsRead.add(BioreactorNodeId.Temp_SV);
        configNodeIdsRead.add(BioreactorNodeId.Temp_PV);
        configNodeIdsWrite = new ArrayList<>();
        configNodeIdsWrite.add(BioreactorNodeId.Temp_TempControl);
        configNodeIdsWrite.add(BioreactorNodeId.Temp_Kp);
        configNodeIdsWrite.add(BioreactorNodeId.Temp_Ti);
        configNodeIdsWrite.add(BioreactorNodeId.Temp_Td);
        configNodeIdsWrite.add(BioreactorNodeId.Temp_SV);
        // configNodeIdsWrite.add(BioreactorNodeId.Temp_PV);
    }

    public static SensorTemp getInstance() {
        if (instance == null) {
            instance = new SensorTemp();
        }
        return instance;
    }

    @Override
    public TempPayload readConfig() throws OkOPCException {
        try {
            List<DataValue> values = client().read(configNodeIdsRead).get();
            TempPayload payload = new TempPayload();
            payload.setIsOn((Boolean) OPCUtils.toJsonValue(values.get(0)));
            payload.setKp((Float) OPCUtils.toJsonValue(values.get(1)));
            payload.setKi((Float) OPCUtils.toJsonValue(values.get(2)));
            payload.setKd((Float) OPCUtils.toJsonValue(values.get(3)));
            payload.setSv((Float) OPCUtils.toJsonValue(values.get(4)));
            payload.setPv((Float) OPCUtils.toJsonValue(values.get(5)));
            return payload;
        } catch (ExecutionException e) {
            throw new OkOPCException(e.getMessage());
        } catch (InterruptedException e) {
            throw new OkOPCException(e.getMessage());
        }
    }

    @Override
    public void writeConfig(TempPayload payload) throws OkOPCException {
        try {
            List<StatusCode> statusCodes = client().write(configNodeIdsWrite, payload.toDataValues()).get();
            List<NodeId> notGoodIds = new ArrayList<>();
            for (int i = 0; i < statusCodes.size(); i++) {
                StatusCode statusCode = statusCodes.get(i);
                if (statusCode == null) continue;
                if (statusCode.isGood()) continue;
                notGoodIds.add(configNodeIdsWrite.get(i));
            }
            if (!notGoodIds.isEmpty()) {
                throw new OkOPCException("PLC 写入异常：[" + notGoodIds.stream().map(NodeId::toString).collect(Collectors.joining(", ")) + "]");
            }
        } catch (ExecutionException e) {
            throw new OkOPCException(e.getMessage());
        } catch (InterruptedException e) {
            throw new OkOPCException(e.getMessage());
        }
    }

    public static class TempPayload {
        private Boolean isOn;
        private Float kp;
        private Float ki;
        private Float kd;
        private Float sv;
        private Float pv;


        public List<DataValue> toDataValues() {
            List<DataValue> values = new ArrayList<>();
            values.add(new DataValue(new Variant(isOn != null && isOn), null, null));
            values.add(new DataValue(new Variant(kp), null, null));
            values.add(new DataValue(new Variant(ki), null, null));
            values.add(new DataValue(new Variant(kd), null, null));
            values.add(new DataValue(new Variant(sv), null, null));
            // values.add(new DataValue(new Variant(pv)));
            return values;
        }

        public Boolean getIsOn() {
            return isOn;
        }

        public void setIsOn(Boolean on) {
            isOn = on;
        }

        public Float getKp() {
            return kp;
        }

        public void setKp(Float kp) {
            this.kp = kp;
        }

        public Float getKi() {
            return ki;
        }

        public void setKi(Float ki) {
            this.ki = ki;
        }

        public Float getKd() {
            return kd;
        }

        public void setKd(Float kd) {
            this.kd = kd;
        }

        public Float getPv() {
            return pv;
        }

        public void setPv(Float pv) {
            this.pv = pv;
        }

        public Float getSv() {
            return sv;
        }

        public void setSv(Float sv) {
            this.sv = sv;
        }
    }
}
