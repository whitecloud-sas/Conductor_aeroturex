package com.whitecloud.hm.whiteclouduser;

import android.content.Intent;
import android.os.Bundle;
import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.util.Log;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class Registro extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
    }

    public void signUp(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
