package com.example.francois.scrummanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class LoginActivity extends Activity {

    EditText email = null;
    EditText motDePasse = null;
    RadioGroup group = null;
    Button connexion = null;
    Button inscription = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = (EditText)findViewById(R.id.email);
        motDePasse = (EditText)findViewById(R.id.password);
        group = (RadioGroup)findViewById(R.id.radioGroup);
        connexion = (Button)findViewById(R.id.login);
        inscription = (Button)findViewById(R.id.subscribe);

        connexion.setOnClickListener(connexionListener);
        inscription.setOnClickListener(inscriptionListener);
    }

    private OnClickListener connexionListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            final String loginTxt = email.getText().toString();
            final String pswTxt = motDePasse.getText().toString();
            final int radioGroup = group.getCheckedRadioButtonId();
            if (loginTxt.equals("") || pswTxt.equals("")) {
                Toast.makeText(LoginActivity.this, "L'adresse email ET le mot de passe sont requis", Toast.LENGTH_SHORT).show();
                return;
            }

            if(!loginTxt.contains("@")){
                email.setError("Cette adresse mail n'est pas valide");
                return;
            }

            if(radioGroup == -1){
                Toast.makeText(LoginActivity.this, "Choisissez entre Scrum Master et Developpeur", Toast.LENGTH_SHORT).show();
                return;
            }

            if(radioGroup == R.id.scrumMaster) {
                Toast.makeText(LoginActivity.this, "Scrum Master", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(LoginActivity.this, "Developpeur", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private OnClickListener inscriptionListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        }
    };
}
