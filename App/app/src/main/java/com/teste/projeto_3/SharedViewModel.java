package com.teste.projeto_3;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.teste.projeto_3.model.User;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<User> user = new MutableLiveData<>(new User());

    private final MutableLiveData<Uri> imagemUri = new MutableLiveData<>();

    public void setImagemUri(Uri uri) {
        imagemUri.setValue(uri);
    }

    public LiveData<Uri> getImagemUri() {
        return imagemUri;
    }

    public void setUser(User newUser) {
        user.setValue(newUser);
    }

    public LiveData<User> getUser() {
        return user;
    }
}
