package com.example.mseifriedsberger16.shoppinglist;

/**
 * Created by mseifriedsberger16 on 12.03.2019.
 */

public class Article {
    private int id;
    private String text;
    private float quantity;

    public Article(int id, String text, float quantity) {
        this.id = id;
        this.text = text;
        this.quantity = quantity;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public float getQuantity() {
        return quantity;
    }

    public void setQuantity(float quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "Article{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}
