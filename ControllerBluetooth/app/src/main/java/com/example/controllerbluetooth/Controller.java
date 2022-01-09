package com.example.controllerbluetooth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Set;

public class Controller extends AppCompatActivity {

    private Button aConnectButton;
    private Button aLeftButton;
    private Button aRightButton;
    private Button aUpButton;
    private Button aDownButton;
    private TextView aTextView;
    private SwitchCompat aDarkSwitch;
    private Spinner aSpinner;
    private Boolean aDarkMode;

    private BluetoothDevice aDeviceSelected;
    private BluetoothAdapter aBluetoothAdapter;
    private HashMap<String, BluetoothDevice> aDeviceMap;
    private BluetoothConnect aThread;
    private BluetoothMessageHandler aMessageThread;
    private BluetoothSocket aSocket;

    private final static int CONNECTED = 1;
    private final static int DISCONNECTED = 0;
    private final static int CONNECTING = 2;
    private final static int NO_DEVICE = 3;
    private final static int FAILED_CONNECT = 4;
    private int aState = DISCONNECTED;

    //Handle the UI change
    private final Handler aHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            if (aState != msg.what) {
                aState = msg.what;
                switch (aState) {
                    case CONNECTED:
                        Log.d("ConnectThread", "handleMessage: Connected");
                        aConnectButton.setText(R.string.disconnect);
                        aConnectButton.setEnabled(true);
                        EnableAllButton(true);
                        aSocket = aThread.getBSocket();
                        if (aMessageThread == null) {
                            if (aSocket == null) {
                                aHandler.sendEmptyMessage(DISCONNECTED);
                                return;
                            }
                            aMessageThread = new BluetoothMessageHandler(aSocket, aHandler);
                            aMessageThread.start();
                        }
                        aTextView.setText(R.string.ConnectedText);
                        break;
                    case DISCONNECTED:
                        Log.d("ConnectThread", "handleMessage: Disconnected");
                        aConnectButton.setText(R.string.connect);
                        aConnectButton.setEnabled(true);
                        EnableAllButton(false);
                        Disconnect();
                        aTextView.setText(R.string.DisconnectedText);
                        break;
                    case CONNECTING:
                        Log.d("ConnectThread", "handleMessage: Connecting");
                        aConnectButton.setText(R.string.connecting);
                        aConnectButton.setEnabled(false);
                        aTextView.setText(R.string.ConnectingText);
                        break;
                    case NO_DEVICE:
                        Log.d("ConnectThread", "handleMessage: No device");
                        aConnectButton.setText(R.string.connect);
                        aConnectButton.setEnabled(true);
                        EnableAllButton(false);
                        aTextView.setText(R.string.NoDeviceText);
                        break;
                    case FAILED_CONNECT:
                        Log.d("ConnectThread", "handleMessage: Failed connect");
                        aConnectButton.setText(R.string.connect);
                        aConnectButton.setEnabled(true);
                        EnableAllButton(false);
                        aTextView.setText(R.string.FailConnectText);
                        break;
                    default:
                        aTextView.setText(R.string.UnexpectedText);
                        break;
                }
            }
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

        //Assign the variables
        aLeftButton = findViewById(R.id.button_left);
        aRightButton = findViewById(R.id.button_right);
        aUpButton = findViewById(R.id.button_up);
        aDownButton = findViewById(R.id.button_down);
        aConnectButton = findViewById(R.id.button_connect);
        aTextView = findViewById(R.id.textView);
        aDarkSwitch = findViewById(R.id.dark_switch);
        aSpinner = findViewById(R.id.spinner);

        //Retrieve the data
        Bundle vBundle = getIntent().getExtras();
        if(vBundle != null){
            aDarkMode = vBundle.getBoolean("Dark");
        } else {
            aDarkMode = false;
        }

        //Initialing the switch
        aDarkSwitch.setChecked(aDarkMode);
        if(aDarkMode) {
            aDarkSwitch.setText(R.string.Light);
        } else {
            aDarkSwitch.setText(R.string.Dark);
        }

        //Define the function of the switch
        aDarkSwitch.setOnCheckedChangeListener((compoundButton, check) -> {
            if(check){
                //switch to dark mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                aDarkSwitch.setText(R.string.Light);
            } else {
                //revert back to light mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                aDarkSwitch.setText(R.string.Dark);
            }
            aDarkMode = check;
        });

        //Initialize the bluetooth
        aBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(aBluetoothAdapter == null){
            Toast.makeText(this,"Error 0 : Returning to the menu", Toast.LENGTH_SHORT).show();
            GoBackToMenu();
        }
        if(!aBluetoothAdapter.isEnabled()){
            Toast.makeText(this,"Error 1 : Returning to the menu", Toast.LENGTH_SHORT).show();
            GoBackToMenu();
        }

        //Initialize and define the function of the Spinner
        InitSpinner();
        aSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView adapterView, View view, int pos, long id) {
                aDeviceSelected = aDeviceMap.get(adapterView.getItemAtPosition(pos).toString());
            }
            @Override public void onNothingSelected(AdapterView adapterView) {
            }
        });


        aLeftButton.setOnTouchListener((view, motionEvent) -> {
            switch(motionEvent.getAction()){

                case MotionEvent.ACTION_DOWN:
                    SendMessage('l');
                    break;

                case MotionEvent.ACTION_UP:
                    SendMessage('p');
                    break;

                default:
                    break;
            }
            return false;
        });

        aRightButton.setOnTouchListener((view, motionEvent) -> {
            switch(motionEvent.getAction()){

                case MotionEvent.ACTION_DOWN:
                    SendMessage('r');
                    break;

                case MotionEvent.ACTION_UP:
                    SendMessage('p');
                    break;

                default:
                    break;
            }
            return false;
        });

        aUpButton.setOnTouchListener((view, motionEvent) -> {
            switch(motionEvent.getAction()){

                case MotionEvent.ACTION_DOWN:
                    SendMessage('u');
                    break;

                case MotionEvent.ACTION_UP:
                    SendMessage('p');
                    break;

                default:
                    break;
            }
            return false;
        });

        aDownButton.setOnTouchListener((view, motionEvent) -> {
            switch(motionEvent.getAction()){

                case MotionEvent.ACTION_DOWN:
                    SendMessage('d');
                    break;

                case MotionEvent.ACTION_UP:
                    SendMessage('p');
                    break;

                default:
                    break;
            }
            return false;
        });
    }

    //Initialize the Spinner
    public void InitSpinner(){
        Set<BluetoothDevice> BluetoothDv = aBluetoothAdapter.getBondedDevices();
        aDeviceMap = new HashMap<>();

        for(BluetoothDevice bd : BluetoothDv)
            aDeviceMap.put(bd.getName(), bd);

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, aDeviceMap.keySet().toArray());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        aSpinner.setAdapter(adapter);
    }

    //Start the main activity
    private void GoBackToMenu(){
        Intent vIntent = new Intent(this, MainActivity.class);
        Bundle vBundle = new Bundle();
        vBundle.putBoolean("Dark", aDarkMode);
        vIntent.putExtras(vBundle);
        startActivity(vIntent);
        finish();
    }

    //Connect or disconnect the device
    public void ConnectButton(View view){
        if(aState == DISCONNECTED || aState == FAILED_CONNECT || aState == NO_DEVICE){
            Log.d("StartConnnection", "Launch the thread");
            StartConnection();
        } else if (aState == CONNECTED){
            Disconnect();
        }
    }

    //Start the connection thread
    public void StartConnection(){
        aThread = new BluetoothConnect(aDeviceSelected, aHandler);
        aThread.start();
    }

    //Call the disconnect function in the connection thread
    public void Disconnect(){
        if(aThread != null){
            aThread.disconnect();
            aThread = null;
        }
        if(aMessageThread != null){
            Log.d("TAG", "Disconnect");
            aMessageThread.interrupt();
            aMessageThread = null;
        }
        aSocket = null;
    }

    //Send the message to the device
    public void SendMessage(char pMessage){
        if(aMessageThread == null){
            if(aSocket == null){
                aHandler.sendEmptyMessage(DISCONNECTED);
                return;
            }
            aMessageThread = new BluetoothMessageHandler(aSocket, aHandler);
            aMessageThread.start();
        }
        aMessageThread.setMessage(pMessage);
    }

    //Enable or disable all button
    public void EnableAllButton(boolean pState){
        aLeftButton.setEnabled(pState);
        aRightButton.setEnabled(pState);
        aUpButton.setEnabled(pState);
        aDownButton.setEnabled(pState);
    }

}