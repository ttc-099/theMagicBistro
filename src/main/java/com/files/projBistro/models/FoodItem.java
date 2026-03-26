package com.files.projBistro.models;

public class FoodItem {
    // within a FoodItem Object, they should have
    // some expected fields
    private int id;
    private String name;
    private double price;
    private int stock;
    private String itemType;

    // used to identify who's menu
    private String charCategory;

    // store string Path
    private String imagePath;

    // 1a. constructor
    private FoodItem(FoodItemBuilder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.price = builder.price;
        this.stock = builder.stock;
        this.charCategory = builder.charCategory;
        this.itemType = builder.itemType;
        this.imagePath = builder.imagePath;
    }

    // 1c. getters ---------
    public int getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
    public String getItemType() { return itemType; }
    public String getCharCategory() { return charCategory; }
    // Added this getter so the UI can find your images:
    public String getImagePath() { return imagePath; }
    // -----------------------

    // 1b. helper builder method
    public static class FoodItemBuilder {
        private int id;
        private String name;
        private double price;
        private int stock = 0;
        private String charCategory;
        private String itemType = "Food";
        private String imagePath;

        public FoodItemBuilder setId(int id) {
            this.id = id;
            return this;
        }

        public FoodItemBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public FoodItemBuilder setPrice(double price) {
            this.price = price;
            return this;
        }

        public FoodItemBuilder setStock(int stock) {
            this.stock = stock;
            return this;
        }

        public FoodItemBuilder setCharCategory(String charCategory) {
            this.charCategory = charCategory;
            return this;
        }

        public FoodItemBuilder setItemType(String itemType) {
            this.itemType = itemType;
            return this;
        }

        public FoodItemBuilder setImagePath(String imagePath) {
            // FIXED: Changed this.name to this.imagePath
            this.imagePath = imagePath;
            return this;
        }

        public FoodItem build() {
            return new FoodItem(this);
        }
    }
}