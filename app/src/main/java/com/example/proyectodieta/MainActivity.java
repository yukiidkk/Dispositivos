package com.example.proyectodieta;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView txtFecha;
    private ListView listDietas;
    private BaseDatosDieta db;
    private DietaAdapter dietaAdapter;
    private List<Dieta> dietas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new BaseDatosDieta(this);

        // Lógica de reinicio semanal
        if (db.semanaExpirada()) {
            db.reiniciarIngredientesConsumidos();
            db.reiniciarSemana();
            Toast.makeText(this, "¡Nueva semana! Se ha reiniciado tu progreso.", Toast.LENGTH_LONG).show();
        } else {
            // Si no ha expirado y ya hay una dieta, ir directo a ella
            int idDietaGuardada = db.obtenerDietaGuardada();
            if (idDietaGuardada != -1) {
                lanzarPlatillosActivity(idDietaGuardada);
                finish();
                return;
            }
        }

        // Inicialización de vistas
        txtFecha = findViewById(R.id.txtFecha);
        listDietas = findViewById(R.id.listDietas);

        // Mostrar fecha
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd 'de' MMMM", new Locale("es", "ES"));
        txtFecha.setText(sdf.format(new Date()));

        cargarDietasEnLista();
    }

    private void cargarDietasEnLista() {
        dietas = db.obtenerTodasLasDietas();
        dietaAdapter = new DietaAdapter(this, dietas);
        listDietas.setAdapter(dietaAdapter);

        listDietas.setOnItemClickListener((parent, view, position, id) -> {
            Dieta dietaSeleccionada = (Dieta) parent.getItemAtPosition(position);
            db.guardarDietaElegida(dietaSeleccionada.getIdDieta());
            lanzarPlatillosActivity(dietaSeleccionada.getIdDieta());
        });
    }

    private void lanzarPlatillosActivity(int idDieta) {
        Intent intent = new Intent(MainActivity.this, PlatillosActivity.class);
        intent.putExtra("ID_DIETA", idDieta);
        startActivity(intent);
    }
}
