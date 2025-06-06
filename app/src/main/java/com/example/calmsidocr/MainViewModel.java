package com.example.calmsidocr;

import androidx.lifecycle.ViewModel;

import java.io.File;

public class MainViewModel extends ViewModel {
    private File fileCapture;

    public File getFileCapture() {
        return fileCapture;
    }

    public void setFileCapture(File fileCapture) {
        this.fileCapture = fileCapture;
    }
}