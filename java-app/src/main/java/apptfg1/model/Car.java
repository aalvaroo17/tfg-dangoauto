package apptfg1.model;

import java.util.List;

public class Car {
    private int id;
    private String name;
    private String price;
    private int year;
    private int km;
    private String fuel;
    private String power;
    private String transmission;
    private String description;
    private List<String> features;
    private String image;
    private String brand;

    public Car() {
    }

    public Car(int id, String name, String price, int year, int km, String fuel, 
               String power, String transmission, String description, 
               List<String> features, String image, String brand) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.year = year;
        this.km = km;
        this.fuel = fuel;
        this.power = power;
        this.transmission = transmission;
        this.description = description;
        this.features = features;
        this.image = image;
        this.brand = brand;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getKm() {
        return km;
    }

    public void setKm(int km) {
        this.km = km;
    }

    public String getFuel() {
        return fuel;
    }

    public void setFuel(String fuel) {
        this.fuel = fuel;
    }

    public String getPower() {
        return power;
    }

    public void setPower(String power) {
        this.power = power;
    }

    public String getTransmission() {
        return transmission;
    }

    public void setTransmission(String transmission) {
        this.transmission = transmission;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getFeatures() {
        return features;
    }

    public void setFeatures(List<String> features) {
        this.features = features;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public double getPriceAsDouble() {
        try {
            String priceClean = price.replace("€", "").replace(",", "").replace(".", "");
            return Double.parseDouble(priceClean);
        } catch (Exception e) {
            return 0.0;
        }
    }
}


