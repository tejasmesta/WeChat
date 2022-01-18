package com.example.wechat.findfriends;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FindFriendsFragment extends Fragment {

    private RecyclerView rvFindFriends;
    private View progressBar;
    private TextView findFriendssText;
    private FindFriendsAdapter findFriendsAdapter;
    private List<FindFriendsModelClass> findFriendsModelClassList;
    private DatabaseReference databaseReference, friendRequestsDatabaseReference;
    private FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();


    public FindFriendsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_find_friends, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvFindFriends = view.findViewById(R.id.friendsRecyclerView);
        progressBar = view.findViewById(R.id.find_friend_progress);
        findFriendssText = view.findViewById(R.id.friendsTextView);

        rvFindFriends.setLayoutManager(new LinearLayoutManager(getActivity()));

        findFriendsModelClassList = new ArrayList<>();

        findFriendsAdapter = new FindFriendsAdapter(getActivity(),findFriendsModelClassList);

        rvFindFriends.setAdapter(findFriendsAdapter);

        databaseReference = FirebaseDatabase.getInstance("https://wechat-da973-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child(NodeNames.USERS);

        friendRequestsDatabaseReference = FirebaseDatabase.getInstance("https://wechat-da973-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child(NodeNames.FRIEND_REQUESTS).child(firebaseUser.getUid());


        findFriendssText.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        //Query query = databaseReference.orderByChild(NodeNames.NAME);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                findFriendsModelClassList.clear();




                for(DataSnapshot ds : dataSnapshot.getChildren())
                {
                        String userId = ds.getKey();

                        if (userId.equals(firebaseUser.getUid())) {
                            continue;
                        }
                        else {

                            final String username = ds.child(NodeNames.NAME).getValue().toString();
                            final String photo = ds.child(NodeNames.PHOTO).getValue().toString();

                            friendRequestsDatabaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                                    if (dataSnapshot1.exists()) {
                                        String requestType = dataSnapshot1.child(NodeNames.REQUEST_TYPE).getValue().toString();
                                        if (requestType.equals(Constants.REQUEST_STATUS_SENT)) {

                                            findFriendsModelClassList.add(new FindFriendsModelClass(username, photo, userId, true));

                                            findFriendsAdapter.notifyDataSetChanged();
                                        }
                                    } else {

                                        findFriendsModelClassList.add(new FindFriendsModelClass(username, photo, userId, false));

                                        findFriendsAdapter.notifyDataSetChanged();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                    Toast.makeText(getActivity(), "Error fetching the list " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                        }

                }




            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                progressBar.setVisibility(View.GONE);

                findFriendssText.setVisibility(View.GONE);

                Toast.makeText(getActivity(), "Error fetching the list "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        progressBar.setVisibility(View.GONE);

        findFriendssText.setVisibility(View.GONE);

    }
}