package faceapi.engine;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.rokid.citrus.citrusfacesdk.CitrusFaceEngine;
import com.rokid.citrus.citrusfacesdk.Face;
import com.rokid.citrus.citrusfacesdk.Feat;
import com.rokid.citrus.citrusfacesdk.Param.ParamDet;
import com.rokid.citrus.citrusfacesdk.Param.ParamFaceInfo;
import com.rokid.citrus.citrusfacesdk.Param.ParamIQA;
import com.rokid.citrus.citrusfacesdk.Param.ParamTrack;
import com.rokid.citrus.citrusfacesdk.SearchEngineFace;
import com.rokid.citrus.utils.CMat;
import com.rokid.facelib.db.FeatureDatabase;
import com.rokid.facelib.db.FeatureDbCreator;
import com.rokid.facelib.db.FeatureInfo;
import com.rokid.facelib.db.FeatureInfoDao;
import com.rokid.facelib.face.FaceDbHelper;
import com.rokid.facelib.utils.FaceBitmapUtils;
import com.rokid.facelib.utils.FaceLogger;
import com.rokid.facelib.utils.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 操作算法底层数据库的引擎
 */
public class FaceDbEngine implements IFaceDbEngine {
    private static final String TAG = "[FaceLib][FaceDbEngine]";
    private SearchEngineFace searchEngine;
    private CitrusFaceEngine faceEngine;
    private static FaceDbEngine dbEngine;
    private Context context;
    private CMat cMat;
    private FeatureDatabase featureDatabase;
    private FeatureInfoDao featureInfoDao;
    private String dbName;
    List<Face> faceList;
    public static FaceDbEngine create(Context context, String dbName) {
        if (dbEngine == null) {
            dbEngine = new FaceDbEngine(context,dbName);
        }
        return dbEngine;
    }

    public FaceDbEngine(Context context,String dbName){
        this.context = context;
        this.dbName = dbName;
    }
    @Override
    public FaceDbEngine init() {
        reset();
        return this;
    }


    private void reset(){
        faceEngine = new CitrusFaceEngine();
        faceEngine.createImgEngin();
        ParamDet paramDet = new ParamDet();
        paramDet.minsz = (float)0.01;
        paramDet.maxsz = (float)1;
        paramDet.maxface = -1;
        paramDet.keep = ParamDet.KEEPSTRATEGY.CENTER_PRIORITY;
        paramDet.with_pts = 0;
        ParamIQA paramIQA = new ParamIQA();
        paramIQA.type = ParamIQA.IQA_SHARPNESS | ParamIQA.IQA_HEADPOSE;
        ParamTrack paramTrack = new ParamTrack();
        ParamFaceInfo paramFaceInfo = new ParamFaceInfo();
        paramFaceInfo.need_iqa = true;
        paramFaceInfo.topk = 1;
//        paramFaceInfo.type = ParamFaceInfo.INFOTYPE_RECG | ParamFaceInfo.INFOTYPE_AGE | ParamFaceInfo.INFOTYPE_GENDER;
        faceEngine.setParam(paramDet, paramIQA, paramFaceInfo, paramTrack);
        featureDatabase = FeatureDbCreator.create(context,dbName);
        featureInfoDao = featureDatabase.getUserInfoDao();
        searchEngine = new SearchEngineFace();
    }



    @Override
    public void delDb() {
        File file = new File(FaceDbHelper.PATH_OUTPUT+FaceDbHelper.ENGINE_NAME);
        if(file.exists()){
            file.delete();
        }
    }
    public Face getFace(Bitmap bitmap) {
        if(cMat==null){
            cMat = new CMat();
        }
        cMat.set(FaceBitmapUtils.bitmapPrimitiveBytes(bitmap),bitmap.getWidth(),bitmap.getHeight(),4,2,(byte) 0);
        if(faceList!=null) {
            for (Face face : faceList) {
                face.clear();
            }
            faceList.clear();
        }
        faceList = faceEngine.detectImage(cMat);
        if(faceList==null||faceList.size()==0){
            FaceLogger.i(TAG,"add faceList==null");
            return null;
        }
        Face maxFace = null;
        for(Face face:faceList){
            if(maxFace==null) {
                maxFace = face;
            }
            if(face.getBox().area()>maxFace.getBox().area()){
                maxFace = face;
            }
        }

        return maxFace;
    }

    @Override
    public Face add(String uuid, Bitmap bitmap) {
        if(cMat==null){
            cMat = new CMat();
        }
        cMat.set(FaceBitmapUtils.bitmapPrimitiveBytes(bitmap),bitmap.getWidth(),bitmap.getHeight(),4,2,(byte) 0);
        if(faceList!=null) {
            for (Face face : faceList) {
                face.clear();
            }
            faceList.clear();
        }
        faceList = faceEngine.detectImage(cMat);
        if(faceList==null||faceList.size()==0){
            FaceLogger.i(TAG,"add faceList==null");
            return null;
        }
        Face maxFace = null;
        for(Face face:faceList){
            if(maxFace==null) {
                maxFace = face;
            }
            if(face.getBox().area()>maxFace.getBox().area()){
                maxFace = face;
            }
        }
//        if(faceEngine.PrepareAnaI(cMat,maxFace,(byte) 0)!=0){
        if(faceEngine.Process(cMat, maxFace, (byte) 0) != 0){
            return null;
        }

        if(faceEngine.UpdateFace(maxFace)!=0){
            return null;
        }

        if(faceEngine.ExtractFeature(maxFace)!=0){
            return null;
        }
        Log.d("fsadfs","Process:"+faceEngine.Process(cMat, maxFace, (byte) 0)+" UpdateFace:"+faceEngine.UpdateFace(maxFace)+" ExtractFeature:"+faceEngine.ExtractFeature(maxFace)+" quere:");
        Feat feature = maxFace.getFeature();
        FaceLogger.i(TAG,"feature:"+ Arrays.toString(feature.feature));
        FeatureInfo featureInfo = new FeatureInfo(uuid, feature.feature);
        featureInfoDao.addFeatureInfo(featureInfo);
        return maxFace;
    }
    public boolean addFeature(float[] feature,String uuid){
        FeatureInfo featureInfo = new FeatureInfo(uuid,feature);
        try {
            featureInfoDao.addFeatureInfo(featureInfo);
        }catch (Exception e){
            return false;
        }
        return true;
    }

    @Override
    public boolean update(String uuid, Bitmap bitmap) {
        if(cMat == null){
            cMat = new CMat();
        }
        cMat.set(FaceBitmapUtils.bitmapPrimitiveBytes(bitmap),bitmap.getWidth(),bitmap.getHeight(),4,2,(byte) 0);
        List<Face> faceList = faceEngine.detectImage(cMat);
        if(faceList==null||faceList.size()==0){
            return false;
        }
        Face maxFace = null;
        for(Face face:faceList){
            if(maxFace==null) {
                maxFace = face;
            }
            if(face.getBox().area()>maxFace.getBox().area()){
                maxFace = face;
            }
        }

        if(faceEngine.Process(cMat,maxFace,(byte) 0)!=0){
            return false;
        }

        if(faceEngine.UpdateFace(maxFace)!=0){
            return false;
        }

        if(faceEngine.ExtractFeature(maxFace)!=0){
            return false;
        }
        Feat feat = maxFace.getFeature();
        FeatureInfo featureInfo = new FeatureInfo(uuid,feat.feature);
        try {
            featureInfoDao.updateFeatureInfo(featureInfo);
        }catch (Exception e){
            return false;
        }
//        searchEngine.Update(maxFace,uuid);
        return true;
    }

    /**
     * remove
     * @param uuid
     * @return
     */
    @Override
    public boolean remove(String uuid) {
        try {
            featureInfoDao.removeFeatureInfoByUUID(uuid);
        }catch (Exception e){
            return false;
        }

        return true;
    }

    public FeatureInfo queryFeature(String uuid){
        FeatureInfo featureInfo;
        try {
            featureInfo = featureInfoDao.getFeatureInfo(uuid);
        }catch (Exception e){
            return null;
        }
        return featureInfo;
    }

    public boolean addFeature(FeatureInfo featureInfo){
        try {
            featureInfoDao.addFeatureInfo(featureInfo);
        }catch (Exception e){
            return false;
        }
        return true;
    }

    public boolean addFeatureList(ArrayList<FeatureInfo> featureInfos){
        try {
            featureInfoDao.addFeatureInfoAll(featureInfos);
        }catch (Exception e){
            return false;
        }
        return true;
    }

    public boolean removeFeatureInfoByUUID(String uuid){
        try {
            featureInfoDao.removeFeatureInfoByUUID(uuid);
        }catch (Exception e){
            return false;
        }
        return true;
    }




    @Override
    public boolean contain(String uuid) {
        return false;
    }

    @Override
    public int dbSize() {
        return 0;
    }

    @Override
    public String getUUID(int index) {
        return null;
    }

    @Override
    public void destroy() {
        if(featureDatabase!=null){
            FaceLogger.i(TAG,"featureDatabase destroy");
            featureDatabase.close();
        }
        if(searchEngine!=null){
            FaceLogger.i(TAG,"searchEngine destroy");
            searchEngine.Destroy();
        }
    }

    @Override
    public void save() {
        //默认maxFace是10000
        save(10000);
    }

    @Override
    public void save(int maxFace) {
        int step = 1000;
        int times = maxFace/step;

        searchEngine.Reset(128,maxFace,1);
        for(int i=0;i<=times;i++){
            List<FeatureInfo> featureInfoList=featureInfoDao.getList(i==times?maxFace%step:step,i*step);
            if(featureInfoList!=null){
                FaceLogger.i(TAG,"featureInfoList size:"+featureInfoList.size());
                for(FeatureInfo featureInfo :featureInfoList){
                    searchEngine.AddWithFeat(new Feat(StringUtils.StringToFloatArray(featureInfo.feature)),featureInfo.uuid);
                }
            }
        }

        File dir = new File(FaceDbHelper.PATH_OUTPUT);
        if(!dir.exists()){
            dir.mkdirs();
        }
        searchEngine.BuildIndex();
        searchEngine.Save(FaceDbHelper.PATH_OUTPUT+FaceDbHelper.ENGINE_NAME);
    }

}
