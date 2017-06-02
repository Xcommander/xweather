package com.example.xulinchao.xweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.xulinchao.xweather.gson.Weather;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
        if(sharedPreferences.getString("weather",null)!=null){
            Intent intent=new Intent(this, WeatherActivity.class);
            startActivity(intent);
            finish();

        }

    }
}
