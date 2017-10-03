package com.example.sala_bd.tallerpermisos;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap = null;
    private EditText dirección;
    private TextView distanci;

    private LocationRequest mLocationRequest; // prender loc si esta apagada
    private LocationCallback mLocationCallback; // objeto que permite suscripción a localización
    final int REQUEST_CHECK_SETTINGS = 4;
    final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 3;
    public	final	static	double	RADIUS_OF_EARTH_KM	 =	6371;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location location = null;
    private LatLng actual,desti = null;
    private Marker bikeActual , destinoAzul , tienda1
            ,tienda2,tienda3,tienda4,tienda5;
    public double longitudCityBike =  -74.052350, latitudCityBike = 4.732924;
    public double longituBabilonia =  -74.033026, latitudBabilonia = 4.743359;
    public double longituBeneton = -74.052110, latitudBeneton = 4.731317;
    public double longituCastillo =  -74.030582, latitudCastillo = 4.698325;
    public double longituBikers =  -74.036585, latitudBikers = 4.719675;
    private View popup = null;
    private boolean first = true, permiso = false;
    private ImageView move = null;

    public static final double lowerLeftLatitude = 4.469636;
    public static final double lowerLeftLongitude = -74.177171;
    public static final double upperRightLatitude = 4.817991;
    public static final double upperRigthLongitude = -74.001390;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mLocationRequest =	createLocationRequest();
        mFusedLocationClient =	LocationServices.getFusedLocationProviderClient(this);
        distanci = (TextView) findViewById(R.id.textViewDistancia);
        move = (ImageView) findViewById(R.id.imageViewMove);
        move.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION);
                if(permissionCheck==0){
                    if(location!=null && mMap!=null) {
                        actual = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(actual));
                        mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
                    }
                }
                else
                    solicitudPermiso ();
            }
        });
        // acá el callback tiene la localización actualizada
        mLocationCallback =	new	LocationCallback()	 {
            @Override
            public	void	onLocationResult(LocationResult locationResult)	 {
                location	=	locationResult.getLastLocation();
                // Log.i("LOCATION",	"Location	update	in	the	callback:	"	+	location);
                localizarActual();
                calculoDistancia();
            }
        };

        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION);
        if(permissionCheck == 0){
            localizacion();
        }else
            solicitudPermiso ();

        dirección = (EditText) findViewById(R.id.texto);
        dirección.setImeActionLabel("Custom text", KeyEvent.KEYCODE_ENTER);
        dirección.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    buscarDireccion();
                    InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(dirección.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Date date = new Date();
        // Add a marker in Sydney and move the camera
        mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        actual = new LatLng(4.628479,-74.064908);
        if(date.getHours()>=6 && date.getHours()<18){
            if(move != null)
                move.setImageResource(R.drawable.movelocation);
            mMap.setMapStyle(MapStyleOptions
                    .loadRawResourceStyle(this, R.raw.style_json));
            bikeActual = mMap.addMarker(new MarkerOptions()
                    .position(actual)
                    .title("Posición actual")
                    .icon(BitmapDescriptorFactory
                            .fromResource(R.drawable.bici)));
            bikeActual.setVisible(false);

        }else {
            if(move != null)
                move.setImageResource(R.drawable.movelocationnight);
            mMap.setMapStyle(MapStyleOptions
                    .loadRawResourceStyle(this, R.raw.style_night_json));
            bikeActual = mMap.addMarker(new MarkerOptions()
                    .position(actual)
                    .title("Posición actual")
                    .icon(BitmapDescriptorFactory
                            .fromResource(R.drawable.bicinight)));
            bikeActual.setVisible(false);
        }

        destinoAzul = mMap.addMarker(new MarkerOptions()
                .position(actual)
                .icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        destinoAzul.setVisible(false);


        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                if(popup == null){
                    popup = getLayoutInflater().inflate(R.layout.popupmaps,null);
                }

                TextView tv = (TextView) popup.findViewById(R.id.title);
                tv.setText(marker.getTitle());
                tv = (TextView) popup.findViewById(R.id.snippet);
                tv.setText(marker.getSnippet());
                return popup;
            }
        });
        LatLng t1 = new LatLng(latitudCityBike, longitudCityBike);
        tienda1 = mMap.addMarker(new MarkerOptions()
                .position(t1)
                .title("City Bike ")
                .snippet("Lunes - Sábado: 10:00-19:00")
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.citybike)));

        LatLng t2 = new LatLng(latitudBeneton, longituBeneton);
        tienda2 = mMap.addMarker(new MarkerOptions()
                .position(t2)
                .title("Bicicletas Beneton")
                .snippet("Lunes - Sábado: 8:00-19:00 \n" +
                        "Domingo: 8:00-13:00")
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.beneton)));

        LatLng t3 = new LatLng(latitudBikers, longituBikers);
        tienda3 = mMap.addMarker(new MarkerOptions()
                .position(t3)
                .title("Speed Bikers")
                .snippet("Lunes - Viernes: 10:30-19:00 \n" +
                        "Sábado: 11:00-18:00 \n" +
                        "Domingo: cerrado")
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.bikers)));

        LatLng t4 = new LatLng(latitudBabilonia, longituBabilonia);
        tienda4 = mMap.addMarker(new MarkerOptions()
                .position(t4)
                .title("Bicicleteria Babilonia")
                .snippet("Lunes - Domingo: 8:00-20:00")
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.babilonia)));

        LatLng t5 = new LatLng(latitudCastillo, longituCastillo);
        tienda5 = mMap.addMarker(new MarkerOptions()
                .position(t5)
                .title("Bicicletas Castillo")
                .snippet("Lunes - Sábado: 9:00-19:00 \n" +
                        "Domingo: 9:30-14:30")
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.castillo)));

    }


    private void localizarActual(){
        if(mMap != null && location!=null){
            actual = new LatLng(location.getLatitude(), location.getLongitude());
            bikeActual.setPosition(actual);
            bikeActual.setVisible(true);
            if(first){
                mMap.moveCamera(CameraUpdateFactory.newLatLng(actual));
                first = false;
            }
        }
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
            // se pide localización usuario en la configuración
            LocationSettingsRequest.Builder builder	=	new
                    LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
            SettingsClient client	 =	LocationServices.getSettingsClient(MapsActivity.this);
            Task<LocationSettingsResponse> task	=	client.checkLocationSettings(builder.build());
            task.addOnSuccessListener(MapsActivity.this,	 new	OnSuccessListener<LocationSettingsResponse>()
            {
                @Override
                public	void	onSuccess(LocationSettingsResponse locationSettingsResponse)	 {
                    startLocationUpdates();	 //Todas las condiciones para	recibir localizaciones
                }
            });

            // paso extra en caso de estar apagado localización
            task.addOnFailureListener(MapsActivity.this,	 new	OnFailureListener()	 {
                @Override
                public	void	onFailure(@NonNull Exception	 e)	{
                    int statusCode =	((ApiException)	e).getStatusCode();
                    switch	(statusCode)	{
                        case	CommonStatusCodes.RESOLUTION_REQUIRED:
                            //	Location	settings	are	not	satisfied,	but	this	can	be	fixed	by	showing	the	user	a	dialog.
                            try	{//	Show	the	dialog	by	calling	startResolutionForResult(),	and	check	the	result	in	onActivityResult().
                                ResolvableApiException resolvable	 =	(ResolvableApiException)	 e;
                                resolvable.startResolutionForResult(MapsActivity.this,
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

    private void buscarDireccion(){
        Geocoder mGeocoder = new Geocoder(getBaseContext());
        String addressString = dirección.getText().toString();
        if (!addressString.isEmpty()) {
            try {
                List<Address> addresses = mGeocoder.getFromLocationName(
                        addressString, 2,
                        lowerLeftLatitude,
                        lowerLeftLongitude,
                        upperRightLatitude,
                        upperRigthLongitude);
                if (addresses != null && !addresses.isEmpty()) {
                    Address addressResult = addresses.get(0);
                   desti = new LatLng(addressResult.getLatitude(), addressResult.getLongitude());
                    if (mMap != null) {
                        System.out.println("destino es "+desti.latitude+ " - "+ desti.longitude);
                        destinoAzul.setPosition(desti);
                        destinoAzul.setVisible(true);
                        destinoAzul.setSnippet(addressResult.getFeatureName());
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(desti));
                        mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
                        calculoDistancia();
                    }
                } else {
                    Toast.makeText(MapsActivity.this, "Dirección no encontrada", Toast.LENGTH_SHORT).show();
                    destinoAzul.setVisible(false);
                    desti = null;
                    calculoDistancia();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(MapsActivity.this, "La dirección esta vacía", Toast.LENGTH_SHORT).show();
            destinoAzul.setVisible(false);
            desti = null;
            calculoDistancia();
        }
    }

    private void calculoDistancia(){
        if	(location!=	null && desti!=null){
            distanci.setText("Distancia es: "+
                    String.valueOf(distance(location.getLatitude(),location.getLongitude(),
                            desti.latitude,desti.longitude))+" km");
        }
        else
            distanci.setText("");
    }
}
