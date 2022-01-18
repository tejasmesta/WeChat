package com.example.wechat.changepassword;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.wechat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private TextInputEditText email;
    private TextView resetMessage;
    Button retry;
    Button close;
    Button reset;
    private TextInputLayout resetv;
    private View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        if(getSupportActionBar()!=null)
        {
            getSupportActionBar().hide();
        }

        email = findViewById(R.id.reset_page_email);
        resetMessage = findViewById(R.id.reset_msg);
        retry = findViewById(R.id.reset_page_retry_button);
        close = findViewById(R.id.reset_page_close_button);
        reset = findViewById(R.id.reset_page_reset_button);
        resetv = findViewById(R.id.resetView);
        progressBar = findViewById(R.id.reset_page_progress);
    }

    public void resetPassButton(View view)
    {
        String email1 = email.getText().toString().trim();

        if(email1.equals(""))
        {
            email.setError("Enter email");
        }
        else
        {
            progressBar.setVisibility(View.VISIBLE);

            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

            firebaseAuth.sendPasswordResetEmail(email1).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    progressBar.setVisibility(View.GONE);

                    resetv.setVisibility(View.GONE);
                    email.setVisibility(View.GONE);
                    reset.setVisibility(View.GONE);


                    if(task.isSuccessful())
                    {
                        resetMessage.setVisibility(View.VISIBLE);
                        close.setVisibility(View.VISIBLE);
                        retry.setVisibility(View.VISIBLE);

                        new CountDownTimer(60000, 1000) {
                            @Override
                            public void onTick(long l) {
                                retry.setText("Retry in "+l/1000);
                                retry.setOnClickListener(null);
                            }

                            @Override
                            public void onFinish() {
                                retry.setText("Retry");

                                retry.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        resetv.setVisibility(View.VISIBLE);
                                        email.setVisibility(View.VISIBLE);
                                        reset.setVisibility(View.VISIBLE);
                                        resetMessage.setVisibility(View.GONE);
                                        close.setVisibility(View.GONE);
                                        retry.setVisibility(View.GONE);
                                    }
                                });
                            }
                        }.start();
                    }
                    else
                    {
                        resetMessage.setText("Failed to send email "+task.getException());
                        retry.setText("Retry");

                        retry.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                email.setVisibility(View.VISIBLE);
                                reset.setVisibility(View.VISIBLE);
                                resetMessage.setVisibility(View.GONE);
                            }
                        });
                    }
                }
            });
        }
    }

    public void close(View v)
    {
        finish();
    }
}