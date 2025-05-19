package com.example.calmsidocr;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    // UI elements
    private Button btnCamera;
    private Button btnClear;
    private Button btnSubmit;
    private ImageView viewCapture;
    private ScrollView viewResult;

    // File management
    private File fileCapture;
    private Uri uriCapture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnCamera = findViewById(R.id.btnCamera);
        btnClear = findViewById(R.id.btnClear);
        btnSubmit = findViewById(R.id.btnSubmit);
        viewCapture = findViewById(R.id.imageCapture);
        viewResult = findViewById(R.id.scrollResults);
    }

    private File createImageFile() throws IOException {
        File imgDir = new File(getCacheDir(), "images");
        if (!imgDir.exists()) imgDir.mkdirs();
        return File.createTempFile("capture_", ".jpg", imgDir);
    }
}