package com.dangoauto.app;

import org.json.JSONObject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Car implements Serializable {
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
    
    public Car(String id, String name, String brand, String model, double price, int year, 
               int km, String fuel, String power, String transmission, String description, 
               String licensePlate, List<String> features, List<String> images) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.model = model;
        this.price = price;
        this.year = year;
        this.km = km;
        this.fuel = fuel;
        this.power = power;
        this.transmission = transmission;
        this.description = description;
        this.licensePlate = licensePlate;
        this.features = features != null ? features : new ArrayList<>();
        this.images = images != null ? images : new ArrayList<>();
    }
    
    // Constructor desde JSON
    public Car(JSONObject json) {
        try {
            // Asegurar que siempre se inicialicen las listas
            this.features = new ArrayList<>();
            this.images = new ArrayList<>();
            
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
            
            // Log para debugging
            android.util.Log.d("Car", "Parseando coche - ID: " + this.id + ", Name: " + this.name);
            
            if (json.has("features")) {
                try {
                    org.json.JSONArray featuresArray = json.getJSONArray("features");
                    for (int i = 0; i < featuresArray.length(); i++) {
                        String feature = featuresArray.optString(i, "");
                        if (!feature.isEmpty()) {
                            this.features.add(feature);
                        }
                    }
                } catch (Exception e) {
                    android.util.Log.w("Car", "Error parseando features: " + e.getMessage());
                }
            }
            
            if (json.has("images")) {
                try {
                    org.json.JSONArray imagesArray = json.getJSONArray("images");
                    for (int i = 0; i < imagesArray.length(); i++) {
                        String image = imagesArray.optString(i, "");
                        if (!image.isEmpty()) {
                            this.images.add(image);
                        }
                    }
                    android.util.Log.d("Car", "Imágenes parseadas: " + this.images.size());
                } catch (Exception e) {
                    android.util.Log.w("Car", "Error parseando images: " + e.getMessage());
                }
            } else if (json.has("image")) {
                // Compatibilidad con formato antiguo (una sola imagen)
                String image = json.optString("image", "");
                if (!image.isEmpty()) {
                    this.images.add(image);
                }
            }
        } catch (Exception e) {
            android.util.Log.e("Car", "Error parseando JSON", e);
            e.printStackTrace();
            // Asegurar que las listas estén inicializadas incluso si hay error
            if (this.features == null) {
                this.features = new ArrayList<>();
            }
            if (this.images == null) {
                this.images = new ArrayList<>();
            }
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
        return String.format("%.0f€", price).replaceAll("(\\d)(?=(\\d{3})+(?!\\d))", "$1,");
    }
    
    // Método para obtener nombre completo
    public String getFullName() {
        if (brand != null && !brand.isEmpty() && model != null && !model.isEmpty()) {
            return brand + " " + model + " " + year;
        }
        return name;
    }
    
    // Método para convertir a JSON
    public JSONObject toJSON() {
        try {
            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("name", name);
            json.put("brand", brand);
            json.put("model", model);
            json.put("price", price);
            json.put("year", year);
            json.put("km", km);
            json.put("fuel", fuel);
            json.put("power", power);
            json.put("transmission", transmission);
            json.put("description", description);
            json.put("licensePlate", licensePlate);
            
            org.json.JSONArray featuresArray = new org.json.JSONArray();
            for (String feature : features) {
                featuresArray.put(feature);
            }
            json.put("features", featuresArray);
            
            org.json.JSONArray imagesArray = new org.json.JSONArray();
            for (String image : images) {
                imagesArray.put(image);
            }
            json.put("images", imagesArray);
            
            return json;
        } catch (Exception e) {
            android.util.Log.e("Car", "Error convirtiendo a JSON", e);
            return new JSONObject();
        }
    }
}

