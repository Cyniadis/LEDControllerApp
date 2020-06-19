package com.example.ledcontroller;

import androidx.annotation.RequiresApi;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class BluetoothFragment extends Fragment {

    private BluetoothAdapter bluetoothAdapter = null;
    private ArrayList<BluetoothDevice> listOfDevices;
    private BluetoothSocket bluetoothSocket = null;
    private TaskDelegate delegate = null;

    public BluetoothFragment() {   }
    public BluetoothFragment(TaskDelegate delegate) {
        this.delegate = delegate;
    }


    private BluetoothConnectionTask bluetoothConnectionTask = null;

    private final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.bluetooth_fragment, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (this.bluetoothAdapter == null) {
            Toast.makeText(getContext(), "Bluetooth NOT supported. Aborting.", Toast.LENGTH_LONG);
        }
        final Button scanButton = getView().findViewById(R.id.btScanButton);
        scanButton.setOnClickListener(view -> connectToDevice());

        final ListView listView = getView().findViewById(R.id.btDeviceList);
        listView.setOnItemClickListener((parent, view, position, arg3) -> {
            bluetoothConnectionTask = new BluetoothConnectionTask(this, listOfDevices.get(position), delegate);
            bluetoothConnectionTask.execute();
        });

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, 0);
            Toast.makeText(getContext(), "Enabled bluetooth", Toast.LENGTH_LONG).show();
        }
        else
        {
            connectToDevice();
        }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bluetoothConnectionTask.cancel(false);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void connectToDevice()
    {
        if (bluetoothAdapter.getScanMode() != android.bluetooth.BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300); // You are able to set how long it is discoverable.
            startActivity(discoverableIntent);
            Toast.makeText(getContext(), "Enable device discovery", Toast.LENGTH_LONG).show();
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        listOfDevices = new ArrayList(pairedDevices);
        List<String> deviceNameList = listOfDevices.stream().map(BluetoothDevice::getName).collect(Collectors.toList());
        final ArrayAdapter arrayAdapter = new ArrayAdapter(getContext(), R.layout.support_simple_spinner_dropdown_item, deviceNameList);

        ListView listView = getView().findViewById(R.id.btDeviceList);
        listView.setAdapter(arrayAdapter);
    }


    public void connectToDevice(int position) {
        if (listOfDevices.isEmpty() || position < 0 || position >= listOfDevices.size()) {
            throw new RuntimeException("Error in selecting device");
        }

        BluetoothDevice device = listOfDevices.get(position);
        try {
            bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(myUUID); //create a RFCOMM (SPP) connection
            //bluetoothAdapter.cancelDiscovery();

        } catch (IOException e) {
            throw new RuntimeException("Cannot create socket with device " + device.getName() + " (" + device.getAddress() + "). " + e.getMessage());
        }

        try {
            bluetoothSocket.connect();//start connection
        } catch (IOException e) {
            throw new RuntimeException("Cannot create socket with device " + device.getName() + " (" + device.getAddress() + "). " + e.getMessage());
        }

    }

    public BluetoothConnectionTask getBluetoothConnectionTask() {  return bluetoothConnectionTask;   }

}