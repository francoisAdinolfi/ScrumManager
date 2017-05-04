package com.example.francois.scrummanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class AddTaskActivity extends AppCompatActivity {
    private static final String PROJECT_URL = "http://scrummaster.pe.hu/project.php";
    private Button btnAdd;
    private EditText inputName;
    private EditText inputDescription;
    private SessionManager session;
    private int idProjet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        session = new SessionManager(getApplicationContext());
        session.checkLogin();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Add A Task :");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        idProjet = getIntent().getIntExtra("idProjet",0);
        btnAdd = (Button) findViewById(R.id.btnAdd);
        inputName = (EditText) findViewById(R.id.name);
        inputDescription = (EditText) findViewById(R.id.description);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = inputName.getText().toString().trim();
                String description = inputDescription.getText().toString().trim();

                if (name.isEmpty() || description.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Inputs must be filled", Toast.LENGTH_LONG).show();
                } else {
                    add(name, description);
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent(AddTaskActivity.this, TaskProjetsListActivity.class);
        intent.putExtra("idProjet",idProjet);
        startActivity(intent);
        finish();
        return true;
    }

    public void add(final String name, final String description) {
        final StringRequest stringRequest = new StringRequest(Request.Method.POST, PROJECT_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(AddTaskActivity.this, response, Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(AddTaskActivity.this, TaskProjetsListActivity.class);
                        intent.putExtra("idProjet",idProjet);
                        startActivity(intent);
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(AddTaskActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "addtask");
                params.put("id_projet", Integer.toString(idProjet));
                params.put("name", name);
                params.put("description", description);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}
