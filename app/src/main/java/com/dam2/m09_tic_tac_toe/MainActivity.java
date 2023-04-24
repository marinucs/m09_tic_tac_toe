package com.dam2.m09_tic_tac_toe;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText ip;
    private TextInputEditText port;
    private Button connect;

    private Button start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ip = findViewById(R.id.ipEditTextInput);
        port = findViewById(R.id.portEditTextInput);
        connect = findViewById(R.id.connectBtn);

        connect.setOnClickListener(view -> {
            // if connection ok

        });

        start = findViewById(R.id.startBtn);


    }
}