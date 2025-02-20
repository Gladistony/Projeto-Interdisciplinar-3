package com.teste.projeto_3;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<String> nomeCompleto = new MutableLiveData<>();

    public void setNomeCompleto(String nome) {
        nomeCompleto.setValue(nome);
    }

    public LiveData<String> getNomeCompleto() {
        return nomeCompleto;
    }
}
