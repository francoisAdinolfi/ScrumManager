package com.example.francois.scrummanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SchedulingActivity extends AppCompatActivity {
    private static final String SCHEDULING_URL = "http://scrummaster.pe.hu/scheduling.php";
    private static final String DEPENDENCE_URL = "http://scrummaster.pe.hu/dependence.php";
    private int idProjet;
    private ArrayList<ArrayList<String>> tasks = new ArrayList<>();
    private ArrayList<Integer> durations = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> dependencies = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> schedule = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheduling);

        SessionManager session = new SessionManager(getApplicationContext());
        session.checkLogin();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getIntent().getStringExtra("nameProjet"));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        idProjet = getIntent().getIntExtra("idProjet", 0);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, SCHEDULING_URL,
                response -> {
                    try {
                        JSONArray j = new JSONArray(response);

                        for (int i = 0; i < j.length(); i++) {
                            JSONObject JOStuff = j.getJSONObject(i);
                            ArrayList<String> taskTmp = new ArrayList<>();
                            int flag = 0;
                            for (ArrayList<String> al : tasks) {
                                if (JOStuff.getString("id_task").equals(al.get(0))) {
                                    flag = 1;
                                    taskTmp = al;
                                    break;
                                }
                            }
                            if (flag == 0) {
                                taskTmp.add(JOStuff.getString("id_task"));
                                taskTmp.add(JOStuff.getString("name"));
                                taskTmp.add(JOStuff.getString("estimation"));
                                tasks.add(taskTmp);
                            } else {
                                taskTmp.add(JOStuff.getString("estimation"));
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(SchedulingActivity.this, error.toString(), Toast.LENGTH_LONG).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "getstasksvotes");
                params.put("id_project", Integer.toString(idProjet));
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

        stringRequest = new StringRequest(Request.Method.POST, DEPENDENCE_URL,
                response -> {
                    try {
                        JSONArray j = new JSONArray(response);

                        for (int i = 0; i < j.length(); i++) {
                            JSONObject JOStuff = j.getJSONObject(i);
                            ArrayList<Integer> tmp = new ArrayList<>();
                            tmp.add(Integer.valueOf(JOStuff.getString("id_task")));
                            tmp.add(Integer.valueOf(JOStuff.getString("id_task_1")));
                            dependencies.add(tmp);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(SchedulingActivity.this, error.toString(), Toast.LENGTH_LONG).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "getdependencies");
                params.put("id_project", Integer.toString(idProjet));
                return params;
            }
        };
        requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

        Button btnStart = (Button) findViewById(R.id.btnStart);

        btnStart.setOnClickListener(v -> {
            ListView list = (ListView) findViewById(R.id.list);

            for (ArrayList<String> al : tasks) {
                ArrayList<Float> abc = getABC(al);
                double a = triangularLaw(abc.get(0), abc.get(1), abc.get(2));
                durations.add((int) a);
            }

            ArrayList<Integer> tasksId = new ArrayList<>();
            for (ArrayList<String> al : tasks) {
                tasksId.add(Integer.valueOf(al.get(0)));
            }

            solver(tasksId, durations, dependencies);

            ArrayList<String> scheduleName = new ArrayList<>();
            for(ArrayList<Integer> al : schedule){
                String taskName = "";
                for(ArrayList<String> task : tasks){
                    if(String.valueOf(al.get(0)).equals(task.get(0))){
                        taskName = task.get(1);
                    }
                }
                scheduleName.add(taskName + "     Start : " + al.get(1) + "     End : " + al.get(2));
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(SchedulingActivity.this, android.R.layout.simple_list_item_1, scheduleName);
            list.setAdapter(adapter);
        });
    }

    public boolean onSupportNavigateUp() {
        Intent intent = new Intent(SchedulingActivity.this, TasksListActivity.class);
        intent.putExtra("idProjet", idProjet);
        intent.putExtra("nameProjet", getIntent().getStringExtra("nameProjet"));
        startActivity(intent);
        finish();
        return true;
    }

    public ArrayList<Float> getABC(ArrayList<String> al) {
        ArrayList<Float> abc = new ArrayList<>();
        Float min = Float.MAX_VALUE;
        Float max = Float.MIN_VALUE;
        Float sum = 0f;
        for (int i = 2; i < al.size(); i++) {
            if (Float.parseFloat(al.get(i)) < min) min = Float.parseFloat(al.get(i));
            if (Float.parseFloat(al.get(i)) > max) max = Float.parseFloat(al.get(i));
            sum += Float.parseFloat(al.get(i));
        }
        abc.add(min);
        abc.add(max);
        abc.add(sum / (al.size() - 2));
        return abc;
    }

    public double triangularLaw(double a, double b, double c) {
        double F = (c - a) / (b - a);
        double rand = Math.random();
        if (rand < F) {
            return a + Math.sqrt(rand * (b - a) * (c - a));
        } else {
            return b - Math.sqrt((1 - rand) * (b - a) * (b - c));
        }
    }

    public void solver(ArrayList<Integer> tasks, ArrayList<Integer> durations, ArrayList<ArrayList<Integer>> dependencies){
        int num_task = tasks.size();

        Model model = new Model("Choco Solver");

        IntVar[] starts = model.intVarArray(num_task, 0, IntVar.MAX_INT_BOUND);
        IntVar[] ends = model.intVarArray(num_task, 0, IntVar.MAX_INT_BOUND);

        IntVar[] _durations = new IntVar[num_task];
        for(int i = 0; i < num_task; i++){
            _durations[i] = model.intVar(durations.get(i));
        }

        Task[] _tasks = new Task[num_task];
        for(int i = 0; i < num_task; i++){
            _tasks[i] = model.taskVar(starts[i], _durations[i], ends[i]);
        }

        for(ArrayList<Integer> d : dependencies){
            model.arithm(_tasks[tasks.indexOf(d.get(0))].getEnd(), "<=", _tasks[tasks.indexOf(d.get(1))].getStart()).post();
        }

        IntVar MaxEndTime = model.intVar(0, IntVar.MAX_INT_BOUND);
        model.max(MaxEndTime, ends).post();
        model.setObjective(Model.MINIMIZE, MaxEndTime);

        if(model.getSolver().solve()){
            for(int i = 0; i < num_task; i++){
                ArrayList<Integer> tmp = new ArrayList<>();
                tmp.add(tasks.get(i));
                tmp.add(_tasks[i].getStart().getValue());
                tmp.add(_tasks[i].getEnd().getValue());
                schedule.add(tmp);
            }
        }
    }
}