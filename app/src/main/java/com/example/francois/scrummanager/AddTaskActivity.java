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

public class AddTaskActivity extends AppCompatActivity {
    private static final String PROJECT_URL = "http://scrummaster.pe.hu/project.php";
    private EditText inputName;
    private EditText inputDescription;
    private int idProjet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        SessionManager session = new SessionManager(getApplicationContext());
        session.checkLogin();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Add A TaskSchedule");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        idProjet = getIntent().getIntExtra("idProjet",0);
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
        Intent intent = new Intent(AddTaskActivity.this, TasksListActivity.class);
        intent.putExtra("idProjet",idProjet);
        intent.putExtra("nameProjet", getIntent().getStringExtra("nameProjet"));
        startActivity(intent);
        finish();
        return true;
    }

    public void add(final String name, final String description) {
        final StringRequest stringRequest = new StringRequest(Request.Method.POST, PROJECT_URL,
                response -> {
                    Toast.makeText(AddTaskActivity.this, response, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(AddTaskActivity.this, TasksListActivity.class);
                    intent.putExtra("idProjet",idProjet);
                    intent.putExtra("nameProjet", getIntent().getStringExtra("nameProjet"));
                    startActivity(intent);
                    finish();
                },
                error -> Toast.makeText(AddTaskActivity.this, error.toString(), Toast.LENGTH_LONG).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "addtask");
                params.put("id_project", Integer.toString(idProjet));
                params.put("name", name);
                params.put("description", description);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}