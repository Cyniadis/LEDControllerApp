package com.example.ledcontroller;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LEDFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LEDFragment extends Fragment {

    private BluetoothConnectionTask bluetoothConnectionTask = null;
    private SeekBar colorBar;

    public LEDFragment() { }
    public LEDFragment(BluetoothConnectionTask bluetoothConnectionTask) {
        // Required empty public constructor
        this.bluetoothConnectionTask = bluetoothConnectionTask;
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

        colorBar = view.findViewById(R.id.colorBar);
        colorBar.setOnSeekBarChangeListener(colorBarListener);

        return view;
    }

    private final SeekBar.OnSeekBarChangeListener colorBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            TextView textValue = getView().findViewById(R.id.colorValue);
            textValue.setText(String.valueOf(progress * 5));

            if (!fromUser || bluetoothConnectionTask == null) {
                return;
            }

            if (progress <= seekBar.getMax() / 2) {
                bluetoothConnectionTask.write((byte) 48);
                Log.d("INFO", "Led off");
            } else {
                bluetoothConnectionTask.write((byte) 49);
                Log.d("INFO", "Led on");
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