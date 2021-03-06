package com.example.yahya.esp.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.yahya.esp.R;

import java.util.HashMap;
import java.util.Map;

import com.example.yahya.esp.db.config;
import com.example.yahya.esp.locationpkg.LocationFinder;


public class SOSActivity extends AppCompatActivity {
    private static final String TAG = SOSActivity.class.getSimpleName();
    double latitude;
    double longitude;

    private ProgressDialog pDialog;
    private AlertDialog.Builder noInternet_adb;
    private AlertDialog.Builder failed_adb;
    private AlertDialog.Builder succeed_adb;
    private AlertDialog noInternet_ad;
    private AlertDialog failed_ad;
    private AlertDialog succeed_ad;

    String url;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sos_layout);

        sharedPreferences = getSharedPreferences(config.SHARED_PREF,0);

        url = getString(R.string.server)+getString(R.string.path_1);

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Sending...");
        pDialog.setCancelable(false);

        noInternetDialog();
        failedDialog();
        succeedDialog();
    }

    private void noInternetDialog(){
        noInternet_adb = new AlertDialog.Builder(this);
        noInternet_adb.setMessage("SOS Request");

        //you can use db.setView(R.layout.layoutname) but it requires 21 api and above,
        //this app minimum api is 18
        LayoutInflater inflater = (LayoutInflater)this.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.nointernet_dialog, null);

        noInternet_adb.setView(v);

        noInternet_adb.setCancelable(false);
        noInternet_adb.setPositiveButton("OK", null);

        noInternet_ad = noInternet_adb.create();
    }

    private void failedDialog(){
        failed_adb = new AlertDialog.Builder(this);
        failed_adb.setMessage("SOS Request");

        //you can use db.setView(R.layout.layoutname) but it requires 21 api and above,
        //this app minimum api is 18
        LayoutInflater inflater = (LayoutInflater)this.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.failed_dialog, null);

        failed_adb.setView(v);

        failed_adb.setCancelable(false);
        failed_adb.setPositiveButton("OK", null);

        failed_ad = failed_adb.create();
    }

    private void succeedDialog(){
        succeed_adb = new AlertDialog.Builder(this);
        succeed_adb.setMessage("SOS Request");

        //you can use db.setView(R.layout.layoutname) but it requires 21 api and above,
        //this app minimum api is 18
        LayoutInflater inflater = (LayoutInflater)this.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.succeed_dialog, null);

        succeed_adb.setView(v);

        succeed_adb.setCancelable(false);
        succeed_adb.setPositiveButton("OK", null);

        succeed_ad = succeed_adb.create();
    }

    public void send(View view){
        if(!isNetworkAvailable()){
            //Toast.makeText(this,"You need Internet connection to Send.",Toast.LENGTH_SHORT).show();
            noInternet_ad.show();
            return;
        }

        try {
            longitude = LocationFinder.getInstance().getLocation().getLongitude();
            latitude = LocationFinder.getInstance().getLocation().getLatitude();
        }catch(NullPointerException e){
            Log.d(TAG, e.getMessage());
            Toast.makeText(this,"Cannot find your location. Please restart the application",Toast.LENGTH_SHORT).show();
            return;
        }

        String regId = sharedPreferences.getString(config.tokenId,"");
        if(TextUtils.isEmpty(regId)){
            Toast.makeText(this,"You cannot communicate with the server",Toast.LENGTH_SHORT).show();
            return;
        }

        sendDataToServer(regId,2);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void sendDataToServer(final String regId, final int op){
        showpDialog();

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        Log.i("VOLLEY", response);
                        hidepDialog();
                        succeed_ad.show();
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VOLLEY", error.toString());
                        hidepDialog();
                        failed_ad.show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("op", String.valueOf(op));
                params.put("lat", String.valueOf(latitude));
                params.put("lon", String.valueOf(longitude));
                params.put("regId", regId);
                return params;
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                if (response != null) {
                    Log.e("Request Code",String.valueOf(response.statusCode));
                }
                return super.parseNetworkResponse(response);
            }
        };

        postRequest.setRetryPolicy(new DefaultRetryPolicy(0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        LocationFinder.getInstance().getRequestQueueSingleton().addToRequestQueue(postRequest);
    }

    private void showpDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hidepDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    public void back(View view){ finish();}

}
