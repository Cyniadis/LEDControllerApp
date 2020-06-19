package com.example.ledcontroller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;


class MyPagerAdapter extends FragmentStateAdapter {
    private ArrayList<Fragment> fragments;

    public MyPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
        fragments = new ArrayList<Fragment>();
    }


    public void addFragment(Fragment fragment) {
        this.fragments.add(fragment);
    }


    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }
}

class MyTaskDelegate implements TaskDelegate {

    @Override
    public void taskCompletionResult(String result) {

    }
}

public class MainActivity extends AppCompatActivity {

    private BluetoothFragment bluetoothFragment;
    private LEDFragment ledFragment;

    private MyPagerAdapter pagerAdapter = null;
    private ViewPager2 viewPager;

    private TaskDelegate delegate = new TaskDelegate() {
        @Override
        public void taskCompletionResult(String result) {
            if (bluetoothFragment.getBluetoothConnectionTask().isConnected()) {
                ledFragment = new LEDFragment(bluetoothFragment.getBluetoothConnectionTask());
                pagerAdapter.addFragment(ledFragment);
                pagerAdapter.notifyDataSetChanged();
                viewPager.setCurrentItem(1);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pagerAdapter = new MyPagerAdapter(getSupportFragmentManager(), getLifecycle());

        if (bluetoothFragment == null) {
            bluetoothFragment = new BluetoothFragment(delegate);
            pagerAdapter.addFragment(bluetoothFragment);

        }
        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu//
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }


    public void onBTSettingsClick(MenuItem item)
    {
        Intent intentOpenBluetoothSettings = new Intent();
        intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
        startActivity(intentOpenBluetoothSettings);
    }
}