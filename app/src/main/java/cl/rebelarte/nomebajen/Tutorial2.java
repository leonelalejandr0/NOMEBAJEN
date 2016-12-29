package cl.rebelarte.nomebajen;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;


public class Tutorial2 extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v =  inflater.inflate(R.layout.fragment_tutorial2, container, false);
        eventos(v);
        return v;
    }

    public void eventos(View view){
        WebView myWebView = (WebView)view.findViewById(R.id.tutorial);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        myWebView.loadUrl("http://www.leonelalejandro.cl/nmb/nmb/index.html");

        final Boolean act = getActivity().getIntent().getExtras().getBoolean("Actividad");

        FloatingActionButton btn = (FloatingActionButton)view.findViewById(R.id.fab);
        if(act)
            btn.setImageResource(R.drawable.ic_arrow_forward_24dp);
        else
            btn.setImageResource(R.drawable.ic_back);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(act){
                    AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
                    ad.setTitle("Tutorial");
                    ad.setMessage("Si después quedas con alguna duda, puedes volver a ver este tutorial en las opciones del Panel")
                            .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent i = new Intent(getActivity(),Login.class);
                                    startActivity(i);
                                    getActivity().finish();

                                }
                            });
                    ad.show();
                }else{
                    getActivity().finish();
                }
            }
        });

    }


}
