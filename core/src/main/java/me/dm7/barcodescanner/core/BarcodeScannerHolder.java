package me.dm7.barcodescanner.core;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.view.ViewGroup;

import com.google.zxing.BarcodeFormat;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import zxing.ZXingScannerView;

/**
 * Created by Ricardo on 27/11/2015.
 */
public class BarcodeScannerHolder {

    private static final int REQUEST_CAMERA_PERMISSION = 123;

    private static final String FLASH_STATE = "FLASH_STATE";
    private static final String AUTO_FOCUS_STATE = "AUTO_FOCUS_STATE";
    private static final String SELECTED_FORMATS = "SELECTED_FORMATS";
    private static final String CAMERA_ID = "CAMERA_ID";
    private static final String PERMISSION_REQUESTED = "PERMISSION_REQUESTED";

    private ZXingScannerView mScannerView;

    private boolean mFlash;
    private boolean mAutoFocus;
    private ArrayList<Integer> mSelectedIndices;
    private int mCameraId = -1;

    private int mCameraViewFinderMarginTop = 0;

    private boolean mCameraStarted = false;
    private boolean mIsPermissionRequested = false;

    private final WeakReference<Activity> mWeakActivity;
    private ViewGroup mScannerViewContainer;
    private final ZXingScannerView.ResultHandler mCallback;

    public BarcodeScannerHolder(Activity activity, ZXingScannerView.ResultHandler callback) {
        mWeakActivity = new WeakReference<>(activity);
        mCallback = callback;
    }

    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mIsPermissionRequested = savedInstanceState.getBoolean(PERMISSION_REQUESTED, false);
        }
    }

    public void onCreateView(ViewGroup scannerViewContainer, Bundle savedInstanceState) {
        mScannerViewContainer = scannerViewContainer;
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            initCameraView(savedInstanceState);
        } else if (!mIsPermissionRequested) {
            mIsPermissionRequested = true;
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        mIsPermissionRequested = false;
        if (requestCode == REQUEST_CAMERA_PERMISSION
                && grantResults.length == 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initCameraView(null);
            return true;
        } else {
            mIsPermissionRequested = true;
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            return false;
        }
    }

    public void onResume() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            initCamera();
        } else if (!mIsPermissionRequested) {
            getActivity().recreate();
        }
    }

    public void onPause() {
        if (mScannerView != null) {
            mScannerView.stopCamera();
            mCameraStarted = false;
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(FLASH_STATE, mFlash);
        outState.putBoolean(AUTO_FOCUS_STATE, mAutoFocus);
        outState.putIntegerArrayList(SELECTED_FORMATS, mSelectedIndices);
        outState.putInt(CAMERA_ID, mCameraId);
        outState.putBoolean(PERMISSION_REQUESTED, mIsPermissionRequested);
    }

    private Activity getActivity() {
        return mWeakActivity.get();
    }

    public void setCameraViewFinderMarginTop(int margin) {
        mCameraViewFinderMarginTop = margin;
    }

    public void initCameraView(final Bundle savedInstanceState) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mScannerView = new ZXingScannerView(mScannerViewContainer.getContext());
                            mScannerView.setViewFinderTopOffset(mCameraViewFinderMarginTop);
                            setupFormats();

                            if (savedInstanceState != null) {
                                mFlash = savedInstanceState.getBoolean(FLASH_STATE, false);
                                mAutoFocus = savedInstanceState.getBoolean(AUTO_FOCUS_STATE, true);
                                mSelectedIndices = savedInstanceState.getIntegerArrayList(SELECTED_FORMATS);
                                mCameraId = savedInstanceState.getInt(CAMERA_ID, -1);
                            } else {
                                mFlash = false;
                                mAutoFocus = true;
                                mSelectedIndices = null;
                                mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
                            }

                            mScannerViewContainer.addView(mScannerView);

                            initCamera();
                        }
                    });
                }
            }
        }, 50);
    }

    public void setupFormats() {
        List<BarcodeFormat> formats = new ArrayList<BarcodeFormat>();
        if (mSelectedIndices == null || mSelectedIndices.isEmpty()) {
            mSelectedIndices = new ArrayList<Integer>();
            for (int i = 0; i < ZXingScannerView.ALL_FORMATS.size(); i++) {
                mSelectedIndices.add(i);
            }
        }

        for (int index : mSelectedIndices) {
            formats.add(ZXingScannerView.ALL_FORMATS.get(index));
        }
        if (mScannerView != null) {
            mScannerView.setFormats(formats);
        }
    }

    private void initCamera() {
        if (mScannerView != null && !mCameraStarted) {
            mCameraStarted = true;
            mScannerView.setAutoFocus(mAutoFocus);
            mScannerView.setFlash(mFlash);
            mScannerView.setResultHandler(mCallback);
            mScannerView.startCamera(mCameraId);
        }
    }
}
