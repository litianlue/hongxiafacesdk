package faceapi;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.Log;

import com.dfxh.wang.face_test.FaceActivity;
import com.dfxh.wang.serialport_test.R;
import com.mv.engine.FaceBox;
import com.rokid.facelib.ImageRokidFace;
import com.rokid.facelib.RokidFace;
import com.rokid.facelib.VideoRokidFace;
import com.rokid.facelib.api.IImageRokidFace;
import com.rokid.facelib.api.IVideoRokidFace;
import com.rokid.facelib.api.ImageFaceCallback;
import com.rokid.facelib.conf.DetectFaceConf;
import com.rokid.facelib.conf.RecogFaceConf;
import com.rokid.facelib.conf.VideoDetectFaceConf;
import com.rokid.facelib.db.FeatureInfo;
import com.rokid.facelib.face.DbAddResult;
import com.rokid.facelib.input.BitmapInput;
import com.rokid.facelib.input.VideoInput;
import com.rokid.facelib.model.FaceDO;
import com.rokid.facelib.model.FaceModel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import faceapi.live.EngineWrapper;

public class HXFaceHelper {

    private int PREVIEW_WIDTH = 1280;
    private int PREVIEW_HEIGHT = 720;

    private float targetScore = 75;//识别阈值
    private int outTime = 3000;//超时时间
    private int recogInterval = 5000;//再次识别时间
    private int maxFaceNum = 5;//设置最多同时识别的人脸
    private boolean isLive;
    private DetectFaceConf dFaceConf;
    private RecogFaceConf sFaceConf;
    private IVideoRokidFace videoFace;
    private IImageRokidFace imageFace;
    private volatile static HXFaceHelper hxFaceHelper = null;
    private Context context;
    private FaceDbHelper faceDbHelper;
    private static final String PATH = FaceDbHelper.PATH_OUTPUT;

    private EngineWrapper engineWrapper;
    private AssetManager assetManager;
    private boolean openLive = false;
    private HXFaceHelper (Context context) {
        this.context  = context;
    }

    public static HXFaceHelper getInstance(Context context) {
        if (hxFaceHelper == null) {
            synchronized (HXFaceHelper.class) {
                if (hxFaceHelper == null) {
                    hxFaceHelper = new HXFaceHelper(context);
                }
            }
        }
        return hxFaceHelper;
    }
    public void initSDK(){
        assetManager = context.getResources().getAssets();
        engineWrapper = new EngineWrapper(assetManager);
        engineWrapper.init();

        RokidFace.Init(context);
        //设置输入数据的宽高
        sFaceConf = new RecogFaceConf().setRecog(true, PATH);
        dFaceConf = new VideoDetectFaceConf().setSize(PREVIEW_HEIGHT, PREVIEW_WIDTH);


        //设置人脸识别的区域为整个CameraPreview的区域
        dFaceConf.setRoi(new Rect(0,0,PREVIEW_HEIGHT,PREVIEW_WIDTH));
        dFaceConf.setSingleRecogModel(false);
        videoFace = VideoRokidFace.create(context,dFaceConf);
        videoFace.sconfig(sFaceConf);
        videoFace.startTrack(model -> {
            Log.d("HXFaceHelper","facemodel:"+model);
            //用于显示返回的人脸model
            if(faceModelCallBack!=null)
                faceModelCallBack.ModelCallBack(model,isLive);
        });
        faceDbHelper = new FaceDbHelper(context);
        faceDbHelper.clearDb();
        faceDbHelper.createDb();

        faceDbHelper.save();

        imageFace = ImageRokidFace.create(context);
        imageFace.sconfig(sFaceConf);
        imageFace.dconfig(dFaceConf);

    }
    private   Bitmap scaleImage(Bitmap bm, int newWidth, int newHeight) {
        if (bm == null) {
            return null;
        }
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        if (bm != null & !bm.isRecycled()) {
            bm.recycle();//销毁原图片
            bm = null;
        }
        return newbm;
    }
    /**
     * 添加图片
     * @param bitmap
     * @return
     */
    public String addBitmap(Bitmap bitmap){
        String uuid = null;
        try {
            if (faceDbHelper != null) {
                uuid = faceDbHelper.add(bitmap);
            }
        }catch (Exception E) { }
        return uuid;

    }


    /**
     * 添加图片
     * @param bitmap
     * @param uuid
     * @return
     */
    public String addBitmap(Bitmap bitmap,String uuid){
        if(faceDbHelper!=null){
            uuid = faceDbHelper.add(bitmap,uuid);
            return uuid;
        }
        return null;
    }

//    public FaceModel  getUser(Bitmap bitmap){
//        if(imageFace==null) return null;
//        final FaceModel[] model = {null};
//        final boolean[] option = {true};
//        imageFace.setImageFaceCallback(new BitmapInput(bitmap), new ImageFaceCallback() {
//            @Override
//            public void onFaceModel(FaceModel m) {
//                model[0] = m;
//                option[0] = false;
//            }
//        });
////        int decount = 1000;
////        while (option[0] && decount > 0) {
////            try {
////                Thread.sleep(5);
////            } catch (InterruptedException e) {
////                e.printStackTrace();
////            }
////            decount--;
////        }
//        return model[0];
//    }

    /**
     * 提取特征
     * @param bitmap
     * @return
     */
    public float[] getFeature(Bitmap bitmap){
        if(faceDbHelper!=null){
            float[] feature = faceDbHelper.getFeature(bitmap);
            return feature;
        }
        return null;
    }
    /**
     * 添加特征
     * @param feature
     * @param uuid
     * @return
     */
    public boolean addFeature(float[] feature,String uuid){
        if(faceDbHelper!=null){
            boolean b = faceDbHelper.addFeature(feature, uuid);
            return b;
        }
        return false;
    }
    /**
     *添加特征
     * @param featureInfos
     * @return
     */
    public boolean addFeatureList(ArrayList<FeatureInfo> featureInfos){
        if(faceDbHelper!=null){
            boolean b = faceDbHelper.addFeatureList(featureInfos);
            return b;
        }
        return false;
    }
    /**
     * 根据uuid查询特征
     * @param uuid
     * @return
     */
    public FeatureInfo queryFeature(String uuid){
        if(faceDbHelper!=null){
            FeatureInfo featureInfo = faceDbHelper.queryFeature(uuid);
            return featureInfo;
        }
        return null;
    }

    /**
     * 删除特征
     * @param uuid
     * @return
     */
    public boolean removeFeatureInfoByUUID(String uuid){
        if(faceDbHelper!=null){
            boolean b = faceDbHelper.removeFeatureInfoByUUID(uuid);
            return b;
        }
        return false;
    }
    /**
     * 获取图片信息
     * @param bitmap
     * @return
     */
    public DbAddResult getReturnDetail(Bitmap bitmap){
        if(faceDbHelper!=null){
            DbAddResult  dbAddResult = faceDbHelper.addReturnDetail(bitmap);
            return dbAddResult;
        }
        return null;
    }
    /**
     * 预览数据检测
     * @param rgbData
     * @param irData
     */
    public void setData(byte[] rgbData,byte[] irData){
        if(videoFace!=null) {
            if(openLive)detectLive(rgbData,irData);
            videoFace.setData(new VideoInput(rgbData));
        }
    }
    private byte[] getSpin(int sp,byte[] data,int width,int height){
        switch (sp){
            case 90:
                return CameraUtil.rotateYUV420Degree90(data, width, height);
            case 180:
                return CameraUtil.rotateYUV420Degree180(data, width, height);
            case 270:
                return CameraUtil.rotateYUV420Degree270(data, width, height);
        }
        return data;
    }
    private void detectLive(byte[] rgbData,byte[] irData) {
        boolean rgbLive = false,irLive =false;
        if(rgbData!=null&&rgbData.length>0) {
            List<FaceBox> faceBoxes = engineWrapper.detectFace(rgbData, PREVIEW_WIDTH, PREVIEW_HEIGHT, 0);
            for (int i = 0; i < faceBoxes.size(); i++) {
                float v = engineWrapper.detectLive(rgbData, PREVIEW_WIDTH, PREVIEW_HEIGHT, 0, faceBoxes.get(i));
                if (v > 0.92f) {
                    rgbLive = true;
                } else {
                    isLive = false;
                    return;
                }
            }
        }
        if(irData!=null&&irData.length>0) {
            List<FaceBox> faceBoxes = engineWrapper.detectFace(irData, PREVIEW_WIDTH, PREVIEW_HEIGHT, 0);
            for (int i = 0; i < faceBoxes.size(); i++) {
                float v = engineWrapper.detectLive(irData, PREVIEW_WIDTH, PREVIEW_HEIGHT, 0, faceBoxes.get(i));
                if (v > 0.92f) {
                    irLive = true;
                } else {
                    isLive = false;
                    return;
                }
            }
        }
        if(rgbLive&&irLive){
            isLive = true;
        }else {
            if(irData==null||irData.length<=0)
                isLive = true;
            else
                isLive = false;

        }
    }
    private FaceModelCallBack faceModelCallBack;
    public void setFaceModelCallBack(FaceModelCallBack faceModelCallBack){
        this.faceModelCallBack  = faceModelCallBack;
    }
    public  interface FaceModelCallBack{
        void ModelCallBack(FaceModel faceModel,boolean isLive);
    }
}
