package io.famartin.warehouse.common;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class OrderRecord {
    
    private String orderId;
    private String itemId;
    private Integer quantity;

    private String processingTimestamp;
    private String processedBy;
    private String error;
    private String reason;
    private Boolean approved;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

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

    public String getProcessingTimestamp() {
        return processingTimestamp;
    }

    public void setProcessingTimestamp(String processingTimestamp) {
        this.processingTimestamp = processingTimestamp;
    }

    public String getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(String processedBy) {
        this.processedBy = processedBy;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

}