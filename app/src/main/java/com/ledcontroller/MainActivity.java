package com.ledcontroller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import java.util.ArrayList;

import static android.content.pm.ActivityInfo.*;

class MyPagerAdapter extends FragmentStateAdapter {
    private ArrayList<Fragment> fragments;
    final private int size = 2;

    public MyPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
        fragments = new ArrayList<Fragment>(size);
    }

    public void addFragment(Fragment fragment) {
        if (fragments.isEmpty()) {
            fragments.add(0, fragment);
        } else if (fragments.size() > 1) {
            fragments.set(1, fragment);
        } else {
            fragments.add(1, fragment);
        }
    }

    public void removeFragment(int pos)
    {
        fragments.remove(1);
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

public class MainActivity extends AppCompatActivity {

    private MyPagerAdapter pagerAdapter = null;
    private ViewPager2 viewPager;
    private BluetoothFragment bluetoothFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);

        pagerAdapter = new MyPagerAdapter(getSupportFragmentManager(), getLifecycle());

        viewPager = findViewById(R.id.view_pager);
        if (bluetoothFragment == null) {
            bluetoothFragment = new BluetoothFragment(viewPager);
            pagerAdapter.addFragment(bluetoothFragment);

        }
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(0);
        pagerAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() > 0) {
            viewPager.setCurrentItem(0);
            pagerAdapter.removeFragment(1);
            pagerAdapter.notifyDataSetChanged();
        } else {
            super.onBackPressed();
        }
    }

}