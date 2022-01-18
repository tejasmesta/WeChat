package com.example.wechat.chat;

import static android.provider.MediaStore.ACTION_IMAGE_CAPTURE;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.wechat.R;
import com.example.wechat.common.Constants;
import com.example.wechat.common.Extras;
import com.example.wechat.common.NodeNames;
import com.example.wechat.common.Util;
import com.example.wechat.selectFriends.select_friend;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE_FORWARD_MESSAGE = 104;
    private EditText enterMessage;
    private ImageView attach;
    private ImageView profile;
    private TextView username;
    private ImageView send;
    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;
    private FirebaseAuth firebaseAuth;
    private String currentUserId;
    private String chatUserId;
    private RecyclerView rvMessages;
    private SwipeRefreshLayout srlMessages;
    private MessagesAdapter adapter;
    private List<MessageModel> messageModelList;
    private LinearLayout llprogress;


    private int currentPage = 1;
    private static final int record_per_page = 30;
    private DatabaseReference databaseReferenceMessages;
    private ChildEventListener childEventListener;
    private BottomSheetDialog bottomSheetDialog;
    private static final int REQUEST_CODE_PICK_IMAGE = 101;
    private static final int REQUEST_CODE_CAPTURE_IMAGE = 102;
    private static final int REQUEST_CODE_PICK_VIDEO = 103;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ActionBar actionBar = getSupportActionBar();



        if(actionBar!=null)
        {
            actionBar.setTitle("");

            ViewGroup actionBarLayout = (ViewGroup) getLayoutInflater().inflate(R.layout.custom_action_bar,null);

            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setElevation(0);

            actionBar.setCustomView(actionBarLayout);
            actionBar.setDisplayOptions(actionBar.getDisplayOptions()|ActionBar.DISPLAY_SHOW_CUSTOM);
        }

        profile = findViewById(R.id.convo_profile);
        username = findViewById(R.id.convo_username);

        enterMessage = findViewById(R.id.enter_message);
        send = findViewById(R.id.send_message);
        attach = findViewById(R.id.attachment);

        attach.setOnClickListener(this);

        send.setOnClickListener(this);

        llprogress = findViewById(R.id.LLprogress);

        firebaseAuth = FirebaseAuth.getInstance();
        srlMessages = findViewById(R.id.srlmessages);

        databaseReference = FirebaseDatabase.getInstance("https://wechat-da973-default-rtdb.asia-southeast1.firebasedatabase.app").getReference();

        currentUserId = firebaseAuth.getCurrentUser().getUid();

        if(getIntent().hasExtra(Extras.User_Key))
        {
            chatUserId = getIntent().getStringExtra(Extras.User_Key);
        }

        databaseReference.child(NodeNames.USERS).child(chatUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child(NodeNames.NAME).getValue().toString();
                username.setText(name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        StorageReference fileref = FirebaseStorage.getInstance().getReference().child(Constants.IMAGES_STORAGE+"/"+chatUserId+".jpg");

        fileref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(ChatActivity.this)
                        .load(uri)
                        .placeholder(R.drawable.deafault_avatar)
                        .error(R.drawable.deafault_avatar)
                        .into(profile);
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        rvMessages = findViewById(R.id.personal_conv);
        messageModelList = new ArrayList<>();
        adapter = new MessagesAdapter(this,messageModelList);


        rvMessages.setLayoutManager(new LinearLayoutManager(this));

        rvMessages.setAdapter(adapter);

        loadMessages();

        rvMessages.scrollToPosition(messageModelList.size()-1);

        srlMessages.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currentPage++;
                loadMessages();
                rvMessages.scrollToPosition(messageModelList.size()-1);
            }
        });

        bottomSheetDialog = new BottomSheetDialog(this);

        View view = getLayoutInflater().inflate(R.layout.chat_file_options,null);

        view.findViewById(R.id.send_camera).setOnClickListener(this);
        view.findViewById(R.id.send_gallery).setOnClickListener(this);
        view.findViewById(R.id.send_video).setOnClickListener(this);

        bottomSheetDialog.setContentView(view);

        if(getIntent().hasExtra(Extras.MESSAGE) && getIntent().hasExtra(Extras.MESSAGE_ID) && getIntent().hasExtra(Extras.MESSAGE_TYPE) )
        {
            String messageId = getIntent().getStringExtra(Extras.MESSAGE_ID);
            String message = getIntent().getStringExtra(Extras.MESSAGE);
            String messageType = getIntent().getStringExtra(Extras.MESSAGE_TYPE);

            DatabaseReference messageRef = databaseReference.child(NodeNames.MESSAGES).child(currentUserId).child(chatUserId).push();

            String newMessageId = messageRef.getKey();

            if(messageType.equals(Constants.TEXT)) {
                sendMessage(message, messageType, newMessageId);
            }
            else
            {
                StorageReference newref = FirebaseStorage.getInstance().getReference();
                String folder = messageType.equals(Constants.VIDEO)?Constants.VIDEOS_STORAGE:Constants.PHOTO_STORAGE;
                String oldFileName = messageType.equals(Constants.VIDEO)?messageId+".mp4":messageId+".jpg";
                String newFileName = messageType.equals(Constants.VIDEO)?newMessageId+".mp4":newMessageId+".jpg";

                String localFilePath = getExternalFilesDir(null).getAbsolutePath()+"/"+oldFileName;
                File localFile = new File(localFilePath);

                StorageReference newFileRef = newref.child(folder).child(newFileName);

                newref.child(folder).child(oldFileName).getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        UploadTask uploadTask = newFileRef.putFile(Uri.fromFile(localFile));
                        uploadProgress(uploadTask,newFileRef,newMessageId,messageType);
                    }
                });
            }
        }

    }

    private void sendMessage(String msg,String type,String pushId)
    {
        try {

            if(!msg.isEmpty())
            {
                HashMap map = new HashMap<>();


                map.put(NodeNames.MESSAGE_ID,pushId);
                map.put(NodeNames.MESSAGE,msg);
                map.put(NodeNames.MESSAGE_TYPE,type);
                map.put(NodeNames.MESSAGE_FROM,currentUserId);
                map.put(NodeNames.MESSAGE_TIME,ServerValue.TIMESTAMP);


                String currentUserRef = NodeNames.MESSAGES+"/"+currentUserId+"/"+chatUserId;
                String chatUserRef = NodeNames.MESSAGES+"/"+chatUserId+"/"+currentUserId;

                HashMap usermap = new HashMap<>();
                usermap.put(currentUserRef+"/"+pushId,map);
                usermap.put(chatUserRef+"/"+pushId,map);

                enterMessage.setText("");

                databaseReference.updateChildren(usermap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if(databaseError!=null)
                        {
                            Toast.makeText(ChatActivity.this, "Failed to send Message "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(ChatActivity.this, "Message Sent", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        }catch (Exception e)
        {

            Toast.makeText(ChatActivity.this, "Failed to send Message "+e.getMessage(), Toast.LENGTH_SHORT).show();

        }
    }

    private void loadMessages()
    {
        messageModelList.clear();

        databaseReferenceMessages = FirebaseDatabase.getInstance("https://wechat-da973-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child(NodeNames.MESSAGES).child(currentUserId).child(chatUserId);

        Query query = databaseReferenceMessages.limitToLast(currentPage*record_per_page);

        if(childEventListener!=null)
        {
            query.removeEventListener(childEventListener);

        }

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                MessageModel message = dataSnapshot.getValue(MessageModel.class);

                messageModelList.add(message);

                adapter.notifyDataSetChanged();

                rvMessages.scrollToPosition(messageModelList.size()-1);

                srlMessages.setRefreshing(false);


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                loadMessages();
                rvMessages.scrollToPosition(messageModelList.size()-1);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        query.addChildEventListener(childEventListener);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.send_message:
                if (Util.connectionAvailable(this)) {
                    DatabaseReference userMessagePush = databaseReference.child(NodeNames.MESSAGE).child(currentUserId).child(chatUserId).push();
                    String pushId = userMessagePush.getKey();
                    sendMessage(enterMessage.getText().toString().trim(), Constants.TEXT, pushId);
                }
                else
                {
                    Toast.makeText(this, "No Internet", Toast.LENGTH_SHORT).show();
                }
                break;



            case R.id.attachment:
                if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)
                {
                    if(bottomSheetDialog!=null)
                    {
                        bottomSheetDialog.show();
                    }
                }
                else
                {
                    ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                }
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if(inputMethodManager!=null)
                {
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),0);
                }
                break;



            case R.id.send_camera:
                bottomSheetDialog.dismiss();
                Intent intent = new Intent(ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,REQUEST_CODE_CAPTURE_IMAGE);
                break;



            case R.id.send_gallery:
                bottomSheetDialog.dismiss();
                Intent intent1 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent1,REQUEST_CODE_PICK_IMAGE);
                break;



            case R.id.send_video:
                bottomSheetDialog.dismiss();
                Intent intent2 = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent2,REQUEST_CODE_PICK_VIDEO);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode==RESULT_OK)
        {
            if(requestCode == REQUEST_CODE_CAPTURE_IMAGE)
            {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,bytes);
                uploadBytes(bytes,Constants.IMAGE);
            }
            else if(requestCode == REQUEST_CODE_PICK_IMAGE)
            {
                Uri uri = data.getData();
                uploadFile(uri,Constants.IMAGE);
            }
            else if(requestCode == REQUEST_CODE_PICK_VIDEO)
            {
                Uri uri = data.getData();
                uploadFile(uri,Constants.VIDEO);
            }
            else if(requestCode == REQUEST_CODE_FORWARD_MESSAGE)
            {
                Intent intent = new Intent(this,ChatActivity.class);

                intent.putExtra(Extras.User_Key,data.getStringExtra(Extras.User_Key));
                intent.putExtra(Extras.User_name,data.getStringExtra(Extras.User_name));
                intent.putExtra(Extras.User_photo,data.getStringExtra(Extras.User_photo));

                intent.putExtra(Extras.MESSAGE,data.getStringExtra(Extras.MESSAGE));
                intent.putExtra(Extras.MESSAGE_ID,data.getStringExtra(Extras.MESSAGE_ID));
                intent.putExtra(Extras.MESSAGE_TYPE,data.getStringExtra(Extras.MESSAGE_TYPE));

                startActivity(intent);
                finish();
            }
        }
    }

    private void uploadFile(Uri uri,String messageType)
    {
        DatabaseReference data = databaseReference.child(NodeNames.MESSAGES).child(currentUserId).child(chatUserId).push();
        String pushId = data.getKey();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();

        String folderName  = messageType.equals(Constants.IMAGE)?Constants.PHOTO_STORAGE:Constants.VIDEOS_STORAGE;

        String fileName = messageType.equals(Constants.IMAGE)?pushId+".jpeg":pushId+".mp4";

        StorageReference fileRef = storageReference.child(folderName).child(fileName);

        UploadTask uploadTask = fileRef.putFile(uri);

        uploadProgress(uploadTask,fileRef,pushId,messageType);
    }

    private void uploadBytes(ByteArrayOutputStream bytes,String messageType)
    {
        DatabaseReference data = databaseReference.child(NodeNames.MESSAGES).child(currentUserId).child(chatUserId).push();
        String pushId = data.getKey();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();

        String folderName  = messageType.equals(Constants.IMAGE)?Constants.PHOTO_STORAGE:Constants.VIDEOS_STORAGE;

        String fileName = messageType.equals(Constants.IMAGE)?pushId+".jpg":pushId+".mp4";

        StorageReference fileRef = storageReference.child(folderName).child(fileName);

        UploadTask uploadTask = fileRef.putBytes(bytes.toByteArray());

        uploadProgress(uploadTask,fileRef,pushId,messageType);

    }

    private void uploadProgress(UploadTask task,StorageReference filePath,String pushId,String messageType)
    {
        View view = getLayoutInflater().inflate(R.layout.file_progress,null);

        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        TextView txt = view.findViewById(R.id.tv_file_progress);
        ImageView pause = view.findViewById(R.id.pause_file);
        ImageView play = view.findViewById(R.id.play_file);
        ImageView stop = view.findViewById(R.id.stop_file);

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                task.pause();
                play.setVisibility(View.VISIBLE);
                pause.setVisibility(View.GONE);
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                task.resume();
                play.setVisibility(View.GONE);
                pause.setVisibility(View.VISIBLE);
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                task.cancel();
            }
        });

        llprogress.addView(view);

        txt.setText("Uploading "+messageType);

        task.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progress  = (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());

                progressBar.setProgress((int)progress);
                txt.setText("Uploading "+messageType+" "+String.valueOf(progressBar.getProgress()+"%"));
            }
        });

        task.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                llprogress.removeView(view);
                if(task.isSuccessful())
                {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String downloadUrl = uri.toString();
                            sendMessage(downloadUrl,messageType,pushId);
                        }
                    });
                }
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                llprogress.removeView(view);
                Toast.makeText(ChatActivity.this, "Failed to upload "+messageType, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==1)
        {
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {
                if(bottomSheetDialog!=null)
                {
                    bottomSheetDialog.show();
                }
                else
                {
                    Toast.makeText(this, "Permissions required to access files", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId)
        {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void deleteMessage(String messageId, String messageType)
    {
        DatabaseReference ref = databaseReference.child(NodeNames.MESSAGES).child(currentUserId).child(chatUserId).child(messageId);

        ref.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    DatabaseReference ref1 = databaseReference.child(NodeNames.MESSAGES).child(chatUserId).child(currentUserId).child(messageId);

                    ref1.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task1) {
                            if(task1.isSuccessful())
                            {
                                Toast.makeText(ChatActivity.this, "Message Deleted", Toast.LENGTH_SHORT).show();

                                if((messageType !=Constants.TEXT))
                                {
                                    StorageReference rootRef = FirebaseStorage.getInstance().getReference();

                                    String folder = messageType.equals(Constants.VIDEO)?Constants.VIDEOS_STORAGE:Constants.PHOTO_STORAGE;

                                    String folderName = messageType.equals(Constants.VIDEO)?messageId+".mp4":messageId+".jpg";

                                    StorageReference fileref = rootRef.child(folder).child(folderName);

                                    fileref.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {

                                            }
                                            else
                                            {
                                                Toast.makeText(ChatActivity.this, "Failed to delete file "+task.getException(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            }
                            else
                            {
                                Toast.makeText(ChatActivity.this, "Failed deleting message", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else
                {
                    Toast.makeText(ChatActivity.this, "Failed deleting message", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void downloadFile(String messageId, String messageType, Boolean isShare)
    {
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},2);
        }
        else
        {
            String folderName = messageType.equals(Constants.VIDEO)?Constants.VIDEOS_STORAGE:Constants.PHOTO_STORAGE;

            String filename = messageType.equals(Constants.VIDEO)?messageId+".mp4":messageId+".jpeg";

            StorageReference fileRef = FirebaseStorage.getInstance().getReference().child(folderName).child(filename);

            String FileName = messageType+".jpeg";

            String localFileRef = getExternalFilesDir(null).getAbsolutePath()+"/"+FileName;

            File localFile = new File(localFileRef);

            try {

                if(localFile.exists() || localFile.createNewFile())
                {
                    FileDownloadTask downloadTask = fileRef.getFile(localFile);

                    View view = getLayoutInflater().inflate(R.layout.file_progress,null);

                    ProgressBar progressBar = view.findViewById(R.id.progressBar);
                    TextView txt = view.findViewById(R.id.tv_file_progress);
                    ImageView pause = view.findViewById(R.id.pause_file);
                    ImageView play = view.findViewById(R.id.play_file);
                    ImageView stop = view.findViewById(R.id.stop_file);

                    pause.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            downloadTask.pause();
                            play.setVisibility(View.VISIBLE);
                            pause.setVisibility(View.GONE);
                        }
                    });

                    play.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            downloadTask.resume();
                            play.setVisibility(View.GONE);
                            pause.setVisibility(View.VISIBLE);
                        }
                    });

                    stop.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            downloadTask.cancel();
                        }
                    });

                    llprogress.addView(view);

                    txt.setText("Downloading "+messageType);

                    downloadTask.addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull FileDownloadTask.TaskSnapshot taskSnapshot) {
                            double progress  = (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());

                            progressBar.setProgress((int)progress);
                            txt.setText("Downloading "+messageType+" "+String.valueOf(progressBar.getProgress()+"%"));
                        }
                    });

                    downloadTask.addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                            llprogress.removeView(view);
                            if(task.isSuccessful())
                            {
                                if(isShare)
                                {
                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_SEND);
                                    intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(localFileRef));


                                    if(messageType.equals(Constants.VIDEO))
                                    {
                                        intent.setType("video/mp4");
                                    }
                                    else if(messageType.equals(Constants.IMAGE))
                                    {
                                        intent.setType("image/jpeg");
                                    }

                                    startActivity(Intent.createChooser(intent,"Share with.."));
                                }
                                else {
                                    Snackbar snackbar = Snackbar.make(llprogress, getString(R.string.download_success),
                                            Snackbar.LENGTH_INDEFINITE);

                                    snackbar.setAction("View", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Uri uri = Uri.parse(localFileRef);
                                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                            if (messageType.equals(Constants.VIDEO)) {
                                                intent.setDataAndType(uri, "video/mp4");
                                            } else if (messageType.equals(Constants.IMAGE)) {
                                                intent.setDataAndType(uri, "image/jpeg");
                                            }

                                            startActivity(intent);
                                        }
                                    });

                                    snackbar.show();
                                }
                            }
                        }
                    });

                    downloadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            llprogress.removeView(view);
                            Toast.makeText(ChatActivity.this, "Failed to download "+messageType, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else
                {
                    Toast.makeText(ChatActivity.this, "Couldn't store the file ", Toast.LENGTH_SHORT).show();
                }

            }catch (Exception e)
            {
                Toast.makeText(ChatActivity.this, "Some error occurred", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void forwardMessage(String seletedMsgId, String seletedMsg, String seletedMsgType) {
        Intent intent = new Intent(this, select_friend.class);
        intent.putExtra(Extras.MESSAGE,seletedMsg);
        intent.putExtra(Extras.MESSAGE_ID,seletedMsgId);
        intent.putExtra(Extras.MESSAGE_TYPE,seletedMsgType);

        startActivityForResult(intent,REQUEST_CODE_FORWARD_MESSAGE);

    }
}