package jlab.tvchannels;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import jlab.tvchannels.controller.SharedPreferencesController;
import jlab.tvchannels.utils.Utils;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();
    private static SwipeRefreshLayout srlLoading;
    private String currentFragmentTAG = TVFragment.TAG;
    private int currentFragmentID = R.id.nav_tv_channels;
    private Toolbar toolbar;
    private BroadcastReceiver refreshCountChannelsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals(Utils.REFRESH_COUNT_CHANNELS_ACTION)) {
                int countChannels = intent.getIntExtra(Utils.COUNT_CHANNELS_KEY, 0);
                boolean isTv = intent.getBooleanExtra(Utils.IS_TV_KEY, true);
                if (countChannels == -1)
                    toolbar.setTitle(getString(R.string.offline));
                else
                    toolbar.setTitle(String.format("%s %s", countChannels,
                            getString(isTv ? R.string.channels : R.string.stations)));
            }
        }
    };

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            int id = item.getItemId();

            Fragment fragment = null;
            currentFragmentID = id;

            if (id == R.id.nav_tv_channels) {
                fragment = TVFragment.newInstance();
                currentFragmentTAG = TVFragment.TAG;
            } else if (id == R.id.nav_radio_channels) {
                fragment = RadioFragment.newInstance();
                currentFragmentTAG = RadioFragment.TAG;
            }
//            else if (id == R.id.nav_settings) {
//                fragment = SettingsFragment.newInstance();
//                fragmentTAG = SettingsFragment.TAG;
//            }

            if (fragment != null) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.content_frame, fragment, currentFragmentTAG);
                ft.commit();
            }
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(savedInstanceState != null)
            currentFragmentID = savedInstanceState.getInt(Utils.CURRENT_FRAGMENT_ID_KEY,
                    R.id.nav_tv_channels);
        srlLoading = findViewById(R.id.srlRefresh);
        srlLoading.setColorSchemeColors(getResources().getColor(R.color.colorPrimaryDark));
        srlLoading.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                boolean isTv = currentFragmentTAG.equals(TVFragment.TAG);
                boolean isRadio = !isTv && currentFragmentTAG.equals(RadioFragment.TAG);
                if (isTv || isRadio)
                    LocalBroadcastManager.getInstance(getBaseContext())
                            .sendBroadcast(new Intent(isTv ? Utils.LOAD_TV_CHANNELS_ACTION
                                    : Utils.LOAD_RADIO_CHANNELS_ACTION));
            }
        });
        LocalBroadcastManager.getInstance(this).registerReceiver(refreshCountChannelsReceiver,
                new IntentFilter(Utils.REFRESH_COUNT_CHANNELS_ACTION));

        SharedPreferencesController.init(this);

        BottomNavigationView navView = findViewById(R.id.nav_view);

        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navView.setSelectedItemId(currentFragmentID);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(refreshCountChannelsReceiver);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(Utils.CURRENT_FRAGMENT_ID_KEY, currentFragmentID);
    }

    public static void setLoading(boolean loading) {
        if (srlLoading != null)
            srlLoading.setRefreshing(loading);
    }
}
