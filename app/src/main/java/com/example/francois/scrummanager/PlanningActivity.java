package com.example.francois.scrummanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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

public class PlanningActivity extends AppCompatActivity {
    private static final String DEVELOPER_URL = "http://scrummaster.pe.hu/developer.php";
    private int idProjet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planning);

        SessionManager session = new SessionManager(getApplicationContext());
        session.checkLogin();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(session.getUserDetails().get(SessionManager.KEY_NAME));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        idProjet = getIntent().getIntExtra("idProjet", 0);

        ListView planningList = (ListView) findViewById(R.id.planningList);

        final StringRequest stringRequest = new StringRequest(Request.Method.POST, DEVELOPER_URL,
                response -> {
                    try {
                        JSONArray j = new JSONArray(response);
                        ArrayList<String> planning = new ArrayList<>();
                        planning.add(j.getJSONObject(0).getString("project_name"));
                        for (int i = 0; i < j.length(); i++) {
                            JSONObject JOStuff = j.getJSONObject(i);
                            planning.add(JOStuff.getString("name") + "     Start : " + JOStuff.getString("start") + "     End : " + JOStuff.getString("end"));
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(PlanningActivity.this, android.R.layout.simple_list_item_1, planning);
                        planningList.setAdapter(adapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(PlanningActivity.this, error.toString(), Toast.LENGTH_LONG).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "getplanning");
                params.put("id_user", session.getUserDetails().get(SessionManager.KEY_ID));
                params.put("id_project", Integer.toString(idProjet));
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent(PlanningActivity.this, ProjectMenuActivity.class);
        intent.putExtra("idProjet", idProjet);
        intent.putExtra("nameProjet", getIntent().getStringExtra("nameProjet"));
        startActivity(intent);
        finish();
        return true;
    }
}
