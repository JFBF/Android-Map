package com.example.sala_bd.tallerpermisos;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LocationActivity extends AppCompatActivity {

    final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 3;

    private FusedLocationProviderClient mFusedLocationClient;

    private TextView latitud, longitud,altitud,distancia;
    private Button guardar;

    private LocationRequest mLocationRequest; // prender loc si esta apagada
    private LocationCallback mLocationCallback; // objeto que permite suscripción a localización
    final int REQUEST_CHECK_SETTINGS = 4;
    // protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    public double longituPlaza = -74.076033, latitudPlaza = 4.598110;
    public	final	static	double	RADIUS_OF_EARTH_KM	 =	6371;

    private List<String> arreglo = new ArrayList<String>();
    private ArrayAdapter<String> adapter;
    private ListView listView;

    private JSONArray localizaciones = new JSONArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // no gira pantalla
         adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, arreglo);
        listView = (ListView) findViewById(R.id.listHistory);
        listView.setAdapter(adapter);

        mLocationRequest =	createLocationRequest();

        latitud = (TextView) findViewById(R.id.textlatitud);
        longitud = (TextView) findViewById(R.id.textViewlongitud);
        altitud = (TextView) findViewById(R.id.textaltitud);
        distancia = (TextView) findViewById(R.id.textVdistancia);
        guardar = (Button) findViewById(R.id.buttonLog);

        mFusedLocationClient =	LocationServices.getFusedLocationProviderClient(this);

        // acá el callback tiene la localización actualizada
        mLocationCallback =	new	LocationCallback()	 {
            @Override
            public	void	onLocationResult(LocationResult locationResult)	 {
                Location	location	=	locationResult.getLastLocation();
               // Log.i("LOCATION",	"Location	update	in	the	callback:	"	+	location);
                if	(location	 !=	null)	{
                    latitud.setText("Latitud: "+String.valueOf(location.getLatitude()));
                    longitud.setText("Longitud: "+String.valueOf(location.getLongitude()));
                    altitud.setText("Altitud: "+String.valueOf(location.getAltitude()));
                    distancia.setText("Distancia a la Plaza de Bolivar es "+
                            String.valueOf(distance(location.getLatitude(),location.getLongitude(),
                            latitudPlaza,longituPlaza))+" km");

                    Date date = new Date();
                    String fecha =  " Fecha: "+"YYYY/MM/DD hh:mm:ss "+(date.getYear()+1900)+"/"+
                            (date.getMonth()+1)+"/"+(date.getDay()+10)+" "+date.getHours()+
                            ":"+date.getMinutes()+":"+date.getSeconds();
                    String valor = "Lat: "+String.valueOf(location.getLatitude())+
                            " Long: "+String.valueOf(location.getLongitude())+fecha;
                    arreglo.add(valor);
                    listView.setAdapter(adapter);

                    localizaciones.put(toJSON(String.valueOf(location.getLatitude()),String.valueOf(location.getLongitude()),
                            fecha));
                }
            }
        };

        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION);
        System.err.println("valir del int es  "+permissionCheck);
        if(permissionCheck == 0){
            System.err.println("entro aca gonorrea ");
            localizacion();
        }else
            solicitudPermiso ();


        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeJSONObject();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    localizacion();
                } else {
                    Toast.makeText(getApplicationContext(),"Permiso denegado localización", Toast.LENGTH_SHORT).show();
                }
                return;
            }

        }
    }

    private void solicitudPermiso (){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // ac+a explicación usuario
                Toast.makeText(this, "Se necesita el permiso para poder mostrar los contactos!", Toast.LENGTH_LONG).show();
            }
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_ACCESS_FINE_LOCATION);


        }
    }

    private void localizacion(){
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if(permissionCheck == 0){

            // acceder a la localizción simplemente
            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if(location!=null){
                      /*  latitud.setText(String.valueOf(location.getLatitude()));
                        longitud.setText(String.valueOf(location.getLongitude()));
                        altitud.setText(String.valueOf(location.getAltitude()));*/

                    }
                }
            });

            // se pide localización usuario en la configuración
            LocationSettingsRequest.Builder builder	=	new
                    LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
            SettingsClient client	 =	LocationServices.getSettingsClient(LocationActivity.this);
            Task<LocationSettingsResponse> task	=	client.checkLocationSettings(builder.build());
            task.addOnSuccessListener(LocationActivity.this,	 new	OnSuccessListener<LocationSettingsResponse>()
            {
                @Override
                public	void	onSuccess(LocationSettingsResponse locationSettingsResponse)	 {
                    startLocationUpdates();	 //Todas las condiciones para	recibir localizaciones
                }
            });

            // paso extra en caso de estar apagado localización
            task.addOnFailureListener(LocationActivity.this,	 new	OnFailureListener()	 {
                @Override
                public	void	onFailure(@NonNull Exception	 e)	{
                    int statusCode =	((ApiException)	e).getStatusCode();
                    switch	(statusCode)	{
                        case	CommonStatusCodes.RESOLUTION_REQUIRED:
                            //	Location	settings	are	not	satisfied,	but	this	can	be	fixed	by	showing	the	user	a	dialog.
                            try	{//	Show	the	dialog	by	calling	startResolutionForResult(),	and	check	the	result	in	onActivityResult().
                                ResolvableApiException resolvable	 =	(ResolvableApiException)	 e;
                                resolvable.startResolutionForResult(LocationActivity.this,
                                        REQUEST_CHECK_SETTINGS);// lanza dialogo para encender localización
                            }	catch	(IntentSender.SendIntentException sendEx)	{
                                //	Ignore	the	error.
                            }	break;
                        case	LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            //	Location	settings	are	not	satisfied.	No	way	to	fix	the	settings	so	we	won't	show	the	dialog.
                            break;
                    }
                }
            });
        }
    }

    // para pedir localización casa 10 seg
    protected	LocationRequest createLocationRequest()	 {
        LocationRequest mLocationRequest =	new	LocationRequest();
        mLocationRequest.setInterval(10000);	 //tasa de	refresco en	milisegundos
        mLocationRequest.setFastestInterval(5000);	 //máxima tasa de	refresco
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return	mLocationRequest;
    }

    // revisa permisos y pide localización
    private	void	startLocationUpdates()	 {
        if	(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)	 ==
                PackageManager.PERMISSION_GRANTED)	 {//Verificación de	permiso!!
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,mLocationCallback,null);
        }
    }

    // resultado del dialogo sigue siendo parte del paso extra
    @Override
    protected	void	onActivityResult(int requestCode,	 int resultCode,	 Intent data)	 {
        switch	(requestCode)	 {
            case	REQUEST_CHECK_SETTINGS:	 {
                if	(resultCode ==	RESULT_OK)	 {
                    startLocationUpdates();	 	//Se	encendió la	localización!!!
                }	else	{
                    Toast.makeText(this,
                            "Sin	acceso a	localización,	hardware	deshabilitado!",
                            Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    // método calcular distancias

    public	double	distance(double	 lat1,	double	long1,	double	lat2,	double	long2)	{
        double	latDistance =Math.toRadians(lat1-lat2);
        double	lngDistance =Math.toRadians(long1 - long2);
        double	a	=	Math.sin(latDistance/2)*Math.sin(latDistance/2)
                +	Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))
                *	Math.sin(lngDistance/2)*Math.sin(lngDistance/2);
        double	c	=	2*Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
        double	result	=RADIUS_OF_EARTH_KM*c;
        return	Math.round(result*100.0)/100.0;
    }


    // guardar localmente en celular en json
    public	JSONObject toJSON (String lat, String lon, String fecha)	{
        JSONObject obj =	new	JSONObject();
        try	{
            obj.put("latitud",	lat);
            obj.put("longitud",	lon);
            obj.put("date",	fecha);
        }	catch	(JSONException e)	{
            e.printStackTrace();
        }
        return	obj;
    }
    private	void	writeJSONObject(){
        Writer output	=	null;
        String	filename=	"locations.json";
        try	{
            File file	=	new	File(getBaseContext().getExternalFilesDir(null), filename);
            Log.i("LOCATION",	"Ubicacion de	archivo:	"+file);
            output	=	new BufferedWriter(new FileWriter(file));
            output.write(localizaciones.toString());
            output.close();
            Toast.makeText(getApplicationContext(),	 "Location	saved",
                    Toast.LENGTH_LONG).show();
        }	catch	(Exception	e)	 {
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
