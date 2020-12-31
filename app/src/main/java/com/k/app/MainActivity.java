package com.k.app;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.kzq.library.widget.KProgressView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        final KProgressView pv = findViewById(R.id.pv);
//        pv.setProgress(1f, "2091");
    }

    public void onClick(View view) {
    }
}