package apptfg1;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            // Cargar el FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();
            
            // Configurar la escena
            Scene scene = new Scene(root, 600, 500);
            primaryStage.setTitle("DangoAuto - Iniciar Sesión");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (Exception e) {
            System.err.println("Error al cargar la aplicación: " + e.getMessage());
            e.printStackTrace();
            
            // Mostrar ventana de error simple
            try {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Error al iniciar la aplicación");
                alert.setContentText("No se pudo cargar la interfaz. Verifica que todos los archivos estén presentes.\n\nError: " + e.getMessage());
                alert.showAndWait();
            } catch (Exception ex) {
                // Si no se puede mostrar el alert, mostrar en consola
                System.err.println("Error crítico: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        try {
            // Inicializar JavaFX
            // Si JavaFX está embebido en el JAR, funcionará sin módulos
            launch(args);
        } catch (Exception e) {
            System.err.println("Error fatal al iniciar JavaFX: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}


