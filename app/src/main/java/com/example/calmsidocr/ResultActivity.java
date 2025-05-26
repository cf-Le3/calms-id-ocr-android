package com.example.calmsidocr;

import static com.example.calmsidocr.Constants.KEY_FILE;
import static com.example.calmsidocr.Constants.TAG_BUNDLE;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;

public class ResultActivity extends AppCompatActivity {

    private TextView txtResult;
    private TextView txtNRIC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Standard activity setup.
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_result);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Bind view data.
        txtResult = findViewById(R.id.txtResultContent);
        txtNRIC = findViewById(R.id.txtNRICContent);

        String incoming = getIntent().getStringExtra(KEY_FILE);
        Log.d(TAG_BUNDLE, "ResultActivity: " + (incoming != null ? incoming : "null"));
    }
}