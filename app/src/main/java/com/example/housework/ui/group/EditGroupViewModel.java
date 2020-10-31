package com.example.housework.ui.group;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class EditGroupViewModel extends ViewModel {


    private MutableLiveData<String> mText;

    public EditGroupViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Ryhmän nimi tässä");
    }

    public LiveData<String> getText() {
        return mText;
    }

}