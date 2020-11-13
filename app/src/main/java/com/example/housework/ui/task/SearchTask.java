package com.example.housework.ui.task;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import com.example.housework.R;

public class SearchTask extends Fragment {
    Button btnetsi1;
    private SearchTaskViewModel mViewModel;

    public static SearchTask newInstance() {
        return new SearchTask();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_search_tasks, container, false);
        btnetsi1 = v.findViewById(R.id.btnetsi);
        btnetsi1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchResults searchResults = new SearchResults();

                FragmentTransaction transaction;
                transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.drawer_layout, searchResults);
                FragmentTransaction transaction1 = transaction.addToBackStack(null);
                int commit = transaction.commit();

            }
        });
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(SearchTaskViewModel.class);
        // TODO: Use the ViewModel
    }

}