package com.cafepos.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Represents a completed sales transaction. */
public class Order {
    private int             id;
    private int             userId;
    private String          cashierName;
    private LocalDateTime   orderDate;
    private double          subtotal;
    private double          taxAmount;
    private double          discountAmount;
    private double          totalAmount;
    private String          paymentMethod;
    private String          status;
    private String          notes;
    private List<OrderItem> items = new ArrayList<>();

    public Order() {}

    // ── Getters ───────────────────────────────────────────────────────────
    public int             getId()             { return id; }
    public int             getUserId()         { return userId; }
    public String          getCashierName()    { return cashierName; }
    public LocalDateTime   getOrderDate()      { return orderDate; }
    public double          getSubtotal()       { return subtotal; }
    public double          getTaxAmount()      { return taxAmount; }
    public double          getDiscountAmount() { return discountAmount; }
    public double          getTotalAmount()    { return totalAmount; }
    public String          getPaymentMethod()  { return paymentMethod; }
    public String          getStatus()         { return status; }
    public String          getNotes()          { return notes; }
    public List<OrderItem> getItems()          { return items; }

    // ── Setters ───────────────────────────────────────────────────────────
    public void setId(int id)                          { this.id = id; }
    public void setUserId(int userId)                  { this.userId = userId; }
    public void setCashierName(String cashierName)     { this.cashierName = cashierName; }
    public void setOrderDate(LocalDateTime orderDate)  { this.orderDate = orderDate; }
    public void setSubtotal(double subtotal)           { this.subtotal = subtotal; }
    public void setTaxAmount(double taxAmount)         { this.taxAmount = taxAmount; }
    public void setDiscountAmount(double d)            { this.discountAmount = d; }
    public void setTotalAmount(double totalAmount)     { this.totalAmount = totalAmount; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setStatus(String status)               { this.status = status; }
    public void setNotes(String notes)                 { this.notes = notes; }
    public void setItems(List<OrderItem> items)        { this.items = items; }

    public int getTotalItemCount() {
        return items.stream().mapToInt(OrderItem::getQuantity).sum();
    }
}

