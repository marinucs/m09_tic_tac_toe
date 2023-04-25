package com.dam2.m09_tic_tac_toe;

import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView[][] table = new ImageView[3][3];

        table[0][0] = findViewById(R.id.imgsubrow01);
        table[0][1] = findViewById(R.id.imgsubrow02);
        table[0][2] = findViewById(R.id.imgsubrow03);

        table[1][0] = findViewById(R.id.imgsubrow11);
        table[1][1] = findViewById(R.id.imgsubrow12);
        table[1][2] = findViewById(R.id.imgsubrow13);

        table[2][0] = findViewById(R.id.imgsubrow21);
        table[2][1] = findViewById(R.id.imgsubrow22);
        table[2][2] = findViewById(R.id.imgsubrow23);

        clearTable(table);

        TextInputEditText ip = findViewById(R.id.ipEditTextInput);
        TextInputEditText port = findViewById(R.id.portEditTextInput);
        Button connect = findViewById(R.id.connectBtn);

        connect.setOnClickListener(view -> {
            // if connection ok

        });

        Button start = findViewById(R.id.startBtn);

        start.setOnClickListener(view -> {
            // if connection ok

        });

    }

    public static void clearTable(ImageView[][] table) {
        for (ImageView[] imageViews : table) {
            for (int col = 0; col < table.length; col++) {
                imageViews[col].setVisibility(View.GONE);
            }
        }
    }

}