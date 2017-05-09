package com.example.francois.scrummanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TaskProjetsListActivity extends AppCompatActivity {

    private static final String PROJECT_URL = "http://scrummaster.pe.hu/project.php";
    private static final String DELETE_URL = "http://scrummaster.pe.hu/delete.php";
    private SessionManager session;
    private ListView taskList;
    private int idProjet;
    private ArrayList<String> tasksName = new ArrayList<>();
    private ArrayList<ArrayList<String>> tasks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_projets_list);

        session = new SessionManager(getApplicationContext());
        session.checkLogin();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("TaskList :");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        idProjet = getIntent().getIntExtra("idProjet",0);
        //Toast.makeText(TaskProjetsListActivity.this, idProjet, Toast.LENGTH_LONG).show();
        taskList = (ListView) findViewById(R.id.taskList);

        if(session.getUserDetails().get(SessionManager.KEY_ROLE).equals("scrummaster")) {
            final Button btnDelete = (Button) findViewById(R.id.btnDelete);
            btnDelete.setVisibility(View.VISIBLE);

            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    delete(idProjet);
                }
            });
        }

        final StringRequest stringRequest = new StringRequest(Request.Method.POST, PROJECT_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray j = new JSONArray(response);

                            for (int i = 0; i < j.length(); i++) {
                                JSONObject JOStuff = j.getJSONObject(i);
                                tasksName.add(JOStuff.getString("name"));
                                ArrayList<String> taskTmp = new ArrayList<>();
                                taskTmp.add(JOStuff.getString("id_task"));
                                taskTmp.add(JOStuff.getString("name"));
                                taskTmp.add(JOStuff.getString("description"));
                                taskTmp.add(JOStuff.getString("id_project"));
                                tasks.add(taskTmp);
                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(TaskProjetsListActivity.this, android.R.layout.simple_list_item_1, tasksName);
                            taskList.setAdapter(adapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(TaskProjetsListActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "scrummastertasks");
                params.put("id_project", Integer.toString(idProjet));
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

        taskList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ArrayList<String> task = tasks.get(tasksName.indexOf(((TextView) view).getText()));
                Intent intent = new Intent(TaskProjetsListActivity.this, TaskActivity.class);
                intent.putExtra("task", task);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        super.onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menutask, menu);
        if(!session.getUserDetails().get(SessionManager.KEY_ROLE).equals("scrummaster")){
            menu.findItem(R.id.action_addtask).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_logout:
                session.logoutUser();
                return true;
            case R.id.action_addtask:
                Intent intent = new Intent(TaskProjetsListActivity.this, AddTaskActivity.class);
                intent.putExtra("idProjet",idProjet);
                startActivity(intent);
                finish();
                return true;
            case R.id.action_subdev:
                intent = new Intent(TaskProjetsListActivity.this, SubDevActivity.class);
                intent.putExtra("idProjet",idProjet);
                startActivity(intent);
                finish();
                return true;
            case R.id.action_adddev:
                intent = new Intent(TaskProjetsListActivity.this, AddDevActivity.class);
                intent.putExtra("idProjet",idProjet);
                startActivity(intent);
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void delete(final int idProjet){
        final StringRequest stringRequest = new StringRequest(Request.Method.POST, DELETE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(TaskProjetsListActivity.this, response, Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(TaskProjetsListActivity.this, ProjectsListActivity.class);
                        startActivity(intent);
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(TaskProjetsListActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "delproject");
                params.put("id_project", Integer.toString(idProjet));
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}
