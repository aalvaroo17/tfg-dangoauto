package com.dangoauto.app;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class Car {
    
    private static final String TAG = "Car";
    
    private String id;
    private String name;
    private String brand;
    private String model;
    private double price;
    private int year;
    private int km;
    private String fuel;
    private String power;
    private String transmission;
    private String description;
    private String licensePlate;
    private List<String> features;
    private List<String> images;
    
    public Car() {
        this.features = new ArrayList<>();
        this.images = new ArrayList<>();
    }
    
    // Constructor desde JSON
    public Car(JSONObject json) {
        this.features = new ArrayList<>();
        this.images = new ArrayList<>();
        
        try {
            this.id = json.optString("id", "");
            this.name = json.optString("name", "");
            this.brand = json.optString("brand", "");
            this.model = json.optString("model", "");
            this.price = json.optDouble("price", 0.0);
            this.year = json.optInt("year", 0);
            this.km = json.optInt("km", 0);
            this.fuel = json.optString("fuel", "");
            this.power = json.optString("power", "");
            this.transmission = json.optString("transmission", "");
            this.description = json.optString("description", "");
            this.licensePlate = json.optString("licensePlate", "");
            
            // Parsear features
            if (json.has("features")) {
                JSONArray featuresArray = json.optJSONArray("features");
                if (featuresArray != null) {
                    for (int i = 0; i < featuresArray.length(); i++) {
                        String feature = featuresArray.optString(i, "");
                        if (!feature.isEmpty()) {
                            this.features.add(feature);
                        }
                    }
                }
            }
            
            // Parsear images
            if (json.has("images")) {
                JSONArray imagesArray = json.optJSONArray("images");
                if (imagesArray != null) {
                    for (int i = 0; i < imagesArray.length(); i++) {
                        String image = imagesArray.optString(i, "");
                        if (!image.isEmpty()) {
                            this.images.add(image);
                        }
                    }
                }
            } else if (json.has("image")) {
                String image = json.optString("image", "");
                if (!image.isEmpty()) {
                    this.images.add(image);
                }
            }
            
            android.util.Log.d(TAG, "Coche parseado: " + getFullName() + " (ID: " + this.id + ")");
            
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error parseando JSON", e);
        }
    }
    
    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public double getPrice() { return price; }
    public int getYear() { return year; }
    public int getKm() { return km; }
    public String getFuel() { return fuel; }
    public String getPower() { return power; }
    public String getTransmission() { return transmission; }
    public String getDescription() { return description; }
    public String getLicensePlate() { return licensePlate; }
    public List<String> getFeatures() { return features; }
    public List<String> getImages() { return images; }
    
    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setBrand(String brand) { this.brand = brand; }
    public void setModel(String model) { this.model = model; }
    public void setPrice(double price) { this.price = price; }
    public void setYear(int year) { this.year = year; }
    public void setKm(int km) { this.km = km; }
    public void setFuel(String fuel) { this.fuel = fuel; }
    public void setPower(String power) { this.power = power; }
    public void setTransmission(String transmission) { this.transmission = transmission; }
    public void setDescription(String description) { this.description = description; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }
    public void setFeatures(List<String> features) { this.features = features; }
    public void setImages(List<String> images) { this.images = images; }
    
    // Método para obtener precio formateado
    public String getFormattedPrice() {
        return String.format("%,.0f€", price);
    }
    
    // Método para obtener nombre completo
    public String getFullName() {
        if (brand != null && !brand.isEmpty() && model != null && !model.isEmpty()) {
            return brand + " " + model + " " + year;
        }
        if (name != null && !name.isEmpty()) {
            return name;
        }
        return "Coche";
    }
}
