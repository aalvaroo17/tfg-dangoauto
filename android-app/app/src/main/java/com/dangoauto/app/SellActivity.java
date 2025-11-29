package com.dangoauto.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.ContentResolver;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import android.util.Base64;

public class SellActivity extends AppCompatActivity {

    private static final String API_BASE_URL = "https://tfg-dangoauto.onrender.com";
    private static final int PICK_IMAGE_REQUEST = 1;
    
    private TextInputEditText editTextDni;
    private TextInputEditText editTextTelefono;
    private TextInputEditText editTextEmail;
    private TextInputEditText editTextCoche;
    private TextInputEditText editTextMatricula;
    private TextInputEditText editTextKilometros;
    private TextInputEditText editTextCombustible;
    private TextInputEditText editTextPrecio;
    private TextInputEditText editTextAño;
    private TextInputEditText editTextPotencia;
    private TextInputEditText editTextTransmision;
    private TextInputEditText editTextDescripcion;
    private TextInputEditText editTextCaracteristicas;
    private Button btnSeleccionarFotos;
    private Button btnSubirInfo;
    private RecyclerView recyclerViewFotos;
    
    private List<Uri> selectedImages;
    private SelectedPhotosAdapter photosAdapter;
    private ExecutorService executorService;
    private OkHttpClient httpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_sell);

            // Inicializar cliente HTTP
            httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();
            executorService = Executors.newSingleThreadExecutor();

            selectedImages = new ArrayList<>();

            // Inicializar vistas
            editTextDni = findViewById(R.id.editTextDni);
            editTextTelefono = findViewById(R.id.editTextTelefono);
            editTextEmail = findViewById(R.id.editTextEmail);
            editTextCoche = findViewById(R.id.editTextCoche);
            editTextMatricula = findViewById(R.id.editTextMatricula);
            editTextKilometros = findViewById(R.id.editTextKilometros);
            editTextCombustible = findViewById(R.id.editTextCombustible);
            editTextPrecio = findViewById(R.id.editTextPrecio);
            editTextAño = findViewById(R.id.editTextAño);
            editTextPotencia = findViewById(R.id.editTextPotencia);
            editTextTransmision = findViewById(R.id.editTextTransmision);
            editTextDescripcion = findViewById(R.id.editTextDescripcion);
            editTextCaracteristicas = findViewById(R.id.editTextCaracteristicas);
            btnSeleccionarFotos = findViewById(R.id.btnSeleccionarFotos);
            btnSubirInfo = findViewById(R.id.btnSubirInfo);
            recyclerViewFotos = findViewById(R.id.recyclerViewFotos);

            // Configurar RecyclerView de fotos
            if (recyclerViewFotos != null) {
                recyclerViewFotos.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                photosAdapter = new SelectedPhotosAdapter(selectedImages, position -> {
                    if (position >= 0 && position < selectedImages.size()) {
                        selectedImages.remove(position);
                        photosAdapter.notifyItemRemoved(position);
                        photosAdapter.notifyItemRangeChanged(position, selectedImages.size());
                        updatePhotosVisibility();
                    }
                });
                recyclerViewFotos.setAdapter(photosAdapter);
                updatePhotosVisibility();
            }

            // Configurar listener del botón de seleccionar fotos
            if (btnSeleccionarFotos != null) {
                btnSeleccionarFotos.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    startActivityForResult(Intent.createChooser(intent, "Seleccionar fotos"), PICK_IMAGE_REQUEST);
                });
            }

            // Configurar listener del botón de subir
            if (btnSubirInfo != null) {
                btnSubirInfo.setOnClickListener(v -> {
                    try {
                        if (validateFields()) {
                            submitCar();
                        } else {
                            Toast.makeText(SellActivity.this, 
                                getString(R.string.campos_vacios), 
                                Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        android.util.Log.e("SellActivity", "Error en onClick", e);
                        Toast.makeText(SellActivity.this, "Error al procesar", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            
            // Configurar validación en tiempo real
            setupRealTimeValidation();
            
        } catch (Exception e) {
            android.util.Log.e("SellActivity", "Error crítico en onCreate", e);
            Toast.makeText(this, "Error al cargar la pantalla", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                if (data.getClipData() != null) {
                    // Múltiples imágenes
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        selectedImages.add(imageUri);
                    }
                } else if (data.getData() != null) {
                    // Una sola imagen
                    selectedImages.add(data.getData());
                }
                
                if (!selectedImages.isEmpty()) {
                    Toast.makeText(this, selectedImages.size() + " foto(s) seleccionada(s)", Toast.LENGTH_SHORT).show();
                    if (photosAdapter != null) {
                        photosAdapter.notifyDataSetChanged();
                    }
                    updatePhotosVisibility();
                }
            }
        }
    }

    private boolean validateFields() {
        try {
            String dni = editTextDni != null ? editTextDni.getText().toString().trim() : "";
            String telefono = editTextTelefono != null ? editTextTelefono.getText().toString().trim() : "";
            String email = editTextEmail != null ? editTextEmail.getText().toString().trim() : "";
            String coche = editTextCoche != null ? editTextCoche.getText().toString().trim() : "";
            String matricula = editTextMatricula != null ? editTextMatricula.getText().toString().trim() : "";
            String kilometros = editTextKilometros != null ? editTextKilometros.getText().toString().trim() : "";
            String combustible = editTextCombustible != null ? editTextCombustible.getText().toString().trim() : "";
            String precio = editTextPrecio != null ? editTextPrecio.getText().toString().trim() : "";
            String año = editTextAño != null ? editTextAño.getText().toString().trim() : "";
            String descripcion = editTextDescripcion != null ? editTextDescripcion.getText().toString().trim() : "";

            return !dni.isEmpty() && !telefono.isEmpty() && !email.isEmpty() 
                && !coche.isEmpty() && !matricula.isEmpty() && !kilometros.isEmpty() 
                && !combustible.isEmpty() && !precio.isEmpty() && !año.isEmpty();
        } catch (Exception e) {
            android.util.Log.e("SellActivity", "Error en validateFields", e);
            return false;
        }
    }

    private void submitCar() {
        try {
            if (btnSubirInfo != null) {
                btnSubirInfo.setEnabled(false);
                btnSubirInfo.setText("Subiendo...");
            }

            executorService.execute(() -> {
                try {
                    // Preparar datos del coche
                    String cocheText = editTextCoche.getText().toString().trim();
                    String[] cocheParts = cocheText.split(" ", 2);
                    String brand = cocheParts.length > 0 ? cocheParts[0] : "";
                    String model = cocheParts.length > 1 ? cocheParts[1] : cocheText;

                    JSONObject carJson = new JSONObject();
                    carJson.put("name", cocheText);
                    carJson.put("brand", brand);
                    carJson.put("model", model);
                    carJson.put("price", Double.parseDouble(editTextPrecio.getText().toString().trim()));
                    carJson.put("year", Integer.parseInt(editTextAño.getText().toString().trim()));
                    carJson.put("km", Integer.parseInt(editTextKilometros.getText().toString().trim()));
                    carJson.put("fuel", editTextCombustible.getText().toString().trim());
                    carJson.put("power", editTextPotencia != null && editTextPotencia.getText() != null ? editTextPotencia.getText().toString().trim() : "");
                    carJson.put("transmission", editTextTransmision != null && editTextTransmision.getText() != null ? editTextTransmision.getText().toString().trim() : "");
                    carJson.put("licensePlate", editTextMatricula.getText().toString().trim());
                    carJson.put("description", editTextDescripcion.getText().toString().trim());
                    
                    // Características (convertir string separado por comas a array)
                    String caracteristicasStr = editTextCaracteristicas.getText().toString().trim();
                    JSONArray featuresArray = new JSONArray();
                    if (!caracteristicasStr.isEmpty()) {
                        String[] features = caracteristicasStr.split(",");
                        for (String feature : features) {
                            String trimmed = feature.trim();
                            if (!trimmed.isEmpty()) {
                                featuresArray.put(trimmed);
                            }
                        }
                    }
                    carJson.put("features", featuresArray);
                    
                    // Datos del vendedor
                    carJson.put("sellerDni", editTextDni.getText().toString().trim());
                    carJson.put("sellerPhone", editTextTelefono.getText().toString().trim());
                    carJson.put("sellerEmail", editTextEmail.getText().toString().trim());
                    
                    // Convertir imágenes a base64 y añadirlas al array
                    JSONArray imagesArray = new JSONArray();
                    for (Uri imageUri : selectedImages) {
                        try {
                            String base64Image = convertImageToBase64(imageUri);
                            if (base64Image != null && !base64Image.isEmpty()) {
                                // Enviar como data URL para que el backend pueda procesarlo
                                imagesArray.put("data:image/jpeg;base64," + base64Image);
                            }
                        } catch (Exception e) {
                            android.util.Log.e("SellActivity", "Error convirtiendo imagen a base64", e);
                        }
                    }
                    carJson.put("images", imagesArray);

                    RequestBody body = RequestBody.create(
                        carJson.toString(),
                        MediaType.parse("application/json")
                    );

                    Request request = new Request.Builder()
                        .url(API_BASE_URL + "/api/cars")
                        .post(body)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Accept", "application/json")
                        .build();

                    try (Response response = httpClient.newCall(request).execute()) {
                        String responseBody = response.body() != null ? response.body().string() : "";
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        
                        runOnUiThread(() -> {
                            if (btnSubirInfo != null) {
                                btnSubirInfo.setEnabled(true);
                                btnSubirInfo.setText(getString(R.string.btn_subir_info));
                            }
                            
                            if (jsonResponse.optBoolean("success", false)) {
                                Toast.makeText(SellActivity.this, 
                                    getString(R.string.info_subida), 
                                    Toast.LENGTH_SHORT).show();
                                
                                // Limpiar campos
                                clearFields();
                                
                                // Notificar que se actualice la lista (se puede hacer con un broadcast o volver a SearchActivity)
                                setResult(RESULT_OK);
                            } else {
                                String message = jsonResponse.optString("message", "Error al subir el coche");
                                Toast.makeText(SellActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        if (btnSubirInfo != null) {
                            btnSubirInfo.setEnabled(true);
                            btnSubirInfo.setText(getString(R.string.btn_subir_info));
                        }
                        android.util.Log.e("SellActivity", "Error subiendo coche", e);
                        Toast.makeText(SellActivity.this, "Error de conexión. Verifica tu internet.", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } catch (Exception e) {
            android.util.Log.e("SellActivity", "Error en submitCar", e);
            if (btnSubirInfo != null) {
                btnSubirInfo.setEnabled(true);
                btnSubirInfo.setText(getString(R.string.btn_subir_info));
            }
            Toast.makeText(this, "Error al procesar", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearFields() {
        if (editTextDni != null) editTextDni.setText("");
        if (editTextTelefono != null) editTextTelefono.setText("");
        if (editTextEmail != null) editTextEmail.setText("");
        if (editTextCoche != null) editTextCoche.setText("");
        if (editTextMatricula != null) editTextMatricula.setText("");
        if (editTextKilometros != null) editTextKilometros.setText("");
        if (editTextCombustible != null) editTextCombustible.setText("");
        if (editTextPrecio != null) editTextPrecio.setText("");
        if (editTextAño != null) editTextAño.setText("");
        if (editTextPotencia != null) editTextPotencia.setText("");
        if (editTextTransmision != null) editTextTransmision.setText("");
        if (editTextDescripcion != null) editTextDescripcion.setText("");
        if (editTextCaracteristicas != null) editTextCaracteristicas.setText("");
        selectedImages.clear();
        if (photosAdapter != null) {
            photosAdapter.notifyDataSetChanged();
        }
        updatePhotosVisibility();
    }
    
    private void updatePhotosVisibility() {
        if (recyclerViewFotos != null) {
            if (selectedImages != null && !selectedImages.isEmpty()) {
                recyclerViewFotos.setVisibility(android.view.View.VISIBLE);
            } else {
                recyclerViewFotos.setVisibility(android.view.View.GONE);
            }
        }
    }
    
    private void setupRealTimeValidation() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                validateFieldInRealTime();
            }
        };
        
        if (editTextDni != null) editTextDni.addTextChangedListener(watcher);
        if (editTextTelefono != null) editTextTelefono.addTextChangedListener(watcher);
        if (editTextEmail != null) editTextEmail.addTextChangedListener(watcher);
        if (editTextCoche != null) editTextCoche.addTextChangedListener(watcher);
        if (editTextMatricula != null) editTextMatricula.addTextChangedListener(watcher);
        if (editTextKilometros != null) editTextKilometros.addTextChangedListener(watcher);
        if (editTextCombustible != null) editTextCombustible.addTextChangedListener(watcher);
        if (editTextPrecio != null) editTextPrecio.addTextChangedListener(watcher);
        if (editTextAño != null) editTextAño.addTextChangedListener(watcher);
    }
    
    private void validateFieldInRealTime() {
        // Validar DNI
        TextInputLayout layoutDni = findViewById(R.id.textInputLayoutDni);
        if (layoutDni != null && editTextDni != null) {
            String dni = editTextDni.getText().toString().trim();
            if (!dni.isEmpty() && dni.length() < 9) {
                layoutDni.setError("El DNI debe tener al menos 9 caracteres");
            } else {
                layoutDni.setError(null);
            }
        }
        
        // Validar Teléfono
        TextInputLayout layoutTelefono = findViewById(R.id.textInputLayoutTelefono);
        if (layoutTelefono != null && editTextTelefono != null) {
            String telefono = editTextTelefono.getText().toString().trim();
            if (!telefono.isEmpty() && (telefono.length() < 9 || !telefono.matches("\\d+"))) {
                layoutTelefono.setError("Teléfono inválido (mínimo 9 dígitos)");
            } else {
                layoutTelefono.setError(null);
            }
        }
        
        // Validar Email
        TextInputLayout layoutEmail = findViewById(R.id.textInputLayoutEmail);
        if (layoutEmail != null && editTextEmail != null) {
            String email = editTextEmail.getText().toString().trim();
            if (!email.isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                layoutEmail.setError("Email inválido");
            } else {
                layoutEmail.setError(null);
            }
        }
        
        // Validar Precio
        TextInputLayout layoutPrecio = findViewById(R.id.textInputLayoutPrecio);
        if (layoutPrecio != null && editTextPrecio != null) {
            String precio = editTextPrecio.getText().toString().trim();
            if (!precio.isEmpty()) {
                try {
                    double precioValue = Double.parseDouble(precio);
                    if (precioValue <= 0) {
                        layoutPrecio.setError("El precio debe ser mayor a 0");
                    } else {
                        layoutPrecio.setError(null);
                    }
                } catch (NumberFormatException e) {
                    layoutPrecio.setError("Precio inválido");
                }
            } else {
                layoutPrecio.setError(null);
            }
        }
        
        // Validar Año
        TextInputLayout layoutAño = findViewById(R.id.textInputLayoutAño);
        if (layoutAño != null && editTextAño != null) {
            String año = editTextAño.getText().toString().trim();
            if (!año.isEmpty()) {
                try {
                    int añoValue = Integer.parseInt(año);
                    int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
                    if (añoValue < 1900 || añoValue > currentYear + 1) {
                        layoutAño.setError("Año inválido (1900-" + (currentYear + 1) + ")");
                    } else {
                        layoutAño.setError(null);
                    }
                } catch (NumberFormatException e) {
                    layoutAño.setError("Año inválido");
                }
            } else {
                layoutAño.setError(null);
            }
        }
        
        // Validar Kilómetros
        TextInputLayout layoutKm = findViewById(R.id.textInputLayoutKilometros);
        if (layoutKm != null && editTextKilometros != null) {
            String km = editTextKilometros.getText().toString().trim();
            if (!km.isEmpty()) {
                try {
                    int kmValue = Integer.parseInt(km);
                    if (kmValue < 0) {
                        layoutKm.setError("Los kilómetros no pueden ser negativos");
                    } else {
                        layoutKm.setError(null);
                    }
                } catch (NumberFormatException e) {
                    layoutKm.setError("Kilómetros inválidos");
                }
            } else {
                layoutKm.setError(null);
            }
        }
    }
    
    /**
     * Convierte una URI de imagen a base64
     */
    private String convertImageToBase64(Uri imageUri) {
        try {
            ContentResolver contentResolver = getContentResolver();
            InputStream inputStream = contentResolver.openInputStream(imageUri);
            
            if (inputStream == null) {
                return null;
            }
            
            // Leer la imagen y redimensionarla si es muy grande (máximo 1024x1024)
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            
            if (bitmap == null) {
                return null;
            }
            
            // Redimensionar si es necesario (máximo 1024px en el lado más largo)
            int maxSize = 1024;
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            
            if (width > maxSize || height > maxSize) {
                float scale = Math.min((float) maxSize / width, (float) maxSize / height);
                int newWidth = Math.round(width * scale);
                int newHeight = Math.round(height * scale);
                bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
            }
            
            // Comprimir a JPEG con calidad 85%
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream);
            byte[] imageBytes = outputStream.toByteArray();
            
            // Convertir a base64
            return Base64.encodeToString(imageBytes, Base64.NO_WRAP);
        } catch (Exception e) {
            android.util.Log.e("SellActivity", "Error convirtiendo imagen", e);
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
