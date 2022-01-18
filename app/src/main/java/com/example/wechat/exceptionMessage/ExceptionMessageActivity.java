package com.example.wechat.exceptionMessage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.wechat.R;
import com.example.wechat.common.Util;

public class ExceptionMessageActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView textView;
    private Button close;
    private Button retry;
    private ConnectivityManager.NetworkCallback networkCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exception_message);

        if(getSupportActionBar()!=null)
        {
            getSupportActionBar().hide();
        }

        progressBar = findViewById(R.id.exception_page_progress);
        close = findViewById(R.id.exception_page_close);
        retry = findViewById(R.id.exception_page_retry);
        textView = findViewById(R.id.exception_page_msg);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            networkCallback = new ConnectivityManager.NetworkCallback()
            {
                @Override
                public void onAvailable(@NonNull Network network) {
                    super.onAvailable(network);
                    finish();
                }

                @Override
                public void onLost(@NonNull Network network) {
                    super.onLost(network);
                    textView.setText("No Internet");
                }
            };

            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            connectivityManager.registerNetworkCallback(new NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build(),networkCallback);
        }
    }

    public void retry(View view)
    {
        progressBar.setVisibility(View.VISIBLE);
        if(Util.connectionAvailable(this))
        {
            finish();
        }
        else
        {
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.GONE);
                }
            },1000);
        }
    }

    public void closeIt(View view)
    {
        finishAffinity();
    }
}