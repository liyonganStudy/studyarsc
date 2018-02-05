package com.example.hzliyongan.test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.parse_arsc.ParseResourceMain;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.text);
        String content = "";
        try {
            InputStream inputStream = getAssets().open("resources.arsc");
            content = ParseResourceMain.parseResourceFile(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        textView.setText(content);

        final String finalContent = content;
        findViewById(R.id.simpleInfo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText(finalContent);
            }
        });


        findViewById(R.id.showString).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText(ParseResourceMain.showStringPoolStrings());
            }
        });
    }
}
