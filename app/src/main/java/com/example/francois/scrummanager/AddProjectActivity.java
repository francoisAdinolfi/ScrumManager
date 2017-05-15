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

public class AddProjectActivity extends AppCompatActivity {
    private static final String PROJECT_URL = "http://scrummaster.pe.hu/project.php";
    private EditText inputName;
    private EditText inputDescription;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_project);

        session = new SessionManager(getApplicationContext());
        session.checkLogin();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Add A Project");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Button btnAdd = (Button) findViewById(R.id.btnAdd);
        inputName = (EditText) findViewById(R.id.name);
        inputDescription = (EditText) findViewById(R.id.description);

        btnAdd.setOnClickListener(v -> {
            String name = inputName.getText().toString().trim();
            String description = inputDescription.getText().toString().trim();

            if (name.isEmpty() || description.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Inputs must be filled", Toast.LENGTH_LONG).show();
            } else {
                add(name, description);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent(AddProjectActivity.this, ProjectsListActivity.class);
        startActivity(intent);
        finish();
        return true;
    }

    public void add(final String name, final String description) {
        final StringRequest stringRequest = new StringRequest(Request.Method.POST, PROJECT_URL,
                response -> {
                    Toast.makeText(AddProjectActivity.this, response, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(AddProjectActivity.this, ProjectsListActivity.class);
                    startActivity(intent);
                    finish();
                },
                error -> Toast.makeText(AddProjectActivity.this, error.toString(), Toast.LENGTH_LONG).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "addproject");
                params.put("id_user", session.getUserDetails().get(SessionManager.KEY_ID));
                params.put("name", name);
                params.put("description", description);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}