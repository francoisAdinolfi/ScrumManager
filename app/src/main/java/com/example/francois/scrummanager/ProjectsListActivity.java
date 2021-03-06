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

public class ProjectsListActivity extends AppCompatActivity {
    private static final String PROJECT_URL = "http://scrummaster.pe.hu/project.php";
    private SessionManager session;
    private ListView listProjects;
    private ArrayList<String> projects = new ArrayList<>();
    private ArrayList<Integer> idProjects = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects_list);

        session = new SessionManager(getApplicationContext());
        session.checkLogin();
        final HashMap<String, String> user = session.getUserDetails();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("My Projects");
        setSupportActionBar(toolbar);

        listProjects = (ListView) findViewById(R.id.listProjects);

        final StringRequest stringRequest = new StringRequest(Request.Method.POST, PROJECT_URL,
                response -> {
                    try {
                        JSONArray j = new JSONArray(response);

                        for (int i = 0; i < j.length(); i++) {
                            JSONObject JOStuff = j.getJSONObject(i);
                            projects.add(JOStuff.getString("name"));
                            idProjects.add(JOStuff.getInt("id_project"));
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(ProjectsListActivity.this, android.R.layout.simple_list_item_1, projects);
                        listProjects.setAdapter(adapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(ProjectsListActivity.this, error.toString(), Toast.LENGTH_LONG).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                if(user.get(SessionManager.KEY_ROLE).equals("scrummaster"))
                    params.put("tag", "getscrummasterprojects");
                else
                    params.put("tag", "getdeveloperprojects");
                params.put("id_user", user.get(SessionManager.KEY_ID));
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

        listProjects.setOnItemClickListener((parent, view, position, id) -> {
            int idprojet = idProjects.get(projects.indexOf(((TextView) view).getText()));
            Intent intent = new Intent(ProjectsListActivity.this, ProjectMenuActivity.class);
            intent.putExtra("idProjet", idprojet);
            intent.putExtra("nameProjet", ((TextView) view).getText());
            startActivity(intent);
            finish();
        });
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
                startActivity(new Intent(ProjectsListActivity.this, AddProjectActivity.class));
                finish();
                return true;
            case R.id.action_setting:
                startActivity(new Intent(ProjectsListActivity.this, SettingActivity.class));
                return true;
            case R.id.action_logout:
                session.logoutUser();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}