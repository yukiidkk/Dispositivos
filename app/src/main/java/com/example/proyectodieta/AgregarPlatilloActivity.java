package com.example.proyectodieta;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.List;

public class AgregarPlatilloActivity extends AppCompatActivity {

    private EditText editNombrePlatillo;
    private ListView listTipoComida, listIngredientesDisponibles;
    private Button btnAgregarPlatillo;

    private BaseDatosDieta db;
    private int idDieta;
    private IngredienteAdapter ingredienteAdapter;
    private TipoComidaAdapter tipoComidaAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_platillo);

        db = new BaseDatosDieta(this);
        idDieta = getIntent().getIntExtra("ID_DIETA", -1);
        if (idDieta == -1) {
            Toast.makeText(this, "Error: Dieta no válida", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inicializar vistas
        editNombrePlatillo = findViewById(R.id.editNombrePlatillo);
        listTipoComida = findViewById(R.id.listTipoComida);
        listIngredientesDisponibles = findViewById(R.id.listIngredientesDisponibles);
        btnAgregarPlatillo = findViewById(R.id.btnAgregarPlatillo);

        configurarListaTipoComida();
        configurarListaIngredientes();

        // Listener para el botón de agregar platillo
        btnAgregarPlatillo.setOnClickListener(v -> guardarPlatillo());
    }

    private void configurarListaTipoComida() {
        List<String> tipos = Arrays.asList("Desayuno", "Almuerzo", "Comida", "Colación", "Cena");
        tipoComidaAdapter = new TipoComidaAdapter(this, tipos);
        listTipoComida.setAdapter(tipoComidaAdapter);

        listTipoComida.setOnItemClickListener((parent, view, position, id) -> {
            tipoComidaAdapter.setSelectedPosition(position);
        });
    }

    private void configurarListaIngredientes() {
        List<Ingrediente> ingredientesPermitidos = db.obtenerIngredientesPorDieta(idDieta);
        ingredienteAdapter = new IngredienteAdapter(this, ingredientesPermitidos);
        listIngredientesDisponibles.setAdapter(ingredienteAdapter);
    }

    private void guardarPlatillo() {
        String nombre = editNombrePlatillo.getText().toString().trim();
        String tipoComidaSeleccionado = tipoComidaAdapter.getSelectedItem();
        List<Integer> idsIngredientesSeleccionados = ingredienteAdapter.getSeleccionados();

        // Validaciones
        if (TextUtils.isEmpty(nombre)) {
            Toast.makeText(this, "El nombre del platillo no puede estar vacío", Toast.LENGTH_SHORT).show();
            return;
        }
        if (tipoComidaSeleccionado == null) {
            Toast.makeText(this, "Debes seleccionar un tipo de comida", Toast.LENGTH_SHORT).show();
            return;
        }
        if (idsIngredientesSeleccionados.size() < 3) {
            Toast.makeText(this, "Debes seleccionar al menos 3 ingredientes", Toast.LENGTH_SHORT).show();
            return;
        }

        // Insertar platillo y sus ingredientes
        long idPlatilloNuevo = db.insertarPlatillo(idDieta, nombre, tipoComidaSeleccionado);
        if (idPlatilloNuevo != -1) {
            for (int idIngrediente : idsIngredientesSeleccionados) {
                db.agregarIngredienteAPlatillo((int) idPlatilloNuevo, idIngrediente);
            }
            Toast.makeText(this, "Platillo añadido correctamente", Toast.LENGTH_SHORT).show();
            setResult(Activity.RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Error al guardar el platillo", Toast.LENGTH_SHORT).show();
        }
    }
}
