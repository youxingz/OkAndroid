package io.okandroid.js;

import java.util.UUID;

import io.okandroid.utils.GsonUtils;

/**
 * Java side sends requests to Js side.
 */
public class EventRequest {
    private String requestId;
    private int type; // 1: okandroid, 0: user
    private Long timestamp;
    private Object data;

    public EventRequest(Object data, int type) {
        this.data = data;
        this.type = type;
        this.requestId = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
    }

    public String toJsonString() {
        return GsonUtils.getInstance().toJson(this);
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
