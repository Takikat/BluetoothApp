package com.example.controllerbluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

public class BluetoothConnect extends Thread {
    static final UUID aUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final BluetoothDevice aDevice;
    private BluetoothSocket aSocket;
    private final Handler aHandler;
    private final static int CONNECTED = 1;
    private final static int DISCONNECTED = 0;
    private final static int CONNECTING = 2;
    private final static int NO_DEVICE = 3;
    private final static int FAILED_CONNECT = 4;

    //Initializing the Thread
    public BluetoothConnect(BluetoothDevice pDevice, Handler pHandler){
        this.aDevice = pDevice;
        this.aSocket = null;
        this.aHandler = pHandler;
    }

    //Return the socket
    public BluetoothSocket getBSocket(){
        return this.aSocket;
    }

    //Disconnect the device
    public void disconnect(){
        if(aDevice != null && aSocket != null){
            try {
                aSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //Connect to the device
    @Override
    public void run() {
        if (aDevice != null) {
            try {
                this.aHandler.sendEmptyMessage(CONNECTING);
                BluetoothSocket BufferSocket = this.aDevice.createRfcommSocketToServiceRecord(aUUID);
                Log.d("Socket is : ", BufferSocket.toString());
                BufferSocket.connect();
                Log.d("C", "connected");
                this.aHandler.sendEmptyMessage(CONNECTED);
                this.aSocket = BufferSocket;
            } catch (IOException e) {
                e.printStackTrace();
                this.aHandler.sendEmptyMessage(FAILED_CONNECT);
            }
        } else {
            this.aHandler.sendEmptyMessage(NO_DEVICE);
        }
    }
}
