package com.worldsnas.volleyhelpe;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.gson.reflect.TypeToken;
import com.worldsnas.volleyhelper.VolleyHelper;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new VolleyHelper<List<String>>(new TypeToken<List<String>>(){});
    }
}
