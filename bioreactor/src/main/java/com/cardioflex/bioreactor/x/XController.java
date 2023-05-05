package com.cardioflex.bioreactor.x;

import com.cardioflex.bioreactor.opc.control_config.LiquidMotor;
import com.cardioflex.bioreactor.opc.control_config.LiquidMotor1;
import com.cardioflex.bioreactor.opc.control_config.LiquidMotor2;
import com.cardioflex.bioreactor.opc.control_config.SensorDO;
import com.cardioflex.bioreactor.opc.control_config.SensorPH;
import com.cardioflex.bioreactor.opc.control_config.SensorTemp;
import com.google.android.material.snackbar.Snackbar;
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
                    // start it if the config was set.
                    if (payload.getIsOn() != null && payload.getIsOn()) {
                        LiquidMotor.getInstance().start();
                    }
                }
                case "liquid_out": {
                    LiquidMotor2.EquitPump2Payload payload = GsonUtils.getInstance().fromJson(body, LiquidMotor2.EquitPump2Payload.class);
                    LiquidMotor2.getInstance().writeConfig(payload);
                    LiquidMotor.getInstance().setLiquidMotor2(LiquidMotor2.getInstance(), payload);
                    // start it if the config was set.
                    if (payload.getIsOn() != null && payload.getIsOn()) {
                        LiquidMotor.getInstance().start();
                    }
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
