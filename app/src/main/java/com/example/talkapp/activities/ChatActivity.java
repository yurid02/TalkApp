package com.example.talkapp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.talkapp.R;
import com.example.talkapp.models.ChatMessage;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class ChatActivity extends AppCompatActivity {

    private ImageView ivSend, ivAddImage;
    private EditText etMessage;
    private RecyclerView rvChat;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseStorage mFirebaseStorage;
    private String userName;
    private DatabaseReference mDatabaseReference;
    private FirebaseRecyclerAdapter<ChatMessage, ChatMessagesViewHolder> mFirebaseRecyclerAdapter;
    private ChildEventListener mChildEventListener;
    private ActivityResultLauncher<Intent> uploadPhotoLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initVars();

        initListeners();

        rvChat.setAdapter(mFirebaseRecyclerAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvChat.setLayoutManager(layoutManager);

        attachDatabaseEventListener();

        mFirebaseRecyclerAdapter.startListening();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuSignOut:
                mFirebaseAuth.signOut();
                onSignedOutCleanup();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initVars() {
        ivSend = findViewById(R.id.ivSend);
        ivAddImage = findViewById(R.id.ivAddImage);
        etMessage = findViewById(R.id.etMessage);
        rvChat = findViewById(R.id.rvChat);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference("messages");
        userName = mFirebaseAuth.getCurrentUser().getDisplayName();

        Query query = mDatabaseReference;

        FirebaseRecyclerOptions<ChatMessage> options =
                new FirebaseRecyclerOptions.Builder<ChatMessage>()
                        .setQuery(query, ChatMessage.class)
                        .build();

        mFirebaseRecyclerAdapter =
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

                        if (isPhoto) {
                            holder.tvMessage.setVisibility(View.GONE);
                            holder.ivPhoto.setVisibility(View.VISIBLE);
                            Glide.with(holder.ivPhoto.getContext())
                                    .load(model.getPhotoUrl())
                                    .into(holder.ivPhoto);
                        } else {
                            holder.tvMessage.setVisibility(View.VISIBLE);
                            holder.ivPhoto.setVisibility(View.GONE);
                            holder.tvMessage.setText(model.getText());
                        }
                        holder.tvName.setText(model.getName());
                    }
                };

        uploadPhotoLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {
                            Uri selectedImageUri = result.getData().getData();
                            StorageReference photoRef =
                                    mFirebaseStorage.getReference().child(selectedImageUri.getLastPathSegment());
                            UploadTask uploadTask = photoRef.putFile(selectedImageUri);
                            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                @Override
                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                    if (!task.isSuccessful()) {
                                        throw task.getException();
                                    }

                                    // Continue with the task to get the download URL
                                    return photoRef.getDownloadUrl();
                                }
                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        Uri downloadUri = task.getResult();
                                        ChatMessage chatMessage =
                                                new ChatMessage(null, userName, downloadUri.toString(), System.currentTimeMillis());
                                        mDatabaseReference.push().setValue(chatMessage);
                                    } else {
                                        System.out.println(task.getException().toString());
                                    }
                                }
                            });
                        }
                    }
                }
        );

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


    private void attachDatabaseEventListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    rvChat.smoothScrollToPosition(mFirebaseRecyclerAdapter.getItemCount() + 1);
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
        }

        mFirebaseDatabase.getReference().addChildEventListener(mChildEventListener);
    }

    private void initListeners() {
        ivAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openPhotoPicker = new Intent();
                openPhotoPicker.setAction(Intent.ACTION_GET_CONTENT);
                openPhotoPicker.setType("image/jpeg");
                openPhotoPicker.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                uploadPhotoLauncher.launch(openPhotoPicker);
            }
        });

        ivSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (etMessage.getText().toString().trim().length() > 0) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setText(etMessage.getText().toString());
                    chatMessage.setName(userName);
                    chatMessage.setTimestamp(System.currentTimeMillis());

                    mDatabaseReference.push().setValue(chatMessage);

                    etMessage.setText("");
                }
            }
        });
    }

    private void onSignedOutCleanup() {
        userName = "ANONYMOUS";
        detachDatabaseListener();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void detachDatabaseListener() {
        if (mChildEventListener != null) {
            mDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

}