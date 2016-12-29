package cl.rebelarte.nomebajen;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import cl.rebelarte.nomebajen.clases.Usuario;

public class Panel extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    MenuItem sync;
    Drawable d = null;
    ImageView imgPerfil;
    Usuario us;
    InterstitialAd mInterstitialAd;

    private InterstitialAd newInterstitialAd() {
        InterstitialAd interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.interstitial_ad));
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                showInterstitial();
            }
        });
        return interstitialAd;
    }

    private void showInterstitial() {
        // Mostramos el anuncio si está listo. En caso contrario, mostramos un mensaje y volvemos a recargarlo
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Toast.makeText(this, "El anuncio no cargó", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()

                .build();
        mInterstitialAd.loadAd(adRequest);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panel);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//////////////

        mInterstitialAd = newInterstitialAd();
        loadInterstitial();










        DBHelper h = new DBHelper(getApplicationContext());
        SQLiteDatabase db = h.getReadableDatabase();
        us = h.getUser(db);

        if(verificaInternet(getApplicationContext())){
            Boolean tipologin = getIntent().getExtras().getBoolean("TipoLogin");
            if(tipologin){
                new ActualizarUsuario().execute();
            }else{



                new SubirUsuario().execute();
            }
            new GetCorrelativo().execute();
            new getProfilePhoto().execute();
        }

        Fragment f;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        f = new Fiscalizadores();
        transaction.replace(R.id.layout_content_panel,f);
        transaction.addToBackStack(null);
        transaction.commit();


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);



        navigationView.post(new Runnable() {
            @Override
            public void run() {
                String name = getIntent().getStringExtra("Nombre");
                TextView nombre = (TextView) findViewById(R.id.txtNombreUser);
                nombre.setText(name);

                imgPerfil = (ImageView)findViewById(R.id.imageView);
            }
        });


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {

            AlertDialog.Builder ad = new AlertDialog.Builder(this);
            ad.setTitle("Salir");
            ad.setMessage("Está seguro que quieres salir de la aplicación?")
                    .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            ad.show();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.panel, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        sync = item;
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_help) {
            Intent i = new Intent(Panel.this,Tutorial.class);
            i.putExtra("Actividad",false);
            startActivity(i);
        }
        if(id == R.id.action_about){
            Intent i = new Intent(Panel.this,Acerca.class);
            startActivity(i);
        }


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        Fragment newFragment;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if(id == R.id.nav_fiscalizadores){


            newFragment = new Fiscalizadores();
            transaction.replace(R.id.layout_content_panel, newFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
        if(id == R.id.nav_buses){


            newFragment = new FrecuenciaBuses();
            transaction.replace(R.id.layout_content_panel, newFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }

        if (id == R.id.nav_saldo) {


            // Handle the camera action
            newFragment = new SaldoBip();
            transaction.replace(R.id.layout_content_panel, newFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }

        if(id == R.id.nav_exit){
            /*LoginManager.getInstance().logOut();
            finish();*/
            AlertDialog.Builder ad = new AlertDialog.Builder(Panel.this);
            ad.setTitle("Salir");
            ad.setMessage("Está seguro que quieres salir de la aplicación?")
                    .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            LoginManager.getInstance().logOut();
                            finish();
                        }
                    })
                    .setNeutralButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            ad.show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public class getProfilePhoto extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... params) {
            Uri url = Profile.getCurrentProfile().getProfilePictureUri(300,300);
            try {
                d = drawableFromUrl(url.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(d != null){
                imgPerfil.setImageDrawable(d);
            }
        }

        public Drawable drawableFromUrl(String url) throws IOException {
            Bitmap x;

            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.connect();
            InputStream input = connection.getInputStream();

            x = BitmapFactory.decodeStream(input);
            return new BitmapDrawable(x);
        }
    }


    public class ActualizarUsuario extends AsyncTask<Void,Void,Void>{
        String jsonstr;

        @Override
        protected Void doInBackground(Void... params) {
            HttpResponse response;
            HttpClient myClient = new DefaultHttpClient();

            //http://www.boxapp.cl/nmb/getUser.php?id=
            HttpGet myConnection = new HttpGet("http://www.leonelalejandro.cl/nmb/getUser.php?id="+ us.getId()+"&foto="+us.getFoto());

            try {
                response = myClient.execute(myConnection);
                jsonstr = EntityUtils.toString(response.getEntity(), "UTF-8");


            } catch (ClientProtocolException e) {
                e.printStackTrace();
                Log.e("ASD",e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("ASD", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            DBHelper h = new DBHelper(getApplicationContext());
            SQLiteDatabase dbw = h.getWritableDatabase();
            try {
                JSONObject jsonObject = new JSONObject(jsonstr);
                int estado = jsonObject.getInt("estado");

                int r = h.actualizarEstadoUsuario(estado,dbw);
                if(r == 1){
                    Log.e("Actualizar Usuario", "Actualizado");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            SQLiteDatabase dbr = h.getReadableDatabase();
            Usuario u = h.getUser(dbr);

            if(u.getEstado() == 1) {

            }else{
                //Toast.makeText(getApplicationContext(),"Tu usuario está bloqueado, contáctese con el administrador",Toast.LENGTH_LONG).show();

                AlertDialog.Builder ad = new AlertDialog.Builder(Panel.this);

                ad.setTitle("Bloqueado");
                ad.setMessage("Tu usuario se encuentra bloqueado por mal uso de la app, contáctate con el administrador");
                ad.setCancelable(false);
                ad.setNeutralButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                ad.show();


            }

        }
    }

    public class SubirUsuario extends AsyncTask<Void,Void,Void>{

        String jsonstr;

        @Override
        protected Void doInBackground(Void... params) {
            HttpResponse response;
            HttpClient myClient = new DefaultHttpClient();
            DBHelper h = new DBHelper(getApplicationContext());
            SQLiteDatabase db = h.getReadableDatabase();
            Usuario u = h.getUser(db);
            //http://www.boxapp.cl/nmb/addUser.php?id=&nombre=&email=&imei=&estado=
            HttpGet myConnection = new HttpGet("http://www.leonelalejandro.cl/nmb/addUser.php?id="+u.getId()+"&nombre="+u.getNombre().replace(" ","%20")+"&email="+u.getEmail()+"&imei="+u.getImei()+"&estado=" + u.getEstado()+"&foto="+u.getFoto());

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
                String res = jsonObject.getString("mensaje");
                if(res.equals("ok")){
                    Log.e("Agregar Usuario","Agregado");
                }else{
                    Log.e("Agregar Usuario","Ya existe usuario");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            DBHelper h = new DBHelper(getApplicationContext());
            SQLiteDatabase db = h.getReadableDatabase();
            Usuario u = h.getUser(db);
            new ActualizarUsuario().execute();
        }
    }

    public class GetCorrelativo extends AsyncTask<Void,Void,Void>{

        String jsonstr;



        @Override
        protected Void doInBackground(Void... params) {
            HttpResponse response;
            HttpClient myClient = new DefaultHttpClient();
            DBHelper h = new DBHelper(getApplicationContext());
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
                DBHelper h = new DBHelper(getApplicationContext());
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
