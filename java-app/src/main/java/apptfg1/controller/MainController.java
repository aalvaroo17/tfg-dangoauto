package apptfg1.controller;

import apptfg1.Main;
import apptfg1.model.Appointment;
import apptfg1.model.Car;
import apptfg1.model.User;
import apptfg1.service.AppointmentService;
import apptfg1.service.CarService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.List;

public class MainController {
    @FXML
    private Label userLabel;
    @FXML
    private Button logoutButton;
    @FXML
    private TabPane mainTabPane;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> brandComboBox;
    @FXML
    private ComboBox<String> fuelComboBox;
    @FXML
    private TextField minPriceField;
    @FXML
    private TextField maxPriceField;
    @FXML
    private TextField maxKmField;
    @FXML
    private TextField minYearField;
    @FXML
    private FlowPane carsFlowPane;
    @FXML
    private VBox appointmentsVBox;
    @FXML
    private Label appointmentsTitle;
    @FXML
    private Label noAppointmentsLabel;
    @FXML
    private Hyperlink webLink;

    private User currentUser;
    private CarService carService;
    private AppointmentService appointmentService;

    @FXML
    public void initialize() {
        carService = new CarService();
        appointmentService = new AppointmentService();
        
        // Inicializar combos
        brandComboBox.getItems().add("Todas");
        brandComboBox.getItems().addAll(carService.getBrands());
        brandComboBox.setValue("Todas");
        
        fuelComboBox.getItems().add("Todos");
        fuelComboBox.getItems().addAll(carService.getFuelTypes());
        fuelComboBox.setValue("Todos");
        
        // Cargar todos los coches inicialmente
        loadCars(carService.getAllCars());
        
        // Listener para cambios en los filtros
        brandComboBox.setOnAction(e -> handleSearch());
        fuelComboBox.setOnAction(e -> handleSearch());
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        userLabel.setText("Bienvenido, " + user.getName());
        loadAppointments();
        
        // Cambiar al tab de citas cuando se carga
        mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab.getText().equals("Mis Citas")) {
                loadAppointments();
            }
        });
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().trim();
        String brand = brandComboBox.getValue();
        String fuel = fuelComboBox.getValue();
        
        Double minPrice = null;
        Double maxPrice = null;
        Integer maxKm = null;
        Integer minYear = null;
        
        try {
            if (!minPriceField.getText().trim().isEmpty()) {
                minPrice = Double.parseDouble(minPriceField.getText().trim());
            }
            if (!maxPriceField.getText().trim().isEmpty()) {
                maxPrice = Double.parseDouble(maxPriceField.getText().trim());
            }
            if (!maxKmField.getText().trim().isEmpty()) {
                maxKm = Integer.parseInt(maxKmField.getText().trim());
            }
            if (!minYearField.getText().trim().isEmpty()) {
                minYear = Integer.parseInt(minYearField.getText().trim());
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Por favor, introduce valores numéricos válidos en los filtros");
            return;
        }
        
        List<Car> filteredCars = carService.searchCars(searchText, brand, fuel, minPrice, maxPrice, maxKm, minYear);
        loadCars(filteredCars);
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        brandComboBox.setValue("Todas");
        fuelComboBox.setValue("Todos");
        minPriceField.clear();
        maxPriceField.clear();
        maxKmField.clear();
        minYearField.clear();
        loadCars(carService.getAllCars());
    }

    private void loadCars(List<Car> cars) {
        carsFlowPane.getChildren().clear();
        
        if (cars.isEmpty()) {
            Label noResults = new Label("No se encontraron coches con los filtros seleccionados");
            noResults.setFont(new Font(16));
            noResults.setStyle("-fx-text-fill: #7f8c8d;");
            carsFlowPane.getChildren().add(noResults);
            return;
        }
        
        for (Car car : cars) {
            VBox carCard = createCarCard(car);
            carsFlowPane.getChildren().add(carCard);
        }
    }

    private VBox createCarCard(Car car) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);");
        card.setPrefWidth(300);
        card.setPadding(new Insets(15));
        
        // Imagen
        ImageView imageView = new ImageView();
        imageView.setFitWidth(270);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);
        try {
            // Intentar diferentes rutas para la imagen
            String[] possiblePaths = {
                car.getImage(),
                "../" + car.getImage(),
                "../../" + car.getImage(),
                Paths.get(System.getProperty("user.dir"), car.getImage()).toString()
            };
            
            File imageFile = null;
            for (String path : possiblePaths) {
                File testFile = new File(path);
                if (testFile.exists()) {
                    imageFile = testFile;
                    break;
                }
            }
            
            if (imageFile != null && imageFile.exists()) {
                Image image = new Image(imageFile.toURI().toString());
                imageView.setImage(image);
            } else {
                imageView.setStyle("-fx-background-color: #ecf0f1;");
            }
        } catch (Exception e) {
            imageView.setStyle("-fx-background-color: #ecf0f1;");
        }
        
        // Información
        Label nameLabel = new Label(car.getName());
        nameLabel.setFont(new Font("System Bold", 16));
        nameLabel.setStyle("-fx-text-fill: #2c3e50;");
        
        Label priceLabel = new Label(car.getPrice());
        priceLabel.setFont(new Font("System Bold", 18));
        priceLabel.setStyle("-fx-text-fill: #e74c3c;");
        
        Label detailsLabel = new Label(String.format("%d • %s km • %s", car.getYear(), 
                String.format("%,d", car.getKm()), car.getFuel()));
        detailsLabel.setStyle("-fx-text-fill: #7f8c8d;");
        
        Button infoButton = new Button("Más Información");
        infoButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5;");
        infoButton.setMaxWidth(Double.MAX_VALUE);
        infoButton.setOnAction(e -> showCarDetails(car));
        
        card.getChildren().addAll(imageView, nameLabel, priceLabel, detailsLabel, infoButton);
        
        return card;
    }

    private void showCarDetails(Car car) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalles del Vehículo");
        alert.setHeaderText(car.getName());
        
        StringBuilder details = new StringBuilder();
        details.append("Precio: ").append(car.getPrice()).append("\n\n");
        details.append("Especificaciones:\n");
        details.append("• Año: ").append(car.getYear()).append("\n");
        details.append("• Kilómetros: ").append(String.format("%,d", car.getKm())).append(" km\n");
        details.append("• Combustible: ").append(car.getFuel()).append("\n");
        details.append("• Potencia: ").append(car.getPower()).append("\n");
        details.append("• Transmisión: ").append(car.getTransmission()).append("\n\n");
        details.append("Equipamiento:\n");
        for (String feature : car.getFeatures()) {
            details.append("• ").append(feature).append("\n");
        }
        details.append("\n").append(car.getDescription());
        
        alert.setContentText(details.toString());
        alert.setResizable(true);
        alert.getDialogPane().setPrefWidth(500);
        alert.showAndWait();
    }

    private void loadAppointments() {
        appointmentsVBox.getChildren().clear();
        
        if (currentUser == null) {
            return;
        }
        
        List<Appointment> appointments = appointmentService.getAppointmentsByUser(currentUser.getName());
        
        if (appointments.isEmpty()) {
            appointmentsTitle.setVisible(false);
            noAppointmentsLabel.setVisible(true);
            webLink.setVisible(true);
        } else {
            appointmentsTitle.setVisible(true);
            noAppointmentsLabel.setVisible(false);
            webLink.setVisible(false);
            
            for (Appointment apt : appointments) {
                VBox appointmentCard = createAppointmentCard(apt);
                appointmentsVBox.getChildren().add(appointmentCard);
            }
        }
    }

    private VBox createAppointmentCard(Appointment apt) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2); -fx-padding: 15;");
        card.setMaxWidth(800);
        
        Label referenceLabel = new Label("Referencia: " + apt.getReference());
        referenceLabel.setFont(new Font("System Bold", 14));
        referenceLabel.setStyle("-fx-text-fill: #3498db;");
        
        Label dateLabel = new Label("Fecha: " + apt.getDate() + " a las " + apt.getTime());
        dateLabel.setFont(new Font(14));
        
        Label statusLabel = new Label("Estado: " + apt.getStatus());
        statusLabel.setFont(new Font(14));
        if (apt.getStatus().equals("confirmada")) {
            statusLabel.setStyle("-fx-text-fill: #27ae60;");
        } else {
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
        }
        
        if (apt.getPhone() != null && !apt.getPhone().isEmpty()) {
            Label phoneLabel = new Label("Teléfono: " + apt.getPhone());
            phoneLabel.setFont(new Font(12));
            phoneLabel.setStyle("-fx-text-fill: #7f8c8d;");
            card.getChildren().add(phoneLabel);
        }
        
        card.getChildren().addAll(referenceLabel, dateLabel, statusLabel);
        
        return card;
    }

    @FXML
    private void openWebPage() {
        try {
            Desktop.getDesktop().browse(new URI("http://localhost:5000"));
        } catch (Exception e) {
            showAlert("Error", "No se pudo abrir la página web. Asegúrate de que el servidor esté ejecutándose.");
        }
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/login.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Scene scene = new Scene(root, 600, 500);
            stage.setScene(scene);
            stage.setTitle("DangoAuto - Iniciar Sesión");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

