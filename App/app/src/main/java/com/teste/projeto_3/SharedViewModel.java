package com.teste.projeto_3;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.teste.projeto_3.model.User;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<User> user = new MutableLiveData<>(new User());

    public void setUser(User newUser) {
        user.setValue(newUser);
    }

    public LiveData<User> getUser() {
        return user;
    }
}
