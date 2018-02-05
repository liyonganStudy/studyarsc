package com.example.hzliyongan.test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.parse_arsc.ParseResourceMain;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView textView = findViewById(R.id.text);
        try {
            InputStream inputStream = getAssets().open("resources.arsc");
            String content = ParseResourceMain.parseResourceFile(inputStream);

            Log.i("lya", content);

            textView.setText(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
