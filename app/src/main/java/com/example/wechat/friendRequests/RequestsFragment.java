package com.example.wechat.friendRequests;

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
import com.example.wechat.common.Constants;
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

public class RequestsFragment extends Fragment {

    private RecyclerView rvRequests;
    private FriendRequestsAdapter adapter;
    private List<FriendRequestsModelClass> list;
    private View progressBar;
    private TextView noPedingTextView;
    private DatabaseReference databaseReferenceRequests, databaseReferenceUsers;
    private FirebaseUser currentUser;

    public RequestsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_requests, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvRequests = view.findViewById(R.id.requestsRecyclerView);
        progressBar = view.findViewById(R.id.friend_requests_progress);
        noPedingTextView = view.findViewById(R.id.requestsTextView);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        list = new ArrayList<FriendRequestsModelClass>();

        adapter = new FriendRequestsAdapter(getActivity(),list);

        rvRequests.setLayoutManager(new LinearLayoutManager(getActivity()));



        rvRequests.setAdapter(adapter);

        databaseReferenceUsers = FirebaseDatabase.getInstance("https://wechat-da973-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child(NodeNames.USERS);

        databaseReferenceRequests = FirebaseDatabase.getInstance("https://wechat-da973-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child(NodeNames.FRIEND_REQUESTS).child(currentUser.getUid());

        noPedingTextView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        databaseReferenceRequests.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);

                list.clear();

                for(DataSnapshot ds: dataSnapshot.getChildren())
                {
                    if(ds.exists())
                    {
                        String requestType = ds.child(NodeNames.REQUEST_TYPE).getValue().toString();

                        if(requestType.equals(Constants.REQUEST_STATUS_RECEIVED))
                        {
                            String userId = ds.getKey();

                            databaseReferenceUsers.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String userName = dataSnapshot.child(NodeNames.NAME).getValue().toString();

                                    String photoName = "";

                                    if(dataSnapshot.child(NodeNames.PHOTO)!=null)
                                    {
                                        photoName = dataSnapshot.child(NodeNames.PHOTO).getValue().toString();
                                    }

                                    FriendRequestsModelClass friendRequestsModelClass = new FriendRequestsModelClass(userId,userName,photoName);

                                    list.add(friendRequestsModelClass);

                                    adapter.notifyDataSetChanged();

                                    noPedingTextView.setVisibility(View.GONE);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(getActivity(), "Failed to fetch friend requests "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getActivity(), "Failed to fetch friend requests "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}