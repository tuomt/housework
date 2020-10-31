package com.example.housework.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CreateUserViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public CreateUserViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Syötä lempinimi, sähköposti ja salasana");
    }

    public LiveData<String> getText() {
        return mText;
    }
}