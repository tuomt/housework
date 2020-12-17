package com.example.housework.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class UserViewModel extends ViewModel {
    private final MutableLiveData<User> user = new MutableLiveData<>();

    public void setUser(User newUser) {
        user.setValue(newUser);
    }

    public LiveData<User> getUser() {
        return user;
    }
}


