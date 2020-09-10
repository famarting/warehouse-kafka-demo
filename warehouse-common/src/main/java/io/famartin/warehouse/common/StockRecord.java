package io.famartin.warehouse.common;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class StockRecord implements Comparable<StockRecord>{
    
    private String itemId;
    private Integer quantity;
    private String action;

    private String timestamp;
    private String error;
    private String message;
    private Boolean approved;
    private String originalRequest;

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public String getOriginalRequest() {
        return originalRequest;
    }

    public void setOriginalRequest(String originalRequest) {
        this.originalRequest = originalRequest;
    }

    @Override
    public String toString() {
        return "StockRecord [action=" + action + ", approved=" + approved + ", error=" + error + ", itemId=" + itemId
                + ", message=" + message + ", originalRequest=" + originalRequest + ", quantity=" + quantity
                + ", timestamp=" + timestamp + "]";
    }

    @Override
    public int compareTo(StockRecord other) {
        return itemId.compareTo(other.itemId);
    }

}