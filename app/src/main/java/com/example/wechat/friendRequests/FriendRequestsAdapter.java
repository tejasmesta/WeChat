package com.example.wechat.friendRequests;

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
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Node;

import java.util.List;

public class FriendRequestsAdapter extends RecyclerView.Adapter<FriendRequestsAdapter.RequestViewHolder> {
    private Context context;
    private List<FriendRequestsModelClass> list;
    private DatabaseReference databaseReference, chatsDatabase;
    private FirebaseUser currentUser;

    public FriendRequestsAdapter(Context context, List<FriendRequestsModelClass> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public FriendRequestsAdapter.RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.friend_requests_layout,parent,false);
        return new RequestViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendRequestsAdapter.RequestViewHolder holder, int position) {

        FriendRequestsModelClass friendRequestsModelClass = list.get(position);

        holder.fullname.setText(friendRequestsModelClass.getUserName());

        StorageReference fileref = FirebaseStorage.getInstance().getReference().child(Constants.IMAGES_STORAGE+"/"+friendRequestsModelClass.getUserId()+".jpg");

        fileref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context)
                        .load(uri)
                        .placeholder(R.drawable.deafault_avatar)
                        .into(holder.profile);
            }
        });

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance("https://wechat-da973-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child(NodeNames.FRIEND_REQUESTS);


        holder.decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.decline.setVisibility(View.GONE);
                holder.accept.setVisibility(View.GONE);

                final String userId = friendRequestsModelClass.getUserId();

                databaseReference.child(currentUser.getUid()).child(userId).child(NodeNames.REQUEST_TYPE).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            databaseReference.child(userId).child(currentUser.getUid()).child(NodeNames.REQUEST_TYPE).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task1) {
                                    if(task1.isSuccessful())
                                    {
                                        Toast.makeText(context, "Request denied successfully", Toast.LENGTH_SHORT).show();
                                        holder.decline.setVisibility(View.GONE);
                                        holder.accept.setVisibility(View.GONE);
                                    }
                                    else
                                    {
                                        Toast.makeText(context, "Failed to decline request "+task.getException(), Toast.LENGTH_SHORT).show();
                                        holder.decline.setVisibility(View.VISIBLE);
                                        holder.accept.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                        }
                        else
                        {
                            Toast.makeText(context, "Failed to decline request "+task.getException(), Toast.LENGTH_SHORT).show();
                            holder.decline.setVisibility(View.VISIBLE);
                            holder.accept.setVisibility(View.VISIBLE);
                        }
                    }
                });

            }
        });

        chatsDatabase = FirebaseDatabase.getInstance("https://wechat-da973-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child(NodeNames.CHAT);

        holder.accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.decline.setVisibility(View.GONE);
                holder.accept.setVisibility(View.GONE);

                final String userId = friendRequestsModelClass.getUserId();

                chatsDatabase.child(currentUser.getUid()).child(userId).child(NodeNames.TIMESTAMP).setValue(ServerValue.TIMESTAMP).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            chatsDatabase.child(userId).child(currentUser.getUid()).child(NodeNames.TIMESTAMP).setValue(ServerValue.TIMESTAMP).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task1) {
                                    if(task1.isSuccessful())
                                    {
                                        databaseReference.child(currentUser.getUid()).child(userId).child(NodeNames.REQUEST_TYPE).setValue(Constants.REQUEST_STATUS_ACCEPTED).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task2) {
                                                if(task2.isSuccessful())
                                                {
                                                    databaseReference.child(userId).child(currentUser.getUid()).child(NodeNames.REQUEST_TYPE).setValue(Constants.REQUEST_STATUS_ACCEPTED).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task3) {
                                                            if(task3.isSuccessful())
                                                            {
                                                                holder.decline.setVisibility(View.GONE);
                                                                holder.accept.setVisibility(View.GONE);
                                                                Toast.makeText(context, "You are friends now", Toast.LENGTH_SHORT).show();
                                                            }
                                                            else
                                                            {
                                                                holder.decline.setVisibility(View.VISIBLE);
                                                                holder.accept.setVisibility(View.VISIBLE);
                                                                Toast.makeText(context, "Failed to accept request "+task3.getException(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                                }
                                                else
                                                {
                                                    holder.decline.setVisibility(View.VISIBLE);
                                                    holder.accept.setVisibility(View.VISIBLE);
                                                    Toast.makeText(context, "Failed to accept request "+task2.getException(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                    else
                                    {
                                        holder.decline.setVisibility(View.VISIBLE);
                                        holder.accept.setVisibility(View.VISIBLE);
                                        Toast.makeText(context, "Failed to accept request "+task.getException(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                        else
                        {
                            holder.decline.setVisibility(View.VISIBLE);
                            holder.accept.setVisibility(View.VISIBLE);
                            Toast.makeText(context, "Failed to accept request", Toast.LENGTH_SHORT).show();
                        }
                    }
                });




            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class RequestViewHolder extends RecyclerView.ViewHolder {
        private TextView fullname;
        private ImageView profile;
        private Button accept;
        private Button decline;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            fullname = itemView.findViewById(R.id.friend_requests_page_username);
            profile = itemView.findViewById(R.id.friend_requests_page_profile);
            accept = itemView.findViewById(R.id.friend_requests_page_accept_button);
            decline = itemView.findViewById(R.id.friend_requests_page_decline_button);
        }
    }
}
