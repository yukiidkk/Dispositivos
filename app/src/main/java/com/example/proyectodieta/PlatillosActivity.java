package com.example.proyectodieta;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PlatillosActivity extends AppCompatActivity {

    private TextView txtNombreDieta, txtFechaPlatillos;
    private Button btnDesayuno, btnAlmuerzo, btnComida, btnColacion, btnCena, btnAgregarPlatillo, btnVerConsumidos;
    private ListView listPlatillos;

    private BaseDatosDieta db;
    private Dieta dieta;
    private List<Platillo> todosLosPlatillos;
    private PlatilloAdapter adapter;

    private static final int REQUEST_CODE_AGREGAR_PLATILLO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_platillos);

        db = new BaseDatosDieta(this);

        // Obtener ID de la dieta desde el Intent
        int idDieta = getIntent().getIntExtra("ID_DIETA", -1);
        if (idDieta == -1) {
            Toast.makeText(this, "Error: Dieta no encontrada", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        dieta = db.obtenerDietaPorId(idDieta);
        if (dieta == null) {
            Toast.makeText(this, "Error: Dieta no encontrada", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inicializar vistas
        txtNombreDieta = findViewById(R.id.txtNombreDieta);
        txtFechaPlatillos = findViewById(R.id.txtFechaPlatillos);
        listPlatillos = findViewById(R.id.listPlatillos);
        btnDesayuno = findViewById(R.id.btnDesayuno);
        btnAlmuerzo = findViewById(R.id.btnAlmuerzo);
        btnComida = findViewById(R.id.btnComida);
        btnColacion = findViewById(R.id.btnColacion);
        btnCena = findViewById(R.id.btnCena);
        btnAgregarPlatillo = findViewById(R.id.btnAgregarPlatillo);
        btnVerConsumidos = findViewById(R.id.btnVerConsumidos);

        // Rellenar datos
        txtNombreDieta.setText(dieta.getNombre());
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd 'de' MMMM", new Locale("es", "ES"));
        txtFechaPlatillos.setText(sdf.format(new Date()));

        cargarPlatillos();

        // Listeners para filtros
        View.OnClickListener filtroListener = v -> {
            Button b = (Button) v;
            filtrarPlatillosPorTipo(b.getText().toString());
        };
        btnDesayuno.setOnClickListener(filtroListener);
        btnAlmuerzo.setOnClickListener(filtroListener);
        btnComida.setOnClickListener(filtroListener);
        btnColacion.setOnClickListener(filtroListener);
        btnCena.setOnClickListener(filtroListener);

        // Listener para abrir detalles del platillo
        listPlatillos.setOnItemClickListener((parent, view, position, id) -> {
            Platillo platilloSeleccionado = (Platillo) parent.getItemAtPosition(position);
            Intent intent = new Intent(PlatillosActivity.this, DetallePlatilloActivity.class);
            intent.putExtra("ID_PLATILLO", platilloSeleccionado.getIdPlatillo());
            startActivity(intent);
        });

        // Listener para agregar nuevo platillo
        btnAgregarPlatillo.setOnClickListener(v -> {
            Intent intent = new Intent(PlatillosActivity.this, AgregarPlatilloActivity.class);
            intent.putExtra("ID_DIETA", dieta.getIdDieta());
            startActivityForResult(intent, REQUEST_CODE_AGREGAR_PLATILLO);
        });

        // Listener para ver ingredientes consumidos
        btnVerConsumidos.setOnClickListener(v -> {
            Intent intent = new Intent(PlatillosActivity.this, ConsumidosActivity.class);
            startActivity(intent);
        });
    }

    private void cargarPlatillos() {
        todosLosPlatillos = db.obtenerPlatillosPorDieta(dieta.getIdDieta());
        adapter = new PlatilloAdapter(this, todosLosPlatillos);
        listPlatillos.setAdapter(adapter);
    }

    private void filtrarPlatillosPorTipo(String tipo) {
        List<Platillo> platillosFiltrados = new ArrayList<>();
        for (Platillo p : todosLosPlatillos) {
            if (p.getTipoComida().equalsIgnoreCase(tipo)) {
                platillosFiltrados.add(p);
            }
        }
        adapter = new PlatilloAdapter(this, platillosFiltrados);
        listPlatillos.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_AGREGAR_PLATILLO && resultCode == RESULT_OK) {
            // Recargar la lista de platillos si se agregó uno nuevo
            cargarPlatillos();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarPlatillos();
    }
}
