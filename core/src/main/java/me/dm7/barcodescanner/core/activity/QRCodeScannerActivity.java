package me.dm7.barcodescanner.core.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Camera;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.util.ArrayList;
import java.util.List;

import me.dm7.barcodescanner.core.BarcodeScannerView;
import me.dm7.barcodescanner.core.R;
import zxing.ZXingScannerView;

/**
 * Created by marcoscardoso on 28/09/15.
 */
public class QRCodeScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

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


    public static final int GET_MESSAGE = 1;
    public static final int SKIP_QRCODE = 2;
    public static final int BACK_PRESSED = 3;


    String message;
    String skip_title = "";

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_qrcode_preview);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.getBackground().setAlpha(0);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


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


        mScannerView = (ZXingScannerView) findViewById(R.id.scanner_view);
        setupFormats();

        messageInfo = (TextView) findViewById(R.id.message_info);


        if (getIntent().getExtras().containsKey(MESSAGE_INFO)) {

            message = getIntent().getExtras().getString(MESSAGE_INFO);
            messageInfo.setText(message);

        } else {
            messageInfo.setVisibility(View.GONE);
        }

        if (getIntent().getExtras().containsKey(SKIP_TITLE)) {

            skip_title = getIntent().getExtras().getString(SKIP_TITLE);

        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_open_and_start_class, menu);


        if ("".equals(skip_title)) {
            menu.getItem(R.id.fora_de_sala).setVisible(false);
        } else {
            menu.getItem(0).setTitle(skip_title);
        }
        return true;

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items


        if (item.getItemId() == R.id.fora_de_sala) {
            setResult(SKIP_QRCODE);
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

        mScannerView.startCamera(-1);
        mScannerView.setAutoFocus(mAutoFocus);
        mScannerView.setFlash(mFlash);
        mScannerView.setResultHandler(this);
    }

    @Override
    public void handleResult(Result rawResult) {

        Intent i = new Intent();
        i.setData(Uri.parse(rawResult.getText()));

        setResult(GET_MESSAGE, i);

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

        mScannerView.stopCamera();
    }

    @Override
    public void onBackPressed() {
        setResult(BACK_PRESSED);
        finish();
    }
}
