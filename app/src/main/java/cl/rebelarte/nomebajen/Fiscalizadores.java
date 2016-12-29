package cl.rebelarte.nomebajen;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Profile;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import cl.rebelarte.nomebajen.clases.Fiscalizador;
import cl.rebelarte.nomebajen.clases.Usuario;


public class Fiscalizadores extends Fragment {

    private LocationManager locManager;
    private LocationListener locListener;
    private Location loc;
    Fiscalizador fSubir;
    GoogleMap mapa;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fiscalizadores, container, false);
        locManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if (locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            eventos(view);
        } else {
            AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
            ad.setTitle("GPS Desactivado");
            ad.setMessage("Tienes el GPS desactivado, quieres activarlo?")
                    .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            ad.show();
        }

        return view;
    }

    public void eventos(View view) {


        mapa = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMap();

        //mostrarFiscalizadores(mapa);
        if(verificaInternet(getActivity()))
        new Sincronizar().execute();

        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if(mapa!= null) {
            mapa.getUiSettings().setMyLocationButtonEnabled(false);

            mapa.setMyLocationEnabled(true);
        }

        Location yo = mapa.getMyLocation();
        //Obtenemos una referencia al LocationManager
        Criteria criteria = new Criteria();
        String best = locManager.getBestProvider(criteria, true);
        loc = locManager.getLastKnownLocation(best);
        if (loc == null) {
            loc = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            /*if (loc != null && yo != null) {
                mapa.addMarker(new MarkerOptions()
                        .position(new LatLng(yo.getLatitude(), yo.getLongitude()))
                        .title("YO")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.me)));
            }*/
            if(loc == null)
                loc = yo;

        }


        locListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                loc = location;
                Log.e("Location", location.toString());



                /*
                mapa.addMarker(new MarkerOptions()
                        .position(new LatLng(loc.getLatitude(), loc.getLongitude()))
                        .title("YO")

                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.me)));
                        */


            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locListener);
                locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, locListener);


            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.e("GPS", "GPS DESACTIVADO");
                Toast.makeText(getActivity(),"GPS Desactivado, tienes que activarlo",Toast.LENGTH_LONG).show();
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                locManager.removeUpdates(locListener);

            }
        };

        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locListener);
        locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, locListener);

        /*CameraUpdate camUpd1;
        camUpd1 = CameraUpdateFactory.newLatLngZoom(new LatLng(loc.getLatitude(), loc.getLongitude()), 16f);
        mapa.moveCamera(camUpd1);

        //mapa.moveCamera(camUpd1);
        //mapa.setMyLocationEnabled(true);
        */

        if(loc != null){
            CameraUpdate camUpd1;
            camUpd1 = CameraUpdateFactory.newLatLngZoom(new LatLng(loc.getLatitude(), loc.getLongitude()), 16f);
            mapa.moveCamera(camUpd1);
        }



        mapa.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng latLng) {
                AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
                ad.setTitle("Agregar Fiscalizador");
                ad.setMessage("Está seguro de agregar un fiscalizador en este punto?")
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                AlertDialog.Builder ad2 = new AlertDialog.Builder(getActivity());
                                ad2.setTitle("Información de la Fiscalización");
                                LinearLayout ll = new LinearLayout(getActivity());
                                ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                ll.setOrientation(LinearLayout.VERTICAL);
                                TextView titulo1 = new TextView(getActivity());
                                titulo1.setText("DESCRIPCIÓN");
                                titulo1.setTextColor(Color.BLACK);
                                titulo1.setPadding(20, 20, 0, 0);
                                //titulo1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                final EditText et = new EditText(getActivity());
                                et.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                //et.setPadding(10,10,10,10);

                                LinearLayout.LayoutParams t = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                t.setMargins(0,10,0,20);


                                ll.addView(titulo1, t);
                                t.setMargins(10,10,10,10);
                                ll.addView(et,t);
                                ad2.setView(ll);
                                ad2.setPositiveButton("Agregar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        Calendar c = Calendar.getInstance();

                                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                                        String fecha = df.format(c.getTime());
                                        Log.e("Fecha",fecha);

                                        // HH:mm
                                        SimpleDateFormat dh = new SimpleDateFormat("HH:mm");
                                        String hora = dh.format(c.getTime());
                                        Log.e("Hora", hora);

                                        Profile profile = Profile.getCurrentProfile();
                                        String user = "";
                                        if(profile != null){
                                            user = profile.getName();
                                        }

                                        DBHelper h = new DBHelper(getActivity());
                                        SQLiteDatabase dbr = h.getReadableDatabase();

                                        Fiscalizador f = new Fiscalizador();
                                        long idF = h.getCorrelativo(dbr);
                                        f.setId(idF);
                                        f.setLatitud(latLng.latitude);
                                        f.setUsuario(profile.getId());
                                        f.setNombreUsuario(user);
                                        f.setLongitud(latLng.longitude);
                                        f.setFecha(fecha);
                                        f.setHora(hora);
                                        f.setDescripcion(et.getText().toString());
                                        f.setSubido(0);



                                        SQLiteDatabase db = h.getWritableDatabase();
                                        int response = h.agregarFiscalizador(f, db);
                                        if (response == 1) {

                                            mapa.addMarker(new MarkerOptions()
                                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.fiscalizador))
                                                    .position(new LatLng(latLng.latitude, latLng.longitude))
                                                    .title(idF + " | Fiscalizador | " + f.getHora())
                                                    .snippet(f.getDescripcion()));

                                            if(h.actualizarCorrelativo(db)== 1){
                                                Log.e("Correlativo","Actualizado");
                                            }

                                        }
                                    }
                                });
                                ad2.show();

                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                ad.show();
            }
        });


        mapa.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                String tituloMarker = marker.getTitle();
                String[] idS = tituloMarker.split(" | ");
                Intent i = new Intent(getActivity(),Detalle.class);
                i.putExtra("ID",idS[0]);
                startActivity(i);
                //trabajar en esto getActivity().finish();
            }
        });


        FloatingActionButton b = (FloatingActionButton) view.findViewById(R.id.btnFiscalizador);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Location myLocation = loc;
                AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
                ad.setTitle("Agregar Fiscalizador");
                ad.setMessage("Está seguro de agregar un fiscalizador en este punto?")
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                AlertDialog.Builder ad2 = new AlertDialog.Builder(getActivity());
                                ad2.setTitle("Información de la Fiscalización");
                                LinearLayout ll = new LinearLayout(getActivity());
                                ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                ll.setOrientation(LinearLayout.VERTICAL);
                                TextView titulo1 = new TextView(getActivity());
                                titulo1.setText("DESCRIPCIÓN");
                                titulo1.setTextColor(Color.BLACK);
                                titulo1.setPadding(20, 20, 0, 0);
                                //titulo1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                final EditText et = new EditText(getActivity());
                                et.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                //et.setPadding(10,10,10,10);

                                LinearLayout.LayoutParams t = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                t.setMargins(0,10,0,20);


                                ll.addView(titulo1, t);
                                t.setMargins(10,10,10,10);
                                ll.addView(et,t);
                                ad2.setView(ll);
                                ad2.setPositiveButton("Agregar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        Calendar c = Calendar.getInstance();

                                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                                        String fecha = df.format(c.getTime());
                                        Log.e("Fecha",fecha);

                                        // HH:mm
                                        SimpleDateFormat dh = new SimpleDateFormat("HH:mm");
                                        String hora = dh.format(c.getTime());
                                        Log.e("Hora", hora);

                                        Profile profile = Profile.getCurrentProfile();
                                        String user = "";
                                        if(profile != null){
                                            user = profile.getName();
                                        }

                                        DBHelper h = new DBHelper(getActivity());
                                        SQLiteDatabase dbr = h.getReadableDatabase();
                                        long idF = h.getCorrelativo(dbr);
                                        Fiscalizador f = new Fiscalizador();
                                        f.setId(idF);
                                        f.setLatitud(myLocation.getLatitude());
                                        f.setUsuario(profile.getId());
                                        f.setNombreUsuario(user);
                                        f.setLongitud(myLocation.getLongitude());
                                        f.setFecha(fecha);
                                        f.setHora(hora);
                                        f.setDescripcion(et.getText().toString());
                                        f.setSubido(0);



                                        SQLiteDatabase db = h.getWritableDatabase();
                                        int response = h.agregarFiscalizador(f, db);
                                        if (response == 1) {
                                            fSubir = f;
                                            mapa.addMarker(new MarkerOptions()
                                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.fiscalizador))
                                                    .position(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()))
                                                    .title(idF + " | Fiscalizador | " + f.getHora())
                                            .snippet(f.getDescripcion()));

                                            if(h.actualizarCorrelativo(db)== 1){
                                                Log.e("Correlativo","Actualizado");
                                            }
                                        }
                                    }
                                });
                                ad2.show();

                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                ad.show();

            }
        });

        FloatingActionButton sync = (FloatingActionButton)view.findViewById(R.id.btnSync);
        sync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(verificaInternet(getActivity())){
                    new GetCorrelativo().execute();
                    new Sincronizar().execute();
                }else{
                    Toast.makeText(getActivity(),"No tienes internet, no puedes actualizar",Toast.LENGTH_LONG).show();
                }
            }
        });

    }


    public void mostrarFiscalizadores(GoogleMap mapa) {
        mapa.clear();
        DBHelper helper = new DBHelper(getActivity());
        SQLiteDatabase dbr = helper.getReadableDatabase();
        ArrayList<Fiscalizador> fs = helper.verFiscalizadores(dbr);
        if (fs != null && fs.size() > 0) {
            for (Fiscalizador f : fs) {
                mapa.addMarker(new MarkerOptions()
                        .position(new LatLng(f.getLatitud(), f.getLongitud()))
                        .title(f.getId() + " | Fiscalizador | " + f.getHora())

                        .snippet(f.getDescripcion()))
                        .setIcon(BitmapDescriptorFactory.fromResource(R.drawable.fiscalizador));
            }
        }
    }


    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locManager.removeUpdates(locListener);
    }

    public class GetCorrelativo extends AsyncTask<Void,Void,Void>{

        String jsonstr;



        @Override
        protected Void doInBackground(Void... params) {
            HttpResponse response;
            HttpClient myClient = new DefaultHttpClient();
            DBHelper h = new DBHelper(getActivity());
            SQLiteDatabase db = h.getReadableDatabase();
            Usuario u = h.getUser(db);
            //http://www.boxapp.cl/nmb/addUser.php?id=&nombre=&email=&imei=&estado=
            HttpGet myConnection = new HttpGet("http://www.leonelalejandro.cl/nmb/getCorrelativo.php");

            try {
                response = myClient.execute(myConnection);
                jsonstr = EntityUtils.toString(response.getEntity(), "UTF-8");


            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;


        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                JSONObject jsonObject = new JSONObject(jsonstr);
                long correlativo = jsonObject.getLong("correlativo");
                DBHelper h = new DBHelper(getActivity());
                SQLiteDatabase dbw = h.getWritableDatabase();
                if(h.borrarCorrelativo(dbw) == 1){
                    if(h.insertarCorrelativo(dbw,correlativo) == 1){
                        Log.e("Correlativo","Correlativo Actualizado");
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public class SubirFiscalizadores extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... params) {

            HttpResponse response;
            HttpClient myClient = new DefaultHttpClient();
            String nombre = fSubir.getNombreUsuario().replace(" ","%20");
            String url = "http://www.leonelalejandro.cl/nmb/add.php?usuario="+nombre+"&latitud="+fSubir.getLatitud()+"&longitud="+fSubir.getLongitud()+"&fecha="+fSubir.getFecha()+"&hora="+fSubir.getHora()+"&descripcion="+fSubir.getDescripcion();
            Uri u = Uri.parse(url);
            HttpGet myConnection = null;
            myConnection = new HttpGet(url);


            try {
                response = myClient.execute(myConnection);


            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class Sincronizar extends AsyncTask<Void,Void,Void>{

        ProgressDialog pd ;
        String jsonstr;

        @Override
        protected void onPreExecute() {
            pd = ProgressDialog.show(getActivity(),"Sincronizando","Sincronizando la app, espera por favor");

        }

        @Override
        protected Void doInBackground(Void... params) {

            DBHelper helper = new DBHelper(getActivity());
            SQLiteDatabase dbr = helper.getReadableDatabase();
            SQLiteDatabase dbw = helper.getWritableDatabase();
            ArrayList<Fiscalizador> fs = helper.SubirFiscalizadores(dbr);
            if(fs != null && fs.size() > 0){
                for (Fiscalizador f : fs){
                    Log.e("Fiscalizador", f.getNombreUsuario());
                    HttpResponse response;
                    HttpClient myClient = new DefaultHttpClient();
                    String nombre = f.getNombreUsuario().replace(" ","%20");
                    String desc = f.getDescripcion().replace(" ","%20");
                    String url = "http://www.leonelalejandro.cl/nmb/add.php?usuario="+f.getUsuario()+"&nombreUsuario="+nombre+"&latitud="+f.getLatitud()+"&longitud="+f.getLongitud()+"&fecha="+f.getFecha()+"&hora="+f.getHora()+"&descripcion="+desc;
                    HttpGet myConnection = null;
                    myConnection = new HttpGet(url);


                    try {
                        response = myClient.execute(myConnection);

                        int r = helper.actualizarFiscalizacionSubida(dbw,f.getId());
                        if(r == 1){
                            Log.e("Subir","Actualizado");
                        }
                    } catch (ClientProtocolException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }


            HttpResponse response;
            HttpClient myClient = new DefaultHttpClient();
            HttpGet myConnection = new HttpGet("http://www.leonelalejandro.cl/nmb/get.php");

            try {
                response = myClient.execute(myConnection);
                jsonstr = EntityUtils.toString(response.getEntity(), "UTF-8");


            } catch (ClientProtocolException e) {
                e.printStackTrace();
                Log.e("ERROR",e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("ERROR", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            DBHelper h = new DBHelper(getActivity());
            SQLiteDatabase db = h.getWritableDatabase();
            int rb = h.BorrarFiscalizaciones(db);
            if(rb == 1){
                Log.e("Fiscalizaciones","Borradas");
            }
            try {
                JSONArray jsonArray = new JSONArray(jsonstr);
                for(int x = 0; x < jsonArray.length(); x++){
                    JSONObject obj = jsonArray.getJSONObject(x);
                    Fiscalizador f = new Fiscalizador();
                    f.setId(obj.getLong("id"));
                    f.setUsuario(obj.getString("usuario"));
                    f.setNombreUsuario(obj.getString("nombreUsuario"));
                    f.setLatitud(obj.getDouble("latitud"));
                    f.setLongitud(obj.getDouble("longitud"));
                    f.setFecha(obj.getString("fecha"));
                    f.setHora(obj.getString("hora"));
                    f.setDescripcion(obj.getString("descripcion"));
                    f.setSubido(1);
                    int r = h.agregarFiscalizador(f,db);
                    if(r == 1){
                        Log.e("Fiscalizador","Agregado a BD");
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("JSON", e.getMessage());
            }
            mostrarFiscalizadores(mapa);
            pd.dismiss();
        }
    }

    public static boolean verificaInternet(Context ctx) {
        boolean bConectado = false;
        ConnectivityManager connec = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        // No sólo wifi, también GPRS
        NetworkInfo[] redes = connec.getAllNetworkInfo();
        // este bucle debería no ser tan ñapa
        for (int i = 0; i < 2; i++) {
            // ¿Tenemos conexión? ponemos a true
            if (redes[i].getState() == NetworkInfo.State.CONNECTED) {
                bConectado = true;
            }
        }
        return bConectado;
    }
}
