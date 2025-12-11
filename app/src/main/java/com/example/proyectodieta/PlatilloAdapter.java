package com.example.proyectodieta;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class PlatilloAdapter extends BaseAdapter {

    private Context context;
    private List<Platillo> lista;

    public PlatilloAdapter(Context context, List<Platillo> lista) {
        this.context = context;
        this.lista = lista;
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
        return lista.get(position).getIdPlatillo();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_platillo, parent, false);
        }

        Platillo p = lista.get(position);

        TextView nombre = view.findViewById(R.id.txtNombrePlatillo);
        TextView tipoComida = view.findViewById(R.id.txtTipoComida);

        nombre.setText(p.getNombre());
        tipoComida.setText(p.getTipoComida());

        // Cambiar color de fondo según el estado del platillo
        if (p.isPreparado()) {
            view.setBackgroundColor(Color.parseColor("#C8E6C9")); // Verde
        } else {
            view.setBackgroundColor(Color.parseColor("#FFF9C4")); // Amarillo
        }

        return view;
    }
}
