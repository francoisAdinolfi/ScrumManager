package com.example.francois.scrummanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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

        btnChangePassword.setOnClickListener(v -> {
            String currentPassword = inputCurrentPassword.getText().toString().trim();
            String newPassword = inputNewPassword.getText().toString().trim();
            changePassword(currentPassword, newPassword);
        });

        if(!session.getUserDetails().get(SessionManager.KEY_ROLE).equals("scrummaster")) {
            LinearLayout layout = (LinearLayout) findViewById(R.id.unavailabilitiesLayout);
            layout.setVisibility(View.VISIBLE);

            final StringRequest stringRequest = new StringRequest(Request.Method.POST, SETTING_URL,
                    response -> {
                        try {
                            JSONArray j = new JSONArray(response);
                            for (int i = 0; i < j.length(); i++) {
                                JSONObject JOStuff = j.getJSONObject(i);
                                int resID = getResources().getIdentifier("check" + JOStuff.getInt("day"), "id", getPackageName());
                                CheckBox box = (CheckBox) findViewById(resID);
                                box.setChecked(true);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    },
                    error -> Toast.makeText(SettingActivity.this, error.toString(), Toast.LENGTH_LONG).show()) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("tag", "getunavailabilities");
                    params.put("id_user", session.getUserDetails().get(SessionManager.KEY_ID));
                    return params;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(stringRequest);

            Button btn = (Button) findViewById(R.id.btnChangeUnavailabilities);

            btn.setOnClickListener(v -> {
                ArrayList<Integer> checked = new ArrayList<>();
                for(int i = 0; i < 14; i++){
                    int resID = getResources().getIdentifier("check" + i, "id", getPackageName());
                    CheckBox box = (CheckBox) findViewById(resID);

                    if(box.isChecked()){
                        checked.add(i);
                    }
                }
                changeUnavailabilities(checked);
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
                response -> {
                    Toast.makeText(SettingActivity.this, response, Toast.LENGTH_LONG).show();
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
                },
                error -> Toast.makeText(SettingActivity.this, error.toString(), Toast.LENGTH_LONG).show()) {
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

    public void changeUnavailabilities(final ArrayList<Integer> checked) {
        final StringRequest stringRequest = new StringRequest(Request.Method.POST, SETTING_URL,
                response -> {
                    Toast.makeText(SettingActivity.this, response, Toast.LENGTH_LONG).show();
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
                },
                error -> Toast.makeText(SettingActivity.this, error.toString(), Toast.LENGTH_LONG).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "setunavailabilities");
                params.put("id_user", session.getUserDetails().get(SessionManager.KEY_ID));
                params.put("c_size", String.valueOf(checked.size()));
                int i = 0;
                for(Integer c : checked){
                    params.put("c" + i, String.valueOf(c));
                    i++;
                }
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}