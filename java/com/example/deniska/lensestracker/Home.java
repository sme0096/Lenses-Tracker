package com.example.deniska.lensestracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import io.netopen.hotbitmapgg.library.view.RingProgressBar;

public class Home extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    RingProgressBar ringProgressBar;
    FloatingActionButton add;
    FloatingActionButton refresh;
    TextView numOfDays;
    long days;
    int interval;
    int progress;
    SharedPreferences save;
    SharedPreferences.Editor shareEditor;
    TextView tv;
    Date currentDate;
    Date futureDate;
    Date progressDate;

    /**
     * onCreate metoda
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //nastavení prvků
        numOfDays = (TextView) findViewById(R.id.tvDays);
        tv = (TextView)findViewById(R.id.textView);
        currentDate = new Date();
        setupSharedPreferences();
        numOfDays.setText(String.valueOf(days));
        setupRingProgressBar();

        //nastavení spodní navigace
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    /**
     * Nastavení progresu
     */
    void setupRingProgressBar(){
        ringProgressBar = (RingProgressBar) findViewById(R.id.progress_bar);
        ringProgressBar.setMax(interval);
        ringProgressBar.setProgress(progress);

        ringProgressBar.setOnProgressListener(new RingProgressBar.OnProgressListener() {
            @Override
            public void progressToComplete() {
                Toast.makeText(Home.this, "Lenses Expired", Toast.LENGTH_SHORT).show();
                ringProgressBar.setRingProgressColor(getColor(R.color.colorPrimaryLight));
                add.setEnabled(false);
            }
        });
    }

    /**
     * nastavení pro kontinuální nošení
     */
    public void continualSetting() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        if((futureDate != null) && (progressDate != null)){
            if (!currentDate.after(futureDate)) {
                long diff = futureDate.getTime() - currentDate.getTime();
                long diff2 = currentDate.getTime() - progressDate.getTime();
                long temp = diff2 / (24 * 60 * 60 * 1000);
                progress = (int) temp;
                days = diff / (24 * 60 * 60 * 1000);
                tv.setText(dateFormat.format(futureDate));
                numOfDays.setText(String.valueOf(days));
            }
        }

    }

    /**
     * Nastavení tlačítek
     */
    void setupButtons(){
        add = (FloatingActionButton) findViewById(R.id.btnPlus);
        refresh = (FloatingActionButton) findViewById(R.id.btnNew);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (progress < ringProgressBar.getMax()) {
                    progress++;
                    ringProgressBar.setProgress(progress);

                    days--;
                    numOfDays.setText(String.valueOf(days));
                }
            }
        });

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(sharedPreferences.getBoolean(getString(R.string.continualKey),false)){
                    continualSetting();
                } else {
                    setupNewInterval(sharedPreferences);
                    add.setEnabled(true);
                }
            }
        });
    }

    /**
     * Přepínače spodní navigace
     */
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    return true;
                case R.id.navigation_profile:
                    Intent intent = new Intent(getApplicationContext(), Profile.class);
                    startActivity(intent);
                    return true;
            }
            return false;
        }
    };

    /**
     * nastavení menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * Nastavení výběru z menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, Settings.class);
                startActivity(intent);
                return true;
            case R.id.action_about:
                Toast.makeText(Home.this, "about", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * nastavení nového intervalu podle kontinual
     * @param sharedPreferences
     */
    private void setupNewInterval(SharedPreferences sharedPreferences) {
        String type = sharedPreferences.getString(getString(R.string.pref_ltype_key),getString(R.string.ltype_biweekly));
        interval = Integer.parseInt(type);
        progress = 0;
        setupRingProgressBar();
        ringProgressBar.setProgress(progress);
        ringProgressBar.setRingProgressColor(getColor(R.color.colorPrimaryDark));

        if((sharedPreferences.getBoolean(getString(R.string.continualKey),false))){
            progressDate = new Date();
            add.setVisibility(FloatingActionButton.INVISIBLE);
            Calendar c = Calendar.getInstance();
            int add = Integer.parseInt(type);
            c.setTime(progressDate);
            c.add(Calendar.DATE, add);
            futureDate = c.getTime();
            continualSetting();
        } else {
            days = Long.parseLong(type);
            numOfDays.setText(String.valueOf(days));
            add.setVisibility(FloatingActionButton.VISIBLE);
        }
    }

    /**
     * uložení
     */
    private void save(){
        save = getSharedPreferences("main", MODE_PRIVATE);
        shareEditor = save.edit();
        shareEditor.putLong("days", days);
        shareEditor.putInt("interval", interval);
        shareEditor.putInt("progress", progress);
        if(futureDate != null)
            shareEditor.putLong("time", futureDate.getTime()).apply();
        if(progressDate != null)
            shareEditor.putLong("created", progressDate.getTime()).apply();
        shareEditor.apply();
    }

    /**
     * nastavení shared preferences
     */
    private void setupSharedPreferences() {
        // Get all of the values from shared preferences to set it up
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setupButtons();
        setupNewInterval(sharedPreferences);

        save = getSharedPreferences("main", MODE_PRIVATE);
        interval = save.getInt("interval", 14);
        progress = save.getInt("progress", 0);

        if(sharedPreferences.getBoolean(getString(R.string.continualKey),false)){
            futureDate = new Date(save.getLong("time", 0));
            progressDate = new Date(save.getLong("created", 0));
            add.setVisibility(FloatingActionButton.INVISIBLE);
            continualSetting();
        } else {
            interval = save.getInt("interval", 14);
            days = save.getLong("days", 14);
            add.setVisibility(FloatingActionButton.VISIBLE);
        }

        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * nastavení změn v menu
     * @param sharedPreferences
     * @param s
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if ((s.equals(getString(R.string.pref_ltype_key))) || (s.equals(getString(R.string.continualKey)))){
            setupNewInterval(sharedPreferences);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        save();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister VisualizerActivity as an OnPreferenceChangedListener to avoid any memory leaks.
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        save();



    }
}


