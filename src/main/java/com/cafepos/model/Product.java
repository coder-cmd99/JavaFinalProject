package com.cafepos.model;

/** Represents a menu product / item available for sale. */
public class Product {
    private int     id;
    private int     categoryId;
    private String  categoryName;
    private String  name;
    private String  description;
    private double  price;
    private String  imageEmoji;
    private boolean available;

    public Product() {}

    public Product(int id, int categoryId, String name,
                   String description, double price,
                   String imageEmoji, boolean available) {
        this.id = id;
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageEmoji = imageEmoji;
        this.available = available;
    }

    // Getters
    public int     getId()           { return id; }
    public int     getCategoryId()   { return categoryId; }
    public String  getCategoryName() { return categoryName; }
    public String  getName()         { return name; }
    public String  getDescription()  { return description; }
    public double  getPrice()        { return price; }
    public String  getImageEmoji()   { return imageEmoji; }
    public boolean isAvailable()     { return available; }

    // Setters
    public void setId(int id)                     { this.id = id; }
    public void setCategoryId(int categoryId)     { this.categoryId = categoryId; }
    public void setCategoryName(String n)         { this.categoryName = n; }
    public void setName(String name)              { this.name = name; }
    public void setDescription(String desc)       { this.description = desc; }
    public void setPrice(double price)            { this.price = price; }
    public void setImageEmoji(String imageEmoji)  { this.imageEmoji = imageEmoji; }
    public void setAvailable(boolean available)   { this.available = available; }

    @Override public String toString() { return name; }
}
