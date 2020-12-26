package utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import android_serialport_api.HexDump;
import android_serialport_api.SerialPort;

import static utils.SerialPortUtils.SliteString;

/**
 * Created by litianlue on 2019/8/12.
 */

public class HanmaATSerial {
    private SerialPort serialPort;
    private Context content;
    private SerialPortUtils serialPortUtils;
    private String path = "/dev/ttyS4";
    private int baudrate = 9600 ;
    public boolean serialPortStatus = false; //是否打开串口标志
    public ChangeTool changeTool = new ChangeTool();

    public static HanmaATSerial hanmaATSerial;
    public HanmaATSerial(){

    }

     public static HanmaATSerial getInstance() {
        if(hanmaATSerial ==null)
            return hanmaATSerial = new HanmaATSerial();
        return hanmaATSerial;
    }
    /**
     * 初始化串口
     */
    public void initSerial(){
        serialPortUtils  = new SerialPortUtils();
        serialPortUtils.setOnDataReceiveListener(new SerialPortUtils.OnDataReceiveListener() {
            @Override
            public void onDataReceive(byte[] buffer, int size) {
                if(onDataReceiveListener!=null)
                onDataReceiveListener.onDataReceive(buffer,size);
            }
            @Override
            public void onDataStr(String data, int size) {

                if(onDataReceiveListener!=null)
                onDataReceiveListener.onDataHexStr(data,size);
            }
        });
    }
    /**
     * 初始化串口
     * @param path 串口物理路径
     * @param baudrate 波特率
     */
    public void initSerial(String path,int baudrate){
        serialPortUtils  = new SerialPortUtils();
        serialPortUtils.setPath(path);
        serialPortUtils.setBaudrate(baudrate);
        serialPortUtils.setOnDataReceiveListener(new SerialPortUtils.OnDataReceiveListener() {
            @Override
            public void onDataReceive(byte[] buffer, int size) {
                onDataReceiveListener.onDataReceive(buffer,size);
            }
            @Override
            public void onDataStr(String data, int size) {
                onDataReceiveListener.onDataHexStr(data,size);
            }
        });
    }
    public boolean openSerial(){
        boolean state = false;
        if(serialPortUtils!=null) {
            serialPort = serialPortUtils.openSerialPort();
            if(serialPort!=null)
            state = true;
        }
        return state;
    }
    /**
     * 关闭串口
     */
    public void closeSerial(){
        if(serialPortUtils!=null)
        serialPortUtils.closeSerialPort();
        if(serialPort!=null)
            serialPort = null;
    }
    /**
     * 发送指令
     *
     */
    public void sendCommant(String data){
        if (serialPort == null){
            Log.d("HanmaATSerial","串口未打开");
            return;
        }
        serialPortUtils.sendSerialPort(data);
    }
    /**
     * 发送指令
     *
     */
    public void sendHexCommant(String hexdata){
        if (serialPort == null){
            Log.d("HanmaATSerial","串口未打开");
            return;
        }
        serialPortUtils.sendSerialPortByte(hexdata);
    }

    public OnDataReceiveListener onDataReceiveListener = null;
    public static interface OnDataReceiveListener {
        public void onDataReceive(byte[] buffer, int size);
        public void onDataHexStr(String data, int size);
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
    public void setSplitString(String splitString){
        SliteString  = splitString;
    }


}
