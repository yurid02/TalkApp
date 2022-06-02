package com.example.talkapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.talkapp.R;
import com.example.talkapp.models.ChatMessage;

import java.util.ArrayList;

public class ChatMessagesAdapter extends RecyclerView.Adapter<ChatMessagesAdapter.ChatMessagesViewHolder> {

    ArrayList<ChatMessage> chatMessageArrayList;

    public ChatMessagesAdapter(ArrayList<ChatMessage> chatMessageArrayList) {
        this.chatMessageArrayList = chatMessageArrayList;
    }

    @NonNull
    @Override
    public ChatMessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ChatMessagesViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ChatMessagesViewHolder holder, int position) {

        ChatMessage currentMessage = chatMessageArrayList.get(position);

        boolean isPhoto = currentMessage.getPhotoUrl() != null;


        if (isPhoto){
            holder.tvMessage.setVisibility(View.GONE);
            holder.ivPhoto.setVisibility(View.VISIBLE);
            Glide.with(holder.ivPhoto.getContext())
                    .load(currentMessage.getPhotoUrl())
                    .into(holder.ivPhoto);
        }else{
            holder.tvMessage.setVisibility(View.VISIBLE);
            holder.ivPhoto.setVisibility(View.GONE);
            holder.tvMessage.setText(currentMessage.getText());
        }
        holder.tvName.setText(currentMessage.getName());

    }

    @Override
    public int getItemCount() {
        return chatMessageArrayList.size();
    }

    static class ChatMessagesViewHolder extends RecyclerView.ViewHolder {

        ImageView ivPhoto;
        TextView tvMessage, tvName;

        public ChatMessagesViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvName = itemView.findViewById(R.id.tvName);
        }
    }
}

