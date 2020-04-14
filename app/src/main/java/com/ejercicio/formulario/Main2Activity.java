package com.ejercicio.formulario;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class Main2Activity extends AppCompatActivity implements View.OnClickListener {

    TextView tvName, tvDate, tvRFC, tvAge, tvZodiac, tvChinZodiac;
    ImageView ivZodiac, ivChinZodiac;
    String name, surname, surname2, date;
    List<String> dateList;
    Button btnBack;

    //Song service variables
    private boolean mIsBound = false;
    private SongService sServ;
    private ServiceConnection sCon = new ServiceConnection(){

        public void onServiceConnected(ComponentName name, IBinder
                binder) {
            sServ = ((SongService.ServiceBinder)binder).getService();
        }

        public void onServiceDisconnected(ComponentName name) {
            sServ = null;
        }
    };
    HomeWatcher mHomeWatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Bundle bundle = new Bundle();
        bundle = getIntent().getExtras();

        name = bundle.getString("name");
        surname = bundle.getString("surname");
        surname2 = bundle.getString("surname2");
        date = bundle.getString("date");
        dateList = Arrays.asList(date.split("/"));

        /*Log.d("DEPURACION", "Nombre: " + name);
        Log.d("DEPURACION", "Apellido Paterno: " + surname);
        Log.d("DEPURACION", "Apellido Materno: " + surname2);
        Log.d("DEPURACION", "Fecha de nacimiento: " + date);*/

        tvName = findViewById(R.id.tvName);
        tvDate = findViewById(R.id.tvDate);
        tvRFC = findViewById(R.id.tvRFC);
        tvAge = findViewById(R.id.tvAge);
        tvZodiac = findViewById(R.id.tvZodiac);
        tvChinZodiac = findViewById(R.id.tvChinZodiac);
        ivZodiac = findViewById(R.id.ivZodiac);
        ivChinZodiac = findViewById(R.id.ivChinZodiac);

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(this);

        String rfc = generaRFC();
        int age = calculaEdad();
        String zodiac = horoscopo();
        String chineseZodiac = horoscopoChino();

        /*Log.d("DEPURACION", "RFC: " + rfc);
        Log.d("DEPURACION", "Age: " + age);
        Log.d("DEPURACION", "Age: " + zodiac);
        Log.d("DEPURACION", "Age: " + chineseZodiac);*/

        tvName.setText(getResources().getString(R.string.complete_name, name, surname, surname2));
        tvDate.setText(getResources().getString(R.string.final_date,date));
        tvRFC.setText(getResources().getString(R.string.rfc,rfc));
        tvAge.setText(getResources().getString(R.string.age,age));
        tvZodiac.setText(getResources().getString(R.string.zodiac,zodiac));
        tvChinZodiac.setText(getResources().getString(R.string.chin_zodiac,chineseZodiac));

        //Song service setup
        doBindService();
        Intent music = new Intent();
        music.setClass(this, SongService.class);
        startService(music);

        mHomeWatcher = new HomeWatcher(this);
        mHomeWatcher.setOnHomePressedListener(new HomeWatcher.OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                if (sServ != null) {
                    sServ.pauseMusic();
                }
            }
            @Override
            public void onHomeLongPressed() {
                if (sServ != null) {
                    sServ.pauseMusic();
                }
            }
        });
        mHomeWatcher.startWatch();
    }

    @Override
    protected void onPause() {
        super.onPause();

        PowerManager pm = (PowerManager)
                getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = false;
        if (pm != null) {
            isScreenOn = pm.isScreenOn();
        }

        if (!isScreenOn) {
            if (sServ != null) {
                sServ.pauseMusic();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (sServ != null) {
            sServ.resumeMusic();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        doUnbindService();
        Intent music = new Intent();
        music.setClass(this, SongService.class);
        stopService(music);

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnBack:
                finish();
                break;
            default:
                break;
        }
    }

    private String generaRFC() {

        for (int i = 0; i < 2; i++) {
            if (dateList.get(i).length() < 2)
                dateList.set(i, "0" + dateList.get(i));
        }

        return surname.substring(0,2).toUpperCase() + surname2.substring(0,1).toUpperCase() +
                name.substring(0,1).toUpperCase() + dateList.get(2).substring(2) + dateList.get(1) + dateList.get(0);
    }

    private int calculaEdad(){

        int mYear, mMonth, mDay;
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH) + 1;
        mDay = c.get(Calendar.DAY_OF_MONTH);

        int age = mYear - Integer.parseInt(dateList.get(2));
        int monthDif =  mMonth - Integer.parseInt(dateList.get(1));

        if(monthDif < 0) {
            //Log.d("DEPURACION", "Mes menor");
            age--;
        } else if((monthDif == 0) && (mDay - Integer.parseInt(dateList.get(0)) < 0)) {
            //Log.d("DEPURACION", "Mes igual, dia menor");
            age--;
        }

        return age;
    }

    private String horoscopo(){

        String zodiac = "";

        int month = Integer.parseInt(dateList.get(1));
        int day = Integer.parseInt(dateList.get(0));

        if((month == 3 && day >= 21) || (month == 4 && day <= 19)) {
            zodiac = getResources().getString(R.string.aries);
            ivZodiac.setImageResource(R.drawable.aries);
        } else if((month == 4) || (month == 5 && day <= 20)) {
            zodiac = getResources().getString(R.string.taurus);
            ivZodiac.setImageResource(R.drawable.taurus);
        } else if((month == 5) || (month == 6 && day <= 20)) {
            zodiac = getResources().getString(R.string.gemini);
            ivZodiac.setImageResource(R.drawable.gemini);
        } else if((month == 6) || (month == 7 && day <= 22)) {
            zodiac = getResources().getString(R.string.cancer);
            ivZodiac.setImageResource(R.drawable.cancer);
        } else if((month == 7) || (month == 8 && day <= 22)) {
            zodiac = getResources().getString(R.string.leo);
            ivZodiac.setImageResource(R.drawable.leo);
        } else if((month == 8) || (month == 9 && day <= 22)) {
            zodiac = getResources().getString(R.string.virgo);
            ivZodiac.setImageResource(R.drawable.virgo);
        } else if((month == 9) || (month == 10 && day <= 22)) {
            zodiac = getResources().getString(R.string.libra);
            ivZodiac.setImageResource(R.drawable.libra);
        } else if((month == 10) || (month == 11 && day <= 21)) {
            zodiac = getResources().getString(R.string.scorpio);
            ivZodiac.setImageResource(R.drawable.scorpio);
        } else if((month == 11) || (month == 12 && day <= 21)) {
            zodiac = getResources().getString(R.string.sagittarius);
            ivZodiac.setImageResource(R.drawable.sagittarius);
        } else if((month == 12) || (month == 1 && day <= 19)) {
            zodiac = getResources().getString(R.string.capricorn);
            ivZodiac.setImageResource(R.drawable.capricorn);
        } else if((month == 1) || (month == 2 && day <= 18)) {
            zodiac = getResources().getString(R.string.aquarius);
            ivZodiac.setImageResource(R.drawable.aquarius);
        } else {
            zodiac = getResources().getString(R.string.pisces);
            ivZodiac.setImageResource(R.drawable.pisces);
        }

        return zodiac;
    }

    private String horoscopoChino(){

        String chineseZodiac = "";

        switch (Integer.parseInt(dateList.get(2)) % 12) {
            case 0:
                chineseZodiac = getResources().getString(R.string.monkey);
                ivChinZodiac.setImageResource(R.drawable.monkey);
                break;
            case 1:
                chineseZodiac = getResources().getString(R.string.rooster);
                ivChinZodiac.setImageResource(R.drawable.rooster);
                break;
            case 2:
                chineseZodiac = getResources().getString(R.string.dog);
                ivChinZodiac.setImageResource(R.drawable.dog);
                break;
            case 3:
                chineseZodiac = getResources().getString(R.string.pig);
                ivChinZodiac.setImageResource(R.drawable.pig);
                break;
            case 4:
                chineseZodiac = getResources().getString(R.string.rat);
                ivChinZodiac.setImageResource(R.drawable.rat);
                break;
            case 5:
                chineseZodiac = getResources().getString(R.string.ox);
                ivChinZodiac.setImageResource(R.drawable.ox);
                break;
            case 6:
                chineseZodiac = getResources().getString(R.string.tiger);
                ivChinZodiac.setImageResource(R.drawable.tiger);
                break;
            case 7:
                chineseZodiac = getResources().getString(R.string.rabbit);
                ivChinZodiac.setImageResource(R.drawable.rabbit);
                break;
            case 8:
                chineseZodiac = getResources().getString(R.string.dragon);
                ivChinZodiac.setImageResource(R.drawable.dragon);
                break;
            case 9:
                chineseZodiac = getResources().getString(R.string.snake);
                ivChinZodiac.setImageResource(R.drawable.snake);
                break;
            case 10:
                chineseZodiac = getResources().getString(R.string.horse);
                ivChinZodiac.setImageResource(R.drawable.horse);
                break;
            case 11:
                chineseZodiac = getResources().getString(R.string.goat);
                ivChinZodiac.setImageResource(R.drawable.goat);
                break;
        }

        return chineseZodiac;
    }

    //Song service binding functions
    void doBindService(){
        bindService(new Intent(this, SongService.class),
                sCon, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService()
    {
        if(mIsBound)
        {
            unbindService(sCon);
            mIsBound = false;
        }
    }
}
