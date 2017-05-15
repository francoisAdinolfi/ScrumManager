package com.example.francois.scrummanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
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

public class RegisterActivity extends AppCompatActivity {
    private static final String USER_URL = "http://scrummaster.pe.hu/user.php";
    private EditText inputName;
    private EditText inputEmail;
    private EditText inputPassword;
    private RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        inputName = (EditText) findViewById(R.id.name);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        Button btnRegister = (Button) findViewById(R.id.btnRegister);
        TextView linkLogin = (TextView) findViewById(R.id.linkLogin);
        radioGroup = (RadioGroup)findViewById(R.id.radioGroup);

        btnRegister.setOnClickListener(v -> {
            String name = inputName.getText().toString().trim();
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();
            final int role = radioGroup.getCheckedRadioButtonId();
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Inputs must be filled", Toast.LENGTH_LONG).show();
            } else if (role == -1) {
                Toast.makeText(getApplicationContext(), "A role must be checked", Toast.LENGTH_LONG).show();
            } else {
                register(name, email, password, role);
            }
        });

        linkLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    public void register(final String name, final String email, final String password, final int role) {
        final StringRequest stringRequest = new StringRequest(Request.Method.POST, USER_URL,
                response -> {
                    try {
                        JSONObject jObj = new JSONObject(response);
                        if(jObj.getString("msg").equals("Successfully Registered")) {
                            Toast.makeText(RegisterActivity.this, jObj.getString("msg"), Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        else {
                            Toast.makeText(RegisterActivity.this, jObj.getString("msg"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(RegisterActivity.this, error.toString(), Toast.LENGTH_LONG).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "register");
                params.put("name", name);
                params.put("email", email);
                params.put("password", password);
                if(role  == R.id.radioScrumMaster){
                    params.put("role", "scrummaster");
                }
                else if(role  == R.id.radioDeveloper){
                    params.put("role", "developer");
                }
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}