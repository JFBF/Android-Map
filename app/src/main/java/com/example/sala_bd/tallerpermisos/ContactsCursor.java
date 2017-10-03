package com.example.sala_bd.tallerpermisos;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by sala-bd on 29/08/2017.
 */

public class ContactsCursor extends CursorAdapter{
    private static final int CONTACT_ID_INDEX = 0;
    private static final int DISPLAY_NAME_INDEX = 1;
    public ContactsCursor(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context)
                .inflate(R.layout.adapter, parent, false);
    }
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tvIdContacto = (TextView) view.findViewById(R.id.idContacto);
        TextView tvNombre = (TextView) view.findViewById(R.id.nombre);
        int idnum = cursor.getInt(CONTACT_ID_INDEX);
        String nombre = cursor.getString(DISPLAY_NAME_INDEX);
        tvIdContacto.setText(String.valueOf(idnum));
        tvNombre.setText(nombre);
    }
}