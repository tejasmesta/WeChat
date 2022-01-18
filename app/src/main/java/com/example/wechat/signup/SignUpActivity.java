package com.example.wechat.signup;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.wechat.MainActivity;
import com.example.wechat.R;
import com.example.wechat.common.NodeNames;
import com.example.wechat.login.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private TextInputEditText Email, Password, Name, ConfirmPassword;
    private String name, email, pass, conPass;
    private Button SignUp;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private StorageReference fileStorage;
    private Uri localfileUri, serverFileUri;
    private ImageView profile;
    private View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        if(getSupportActionBar()!=null)
        {
            getSupportActionBar().hide();
        }

        Name = findViewById(R.id.signup_page_name);
        Email = findViewById(R.id.signup_page_email);
        Password = findViewById(R.id.signup_page_password);
        ConfirmPassword = findViewById(R.id.signup_page_confirm_password);
        SignUp = findViewById(R.id.signup_page_signup_button);
        profile = findViewById(R.id.signup_page_profile_picture);

        progressBar = findViewById(R.id.signup_page_progress);

        fileStorage = FirebaseStorage.getInstance().getReference();
    }

    private void updateOnlyName()
    {
        progressBar.setVisibility(View.VISIBLE);

        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(Name.getText().toString())
                .build();

        firebaseUser.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressBar.setVisibility(View.GONE);
                if(task.isSuccessful())
                {
                    String userId = firebaseUser.getUid();

                    databaseReference = FirebaseDatabase.getInstance("https://wechat-da973-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child(NodeNames.USERS);

                    HashMap<String,String> map = new HashMap<>();

                    map.put(NodeNames.NAME,Name.getText().toString());
                    map.put(NodeNames.EMAIL,Email.getText().toString().trim());
                    map.put(NodeNames.ONLINE,"true");
                    map.put(NodeNames.PHOTO,"");

                    progressBar.setVisibility(View.VISIBLE);

                    databaseReference.child(userId).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressBar.setVisibility(View.GONE);
                            if(task.isSuccessful())
                            {
                                Toast.makeText(SignUpActivity.this, "User created successfully", Toast.LENGTH_SHORT).show();

                                startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                            }
                            else
                            {
                                Toast.makeText(SignUpActivity.this, "Failed to create user "+task.getException(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else
                {
                    Toast.makeText(SignUpActivity.this, "Failed to update profile "+task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void pickProfile(View v)
    {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)
        {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            startActivityForResult(intent,101);
        }
        else
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},102);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==101)
        {
            if(resultCode==RESULT_OK)
            {
                localfileUri = data.getData();

                profile.setImageURI(localfileUri);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==102)
        {
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(intent,101);
            }
            else
            {
                Toast.makeText(SignUpActivity.this, "Please provide the required permissions", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateNameAndPhoto()
    {
        String fileName = firebaseUser.getUid()+".jpg";

        final StorageReference storageReference = fileStorage.child("images/"+fileName);

        progressBar.setVisibility(View.VISIBLE);

        storageReference.putFile(localfileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                progressBar.setVisibility(View.GONE);
                if(task.isSuccessful())
                {
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            serverFileUri = uri;

                            UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(Name.getText().toString())
                                    .setPhotoUri(serverFileUri)
                                    .build();

                            firebaseUser.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        String userId = firebaseUser.getUid();

                                        databaseReference = FirebaseDatabase.getInstance("https://wechat-da973-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child(NodeNames.USERS);

                                        HashMap<String,String> map = new HashMap<>();

                                        map.put(NodeNames.NAME,Name.getText().toString());
                                        map.put(NodeNames.EMAIL,Email.getText().toString().trim());
                                        map.put(NodeNames.ONLINE,"true");
                                        map.put(NodeNames.PHOTO,serverFileUri.getPath());

                                        databaseReference.child(userId).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful())
                                                {
                                                    Toast.makeText(SignUpActivity.this, "User created successfully", Toast.LENGTH_SHORT).show();

                                                    startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                                                }
                                                else
                                                {
                                                    Toast.makeText(SignUpActivity.this, "Failed to create user "+task.getException(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                    else
                                    {
                                        Toast.makeText(SignUpActivity.this, "Failed to update profile "+task.getException(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });


                        }
                    });
                }
            }
        });
    }

    public void signupClick(View v)
    {
        name = Name.getText().toString();
        email = Email.getText().toString().trim();
        pass = Password.getText().toString().trim();
        conPass = ConfirmPassword.getText().toString().trim();

        if(email.equals(""))
        {
            Email.setError("Enter email address");
        }
        else if(name.isEmpty())
        {
            Name.setError("Enter name");
        }
        else if(pass.isEmpty())
        {
            Password.setError("Enter password");
        }
        else if(conPass.isEmpty())
        {
            ConfirmPassword.setError("Confirm password");
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            Email.setError("Enter correct email");
        }
        else if(!pass.equals(conPass))
        {
            ConfirmPassword.setError("Password mismatched");
        }
        else
        {
            progressBar.setVisibility(View.VISIBLE);

            final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

            firebaseAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    progressBar.setVisibility(View.GONE);
                    if(task.isSuccessful())
                    {
                        firebaseUser = firebaseAuth.getCurrentUser();

                        if(localfileUri!=null)
                        {
                            updateNameAndPhoto();
                        }
                        else
                        {
                            updateOnlyName();
                        }
                    }
                    else
                    {
                        Toast.makeText(SignUpActivity.this, "SignUp failed "+task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}