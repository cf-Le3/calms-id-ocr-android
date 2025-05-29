package com.example.calmsidocr;

import android.content.Context;
import android.net.Uri;

import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ImageParser {
    TextRecognizer recognizer;
    FaceDetector detector;
    InputImage image;

    public ImageParser(Context context, File fileCapture) throws IOException {
        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .build();
        detector = FaceDetection.getClient(options);
        Uri uriCapture = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",
                fileCapture
        );
        image = InputImage.fromFilePath(context, uriCapture);
    }

    public Task<Text> parseImageText(){
        return recognizer.process(image);
    }

    public Task<List<Face>> extractFace(){
        return detector.process(image);
    }
}
