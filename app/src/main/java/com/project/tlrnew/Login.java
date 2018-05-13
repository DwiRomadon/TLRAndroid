package com.project.tlrnew;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import server.AppController;
import server.Url;

public class Login extends AppCompatActivity {

    private static final String TAG = Login.class.getSimpleName();

    private EditText edEmail;
    private EditText edPass;

    private Button btnLogin;


    int socketTimeout = 30000;
    RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

    private ProgressDialog pDialog;
    private SessionManager session;
    SharedPreferences prefs;

    private String emailnya = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edEmail = (EditText) findViewById(R.id.email);
        edPass = (EditText) findViewById(R.id.pass);

        prefs = getSharedPreferences("UserDetails",
                Context.MODE_PRIVATE);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Session manager
        session = new SessionManager(getApplicationContext());

        emailnya = prefs.getString("email" ,"");
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
                Intent intent = new Intent(Login.this, MainActivity.class);
                intent.putExtra("email",emailnya);
                startActivity(intent);
                finish();
        }


        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = edEmail.getText().toString();
                String password = edPass.getText().toString();

                //Check for empty data in the form
                if(email.trim().length() > 0 && password.trim().length() > 0){
                    checkLogin(email, password);
                }else{
                    //Prompt user to enter credential
                    Toast.makeText(getApplicationContext(),
                            "Masukan Username atau Password Anda !!",
                            Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private void checkLogin(final String email, final String password){

        //Tag used to cancel the request
        String tag_string_req = "req_login";

        pDialog.setMessage("Login, Please Wait.....");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                Url.BASE_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    if(!error){
                        String nama = jObj.getString("namanya");
                        emailnya = jObj.getString("email");
                        session.setLogin(true);

                        storeRegIdinSharedPref(getApplicationContext(),nama, email);
                            Intent i = new Intent(getApplicationContext(),
                                    MainActivity.class);
                            i.putExtra("nama", nama);
                            i.putExtra("email", email);
                            startActivity(i);
                            finish();
                    }else {
                        String error_msg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                error_msg, Toast.LENGTH_LONG).show();
                    }

                }catch (JSONException e){
                    //JSON error
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError error){
                Log.e(TAG, "Login Error : " + error.getMessage());
                error.printStackTrace();
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                Toast.makeText(getApplicationContext(), "Please Check Your Network Connection", Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }){

            @Override
            protected Map<String, String> getParams(){
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "login");
                params.put("email", email);
                params.put("password", password);
                return params;
            }
        };

        strReq.setRetryPolicy(policy);
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);

    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    private void storeRegIdinSharedPref(Context context,String nama, String email) {

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("nama", nama);
        editor.putString("email", email);
        editor.commit();
    }
}
