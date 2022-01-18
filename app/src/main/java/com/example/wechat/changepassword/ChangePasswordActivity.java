package com.example.wechat.changepassword;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.wechat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private TextInputEditText pass, conPass;
    private View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        if(getSupportActionBar()!=null)
        {
            getSupportActionBar().hide();
        }

        pass = findViewById(R.id.change_password_page_new);
        conPass = findViewById(R.id.change_password_page_confirm);
        progressBar = findViewById(R.id.changepass_page_progress);
    }

    public void saveNewPass(View view)
    {
        String newP = pass.getText().toString().trim();
        String conP = conPass.getText().toString().trim();

        if(newP.equals(""))
        {
            pass.setError("Enter password");
        }
        else if(conP.equals(""))
        {
            conPass.setError("Confirm password");
        }
        else if(!newP.equals(conP))
        {
            conPass.setError("Password mismatched");
        }
        else
        {
            progressBar.setVisibility(View.VISIBLE);

            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

            if(firebaseUser!=null)
            {
                firebaseUser.updatePassword(newP).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressBar.setVisibility(View.GONE);

                        if(task.isSuccessful())
                        {
                            Toast.makeText(ChangePasswordActivity.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        else
                        {
                            Toast.makeText(ChangePasswordActivity.this, "Something went wrong "+task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }
}