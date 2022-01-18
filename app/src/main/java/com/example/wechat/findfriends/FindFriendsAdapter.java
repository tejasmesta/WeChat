package com.example.wechat.findfriends;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.wechat.R;
import com.example.wechat.common.Constants;
import com.example.wechat.common.NodeNames;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class FindFriendsAdapter extends RecyclerView.Adapter<FindFriendsAdapter.FindFriendViewHolder> {

    private Context context;
    private List<FindFriendsModelClass> findFriendsModelClassList;
    private DatabaseReference friendDatabaseReference;
    private FirebaseUser currentUser;
    private String userId;

    public FindFriendsAdapter(Context context, List<FindFriendsModelClass> findFriendsModelClassList) {
        this.context = context;
        this.findFriendsModelClassList = findFriendsModelClassList;
    }

    @NonNull
    @Override
    public FindFriendsAdapter.FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.find_friends_layout,parent,false);
        return new FindFriendViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FindFriendsAdapter.FindFriendViewHolder holder, int position) {
        FindFriendsModelClass friendsModelClass = findFriendsModelClassList.get(position);

        holder.username.setText(friendsModelClass.getUsername());

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(Constants.IMAGES_STORAGE+"/"+friendsModelClass.getUserId()+".jpg");

        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context)
                        .load(uri)
                        .placeholder(R.drawable.deafault_avatar)
                        .error(R.drawable.deafault_avatar)
                        .into(holder.profile);
            }
        });






        friendDatabaseReference = FirebaseDatabase.getInstance("https://wechat-da973-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child(NodeNames.FRIEND_REQUESTS);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if(friendsModelClass.getRequestSent())
        {
            holder.sendRequest.setText("Request Sent");
            holder.sendRequest.setOnClickListener(null);
            holder.cancelRequest.setVisibility(View.VISIBLE);
        }
        else
        {
            holder.sendRequest.setVisibility(View.VISIBLE);
            holder.sendRequest.setText("Send Request");
            holder.cancelRequest.setVisibility(View.GONE);
        }

        holder.sendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.sendRequest.setText("Sending ->");

                userId = friendsModelClass.getUserId();

                friendDatabaseReference.child(currentUser.getUid()).child(userId).child(NodeNames.REQUEST_TYPE)
                        .setValue(Constants.REQUEST_STATUS_SENT)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful())
                                {
                                    friendDatabaseReference.child(userId).child(currentUser.getUid()).child(NodeNames.REQUEST_TYPE)
                                            .setValue(Constants.REQUEST_STATUS_RECEIVED)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task1) {
                                                    if(task1.isSuccessful())
                                                    {
                                                        Toast.makeText(context, "Request Sent Successfully", Toast.LENGTH_SHORT).show();
                                                        holder.sendRequest.setText("Request Sent");
                                                        holder.sendRequest.setOnClickListener(null);
                                                        holder.cancelRequest.setVisibility(View.VISIBLE);
                                                    }
                                                    else
                                                    {
                                                        Toast.makeText(context, "Request Failed "+task1.getException(), Toast.LENGTH_SHORT).show();
                                                        holder.cancelRequest.setVisibility(View.GONE);
                                                        holder.sendRequest.setText("Send Request");
                                                    }
                                                }
                                            });
                                }
                                else
                                {
                                    Toast.makeText(context, "Request Failed "+task.getException(), Toast.LENGTH_SHORT).show();
                                    holder.cancelRequest.setVisibility(View.GONE);
                                    holder.sendRequest.setText("Send Request");
                                }
                            }
                        });
            }
        });

        holder.cancelRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                userId = friendsModelClass.getUserId();

                friendDatabaseReference.child(currentUser.getUid()).child(userId).child(NodeNames.REQUEST_TYPE)
                        .setValue(null)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful())
                                {
                                    friendDatabaseReference.child(userId).child(currentUser.getUid()).child(NodeNames.REQUEST_TYPE)
                                            .setValue(null)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task1) {
                                                    if(task1.isSuccessful())
                                                    {
                                                        Toast.makeText(context, "Request Cancelled Successfully", Toast.LENGTH_SHORT).show();
                                                        holder.sendRequest.setText("Send Request");
                                                        holder.cancelRequest.setVisibility(View.GONE);
                                                    }
                                                    else
                                                    {
                                                        Toast.makeText(context, "Cancelling Request Failed "+task1.getException(), Toast.LENGTH_SHORT).show();
                                                        holder.sendRequest.setText("Request Sent");
                                                        holder.cancelRequest.setVisibility(View.VISIBLE);
                                                    }
                                                }
                                            });
                                }
                                else
                                {
                                    Toast.makeText(context, "Cancelling Request Failed "+task.getException(), Toast.LENGTH_SHORT).show();
                                    holder.sendRequest.setText("Request Sent");
                                    holder.cancelRequest.setVisibility(View.VISIBLE);
                                }
                            }
                        });
            }
        });

    }

    @Override
    public int getItemCount() {
        return findFriendsModelClassList.size();
    }

    public class FindFriendViewHolder extends RecyclerView.ViewHolder{

        private ImageView profile;
        private TextView username;
        private Button sendRequest, cancelRequest;

        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);

            profile = itemView.findViewById(R.id.find_friends_page_profile);
            username = itemView.findViewById(R.id.find_friends_page_username);
            sendRequest = itemView.findViewById(R.id.find_friends_page_request_button);
            cancelRequest = itemView.findViewById(R.id.find_friends_page_cancel_request_button);
        }
    }
}
