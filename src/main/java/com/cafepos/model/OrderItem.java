package com.cafepos.model;

/** A single line item within a saved order. */
public class OrderItem {
    private int    id;
    private int    orderId;
    private int    productId;
    private String productName;
    private double unitPrice;
    private int    quantity;
    private double lineTotal;

    public OrderItem() {}

    public OrderItem(int productId, String productName,
                     double unitPrice, int quantity) {
        this.productId   = productId;
        this.productName = productName;
        this.unitPrice   = unitPrice;
        this.quantity    = quantity;
        this.lineTotal   = unitPrice * quantity;
    }

    // Getters
    public int    getId()          { return id; }
    public int    getOrderId()     { return orderId; }
    public int    getProductId()   { return productId; }
    public String getProductName() { return productName; }
    public double getUnitPrice()   { return unitPrice; }
    public int    getQuantity()    { return quantity; }
    public double getLineTotal()   { return lineTotal; }

    // Setters
    public void setId(int id)                    { this.id = id; }
    public void setOrderId(int orderId)          { this.orderId = orderId; }
    public void setProductId(int productId)      { this.productId = productId; }
    public void setProductName(String name)      { this.productName = name; }
    public void setUnitPrice(double unitPrice)   { this.unitPrice = unitPrice; }
    public void setQuantity(int quantity)        { this.quantity = quantity; }
    public void setLineTotal(double lineTotal)   { this.lineTotal = lineTotal; }
}
