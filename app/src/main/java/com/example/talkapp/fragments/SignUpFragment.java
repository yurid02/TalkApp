package com.example.talkapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.talkapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpFragment extends Fragment {

    private EditText etEmail, etPassword, etRePassword, etUsername;
    private Button btnSignup, btnLogin;
    private OnSignInListener mOnSignInListener;
    private FirebaseAuth mFirebaseAuth;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnSignInListener) {
            mOnSignInListener = (OnSignInListener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);
        mFirebaseAuth = FirebaseAuth.getInstance();
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        etRePassword = view.findViewById(R.id.etRePassword);
        btnSignup = view.findViewById(R.id.btnSignup);
        btnLogin = view.findViewById(R.id.btnLogin);
        etUsername = view.findViewById(R.id.etUsername);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email, password, rePassword;
                email = etEmail.getText().toString();
                password = etPassword.getText().toString();
                rePassword = etRePassword.getText().toString();

                if (TextUtils.isEmpty(etUsername.getText().toString())) {
                    Snackbar.make(btnSignup, "Enter username", Snackbar.LENGTH_SHORT).show();
                    return;
                } else if (!(password.equals(rePassword))) {
                    Snackbar.make(btnSignup, "passwords do not match", Snackbar.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Snackbar.make(btnSignup, "Make sure to fill all fields", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                Bundle bundle = new Bundle();
                bundle.putString("username", etUsername.getText().toString());
                bundle.putInt("userStatus", 2);
                mOnSignInListener.onSignInListener(bundle);

                mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (!task.isSuccessful()) {
                                    String errorMessage = task.getException().getLocalizedMessage();
                                    Snackbar.make(btnSignup, errorMessage, Snackbar.LENGTH_SHORT).show();
                                    if (task.getException().toString().toLowerCase().contains("usercollision")) {
                                        bundle.putInt("userStatus", 1);
                                        bundle.putString("email", email);

                                        mOnSignInListener.onSignInListener(bundle);
                                    }
                                    return;
                                }

                            }
                        });
            }
        });


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putInt("userStatus", 1);
                bundle.putString("email", "");
                mOnSignInListener.onSignInListener(bundle);
            }
        });
    }

    public interface OnSignInListener {
        void onSignInListener(Bundle userStatus);
    }
}