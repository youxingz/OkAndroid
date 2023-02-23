package io.okandroid.sensor.motor;

import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.msg.ReadCoilsResponse;
import com.serotonin.modbus4j.msg.ReadHoldingRegistersRequest;

/**
 * 雷塞电机 RS485 协议
 */
public class LeadShine {
    private ModbusMaster modbus;

    public LeadShine(ModbusMaster master) {
        this.modbus = master;
    }

    /**
     * 开环/闭环
     * 0x0003 Pr0.01
     *
     * @return 开环：0, 闭环: 2
     */
    public int loopMode(int slaveId) {
        try {
            ReadCoilsResponse response = (ReadCoilsResponse) modbus.send(new ReadHoldingRegistersRequest(slaveId, 0x0003, 1));
            byte[] data = response.getData();
            System.out.println("====================");
            System.out.println("0x0003: length" + data.length);
            return data[data.length - 1];
        } catch (ModbusTransportException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void loopMode(int slaveId, int mode) {
    }

    /**
     * 电机运转方向
     * 0x0007 Pr0.03
     *
     * @return 0: 正, 1: 负
     */
    public int direction(int slaveId) {
        return -1;
    }

    public void direction(int slaveId, int direction) {
    }

    /**
     * 位置环 Kp
     *
     * @return 0-3000, default: 25
     */
    public int positionKp(int slaveId) {
        return -1;
    }

    public void positionKp(int slaveId, int kp) {

    }

    /**
     * 位置环 KpH
     *
     * @return 0-3000, default: 0
     */
    public int positionKpH(int slaveId) {
        return -1;
    }

    public void positionKpH(int slaveId, int kpH) {
    }

    /**
     * 速度环 KI
     *
     * @return 0-3000, default: 3
     */
    public int velocityKI(int slaveId) {
        return -1;
    }

    public void velocityKI(int slaveId, int kI) {
    }

    /**
     * 速度环 Kp
     *
     * @return 0-3000, default: 25
     */
    public int velocityKp(int slaveId) {
        return -1;
    }

    public void velocityKp(int slaveId, int kp) {
    }

    /**
     * 上电自动运行
     * 0x01A3 Pr5.09
     *
     * @return 1: 开启, 0: 关闭
     */
    public int autoRun(int slaveId) {
        return -1;
    }

    /**
     * 实时速度反馈
     * 0x1046: 高 16 位
     * 0x1047: 低 16 位
     *
     * @return rpm
     */
    public int realtimeVelocity(int slaveId) {
        return -1;
    }

    /**
     * 当前报警
     * 0x2203
     * 故障码 备注
     * 0x01 过流
     * 0x02 过压
     * 0x40 电流采样回路故障
     * 0x80 锁轴故障
     * 0x200 EEPROM 故障
     * 0x100 参数自整定故障
     *
     * @return
     */
    public int realtimeException(int slaveId) {
        return -1;
    }
}
