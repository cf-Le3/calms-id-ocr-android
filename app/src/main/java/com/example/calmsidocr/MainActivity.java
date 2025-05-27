package com.example.calmsidocr;

import static com.example.calmsidocr.Constants.KEY_FILE;
import static com.example.calmsidocr.Constants.TAG_BUNDLE;
import static com.example.calmsidocr.Constants.TAG_EVENT;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    // View data
    private PreviewView previewCamera;
    private FloatingActionButton btnCapture;
    private ViewGroup layoutResult;

    // CameraX
    private ImageCapture imageCapture;
    private Camera camera;

    // Non-view data
    private File fileCapture;

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
        previewCamera = findViewById(R.id.previewCamera);
        btnCapture = findViewById(R.id.btnCapture);
        layoutResult = findViewById(R.id.layoutResult);
        Button btnRetry = findViewById(R.id.btnRetry);
        Button btnSubmit = findViewById(R.id.btnSubmit);
        ImageView imagePhoto = findViewById(R.id.imagePhoto);

        setupCamera();

        btnCapture.setOnClickListener(view -> {
            if (camera != null) {
                try {
                    fileCapture = getFilePath();
                    ImageCapture.OutputFileOptions outputFileOptions =
                            new ImageCapture.OutputFileOptions.Builder(fileCapture)
                                    .build();

                    imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this),
                            new ImageCapture.OnImageSavedCallback() {
                                @Override
                                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                                    Glide.with(view.getContext()).load(fileCapture).into(imagePhoto);
                                    setViewVisibility(ViewState.AFTER_CAPTURE);
                                    Log.d(TAG_EVENT, "CALLBACK: takePicture onImageSaved");
                                }
                                @Override
                                public void onError(@NonNull ImageCaptureException e) {
                                    Log.e(TAG_EVENT, "ERROR: takePicture onError\n" + e);
                                }
                            });

                    Log.d(TAG_EVENT, "CALLBACK: btnCapture onClick");
                } catch (Exception e){
                    Log.e(TAG_EVENT, "EXCEPTION: btnCapture onClick\n" + e);
                }
            } else {
                Toast.makeText(
                        this,
                        "ERROR: Camera unavailable.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        btnRetry.setOnClickListener(view -> {
            Glide.with(view.getContext()).clear(imagePhoto);
            setViewVisibility(ViewState.BEFORE_CAPTURE);
            Log.d(TAG_EVENT, "File deleted: " + fileCapture.delete());
            Log.d(TAG_EVENT, "CALLBACK: btnRetry onClick");
        });

        btnSubmit.setOnClickListener(view -> {
            Intent launchResultActivity = new Intent(view.getContext(), ResultActivity.class);
            Log.d(TAG_BUNDLE, "MainActivity: " + fileCapture.getAbsolutePath());
            launchResultActivity.putExtra(KEY_FILE, fileCapture.getAbsolutePath());
            view.getContext().startActivity(launchResultActivity);
        });

        Log.d(TAG_EVENT, "CALLBACK: MainActivity onCreate");
    }

    private void setupCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider
                .getInstance(this);

        cameraProviderFuture.addListener(() -> {
            CameraSelector cameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build();
            imageCapture = new ImageCapture.Builder()
                    .setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation())
                    .build();
            Preview preview = new Preview.Builder()
                    .build();
            preview.setSurfaceProvider(previewCamera.getSurfaceProvider());

            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                camera = cameraProvider.bindToLifecycle(
                        this, cameraSelector, imageCapture, preview);
                Log.d(TAG_EVENT, "CALLBACK cameraProviderFuture listener");
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG_EVENT, "EXCEPTION: cameraProviderFuture listener\n" + e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private File getFilePath() throws IOException {
        File targetDir = new File(getCacheDir(), "images");
        if (!targetDir.exists()) targetDir.mkdirs();
        return File.createTempFile("capture_", ".jpg", targetDir);
    }

    private void setViewVisibility(ViewState state) {
        btnCapture.setVisibility(state == ViewState.BEFORE_CAPTURE ? View.VISIBLE : View.INVISIBLE);
        layoutResult.setVisibility(state == ViewState.AFTER_CAPTURE ? View.VISIBLE : View.INVISIBLE);
    }
}