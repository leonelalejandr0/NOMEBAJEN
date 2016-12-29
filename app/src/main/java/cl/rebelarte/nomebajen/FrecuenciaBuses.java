package cl.rebelarte.nomebajen;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

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


public class FrecuenciaBuses extends Fragment {

    String paradero = "";
    View vista;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_frencuencia_buses, container, false);
        vista = v;
        eventosFragmento(v);
        return v;
    }

    public void eventosFragmento(final View view){
        Button verBuses = (Button)view.findViewById(R.id.verTiempos);
        verBuses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String codigo = ((EditText) view.findViewById(R.id.txtParadero)).getText().toString();
                if (!codigo.equals("")) {
                    paradero = codigo;
                    if (verificaInternet(getActivity())) {
                        new VerBuses().execute();
                    } else {
                        Snackbar.make(v, "No tienes conexión a internet", Snackbar.LENGTH_SHORT).show();
                    }
                } else {
                    Snackbar.make(v, "EL CÓDIGO DEL PARADERO ES OBLIGATORIO", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    public class VerBuses extends AsyncTask<Void,Void,Void> {
        ProgressDialog pd;
        String jsonstr;
        @Override
        protected void onPreExecute() {
            pd = ProgressDialog.show(getActivity(),"Próximos Buses","Consultando sobre los próximos buses! Espera por favor");

        }

        @Override
        protected Void doInBackground(Void... params) {
            String paramsUrl = paradero;

            HttpResponse response;
            HttpClient myClient = new DefaultHttpClient();
            HttpGet myConnection = new HttpGet("http://dev.adderou.cl/transanpbl/busdata.php?paradero="+paramsUrl+"&ordenar=bus");

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
            Log.e("JSON", jsonstr);
            JSONObject jsonObject = null;
            LinearLayout proxBuses = (LinearLayout)vista.findViewById(R.id.linearBuses);
            proxBuses.removeAllViews();
            try {
                jsonObject = new JSONObject(jsonstr);
                ((TextView)vista.findViewById(R.id.lblParadero)).setText(jsonObject.getString("descripcion"));

                String servicio = "";
                JSONArray jsonArray = jsonObject.getJSONArray("servicios");
                for (int x = 0; x < jsonArray.length();x++){
                    JSONObject object = jsonArray.getJSONObject(x);
                    if(!servicio.equals(object.getString("servicio"))){
                        TextView tv = new TextView(getActivity());
                        tv.setText(object.getString("servicio"));
                        tv.setBackgroundColor(Color.BLACK);
                        tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        tv.setTextColor(Color.WHITE);
                        tv.setPadding(20,10,0,10);
                        proxBuses.addView(tv);
                        servicio = object.getString("servicio");
                    }
                    if(object.getInt("valido") == 1){
                        LinearLayout add = new LinearLayout(getActivity());
                        add.setPadding(30, 10, 0, 10);
                        add.setBackgroundColor(Color.WHITE);
                        add.setOrientation(LinearLayout.HORIZONTAL);
                        add.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                        TextView patente = new TextView(getActivity());
                        patente.setText(object.getString("patente"));
                        patente.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                        patente.setTextColor(Color.BLACK);
                        patente.setPadding(0, 0, 30, 0);

                        TextView tiempo = new TextView(getActivity());
                        tiempo.setText(object.getString("tiempo"));
                        tiempo.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT,1f));
                        tiempo.setTextColor(Color.BLACK);
                        tiempo.setPadding(0, 0, 30, 0);

                        TextView distancia = new TextView(getActivity());
                        distancia.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT,1f));
                        distancia.setText(object.getString("distancia"));
                        distancia.setTextColor(Color.BLACK);
                        distancia.setPadding(0, 0, 30, 0);

                        add.addView(patente);
                        add.addView(tiempo);
                        add.addView(distancia);
                        proxBuses.addView(add);
                    }else{
                        TextView tv = new TextView(getActivity());
                        tv.setText(object.getString("descripcionError"));
                        tv.setTextColor(Color.BLACK);
                        tv.setPadding(30, 10, 0, 10);
                        tv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        proxBuses.addView(tv);
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            ((LinearLayout)vista.findViewById(R.id.proxBuses)).setVisibility(View.VISIBLE);
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
