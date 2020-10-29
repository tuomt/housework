package com.example.housework.ui.Group;

import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.housework.R;

public class EditGroup extends Fragment {

    private EditGroupViewModel galleryViewModel;

    public static EditGroup newInstance() {
        return new EditGroup();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.edit_group_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        galleryViewModel = ViewModelProviders.of(this).get(EditGroupViewModel.class);
        // TODO: Use the ViewModel
    }

}