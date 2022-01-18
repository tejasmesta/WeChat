package com.example.wechat.chat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.appcompat.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.wechat.R;
import com.example.wechat.common.Constants;
import com.example.wechat.selectFriends.select_friend;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHodler>{

    private Context context;
    private List<MessageModel> messageModelList;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private ActionMode actionMode;

    private LinearLayout linearLayout;

    public MessagesAdapter(Context context, List<MessageModel> messageModelList) {
        this.context = context;
        this.messageModelList = messageModelList;
    }

    @NonNull
    @Override
    public MessagesAdapter.MessageViewHodler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.message_convo_layout,parent,false);
        return new MessageViewHodler(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessagesAdapter.MessageViewHodler holder, int position) {
        MessageModel messageModel = messageModelList.get(position);

        firebaseAuth = FirebaseAuth.getInstance();

        String currentId = firebaseAuth.getCurrentUser().getUid();

        String fromId = messageModel.getMessageFrom();

        SimpleDateFormat sfd = new SimpleDateFormat("dd-MM-yyyy HH:mm");

        String dateTime = sfd.format(new Date(messageModel.getMessageTime()));

        String[] split = dateTime.split(" ");

        String messageTime = split[1];

        if(currentId.equals(fromId))
        {
            if(messageModel.getMessageType().equals(Constants.TEXT))
            {
                holder.llsent.setVisibility(View.VISIBLE);
                holder.llrcvd.setVisibility(View.GONE);
                holder.sentImage.setVisibility(View.GONE);
                holder.sentT.setVisibility(View.GONE);
                holder.sentMsg.setText(messageModel.getMessage());
                holder.sentTime.setText(messageTime);
            }
            else
            {
                holder.llsent.setVisibility(View.VISIBLE);
                holder.llrcvd.setVisibility(View.GONE);
                holder.sentImage.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(messageModel.getMessage())
                        .placeholder(R.drawable.ic_baseline_image_24)
                        .error(R.drawable.ic_baseline_image_24)
                        .into(holder.sentImage);
                holder.sentT.setVisibility(View.VISIBLE);
                holder.sentT.setText(messageTime);
                holder.sentMsg.setVisibility(View.GONE);
                holder.sentTime.setVisibility(View.GONE);
            }
        }
        else
        {
            if(messageModel.getMessageType().equals(Constants.TEXT))
            {
                holder.llsent.setVisibility(View.GONE);
                holder.llrcvd.setVisibility(View.VISIBLE);
                holder.rcvdImage.setVisibility(View.GONE);
                holder.rcvdT.setVisibility(View.GONE);
                holder.rcvdMsg.setText(messageModel.getMessage());
                holder.rcvdT.setText(messageTime);
            }
            else
            {
                holder.llsent.setVisibility(View.GONE);
                holder.llrcvd.setVisibility(View.VISIBLE);
                holder.rcvdImage.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(messageModel.getMessage())
                        .placeholder(R.drawable.ic_baseline_image_24)
                        .error(R.drawable.ic_baseline_image_24)
                        .into(holder.rcvdImage);
                holder.rcvdT.setVisibility(View.VISIBLE);
                holder.rcvdT.setText(messageTime);
                holder.rcvdMsg.setVisibility(View.GONE);
                holder.rcvdTime.setVisibility(View.GONE);
            }
        }

        holder.mainll.setTag(R.id.TAG_MESSAGE,messageModel.getMessage());
        holder.mainll.setTag(R.id.TAG_MESSAGE_ID,messageModel.getMessage_id());
        holder.mainll.setTag(R.id.TAG_MESSAGE_TYPE,messageModel.getMessageType());

        holder.mainll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String type = view.getTag(R.id.TAG_MESSAGE_TYPE).toString();

                Uri uri = Uri.parse(view.getTag(R.id.TAG_MESSAGE).toString());

                if(type.equals(Constants.VIDEO))
                {
                    Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                    intent.setDataAndType(uri,"video/mp4");
                    context.startActivity(intent);
                }
                else if(type.equals(Constants.VIDEO))
                {
                    Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                    intent.setDataAndType(uri,"image/jpg");
                    context.startActivity(intent);
                }
            }
        });

        holder.mainll.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(actionMode!=null)
                {
                    return false;
                }

                linearLayout = holder.mainll;

                actionMode = ((AppCompatActivity)context).startSupportActionMode(actionmodeCallback);

                holder.mainll.setBackgroundColor(context.getResources().getColor(R.color.olive));

                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return messageModelList.size();
    }

    public class MessageViewHodler extends RecyclerView.ViewHolder{

        private LinearLayout llsent, llrcvd, mainll;
        private TextView sentMsg, rcvdMsg, sentTime, rcvdTime, sentT, rcvdT;
        private ImageView sentImage, rcvdImage;

        public MessageViewHodler(@NonNull View itemView) {
            super(itemView);

            mainll = itemView.findViewById(R.id.mainLayout);
            llsent = itemView.findViewById(R.id.message_convo_sent_ll);
            llrcvd = itemView.findViewById(R.id.message_convo_rcvd_ll);
            sentMsg = itemView.findViewById(R.id.convo_sent_message);
            rcvdMsg = itemView.findViewById(R.id.convo_rcvd_message);
            sentTime = itemView.findViewById(R.id.convo_sent_message_time);
            rcvdTime = itemView.findViewById(R.id.convo_rcvd_message_time);
            sentImage = itemView.findViewById(R.id.convo_sent_photo);
            rcvdImage = itemView.findViewById(R.id.convo_rcvd_photo);
            sentT = itemView.findViewById(R.id.convo_sent_message_time);
            rcvdT = itemView.findViewById(R.id.convo_rcvd_message_time);
        }
    }

    private ActionMode.Callback actionmodeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            MenuInflater menuInflater = actionMode.getMenuInflater();

            String selectedMessageType = (String) linearLayout.getTag(R.id.TAG_MESSAGE_TYPE);



            menuInflater.inflate(R.menu.menu_chat_options,menu);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {

            String seletedMsgId = (String) linearLayout.getTag(R.id.TAG_MESSAGE_ID);
            String seletedMsg = (String) linearLayout.getTag(R.id.TAG_MESSAGE);
            String seletedMsgType = (String) linearLayout.getTag(R.id.TAG_MESSAGE_TYPE);

            int itemId = menuItem.getItemId();

            switch (itemId)
            {
                case R.id.delete_message:
                    if(context instanceof ChatActivity)
                    {
                        ((ChatActivity)context).deleteMessage(seletedMsgId,seletedMsgType);
                    }
                    actionMode.finish();
                    break;

                case R.id.forward_message:
                    if(context instanceof ChatActivity)
                    {
                        ((ChatActivity)context).forwardMessage(seletedMsgId,seletedMsg,seletedMsgType);
                    }
                    actionMode.finish();
                    break;

                case R.id.share_message:
                    if(seletedMsgType.equals(Constants.TEXT))
                    {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_SEND);
                        intent.putExtra(Intent.EXTRA_TEXT,seletedMsg);
                        intent.setType("text/plain");
                        context.startActivity(intent);
                    }
                    else
                    {
                        if(context instanceof ChatActivity)
                        {
                            ((ChatActivity)context).downloadFile(seletedMsgId,seletedMsgType,true);
                        }
                    }
                    actionMode.finish();
                    break;

                case R.id.download:
                    if(context instanceof ChatActivity)
                    {
                        ((ChatActivity)context).downloadFile(seletedMsgId,seletedMsgType,false);
                    }
                    actionMode.finish();
                    break;
                    
            }

            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            actionMode = null;
            linearLayout.setBackgroundColor(context.getResources().getColor(R.color.chatColor));
        }
    };
}
