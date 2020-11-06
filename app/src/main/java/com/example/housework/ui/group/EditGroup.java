package com.example.housework.ui.group;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.housework.R;

public class EditGroup extends Fragment {

    private EditGroupViewModel EditGroupViewModel;

    public static EditGroup newInstance() {
        return new EditGroup();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        EditGroupViewModel =
                ViewModelProviders.of(this).get(EditGroupViewModel.class);
        View root = inflater.inflate(R.layout.edit_group_fragment, container, false);
        final TextView textView = root.findViewById(R.id.groupname);
        EditGroupViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }

}