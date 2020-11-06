package com.example.housework.ui.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.housework.R;

public class CreateUser extends Fragment {

    private CreateUserViewModel createUserViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        createUserViewModel =
                ViewModelProviders.of(this).get(CreateUserViewModel.class);
        View root = inflater.inflate(R.layout.create_user_fragment, container, false);
        final TextView textView = root.findViewById(R.id.text_createuser);
        createUserViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });



        return root;
    }
}