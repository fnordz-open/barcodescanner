package me.dm7.barcodescanner.core.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.util.ArrayList;
import java.util.List;

import me.dm7.barcodescanner.core.R;
import zxing.ZXingScannerView;

/**
 * Created by marcoscardoso on 28/09/15.
 */
public class QRCodeScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private static final int REQUEST_CAMERA_PERMISSION = 123;

    public static final String MESSAGE_INFO = "messageinfo";
    public static final String SKIP_TITLE = "skiptitle";
    public static final String MESSAGE = "message";

    ZXingScannerView mScannerView;
    TextView messageInfo;

    private static final String FLASH_STATE = "FLASH_STATE";
    private static final String AUTO_FOCUS_STATE = "AUTO_FOCUS_STATE";
    private static final String SELECTED_FORMATS = "SELECTED_FORMATS";
    private static final String CAMERA_ID = "CAMERA_ID";
    private boolean mFlash;
    private boolean mAutoFocus;
    private ArrayList<Integer> mSelectedIndices;
    private int mCameraId = -1;


    public static final int RESULT_GET_MESSAGE = 1;
    public static final int RESULT_SKIP_QRCODE = 2;
    public static final int RESULT_PERMISSION_DENIED = 3;

    private boolean isPermissionRequested = false;

    String message;
    String skip_title = "";

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setResult(RESULT_CANCELED);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            initViews(state);
        } else {
            isPermissionRequested = true;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (TextUtils.isEmpty(skip_title)) {
            return super.onCreateOptionsMenu(menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_open_and_start_class, menu);
            menu.getItem(0).setTitle(skip_title);
            return true;
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items


        if (item.getItemId() == R.id.fora_de_sala) {
            setResult(RESULT_SKIP_QRCODE);
            finish();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            initCamera();
        } else if (!isPermissionRequested) {
            recreate();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        isPermissionRequested = false;
        if (requestCode == REQUEST_CAMERA_PERMISSION
                && grantResults.length == 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initViews(null);
            initCamera();
        } else {
            setResult(RESULT_PERMISSION_DENIED);
            finish();
        }
    }

    private void initViews(Bundle state) {
        setContentView(R.layout.activity_qrcode_preview);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.getBackground().setAlpha(0);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        messageInfo = (TextView) findViewById(R.id.message_info);
        Bundle b = getIntent().getExtras() == null ? new Bundle() : getIntent().getExtras();

        if (b.containsKey(MESSAGE_INFO)) {
            message = getIntent().getExtras().getString(MESSAGE_INFO);
            messageInfo.setText(message);
        } else {
            messageInfo.setVisibility(View.GONE);
        }

        if (b.containsKey(SKIP_TITLE)) {
            skip_title = getIntent().getExtras().getString(SKIP_TITLE);
        }

        mScannerView = (ZXingScannerView) findViewById(R.id.scanner_view);
        setupFormats();

        if (state != null) {
            mFlash = state.getBoolean(FLASH_STATE, false);
            mAutoFocus = state.getBoolean(AUTO_FOCUS_STATE, true);
            mSelectedIndices = state.getIntegerArrayList(SELECTED_FORMATS);
            mCameraId = state.getInt(CAMERA_ID, -1);
        } else {
            mFlash = false;
            mAutoFocus = true;
            mSelectedIndices = null;
            mCameraId = -1;
        }
    }

    private void initCamera() {
        mScannerView.startCamera(-1);
        mScannerView.setAutoFocus(mAutoFocus);
        mScannerView.setFlash(mFlash);
        mScannerView.setResultHandler(this);
    }

    @Override
    public void handleResult(Result rawResult) {

        Intent i = new Intent();
        i.setData(Uri.parse(rawResult.getText()));

        setResult(RESULT_GET_MESSAGE, i);

        finish();

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

    @Override
    protected void onPause() {
        super.onPause();

        if (mScannerView != null) {
            mScannerView.stopCamera();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
