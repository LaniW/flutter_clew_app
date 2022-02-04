package com.example.clewapplication;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Button;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

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

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_view);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}