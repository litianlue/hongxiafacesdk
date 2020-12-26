package faceapi.engine;

import android.content.Context;
import android.util.Log;

import com.rokid.citrus.citrusfacesdk.Face;
import com.rokid.citrus.citrusfacesdk.Param.ParamDet;
import com.rokid.citrus.citrusfacesdk.Param.ParamFaceInfo;
import com.rokid.citrus.citrusfacesdk.Param.ParamIQA;
import com.rokid.citrus.citrusfacesdk.Param.ParamTrack;
import com.rokid.citrus.utils.CMat;
import com.rokid.facelib.BuildConfig;
import com.rokid.facelib.model.FaceRecogResult;

import java.util.ArrayList;
import java.util.List;

public class ImageFaceEngine extends RokidFaceEngine implements IRokidFaceEngine {

    private static final String TAG = ImageFaceEngine.class.getSimpleName();
    private List<Face> faceList;

    public ImageFaceEngine(Context context) {
        super(context);
        init();
    }

    private void init(){
        engineId = faceSDK.createImgEngin();
        ParamDet paramDet = new ParamDet();
        paramDet.minsz = (float)0.05554;
        paramDet.maxsz = (float)1;
        paramDet.maxface = -1;
        paramDet.keep = ParamDet.KEEPSTRATEGY.CENTER_PRIORITY;
        paramDet.with_pts = 1;
        ParamIQA paramIQA = new ParamIQA();
        paramIQA.type = ParamIQA.IQA_SHARPNESS | ParamIQA.IQA_HEADPOSE;
        ParamTrack paramTrack = new ParamTrack();
        ParamFaceInfo paramFaceInfo = new ParamFaceInfo();
        paramFaceInfo.need_iqa = false;
        paramFaceInfo.topk = 1;
//        paramFaceInfo.type = ParamFaceInfo.INFOTYPE_RECG ;
        faceSDK.setParam(paramDet, paramIQA, paramFaceInfo, paramTrack);
    }

    @Override
    public void setData(byte[] data) {
        if (BuildConfig.DEBUG)
            Log.e(TAG, "setData");

        if (faceSDK != null) {
            if(cMat == null){
                cMat = new CMat();
            }
//            float rx = (float) dConfig.roiRect.left/(float) dConfig.width;
//            float ry = (float)dConfig.roiRect.top/(float)dConfig.height;
//            float rw = (float)(dConfig.roiRect.right-dConfig.roiRect.left)/(float)dConfig.width;
//            float rh = (float)(dConfig.roiRect.bottom-dConfig.roiRect.top)/(float)dConfig.height;
            cMat.set(data,dConfig.width,dConfig.height,4,2,(byte)1);
        }
    }



    @Override
    public List<Face> getFaceList() {
        if (faceList != null) {
            return faceList;
        }
        return null;
    }

    @Override
    public FaceRecogResult getFaceSearchResult(Face face) {

        FaceRecogResult item = null;
        if (searchEngine != null) {
            item = new FaceRecogResult();
            item.setFace(face);
//            List<Pair<String, Float>> pairList = searchEngine.Search(face,1,5);
//            //判断阈值
//            if(pairList!=null&&pairList.get(0).second>=sConfig.targetScore){
//                item.setSearchResult(pairList);
//            }
        }
        return item;
    }

    @Override
    public List<FaceRecogResult> getFaceSearchResultList() {
        List<FaceRecogResult> list ;
        if(faceList != null){
            list = new ArrayList<>();
            for(Face face:faceList){
                list.add(getFaceSearchResult(face));
            }
            return list;
        }
        return null;
    }

    @Override
    public void detect() {
        if (BuildConfig.DEBUG)
            Log.e(TAG, "detect");

        if (faceSDK != null) {
            faceList = faceSDK.detectImage(cMat);
            if(faceList!=null&&faceList.size()>0){
                for(Face face:faceList){
                    if(faceSDK.Process(cMat,face,(byte) 0)!=0){
                        break;
                    }
                    if(faceSDK.FaceQuality(face)!=0){
                       break;
                    }
                }
            }
        }
    }

    public void recog(Face face) {
        if (faceSDK != null) {
            if(faceSDK.UpdateFace(face)!=0){
                return;
            }
            faceSDK.ExtractFeature(face);
        }
    }

    @Override
    public void recogAll() {
        if (BuildConfig.DEBUG)
            Log.e(TAG, "recogAll");

        if (getFaceList() == null || getFaceList().size() == 0) {
            return;
        }

        for (Face face : getFaceList()) {
            recog(face);
        }
    }

    @Override
    public void destroy() {
        if (BuildConfig.DEBUG)
            Log.e(TAG, "destroy");

        if (faceSDK != null) {
            faceSDK.Release();
//            faceSDK.Destroy();
            faceSDK = null;
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public void reStart() {

    }

//
//    @Override
//    public FaceDO faceAlign(int trackid) {
//        return null;
//    }
}
