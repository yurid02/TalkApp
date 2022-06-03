package com.example.talkapp.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.talkapp.R;
import com.example.talkapp.models.ChatMessage;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class ChatActivity extends AppCompatActivity {

    private ImageView ivSend, ivAddImage;
    private EditText etMessage;
    private RecyclerView rvChat;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mFirebaseAuth;
    private String userName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initVars();

        userName = mFirebaseAuth.getCurrentUser().getDisplayName();

        Query query = mFirebaseDatabase.getReference("messages");

        FirebaseRecyclerOptions<ChatMessage> options =
                new FirebaseRecyclerOptions.Builder<ChatMessage>()
                        .setQuery(query, ChatMessage.class)
                        .build();

        FirebaseRecyclerAdapter<ChatMessage, ChatMessagesViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<ChatMessage, ChatMessagesViewHolder>(options) {
            @NonNull
            @Override
            public ChatMessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new ChatMessagesViewHolder(
                        LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.item_message, parent, false)
                );
            }

            @Override
            protected void onBindViewHolder(@NonNull ChatMessagesViewHolder holder, int position, @NonNull ChatMessage model) {

                boolean isPhoto = model.getPhotoUrl() != null;

                if (isPhoto){
                    holder.tvMessage.setVisibility(View.GONE);
                    holder.ivPhoto.setVisibility(View.VISIBLE);
                    Glide.with(holder.ivPhoto.getContext())
                            .load(model.getPhotoUrl())
                            .into(holder.ivPhoto);
                }else{
                    holder.tvMessage.setVisibility(View.VISIBLE);
                    holder.ivPhoto.setVisibility(View.GONE);
                    holder.tvMessage.setText(model.getText());
                }
                holder.tvName.setText(model.getName());
            }
        };

        rvChat.setAdapter(firebaseRecyclerAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        rvChat.setLayoutManager(layoutManager);

        ivSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (etMessage.getText().toString().trim().length() > 0) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setText(etMessage.getText().toString());
                    chatMessage.setName(userName);
                    chatMessage.setTimestamp(System.currentTimeMillis());

                    mFirebaseDatabase.getReference("messages").push().setValue(chatMessage);

                    etMessage.setText("");
                }
            }
        });

        firebaseRecyclerAdapter.startListening();

    }

    private void initVars() {
        ivSend = findViewById(R.id.ivSend);
        ivAddImage = findViewById(R.id.ivAddImage);
        etMessage = findViewById(R.id.etMessage);
        rvChat = findViewById(R.id.rvChat);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = mFirebaseDatabase.getInstance();
//        mFirebaseStorage = FirebaseStorage.getInstance();
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