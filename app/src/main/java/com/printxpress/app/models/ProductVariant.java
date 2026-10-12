package com.printxpress.app.models;

public class ProductVariant {
    private String variantId;
    private String productId;
    private String size;
    private String material;
    private double priceModifier;

    public ProductVariant() {}

    public ProductVariant(String variantId, String productId, String size, String material, double priceModifier) {
        this.variantId = variantId;
        this.productId = productId;
        this.size = size;
        this.material = material;
        this.priceModifier = priceModifier;
    }

    public String getVariantId() { return variantId; }
    public void setVariantId(String variantId) { this.variantId = variantId; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }

    public double getPriceModifier() { return priceModifier; }
    public void setPriceModifier(double priceModifier) { this.priceModifier = priceModifier; }
}
