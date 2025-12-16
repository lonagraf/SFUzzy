package com.example.sfuzzy;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container_view_tag, new LoginFragment())
                .addToBackStack("fragment")
                .commit();
    }
}