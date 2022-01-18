package com.example.wechat.chat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.wechat.R;
import com.example.wechat.common.Constants;
import com.example.wechat.common.Extras;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder> {
    private Context context;
    private List<ChatModelList> list;

    public ChatListAdapter(Context context, List<ChatModelList> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ChatListAdapter.ChatListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_list_layout,parent,false);
        return new ChatListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatListAdapter.ChatListViewHolder holder, int position) {
        ChatModelList chatModelList = list.get(position);

        holder.fullname.setText(chatModelList.getUsername());

        StorageReference fileref = FirebaseStorage.getInstance().getReference().child(Constants.IMAGES_STORAGE+"/"+chatModelList.getUserId()+".jpg");

        fileref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context)
                        .load(uri)
                        .placeholder(R.drawable.deafault_avatar)
                        .error(R.drawable.deafault_avatar)
                        .into(holder.profile);
            }
        });

        holder.chatList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,ChatActivity.class);

                intent.putExtra(Extras.User_Key,chatModelList.getUserId());

                context.startActivity(intent);
            }
        });


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ChatListViewHolder extends RecyclerView.ViewHolder{
        private LinearLayout chatList;
        private TextView fullname;
        private TextView lastMessage;
        private TextView lastMessageTime;
        private TextView unreadCount;
        private ImageView profile;

        public ChatListViewHolder(@NonNull View itemView) {
            super(itemView);

            fullname = itemView.findViewById(R.id.chat_list_username);
            lastMessage = itemView.findViewById(R.id.chat_list_last_message);
            lastMessageTime = itemView.findViewById(R.id.last_message_time);
            unreadCount = itemView.findViewById(R.id.chatslist_unread_count);
            profile = itemView.findViewById(R.id.chat_list_profile);
            chatList = itemView.findViewById(R.id.chatListlinear);
        }
    }
}
