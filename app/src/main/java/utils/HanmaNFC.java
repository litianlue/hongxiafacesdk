package utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.dfxh.wang.serialport_test.MainActivity;

import android_serialport_api.HexDump;
import android_serialport_api.SerialPort;

/**
 * Created by litianlue on 2019/8/12.
 */

public class HanmaNFC {
    private SerialPort serialPort;
    private Context content;
    private SerialPortUtils serialPortUtils;
    private String path = "/dev/ttyS4";
    private int baudrate = 19200;
    public boolean serialPortStatus = false; //是否打开串口标志
    public ChangeTool changeTool = new ChangeTool();
    public HanmaNFC(Context content){
        this.content  = content;
    }

    /**
     * 初始化NFC
     */
    public void initNFC(){
        serialPortUtils  = new SerialPortUtils();
        serialPort = serialPortUtils.openSerialPort();
        serialPortUtils.setOnDataReceiveListener(new SerialPortUtils.OnDataReceiveListener() {
            @Override
            public void onDataReceive(byte[] buffer, int size) {

                onDataReceiveListener.onDataReceive(buffer,size);
            }

            @Override
            public void onDataStr(String data, int size) {
                onDataReceiveListener.onDataStr(data,size);
            }


        });
    }
    /**
     * 初始化NFC
     * @param path 串口物理路径
     * @param baudrate 波特率
     */
    public void initNFC(String path,int baudrate){
        serialPortUtils  = new SerialPortUtils();
        serialPortUtils.setPath(path);
        serialPortUtils.setBaudrate(baudrate);
        serialPort = serialPortUtils.openSerialPort();
        serialPortUtils.setOnDataReceiveListener(new SerialPortUtils.OnDataReceiveListener() {
            @Override
            public void onDataReceive(byte[] buffer, int size) {
                onDataReceiveListener.onDataReceive(buffer,size);
            }

            @Override
            public void onDataStr(String data, int size) {
                onDataReceiveListener.onDataStr(data,size);
            }


        });
    }
    /**
     * 关闭NFC
     */
    public void closeNFC(){
        if(serialPortUtils!=null)
        serialPortUtils.closeSerialPort();
        if(serialPort!=null)
            serialPort = null;
    }

    /**
     * triggerNumber :触发蜂鸣器次数
     * @param triggerNumber
     */
    public void triggerBuzzer(int triggerNumber){
        if(triggerNumber>255){
            triggerNumber = 255;
        }
        if (serialPort == null){
            Toast.makeText(content,"请先打开串口",Toast.LENGTH_SHORT).show();
            return;
        }
        String number = Integer.toHexString(triggerNumber);
        number = number.replace("7E","7F01");
        number = number.replace("7F","7F02");
        if(number.length()<2){
            number  = "0"+number;
        }
        String s = HexDump.makeChecksum("010102010A01"+number);
        String sends = "7E010102010A01"+number+s+"7E";
        serialPortUtils.sendSerialPortByte(sends);
    }
    /**
     * light :打开灯  1表示红灯亮  2表示绿灯亮
     * @param light
     */
    public void openLight(int light){
        if (serialPort == null){
            Toast.makeText(content,"请先打开串口",Toast.LENGTH_SHORT).show();
            return;
        }
        String number = Integer.toHexString(light);
        number = number.replace("7E","7F01");
        number = number.replace("7F","7F02");
        if(number.length()<2){
            number  = "0"+number;
        }
        String s = HexDump.makeChecksum("010102010B01"+number);
        String sends = "7E010102010B01"+number+s+"7E";
        serialPortUtils.sendSerialPortByte(sends);
    }

    /**
     * 读取机器号
     *
     */
    public void readDeviceNo(){
        if (serialPort == null){
            Toast.makeText(content,"请先打开串口",Toast.LENGTH_SHORT).show();
            return;
        }
        String s1 = HexDump.makeChecksum("010102010100");
        String sends = "7E010102010100"+s1+"7E";
        serialPortUtils.sendSerialPortByte(sends);
    }
    /**
     * 写机器号
     *@param no  0x00~0xFF
     */
    public void writeDeviceNo(String no){
        if (serialPort == null){
            Toast.makeText(content,"请先打开串口",Toast.LENGTH_SHORT).show();
            return;
        }

        no = no.replace("7E","7F01");
        no = no.replace("7F","7F02");
        String s1 = HexDump.makeChecksum("01010201F101"+no);
        String sends = "7E01010201F101"+no+s1+"7E";
        serialPortUtils.sendSerialPortByte(sends);
    }
    /**
     * 读取设备版本
     *
     */
    public void readDeviceVersion(){
        if (serialPort == null){
            Toast.makeText(content,"请先打开串口",Toast.LENGTH_SHORT).show();
            return;
        }
        String s1 = HexDump.makeChecksum("010102010300");
        String sends = "7E010102010300"+s1+"7E";
        serialPortUtils.sendSerialPortByte(sends);
    }

    /**
     * 读取读卡类型
     *
     */
    public void readCardType(){
        if (serialPort == null){
            Toast.makeText(content,"请先打开串口",Toast.LENGTH_SHORT).show();
            return;
        }
        String s1 = HexDump.makeChecksum("01010201850000");
        String sends = "7E01010201850000"+s1+"7E";
        serialPortUtils.sendSerialPortByte(sends);
    }
    /**
     * 设置读卡类型
     * @param type  0x00~0x06 :MF1 IC卡 、 NFC标签卡  、NFC手机、身份证、CPU IC卡、CPU卡
     */
    public void setCardType(String type){
        if (serialPort == null){
            Toast.makeText(content,"请先打开串口",Toast.LENGTH_SHORT).show();
            return;
        }

        type = type.replace("7E","7F01");
        type = type.replace("7F","7F02");
        String s1 = HexDump.makeChecksum("010102010502"+type);
        String sends = "7E010102010502"+type+s1+"7E";
        serialPortUtils.sendSerialPortByte(sends);
    }
    /**
     * 读取输出格式
     *
     */
    public void readOutPutType(){
        if (serialPort == null){
            Toast.makeText(content,"请先打开串口",Toast.LENGTH_SHORT).show();
            return;
        }
        String s1 = HexDump.makeChecksum("01010201860000");
        String sends = "7E01010201860000"+s1+"7E";
        serialPortUtils.sendSerialPortByte(sends);
    }
    /**
     * 设置输出格式
     * @param type  0x00~0x02 :禁止输出 、 WG26（三字节）+WG34（4字节需将26/34引脚置低电平）  、WG26（三字节）+WG66（八字节需将26/34引脚置低电平）
     */
    public void setOutPutType(String type){
        if (serialPort == null){
            Toast.makeText(content,"请先打开串口",Toast.LENGTH_SHORT).show();
            return;
        }
        type = type.replace("7E","7F01");
        type = type.replace("7F","7F02");
        String s1 = HexDump.makeChecksum("010102010602"+type);
        String sends = "7E010102010602"+type+s1+"7E";
        serialPortUtils.sendSerialPortByte(sends);
    }


    /**
     * 读取卡号参数
     *
     */
    public void readCardNumber(){
        if (serialPort == null){
            Toast.makeText(content,"请先打开串口",Toast.LENGTH_SHORT).show();
            return;
        }
        String s1 = HexDump.makeChecksum("01010201880000");
        String sends = "7E01010201880000"+s1+"7E";
        serialPortUtils.sendSerialPortByte(sends);
    }
    /**
     * 设置卡号参数
     * @param sw 卡号开关
     *@param SectorNumber 扇区号 1-15
     * @param  SectorPassword  扇区密码 （6个字节） 例如：“FFFFFFFFFFFF”
     *  @param  PasswordType 密码验证方式  1-A密钥 2-B密钥
     *   @param  PasswordCalculationType 密码计算方式 1--直接验证  2--使用动态加密（RC4加密）
     * @param  CardNuberLenght 卡号长度  2-8
     *  @param CardStartNumber 卡号数据起始位;
     */
    public void setCardNumber(boolean sw,int SectorNumber,String SectorPassword ,int PasswordType,int PasswordCalculationType,int CardNuberLenght,int CardStartNumber){
        String swstr;
        String sectorNumberStr;
        String sectorPasswordStr;
        String passWordTypeStr;
        String PasswordCalculationTypeStr;
        String CardNuberLenghtStr;
        String CardStartNumberStr;
        if (serialPort == null){
            Toast.makeText(content,"请先打开串口",Toast.LENGTH_SHORT).show();
            return;
        }
        if(sw){
            swstr = "01";
        }else {
            swstr = "00";
        }
        sectorNumberStr  = Integer.toHexString(SectorNumber);
        passWordTypeStr  = Integer.toHexString(PasswordType);
        PasswordCalculationTypeStr = Integer.toHexString(PasswordCalculationType);
        CardNuberLenghtStr = Integer.toBinaryString(CardNuberLenght);
        CardStartNumberStr  =  Integer.toBinaryString(CardStartNumber);
        if(sectorNumberStr.length()<2)
            sectorNumberStr = "0"+sectorNumberStr;
        if(passWordTypeStr.length()<2)
            passWordTypeStr = "0"+passWordTypeStr;
        if(PasswordCalculationTypeStr.length()<2)
            PasswordCalculationTypeStr  = "0"+PasswordCalculationTypeStr;
        if(CardNuberLenghtStr.length()<2)
            CardNuberLenghtStr  = "0"+CardNuberLenghtStr;
        sectorPasswordStr  = SectorPassword;
        if(CardNuberLenghtStr.length()<2)
            CardStartNumberStr  = "0"+CardNuberLenghtStr;

        String number  = swstr+sectorNumberStr+sectorPasswordStr+passWordTypeStr+PasswordCalculationTypeStr+CardNuberLenghtStr+CardStartNumberStr;
         number = number.replace("7E","7F01");
        number = number.replace("7F","7F02");
        String s1 = HexDump.makeChecksum("01010201080C"+number);
        String sends = "7E01010201080C"+number+s1+"7E";
        serialPortUtils.sendSerialPortByte(sends);
    }

    /**
     * 读取扇区验证
     *
     */
    public void readSectorCertification(){
        if (serialPort == null){
            Toast.makeText(content,"请先打开串口",Toast.LENGTH_SHORT).show();
            return;
        }
        String s1 = HexDump.makeChecksum("01010201870000");
        String sends = "7E01010201870000"+s1+"7E";
        serialPortUtils.sendSerialPortByte(sends);
    }
    /**
     * 设置扇区验证
     * @param sw 扇区开关
     *@param SectorNumber 扇区号 1-15
     * @param  SectorPassword  扇区密码 （6个字节） 例如：“FFFFFFFFFFFF”
     * @param  PasswordType 密码验证方式  1-A密钥 2-B密钥
     * @param  PasswordCalculationType 密码计算方式 1--直接验证  2--使用动态加密（RC4加密）
     * @param SectorContentSw  验证扇区内容开关
     * @param  CertificationNuberLenght 扇区内容长度  0-16
     *  @param CertificationStartNumber 验证数据起始位;
     */
    public void setSectorCertification(boolean sw,int SectorNumber,String SectorPassword ,int PasswordType,int PasswordCalculationType,boolean SectorContentSw,int CertificationNuberLenght,int CertificationStartNumber){
        String swstr;
        String sectorNumberStr;
        String sectorPasswordStr;
        String passWordTypeStr;
        String PasswordCalculationTypeStr;
        String SectorContentSwStr ;
        String CertificationNuberLenghtStr;
        String CertificationStartNumberStr;
        if (serialPort == null){
            Toast.makeText(content,"请先打开串口",Toast.LENGTH_SHORT).show();
            return;
        }
        if(sw){
            swstr = "01";
        }else {
            swstr = "00";
        }
        if(SectorContentSw){
            SectorContentSwStr  = "01";
        }else {
            SectorContentSwStr  = "00";
        }
        sectorNumberStr  = Integer.toHexString(SectorNumber);
        passWordTypeStr  = Integer.toHexString(PasswordType);
        PasswordCalculationTypeStr = Integer.toHexString(PasswordCalculationType);
        CertificationNuberLenghtStr = Integer.toBinaryString(CertificationNuberLenght);
        CertificationStartNumberStr  =  Integer.toBinaryString(CertificationStartNumber);
        if(sectorNumberStr.length()<2)
            sectorNumberStr = "0"+sectorNumberStr;
        if(passWordTypeStr.length()<2)
            passWordTypeStr = "0"+passWordTypeStr;
        if(PasswordCalculationTypeStr.length()<2)
            PasswordCalculationTypeStr  = "0"+PasswordCalculationTypeStr;
        if(CertificationNuberLenghtStr.length()<2)
            CertificationNuberLenghtStr  = "0"+CertificationNuberLenghtStr;
        sectorPasswordStr  = SectorPassword;
        if(CertificationNuberLenghtStr.length()<2)
            CertificationStartNumberStr  = "0"+CertificationNuberLenghtStr;

        String number  = swstr+sectorNumberStr+sectorPasswordStr+passWordTypeStr+PasswordCalculationTypeStr+SectorContentSwStr+CertificationNuberLenghtStr+CertificationStartNumberStr;
        number = number.replace("7E","7F01");
        number = number.replace("7F","7F02");
        String s1 = HexDump.makeChecksum("01010201080C"+number);
        String sends = "7E01010201080C"+number+s1+"7E";
        serialPortUtils.sendSerialPortByte(sends);
    }





    public OnDataReceiveListener onDataReceiveListener = null;
    public static interface OnDataReceiveListener {
        public void onDataReceive(byte[] buffer, int size);
        public void onDataStr(String data,int size);
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

    public void opendNFC(){

     }
}
