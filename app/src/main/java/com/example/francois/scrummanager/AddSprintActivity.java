package com.example.francois.scrummanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class AddSprintActivity extends AppCompatActivity {

    private static final String PROJECT_URL = "http://scrummaster.pe.hu/sprint.php";
    private EditText inputName;
    private int idProjet;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sprint);

        session = new SessionManager(getApplicationContext());
        session.checkLogin();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Add A Sprint");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Button btnAdd = (Button) findViewById(R.id.btnAdd);
        inputName = (EditText) findViewById(R.id.name);
        idProjet = getIntent().getIntExtra("idProjet", 0);

        btnAdd.setOnClickListener(v -> {
            String name = inputName.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Input must be filled", Toast.LENGTH_LONG).show();
            } else {
                add(name, idProjet);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent(AddSprintActivity.this, SprintListActivity.class);
        intent.putExtra("idProjet", idProjet);
        intent.putExtra("nameProjet", getIntent().getStringExtra("nameProjet"));
        startActivity(intent);
        finish();
        return true;
    }

    public void add(final String name, final int idProjet) {
        final StringRequest stringRequest = new StringRequest(Request.Method.POST, PROJECT_URL,
                response -> {
                    Toast.makeText(AddSprintActivity.this, response, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(AddSprintActivity.this, SprintListActivity.class);
                    intent.putExtra("idProjet", idProjet);
                    intent.putExtra("nameProjet", getIntent().getStringExtra("nameProjet"));
                    startActivity(intent);
                    finish();
                },
                error -> Toast.makeText(AddSprintActivity.this, error.toString(), Toast.LENGTH_LONG).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "addsprint");
                params.put("id_project", Integer.toString(idProjet));
                params.put("name", name);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}