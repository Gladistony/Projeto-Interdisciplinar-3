package com.teste.projeto_3;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class FileEditor {
    Context context;
    public FileEditor(Context context) {
        this.context = context;
    }

    public void writeFile(String texto) {
        try {
            FileOutputStream fos = context.openFileOutput("id.txt", Context.MODE_PRIVATE);
            fos.write(texto.getBytes());
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readFile() {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            FileInputStream fis = context.openFileInput("id.txt");
            int character;
            while ((character = fis.read()) != -1) {
                stringBuilder.append((char) character);
            }
            fis.close();

        } catch (IOException e) {
            return "defaultString";
        }

        return stringBuilder.toString();
    }
}
