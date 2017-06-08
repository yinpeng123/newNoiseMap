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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import static android.R.attr.name;
import static com.noisemap.MainActivity.cLoc;


public class NoiseMeasure extends AppCompatActivity {
    public static TextView plateView;
    public static TextView res;
    private Button sub;
    private AudioRecord ar;
    private int bs;
    private static final int SAMPLE_RATE_IN_HZ = 44100;
    public boolean isRun = false;
    int i = 0;
    int s = 0;
    int aveNoise = 0;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    //去掉不合实际的0分贝
                    if (Integer.parseInt(msg.obj.toString()) != 0) {
                        i++;
                        s = Integer.parseInt(msg.obj.toString()) + s;
                        aveNoise = s / i;
                    }
                    plateView.setText(msg.obj.toString() + "db");
                    break;
                case 0:
                    res.append("\n平均噪音分贝值：" + String.valueOf(aveNoise));
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
        res = (TextView) findViewById(R.id.res);
        sub = (Button) findViewById(R.id.sub);
        CLoc cLoc = (CLoc) getIntent().getSerializableExtra("CLoc");
        res.append(cLoc.getName() + "------\n" + "地址：" + cLoc.getAddress() + "\n纬度：" + cLoc.getLatitude() + "\n经度：" + cLoc.getLongitude());
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
            if (isRun = false) {
                return;
            }
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
                int r = ar.read(buffer, 0, bs);
                int v = 0;
                for (int i = 0; i < buffer.length; i++) {
                    v += buffer[i] * buffer[i];
                }
                Message msg = new Message();
                msg.what = 1;
                msg.obj = (int) Math.round(10 * Math.log10(v / r));//单位是dB
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

    public void stop(View v) {
        isRun = false;
        v.setEnabled(false);
        sub.setEnabled(true);
        Message msg = new Message();
        msg.what = 0;
        handler.sendMessage(msg);
    }

    public void sub(View v) {
        String url="http://api.map.baidu.com/geodata/v3/poi/create";
        String charset = "utf-8";
        Map<String,String> createMap = new HashMap<String, String>();
        createMap.put("title",cLoc.getName());
        createMap.put("address",cLoc.getAddress());
        createMap.put("latitude",Double.toString(cLoc.getLatitude()));
        createMap.put("longitude",Double.toString(cLoc.getLongitude()));
        createMap.put("coord_type",String.valueOf(cLoc.getCoord_type()));
        createMap.put("geotable_id","169399");
        createMap.put("ak","AXI0cm3LRMgxmkDOm3LjvjSuxSp20jg0");
        createMap.put("noise",String.valueOf(aveNoise));

        //使用POST方法向服务器发送数据
        PostThread postThread = new PostThread(url,createMap,charset);
        postThread.start();
        if(postThread.getResu()==0){
            v.setEnabled(false);
            sub.setText("成功");
        }
        else{
            v.setEnabled(false);
            sub.setText("失败");
        }
    }


}
