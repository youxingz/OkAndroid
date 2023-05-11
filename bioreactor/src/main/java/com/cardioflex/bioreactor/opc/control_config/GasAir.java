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

public class GasAir extends ControlConfig<GasAir.GasAirPayload> {
    private static NodeId RESET_CMD = BioreactorNodeId.Equit_Air_DosageReset;
    private static GasAir instance;

    public static GasAir getInstance() {
        if (instance == null) {
            instance = new GasAir();
        }
        return instance;
    }

    private GasAir() {
        configNodeIdsRead = new ArrayList<>();
        configNodeIdsRead.add(BioreactorNodeId.Equit_Air_ControlMode); // 控制方式0停止1控制循环3手动
        configNodeIdsRead.add(BioreactorNodeId.Equit_Air_Dosage);
        configNodeIdsRead.add(BioreactorNodeId.Equit_Air_PV);
        configNodeIdsRead.add(BioreactorNodeId.Equit_Air_SV);
        configNodeIdsWrite = new ArrayList<>();
        configNodeIdsWrite.add(BioreactorNodeId.Equit_Air_ControlMode); // 控制方式0停止1控制循环3手动
        configNodeIdsWrite.add(BioreactorNodeId.Equit_Air_Dosage);
        configNodeIdsWrite.add(BioreactorNodeId.Equit_Air_PV);
        configNodeIdsWrite.add(BioreactorNodeId.Equit_Air_SV);
    }

    @Override
    public GasAirPayload readConfig() throws OkOPCException {
        try {
            List<DataValue> values = client().read(configNodeIdsRead).get();
            GasAir.GasAirPayload payload = new GasAir.GasAirPayload();
            Integer isOn = (Integer) OPCUtils.toJsonValue(values.get(0));
            payload.setIsOn(isOn != null && isOn == 3); // 2: 手动模式
            payload.setTotal((Float) OPCUtils.toJsonValue(values.get(1)));
            payload.setPv((Float) OPCUtils.toJsonValue(values.get(2)));
            payload.setSv((Float) OPCUtils.toJsonValue(values.get(3)));
            return payload;
        } catch (ExecutionException e) {
            throw new OkOPCException(e.getMessage());
        } catch (InterruptedException e) {
            throw new OkOPCException(e.getMessage());
        }
    }

    @Override
    public void writeConfig(GasAirPayload payload) throws OkOPCException {
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

    public void resetDosage() throws OkOPCException {
        try {
            StatusCode code = client().write(RESET_CMD, new DataValue(new Variant(true), null, null)).get();
            if (!code.isGood()) {
                throw new OkOPCException("PLC 写入异常：[" + RESET_CMD.toString() + "]");
            }
        } catch (ExecutionException e) {
            throw new OkOPCException(e.getMessage());
        } catch (InterruptedException e) {
            throw new OkOPCException(e.getMessage());
        }
    }

    public static class GasAirPayload {
        private Boolean isOn;
        private Float pv;
        private Float sv;
        private Float total;

        public GasAirPayload() {
        }

        public GasAirPayload(Boolean isOn, Float pv, Float sv, Float total) {
            this.isOn = isOn;
            this.pv = pv;
            this.sv = sv;
            this.total = total;
        }

        public List<DataValue> toDataValues() {
            List<DataValue> values = new ArrayList<>();
            values.add(new DataValue(new Variant(isOn != null && isOn ? (short) 3 : (short) 0), null, null)); // 控制方式0停止1控制循环3手动
            values.add(new DataValue(new Variant(total), null, null));
            values.add(new DataValue(new Variant(pv), null, null));
            values.add(new DataValue(new Variant(sv), null, null));
            return values;
        }

        public Boolean getIsOn() {
            return isOn;
        }

        public void setIsOn(Boolean on) {
            isOn = on;
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

        public Float getTotal() {
            return total;
        }

        public void setTotal(Float total) {
            this.total = total;
        }
    }
}
