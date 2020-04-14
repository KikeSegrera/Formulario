package com.ejercicio.formulario;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
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
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    EditText etName, etSurname, et2ndSurname, etDate;
    ArrayList<EditText> alEditText = new ArrayList<EditText>();
    Calendar c;
    DatePickerDialog datePicker;
    Button btnSave;
    int mYear, mMonth, mDay;

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
        setContentView(R.layout.activity_main);

        etName = findViewById(R.id.etName);
        etSurname = findViewById(R.id.etSurname);
        et2ndSurname = findViewById(R.id.et2ndSurname);
        etDate = findViewById(R.id.etDate);

        btnSave = findViewById(R.id.btnSave);

        etDate.setOnClickListener(this);
        btnSave.setOnClickListener(this);

        alEditText.add(etName);
        alEditText.add(etSurname);
        alEditText.add(et2ndSurname);
        alEditText.add(etDate);

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
        switch(v.getId()){
            case R.id.btnSave:
                if(validaFormulario()){
                    //Log.d("DEPURACION", "Formulario válido");
                    Bundle bundle = new Bundle();
                    bundle.putString("name",alEditText.get(0).getText().toString());
                    bundle.putString("surname",alEditText.get(1).getText().toString());
                    bundle.putString("surname2",alEditText.get(2).getText().toString());
                    bundle.putString("date",alEditText.get(3).getText().toString());

                    Intent intent = new Intent(MainActivity.this, Main2Activity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                break;
            case R.id.etDate:
                alEditText.get(3).setError(null);
                if(alEditText.get(3).getText().length() == 0) {
                    //Log.d("DEPURACION", "Primera selección");
                    c = Calendar.getInstance();
                    mYear = c.get(Calendar.YEAR);
                    mMonth = c.get(Calendar.MONTH);
                    mDay = c.get(Calendar.DAY_OF_MONTH);
                }
                datePicker = new DatePickerDialog(MainActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view,
                                                  int year, int monthOfYear, int dayOfMonth) {
                                etDate.setText(getResources().getString(R.string.date_format,dayOfMonth, monthOfYear + 1, year));
                                mYear = year;
                                mMonth = monthOfYear;
                                mDay = dayOfMonth;
                            }
                        }, mYear, mMonth, mDay);
                datePicker.getDatePicker().setMaxDate(c.getTimeInMillis());
                datePicker.show();

                break;
            default:
                break;
        }
    }

    private boolean validaFormulario() {

        boolean failed = false;

        for(int i = 0; i < 3; i++) {
            if (alEditText.get(i).getText().length() == 0) {
                alEditText.get(i).requestFocus();
                alEditText.get(i).setError(getResources().getString(R.string.error_empty));
                failed = true;
            }else if(!alEditText.get(i).getText().toString().matches("([A-Za-z][a-z\\u00C0-\\u024F]+ ?)+")){
                alEditText.get(i).requestFocus();
                alEditText.get(i).setError(getResources().getString(R.string.error_regex));
                failed = true;
            }
        }

        if(alEditText.get(3).getText().length() == 0) {
            alEditText.get(3).requestFocus();
            alEditText.get(3).setError(getResources().getString(R.string.error_empty));
            failed = true;
        }

        return !failed;
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
