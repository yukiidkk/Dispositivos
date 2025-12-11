package com.example.proyectodieta;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class TipoComidaAdapter extends BaseAdapter {

    private Context context;
    private List<String> tiposComida;
    private int selectedPosition = -1;

    public TipoComidaAdapter(Context context, List<String> tiposComida) {
        this.context = context;
        this.tiposComida = tiposComida;
    }

    @Override
    public int getCount() {
        return tiposComida.size();
    }

    @Override
    public Object getItem(int position) {
        return tiposComida.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_tipo_comida, parent, false);
        }

        TextView txtTipo = view.findViewById(R.id.txtTipoComidaItem);
        txtTipo.setText(tiposComida.get(position));

        // Cambiar el color de fondo si es el elemento seleccionado
        if (position == selectedPosition) {
            view.setBackgroundColor(Color.parseColor("#D1FFD1")); // Verde claro
        } else {
            view.setBackgroundColor(Color.TRANSPARENT);
        }

        return view;
    }

    // Método para actualizar la selección
    public void setSelectedPosition(int position) {
        selectedPosition = position;
        notifyDataSetChanged();
    }

    // Método para obtener el tipo de comida seleccionado
    public String getSelectedItem() {
        if (selectedPosition != -1) {
            return tiposComida.get(selectedPosition);
        }
        return null;
    }
}
