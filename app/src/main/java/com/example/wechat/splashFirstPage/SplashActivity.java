package com.example.wechat.splashFirstPage;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wechat.R;
import com.example.wechat.login.LoginActivity;

public class SplashActivity extends AppCompatActivity {

    private ImageView image;
    private TextView text;
    private Animation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if(getSupportActionBar()!=null)
        {
            getSupportActionBar().hide();
        }

        image = findViewById(R.id.splash_image);
        text = findViewById(R.id.splash_text);

        animation = AnimationUtils.loadAnimation(this,R.anim.splashanimation);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        image.startAnimation(animation);
        text.startAnimation(animation);
    }
}