package com.teste.projeto_3;

import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnection {
    Connection con;
    String databaseName = "defaultdb";
    String ip = "mysqlservertest-gustavofr0411-7c73.k.aivencloud.com";
    String port = "13761";
    String username = "avnadmin";
    String password = "tive que retirar a senha. O GitHub não permite o commit com ela.";
    
    public Connection connectionClass() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Connection connection = null;
        String connectionURL = null;

        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            connectionURL = "jdbc:jtds:sqlserver://"+ ip + ":"+ port+";"+ "databasename="+ databaseName +";user="+username+";password="+ password +";";
            connection = DriverManager.getConnection(connectionURL);
        } catch (Exception e) {
            Log.e("Error ", e.getMessage());
        }
        return connection;
    }
}
