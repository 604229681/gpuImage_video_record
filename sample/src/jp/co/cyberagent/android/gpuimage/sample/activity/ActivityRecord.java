package jp.co.cyberagent.android.gpuimage.sample.activity;

import android.app.Activity;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.util.List;

import cn.iwgang.countdownview.CountdownView;
import jp.co.cyberagent.android.gpuimage.util.CameraHelper;
import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageMovieWriter;
import jp.co.cyberagent.android.gpuimage.sample.GPUImageBeautyFilter;
import jp.co.cyberagent.android.gpuimage.sample.R;

/**
 * Created by HuangBing on 2017/5/26.
 */
public class ActivityRecord extends Activity implements View.OnClickListener {

    private GPUImage mGPUImage;
    private CameraHelper mCameraHelper;
    private ActivityRecord.CameraLoader cameraLoader;
    private GPUImageMovieWriter mMovieWriter;

    private CountdownView mCvCountdownView;

    private boolean mIsRecording = false;

    private int width = 320;
    private int height = 240;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        findViewById(R.id.img_switch_camera).setOnClickListener(this);
        findViewById(R.id.button_record).setOnClickListener(this);

        mCvCountdownView = (CountdownView) findViewById(R.id.coutdown_view);

        mGPUImage = new GPUImage(this);
        mGPUImage.setGLSurfaceView((GLSurfaceView) findViewById(R.id.surfaceView));

        mMovieWriter = new GPUImageMovieWriter();
        mMovieWriter.addFilter(new GPUImageBeautyFilter());
        mGPUImage.setFilter(mMovieWriter);

        mCameraHelper = new CameraHelper(this);
        cameraLoader = new ActivityRecord.CameraLoader();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraLoader.onResume();
    }

    @Override
    protected void onPause() {
        cameraLoader.onPause();
        super.onPause();

        if (mIsRecording) {
            mMovieWriter.stopRecording();
        }
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.img_switch_camera:
                cameraLoader.switchCamera();
                break;

            case R.id.button_record:
                onClickRecord((Button) v);
                break;
        }
    }

    private void onClickRecord(Button btn) {
        try {
            if (Build.VERSION.SDK_INT >= 18) {
                if (mIsRecording) {
                    // go to stop recording
                    mIsRecording = false;
                    mMovieWriter.stopRecording();
                    btn.setText("Start Record");
                    mCvCountdownView.stop();
                } else {
                    // go to start recording
                    mIsRecording = true;
                    mCvCountdownView.start(1000 * 1000 * 60); // Millisecond
                    File recordFile = CameraHelper.getOutputMediaFile(CameraHelper.MEDIA_TYPE_VIDEO);
                    if (recordFile != null) {
                        mMovieWriter.startRecording(recordFile.getAbsolutePath(), width, height);
                        btn.setText("Stop Record");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private class CameraLoader {
        private int mCurrentCameraId = 1;
        private Camera mCamera;

        public void onResume() {
            setUpCamera(mCurrentCameraId);
        }

        public void onPause() {
            releaseCamera();
        }

        public void switchCamera() {
            releaseCamera();
            mCurrentCameraId = (mCurrentCameraId + 1) % mCameraHelper.getNumberOfCameras();
            setUpCamera(mCurrentCameraId);
        }

        private void setUpCamera(final int id) {
            mCamera = getCameraInstance(id);
            System.out.println("setUpCamera==================id " + id);
            Camera.Parameters parameters = mCamera.getParameters();
            // TODO adjust by getting supportedPreviewSizes and then choosing
            // the best one for screen size (best fill screen)
            List<Camera.Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
            List<Camera.Size> mSupportedVideoSizes = parameters.getSupportedVideoSizes();
            Camera.Size optimalSize = CameraHelper.getOptimalVideoSize(mSupportedVideoSizes,
                    mSupportedPreviewSizes, width, height);
            width = optimalSize.width;
            height = optimalSize.height;
            if (parameters.getSupportedFocusModes().contains(
                    Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
            parameters.setPreviewSize(optimalSize.width, optimalSize.height);
            mCamera.setParameters(parameters);

            int orientation = mCameraHelper.getCameraDisplayOrientation(
                    ActivityRecord.this, mCurrentCameraId);
            CameraHelper.CameraInfo2 cameraInfo = new CameraHelper.CameraInfo2();
            mCameraHelper.getCameraInfo(mCurrentCameraId, cameraInfo);
            boolean flipHorizontal = cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT;
            if (mCamera != null && mGPUImage != null) {
                mGPUImage.setUpCamera(mCamera, orientation, flipHorizontal, false);
            }
        }

        /**
         * A safe way to get an instance of the Camera object.
         */
        private Camera getCameraInstance(final int id) {
            Camera c = null;
            try {
                c = mCameraHelper.openCamera(id);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return c;
        }

        private void releaseCamera() {
            if(mCamera != null) {
                mCamera.setPreviewCallback(null);
                mCamera.release();
            }
        }
    }
}
