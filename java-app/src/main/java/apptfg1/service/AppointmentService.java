package apptfg1.service;

import apptfg1.model.Appointment;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AppointmentService {
    private static final String APPOINTMENTS_FILE = "citas.json";
    private ObjectMapper objectMapper;

    public AppointmentService() {
        this.objectMapper = new ObjectMapper();
    }

    private File getAppointmentsFile() {
        // Intentar encontrar el archivo en diferentes ubicaciones
        // Busca en: raiz del proyecto, backend/data/, y directorio actual
        String[] possiblePaths = {
            "../backend/data/" + APPOINTMENTS_FILE,  // Desde java-app hacia backend/data
            "../../backend/data/" + APPOINTMENTS_FILE,  // Desde subdirectorios
            APPOINTMENTS_FILE,  // Directorio actual
            Paths.get(System.getProperty("user.dir"), "backend", "data", APPOINTMENTS_FILE).toString()
        };
        
        for (String path : possiblePaths) {
            File file = new File(path);
            if (file.exists()) {
                return file;
            }
        }
        
        // Si no se encuentra, devolver el archivo en el directorio actual
        return new File(APPOINTMENTS_FILE);
    }

    public List<Appointment> loadAppointments() {
        File file = getAppointmentsFile();
        if (file.exists()) {
            try {
                Appointment[] appointments = objectMapper.readValue(file, Appointment[].class);
                return new ArrayList<>(Arrays.asList(appointments));
            } catch (IOException e) {
                System.err.println("Error cargando citas: " + e.getMessage());
            }
        }
        return new ArrayList<>();
    }

    public List<Appointment> getAppointmentsByUser(String userName) {
        List<Appointment> allAppointments = loadAppointments();
        return allAppointments.stream()
                .filter(apt -> apt.getName().equalsIgnoreCase(userName))
                .collect(Collectors.toList());
    }
}

