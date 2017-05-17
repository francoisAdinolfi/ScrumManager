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
import android.widget.TextView;
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

public class SprintTaskListActivity extends AppCompatActivity {
    private static final String PROJECT_URL = "http://scrummaster.pe.hu/project.php";
    private static final String DELETE_URL = "http://scrummaster.pe.hu/delete.php";
    private static final String SCHEDULING_URL = "http://scrummaster.pe.hu/scheduling.php";
    private static final String DEPENDENCE_URL = "http://scrummaster.pe.hu/dependence.php";
    private SessionManager session;
    private ListView taskList;
    private int idProjet;
    private ArrayList<String> tasksName = new ArrayList<>();
    private ArrayList<ArrayList<String>> tasks = new ArrayList<>();
    // scheduling
    private boolean schedulingDone;
    private ArrayList<ArrayList<String>> votes = new ArrayList<>();
    private ArrayList<Integer> durations = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> dependencies = new ArrayList<>();
    private ArrayList<Integer> devsId = new ArrayList<>();
    private ArrayList<String> devsName = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> unavailabilities = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sprint_list);

        session = new SessionManager(getApplicationContext());
        session.checkLogin();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getIntent().getStringExtra("nameProjet"));
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
                            ArrayList<String> taskTmp = new ArrayList<>();
                            taskTmp.add(JOStuff.getString("id_task"));
                            taskTmp.add(JOStuff.getString("name"));
                            taskTmp.add(JOStuff.getString("description"));
                            taskTmp.add(JOStuff.getString("id_project"));
                            tasks.add(taskTmp);
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(SprintTaskListActivity.this, android.R.layout.simple_list_item_1, tasksName);
                        taskList.setAdapter(adapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(SprintTaskListActivity.this, error.toString(), Toast.LENGTH_LONG).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "gettasks");
                params.put("id_project", Integer.toString(idProjet));
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

        taskList.setOnItemClickListener((parent, view, position, id) -> {
            ArrayList<String> task = tasks.get(tasksName.indexOf(((TextView) view).getText()));
            Intent intent = new Intent(SprintTaskListActivity.this, TaskActivity.class);
            intent.putExtra("task", task);
            intent.putExtra("nameProjet", getIntent().getStringExtra("nameProjet"));
            startActivity(intent);
            finish();
        });

        // Scheduling
        stringRequest = new StringRequest(Request.Method.POST, SCHEDULING_URL,
                response -> {
                    try {
                        JSONObject j = new JSONObject(response);
                        schedulingDone = !j.getString("duration").equals("null");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(SprintTaskListActivity.this, error.toString(), Toast.LENGTH_LONG).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "getduration");
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
                            ArrayList<String> voteTmp = new ArrayList<>();
                            int flag = 0;
                            for (ArrayList<String> al : votes) {
                                if (JOStuff.getString("id_task").equals(al.get(0))) {
                                    flag = 1;
                                    voteTmp = al;
                                    break;
                                }
                            }
                            if (flag == 0) {
                                voteTmp.add(JOStuff.getString("id_task"));
                                voteTmp.add(JOStuff.getString("name"));
                                voteTmp.add(JOStuff.getString("estimation"));
                                votes.add(voteTmp);
                            } else {
                                voteTmp.add(JOStuff.getString("estimation"));
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(SprintTaskListActivity.this, error.toString(), Toast.LENGTH_LONG).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "gettasksvotes");
                params.put("id_project", Integer.toString(idProjet));
                return params;
            }
        };
        requestQueue = Volley.newRequestQueue(this);
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
                error -> Toast.makeText(SprintTaskListActivity.this, error.toString(), Toast.LENGTH_LONG).show()) {
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
                error -> Toast.makeText(SprintTaskListActivity.this, error.toString(), Toast.LENGTH_LONG).show()) {
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
                error -> Toast.makeText(SprintTaskListActivity.this, error.toString(), Toast.LENGTH_LONG).show()) {
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

        if(session.getUserDetails().get(SessionManager.KEY_ROLE).equals("scrummaster")) {
            final Button btnScheduling = (Button) findViewById(R.id.btnScheduling);
            btnScheduling.setVisibility(View.VISIBLE);

            btnScheduling.setOnClickListener(v -> {
                if(schedulingDone) {
                    Intent intent = new Intent(SprintTaskListActivity.this, SchedulingActivity.class);
                    intent.putExtra("idProjet", idProjet);
                    intent.putExtra("nameProjet", getIntent().getStringExtra("nameProjet"));
                    startActivity(intent);
                    finish();
                } else {
                    for (ArrayList<String> al : votes) {
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

                    if(tasksId.isEmpty()){
                        Toast.makeText(SprintTaskListActivity.this, "No task", Toast.LENGTH_LONG).show();
                    } else if (devsId.isEmpty()){
                        Toast.makeText(SprintTaskListActivity.this, "No developer", Toast.LENGTH_LONG).show();
                    } else{
                        Schedule schedule = solver(tasksId, durations, dependencies, devsId, unavailabilitiesDay);
                        if (schedule != null) {
                            addSchedule(schedule);
                        } else {
                            Toast.makeText(SprintTaskListActivity.this, "No Solution", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });

            final Button btnDelete = (Button) findViewById(R.id.btnDelete);
            btnDelete.setVisibility(View.VISIBLE);

            btnDelete.setOnClickListener(v -> delete(idProjet));
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent(SprintTaskListActivity.this, ProjectsListActivity.class);
        startActivity(intent);
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menuproject, menu);
        if(!session.getUserDetails().get(SessionManager.KEY_ROLE).equals("scrummaster")){
            menu.findItem(R.id.action_subdev).setVisible(false);
            menu.findItem(R.id.action_adddev).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_adddev:
                Intent intent = new Intent(SprintTaskListActivity.this, AddDevActivity.class);
                intent.putExtra("idProjet",idProjet);
                intent.putExtra("nameProjet", getIntent().getStringExtra("nameProjet"));
                startActivity(intent);
                finish();
                return true;
            case R.id.action_subdev:
                intent = new Intent(SprintTaskListActivity.this, SubDevActivity.class);
                intent.putExtra("idProjet",idProjet);
                intent.putExtra("nameProjet", getIntent().getStringExtra("nameProjet"));
                startActivity(intent);
                finish();
                return true;
            case R.id.action_setting:
                startActivity(new Intent(SprintTaskListActivity.this, SettingActivity.class));
                return true;
            case R.id.action_logout:
                session.logoutUser();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void delete(final int idProjet){
        final StringRequest stringRequest = new StringRequest(Request.Method.POST, DELETE_URL,
                response -> {
                    Toast.makeText(SprintTaskListActivity.this, response, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(SprintTaskListActivity.this, ProjectsListActivity.class);
                    startActivity(intent);
                    finish();
                },
                error -> Toast.makeText(SprintTaskListActivity.this, error.toString(), Toast.LENGTH_LONG).show()) {
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
                TaskSchedule task = new TaskSchedule(tasksId.get(i) ,tasks.get(i).get(1), _tasks[i].getStart().getValue(), _tasks[i].getEnd().getValue(), devsName.get(dev_id));
                schedule.addTask(task);
            }
        }
        return schedule;
    }

    public void addSchedule(Schedule schedule){
        final StringRequest stringRequest = new StringRequest(Request.Method.POST, SCHEDULING_URL,
                response -> {
                    Intent intent = new Intent(SprintTaskListActivity.this, SchedulingActivity.class);
                    intent.putExtra("idProjet", idProjet);
                    intent.putExtra("nameProjet", getIntent().getStringExtra("nameProjet"));
                    startActivity(intent);
                    finish();
                    Toast.makeText(SprintTaskListActivity.this, response, Toast.LENGTH_LONG).show();
                },
                error -> Toast.makeText(SprintTaskListActivity.this, error.toString(), Toast.LENGTH_LONG).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "addschedule");
                params.put("id_project", Integer.toString(idProjet));
                params.put("duration", String.valueOf(schedule.getDuration()));
                params.put("task_size", String.valueOf(schedule.getTasks().size()));
                for(int i = 0; i < schedule.getTasks().size(); i++){
                    params.put("id_task" + i, String.valueOf(schedule.getTasks().get(i).getIdTask()));
                    params.put("start" + i, String.valueOf(schedule.getTasks().get(i).getStart()));
                    params.put("end" + i, String.valueOf(schedule.getTasks().get(i).getEnd()));
                    params.put("developer" + i, String.valueOf(devsId.get(devsName.indexOf(schedule.getTasks().get(i).getDeveloper()))));
                }
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}