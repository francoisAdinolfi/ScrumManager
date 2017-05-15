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
import org.chocosolver.util.tools.ArrayUtils;
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
    private ArrayList<Integer> devsId = new ArrayList<>();
    private ArrayList<String> devsName = new ArrayList<>();
    private ArrayList<Integer> disponibilities = new ArrayList<>();
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

        stringRequest = new StringRequest(Request.Method.POST, SCHEDULING_URL,
                response -> {
                    try {
                        JSONArray j = new JSONArray(response);

                        for (int i = 0; i < j.length(); i++) {
                            JSONObject JOStuff = j.getJSONObject(i);
                            devsId.add(Integer.valueOf(JOStuff.getString("id_user")));
                            devsName.add(JOStuff.getString("name"));
                            disponibilities.add(Integer.valueOf(JOStuff.getString("disponibility")));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(SchedulingActivity.this, error.toString(), Toast.LENGTH_LONG).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "getdevelopers");
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

            solver(tasksId, durations, dependencies, devsId, disponibilities);

            ArrayList<String> scheduleName = new ArrayList<>();
            for(ArrayList<Integer> al : schedule){
                String taskName = "";
                for(ArrayList<String> task : tasks){
                    if(String.valueOf(al.get(0)).equals(task.get(0))){
                        taskName = task.get(1);
                    }
                }
                scheduleName.add(taskName + "     Start : " + al.get(1) + "     End : " + al.get(2) + "     Dev : " + devsName.get(devsId.indexOf(al.get(3))));
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

    public void solver(ArrayList<Integer> tasks, ArrayList<Integer> durations, ArrayList<ArrayList<Integer>> dependencies, ArrayList<Integer> devs, ArrayList<Integer> dispos){
        int num_task = tasks.size();
        int num_dev = devs.size();

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

        // Gestion des dépendances
        for(ArrayList<Integer> d : dependencies){
            model.arithm(_tasks[tasks.indexOf(d.get(0))].getEnd(), "<=", _tasks[tasks.indexOf(d.get(1))].getStart()).post();
        }

        IntVar[][] assign = new IntVar[num_dev][num_task];
        for(int i = 0; i < num_dev; i++){
            assign[i] = model.intVarArray(num_task, 0, 1);
        }

        // Un seul dev par tache
        for(int i = 0; i < num_task; i++){
            model.sum(ArrayUtils.getColumn(assign, i), "=", 1).post();
        }

        IntVar[][] d = new IntVar[num_dev][num_task];
        for(int i = 0; i < num_dev; i++){
            for(int j = 0; j < num_task; j++){
                d[i][j] = model.intVar(durations.get(j));
            }
        }

        IntVar[][] dfa = new IntVar[num_dev][num_task];
        for(int i = 0; i < num_dev; i++){
            dfa[i] = model.intVarArray(num_task, 0, IntVar.MAX_INT_BOUND);
            for(int j = 0; j < num_task; j++){
                model.arithm(d[i][j], "*", assign[i][j], "=", dfa[i][j]).post();
            }
        }

        // Gestion des dispos
        for(int i = 0; i < num_dev; i++){
            model.sum(dfa[i], "<=", dispos.get(i)).post();
        }

        IntVar[][] s = new IntVar[num_dev][num_task];
        for(int i = 0; i < num_dev; i++){
            s[i] = model.intVarArray(num_task, 0, IntVar.MAX_INT_BOUND);
            for(int j = 0; j < num_task; j++){
                model.arithm(s[i][j], "=", _tasks[j].getStart()).post();
            }
        }

        IntVar[][] sfa = new IntVar[num_dev][num_task];
        for(int i = 0; i < num_dev; i++){
            sfa[i] = model.intVarArray(num_task, 0, IntVar.MAX_INT_BOUND);
            for(int j = 0; j < num_task; j++){
                model.arithm(s[i][j], "*", assign[i][j], "=", sfa[i][j]).post();
            }
        }

        IntVar[][] e = new IntVar[num_dev][num_task];
        for(int i = 0; i < num_dev; i++){
            e[i] = model.intVarArray(num_task, 0, IntVar.MAX_INT_BOUND);
            for(int j = 0; j < num_task; j++){
                model.arithm(e[i][j], "=", _tasks[j].getEnd()).post();
            }
        }

        IntVar[][] efa = new IntVar[num_dev][num_task];
        for(int i = 0; i < num_dev; i++){
            efa[i] = model.intVarArray(num_task, 0, IntVar.MAX_INT_BOUND);
            for(int j = 0; j < num_task; j++){
                model.arithm(e[i][j], "*", assign[i][j], "=", efa[i][j]).post();
            }
        }

        // Deux taches effectuées par le même dev ne s'effectuent pas en même temps
        for(int i = 0; i < num_dev; i++){
            for(int j = 0; j < num_task; j++){
                for(int k = 0; k < num_task; k++){
                    if(k != j){
                        model.or(model.arithm(sfa[i][j], ">=", efa[i][k]), model.arithm(efa[i][j], "<=", sfa[i][k])).post();
                    }
                }
            }
        }

        IntVar MaxEndTime = model.intVar(0, IntVar.MAX_INT_BOUND);
        model.max(MaxEndTime, ends).post();
        model.setObjective(Model.MINIMIZE, MaxEndTime);

        if(model.getSolver().solve()){
            for(int i = 0; i < num_task; i++){
                int dev_id = 0;
                for(int j = 0; j < num_dev; j++){
                    if(assign[j][i].getValue() == 1){
                        dev_id = devs.get(j);
                    }
                }
                ArrayList<Integer> tmp = new ArrayList<>();
                tmp.add(tasks.get(i));
                tmp.add(_tasks[i].getStart().getValue());
                tmp.add(_tasks[i].getEnd().getValue());
                tmp.add(dev_id);
                schedule.add(tmp);
            }
            System.out.println();
            for(int i = 0; i < num_dev; i++){
                for(int j = 0; j < num_task; j++){
                    System.out.print(assign[i][j].getValue());
                }
                System.out.println();
            }
        } else {
            Toast.makeText(SchedulingActivity.this, "No solution", Toast.LENGTH_LONG).show();
        }
    }
}