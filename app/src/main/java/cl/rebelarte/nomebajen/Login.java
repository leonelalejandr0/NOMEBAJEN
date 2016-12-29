package cl.rebelarte.nomebajen;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import cl.rebelarte.nomebajen.clases.Usuario;

public class Login extends AppCompatActivity {

    String id;
    Usuario us;
    CallbackManager callbackManager;
    String idUser;
    ProfileTracker mProfileTracker;
    int PERMISO_GPS;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);

        boolean permisoGPS = false;

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                permisoGPS = true;
            }else{
                pedirPermisoGPS();
            }
        }


        if(AccessToken.getCurrentAccessToken() != null && Profile.getCurrentProfile() != null){
            Profile p = Profile.getCurrentProfile();
            DBHelper h = new DBHelper(getApplicationContext());
            SQLiteDatabase dbw = h.getWritableDatabase();
            SQLiteDatabase dbr = h.getReadableDatabase();

            if(h.getUser(dbr) == null){
                Usuario u = null;

                u = new Usuario();
                u.setId(p.getId());
                u.setNombre(p.getName());
                u.setEmail("");
                u.setImei("null");
                //u.setImei(((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId());
                u.setEstado(1);
                u.setFoto(p.getProfilePictureUri(300,300).toString());
                h.agregarUsuario(u, dbw);
            }


            int response = h.modificarFoto(p.getId(),p.getProfilePictureUri(300,300).toString(),dbw);
            if(response == 1){
                Log.e("Foto","Foto modificada");
            }
            Intent i = new Intent(Login.this,Panel.class);
            i.putExtra("TipoLogin", true);
            startActivity(i);
            finish();
        }

        callbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {

                /*Intent i = new Intent(Login.this, Panel.class);
                i.putExtra("TipoLogin", false);
                startActivity(i);
                finish();*/





                if(Profile.getCurrentProfile() == null) {
                    mProfileTracker = new ProfileTracker() {
                        @Override
                        protected void onCurrentProfileChanged(Profile profile, Profile profile2) {
                            Profile me = profile2;

                            Log.v("facebookprofiletracker", profile2.getId());
                            DBHelper h = new DBHelper(getApplicationContext());
                            SQLiteDatabase dbw = h.getWritableDatabase();

                            Usuario u = null;

                            u = new Usuario();
                            u.setId(me.getId());
                            u.setNombre(me.getName());
                            u.setEmail("");
                            u.setImei(((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId());
                            u.setEstado(1);
                            u.setFoto(me.getProfilePictureUri(300,300).toString());
                            h.agregarUsuario(u, dbw);

                            Intent i = new Intent(Login.this, Panel.class);
                            i.putExtra("TipoLogin", false);
                            startActivity(i);
                            finish();

                            mProfileTracker.stopTracking();
                        }
                    };
                    mProfileTracker.startTracking();
                }
                else {
                    Profile me = Profile.getCurrentProfile();

                    Log.v("facebook - profile", me.getId());
                    DBHelper h = new DBHelper(getApplicationContext());
                    SQLiteDatabase dbw = h.getWritableDatabase();

                    Usuario u = null;

                    u = new Usuario();
                    u.setId(me.getId());
                    u.setNombre(me.getName());
                    u.setEmail("");
                    u.setImei("");
                    //u.setImei(((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId());
                    u.setEstado(1);
                    u.setFoto(me.getProfilePictureUri(300,300).toString());
                    h.agregarUsuario(u, dbw);

                    Intent i = new Intent(Login.this, Panel.class);
                    i.putExtra("TipoLogin", false);
                    startActivity(i);
                    finish();
                }

                /*
                DBHelper h = new DBHelper(getApplicationContext());
                SQLiteDatabase dbw = h.getWritableDatabase();

                Usuario u = null;

                u = new Usuario();
                u.setId(me.getId());
                u.setNombre(me.getName());
                u.setEmail("");
                u.setImei(((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId());
                u.setEstado(1);
                h.agregarUsuario(u, dbw);

                Intent i = new Intent(Login.this, Panel.class);
                i.putExtra("TipoLogin", false);
                startActivity(i);
                finish();
                */


            }

            @Override
            public void onCancel() {
                Toast.makeText(getApplicationContext(), "Cancelaste el inicio de sesión", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Log.e("Errorfb", error.getMessage());
                Toast.makeText(getApplicationContext(), "No tienes internet", Toast.LENGTH_LONG).show();
            }
        });




    }

    public void pedirPermisoGPS()
    {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISO_GPS);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISO_GPS){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            }else{

                Toast.makeText(this,"Necesitamos permiso de GPS para el Mapa",Toast.LENGTH_SHORT).show();
                pedirPermisoGPS();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }






}
