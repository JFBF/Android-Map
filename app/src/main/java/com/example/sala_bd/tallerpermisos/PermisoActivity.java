package com.example.sala_bd.tallerpermisos;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class PermisoActivity extends AppCompatActivity {


    ListView list;
    Cursor mContactsCursor;
    ContactsCursor mCursorAdapter;
    String mProjection[];
    List<String> arreglo = new ArrayList<>();
    final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permiso);

            list = (ListView) findViewById(R.id.lista);
            mProjection = new String[]{
                    ContactsContract.Profile._ID,
                    ContactsContract.Profile.DISPLAY_NAME_PRIMARY,
            };
            mCursorAdapter = new ContactsCursor(this, null, 0);
            list.setAdapter(mCursorAdapter);



            int permissionCheck = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_CONTACTS);
            if(permissionCheck == 0){
                mContactsCursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                        mProjection, null, null, null);
                mCursorAdapter.changeCursor(mContactsCursor);
            }else
                solicitudPermiso ();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mContactsCursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                            mProjection, null, null, null);
                    mCursorAdapter.changeCursor(mContactsCursor);
                } else {
                    Toast.makeText(getApplicationContext(),"Permiso denegado contactos", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        /*    case MY_PERMISSIONS_REQUEST_CAMARA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //
                } else {
                    Toast.makeText(getApplicationContext(),"Permiso denegado camara", Toast.LENGTH_LONG).show();
                }
                return;
            }*/
        }
    }



    private void solicitudPermiso (){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {
                // ac+a explicación usuario
                Toast.makeText(this, "Se necesita el permiso para poder mostrar los contactos!", Toast.LENGTH_LONG).show();
            }
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);


        }
    }


}
