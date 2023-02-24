package io.okandroid.sensor.motor;

import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.base.ModbusUtils;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.msg.ReadCoilsResponse;
import com.serotonin.modbus4j.msg.ReadHoldingRegistersRequest;

/**
 * 雷塞电机 RS485 协议
 */
@Deprecated
public class LeadShine {
    private ModbusMaster modbus;
    private int slaveId;

    public LeadShine(ModbusMaster master, int slaveId) {
        this.modbus = master;
        this.slaveId = slaveId;
    }

    /**
     * 开环/闭环
     * 0x0003 Pr0.01
     *
     * @return 开环：0, 闭环: 2
     */
    public int loopMode() throws ModbusTransportException {
        ReadCoilsResponse response = (ReadCoilsResponse) modbus.send(new ReadHoldingRegistersRequest(slaveId, 0x0003, 2));
        byte[] data = response.getData();
        return ModbusUtils.toShort(data[0], data[1]);
    }

    public void loopMode(int mode) {
    }

    /**
     * 电机运转方向
     * 0x0007 Pr0.03
     *
     * @return 0: 正, 1: 负
     */
    public int direction() {
        return -1;
    }

    public void direction(int direction) {
    }

    /**
     * 位置环 Kp
     *
     * @return 0-3000, default: 25
     */
    public int positionKp() {
        return -1;
    }

    public void positionKp(int kp) {

    }

    /**
     * 位置环 KpH
     *
     * @return 0-3000, default: 0
     */
    public int positionKpH() {
        return -1;
    }

    public void positionKpH(int kpH) {
    }

    /**
     * 速度环 KI
     *
     * @return 0-3000, default: 3
     */
    public int velocityKI() {
        return -1;
    }

    public void velocityKI(int kI) {
    }

    /**
     * 速度环 Kp
     *
     * @return 0-3000, default: 25
     */
    public int velocityKp() {
        return -1;
    }

    public void velocityKp(int kp) {
    }

    /**
     * 上电自动运行
     * 0x01A3 Pr5.09
     *
     * @return 1: 开启, 0: 关闭
     */
    public int autoRun() {
        return -1;
    }

    /**
     * 实时速度反馈
     * 0x1046: 高 16 位
     * 0x1047: 低 16 位
     *
     * @return rpm
     */
    public int realtimeVelocity() {
        return -1;
    }

    /**
     * 当前报警 [R]
     * 0x2203
     * 故障码 备注
     * 0x01 过流              1
     * 0x02 过压              2
     * 0x40 电流采样回路故障    3
     * 0x80 锁轴故障           4
     * 0x200 EEPROM 故障      5
     * 0x100 参数自整定故障     6
     * 0x020 超差报警          7
     * 0    编码器断线检测      8
     * 0    输入 IO 配置重复    9
     *
     * @return
     */
    public int realtimeException() {
        return -1;
    }
}
