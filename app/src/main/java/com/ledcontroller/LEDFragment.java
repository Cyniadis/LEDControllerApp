package com.ledcontroller;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.IBinder;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LEDFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LEDFragment extends Fragment implements ServiceConnection, SerialListener {

     class BluetoothConnectionTask extends AsyncTask<Void, Void, Void> {
         private ProgressDialog progressDialog;

         @Override
         protected Void doInBackground(Void... voids) {
             connect();
             return null;
         }

         @Override
         protected void onPreExecute() {
             progressDialog = ProgressDialog.show(getContext(), "Connecting...", "Please wait.");
         }

         @Override
         protected void onPostExecute(Void aVoid) {
             super.onPostExecute(aVoid);
             progressDialog.dismiss();
         }
     };

    private SeekBar colorBar;
    private String deviceAddress;
    private SerialService service;
    private boolean connected = false;
    private boolean initialStart = true;
    private BluetoothConnectionTask task = null;

    public LEDFragment(BluetoothDevice device) {
        this.deviceAddress = device.getAddress();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_l_e_d, container, false);

        colorBar = view.findViewById(R.id.colorBar);
        colorBar.setOnSeekBarChangeListener(colorBarListener);

        return view;
    }

    @Override
    public void onDestroy() {
        if (!connected)
            disconnect();
        getActivity().stopService(new Intent(getActivity(), SerialService.class));
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (service != null)
            service.attach(this);
        else
            getActivity().startService(new Intent(getActivity(), SerialService.class)); // prevents service destroy on unbind from recreated activity caused by orientation change
    }

    @Override
    public void onStop() {
        if (service != null && !getActivity().isChangingConfigurations())
            service.detach();
        super.onStop();
    }

    @SuppressWarnings("deprecation")
    // onAttach(context) was added with API 23. onAttach(activity) works for all API versions
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        getContext().getApplicationContext().bindService(new Intent(getActivity(), SerialService.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDetach() {
        try {
            getContext().getApplicationContext().unbindService(this);
        } catch (Exception ignored) {
        }
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (initialStart && service != null) {
            initialStart = false;

            task = new BluetoothConnectionTask();
            task.execute();
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((SerialService.SerialBinder) binder).getService();
        service.attach(this);
        if (initialStart && isResumed()) {
            initialStart = false;
            task = new BluetoothConnectionTask();
            task.execute();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        service = null;
    }

    @Override
    public void onBindingDied(ComponentName name) {
        service = null;
    }

    @Override
    public void onNullBinding(ComponentName name) {
        service = null;
    }

    /*
     * Serial + UI
     */
    public void connect() {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            connected = false;
            SerialSocket socket = new SerialSocket(getActivity().getApplicationContext(), device);
            service.connect(socket);
        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }

    private void disconnect() {
        connected = false;
        service.disconnect();
    }

    /*
     * SerialListener
     */
    @Override
    public void onSerialConnect() {

        getActivity().runOnUiThread(() -> { Toast.makeText(getContext(), "Connected", Toast.LENGTH_LONG).show(); });
        connected = true;
    }

    @Override
    public void onSerialConnectError(Exception e) {
        getActivity().runOnUiThread(() -> { Toast.makeText(getContext(), "Connection failed: " + e.getMessage(), Toast.LENGTH_LONG).show(); });
        disconnect();
        if (task != null)
        {
            task.progressDialog.dismiss();
        }
    }

    @Override
    public void onSerialRead(byte[] data) {
    }

    @Override
    public void onSerialIoError(Exception e) {
        Toast.makeText(getContext(), "Connection lost: " + e.getMessage(), Toast.LENGTH_LONG).show();
        disconnect();
    }


    /////////////
    private final SeekBar.OnSeekBarChangeListener colorBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            TextView textValue = getView().findViewById(R.id.colorValue);
            textValue.setText(String.valueOf(progress));

            if (!connected) {
                Toast.makeText(getContext(), "not connected", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                byte value = (byte) ((progress <= seekBar.getMax() / 2) ? 48 : 49);
                byte[] data = {value};
                service.write(data);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Cannot send data.", Toast.LENGTH_SHORT).show();
                onSerialIoError(e);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };
}