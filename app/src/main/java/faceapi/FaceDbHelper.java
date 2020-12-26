package faceapi;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.rokid.citrus.citrusfacesdk.Face;
import com.rokid.citrus.citrusfacesdk.Feat;
import com.rokid.facelib.db.FeatureInfo;

import com.rokid.facelib.face.DbAddResult;
import com.rokid.facelib.utils.FaceFileUtils;
import com.rokid.facelib.utils.FaceLogger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import faceapi.engine.FaceDbEngine;

/**
 * 维护特征值数据库
 */
public class FaceDbHelper {

    private static final String TAG = "[FaceLib][FaceDbHelper]";

    public static String PATH_OUTPUT = "/sdcard/rokid/facesdk/";

    public static final String FEATURE_DB_NAME = "feature.db";

    public static final String ENGINE_NAME = "SearchEngine.bin";

    FaceDbEngine faceDbEngine;
    private Context mContext;
    public FaceDbHelper(Context context) {
        mContext = context;
    }

    //输入数据库的名字，在database目录下进行操作
    public void createDb() {
        faceDbEngine = FaceDbEngine.create(mContext,FEATURE_DB_NAME).init();
    }


    /**
     * 导出feature数据库
     */
    public void exportFeatDb(){
        File featDbFile = new File(mContext.getDatabasePath(FEATURE_DB_NAME).getAbsolutePath());
        try {
            File file = new File(PATH_OUTPUT);
            if(!file.exists()){
                file.mkdirs();
            }
            FaceFileUtils.copyFileByChannel(featDbFile, new File(PATH_OUTPUT + FEATURE_DB_NAME));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置导出的SearchEngine.bin的目录
     */
    public void setEnginePath(String enginePath){
        File file = new File (enginePath);
        if(!file.exists()){
            file.mkdirs();
        }
        PATH_OUTPUT = enginePath;
    }

    /**
     * 替换feature数据库
     */
    public void replaceFeatDb(String dbPath){
        File dbFile = new File(dbPath);
        if(dbFile.exists()){
            try {
                File feature_db = new File(dbPath);
                FaceFileUtils.copyFileByChannel(feature_db, new File(mContext.getDatabasePath(FEATURE_DB_NAME).getAbsolutePath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            FaceLogger.e(TAG,"db not exit!!!!!!!!");
        }
    }


    public boolean remove(String uuid){
        try {
            faceDbEngine.remove(uuid);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    /**
     * 添加 返回uuid
     * @param bm
     * @return
     */
    public String add(@NonNull Bitmap bm){
        String uuid = UUID.randomUUID().toString();
        Face face = faceDbEngine.add(uuid,bm);
        if(face!=null){
            return uuid;
        }else{
            return null;
        }
    }

    /**
     * 添加
     * @param bm 图片
     * @param uuid 该图片对应的uuid
     */
    public String add(@NonNull Bitmap bm,String uuid){
        Face face =faceDbEngine.add(uuid,bm);
        if(face!=null){
            return uuid;
        }else{
            return null;
        }

    }
    /**
     * @param bm 图片
     */
    public float[] getFeature(@NonNull Bitmap bm){
        Face face =faceDbEngine.getFace(bm);
        if(face!=null){
            Feat feature = face.getFeature();
            return feature.feature;
        }else
            return null;
    }


    /**
     * 添加 返回
     * @param bm
     * @return
     */
    public DbAddResult addReturnDetail(@NonNull Bitmap bm){
        DbAddResult result= new DbAddResult();
        String uuid = UUID.randomUUID().toString();
        Face face = faceDbEngine.getFace(bm);
        if(face!=null){
            result.featId = uuid;
            int width = bm.getWidth();
            int height = bm.getHeight();
            result.maxFaceRect.left = (int) (face.getBox().x*width);
            result.maxFaceRect.top = (int) (face.getBox().y*height);
            result.maxFaceRect.right = (int) ((face.getBox().x+face.getBox().width)*width);
            result.maxFaceRect.bottom= (int) ((face.getBox().y+face.getBox().height)*height);
            return result;
        }else{
            return null;
        }
    }

    /**
     * 根据uuid查询特征
     * @param uuid
     * @return
     */
    public FeatureInfo queryFeature(String uuid){
        return faceDbEngine.queryFeature(uuid);
    }

    /**
     * 添加特征
     * @param featureInfo
     * @return
     */
    public boolean addFeature(FeatureInfo featureInfo){
        return faceDbEngine.addFeature(featureInfo);
    }

    /**
     * 添加特征
     * @param feature
     * @param uuid
     * @return
     */
    public boolean addFeature(float[] feature,String uuid){
        return faceDbEngine.addFeature(feature,uuid);
    }

    /**
     *
     * @param featureInfos
     * @return
     */
    public boolean addFeatureList(ArrayList<FeatureInfo> featureInfos){
        return faceDbEngine.addFeatureList(featureInfos);
    }

    public boolean removeFeatureInfoByUUID(String uuid){
        return faceDbEngine.removeFeatureInfoByUUID(uuid);
    }


    /**
     * 保存数据库
     * 最大人脸数，默认10000
     */
    public void save(){
        faceDbEngine.save();
    }

    /**
     * @param maxFace 最大人脸数
     */
    public void save(int maxFace){
        faceDbEngine.save(maxFace);
    }

    public void close(){
        if(faceDbEngine!=null) {
            faceDbEngine.destroy();
        }
    }

    public void clearDb() {

        File dbFile2 = new File(PATH_OUTPUT + ENGINE_NAME);
        dbFile2.delete();

        File dbFile3 = new File(mContext.getDatabasePath(ENGINE_NAME).getAbsolutePath());
        dbFile3.delete();

        File dbFile4 = new File(mContext.getDatabasePath(FEATURE_DB_NAME).getAbsolutePath());
        dbFile4.delete();

        File dbFile5 = new File(PATH_OUTPUT + FEATURE_DB_NAME);
        dbFile5.delete();
    }
}