package com.example.francois.scrummanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
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

public class TaskActivity extends AppCompatActivity {
    private static final String VOTE_URL = "http://scrummaster.pe.hu/vote.php";
    private static final String DELETE_URL = "http://scrummaster.pe.hu/delete.php";
    private SessionManager session;
    private ArrayList<String> votes = new ArrayList<>();
    private TextView estimationText;
    private ArrayList<String> task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        session = new SessionManager(getApplicationContext());
        session.checkLogin();

        task = (ArrayList<String>) getIntent().getSerializableExtra("task");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(task.get(1));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        final TextView description = (TextView) findViewById(R.id.description);
        description.setText(task.get(2));

        final TextView TextVote = (TextView) findViewById(R.id.textVote);

        if(session.getUserDetails().get(SessionManager.KEY_ROLE).equals("scrummaster")){
            TextVote.setText("Votes : ");
            final TextView countVotes = (TextView) findViewById(R.id.countVotes);
            countVotes.setVisibility(View.VISIBLE);
            final ListView listVotes = (ListView) findViewById(R.id.listVotes);
            listVotes.setVisibility(View.VISIBLE);
            final Button btnDelete = (Button) findViewById(R.id.btnDelete);
            btnDelete.setVisibility(View.VISIBLE);

            final StringRequest stringRequest = new StringRequest(Request.Method.POST, VOTE_URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONArray j = new JSONArray(response);

                                countVotes.setText(j.length()-1 + " / " + j.getJSONObject(j.length()-1).getString("0"));

                                for (int i = 0; i < j.length()-1; i++) {
                                    JSONObject JOStuff = j.getJSONObject(i);
                                    votes.add("Name : " + JOStuff.getString("name") + "     Estimation : " + JOStuff.getString("estimation"));
                                }

                                ArrayAdapter<String> adapter = new ArrayAdapter<>(TaskActivity.this, android.R.layout.simple_list_item_1, votes);
                                listVotes.setAdapter(adapter);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(TaskActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("tag", "getvotes");
                    params.put("id_task", task.get(0));
                    return params;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(stringRequest);

            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    delete(task);
                }
            });

        } else {
            TextVote.setText("Vote : ");
            final StringRequest stringRequest = new StringRequest(Request.Method.POST, VOTE_URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if (response.equals("true")) {
                                findViewById(R.id.isVotedText).setVisibility(View.VISIBLE);
                            } else {
                                findViewById(R.id.seekBar).setVisibility(View.VISIBLE);
                                findViewById(R.id.estimationText).setVisibility(View.VISIBLE);
                                findViewById(R.id.btnVote).setVisibility(View.VISIBLE);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(TaskActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("tag", "isvoted");
                    params.put("id_user", session.getUserDetails().get(SessionManager.KEY_ID));
                    params.put("id_task", task.get(0));
                    return params;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(stringRequest);

            final SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
            seekBar.setMax(14);
            estimationText = (TextView) findViewById(R.id.estimationText);
            estimationText.setText(seekBar.getProgress() + " half days");

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                int progress = 0;

                public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                    progress = progresValue;
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    estimationText.setText(seekBar.getProgress() + " half days");
                }
            });

            Button btnVote = (Button) findViewById(R.id.btnVote);

            btnVote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int estimation = seekBar.getProgress();
                    if (estimation == 0) {
                        Toast.makeText(getApplicationContext(), "The estimation should be different from 0", Toast.LENGTH_LONG).show();
                    } else {
                        add(estimation, task);
                    }
                }
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent(TaskActivity.this, TasksListActivity.class);
        intent.putExtra("idProjet", Integer.valueOf(task.get(3)));
        startActivity(intent);
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        menu.findItem(R.id.action_add).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_setting:
                startActivity(new Intent(TaskActivity.this, SettingActivity.class));
                return true;
            case R.id.action_logout:
                session.logoutUser();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void add(final int estimation, final ArrayList<String> task) {
        final StringRequest stringRequest = new StringRequest(Request.Method.POST, VOTE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(TaskActivity.this, response, Toast.LENGTH_LONG).show();
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(TaskActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "addvote");
                params.put("estimation", String.valueOf(estimation));
                params.put("id_user", session.getUserDetails().get(SessionManager.KEY_ID));
                params.put("id_task", task.get(0));
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    public void delete(final ArrayList<String> task){
        final StringRequest stringRequest = new StringRequest(Request.Method.POST, DELETE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(TaskActivity.this, response, Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(TaskActivity.this, TasksListActivity.class);
                        intent.putExtra("idProjet", Integer.valueOf(task.get(3)));
                        startActivity(intent);
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(TaskActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "deltask");
                params.put("id_task", task.get(0));
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}
