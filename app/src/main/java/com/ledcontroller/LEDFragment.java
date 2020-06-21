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

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.os.IBinder;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.larswerkman.holocolorpicker.ColorPicker;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.Arrays;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LEDFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LEDFragment extends Fragment implements ServiceConnection, SerialListener {


    private ColorPicker colorPicker;
    private String deviceAddress;
    private SerialService service;
    private boolean connected = false;
    private boolean initialStart = true;

    public LEDFragment(BluetoothDevice device) {
        this.deviceAddress = device.getAddress();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_l_e_d, container, false);

        colorPicker = view.findViewById(R.id.color_picker);
        colorPicker.setOnColorChangedListener(colorPickerListener);
        colorPicker.setShowOldCenterColor(false);

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
        if (service != null) {
            getActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onPause() {
        if (connected)
            disconnect();
        super.onPause();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((SerialService.SerialBinder) binder).getService();
        service.attach(this);
        if (initialStart && isResumed()) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);

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
            SerialSocket socket = new SerialSocket(getActivity().getApplicationContext(), device);
            service.connect(socket);
            connected = false;
        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }

    private void disconnect() {
        connected = false;
        service.disconnect();
        getActivity().runOnUiThread(() -> {
            Toast.makeText(getContext(), "Disconnected", Toast.LENGTH_SHORT).show();
        });
    }

    /*
     * SerialListener
     */
    @Override
    public void onSerialConnect() {

        LinearLayout loadingLayout = (LinearLayout) getView().findViewById(R.id.loading_layout);
        loadingLayout.setVisibility(View.GONE);
        ConstraintLayout ledLayout = (ConstraintLayout) getView().findViewById(R.id.led_layout);
        ledLayout.setVisibility(View.VISIBLE);


        getActivity().runOnUiThread(() -> {
            Toast.makeText(getContext(), "Connected", Toast.LENGTH_SHORT).show();
        });
        connected = true;

        sendColor(colorPicker.getColor());
    }

    @Override
    public void onSerialConnectError(Exception e) {

        ProgressBar progressBar = getView().findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        TextView textView = getView().findViewById(R.id.connectionStatus);
        textView.setText("Connection failed.");

        disconnect();

        getActivity().onBackPressed();
    }

    @Override
    public void onSerialRead(byte[] data) {
    }

    @Override
    public void onSerialIoError(Exception e) {
        disconnect();
        Toast.makeText(getContext(), "Connection lost: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }


    /////////////
    private void sendColor(int color)
    {
        try {
            ByteBuffer argb = ByteBuffer.allocate(4).putInt(color);

            byte[] rgb = { argb.get(1), argb.get(2), argb.get(3) };
            service.write(rgb);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Cannot send data.", Toast.LENGTH_SHORT).show();
            onSerialIoError(e);
        }
    }

    private final ColorPicker.OnColorChangedListener colorPickerListener = new ColorPicker.OnColorChangedListener() {
        @Override
        public void onColorChanged(int color) {
            sendColor(color);
            if (!connected) {
                Toast.makeText(getContext(), "not connected", Toast.LENGTH_SHORT).show();
                return;
            }

        }

    };

}