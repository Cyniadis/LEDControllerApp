package com.example.ledcontroller;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class BluetoothConnectionTask extends AsyncTask<Void, Void, Void> {
    private boolean connected = false;
    private ProgressDialog progressDialog;
    private BluetoothSocket bluetoothSocket = null;
    private BluetoothFragment currentActivity = null;
    private BluetoothDevice device;
    private TaskDelegate delegate;

    private final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public BluetoothConnectionTask(BluetoothFragment bluetoothFragment, BluetoothDevice device, TaskDelegate delegate) {
        this.currentActivity = bluetoothFragment;
        this.device = device;
        this.delegate = delegate;
    }

    public boolean isConnected() {
        return connected;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            if (bluetoothSocket == null || !connected) {
                bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                bluetoothSocket.connect();//start connection
                connected = true;
            }
        } catch (IOException e) {
            connected = false;//if the try failed, you can check the exception here
            try {
                bluetoothSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            Toast.makeText(currentActivity.getContext(), "Connection failed.", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        if (!connected) {
            progressDialog = ProgressDialog.show(currentActivity.getContext(), "Connecting...", "Please wait.");
        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        progressDialog.dismiss();
        if (delegate != null && connected) {
            delegate.taskCompletionResult("Connected");
            Toast.makeText(currentActivity.getContext(), "Connection successful.", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(currentActivity.getContext(), "Connection failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        close();
    }

    public void write(byte b) {

        try {
            bluetoothSocket.getOutputStream().write((int) b);
        } catch (IOException e) {
            Toast.makeText(currentActivity.getContext(), "Error while sending data to remote device.", Toast.LENGTH_SHORT).show();
        }
    }

    public void close() {
        if (connected) {
            try {
                bluetoothSocket.close();
                bluetoothSocket = null;
            } catch (IOException e) {
                Toast.makeText(currentActivity.getContext(), "Error while closing connection.", Toast.LENGTH_SHORT).show();

            }
        }
    }
}
