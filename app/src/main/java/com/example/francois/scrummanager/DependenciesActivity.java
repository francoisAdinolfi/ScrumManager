package com.example.francois.scrummanager;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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

public class DependenciesActivity extends AppCompatActivity {
    private static final String DEPENDENCE_URL = "http://scrummaster.pe.hu/dependence.php";
    private SessionManager session;
    private int idProjet;
    private ArrayList<ArrayList<String>> tasks;
    private ArrayList<ArrayList<String>> dependencies = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dependencies);

        session = new SessionManager(getApplicationContext());
        session.checkLogin();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Dependencies");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        idProjet = getIntent().getIntExtra("idProjet",0);
        tasks = (ArrayList<ArrayList<String>>) getIntent().getSerializableExtra("tasks");

        final ListView dependenciesList = (ListView) findViewById(R.id.dependenciesList);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, DEPENDENCE_URL,
                response -> {
                    try {
                        JSONArray j = new JSONArray(response);
                        ArrayList<String> dependenciesName = new ArrayList<>();

                        for (int i = 0; i < j.length(); i++) {
                            JSONObject JOStuff = j.getJSONObject(i);
                            ArrayList<String> tmp = new ArrayList<>();
                            String task1 = "", task2 = "";
                            tmp.add(JOStuff.getString("id_task"));
                            tmp.add(JOStuff.getString("id_task_1"));
                            dependencies.add(tmp);
                            for(ArrayList<String> al : tasks){
                                if(al.get(0).equals(JOStuff.getString("id_task"))){
                                    task1 = al.get(1);
                                }
                                if(al.get(0).equals(JOStuff.getString("id_task_1"))){
                                    task2 = al.get(1);
                                }
                            }
                            dependenciesName.add(task1 + " > " + task2);
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(DependenciesActivity.this, android.R.layout.simple_list_item_1, dependenciesName);
                        dependenciesList.setAdapter(adapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(DependenciesActivity.this, error.toString(), Toast.LENGTH_LONG).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "getdependencies");
                params.put("id_project", Integer.toString(idProjet));
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

        dependenciesList.setOnItemClickListener((parent, view, position, id) -> {
            final String id_task1 = dependencies.get((int) id).get(0);
            final String id_task2 = dependencies.get((int) id).get(1);
            new AlertDialog.Builder(DependenciesActivity.this)
                    .setTitle("Delete")
                    .setMessage("Are you sure you want to delete this dependence ?")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> del(id_task1, id_task2))
                    .setNegativeButton(android.R.string.no, (dialog, which) -> { })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent(DependenciesActivity.this, TasksListActivity.class);
        intent.putExtra("idProjet",idProjet);
        intent.putExtra("nameProjet", getIntent().getStringExtra("nameProjet"));
        startActivity(intent);
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_add:
                Intent intent = new Intent(DependenciesActivity.this, AddDependenceActivity.class);
                intent.putExtra("idProjet",idProjet);
                intent.putExtra("nameProjet", getIntent().getStringExtra("nameProjet"));
                intent.putExtra("tasks", tasks);
                startActivity(intent);
                finish();
                return true;
            case R.id.action_setting:
                startActivity(new Intent(DependenciesActivity.this, SettingActivity.class));
                return true;
            case R.id.action_logout:
                session.logoutUser();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void del(final String id_task1, final String id_task2) {
        final StringRequest stringRequest = new StringRequest(Request.Method.POST, DEPENDENCE_URL,
                response -> {
                    Toast.makeText(DependenciesActivity.this, response, Toast.LENGTH_LONG).show();
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
                },
                error -> Toast.makeText(DependenciesActivity.this, error.toString(), Toast.LENGTH_LONG).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "deldependence");
                params.put("id_task1", id_task1);
                params.put("id_task2", id_task2);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}
