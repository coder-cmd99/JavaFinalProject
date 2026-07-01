package com.cafepos.model;

/** A product + quantity combination inside the active shopping cart. */
public class CartItem {
    private Product product;
    private int     quantity;

    public CartItem(Product product, int quantity) {
        this.product  = product;
        this.quantity = quantity;
    }

    public Product getProduct()  { return product; }
    public int     getQuantity() { return quantity; }
    public void    setQuantity(int quantity) { this.quantity = quantity; }

    /** Convenience: line total = price × qty */
    public double getLineTotal() {
        return product.getPrice() * quantity;
    }

    @Override public String toString() {
        return product.getName() + " x" + quantity;
    }
}
