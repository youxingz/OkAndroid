package com.cardioflex.bioreactor.x;

import com.cardioflex.bioreactor.motor.PulseMotor;
import com.cardioflex.bioreactor.motor.PulseMotorWorker;
import com.cardioflex.bioreactor.opc.control_config.GasAir;
import com.cardioflex.bioreactor.opc.control_config.GasCO2;
import com.cardioflex.bioreactor.opc.control_config.GasN2;
import com.cardioflex.bioreactor.opc.control_config.GasO2;
import com.cardioflex.bioreactor.opc.control_config.LiquidMotor;
import com.cardioflex.bioreactor.opc.control_config.LiquidMotor1;
import com.cardioflex.bioreactor.opc.control_config.LiquidMotor2;
import com.cardioflex.bioreactor.opc.control_config.SensorDO;
import com.cardioflex.bioreactor.opc.control_config.SensorPH;
import com.cardioflex.bioreactor.opc.control_config.SensorTemp;
import com.cardioflex.bioreactor.sys.Sys;
import com.yanzhenjie.andserver.annotation.GetMapping;
import com.yanzhenjie.andserver.annotation.PathVariable;
import com.yanzhenjie.andserver.annotation.PostMapping;
import com.yanzhenjie.andserver.annotation.RequestBody;
import com.yanzhenjie.andserver.annotation.RequestParam;
import com.yanzhenjie.andserver.annotation.RestController;
import com.yanzhenjie.andserver.http.HttpResponse;

import io.okandroid.exception.OkOPCException;
import io.okandroid.utils.GsonUtils;

@RestController
public class XController {

    @GetMapping("/local-api/test")
    public String test(@RequestParam("text") String text) {
        return "Resp: " + text;
    }

    @PostMapping("/local-api/system/{status}")
    public String systemOn(@PathVariable("status") String status, HttpResponse response) {
        switch (status) {
            case "on": {
                Sys.start();
                return "success";
            }
            case "off": {
                Sys.stop();
                return "success";
            }
        }
        response.setStatus(400);
        return "fail";
    }

    /**
     * 读配置
     *
     * @param tag motor_a1 | motor_b1 | ... | liquid_in | liquid_out | gas_1 | gas_2 | ... | sensor_ph | ...
     * @return
     */
    @GetMapping("/local-api/control-config/{tag}")
    public String readControlConfig(@PathVariable("tag") String tag, HttpResponse response) {
        try {
            switch (tag) {
                case "sensor_ph": {
                    SensorPH.PHPayload data = SensorPH.getInstance().readConfig();
                    return GsonUtils.getInstance().toJson(data);
                }
                case "sensor_do": {
                    SensorDO.DOPayload data = SensorDO.getInstance().readConfig();
                    return GsonUtils.getInstance().toJson(data);
                }
                case "sensor_temp": {
                    SensorTemp.TempPayload data = SensorTemp.getInstance().readConfig();
                    return GsonUtils.getInstance().toJson(data);
                }
                case "liquid_in": {
                    LiquidMotor1.EquitPump1Payload data = LiquidMotor1.getInstance().readConfig();
                    return GsonUtils.getInstance().toJson(data);
                }
                case "liquid_out": {
                    LiquidMotor2.EquitPump2Payload data = LiquidMotor2.getInstance().readConfig();
                    return GsonUtils.getInstance().toJson(data);
                }
                case "motor_a1":
                case "motor_a2":
                case "motor_a3":
                case "motor_b1":
                case "motor_b2":
                case "motor_b3": {
                    String subtag = tag.substring(6);
                    PulseMotor.PulseMotorConfig data = PulseMotorWorker.getConfig(subtag);
                    return GsonUtils.getInstance().toJson(data);
                }
                case "gas_1": {
                    GasAir.GasAirPayload data = GasAir.getInstance().readConfig();
                    return GsonUtils.getInstance().toJson(data);
                }
                case "gas_2": {
                    GasCO2.GasCO2Payload data = GasCO2.getInstance().readConfig();
                    return GsonUtils.getInstance().toJson(data);
                }
                case "gas_3": {
                    GasN2.GasN2Payload data = GasN2.getInstance().readConfig();
                    return GsonUtils.getInstance().toJson(data);
                }
                case "gas_4": {
                    GasO2.GasO2Payload data = GasO2.getInstance().readConfig();
                    return GsonUtils.getInstance().toJson(data);
                }
            }
        } catch (OkOPCException e) {
            response.setStatus(400);
            return e.getMessage();
        }
        return "";
    }

    /**
     * 写配置
     *
     * @return
     */
    @PostMapping("/local-api/control-config/{tag}")
    public String writeControlConfig(@PathVariable("tag") String tag, @RequestBody String body, HttpResponse response) {
        try {
            switch (tag) {
                case "sensor_ph": {
                    SensorPH.PHPayload pHPayload = GsonUtils.getInstance().fromJson(body, SensorPH.PHPayload.class);
                    SensorPH.getInstance().writeConfig(pHPayload);
                    break;
                }
                case "sensor_do": {
                    SensorDO.DOPayload payload = GsonUtils.getInstance().fromJson(body, SensorDO.DOPayload.class);
                    SensorDO.getInstance().writeConfig(payload);
                    break;
                }
                case "sensor_temp": {
                    SensorTemp.TempPayload payload = GsonUtils.getInstance().fromJson(body, SensorTemp.TempPayload.class);
                    SensorTemp.getInstance().writeConfig(payload);
                    break;
                }
                case "liquid_in": {
                    LiquidMotor1.EquitPump1Payload payload = GsonUtils.getInstance().fromJson(body, LiquidMotor1.EquitPump1Payload.class);
                    LiquidMotor1.getInstance().writeConfig(payload);
                    LiquidMotor.getInstance().setLiquidMotor1(LiquidMotor1.getInstance(), payload);
                    // start it if the config was set. // 交给系统总开关控制
                    if (payload.getIsOn() != null && payload.getIsOn()) {
                        LiquidMotor.getInstance().start();
                    }
                    break;
                }
                case "liquid_out": {
                    LiquidMotor2.EquitPump2Payload payload = GsonUtils.getInstance().fromJson(body, LiquidMotor2.EquitPump2Payload.class);
                    LiquidMotor2.getInstance().writeConfig(payload);
                    LiquidMotor.getInstance().setLiquidMotor2(LiquidMotor2.getInstance(), payload);
                    // start it if the config was set. // 交给系统总开关控制
                    if (payload.getIsOn() != null && payload.getIsOn()) {
                        LiquidMotor.getInstance().start();
                    }
                    break;
                }
                case "motor_a1":
                case "motor_a2":
                case "motor_a3":
                case "motor_b1":
                case "motor_b2":
                case "motor_b3": {
                    String subtag = tag.substring(6);
                    PulseMotor.PulseMotorConfig payload = GsonUtils.getInstance().fromJson(body, PulseMotor.PulseMotorConfig.class);
                    PulseMotorWorker.updateConfig(subtag, payload);
                    break;
                }
                case "gas_1": {
                    GasAir.GasAirPayload payload = GsonUtils.getInstance().fromJson(body, GasAir.GasAirPayload.class);
                    GasAir.getInstance().writeConfig(payload);
                    break;
                }
                case "gas_2": {
                    GasCO2.GasCO2Payload payload = GsonUtils.getInstance().fromJson(body, GasCO2.GasCO2Payload.class);
                    GasCO2.getInstance().writeConfig(payload);
                    break;
                }
                case "gas_3": {
                    GasN2.GasN2Payload payload = GsonUtils.getInstance().fromJson(body, GasN2.GasN2Payload.class);
                    GasN2.getInstance().writeConfig(payload);
                    break;
                }
                case "gas_4": {
                    GasO2.GasO2Payload payload = GsonUtils.getInstance().fromJson(body, GasO2.GasO2Payload.class);
                    GasO2.getInstance().writeConfig(payload);
                    break;
                }
            }
            response.setStatus(201);
            return "success";
        } catch (OkOPCException e) {
            response.setStatus(400);
            return e.getMessage();
        }
    }
}
