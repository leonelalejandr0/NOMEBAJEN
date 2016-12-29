package cl.rebelarte.nomebajen;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.jar.Manifest;

import cl.rebelarte.nomebajen.clases.Usuario;

public class MainActivity extends AppCompatActivity {
    int GPS_PERMISO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ProgressBar spinner;
        spinner = (ProgressBar)findViewById(R.id.progressBar1);
        spinner.setVisibility(View.GONE);
        spinner.setVisibility(View.VISIBLE);



            Thread timer = new Thread() {
                public void run(){
                    try{
                        sleep(3000);
                    }catch(InterruptedException e){
                        e.printStackTrace();
                    }catch(Exception e) {
                        e.printStackTrace();
                    }
                    finally
                    {

                        DBHelper h = new DBHelper(getApplicationContext());
                        SQLiteDatabase db = h.getReadableDatabase();
                        Usuario u = h.getUser(db);
                        if(u == null){
                            Intent i = new Intent(MainActivity.this,Tutorial.class);
                            i.putExtra("Actividad",true);
                            startActivity(i);
                        }else{
                            Intent i = new Intent(MainActivity.this,Login.class);
                            startActivity(i);
                        }



                    }
                }

            };
            timer.start();



    }



    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
