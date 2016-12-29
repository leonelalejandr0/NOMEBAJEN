package cl.rebelarte.nomebajen;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Profile;

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

import cl.rebelarte.nomebajen.clases.Fiscalizador;
import cl.rebelarte.nomebajen.clases.Usuario;

public class Detalle extends AppCompatActivity {
    ImageView fotoPerfil;
    String urlId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String  ids = getIntent().getExtras().getString("ID");

        DBHelper h = new DBHelper(getApplicationContext());
        SQLiteDatabase db = h.getReadableDatabase();

        Fiscalizador f = h.getFiscalizador(db,Long.parseLong(ids));

        if(f != null){
            ((TextView)findViewById(R.id.nombrePerfil)).setText(f.getNombreUsuario());
            ((TextView)findViewById(R.id.fecha)).setText(f.getFecha() + " " + f.getHora());
            ((TextView)findViewById(R.id.latitud)).setText(f.getLatitud() + "");
            ((TextView)findViewById(R.id.longitud)).setText(f.getLongitud() + "");
            ((TextView)findViewById(R.id.descripcion)).setText(f.getDescripcion());
            fotoPerfil = (ImageView)findViewById(R.id.fotoPerfil);
            urlId = f.getUsuario();
            if(verificaInternet(getApplicationContext()))
                new getProfilePhoto().execute();



        }

        //Toast.makeText(getApplicationContext(),ids,Toast.LENGTH_LONG).show();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabEliminar);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Estamos trabajando en esta función", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        FloatingActionButton volver = (FloatingActionButton)findViewById(R.id.fabVolver);
        volver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    public class getProfilePhoto extends AsyncTask<Void,Void,Void> {
        Drawable d = null;
        @Override
        protected Void doInBackground(Void... params) {
            //Uri url = Profile.getCurrentProfile().getProfilePictureUri(300,300);
            String jsonstr = "";
            HttpResponse response;
            HttpClient myClient = new DefaultHttpClient();

            HttpGet myConnection = new HttpGet("http://www.leonelalejandro.cl/nmb/getFoto.php?id="+urlId);

            try {
                response = myClient.execute(myConnection);
                jsonstr = EntityUtils.toString(response.getEntity(), "UTF-8");


            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String url = "";
            try {
                JSONObject jsonObject = new JSONObject(jsonstr);
                url = jsonObject.getString("foto");

            } catch (JSONException e) {
                e.printStackTrace();
            }

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
                fotoPerfil.setImageDrawable(d);
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
