package com.example.gesture;


import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.video.Video;
import android.Manifest;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private CameraBridgeViewBase cameraBridgeViewBase;
    private Mat previousFrame;
    private Mat currentFrame;
    private MatOfPoint2f prevPts;
    private MatOfPoint2f nextPts;
    private MatOfByte status;
    private MatOfFloat err;
    private boolean isCameraPermissionGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraBridgeViewBase = findViewById(R.id.cameraPreview);
        cameraBridgeViewBase.setVisibility(CameraBridgeViewBase.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            isCameraPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        previousFrame = new Mat();
        currentFrame = new Mat();
        prevPts = new MatOfPoint2f();
        nextPts = new MatOfPoint2f();
        status = new MatOfByte();
        err = new MatOfFloat();
    }

    @Override
    public void onCameraViewStopped() {
        previousFrame.release();
        currentFrame.release();
        prevPts.release();
        nextPts.release();
        status.release();
        err.release();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isCameraPermissionGranted) {
            OpenCVLoader.initDebug();
            cameraBridgeViewBase.enableView();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isCameraPermissionGranted) {
            if (cameraBridgeViewBase != null) {
                cameraBridgeViewBase.disableView();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        currentFrame = inputFrame.gray();

        if (!previousFrame.empty()) {
            // Assuming you have already detected key points in the previous frame and stored them in prevPts
            Video.calcOpticalFlowPyrLK(previousFrame, currentFrame, prevPts, nextPts, status, err);

            // Perform motion detection and visualization here
            // For example, draw lines based on the optical flow
            for (int i = 0; i < nextPts.rows(); i++) {
                double[] flow = nextPts.get(i, 0);
                double flowX = flow[0];
                double flowY = flow[1];

                // Draw a line if motion vector is large enough
                if (Math.sqrt(flowX * flowX + flowY * flowY) > 5) {
                    // Adjust the drawing based on the detected flow
                    // For example, you can draw lines using Imgproc.line
                    // Make sure to adjust the coordinates according to the points provided by nextPts
                }
            }
        }

        previousFrame.release();
        previousFrame = currentFrame.clone();

        return currentFrame;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                isCameraPermissionGranted = true;
            } else {
                finish();
            }
        }
    }
}

