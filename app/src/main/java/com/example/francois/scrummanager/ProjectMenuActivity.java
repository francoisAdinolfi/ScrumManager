package com.example.francois.scrummanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;

import java.util.HashMap;

public class ProjectMenuActivity extends AppCompatActivity {

    private SessionManager session;
    private int idProjet;
    private ImageButton btnProductBacklog;
    private ImageButton btnSprint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_menu);

        session = new SessionManager(getApplicationContext());
        session.checkLogin();
        final HashMap<String, String> user = session.getUserDetails();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getIntent().getStringExtra("nameProjet"));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        idProjet = getIntent().getIntExtra("idProjet", 0);

        btnProductBacklog = (ImageButton) findViewById(R.id.btnProductBacklog);
        btnSprint = (ImageButton) findViewById(R.id.btnSprint);

        btnProductBacklog.setOnClickListener(v -> {
            Intent intent = new Intent(ProjectMenuActivity.this, ProductBacklogActivity.class);
            intent.putExtra("idProjet",idProjet);
            intent.putExtra("nameProjet", getIntent().getStringExtra("nameProjet"));
            startActivity(intent);
            finish();
        });

        btnSprint.setOnClickListener(v -> {
            Intent intent = new Intent(ProjectMenuActivity.this, SprintListActivity.class);
            intent.putExtra("idProjet",idProjet);
            intent.putExtra("nameProjet", getIntent().getStringExtra("nameProjet"));
            startActivity(intent);
            finish();
        });

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
}
