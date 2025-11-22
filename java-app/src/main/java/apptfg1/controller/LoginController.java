package apptfg1.controller;

import apptfg1.Main;
import apptfg1.model.User;
import apptfg1.service.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField emailField;
    @FXML
    private Label titleLabel;
    @FXML
    private Label errorLabel;
    @FXML
    private Button actionButton;
    @FXML
    private Button toggleButton;
    @FXML
    private HBox registerFields;

    private UserService userService;
    private boolean isRegisterMode = false;

    @FXML
    public void initialize() {
        userService = new UserService();
        errorLabel.setText("");
    }

    @FXML
    private void handleAction() {
        errorLabel.setText("");
        
        if (isRegisterMode) {
            handleRegister();
        } else {
            handleLogin();
        }
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Por favor, completa todos los campos");
            return;
        }

        User user = userService.loginUser(username, password);
        if (user != null) {
            try {
                loadMainView(user);
            } catch (IOException e) {
                errorLabel.setText("Error al cargar la aplicación");
                e.printStackTrace();
            }
        } else {
            errorLabel.setText("Usuario o contraseña incorrectos");
        }
    }

    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || name.isEmpty()) {
            errorLabel.setText("Por favor, completa los campos obligatorios (Usuario, Contraseña, Nombre)");
            return;
        }

        if (userService.userExists(username)) {
            errorLabel.setText("El usuario ya existe");
            return;
        }

        boolean success = userService.registerUser(username, password, name, phone, email);
        if (success) {
            errorLabel.setText("Registro exitoso. Inicia sesión ahora.");
            toggleMode();
        } else {
            errorLabel.setText("Error al registrar usuario");
        }
    }

    @FXML
    private void toggleMode() {
        isRegisterMode = !isRegisterMode;
        errorLabel.setText("");
        
        if (isRegisterMode) {
            titleLabel.setText("Registrarse");
            actionButton.setText("Registrarse");
            toggleButton.setText("¿Ya tienes cuenta? Inicia sesión");
            registerFields.setVisible(true);
            registerFields.setManaged(true);
            emailField.setVisible(true);
            emailField.setManaged(true);
        } else {
            titleLabel.setText("Iniciar Sesión");
            actionButton.setText("Iniciar Sesión");
            toggleButton.setText("¿No tienes cuenta? Regístrate");
            registerFields.setVisible(false);
            registerFields.setManaged(false);
            emailField.setVisible(false);
            emailField.setManaged(false);
        }
        
        usernameField.clear();
        passwordField.clear();
        nameField.clear();
        phoneField.clear();
        emailField.clear();
    }

    private void loadMainView(User user) throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/main.fxml"));
        Parent root = loader.load();
        
        MainController controller = loader.getController();
        controller.setCurrentUser(user);
        
        Stage stage = (Stage) usernameField.getScene().getWindow();
        Scene scene = new Scene(root, 1200, 800);
        stage.setScene(scene);
        stage.setTitle("DangoAuto - Catálogo");
        stage.centerOnScreen();
    }
}


