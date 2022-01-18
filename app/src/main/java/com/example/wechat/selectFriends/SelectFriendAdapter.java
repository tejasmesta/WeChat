package com.example.wechat.selectFriends;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.wechat.R;
import com.example.wechat.common.Constants;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class SelectFriendAdapter extends RecyclerView.Adapter<SelectFriendAdapter.SelectFriendViewHolder> {

    private Context context;
    private List<SelectFriendModel> selectFriendModelList;

    public SelectFriendAdapter(Context context, List<SelectFriendModel> selectFriendModelList) {
        this.context = context;
        this.selectFriendModelList = selectFriendModelList;
    }

    @NonNull
    @Override
    public SelectFriendAdapter.SelectFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.select_friend_rv,parent,false);
        return new SelectFriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectFriendAdapter.SelectFriendViewHolder holder, int position) {
        SelectFriendModel selectFriendModel = selectFriendModelList.get(position);

        holder.rvUsername.setText(selectFriendModel.getUsername());

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(Constants.IMAGES_STORAGE+"/"+selectFriendModel.getUserId()+".jpg");

        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context)
                        .load(uri)
                        .placeholder(R.drawable.deafault_avatar)
                        .error(R.drawable.deafault_avatar)
                        .into(holder.rvProfile);
            }
        });

        holder.llselectfriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(context instanceof select_friend)
                {
                    ((select_friend) context).returnSelectedFrnd(selectFriendModel.getUserId(),selectFriendModel.getUsername(),selectFriendModel.getUserId()+".jpg");


                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return selectFriendModelList.size();
    }

    public class SelectFriendViewHolder extends RecyclerView.ViewHolder{

        private LinearLayout llselectfriend;
        private ImageView rvProfile;
        private TextView rvUsername;

        public SelectFriendViewHolder(@NonNull View itemView) {
            super(itemView);

            llselectfriend = itemView.findViewById(R.id.selectFriendLinear);
            rvProfile = itemView.findViewById(R.id.selectFriendProfile);
            rvUsername = itemView.findViewById(R.id.selectFriendusername);
        }
    }
}
