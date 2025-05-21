package com.example.calmsidocr;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.impl.utils.Exif;
import androidx.exifinterface.media.ExifInterface;

import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    // View data
    private Button btnCamera;
    private Button btnClear;
    private Button btnParse;
    private ImageView imgCapture;
    private LinearLayout linearResults;
    private TextView txtResult;
    private ActivityResultLauncher<Uri> launcherCamera;

    // Non-view data
    private File fileCapture;
    private Uri uriCapture;
    private ViewState viewState;

    // Instance state bundle keys
    private final String KEY_FILE = "KEY_FILE";
    private final String KEY_URI = "KEY_URI";
    private final String KEY_STATE = "KEY_STATE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Standard activity setup.
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Bind View components.
        btnCamera = findViewById(R.id.btnCamera);
        btnClear = findViewById(R.id.btnClear);
        btnParse = findViewById(R.id.btnParse);
        imgCapture = findViewById(R.id.imageCapture);
        linearResults = findViewById(R.id.linearResults);
        txtResult = findViewById(R.id.txtResultTest);

        launcherCamera = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                isSuccess -> {
                    if (isSuccess) {
                        Glide.with(this).load(uriCapture).into(imgCapture);
                        viewState = setViewVisibility(ViewState.CAPTURED);
                    }
                }
        );

        btnCamera.setOnClickListener(view -> {
            try {
                // Define path for new temporary file.
                fileCapture = getFilePath();

                // Derive URI from given path.
                uriCapture = FileProvider.getUriForFile(
                        this,
                        getPackageName() + ".fileprovider",
                        fileCapture
                );

                launcherCamera.launch(uriCapture);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        btnClear.setOnClickListener(view -> {
            Glide.with(view.getContext()).clear(imgCapture);
            viewState = setViewVisibility(ViewState.CLEARED);
            System.out.println(fileCapture.delete() ? "File deleted." : "File not deleted.");
        });

        btnParse.setOnClickListener(view -> {
            if (viewState != ViewState.PARSED) {
                try {
                    ExifInterface exif = new ExifInterface(fileCapture);
                    txtResult.setText(String.valueOf(exif.getRotationDegrees()));
                    viewState = setViewVisibility(ViewState.PARSED);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (fileCapture != null) outState.putString(KEY_FILE, fileCapture.getAbsolutePath());
        if (uriCapture != null) outState.putString(KEY_URI, uriCapture.toString());
        if (viewState != null) outState.putString(KEY_STATE, viewState.name());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        String strFile = savedInstanceState.getString(KEY_FILE);
        String strUri = savedInstanceState.getString(KEY_URI);
        String strState = savedInstanceState.getString(KEY_STATE);
        if (strFile != null) fileCapture = new File(strFile);
        if (strUri != null) {
            uriCapture = Uri.parse(strUri);
            Glide.with(this).load(uriCapture).into(imgCapture);
        }
        if (strState != null) viewState = setViewVisibility(ViewState.valueOf(strState));
        super.onRestoreInstanceState(savedInstanceState);
    }

    private File getFilePath() throws IOException {
        File targetDir = new File(getCacheDir(), "images");
        if (!targetDir.exists()) targetDir.mkdirs();
        return File.createTempFile("capture_", ".jpg", targetDir);
    }

    private ViewState setViewVisibility(ViewState state) {
        btnCamera.setVisibility(state == ViewState.CLEARED ? View.VISIBLE : View.INVISIBLE);
        imgCapture.setVisibility(state != ViewState.CLEARED ? View.VISIBLE : View.INVISIBLE);
        linearResults.setVisibility(state == ViewState.PARSED ? View.VISIBLE : View.INVISIBLE);
        return state;
    }


}