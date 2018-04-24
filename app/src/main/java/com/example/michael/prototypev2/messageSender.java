package com.example.michael.prototypev2;

/**
 * Created by Michael on 4/22/2018.
 */

import android.os.AsyncTask;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by Michael on 4/21/2018.
 */

public class messageSender extends AsyncTask<String, Integer, String> {
    private static final int SERVERPORT = 8090;
    //private static final String SERVER_IP = "172.16.119.211";
    private static final String SERVER_IP = "35.185.227.245";
    //private static final String SERVER_IP = "";

    Socket s;
    DataOutputStream dos;
    PrintWriter out;
    String response;

    public AsyncResponse delegate = null;

    //public messageSender(AsyncResponse delegate){
      //  this.delegate = delegate;
    //}

    @Override
    protected String doInBackground(String... voids) {

        String message = voids[0];
        System.out.println(message);
        try {
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
            s = new Socket(serverAddr, SERVERPORT);
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true );

            out.println( message);
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            /*
            if(in.ready()) {
                String response = in.readLine();

                System.out.println("message from server:" + response);
            }
            */
            response = null;


            System.out.println("Trying to read...");
            response = in.readLine();
            System.out.println(response);
            //out.print("Try"+"\r\n");
            System.out.println("Message sent");

            in.close();
            out.flush();
            out.close();
            s.close();
            System.out.println("end of try");

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("out of trycatch");


        return response ;
    }

    @Override
    protected void onPostExecute(String result){
        delegate.processFinish(result);
    }

}