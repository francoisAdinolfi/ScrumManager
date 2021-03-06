package com.example.francois.scrummanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
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

public class AddDevActivity extends AppCompatActivity {
    private static final String DEVELOPER_URL = "http://scrummaster.pe.hu/developer.php";
    private ListView devList;
    private int idProjet;
    private EditText inputName;
    private ArrayList<String> developerName;
    private ArrayList<String> developerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_dev);

        SessionManager session = new SessionManager(getApplicationContext());
        session.checkLogin();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Add A Developer");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        idProjet = getIntent().getIntExtra("idProjet", 0);
        devList = (ListView) findViewById(R.id.listDev);
        Button btnSearch = (Button) findViewById(R.id.btnSearch);
        inputName = (EditText) findViewById(R.id.name);

        btnSearch.setOnClickListener(v -> {
            String name = inputName.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Inputs must be filled", Toast.LENGTH_LONG).show();
            } else {
                search(name);
            }
        });

        devList.setOnItemClickListener((parent, view, position, id) -> {
            add(developerId.get(developerName.indexOf(((TextView) view).getText())));
            Intent intent = new Intent(AddDevActivity.this, ProjectMenuActivity.class);
            intent.putExtra("idProjet", idProjet);
            intent.putExtra("nameProjet", getIntent().getStringExtra("nameProjet"));
            startActivity(intent);
            finish();
        });
    }

    public boolean onSupportNavigateUp() {
        Intent intent = new Intent(AddDevActivity.this, ProjectMenuActivity.class);
        intent.putExtra("idProjet", idProjet);
        intent.putExtra("nameProjet", getIntent().getStringExtra("nameProjet"));
        startActivity(intent);
        finish();
        return true;
    }

    public void search(final String name) {
        final StringRequest stringRequest = new StringRequest(Request.Method.POST, DEVELOPER_URL,
                response -> {
                    try {
                        JSONArray j = new JSONArray(response);

                        developerName = new ArrayList<>();
                        developerId = new ArrayList<>();

                        for (int i = 0; i < j.length(); i++) {
                            JSONObject JOStuff = j.getJSONObject(i);
                            developerName.add(JOStuff.getString("name"));
                            developerId.add(JOStuff.getString("id_user"));
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(AddDevActivity.this, android.R.layout.simple_list_item_1, developerName);
                        devList.setAdapter(adapter);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(AddDevActivity.this, error.toString(), Toast.LENGTH_LONG).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "searchdev");
                params.put("name", name);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    public void add(final String id) {
        final StringRequest stringRequest = new StringRequest(Request.Method.POST, DEVELOPER_URL,
                response -> Toast.makeText(AddDevActivity.this, response, Toast.LENGTH_LONG).show(),
                error -> Toast.makeText(AddDevActivity.this, error.toString(), Toast.LENGTH_LONG).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "adddev");
                params.put("id_user", id);
                params.put("id_projet", Integer.toString(idProjet));
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}