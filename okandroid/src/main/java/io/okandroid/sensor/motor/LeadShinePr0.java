package io.okandroid.sensor.motor;

import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.base.ModbusUtils;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.msg.ReadHoldingRegistersRequest;
import com.serotonin.modbus4j.msg.ReadHoldingRegistersResponse;
import com.serotonin.modbus4j.msg.WriteRegisterRequest;
import com.serotonin.modbus4j.msg.WriteRegisterResponse;

/**
 * ⚠️ RS485 通讯触发的 JOG，触发间隔时间小于 50ms 才会连续运行，否则就只能进行点动。
 * <p>
 * 故障解决办法：
 * 绿色 LED 不亮	    未上电	               检查驱动器电源线是否正确连接。
 * 红色LED闪烁1次	    过流	"重启驱动器;         重启驱动器报警依然存在，检查电机动力线是否短路。"
 * 红色LED闪烁2次	    过压	"重启驱动器;         重启驱动器报警依然存在，检查电源电压是否过高。"
 * 红色LED闪烁3次	    运放错误	               "重启驱动器;重启驱动器报警依然存在，驱动器硬件故障。"
 * 红色LED闪烁4次	    锁轴错误	                检查电机动力线是否断线，检查是否有接电机;恢复出厂设置
 * 红色LED闪烁5次	    存储错误	                使用 RS232 调试口连接上位机，恢复驱动器到出厂设置; 恢复出厂设置报警依然存在，驱动器硬件故障。
 * 红色LED闪烁6次	    电机参数自整定错误	        "重启驱动器; 使用上位机关闭自整定功能。"
 * 红色LED闪烁7次	    超差报警	                如果电机一使能动作就报警，检查电机 A+A-B+B-与驱动器对应 口是否一一对应;检查编码器分辨率是否设置正确。如果电机运 行过程中报警，则检查电机是否有发生堵转、卡顿;
 * 红色LED闪烁8次	    编码器断线检测	        检查编码器接口是否有效插入;检查是否有编码器线断线;
 * 红色LED闪烁9次	    输入 IO 配置重复	        检测输入 IO 口的功能配置是否有重复;恢复出厂设置
 * 电机不转	        未使能	                检查输入口是否配置使能功能，且极性为常闭。
 * 连不上主站	        通讯故障	                检查网线是否有问题485 ID 设置错误，检查地址设置是否正确
 */
public class LeadShinePr0 {
    private ModbusMaster modbus;
    private int slaveId;

    public LeadShinePr0(ModbusMaster master, int slaveId) {
        this.modbus = master;
        this.slaveId = slaveId;
    }

    /**
     * 设置 Pr0 为速度模式
     */
    public void setModeToVelocity() throws ModbusTransportException {
        WriteRegisterRequest request = new WriteRegisterRequest(slaveId, 0x6200, 0x02);
        WriteRegisterResponse response = (WriteRegisterResponse) modbus.send(request);
        if (response.isException()) {
            // error.
        }
    }

    /**
     * 设置速度 0x6203
     *
     * @param velocity rpm
     * @throws ModbusTransportException
     */
    public void velocity(int velocity) throws ModbusTransportException {
        WriteRegisterResponse response = (WriteRegisterResponse) modbus.send(new WriteRegisterRequest(slaveId, 0x6203, velocity));
        if (response.isException()) {
            // error.
        }
    }

    /**
     * 读取速度
     *
     * @return rpm
     * @throws ModbusTransportException
     */
    public int velocity() throws ModbusTransportException {
        ReadHoldingRegistersResponse response = (ReadHoldingRegistersResponse) modbus.send(new ReadHoldingRegistersRequest(slaveId, 0x6203, 2));
        if (response.isException()) {
            // error
            return -1;
        }
        byte[] data = response.getData();
        return ModbusUtils.toShort(data[0], data[1]);
    }

    /**
     * 启动/急停
     *
     * @param turnOn {true} 启动, {false} 急停
     * @throws ModbusTransportException
     */
    public void turn(boolean turnOn) throws ModbusTransportException {
        WriteRegisterResponse response = (WriteRegisterResponse) modbus.send(new WriteRegisterRequest(slaveId, 0x6202, turnOn ? 0x10 : 0x40));
        if (response.isException()) {
            // error.
        }
    }

    /**
     * 设置加减速时间 0x01E7
     *
     * @param time ms/Krmp
     * @throws ModbusTransportException
     */
    public void accelerationTime(boolean positive, int time) throws ModbusTransportException {
        WriteRegisterResponse response = (WriteRegisterResponse) modbus.send(new WriteRegisterRequest(slaveId, positive ? 0x6204 : 0x6205, time));
        if (response.isException()) {
            // error.
        }
    }

    /**
     * 读取加减速时间
     *
     * @return ms/Krmp
     * @throws ModbusTransportException
     */
    public int accelerationTime(boolean positive) throws ModbusTransportException {
        ReadHoldingRegistersResponse response = (ReadHoldingRegistersResponse) modbus.send(new ReadHoldingRegistersRequest(slaveId, positive ? 0x6204 : 0x6205, 2));
        if (response.isException()) {
            // error
            return -1;
        }
        byte[] data = response.getData();
        return ModbusUtils.toShort(data[0], data[1]);
    }
}
