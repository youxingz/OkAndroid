package io.okandroid.sensor.motor;

import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.base.ModbusUtils;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.msg.ReadCoilsRequest;
import com.serotonin.modbus4j.msg.ReadCoilsResponse;
import com.serotonin.modbus4j.msg.ReadHoldingRegistersRequest;
import com.serotonin.modbus4j.msg.ReadHoldingRegistersResponse;
import com.serotonin.modbus4j.msg.WriteCoilRequest;
import com.serotonin.modbus4j.msg.WriteCoilResponse;
import com.serotonin.modbus4j.msg.WriteRegisterRequest;
import com.serotonin.modbus4j.msg.WriteRegisterResponse;

public class LeadFluidPump {

    private ModbusMaster master;
    private int slaveId;

    public LeadFluidPump(ModbusMaster modbusMaster, int slaveId) {
        this.master = modbusMaster;
        this.slaveId = slaveId;
    }

    /**
     * 转速 1-6000 RPM
     * 1003 保持寄存器地址（掉电丢失）
     * 3100 保持寄存器地址（掉电可存）
     *
     * @return rpm
     */
    public int velocity() throws ModbusTransportException {
        // FunctionCode.READ_HOLDING_REGISTERS
        ReadHoldingRegistersResponse response = (ReadHoldingRegistersResponse) master.send(new ReadHoldingRegistersRequest(slaveId, 3100, 0x02));
        byte[] data = response.getData();
        return ModbusUtils.toShort(data[0], data[1]);
    }

    public void velocity(int velocity) throws ModbusTransportException {
        // FunctionCode.WRITE_REGISTER
        WriteRegisterResponse response = (WriteRegisterResponse) master.send(new WriteRegisterRequest(slaveId, 3100, velocity));
        if (response.isException()) {
            // error.
        }
    }


    /**
     * 正反, 0: 逆时针, 1: 顺时针
     * 3101
     */
    public int direction() throws ModbusTransportException {
        // FunctionCode.READ_HOLDING_REGISTERS
        ReadHoldingRegistersResponse response = (ReadHoldingRegistersResponse) master.send(new ReadHoldingRegistersRequest(slaveId, 3101, 0x02));
        byte[] data = response.getData();
        return ModbusUtils.toShort(data[0], data[1]);
    }

    public void direction(int direction) throws ModbusTransportException {
        // FunctionCode.WRITE_REGISTER
        WriteRegisterResponse response = (WriteRegisterResponse) master.send(new WriteRegisterRequest(slaveId, 3101, direction));
        if (response.isException()) {
            // error.
        }
    }

    /**
     * 启停, 0: 停止, 1: 启动
     * 3102
     */
    public int turn() throws ModbusTransportException {
        // FunctionCode.READ_HOLDING_REGISTERS
        ReadHoldingRegistersResponse response = (ReadHoldingRegistersResponse) master.send(new ReadHoldingRegistersRequest(slaveId, 3102, 0x02));
        byte[] data = response.getData();
        return ModbusUtils.toShort(data[0], data[1]);
    }

    public boolean turn(boolean turnOn) throws ModbusTransportException {
        // FunctionCode.WRITE_REGISTER
        WriteRegisterResponse response = (WriteRegisterResponse) master.send(new WriteRegisterRequest(slaveId, 3102, turnOn ? 1 : 0));
        if (response.isException()) {
            // error.
            return false;
        }
        return true;
    }

}
