package com.cardioflex.bioreactor;

import android.util.Log;

import com.cardioflex.bioreactor.opc.BioreactorNodeId;
import com.cardioflex.bioreactor.opc.OPC;
import com.cardioflex.bioreactor.opc.OPCUtils;
import com.cardioflex.bioreactor.sys.Sys;
import com.cardioflex.bioreactor.x.EventPayload;
import com.google.android.material.snackbar.Snackbar;

import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import io.okandroid.OkAndroid;
import io.okandroid.exception.OkOPCException;
import io.okandroid.js.EventResponse;
import io.okandroid.opcua.OpcClient;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;

public class PageDashboard {
    private final static String TAG = "PageDashboardAct";
    private static CoreActivity coreActivity;
    private static Disposable disposable;

    public static int TIME_INTERVAL = 5000; // 每 5000ms 读一次数据

    public static void setCoreActivity(CoreActivity coreActivity) {
        PageDashboard.coreActivity = coreActivity;
    }

    public static void start() {
        Sys.start();
        if (1 == 1) return;
        List<NodeId> nodeIds = new ArrayList<>();
        nodeIds.add(BioreactorNodeId.Equit_Air_PV);
        nodeIds.add(BioreactorNodeId.Equit_CO2_PV);
        nodeIds.add(BioreactorNodeId.Equit_N2_PV);
        nodeIds.add(BioreactorNodeId.Equit_O2_PV);
        nodeIds.add(BioreactorNodeId.DO_PV);
        nodeIds.add(BioreactorNodeId.PH_PV);
        nodeIds.add(BioreactorNodeId.Temp_PV);
        nodeIds.add(BioreactorNodeId.Level_PV);
        nodeIds.add(BioreactorNodeId.Equit_Stir_PV);

        nodeIds.add(BioreactorNodeId.Equit_Air_SV);
        nodeIds.add(BioreactorNodeId.Equit_CO2_SV);
        nodeIds.add(BioreactorNodeId.Equit_N2_SV);
        nodeIds.add(BioreactorNodeId.Equit_O2_SV);
        nodeIds.add(BioreactorNodeId.DO_SV);
        nodeIds.add(BioreactorNodeId.PH_SV);
        nodeIds.add(BioreactorNodeId.Temp_SV);
        nodeIds.add(BioreactorNodeId.Level_SV);
        nodeIds.add(BioreactorNodeId.Equit_Stir_SV);

        // 是否开启
        nodeIds.add(BioreactorNodeId.Equit_Air_ControlMode);
        nodeIds.add(BioreactorNodeId.Equit_CO2_ControlMode);
        nodeIds.add(BioreactorNodeId.Equit_N2_ControlMode);
        nodeIds.add(BioreactorNodeId.Equit_O2_ControlMode);
        disposable = OkAndroid.newThread().scheduleDirect(() -> {
            Snackbar connError_ = Snackbar.make(CoreActivity.getOkWebViewInstance().getWebView(), "PLC 连接失败，正在重试...", Snackbar.LENGTH_SHORT);
            HashMap<String, Object> map = new HashMap<>(18);
            long current = System.currentTimeMillis();
            while (true) {
                try {
                    OpcClient client = OPC.getClient();
                    if (disposable != null && disposable.isDisposed()) {
                        client.disconnect();
                        return;
                    }
                    CompletableFuture<List<DataValue>> future = client.read(nodeIds);
                    List<DataValue> dataValues = future.get(); // wait...
                    map.put("timestamp", current);
                    // pv
                    map.put("pv_air", OPCUtils.toJsonValue(dataValues.get(0)));
                    map.put("pv_co2", OPCUtils.toJsonValue(dataValues.get(1)));
                    map.put("pv_n2", OPCUtils.toJsonValue(dataValues.get(2)));
                    map.put("pv_o2", OPCUtils.toJsonValue(dataValues.get(3)));
                    map.put("pv_do", OPCUtils.toJsonValue(dataValues.get(4)));
                    map.put("pv_ph", OPCUtils.toJsonValue(dataValues.get(5)));
                    map.put("pv_temp", OPCUtils.toJsonValue(dataValues.get(6)));
                    map.put("pv_level", OPCUtils.toJsonValue(dataValues.get(7)));
                    map.put("pv_stir", OPCUtils.toJsonValue(dataValues.get(8)));
                    // sv
                    map.put("sv_air", OPCUtils.toJsonValue(dataValues.get(9)));
                    map.put("sv_co2", OPCUtils.toJsonValue(dataValues.get(10)));
                    map.put("sv_n2", OPCUtils.toJsonValue(dataValues.get(11)));
                    map.put("sv_o2", OPCUtils.toJsonValue(dataValues.get(12)));
                    map.put("sv_do", OPCUtils.toJsonValue(dataValues.get(13)));
                    map.put("sv_ph", OPCUtils.toJsonValue(dataValues.get(14)));
                    map.put("sv_temp", OPCUtils.toJsonValue(dataValues.get(15)));
                    map.put("sv_level", OPCUtils.toJsonValue(dataValues.get(16)));
                    map.put("sv_stir", OPCUtils.toJsonValue(dataValues.get(17)));
                    // status
                    map.put("ison_air", OPCUtils.toJsonValue(dataValues.get(18)));
                    map.put("ison_co2", OPCUtils.toJsonValue(dataValues.get(19)));
                    map.put("ison_n2", OPCUtils.toJsonValue(dataValues.get(20)));
                    map.put("ison_o2", OPCUtils.toJsonValue(dataValues.get(21)));
                    CoreActivity.getOkWebViewInstance().sendToWeb(new EventPayload("pv-sv-stream", 200, map)).observeOn(OkAndroid.subscribeIOThread()).observeOn(OkAndroid.mainThread()).subscribe(new SingleObserver<EventResponse>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {
                            // Snackbar.make(CoreActivity.getOkWebViewInstance().getWebView(), "test", Snackbar.LENGTH_LONG).show();
                        }

                        @Override
                        public void onSuccess(@NonNull EventResponse eventResponse) {
                            Log.i(TAG, eventResponse.toString());
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            e.printStackTrace();
                        }
                    });
                    // wait 1s
                    do {
                        Thread.sleep(100);
                    } while (System.currentTimeMillis() <= current + TIME_INTERVAL);
                    current = System.currentTimeMillis();
                } catch (OkOPCException e) {
                    e.printStackTrace();
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                    connError_.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        // subscribe values
    }

    public static void destroy() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }

    }
}
