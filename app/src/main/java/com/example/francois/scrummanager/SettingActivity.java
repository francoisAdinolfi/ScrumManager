package com.example.francois.scrummanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SettingActivity extends AppCompatActivity {
    private static final String SETTING_URL = "http://scrummaster.pe.hu/setting.php";
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        session = new SessionManager(getApplicationContext());
        session.checkLogin();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Setting");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        final EditText inputCurrentPassword = (EditText) findViewById(R.id.currentPassword);
        final EditText inputNewPassword = (EditText) findViewById(R.id.newPassword);
        Button btnChangePassword = (Button) findViewById(R.id.btnChangePassword);

        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentPassword = inputCurrentPassword.getText().toString().trim();
                String newPassword = inputNewPassword.getText().toString().trim();
                changePassword(currentPassword, newPassword);
            }
        });

        if(!session.getUserDetails().get(SessionManager.KEY_ROLE).equals("scrummaster")) {
            TextView disponibilityTitle = (TextView) findViewById(R.id.disponibilitiyTitle);
            disponibilityTitle.setVisibility(View.VISIBLE);

            final SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
            seekBar.setVisibility(View.VISIBLE);
            seekBar.setMax(14);

            final TextView disponibilityText = (TextView) findViewById(R.id.disponibilitiyText);
            disponibilityText.setVisibility(View.VISIBLE);

            Button btnChangeDisponibility = (Button) findViewById(R.id.btnChangeDisponibility);
            btnChangeDisponibility.setVisibility(View.VISIBLE);

            final StringRequest stringRequest = new StringRequest(Request.Method.POST, SETTING_URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject j = new JSONObject(response);
                                seekBar.setProgress(j.getInt("disponibility"));
                                disponibilityText.setText(j.getInt("disponibility") + " half days");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(SettingActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("tag", "getdisponibility");
                    params.put("id_user", session.getUserDetails().get(SessionManager.KEY_ID));
                    return params;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(stringRequest);

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                int progress = 0;

                public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                    progress = progresValue;
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    disponibilityText.setText(seekBar.getProgress() + " half days");
                }
            });

            btnChangeDisponibility.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int disponibility = seekBar.getProgress();
                    changeDisponibility(disponibility);
                }
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        super.onBackPressed();
        return true;
    }

    public void changePassword(final String currentPassword, final String newPassword) {
        final StringRequest stringRequest = new StringRequest(Request.Method.POST, SETTING_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(SettingActivity.this, response, Toast.LENGTH_LONG).show();
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(SettingActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "changepassword");
                params.put("id_user", session.getUserDetails().get(SessionManager.KEY_ID));
                params.put("current_password", String.valueOf(currentPassword));
                params.put("new_password", String.valueOf(newPassword));
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }


    public void changeDisponibility(final int disponibility) {
        final StringRequest stringRequest = new StringRequest(Request.Method.POST, SETTING_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(SettingActivity.this, response, Toast.LENGTH_LONG).show();
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(SettingActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "setdisponibility");
                params.put("id_user", session.getUserDetails().get(SessionManager.KEY_ID));
                params.put("disponibility", String.valueOf(disponibility));
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}