package com.example.controllerbluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;

public class BluetoothMessageHandler extends Thread {

    private final BluetoothSocket aSocket;
    private char aCharToSend;
    private final Handler aHandler;
    private final static int CONNECTED = 1;
    private final static int DISCONNECTED = 0;
    private final static int CONNECTING = 2;
    private final static int NO_DEVICE = 3;
    private final static int FAILED_CONNECT = 4;

    //Initializing the Thread
    public BluetoothMessageHandler(BluetoothSocket pSocket, Handler pHandler){
        this.aSocket = pSocket;
        this.aHandler = pHandler;
        this.aCharToSend = 'p';
    }

    //Set the message to send to the device
    public void setMessage(char pMessage){
        Log.d("MessageSent", "setMessage: " + pMessage);
        this.aCharToSend = pMessage;
    }

    //Send the message
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                //Sending the message
                this.aSocket.getOutputStream().write(this.aCharToSend);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                Thread.currentThread().interrupt();
                this.aHandler.sendEmptyMessage(DISCONNECTED);
            }
        }
    }
}
