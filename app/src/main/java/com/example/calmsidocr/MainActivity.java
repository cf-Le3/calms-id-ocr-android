package com.example.calmsidocr;

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

/** @noinspection ResultOfMethodCallIgnored*/
public class MainActivity extends AppCompatActivity {

    // View data
    private PreviewView previewCamera;
    private FloatingActionButton btnCapture;
    private Button btnRetry;
    private Button btnSubmit;
    private ImageView imagePhoto;
    private ViewGroup layoutResult;

    // CameraX
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ProcessCameraProvider cameraProvider;
    private CameraSelector cameraSelector;
    private Preview preview;
    private ImageCapture imageCapture;
    private Camera camera;

    // Non-view data
    private File fileCapture;
    private static final String TAG_CALLBACK = "CALLBACK";

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
        btnRetry = findViewById(R.id.btnRetry);
        btnSubmit = findViewById(R.id.btnSubmit);
        imagePhoto = findViewById(R.id.imagePhoto);
        layoutResult = findViewById(R.id.layoutResult);

        setupCamera();

        btnCapture.setOnClickListener(view -> {
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
                                Log.d(TAG_CALLBACK, "CALLBACK: takePicture onImageSaved");
                            }

                            @Override
                            public void onError(@NonNull ImageCaptureException exception) {
                                Log.w(TAG_CALLBACK, "CALLBACK: takePicture onError");
                            }
                        });
                Log.d(TAG_CALLBACK, "CALLBACK: btnCapture onClick");
            } catch (Exception e){
                Log.e(TAG_CALLBACK, "EXCEPTION: btnCapture onClick");
            }
        });

        btnRetry.setOnClickListener(view -> {
            Glide.with(view.getContext()).clear(imagePhoto);
            setViewVisibility(ViewState.BEFORE_CAPTURE);
            fileCapture.delete();
            Log.d(TAG_CALLBACK, "CALLBACK: btnRetry onClick");
        });

        btnSubmit.setOnClickListener(view -> {
            Toast.makeText(this, (CharSequence) "Coming soon...", Toast.LENGTH_SHORT).show();
            Log.d(TAG_CALLBACK, "CALLBACK: btnSubmit onClick");
        });

        Log.d(TAG_CALLBACK, "CALLBACK: MainActivity onCreate");
    }

    private void setupCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();
                imageCapture = new ImageCapture.Builder()
                        .setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation())
                        .build();
                preview = new Preview.Builder()
                        .build();
                preview.setSurfaceProvider(previewCamera.getSurfaceProvider());
                camera = cameraProvider.bindToLifecycle(
                        this, cameraSelector, imageCapture, preview);
                Log.d(TAG_CALLBACK, "CALLBACK: cameraProviderFuture listener");
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                Log.e(TAG_CALLBACK, "EXCEPTION: cameraProviderFuture listener");
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