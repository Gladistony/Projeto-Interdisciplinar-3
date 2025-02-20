package com.teste.projeto_3;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.teste.projeto_3.model.User;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<User> dados = new MutableLiveData<>();


    public void setUser(User user) {
        dados.setValue(user);
    }

    public LiveData<User> getUser() {
        return dados;
    }
}
