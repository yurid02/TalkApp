package com.example.talkapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;

import com.example.talkapp.R;
import com.example.talkapp.fragments.LoginFragment;
import com.example.talkapp.fragments.SignUpFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;


public class MainActivity extends AppCompatActivity implements SignUpFragment.OnSignUpListener {

    FragmentContainerView mFragmentContainerView;
    final FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    private String username;
    private int mUserStatusNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkSignedIn();

        mFragmentContainerView = findViewById(R.id.fragmentContainer);

        SignUpFragment signUpFragment = new SignUpFragment();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, signUpFragment)
                .commit();

    }

    @Override
    public void onSignUpClickListener(Bundle userStatus) {
        LoginFragment loginFragment = new LoginFragment();

        mUserStatusNumber = userStatus.getInt("userStatus");

        switch (mUserStatusNumber) {
            case 0:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new SignUpFragment())
                        .addToBackStack(null)
                        .commit();
                break;

            case 1:
                loginFragment.setArguments(userStatus);
                username = userStatus.getString("username");

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, loginFragment)
                        .addToBackStack(null)
                        .commit();
                break;
            case 2:
                username = userStatus.getString("username");
                checkSignedIn();
                break;
        }
    }

    private void checkSignedIn() {
        mFirebaseAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = mFirebaseAuth.getCurrentUser();
                if (user != null && mUserStatusNumber == 2) {
                    Toast.makeText(MainActivity.this, ""+username, Toast.LENGTH_SHORT).show();

                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(username).build();

                    user.updateProfile(profileUpdates)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isComplete() && task.isSuccessful()) {
                                        startActivity(new Intent(MainActivity.this, ChatActivity.class));
                                        finish();
                                    }
                                }
                            });
                } else if (user != null && user.getDisplayName() != null) {
                    startActivity(new Intent(MainActivity.this, ChatActivity.class));
                    finish();
                }
            }
        });
    }
}