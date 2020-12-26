package com.dfxh.wang.serialport_test;

import android.content.Context;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android_serialport_api.HexDump;
import android_serialport_api.SerialPort;
import utils.HanmaATSerial;
import utils.HanmaNFC;
import utils.SerialPortUtils;

public class MainActivity extends AppCompatActivity implements HanmaATSerial.OnDataReceiveListener{
    private final String TAG = "MainActivity";

    private Button button_open;
    private Button button_close;
    private EditText editText_send;
    private Button button_send;
    private TextView textView_status;
    private Button button_status;
    private Spinner spinner_one;


    private Handler handler;

    private HanmaNFC hanmaNFC ;
    private HanmaATSerial hanmaATSerial;
    public static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hanmaATSerial = new HanmaATSerial();
        hanmaATSerial.initSerial();
        hanmaATSerial.setOnDataReceiveListener(this);

        handler = new Handler(); //创建主线程的handler  用于更新UI

        button_open = (Button)findViewById(R.id.button_open);
        button_close = (Button)findViewById(R.id.button_close);
        button_send = (Button)findViewById(R.id.button_send);
        editText_send = (EditText)findViewById(R.id.editText_send);
        textView_status = (TextView)findViewById(R.id.textView_status);
        button_status = (Button)findViewById(R.id.button_status);
        spinner_one = (Spinner)findViewById(R.id.spinner_one);
        Button viewById = (Button) findViewById(R.id.read_send_sdk);
        Button trigger_btn = (Button) findViewById(R.id.trigger_btn);
        Button over = (Button) findViewById(R.id.set_send_sdk);
        viewById.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hanmaATSerial.sendCommant("DZ+ALTEST");//发送请求测试
            }
        });
        trigger_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hanmaATSerial.sendCommant("DZ+CONN");//发送请求测试
            }
        });
        over.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hanmaATSerial.sendCommant("DZ+TOVER");//发送请求测试
            }
        });

        button_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Boolean state = hanmaATSerial.openSerial();
                if (state==false){
                    Log.e(TAG, "串口打开失败");
                    Toast.makeText(MainActivity.this,"串口打开失败",Toast.LENGTH_SHORT).show();
                    return;
                }
                textView_status.setText("串口已打开");
                Toast.makeText(MainActivity.this,"串口已打开",Toast.LENGTH_SHORT).show();

            }
        });
        button_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hanmaATSerial.closeSerial();
                textView_status.setText("串口已关闭");
                Toast.makeText(MainActivity.this,"串口关闭成功",Toast.LENGTH_SHORT).show();
            }
        });
        button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SendCommantTime();
//                hanmaATSerial.sendCommant("DZ+CONN");//发送请求连接
                textView_status.setText(editText_send.getText().toString()+"\r\n");
                Log.e(TAG, "editText_send.getText().toString()"+"\r\n");
            }
        });

    }

    private void SendCommantTime() {
        scheduledExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                hanmaATSerial.sendCommant("DZ+CONN"+"\r\n");//发送请求连接
                SendCommantTime();
            }
        },200, TimeUnit.MILLISECONDS);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(hanmaNFC!=null)
            hanmaNFC.closeNFC();
    }

    @Override
    public void onDataReceive(byte[] buffer, int size) {

    }

    @Override
    public void onDataHexStr(String data, int size) {
        Log.d(TAG, "数据监听:" + data+" size:"+size);
    }
}
