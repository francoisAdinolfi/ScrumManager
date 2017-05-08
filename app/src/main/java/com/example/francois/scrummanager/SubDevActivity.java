package com.example.francois.scrummanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SubDevActivity extends AppCompatActivity {

    private static final String PROJECT_URL = "http://scrummaster.pe.hu/developer.php";
    private SessionManager session;
    private ListView devList;
    private int idProjet;
    private ArrayList<String> developerName = new ArrayList<>();
    private ArrayList<String> developerId = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_dev);

        session = new SessionManager(getApplicationContext());
        session.checkLogin();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("TaskList :");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        idProjet = getIntent().getIntExtra("idProjet",0);
        devList = (ListView) findViewById(R.id.subdevlist);

        final StringRequest stringRequest = new StringRequest(Request.Method.POST, PROJECT_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                         try {
                             JSONArray j = new JSONArray(response);

                            for (int i = 0; i < j.length(); i++) {
                                JSONObject JOStuff = j.getJSONObject(i);
                                developerId.add(JOStuff.getString("id_user"));
                            }

                             ArrayAdapter<String> adapter = new ArrayAdapter<>(SubDevActivity.this, android.R.layout.simple_list_item_1, developerId);
                             devList.setAdapter(adapter);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(SubDevActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "subdevpart1");
                params.put("id_project", Integer.toString(idProjet));
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(SubDevActivity.this, android.R.layout.simple_list_item_1, developerId);
        devList.setAdapter(adapter);

        /*for(int i = 0; i <developerId.size(); i++) {
            final int index = i;
            final StringRequest stringRequest2 = new StringRequest(Request.Method.POST, PROJECT_URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONArray j = new JSONArray(response);

                                for (int i = 0; i < j.length(); i++) {
                                    JSONObject JOStuff = j.getJSONObject(i);
                                    developerName.add(JOStuff.getString("name"));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(SubDevActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("tag", "subdevpart2");
                    params.put("id_user", developerId.get(index));
                    return params;
                }
            };
            requestQueue.add(stringRequest2);
        }*/
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent(SubDevActivity.this, TaskProjetsListActivity.class);
        intent.putExtra("idProjet",idProjet);
        startActivity(intent);
        finish();
        return true;
    }
}
