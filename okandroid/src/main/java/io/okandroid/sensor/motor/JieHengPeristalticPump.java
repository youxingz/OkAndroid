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

public class JieHengPeristalticPump {

    private ModbusMaster master;
    private int slaveId;

    public JieHengPeristalticPump(ModbusMaster modbusMaster, int slaveId) {
        this.master = modbusMaster;
        this.slaveId = slaveId;
    }

    /**
     * 转速 RPM
     * 0x00 地址
     *
     * @return rpm
     */
    public int velocity() throws ModbusTransportException {
        // FunctionCode.READ_HOLDING_REGISTERS
        ReadHoldingRegistersResponse response = (ReadHoldingRegistersResponse) master.send(new ReadHoldingRegistersRequest(slaveId, 0x00, 0x02));
        byte[] data = response.getData();
        return ModbusUtils.toShort(data[0], data[1]);
    }

    public void velocity(int velocity) throws ModbusTransportException {
        // FunctionCode.WRITE_REGISTER
        WriteRegisterResponse response = (WriteRegisterResponse) master.send(new WriteRegisterRequest(slaveId, 0x00, velocity));
        if (response.isException()) {
            // error.
        }
    }

    /**
     * 启停
     * 0x00
     *
     * @return
     */
    public boolean turn() throws ModbusTransportException {
        // 03 FunctionCode.READ_COILS
        ReadCoilsResponse response = (ReadCoilsResponse) master.send(new ReadCoilsRequest(slaveId, 0x00, 2));
        byte[] data = response.getData();
        return 0x01 == ModbusUtils.toShort(data[0], data[1]);
    }

    public void turn(boolean turnOn) throws ModbusTransportException {
        // 05
        WriteCoilResponse response = (WriteCoilResponse) master.send(new WriteCoilRequest(slaveId, 0x00, turnOn));
        if (response.isException()) {
            // error.
        }
    }


    /**
     * 正反
     * 0x01
     */
    public int direction() throws ModbusTransportException {
        // 03 FunctionCode.READ_COILS
        ReadCoilsResponse response = (ReadCoilsResponse) master.send(new ReadCoilsRequest(slaveId, 0x01, 2));
        byte[] data = response.getData();
        return ModbusUtils.toShort(data[0], data[1]);
    }

    public void direction(int direction) throws ModbusTransportException {
        // 05
        WriteCoilResponse response = (WriteCoilResponse) master.send(new WriteCoilRequest(slaveId, 0x01, direction == 0));
        if (response.isException()) {
            // error.
        }

    }

}
