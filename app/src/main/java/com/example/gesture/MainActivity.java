package com.example.gesture;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.video.Video;

import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends CameraActivity {

    private JavaCameraView javaCameraView;
    private CascadeClassifier faceDetector;
    private File cascadeFile;
    Button select,camera;
    ImageView imageView;
    Bitmap bitmap;
    int SELECT_CODE=100,CAMERA_CODE=101;
    Mat mat;
    CameraBridgeViewBase cameraBridgeViewBase;
    Mat curr_gray,prev_gray,rgb,diff;
    List<MatOfPoint> cnts;
    boolean is_init;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //select=findViewById(R.id.select);
        //camera=findViewById(R.id.camera);
        //imageView=findViewById(R.id.imageView);
        if (OpenCVLoader.initDebug()){
            Log.d("Loaded","openCv");
        }
        else {
            Log.d("Loaded","error");
        }
        getPermission();
        is_init=false;
        cameraBridgeViewBase=findViewById(R.id.cameraView);
        cameraBridgeViewBase.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
            @Override
            public void onCameraViewStarted(int width, int height) {
                curr_gray=new Mat();
                prev_gray=new Mat();
                rgb=new Mat();
                diff=new Mat();
                cnts=new ArrayList<>();
            }

            @Override
            public void onCameraViewStopped() {

            }

            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

                if (!is_init){
                    prev_gray=inputFrame.gray();
                    is_init=true;
                    return prev_gray;
                }
                rgb=inputFrame.rgba();
                curr_gray=inputFrame.gray();

                Core.absdiff(curr_gray,prev_gray,diff);
                Imgproc.threshold(diff,diff,40,255,Imgproc.THRESH_BINARY);
                Imgproc.findContours(diff,cnts,new Mat(),Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE);



                Imgproc.drawContours(rgb,cnts,-1,new Scalar(255,0,0),4);

                for (MatOfPoint m:cnts){
                    Rect r=Imgproc.boundingRect(m);
                    Imgproc.rectangle(rgb,r,new Scalar(0,0,255),3);
                }
                cnts.clear();
                prev_gray=curr_gray.clone();
                return rgb;
            }
        });
        if (OpenCVLoader.initLocal()){
            cameraBridgeViewBase.enableView();
        }

//        select.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
//                intent.setType("image/*");
//                startActivityForResult(intent,SELECT_CODE);
//
//            }
//        });
//        camera.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                startActivityForResult(intent,CAMERA_CODE);
//            }
//        });





    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraBridgeViewBase.disableView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraBridgeViewBase.enableView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraBridgeViewBase.disableView();
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(cameraBridgeViewBase);
    }

    void  getPermission(){
        if (checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.CAMERA},102);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==102&&grantResults.length>0){
            getPermission();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==SELECT_CODE&&data!=null){
            try {
                bitmap= MediaStore.Images.Media.getBitmap(this.getContentResolver(),data.getData());
                imageView.setImageBitmap(bitmap);
                mat=new Mat();
                Utils.bitmapToMat(bitmap,mat);

                Imgproc.cvtColor(mat,mat,Imgproc.COLOR_RGB2GRAY);
                Utils.matToBitmap(mat,bitmap);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (requestCode==CAMERA_CODE&&data!=null){
            bitmap=(Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(bitmap);
            mat=new Mat();
            Utils.bitmapToMat(bitmap,mat);
            Imgproc.cvtColor(mat,mat,Imgproc.COLOR_RGB2GRAY);
            Utils.matToBitmap(mat,bitmap);
            imageView.setImageBitmap(bitmap);
        }
    }
    //    private void initializeOpenCV() {
//        if (!OpenCVLoader.initDebug()) {
//            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, baseCallback);
//        } else {
//            try {
//                baseCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == 1) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                initializeOpenCV();
//            } else {
//                finish();
//            }
//        }
//    }
//
//    @Override
//    public void onCameraViewStarted(int width, int height) {
//        cascadeFile = new File(getDir("cascade", Context.MODE_PRIVATE), "haarcascade_frontalface_alt2.xml");
//        try {
//            InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_alt2);
//            FileOutputStream fos = new FileOutputStream(cascadeFile);
//
//            byte[] buffer = new byte[4096];
//            int bytesRead;
//
//            while ((bytesRead = is.read(buffer)) != -1) {
//                fos.write(buffer, 0, bytesRead);
//            }
//            is.close();
//            fos.close();
//            faceDetector = new CascadeClassifier(cascadeFile.getAbsolutePath());
//            if (faceDetector.empty()) {
//                faceDetector = null;
//            } else {
//                cascadeFile.delete();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void onCameraViewStopped() {
//    }
//
//    @Override
//    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
//        Mat rgba = inputFrame.rgba();
//        Mat grey = inputFrame.gray();
//
//        MatOfRect faceDetections = new MatOfRect();
//        faceDetector.detectMultiScale(rgba, faceDetections);
//
//        for (Rect rect : faceDetections.toArray()) {
//            Imgproc.rectangle(rgba, new Point(rect.x, rect.y),
//                    new Point(rect.x + rect.width, rect.y + rect.height),
//                    new Scalar(255, 0, 0), 3);
//        }
//
//        return rgba;
//    }
//
//    private final BaseLoaderCallback baseCallback = new BaseLoaderCallback(this) {
//        @Override
//        public void onManagerConnected(int status) {
//            switch (status) {
//                case LoaderCallbackInterface.SUCCESS:
//                    Log.i("OpenCV", "OpenCV loaded successfully");
//                    javaCameraView.enableView();
//                    break;
//                default:
//                    super.onManagerConnected(status);
//            }
//        }
//    }

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        if (OpenCVLoader.initLocal()) {
//            Log.d("OpenCV", "OpenCV successfully loaded.");
//        }
//
//        cameraBridgeViewBase = findViewById(R.id.cameraPreview);
//        cameraBridgeViewBase.setVisibility(CameraBridgeViewBase.VISIBLE);
//        cameraBridgeViewBase.setCvCameraViewListener(this);
//
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
//            isCameraPermissionGranted = true;
//        } else {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
//        }
//        if (isCameraPermissionGranted) {
//            OpenCVLoader.initDebug();
//            cameraBridgeViewBase.enableView();
//        }
//    }
//
//    @Override
//    public void onCameraViewStarted(int width, int height) {
//        previousFrame = new Mat();
//        currentFrame = new Mat();
//        prevPts = new MatOfPoint2f();
//        nextPts = new MatOfPoint2f();
//        status = new MatOfByte();
//        err = new MatOfFloat();
//    }
//
//    @Override
//    public void onCameraViewStopped() {
//        previousFrame.release();
//        currentFrame.release();
//        prevPts.release();
//        nextPts.release();
//        status.release();
//        err.release();
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        if (isCameraPermissionGranted) {
//            OpenCVLoader.initDebug();
//            cameraBridgeViewBase.enableView();
//        }
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        if (isCameraPermissionGranted) {
//            if (cameraBridgeViewBase != null) {
//                cameraBridgeViewBase.disableView();
//            }
//        }
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        if (cameraBridgeViewBase != null) {
//            cameraBridgeViewBase.disableView();
//        }
//    }
//
//    @Override
//    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
//        currentFrame = inputFrame.rgba();
//
//        if (!previousFrame.empty()) {
//            // Assuming you have already detected key points in the previous frame and stored them in prevPts
//            Video.calcOpticalFlowPyrLK(previousFrame, currentFrame, prevPts, nextPts, status, err);
//
//            // Perform motion detection and visualization here
//            // For example, draw lines based on the optical flow
//            for (int i = 0; i < nextPts.rows(); i++) {
//                double[] flow = nextPts.get(i, 0);
//                double flowX = flow[0];
//                double flowY = flow[1];
//
//                // Draw a line if motion vector is large enough
//                if (Math.sqrt(flowX * flowX + flowY * flowY) > 5) {
//                    // Adjust the drawing based on the detected flow
//                    // For example, you can draw lines using Imgproc.line
//                    // Make sure to adjust the coordinates according to the points provided by nextPts
//                }
//            }
//        }
//
//        previousFrame.release();
//        previousFrame = currentFrame.clone();
//
//        return currentFrame;
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == 1) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                isCameraPermissionGranted = true;
//            } else {
//                finish();
//            }
//        }
//    }
}

