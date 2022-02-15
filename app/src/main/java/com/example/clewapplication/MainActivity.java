package com.example.clewapplication;
//reformat code Ctrl + Alt + L

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.navigation.NavigationView;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home);

        FragmentManager fragmentManager = getSupportFragmentManager();
        HomeFragment fragment = new HomeFragment();
        fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        Fragment fragment = null;
        FragmentManager fragmentManager = getSupportFragmentManager();

        if (id == R.id.nav_home) {
            fragment = new HomeFragment();
        } else if (id == R.id.nav_settings) {
            fragment = new SettingsActivity();
        } else if (id == R.id.nav_tutorial) {
            fragment = new TutorialActivity();
        } else if (id == R.id.nav_ga_shortcuts) {
            fragment = new GAShortcutsActivity();
        } else if (id == R.id.nav_contact_us) {
            fragment = new ContactUsActivity();
        }
        fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void buttonClick(@NonNull View v) {
        switch(v.getId()) {
            case R.id.button100:
                Intent myIntent = new Intent();
                myIntent.setClassName("com.example.clewapplication", "com.example.clewapplication.SingleUseRouteActivity");
                startActivity(myIntent);
                break;
            case R.id.button200:
                Intent myIntent1 = new Intent();
                myIntent1.setClassName("com.example.clewapplication", "com.example.clewapplication.SaveRouteActivity");
                startActivity(myIntent1);
                break;
            case R.id.button300:
                Intent myIntent2 = new Intent();
                myIntent2.setClassName("com.example.clewapplication", "com.example.clewapplication.SavedRoutesListActivity");
                startActivity(myIntent2);
                break;
        }
    }
}