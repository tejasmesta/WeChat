package com.example.wechat.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wechat.MainActivity;
import com.example.wechat.R;
import com.example.wechat.changepassword.ResetPasswordActivity;
import com.example.wechat.common.Util;
import com.example.wechat.exceptionMessage.ExceptionMessageActivity;
import com.example.wechat.signup.SignUpActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.*;


public class LoginActivity extends AppCompatActivity {

    private TextInputEditText Email, Password;
    private String email, password;
    private Button login;
    private View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if(getSupportActionBar()!=null)
        {
            getSupportActionBar().hide();
        }

        Email = findViewById(R.id.login_page_email);
        Password = findViewById(R.id.login_page_password);
        login = findViewById(R.id.login_page_login_button);
        progressBar = findViewById(R.id.login_page_progress);
    }

    public void newSignUp(View v)
    {
        startActivity(new Intent(LoginActivity.this, SignUpActivity.class));

    }

    public void loginClick(View v)
    {
        email = Email.getText().toString().trim();
        password = Password.getText().toString().trim();

        if(email.equals(""))
        {
            Email.setError("Enter email Address");
        }
        else if(password.equals(""))
        {
            Password.setError("Enter password");
        }
        else
        {
            if(Util.connectionAvailable(this)) {
                progressBar.setVisibility(View.VISIBLE);

                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));

                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Login Failed " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            else
            {
                startActivity(new Intent(LoginActivity.this, ExceptionMessageActivity.class));
            }
        }
    }

    public void resetPassLogin(View view)
    {

        startActivity(new Intent(LoginActivity.this,ResetPasswordActivity.class));

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if(firebaseUser!=null)
        {


            startActivity(new Intent(LoginActivity.this,MainActivity.class));
            finish();
        }


    }
}