package com.wei.grabmoney.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.wei.grabmoney.R;
import com.wei.grabmoney.utils.Log;

public class EmptyActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty);
        Log.e(TAG, "--- onCreate ---");
        finish();
    }
}
