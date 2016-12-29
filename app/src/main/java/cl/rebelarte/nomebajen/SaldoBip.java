package cl.rebelarte.nomebajen;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;


public class SaldoBip extends Fragment {

    String tarjeta = "";
    View saldo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       View v = inflater.inflate(R.layout.fragment_saldo_bip, container, false);
        saldo = v;

        eventosFragment(v);
        return v;
    }

    public void eventosFragment(final View view){



        Button saldo = (Button)view.findViewById(R.id.btnVerSaldo);
        saldo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nroTarjeta = ((EditText) view.findViewById(R.id.txtNumeroTarjeta)).getText().toString();
                if (!nroTarjeta.equals("")) {
                    tarjeta = nroTarjeta;
                    if (verificaInternet(getActivity())) {
                        new VerSaldo().execute();
                    } else {
                        Snackbar.make(v, "No tienes conexión a internet", Snackbar.LENGTH_SHORT).show();
                    }
                } else {
                    Snackbar.make(v, "EL NÚMERO DE LA TARJETA ES OBLIGATORIO", Snackbar.LENGTH_SHORT).show();
                }


            }
        });
    }


    public class VerSaldo extends AsyncTask<Void,Void,Void>{
        ProgressDialog pd;
        String jsonstr;
        @Override
        protected void onPreExecute() {
            pd = ProgressDialog.show(getActivity(),"Información BIP!","Consultando sobre la tarjeta BIP! Espera por favor");

        }

        @Override
        protected Void doInBackground(Void... params) {
            String paramsUrl = tarjeta;

            HttpResponse response;
            HttpClient myClient = new DefaultHttpClient();
            HttpGet myConnection = new HttpGet("http://bip.franciscocapone.com/api/getSaldo/"+paramsUrl);

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
            //Log.e("JSON", jsonstr);
            JSONObject jsonObject = null;

            try {
                jsonObject = new JSONObject(jsonstr);
                ((TextView)saldo.findViewById(R.id.lblNumeroTarjeta)).setText(jsonObject.getString("numero_tarjeta"));
                ((TextView)saldo.findViewById(R.id.lblSaldoTarjeta)).setText(jsonObject.getString("saldo"));
                ((TextView)saldo.findViewById(R.id.lblEstadoTarjeta)).setText(jsonObject.getString("estado_contrato"));
                ((TextView)saldo.findViewById(R.id.lblFechaSaldo)).setText(jsonObject.getString("fecha_saldo"));

                ((CardView)saldo.findViewById(R.id.responseSaldo)).setVisibility(View.VISIBLE);

            } catch (JSONException e) {
                e.printStackTrace();
            }
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
