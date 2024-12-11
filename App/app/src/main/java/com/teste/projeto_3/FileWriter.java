package com.teste.projeto_3;

import android.content.Context;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileWriter {
    public void escreverEmArquivo(Context context, String texto) {
        try {
            FileOutputStream fos = context.openFileOutput("id.txt", Context.MODE_PRIVATE);
            fos.write(texto.getBytes());
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String lerDeArquivo(Context context) {
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
