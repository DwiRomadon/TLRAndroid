package com.project.tlrnew;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import server.AppController;
import server.Url;

public class SendInformation extends AppCompatActivity {

    private static final String TAG = SendInformation.class.getSimpleName();
    EditText edInfo1;
    EditText edInfo2;
    EditText edInfo3;

    private ProgressDialog pDialog;

    private Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slidein, R.anim.slideout);
        setContentView(R.layout.activity_send_information);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("TLR Informasi");

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        edInfo1 = (EditText) findViewById(R.id.pesan);
        edInfo2 = (EditText) findViewById(R.id.pesan2);
        edInfo3 = (EditText) findViewById(R.id.pesan3);

        btnSubmit = (Button) findViewById(R.id.submitButton);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String info1 = edInfo1.getText().toString();
                String info2 = edInfo2.getText().toString();
                String info3    = edInfo3.getText().toString();
                if (!info1.isEmpty() && !info2.isEmpty() &&  !info3.isEmpty()) {
                    sendInformasi(info1, info2, info3);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Please enter your details!", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });
    }

    /*
    fungsi register
     */
    private void sendInformasi(final String info1,final String info2,final String info3) {
        // Tag used to cancel the request
        String tag_string_req = "req_register";

        pDialog.setMessage("Registering ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                Url.BASE_URL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    if (!error) {
                        // Launch login activity
                        String errorrr = jObj.getString("success");
                        Toast.makeText(getApplicationContext(),
                                errorrr, Toast.LENGTH_LONG).show();
                    } else {

                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Registration Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "insertInformasi");
                params.put("info1", info1);
                params.put("info2", info2);
                params.put("info3", info3);
                return params;
            }
        };
        // Adding request to request queue
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


    @Override
    public void onBackPressed() {
        Intent a = new Intent(SendInformation.this, MainActivity.class);
        startActivity(a);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
