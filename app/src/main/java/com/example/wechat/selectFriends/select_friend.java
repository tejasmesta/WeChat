package com.example.wechat.selectFriends;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.wechat.R;
import com.example.wechat.common.Extras;
import com.example.wechat.common.NodeNames;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class select_friend extends AppCompatActivity {

    private RecyclerView rvSelectFriends;
    private SelectFriendAdapter selectFriendAdapter;
    private List<SelectFriendModel> selectFriendModelList;
    private View progressBar;
    private DatabaseReference UsersDatabaseReference, chatsDatabaseReference;
    private FirebaseUser currentUser;
    private FirebaseAuth firebaseAuth;
    private ValueEventListener valueEventListener;
    private String selectedMsg, selectedMsgId, selectedMsgType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_friend);

        if(getIntent().hasExtra(Extras.MESSAGE))
        {
            selectedMsg = getIntent().getStringExtra(Extras.MESSAGE);
            selectedMsgId = getIntent().getStringExtra(Extras.MESSAGE_ID);
            selectedMsgType = getIntent().getStringExtra(Extras.MESSAGE_TYPE);
        }

        rvSelectFriends = findViewById(R.id.rvSelectFriend);
        selectFriendModelList = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvSelectFriends.setLayoutManager(linearLayoutManager);
        progressBar = findViewById(R.id.select_friend_progress);
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        UsersDatabaseReference = FirebaseDatabase.getInstance("https://wechat-da973-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child(NodeNames.USERS);
        chatsDatabaseReference = FirebaseDatabase.getInstance("https://wechat-da973-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child(NodeNames.CHAT).child(currentUser.getUid());
        selectFriendAdapter = new SelectFriendAdapter(this,selectFriendModelList);
        rvSelectFriends.setAdapter(selectFriendAdapter);
        progressBar.setVisibility(View.VISIBLE);

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren())
                {
                    String userId = ds.getKey();
                    UsersDatabaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String username = (String) dataSnapshot.child(NodeNames.NAME).getValue();

                            SelectFriendModel selectFriendModel = new SelectFriendModel(userId,username,userId+".jpg");

                            selectFriendModelList.add(selectFriendModel);
                            selectFriendAdapter.notifyDataSetChanged();

                            progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(select_friend.this, "Failed to fetch friends "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(select_friend.this, "Failed to fetch friends "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        chatsDatabaseReference.addValueEventListener(valueEventListener);
    }

    public void returnSelectedFrnd(String userId,String username,String photo)
    {
        chatsDatabaseReference.removeEventListener(valueEventListener);

        Intent intent = new Intent();

        intent.putExtra(Extras.User_Key,userId);
        intent.putExtra(Extras.User_name,username);
        intent.putExtra(Extras.User_photo,photo);

        intent.putExtra(Extras.MESSAGE,selectedMsg);
        intent.putExtra(Extras.MESSAGE_ID,selectedMsgId);
        intent.putExtra(Extras.MESSAGE_TYPE,selectedMsgType);

        setResult(Activity.RESULT_OK,intent);

        finish();
    }
}