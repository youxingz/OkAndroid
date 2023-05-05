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

public class LiquidMotor2 extends ControlConfig<LiquidMotor2.EquitPump2Payload> {

    private static LiquidMotor2 instance;
    private List<NodeId> configNodeIdsRead;
    private List<NodeId> configNodeIdsWrite;

    private LiquidMotor2() {
        configNodeIdsRead = new ArrayList<>();
        configNodeIdsRead.add(BioreactorNodeId.Equit_Pump2_ControlMode); // 控制方式0停止1控制循环2手动
        configNodeIdsRead.add(BioreactorNodeId.Equit_Pump2_Dir);
        configNodeIdsRead.add(BioreactorNodeId.Equit_Pump2_MeanRPM);
        // configNodeIdsRead.add(BioreactorNodeId.Equit_Pump2_Mode);        // 运行方式0定速1变速
        configNodeIdsWrite = new ArrayList<>();
        configNodeIdsWrite.add(BioreactorNodeId.Equit_Pump2_ControlMode); // 控制方式0停止1控制循环2手动
        configNodeIdsWrite.add(BioreactorNodeId.Equit_Pump2_Dir);
        configNodeIdsWrite.add(BioreactorNodeId.Equit_Pump2_MeanRPM);
        configNodeIdsWrite.add(BioreactorNodeId.Equit_Pump2_Mode);        // 运行方式0定速1变速
    }

    public static LiquidMotor2 getInstance() {
        if (instance == null) {
            instance = new LiquidMotor2();
        }
        return instance;
    }

    @Override
    public EquitPump2Payload readConfig() throws OkOPCException {
        try {
            List<DataValue> values = client().read(configNodeIdsRead).get();
            EquitPump2Payload payload = new EquitPump2Payload();
            Integer isOn = (Integer) OPCUtils.toJsonValue(values.get(0));
            payload.setIsOn(isOn != null && isOn == 2); // 2: 手动模式
            payload.setD((Boolean) OPCUtils.toJsonValue(values.get(1)));
            payload.setV((Float) OPCUtils.toJsonValue(values.get(2)));
            if (LiquidMotor.getInstance().getPump2Payload() != null) {
                payload.setT(LiquidMotor.getInstance().getPump2Payload().getT());
            }
            return payload;
        } catch (ExecutionException e) {
            throw new OkOPCException(e.getMessage());
        } catch (InterruptedException e) {
            throw new OkOPCException(e.getMessage());
        }
    }

    @Override
    public void writeConfig(EquitPump2Payload payload) throws OkOPCException {
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

    public static class EquitPump2Payload {
        private Boolean isOn;
        private Boolean d;
        private Float v;
        private Float t;

        public List<DataValue> toDataValues() {
            List<DataValue> values = new ArrayList<>();
            values.add(new DataValue(new Variant(isOn != null && isOn ? 2 : 1), null, null)); // 控制方式0停止1控制循环2手动
            values.add(new DataValue(new Variant(d), null, null));
            values.add(new DataValue(new Variant(v), null, null));
            // values.add(new DataValue(new Variant(t), null, null));
            values.add(new DataValue(new Variant(0), null, null)); // 0: 定速，1: 变速；每次都写入，确保正确执行转动运行要求
            return values;
        }

        public EquitPump2Payload() {
        }

        public EquitPump2Payload(Boolean isOn, Boolean d, Float v, Float t) {
            this.isOn = isOn;
            this.d = d;
            this.v = v;
            this.t = t;
        }

        public Boolean getIsOn() {
            return isOn;
        }

        public void setIsOn(Boolean on) {
            isOn = on;
        }

        public Boolean getD() {
            return d;
        }

        public void setD(Boolean d) {
            this.d = d;
        }

        public Float getV() {
            return v;
        }

        public void setV(Float v) {
            this.v = v;
        }

        public Float getT() {
            return t;
        }

        public void setT(Float t) {
            this.t = t;
        }

    }
}
