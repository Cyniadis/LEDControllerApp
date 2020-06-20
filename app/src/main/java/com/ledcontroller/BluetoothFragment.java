package com.ledcontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Set;

public class BluetoothFragment extends Fragment {

    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothManager bluetoothManager = null;

    private ArrayList<BluetoothDevice> listOfDevices;

    public BluetoothFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    public void onItemClick(int position) {
        BluetoothDevice device = listOfDevices.get(position);
        Fragment fragment = new LEDFragment(device);
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fragment).addToBackStack(null).commit();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.bluetooth_fragment, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.bluetoothManager = (BluetoothManager) getContext().getSystemService(Context.BLUETOOTH_SERVICE);
        this.bluetoothAdapter = bluetoothManager.getAdapter();

        if (this.bluetoothAdapter == null) {
            Toast.makeText(getContext(), "Bluetooth NOT supported. Aborting.", Toast.LENGTH_LONG);
            getActivity().finish();
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, 0);
            Toast.makeText(getContext(), "Enabled bluetooth", Toast.LENGTH_LONG).show();
        } else {
            scanDevices();
        }

        ListView listView = getView().findViewById(R.id.btDeviceList);
        listView.setOnItemClickListener((adapterView, view, i, l) -> onItemClick(i) );

    }

    public boolean scanDevices() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        listOfDevices = new ArrayList(pairedDevices);

        ArrayList<String> deviceNameList = new ArrayList<>();
        for (BluetoothDevice device : listOfDevices) {
            deviceNameList.add(device.getName() + " (" + device.getAddress() + ")");
        }
        final ArrayAdapter arrayAdapter = new ArrayAdapter(getContext(), R.layout.support_simple_spinner_dropdown_item, deviceNameList);

        ListView listView = getView().findViewById(R.id.btDeviceList);
        listView.setAdapter(arrayAdapter);


        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Add your menu entries here
        inflater.inflate(R.menu.menu, menu);

        MenuItem btSettings = menu.findItem(R.id.BTSettings);
        btSettings.setOnMenuItemClickListener(menuItem -> onBTSettingsClick());

        MenuItem btRefresh = menu.findItem(R.id.BTRefresh);
        btRefresh.setOnMenuItemClickListener(menuItem -> scanDevices());
    }


    public boolean onBTSettingsClick()
    {
        Intent intentOpenBluetoothSettings = new Intent();
        intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
        startActivity(intentOpenBluetoothSettings);
        return true;
    }

}