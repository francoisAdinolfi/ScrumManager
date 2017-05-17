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
    private ArrayList<ArrayList<Integer>> unavailabilities = new ArrayList<>();

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

        stringRequest = new StringRequest(Request.Method.POST, SCHEDULING_URL,
                response -> {
                    try {
                        JSONArray j = new JSONArray(response);
                        for (int i = 0; i < j.length(); i++) {
                            JSONObject JOStuff = j.getJSONObject(i);
                            ArrayList<Integer> tmp = new ArrayList<>();
                            int flag = 0;
                            for (ArrayList<Integer> al : unavailabilities) {
                                if (JOStuff.getInt("id_user") == al.get(0)) {
                                    flag = 1;
                                    tmp = al;
                                    break;
                                }
                            }
                            if (flag == 0) {
                                tmp.add(JOStuff.getInt("id_user"));
                                tmp.add(JOStuff.getInt("day"));
                                unavailabilities.add(tmp);
                            } else {
                                tmp.add(JOStuff.getInt("day"));
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
                params.put("tag", "getunavailabilities");
                params.put("id_project", Integer.toString(idProjet));
                return params;
            }
        };
        requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

        Button btnStart = (Button) findViewById(R.id.btnStart);

        btnStart.setOnClickListener(v -> {
            for (ArrayList<String> al : tasks) {
                ArrayList<Float> abc = getABC(al);
                double a = triangularLaw(abc.get(0), abc.get(1), abc.get(2));
                durations.add((int) a);
            }

            ArrayList<Integer> tasksId = new ArrayList<>();
            for (ArrayList<String> al : tasks) {
                tasksId.add(Integer.valueOf(al.get(0)));
            }

            ArrayList<ArrayList<Integer>> unavailabilitiesDay = new ArrayList<>();
            for (Integer i : devsId) {
                ArrayList<Integer> tmp = new ArrayList<>();
                for(ArrayList<Integer> al : unavailabilities){
                    if(i.equals(al.get(0))){
                        for(int j = 1; j < al.size(); j++){
                            tmp.add(al.get(j));
                        }
                    }
                }
                unavailabilitiesDay.add(tmp);
            }

            Schedule schedule = solver(tasksId, durations, dependencies, devsId, unavailabilitiesDay);

            if(schedule != null) {
                ListView list = (ListView) findViewById(R.id.list);
                ArrayList<TaskSchedule> tasks = schedule.getTasks();
                ArrayList<String> tasksName = new ArrayList<>();
                tasksName.add("Duration : " + schedule.getDuration());
                for (TaskSchedule task : tasks) {
                    tasksName.add(task.toString());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(SchedulingActivity.this, android.R.layout.simple_list_item_1, tasksName);
                list.setAdapter(adapter);
            }
            else {
                Toast.makeText(SchedulingActivity.this, "No Solution", Toast.LENGTH_LONG).show();
            }
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

    public Schedule solver(ArrayList<Integer> tasksId, ArrayList<Integer> durations, ArrayList<ArrayList<Integer>> dependencies, ArrayList<Integer> devs, ArrayList<ArrayList<Integer>> unavailabilities){
        Schedule schedule = null;

        int num_task = tasksId.size();
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

        // Gestion des dépendances (fin[t1] < debut[t2])
        for(ArrayList<Integer> d : dependencies){
            _tasks[tasksId.indexOf(d.get(0))].getEnd().le(_tasks[tasksId.indexOf(d.get(1))].getStart()).post();
        }

        // Matrice d'assignation des taches
        IntVar[][] assign = new IntVar[num_dev][num_task];
        for(int i = 0; i < num_dev; i++){
            assign[i] = model.intVarArray(num_task, 0, 1);
        }

        // Un seul dev par tache
        for(int i = 0; i < num_task; i++){
            model.sum(ArrayUtils.getColumn(assign, i), "=", 1).post();
        }

        // start * assign
        IntVar[][] sfa = new IntVar[num_dev][num_task];
        for(int i = 0; i < num_dev; i++){
            sfa[i] = model.intVarArray(num_task, 0, IntVar.MAX_INT_BOUND);
            for(int j = 0; j < num_task; j++){
                sfa[i][j].eq(_tasks[j].getStart().mul(assign[i][j])).post();
            }
        }

        // end * assign
        IntVar[][] efa = new IntVar[num_dev][num_task];
        for(int i = 0; i < num_dev; i++){
            efa[i] = model.intVarArray(num_task, 0, IntVar.MAX_INT_BOUND);
            for(int j = 0; j < num_task; j++){
                efa[i][j].eq(_tasks[j].getEnd().mul(assign[i][j])).post();
            }
        }

        // start * assign % 14
        IntVar[][] ms = new IntVar[num_dev][num_task];
        for(int i = 0; i < num_dev; i++){
            ms[i] = model.intVarArray(num_task, 0, IntVar.MAX_INT_BOUND);
            for(int j = 0; j < num_task; j++){
                ms[i][j].eq(sfa[i][j].mod(model.intVar(14))).post();
            }
        }

        // end * assign % 14
        IntVar[][] me = new IntVar[num_dev][num_task];
        for(int i = 0; i < num_dev; i++){
            me[i] = model.intVarArray(num_task, 0, IntVar.MAX_INT_BOUND);
            for(int j = 0; j < num_task; j++){
                me[i][j].eq(efa[i][j].mod(model.intVar(14))).post();
            }
        }

        // Gestion des dispos
        for(int i = 0; i < num_dev; i++){
            for(int j = 0; j < num_task; j++){
                for(int k = 0; k < unavailabilities.get(i).size(); k++){
                    // start[j] * assign[i][j] % 14 < indispos.get(i).get(k)
                    ms[i][j].lt(unavailabilities.get(i).get(k)).and(
                            // AND  end[j] * assign[i][j] % 14 <= indispos.get(i).get(k)
                            me[i][j].le(unavailabilities.get(i).get(k))).or(
                            // OR start[j] * assign[i][j] % 14 > indispos.get(i).get(k)
                            ms[i][j].gt(unavailabilities.get(i).get(k)).and(
                                    // AND end[j] * assign[i][j] % 14 >= indispos.get(i).get(k)
                                    me[i][j].ge(unavailabilities.get(i).get(k)))).post();
                }
            }
        }

        // Deux taches effectuées par le même dev ne s'effectuent pas en même temps
        for(int i = 0; i < num_dev; i++){
            for(int j = 0; j < num_task; j++){
                for(int k = 0; k < num_task; k++){
                    if(k != j){
                        sfa[i][j].ge(efa[i][k]).or(efa[i][j].le(sfa[i][k])).post();
                    }
                }
            }
        }

        IntVar MaxEndTime = model.intVar(0, IntVar.MAX_INT_BOUND);
        model.max(MaxEndTime, ends).post();
        model.setObjective(Model.MINIMIZE, MaxEndTime);

        while(model.getSolver().solve()) {
            schedule = new Schedule(MaxEndTime.getValue());

            for (int i = 0; i < num_task; i++) {
                int dev_id = 0;
                for (int j = 0; j < num_dev; j++) {
                    if (assign[j][i].getValue() == 1) {
                        dev_id = j;
                    }
                }
                TaskSchedule task = new TaskSchedule(tasks.get(i).get(1), _tasks[i].getStart().getValue(), _tasks[i].getEnd().getValue(), devsName.get(dev_id));
                schedule.addTask(task);
            }
        }
        return schedule;
    }
}