package com.cardioflex.pulse_generator.x;

/**
 * Notify 事件下发（通常用于：蓝牙状态更新、电池电量更新、波形数据更新）
 */
public class EventPayload {
    private String type;
    private int status;
    private String message;
    private Object data;

    public EventPayload(String type, int status, String message, Object data) {
        this.type = type;
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
