package com.example.francois.scrummanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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

public class AddDependenceActivity extends AppCompatActivity {
    private static final String DEPENDENCE_URL = "http://scrummaster.pe.hu/dependence.php";
    private static final String PROJECT_URL = "http://scrummaster.pe.hu/project.php";
    private int idProjet;
    private ArrayList<String> tasksId = new ArrayList<>();
    private ArrayList<String> tasksName = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_dependence);

        SessionManager session = new SessionManager(getApplicationContext());
        session.checkLogin();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Add A Dependence");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        idProjet = getIntent().getIntExtra("idProjet",0);

        final Spinner spinner1 = (Spinner) findViewById(R.id.spinner1);
        final Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);

        final StringRequest stringRequest = new StringRequest(Request.Method.POST, PROJECT_URL,
                response -> {
                    try {
                        JSONArray j = new JSONArray(response);

                        for (int i = 0; i < j.length(); i++) {
                            JSONObject JOStuff = j.getJSONObject(i);
                            tasksId.add(JOStuff.getString("id_task"));
                            tasksName.add(JOStuff.getString("name"));
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(AddDependenceActivity.this, android.R.layout.simple_spinner_item, tasksName);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner1.setAdapter(adapter);
                        spinner2.setAdapter(adapter);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(AddDependenceActivity.this, error.toString(), Toast.LENGTH_LONG).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "gettasks2");
                params.put("id_project", String.valueOf(idProjet));
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

        Button btnAddDependence = (Button) findViewById(R.id.btnAddDependence);

        btnAddDependence.setOnClickListener(v -> {
            String id_task1 = tasksId.get(spinner1.getSelectedItemPosition());
            String id_task2 = tasksId.get(spinner2.getSelectedItemPosition());
            if (id_task1.equals(id_task2)) {
                Toast.makeText(getApplicationContext(), "Tasks should be different", Toast.LENGTH_LONG).show();
            } else {
                add(id_task1, id_task2);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent(AddDependenceActivity.this, DependenciesActivity.class);
        intent.putExtra("idProjet", idProjet);
        intent.putExtra("nameProjet", getIntent().getStringExtra("nameProjet"));
        intent.putExtra("tasks", getIntent().getSerializableExtra("tasks"));
        startActivity(intent);
        finish();
        return true;
    }

    public void add(final String id_task1, final String id_task2) {
        final StringRequest stringRequest = new StringRequest(Request.Method.POST, DEPENDENCE_URL,
                response -> {
                    Toast.makeText(AddDependenceActivity.this, response, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(AddDependenceActivity.this, DependenciesActivity.class);
                    intent.putExtra("idProjet", idProjet);
                    intent.putExtra("nameProjet", getIntent().getStringExtra("nameProjet"));
                    intent.putExtra("tasks", getIntent().getSerializableExtra("tasks"));
                    startActivity(intent);
                    finish();
                },
                error -> Toast.makeText(AddDependenceActivity.this, error.toString(), Toast.LENGTH_LONG).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "adddependence");
                params.put("id_task1", id_task1);
                params.put("id_task2", id_task2);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}
