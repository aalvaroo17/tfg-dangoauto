package apptfg1.service;

import apptfg1.model.Car;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CarService {
    private List<Car> cars;

    public CarService() {
        initializeCars();
    }

    private void initializeCars() {
        cars = new ArrayList<>();
        
        cars.add(new Car(1, "BMW Serie 3", "35,900€", 2022, 15000, "Diésel", 
                "190 CV", "Automático", 
                "Sedán premium con excelente rendimiento y tecnología avanzada. Ideal para ejecutivos que buscan confort y elegancia.",
                Arrays.asList("GPS", "Asientos de cuero", "Climatizador automático", "Sistema de sonido premium"),
                "../frontend/static/Imagenes/ImagenBMW.jpg", "BMW"));
        
        cars.add(new Car(2, "Audi A4", "32,500€", 2021, 22000, "Gasolina", 
                "150 CV", "Manual", 
                "Elegante berlina con diseño sofisticado y motor eficiente. Perfecto equilibrio entre deportividad y confort.",
                Arrays.asList("Faros LED", "Tapicería mixta", "Control de crucero", "Conexión Bluetooth"),
                "../frontend/static/Imagenes/ImagenAudi.webp", "Audi"));
        
        cars.add(new Car(3, "Mercedes Clase C", "38,750€", 2023, 8500, "Híbrido", 
                "204 CV", "Automático", 
                "Lujo y tecnología híbrida en perfecta armonía. Bajo consumo y máximo confort para el conductor exigente.",
                Arrays.asList("Pantalla táctil 10.25\"", "Asientos eléctricos", "Sistema de navegación", "Cámara trasera"),
                "../frontend/static/Imagenes/ImagenMercedes.webp", "Mercedes"));
        
        cars.add(new Car(4, "Volkswagen Golf", "24,300€", 2022, 18000, "Gasolina", 
                "130 CV", "Manual", 
                "El compacto más versátil del mercado. Ideal para ciudad y carretera con excelente relación calidad-precio.",
                Arrays.asList("Car Play", "Sensores de aparcamiento", "Volante multifunción", "Ordenador de viaje"),
                "../frontend/static/Imagenes/ImagenGolf.jpeg", "Volkswagen"));
        
        cars.add(new Car(5, "Toyota RAV4", "31,200€", 2021, 28000, "Híbrido", 
                "218 CV", "Automático", 
                "SUV híbrido con tracción integral. Perfecto para familias aventureras que buscan eficiencia y espacio.",
                Arrays.asList("Tracción 4x4", "Cámara 360°", "Techo solar", "Sistema de seguridad Toyota Safety Sense"),
                "../frontend/static/Imagenes/ImagenToyota.webp", "Toyota"));
        
        cars.add(new Car(6, "Ford Focus", "21,800€", 2022, 12000, "Gasolina", 
                "125 CV", "Manual", 
                "Compacto dinámico con tecnología intuitiva. Diseño moderno y conducción ágil para el día a día.",
                Arrays.asList("SYNC 3", "Control por voz", "Asistente de mantenimiento de carril", "Arranque sin llave"),
                "../frontend/static/Imagenes/ImagenFord.jpg", "Ford"));
    }

    public List<Car> getAllCars() {
        return new ArrayList<>(cars);
    }

    public List<Car> searchCars(String searchText, String brand, String fuel, 
                                Double minPrice, Double maxPrice, 
                                Integer maxKm, Integer minYear) {
        return cars.stream()
                .filter(car -> matchesSearch(car, searchText, brand, fuel, minPrice, maxPrice, maxKm, minYear))
                .collect(Collectors.toList());
    }

    private boolean matchesSearch(Car car, String searchText, String brand, String fuel,
                                 Double minPrice, Double maxPrice, Integer maxKm, Integer minYear) {
        // Búsqueda por texto
        if (searchText != null && !searchText.trim().isEmpty()) {
            String searchLower = searchText.toLowerCase();
            if (!car.getName().toLowerCase().contains(searchLower) &&
                !car.getDescription().toLowerCase().contains(searchLower) &&
                !car.getBrand().toLowerCase().contains(searchLower)) {
                return false;
            }
        }

        // Filtro por marca
        if (brand != null && !brand.isEmpty() && !brand.equals("Todas")) {
            if (!car.getBrand().equals(brand)) {
                return false;
            }
        }

        // Filtro por combustible
        if (fuel != null && !fuel.isEmpty() && !fuel.equals("Todos")) {
            if (!car.getFuel().equals(fuel)) {
                return false;
            }
        }

        // Filtro por precio
        double carPrice = car.getPriceAsDouble();
        if (minPrice != null && carPrice < minPrice) {
            return false;
        }
        if (maxPrice != null && carPrice > maxPrice) {
            return false;
        }

        // Filtro por kilometraje
        if (maxKm != null && car.getKm() > maxKm) {
            return false;
        }

        // Filtro por año mínimo
        if (minYear != null && car.getYear() < minYear) {
            return false;
        }

        return true;
    }

    public List<String> getBrands() {
        return cars.stream()
                .map(Car::getBrand)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<String> getFuelTypes() {
        return cars.stream()
                .map(Car::getFuel)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}


