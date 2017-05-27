package com.example.francois.scrummanager;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class ProjectMenuActivity extends AppCompatActivity {
    private static final String DELETE_URL = "http://scrummaster.pe.hu/delete.php";
    private SessionManager session;
    private int idProjet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_menu);

        session = new SessionManager(getApplicationContext());
        session.checkLogin();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getIntent().getStringExtra("nameProjet"));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        idProjet = getIntent().getIntExtra("idProjet", 0);

        ImageButton btnSprint = (ImageButton) findViewById(R.id.btnSprint);

        btnSprint.setOnClickListener(v -> {
            Intent intent = new Intent(ProjectMenuActivity.this, SprintListActivity.class);
            intent.putExtra("idProjet", idProjet);
            intent.putExtra("nameProjet", getIntent().getStringExtra("nameProjet"));
            startActivity(intent);
            finish();
        });

        if(session.getUserDetails().get(SessionManager.KEY_ROLE).equals("scrummaster")){
            ImageButton btnProductBacklog = (ImageButton) findViewById(R.id.btnProductBacklog);
            btnProductBacklog.setVisibility(View.VISIBLE);

            btnProductBacklog.setOnClickListener(v -> {
                Intent intent = new Intent(ProjectMenuActivity.this, ProductBacklogActivity.class);
                intent.putExtra("idProjet",idProjet);
                intent.putExtra("nameProjet", getIntent().getStringExtra("nameProjet"));
                startActivity(intent);
                finish();
            });

            Button btnDelete = (Button) findViewById(R.id.btnDelete);
            btnDelete.setVisibility(View.VISIBLE);

            btnDelete.setOnClickListener(v -> new AlertDialog.Builder(ProjectMenuActivity.this)
                    .setTitle("Delete")
                    .setMessage("Are you sure you want to delete this project ?")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> del(idProjet))
                    .setNegativeButton(android.R.string.no, (dialog, which) -> { })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
            );
        } else {
            ImageButton btnPlanning = (ImageButton) findViewById(R.id.btnPlanning);
            btnPlanning.setVisibility(View.VISIBLE);

            btnPlanning.setOnClickListener(v -> {
                Intent intent = new Intent(ProjectMenuActivity.this, PlanningActivity.class);
                intent.putExtra("idProjet",idProjet);
                intent.putExtra("nameProjet", getIntent().getStringExtra("nameProjet"));
                startActivity(intent);
                finish();
            });
        }
    }

    public boolean onSupportNavigateUp() {
        Intent intent = new Intent(ProjectMenuActivity.this, ProjectsListActivity.class);
        startActivity(intent);
        finish();
        return true;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menuproject, menu);
        if(!session.getUserDetails().get(SessionManager.KEY_ROLE).equals("scrummaster")){
            menu.findItem(R.id.action_subdev).setVisible(false);
            menu.findItem(R.id.action_adddev).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_adddev:
                Intent intent = new Intent(ProjectMenuActivity.this, AddDevActivity.class);
                intent.putExtra("idProjet",idProjet);
                intent.putExtra("nameProjet", getIntent().getStringExtra("nameProjet"));
                startActivity(intent);
                finish();
                return true;
            case R.id.action_subdev:
                intent = new Intent(ProjectMenuActivity.this, SubDevActivity.class);
                intent.putExtra("idProjet",idProjet);
                intent.putExtra("nameProjet", getIntent().getStringExtra("nameProjet"));
                startActivity(intent);
                finish();
                return true;
            case R.id.action_setting:
                startActivity(new Intent(ProjectMenuActivity.this, SettingActivity.class));
                return true;
            case R.id.action_logout:
                session.logoutUser();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void del(final int id_project) {
        final StringRequest stringRequest = new StringRequest(Request.Method.POST, DELETE_URL,
                response -> {
                    Toast.makeText(ProjectMenuActivity.this, response, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(ProjectMenuActivity.this, ProjectsListActivity.class);
                    startActivity(intent);
                    finish();
                },
                error -> Toast.makeText(ProjectMenuActivity.this, error.toString(), Toast.LENGTH_LONG).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "delproject");
                params.put("id_project", String.valueOf(id_project));
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}
