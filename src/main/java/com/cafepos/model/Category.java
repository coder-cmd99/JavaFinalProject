package com.cafepos.model;

/** Represents a product category (e.g. Hot Drinks, Pastries). */
public class Category {
    private int id;
    private String name;
    private String iconEmoji;
    private int sortOrder;

    public Category() {}

    public Category(int id, String name, String iconEmoji, int sortOrder) {
        this.id = id;
        this.name = name;
        this.iconEmoji = iconEmoji;
        this.sortOrder = sortOrder;
    }

    public int    getId()        { return id; }
    public String getName()      { return name; }
    public String getIconEmoji() { return iconEmoji; }
    public int    getSortOrder() { return sortOrder; }

    public void setId(int id)                   { this.id = id; }
    public void setName(String name)            { this.name = name; }
    public void setIconEmoji(String iconEmoji)  { this.iconEmoji = iconEmoji; }
    public void setSortOrder(int sortOrder)     { this.sortOrder = sortOrder; }

    @Override public String toString() { return iconEmoji + " " + name; }
}
