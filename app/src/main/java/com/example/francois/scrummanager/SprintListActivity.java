package com.example.francois.scrummanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
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

public class SprintListActivity extends AppCompatActivity {
    private static final String PROJECT_URL = "http://scrummaster.pe.hu/sprint.php";
    private SessionManager session;
    private ListView listSprint;
    private int idProjet;
    private ArrayList<String> sprints = new ArrayList<>();
    private ArrayList<Integer> idSprints = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sprint_list2);

        session = new SessionManager(getApplicationContext());
        session.checkLogin();
        final HashMap<String, String> user = session.getUserDetails();

        idProjet = getIntent().getIntExtra("idProjet", 0);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Sprints");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        listSprint = (ListView) findViewById(R.id.listSprint);

        final StringRequest stringRequest = new StringRequest(Request.Method.POST, PROJECT_URL,
                response -> {
                    try {
                        JSONArray j = new JSONArray(response);

                        for (int i = 0; i < j.length(); i++) {
                            JSONObject JOStuff = j.getJSONObject(i);
                            sprints.add(JOStuff.getString("name"));
                            idSprints.add(JOStuff.getInt("id_sprint"));
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(SprintListActivity.this, android.R.layout.simple_list_item_1, sprints);
                        listSprint.setAdapter(adapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(SprintListActivity.this, error.toString(), Toast.LENGTH_LONG).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "getsprint");
                params.put("id_project", Integer.toString(idProjet));
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

        listSprint.setOnItemClickListener((parent, view, position, id) -> {
            int idsprint = idSprints.get(sprints.indexOf(((TextView) view).getText()));
            Intent intent = new Intent(SprintListActivity.this, SprintTaskListActivity.class);
            intent.putExtra("idProjet", idProjet);
            intent.putExtra("nameProjet", getIntent().getStringExtra("nameProjet"));
            intent.putExtra("idSprint", Integer.toString(idsprint));
            intent.putExtra("nameSprint", ((TextView) view).getText());
            startActivity(intent);
            finish();
        });
    }

    public boolean onSupportNavigateUp() {
        Intent intent = new Intent(SprintListActivity.this, ProjectMenuActivity.class);
        intent.putExtra("idProjet", idProjet);
        intent.putExtra("nameProjet", getIntent().getStringExtra("nameProjet"));
        startActivity(intent);
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        if(!session.getUserDetails().get(SessionManager.KEY_ROLE).equals("scrummaster")){
            menu.findItem(R.id.action_add).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_add:
                Intent intent = new Intent(SprintListActivity.this, AddSprintActivity.class);
                intent.putExtra("idProjet", idProjet);
                intent.putExtra("nameProjet", getIntent().getStringExtra("nameProjet"));
                startActivity(intent);
                finish();
                return true;
            case R.id.action_setting:
                startActivity(new Intent(SprintListActivity.this, SettingActivity.class));
                return true;
            case R.id.action_logout:
                session.logoutUser();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}