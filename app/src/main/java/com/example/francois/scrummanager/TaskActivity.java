package com.example.francois.scrummanager;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

@SuppressWarnings("ConstantConditions")
public class TaskActivity extends AppCompatActivity {
    private TextView voteText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        SessionManager session = new SessionManager(getApplicationContext());
        session.checkLogin();

        ArrayList<String> task = (ArrayList<String>) getIntent().getSerializableExtra("task");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(task.get(1));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        TextView description = (TextView) findViewById(R.id.description);
        description.setText(task.get(2));

        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setMax(30);
        voteText = (TextView) findViewById(R.id.voteText);
        voteText.setText(seekBar.getProgress() + " days");

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
                voteText.setText(seekBar.getProgress() + " days");
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        super.onBackPressed();
        return true;
    }
}
