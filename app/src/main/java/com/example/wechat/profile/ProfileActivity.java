package com.example.wechat.profile;

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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.wechat.R;
import com.example.wechat.changepassword.ChangePasswordActivity;
import com.example.wechat.common.NodeNames;
import com.example.wechat.login.LoginActivity;
import com.example.wechat.signup.SignUpActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private TextInputEditText Name;
    private TextView Email;
    private String name,email;
    private Button Update;
    private Button LogOut;
    private ImageView Profile;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private StorageReference fileStorage;
    private Uri localfileUri, serverFileUri;
    private ImageView profile;
    private FirebaseAuth firebaseAuth;
    private View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        if(getSupportActionBar()!=null)
        {
            getSupportActionBar().hide();
        }

        Name = findViewById(R.id.profile_page_name);
        Email = findViewById(R.id.profile_page_email);
        Update = findViewById(R.id.profile_page_update_button);
        LogOut = findViewById(R.id.profile_page_logout_button);
        Profile = findViewById(R.id.profile_page_profile_picture);
        fileStorage = FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        progressBar = findViewById(R.id.profile_page_progress);

        if(firebaseUser!=null)
        {
            progressBar.setVisibility(View.VISIBLE);

            Name.setText(firebaseUser.getDisplayName());
            Email.setText(firebaseUser.getEmail());
            serverFileUri = firebaseUser.getPhotoUrl();

            if(serverFileUri!=null)
            {
               Glide.with(this)
                    .load(serverFileUri)
                    .placeholder(R.drawable.deafault_avatar)
                    .error(R.drawable.deafault_avatar)
                    .into(Profile);
            }

            progressBar.setVisibility(View.GONE);

        }
    }

    public void changePass(View view)
    {
        startActivity(new Intent(ProfileActivity.this, ChangePasswordActivity.class));
    }

    public void logOut(View view)
    {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        firebaseAuth.signOut();

        startActivity(new Intent(ProfileActivity.this,LoginActivity.class));

        finish();
    }

    public void save(View view)
    {
        if(Name.getText().toString().trim().equals(""))
        {
            Name.setError("Enter name");
        }
        else
        {
            if(localfileUri!=null)
            {
                updateNameAndPhoto();
            }
            else
            {
                updateOnlyName();
            }
        }
    }

    public void changeProfile(View v)
    {
        if(serverFileUri==null)
        {
            pickProfile();
        }
        else
        {
            PopupMenu popupMenu = new PopupMenu(this,v);

            popupMenu.getMenuInflater().inflate(R.menu.menu_picture, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    int id = menuItem.getItemId();

                    if(id==R.id.change_profile_picture)
                    {
                        pickProfile();
                    }
                    else if(id==R.id.remove_profile_picture)
                    {
                        remove();
                    }

                    return false;
                }
            });

            popupMenu.show();
        }
    }

    private void remove()
    {
        progressBar.setVisibility(View.VISIBLE);

        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(Name.getText().toString())
                .setPhotoUri(null)
                .build();

        firebaseUser.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressBar.setVisibility(View.GONE);

                if(task.isSuccessful())
                {
                    String userId = firebaseUser.getUid();

                    databaseReference = FirebaseDatabase.getInstance("https://wechat-da973-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child(NodeNames.USERS);

                    DatabaseReference ref = databaseReference;

                    ref.child(userId).child(NodeNames.PHOTO).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(ProfileActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                Toast.makeText(ProfileActivity.this, "Failed to update "+task.getException(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
                else
                {
                    Toast.makeText(ProfileActivity.this, "Failed to update profile "+task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void pickProfile()
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

                Profile.setImageURI(localfileUri);
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
                Toast.makeText(ProfileActivity.this, "Please provide the required permissions", Toast.LENGTH_SHORT).show();
            }
        }
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

                    DatabaseReference ref = databaseReference;



                    ref.child(userId).child(NodeNames.NAME).setValue(Name.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(ProfileActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                Toast.makeText(ProfileActivity.this, "Failed to update "+task.getException(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
                else
                {
                    Toast.makeText(ProfileActivity.this, "Failed to update "+task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateNameAndPhoto()
    {
        progressBar.setVisibility(View.VISIBLE);

        String fileName = firebaseUser.getUid()+".jpg";

        final StorageReference storageReference = fileStorage.child("images/"+fileName);

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

                            DatabaseReference ref = FirebaseDatabase.getInstance("https://wechat-da973-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child(NodeNames.USERS);

                            firebaseUser.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        String userId = firebaseUser.getUid();

                                        databaseReference = FirebaseDatabase.getInstance("https://wechat-da973-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child(NodeNames.USERS);

                                        DatabaseReference ref = databaseReference;

                                        ref.child(userId).child(NodeNames.NAME).setValue(Name.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful())
                                                {
                                                    Toast.makeText(ProfileActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                                                }
                                                else
                                                {
                                                    Toast.makeText(ProfileActivity.this, "Failed to update "+task.getException(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });


                                        ref.child(userId).child(NodeNames.PHOTO).setValue(serverFileUri.getPath()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful())
                                                {
                                                    Toast.makeText(ProfileActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                                                }
                                                else
                                                {
                                                    Toast.makeText(ProfileActivity.this, "Failed to update "+task.getException(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });

                                    }
                                    else
                                    {
                                        Toast.makeText(ProfileActivity.this, "Failed to update "+task.getException(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });


                        }
                    });
                }
            }
        });
    }
}