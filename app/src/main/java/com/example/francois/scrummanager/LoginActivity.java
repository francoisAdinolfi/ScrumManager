package com.example.francois.scrummanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends Activity {
    private static final String USER_URL = "http://scrummaster.pe.hu/user.php";
    private EditText inputEmail;
    private EditText inputPassword;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        session = new SessionManager(getApplicationContext());
        if(session.isLoggedIn()){
            Intent intent = new Intent(LoginActivity.this, ProjectsListActivity.class);
            startActivity(intent);
            finish();
        }

        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        Button btnLogin = (Button) findViewById(R.id.btnLogin);
        TextView linkRegister = (TextView) findViewById(R.id.linkRegister);

        btnLogin.setOnClickListener(v -> {
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Inputs must be filled", Toast.LENGTH_LONG).show();
            } else {
                login(email, password);
            }
        });

        linkRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });
    }

    public void login(final String email, final String password) {
        final StringRequest stringRequest = new StringRequest(Request.Method.POST, USER_URL,
                response -> {
                    try {
                        JSONObject jObj = new JSONObject(response);
                        if(jObj.getString("msg").equals("Successfully Login")) {
                            Toast.makeText(LoginActivity.this, jObj.getString("msg"), Toast.LENGTH_LONG).show();
                            session.createLoginSession(jObj.getString("id_user"), jObj.getString("name"), jObj.getString("email"), jObj.getString("role"));
                            Intent intent = new Intent(LoginActivity.this, ProjectsListActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        else {
                            Toast.makeText(LoginActivity.this, jObj.getString("msg"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(LoginActivity.this, error.toString(), Toast.LENGTH_LONG).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "login");
                params.put("email", email);
                params.put("password", password);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}