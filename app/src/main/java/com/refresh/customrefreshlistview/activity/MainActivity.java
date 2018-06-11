package com.refresh.customrefreshlistview.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.refresh.customrefreshlistview.R;
import com.refresh.customrefreshlistview.utils.StatusBarUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StatusBarUtil.setActivityTranslucent(this);
        TextView basetext = (TextView) findViewById(R.id.basetext);
        TextView atext = (TextView) findViewById(R.id.atext);
        TextView btext = (TextView) findViewById(R.id.btext);

        basetext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, BaseListActivity.class));
            }
        });
        atext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AListActivity.class));
            }
        });
        btext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, BListActivity.class));
            }
        });
    }
}