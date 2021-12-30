package com.example.clewapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.button100);
        button.setOnClickListener(view -> {
            Intent i = new Intent(view.getContext(), SingleUseRouteActivity.class);
            startActivity(i);
        });

        Button button1 = findViewById(R.id.button200);
        button1.setOnClickListener(view -> {
            Intent ii = new Intent(view.getContext(), SaveRouteActivity.class);
            startActivity(ii);
        });

        Button button2 = findViewById(R.id.button300);
        button2.setOnClickListener(view -> {
            Intent iii = new Intent(view.getContext(), SavedRoutesListActivity.class);
            startActivity(iii);
        });
    }
}