package com.example.proyectodieta;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class IngredienteAdapter extends BaseAdapter {

    private Context context;
    private List<Ingrediente> lista;
    private List<Integer> seleccionados = new ArrayList<>();
    private boolean seleccionHabilitada = true;

    public IngredienteAdapter(Context context, List<Ingrediente> lista) {
        this.context = context;
        this.lista = lista;
    }

    public IngredienteAdapter(Context context, List<Ingrediente> lista, boolean seleccionHabilitada) {
        this.context = context;
        this.lista = lista;
        this.seleccionHabilitada = seleccionHabilitada;
    }

    @Override
    public int getCount() {
        return lista.size();
    }

    @Override
    public Object getItem(int position) {
        return lista.get(position);
    }

    @Override
    public long getItemId(int position) {
        return lista.get(position).getIdIngrediente();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_ingrediente, parent, false);
        }

        Ingrediente ing = lista.get(position);
        TextView nombre = view.findViewById(R.id.txtNombreIngrediente);

        nombre.setText(ing.getNombre());

        if (seleccionHabilitada) {
            // Cambiar color según si está seleccionado o no
            if (seleccionados.contains(ing.getIdIngrediente())) {
                view.setBackgroundColor(Color.parseColor("#D1FFD1")); // verde
            } else {
                view.setBackgroundColor(Color.WHITE);
            }

            // Evento del click
            view.setOnClickListener(v -> {
                if (seleccionados.contains(ing.getIdIngrediente())) {
                    seleccionados.remove(Integer.valueOf(ing.getIdIngrediente()));
                } else {
                    if (seleccionados.size() < 10) {
                        seleccionados.add(ing.getIdIngrediente());
                    } else {
                        Toast.makeText(context, "No puedes seleccionar más de 10 ingredientes", Toast.LENGTH_SHORT).show();
                    }
                }
                notifyDataSetChanged();
            });
        } else {
            view.setBackgroundColor(Color.WHITE);
            view.setOnClickListener(null);
        }

        return view;
    }

    public List<Integer> getSeleccionados() {
        return seleccionados;
    }
}
