package com.example.wechat.chat;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wechat.R;
import com.example.wechat.common.NodeNames;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {
    private DatabaseReference chatDatabaseReference, usersDatabaseRef;
    private RecyclerView rvChat;
    private FirebaseUser currentUser;
    private View progressBar;
    private TextView emptyText;
    private ChatListAdapter adapter;
    private List<ChatModelList> chatModelListList;

    private ChildEventListener childEventListener;
    private Query query;


    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvChat = view.findViewById(R.id.chatsRecyclerView);
        emptyText = view.findViewById(R.id.chatTextView);
        progressBar = view.findViewById(R.id.chat_progress);
        chatModelListList = new ArrayList<>();
        adapter = new ChatListAdapter(getActivity(), chatModelListList);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());

        linearLayoutManager.setReverseLayout(true);

        linearLayoutManager.setStackFromEnd(true);

        rvChat.setLayoutManager(linearLayoutManager);

        rvChat.setAdapter(adapter);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        usersDatabaseRef = FirebaseDatabase.getInstance("https://wechat-da973-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child(NodeNames.USERS);

        chatDatabaseReference = FirebaseDatabase.getInstance("https://wechat-da973-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child(NodeNames.CHAT).child(currentUser.getUid());

        query =  chatDatabaseReference.orderByChild(NodeNames.TIMESTAMP);

        progressBar.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.VISIBLE);

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    updateList(dataSnapshot, true, dataSnapshot.getKey());

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

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

    private void updateList(DataSnapshot dataSnapshot,boolean isNew,String userId)
    {


        progressBar.setVisibility(View.GONE);
        emptyText.setVisibility(View.GONE);

        final String lastMessage;
        final String lastMessageTime;
        final String unreadCount;

        lastMessage = "";
        lastMessageTime = "";
        unreadCount = "";

        usersDatabaseRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child(NodeNames.NAME).getValue().toString();

                    String photo = dataSnapshot.child(NodeNames.PHOTO).getValue().toString();

                    ChatModelList chatModelList = new ChatModelList(userId, name, photo, unreadCount, lastMessage, lastMessageTime);

                    chatModelListList.add(chatModelList);

                    adapter.notifyDataSetChanged();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Failed to fetch chat list " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        query.removeEventListener(childEventListener);
    }
}