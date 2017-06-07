package com.noisemap;

import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class NoiseMeasure extends AppCompatActivity {
    public static TextView plateView;
    private Button start;
    private AudioRecord ar;
    private int bs;
    private static final int SAMPLE_RATE_IN_HZ = 44100;
    public boolean isRun = false;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case 1:
                    plateView.setText(msg.obj.toString()+"db");
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noise_measure);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        plateView = (TextView) findViewById(R.id.soundplate);
        start=(Button)findViewById(R.id.start);

        MainThread mt = new MainThread();
        mt.start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                isRun = false;
                ar.release();
                System.exit(0);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    class MainThread extends Thread {

        public void run() {
            Looper.prepare();
            bs = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            ar = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, bs);
            ar.startRecording();
            isRun = true;
            while (isRun) {
                short[] buffer = new short[bs];
                int r=ar.read(buffer, 0, bs);
                int v = 0;
                for (int i = 0; i < buffer.length; i++) {
                    v += buffer[i]*buffer[i];
                }
                Message msg = new Message();
                msg.what = 1;
                msg.obj = (int)Math.round(10*Math.log10(v/r));//单位是dB
                handler.sendMessage(msg);
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            ar.stop();
            Looper.loop();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        isRun = false;
        ar.release();
        System.exit(0);
    }



}
