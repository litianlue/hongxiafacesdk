package faceapi.engine;

import android.content.Context;
import android.os.SystemClock;

import com.rokid.citrus.citrusfacesdk.CitrusFaceEngine;
import com.rokid.citrus.citrusfacesdk.SearchEngineFace;
import com.rokid.citrus.utils.CMat;
import com.rokid.facelib.conf.DetectFaceConf;
import com.rokid.facelib.conf.RecogFaceConf;
import com.rokid.facelib.face.FaceDbHelper;
import com.rokid.facelib.utils.FaceLogger;

import java.io.File;

//import com.rokid.citrus.citrusfacesdk.common.FaceInfoParam;

/**
 * 人脸识别引擎，封装人脸识别C层引擎
 */
public abstract class RokidFaceEngine implements IRokidFaceEngine {

    private static final String TAG = "[FaceLib][RokidFaceEngine]";

    public static boolean DEBUG = false;

    protected CitrusFaceEngine faceSDK;
    protected SearchEngineFace searchEngine;

    protected long engineId;

    public DetectFaceConf dConfig;

    public RecogFaceConf sConfig;

    protected CMat cMat;


    public RokidFaceEngine(Context context) {
        long time = SystemClock.elapsedRealtime();
        FaceLogger.i(TAG,"cost time total:"+(SystemClock.elapsedRealtime()-time));
        faceSDK = new CitrusFaceEngine();
    }

    @Override
    public int getWidth() {
        return dConfig != null ? dConfig.width : 0;
    }

    @Override
    public int getHeight() {
        return dConfig != null ? dConfig.height : 0;
    }

    @Override
    public void dconfig(DetectFaceConf config) {
        dConfig = config;
    }

    @Override
    public void sconfig(RecogFaceConf config) {
        sConfig = config;
        searchEngine = new SearchEngineFace();
        searchEngine.Reset(128,10000,1);
        String searchEnignePath;
        if(sConfig.searchEnignePath!=null){
            searchEnignePath = sConfig.searchEnignePath;
        }else{
            searchEnignePath = sConfig.dbPath+ FaceDbHelper.ENGINE_NAME;
        }
        File file = new File (searchEnignePath);
        if(file.exists()) {
            searchEngine.Load(searchEnignePath);
        }
    }
//    protected FaceInfoParam getDefaultFaceInfo() {
//        FaceInfoParam param = new FaceInfoParam();
//        param.type = FaceInfoParam.INFOTYPE_RECG;
//        return param;
//    }
}