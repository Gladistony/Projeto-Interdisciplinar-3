package com.teste.projeto_3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class LoggedInScreenTest extends AppCompatActivity{

    String typedEmail;
    String typedPassword;

    private String serverIP = "time.nist.gov";
    private int serverPort = 13;

    private TextView timeScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in_screen_test);

        Intent intent = getIntent();
        String typedEmail = intent.getStringExtra("typedEmail");
        String typedPassword = intent.getStringExtra("typedPassword");

        TextView tv1 = findViewById(R.id.email);
        TextView tv2 = findViewById(R.id.password);

        tv1.setText("O e-mail digitado foi: " + typedEmail);
        tv2.setText("A senha digitada foi: " + typedPassword);

    }
    public void voltar(View v){
        this.finish();
    }

    public void onClickedTempo(View v){
        TextView timeScreen = findViewById(R.id.timeTextView);
        GetTime getTime = new GetTime(serverIP, serverPort);
        new Thread(getTime).start();

    }

    public class GetTime implements Runnable{
        private String serverIP;
        private int serverPort;

        public GetTime(String serverIP, int serverPort){
            this.serverIP = serverIP;
            this.serverPort = serverPort;
        }

        @Override
        public void run() {
            try {
                Socket socket = new Socket(serverIP, serverPort);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                bufferedReader.readLine(); // skips an empty line
                String timeReceived = bufferedReader.readLine().substring(6,23);
                socket.close();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView timeScreen = findViewById(R.id.timeTextView);
                        timeScreen.setText(timeReceived);

                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);

            }
        }
    }

}