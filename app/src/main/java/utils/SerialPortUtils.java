package utils;

import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android_serialport_api.HexDump;
import android_serialport_api.SerialPort;

/**
 * Created by litianlue on 2019/8/12.
 */

public class SerialPortUtils {

    private final String TAG = "SerialPortUtils";
    private String path = "/dev/ttyS4";
    private int baudrate = 9600;
    public boolean serialPortStatus = false; //是否打开串口标志
    public String data_;
    public boolean threadStatus; //线程状态，为了安全终止线程

    public SerialPort serialPort = null;
    public InputStream inputStream = null;
    public OutputStream outputStream = null;
    public ChangeTool changeTool = new ChangeTool();

    private int isAllData = 0;//判断数据完整

    /**
     * 打开串口
     *
     * @return serialPort串口对象
     */
    public SerialPort openSerialPort() {
        try {
            serialPort = new SerialPort(new File(path), baudrate, 0);
            this.serialPortStatus = true;
            threadStatus = false; //线程状态

            //获取打开的串口中的输入输出流，以便于串口数据的收发
            inputStream = serialPort.getInputStream();
            outputStream = serialPort.getOutputStream();

            new ReadThread().start(); //开始线程监控是否有数据要接收
        } catch (IOException e) {
            Log.e(TAG, "openSerialPort: 打开串口异常：" + e.toString());
            return serialPort;
        }
        Log.d(TAG, "openSerialPort: 打开串口");
        return serialPort;
    }

    /**
     * 关闭串口
     */
    public void closeSerialPort() {
        try {
            inputStream.close();
            outputStream.close();

            this.serialPortStatus = false;
            this.threadStatus = true; //线程状态
            serialPort.close();
        } catch (IOException e) {
            Log.e(TAG, "closeSerialPort: 关闭串口异常：" + e.toString());
            return;
        }
        Log.d(TAG, "closeSerialPort: 关闭串口成功");
    }

    /**
     * 发送串口指令（字符串）
     *
     * @param data String数据指令
     */
    public void sendSerialPort(String data) {
        Log.d(TAG, "sendSerialPort: 发送数据:" + data);

        try {
            byte[] sendData = data.getBytes(); //string转byte[]
            this.data_ = new String(sendData); //byte[]转string
            if (sendData.length > 0) {
                outputStream.write(sendData);
//                outputStream.write('\n');


                outputStream.write('\n');
                outputStream.flush();
                Log.d(TAG, "sendSerialPort: 串口数据发送成功");
            }
        } catch (IOException e) {
            Log.e(TAG, "sendSerialPort: 串口数据发送失败：" + e.toString());
        }

    }

    /**
     * 发送串口指令（字节数组）
     *
     * @param data String数据指令
     */
    public void sendSerialPortByte(String data) {
        Log.d(TAG, "sendSerialPort: 发送数据");

        try {
            byte[] sendData = HexDump.hexStringToByteArray(data); //string转byte[]
            this.data_ = new String(sendData); //byte[]转string
            if (sendData.length > 0) {
                outputStream.write(sendData);
//                outputStream.write('\n');
                //outputStream.write('\r'+'\n');
                outputStream.flush();
                Log.d(TAG, "sendSerialPort: 串口数据发送成功");
            }
        } catch (IOException e) {
            Log.e(TAG, "sendSerialPort: 串口数据发送失败：" + e.toString());
        }

    }

    /**
     * 单开一线程，来读数据
     */
    private StringBuffer dataBufferStr = new StringBuffer();
    private int allSize;
    public static String SliteString = "R+";

    private class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            //判断进程是否在运行，更安全的结束进程
            while (!threadStatus) {
                //Log.d(TAG, "进入线程run");
                //64   1024
                byte[] buffer = new byte[64];
                int size; //读取数据的大小
                try {


                    size = inputStream.read(buffer);


                    if (size > 0) {

                        byte[] temp = new byte[size];
                        System.arraycopy(buffer, 0, temp, 0, size);
                        String bufferstr = HexDump.bytesToHexString(temp).trim();
//                        dataBufferStr.append(bufferstr);
//                        allSize += size;
                        if (bufferstr.length() > 0) {

                            onDataReceiveListener.onDataStr(bufferstr, allSize);
                            dataBufferStr.delete(0, dataBufferStr.length());

                            Log.d(TAG, "bufferstr:" + bufferstr+"");
                            allSize = 0;
                        }
                        onDataReceiveListener.onDataReceive(temp, size);
                    } else {
                        SystemClock.sleep(5);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "run: 数据读取异常：" + e.toString());
                }
            }

        }
    }

    public OnDataReceiveListener onDataReceiveListener = null;

    public static interface OnDataReceiveListener {
        public void onDataReceive(byte[] buffer, int size);

        public void onDataStr(String data, int size);
    }

    public void setOnDataReceiveListener(OnDataReceiveListener dataReceiveListener) {
        onDataReceiveListener = dataReceiveListener;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getBaudrate() {
        return baudrate;
    }

    public void setBaudrate(int baudrate) {
        this.baudrate = baudrate;
    }
}
