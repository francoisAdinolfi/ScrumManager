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

public class SubTaskSprintActivity extends AppCompatActivity {
    private static final String PROJECT_URL = "http://scrummaster.pe.hu/project.php";
    private SessionManager session;
    private ListView taskList;
    private int idProjet;
    private ArrayList<String> tasksName = new ArrayList<>();
    private ArrayList<String> taskId = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_task_sprint);

        session = new SessionManager(getApplicationContext());
        session.checkLogin();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Remove Task");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        idProjet = getIntent().getIntExtra("idProjet", 0);
        taskList = (ListView) findViewById(R.id.taskList);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, PROJECT_URL,
                response -> {
                    try {
                        JSONArray j = new JSONArray(response);

                        for (int i = 0; i < j.length(); i++) {
                            JSONObject JOStuff = j.getJSONObject(i);
                            tasksName.add(JOStuff.getString("name"));
                            taskId.add(JOStuff.getString("id_task"));
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(SubTaskSprintActivity.this, android.R.layout.simple_list_item_1, tasksName);
                        taskList.setAdapter(adapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(SubTaskSprintActivity.this, error.toString(), Toast.LENGTH_LONG).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "gettasks");
                params.put("id_sprint", getIntent().getStringExtra("idSprint"));
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

        taskList.setOnItemClickListener((parent, view, position, id) -> {
            sub(getIntent().getStringExtra("idSprint"), taskId.get(tasksName.indexOf(((TextView) view).getText())));
            Intent intent = new Intent(SubTaskSprintActivity.this, SprintTaskListActivity.class);
            intent.putExtra("idProjet", idProjet);
            intent.putExtra("nameProjet", getIntent().getStringExtra("nameProjet"));
            intent.putExtra("idSprint", getIntent().getStringExtra("idSprint"));
            intent.putExtra("nameSprint", getIntent().getStringExtra("nameSprint"));
            startActivity(intent);
            finish();
        });
    }

    public boolean onSupportNavigateUp() {
        Intent intent = new Intent(SubTaskSprintActivity.this, SprintTaskListActivity.class);
        intent.putExtra("idProjet", idProjet);
        intent.putExtra("nameProjet", getIntent().getStringExtra("nameProjet"));
        intent.putExtra("idSprint", getIntent().getStringExtra("idSprint"));
        intent.putExtra("nameSprint", getIntent().getStringExtra("nameSprint"));
        startActivity(intent);
        finish();
        return true;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        if(session.getUserDetails().get(SessionManager.KEY_ROLE).equals("scrummaster")){
            menu.findItem(R.id.action_add).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_setting:
                startActivity(new Intent(SubTaskSprintActivity.this, SettingActivity.class));
                return true;
            case R.id.action_logout:
                session.logoutUser();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void sub(final String idSprint, final String idTask) {
        final StringRequest stringRequest = new StringRequest(Request.Method.POST, PROJECT_URL,
                response -> Toast.makeText(SubTaskSprintActivity.this, response, Toast.LENGTH_LONG).show(),
                error -> Toast.makeText(SubTaskSprintActivity.this, error.toString(), Toast.LENGTH_LONG).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "subTaskSprint");
                params.put("id_sprint", idSprint);
                params.put("id_task", idTask);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}