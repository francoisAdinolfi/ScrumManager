package com.example.francois.scrummanager;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private Button btnLogout;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        session = new SessionManager(getApplicationContext());

        session.checkLogin();

        TextView name = (TextView) findViewById(R.id.name);
        TextView email = (TextView) findViewById(R.id.email);
        TextView role = (TextView) findViewById(R.id.role);
        btnLogout = (Button) findViewById(R.id.btnLogout);

        HashMap<String, String> user = session.getUserDetails();

        name.setText("Name: " + user.get(SessionManager.KEY_NAME));
        email.setText("Email: " + user.get(SessionManager.KEY_EMAIL));
        role.setText("Role: " + user.get(SessionManager.KEY_ROLE));

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                session.logoutUser();
            }
        });
    }
}
