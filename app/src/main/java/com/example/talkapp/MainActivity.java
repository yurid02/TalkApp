package com.example.talkapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;


public class MainActivity extends AppCompatActivity implements SignUpFragment.OnSignUpListener {

    FragmentContainerView mFragmentContainerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFragmentContainerView = findViewById(R.id.fragmentContainer);

        SignUpFragment signUpFragment = new SignUpFragment();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, signUpFragment)
                .commit();

    }

    @Override
    public void onSignUpClickListener(Bundle userStatus) {
        LoginFragment loginFragment = new LoginFragment();

        int mUserStatus = userStatus.getInt("userStatus");

        switch (mUserStatus) {
            case 0:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new SignUpFragment())
                        .addToBackStack(null)
                        .commit();
                break;

            case 1:
                if (userStatus.getInt("userStatus") == 1) {
                    loginFragment.setArguments(userStatus);
                }

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, loginFragment)
                        .addToBackStack(null)
                        .commit();
                break;
        }
    }
}