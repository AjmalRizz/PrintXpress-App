package com.printxpress.app.models;

import com.google.firebase.Timestamp;

public class Order {
    private String orderId;
    private String uid;
    private String productId;
    private String productName;
    private String variantId;
    private int quantity;
    private String customText;
    private String designFileUrl;
    private String deliveryType; // "Pickup" or "Delivery"
    private String addressId;
    private String status; // "Pending", "Processing", "Printing", "Ready", "Completed", "Cancelled"
    private double totalPrice;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Order() {}

    public Order(String orderId, String uid, String productId, String productName, String variantId,
                 int quantity, String customText, String designFileUrl, String deliveryType,
                 String addressId, String status, double totalPrice, Timestamp createdAt, Timestamp updatedAt) {
        this.orderId = orderId;
        this.uid = uid;
        this.productId = productId;
        this.productName = productName;
        this.variantId = variantId;
        this.quantity = quantity;
        this.customText = customText;
        this.designFileUrl = designFileUrl;
        this.deliveryType = deliveryType;
        this.addressId = addressId;
        this.status = status;
        this.totalPrice = totalPrice;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getVariantId() { return variantId; }
    public void setVariantId(String variantId) { this.variantId = variantId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getCustomText() { return customText; }
    public void setCustomText(String customText) { this.customText = customText; }

    public String getDesignFileUrl() { return designFileUrl; }
    public void setDesignFileUrl(String designFileUrl) { this.designFileUrl = designFileUrl; }

    public String getDeliveryType() { return deliveryType; }
    public void setDeliveryType(String deliveryType) { this.deliveryType = deliveryType; }

    public String getAddressId() { return addressId; }
    public void setAddressId(String addressId) { this.addressId = addressId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
