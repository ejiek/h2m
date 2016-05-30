package com.ejiek;

import java.io.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import java.net.ServerSocket;

public class Main {

    public static void main(String[] args) throws IOException {


        ServerSocket servers = null;
        Socket client = null;

        // create server socket
        try {
            servers = new ServerSocket(8080);

            while (true) {
                client = servers.accept();
                EchoHandler handler = new EchoHandler(client);
                handler.start();
            }


        } catch (IOException e) {
            System.out.println("Couldn't listen to this port");
            System.exit(-1);
        }


        client.close();
        servers.close();
    }
}


class EchoHandler extends Thread {
    Socket client;

    EchoHandler(Socket client) {
        this.client = client;
    }

    public void run() {
        BufferedReader in = null;
        PrintWriter out = null;
        try {

            in = new BufferedReader(new
                    InputStreamReader(client.getInputStream()));

            out = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(client.getOutputStream())),
                    true);
            out.flush();
            String input;

            while ((input = in.readLine()) != null) {
                String[] tokens = input.split(" ");
                if(tokens[0].equals("GET")) {
                    if(suitable(tokens[1])){
                        httpResponce(out, "<H1>msg sent</H1>");
                    }else{
                        httpResponce(out, "Ooops..");
                    }
                }
            }
            try {
                in.close();
            } catch (Exception d) {
                System.err.println("Exception caught: unable to close client.");
            }
        } catch (Exception e) {
            //System.err.println("Exception caught: client disconnected.");
        }
    }

    public boolean suitable(String input){
        String[] arguments = input.split("/");
        //System.out.println("Input: "+ input);
        System.out.println("Number of arguments: "+arguments.length);
        if (arguments.length == 5) {
            System.out.println("From:    "+arguments[1]);
            System.out.println("To:      "+arguments[2]);
            System.out.println("Subject: "+arguments[3]);
            System.out.println("Body   : "+arguments[4]);
            try {
                if (smtpSend(arguments[1], arguments[2],arguments[3],arguments[4])) {
                    return true;
                }
            } catch (IOException e) {
                return false;
            }
        }
        return false;
    }

    public boolean smtpSend(String from, String to, String subj, String body)
            throws IOException {
        BufferedReader in;
        PrintWriter out;
        String localhost="localhost";

        Socket smtpPipe;
        InputStream inn;
        OutputStream outt;

        smtpPipe = new Socket("localhost", 25);

        inn = smtpPipe.getInputStream();
        outt = smtpPipe.getOutputStream();

        in = new BufferedReader(new InputStreamReader(inn));
        out = new PrintWriter(new OutputStreamWriter(outt), true);

        String initialID = in.readLine();
        System.out.println(initialID);
        out.println("HELO " + localhost);//System.out.println("HELO " + localhost);
        accept(in,250);
        out.println("MAIL From:<" + from +"@" +localhost+ ">");//System.out.println("MAIL From:<" + from +"@" +localhost+">");
        accept(in,250);
        out.println("RCPT TO:<" + to + ">");//System.out.println("RCPT TO:<" + to + ">");
        accept(in,250);
        out.println("DATA");//System.out.println("DATA");
        accept(in,354);
        out.println("Subject: "+subj);System.out.println("Subject: "+subj);
        out.println("From: " + from+"@"+localhost);System.out.println("From: " + from+"@"+localhost);
        out.println("To: "+to);//System.out.println("To: "+to);
        out.println();System.out.println();
        out.println(body);//System.out.println(body);
        out.println(".");//System.out.println(".");
        accept(in,250);
        out.println("QUIT");//System.out.println("QUIT");
        return true;
    }

    public boolean accept(BufferedReader in, int awaits) throws IOException {
        String temp = in.readLine();
        String[] arguments = temp.split(" ");
        if(arguments[0] != null){
            if(arguments[0].equals(Integer.toString(awaits))){
                return true;
            }
        }
        throw new IOException();
    }

    public void httpResponce(PrintWriter out, String text){
        out.write("HTTP/1.0 200 OK\r\n"+
                "Date: Fri, 31 Dec 1999 23:59:59 GMT\r\n"+
                "Server: Apache/0.8.4\r\n"+
                "Content-Type: text/html\r\n"+
                "\r\n"+
                "<TITLE>h2s</TITLE>"+
                text);

        try {
            out.close();
            client.close();
        } catch (Exception d) {
            System.err.println("Exception caught: unable to close client.");
        }
    }

}
