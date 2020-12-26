package faceapi.engine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.Log;
import android.util.LruCache;
import android.util.Pair;

import com.rokid.citrus.citrusfacesdk.Face;
import com.rokid.citrus.citrusfacesdk.Param.ParamDet;
import com.rokid.citrus.citrusfacesdk.Param.ParamEngine;
import com.rokid.citrus.citrusfacesdk.Param.ParamFaceInfo;
import com.rokid.citrus.citrusfacesdk.Param.ParamIQA;
import com.rokid.citrus.citrusfacesdk.Param.ParamTrack;
import com.rokid.citrus.utils.CMat;
import com.rokid.facelib.RokidFace;
import com.rokid.facelib.conf.DetectFaceConf;
import com.rokid.facelib.model.FaceCache;
import com.rokid.facelib.model.FaceRecogResult;
import com.rokid.facelib.utils.FaceBitmapUtils;
import com.rokid.facelib.utils.FaceFileUtils;
import com.rokid.facelib.utils.FaceLogger;
import com.rokid.facelib.utils.VideoFaceEngineTools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VideoFaceEngine extends RokidFaceEngine implements IRokidFaceEngine {

    private static final int QUALITY_GOOD_ENOUGH = 70;

    private static final String TAG = "[FaceLib][VideoFaceEngine]";
    /**
     * 当前帧的faceList
     */
    private List<Face> faceList;

    /**
     * 当前帧的大人脸队列
     */
    private List<Face> bigFaceList;

    /**
     * 当前帧的小人脸队列
     */
    private List<Face> smallFaceList;

    /**
     * 需要去做align的list
     */
    private List<Face> alignList;
    /**
     * 去做识别的lruCache
     */
    private LruCache<Integer, FaceCache> lruCache;

    /**
     * 做faceAlign的face的id，所组成的集合，用于face的排序
     */
    private ArrayList<Integer> preAlignIds;

    private int NPU_WIDTH = 1280;
    private int NPU_HEIGHT = 720;

    private ArrayList<Integer> ids = new ArrayList<>();

//    private TrackKernel trackKernel;
//    private AlignKernel alignKernel;
//    private RecogKernel recogKernel;

    public static final String SYN_FACE_LIST = "SYN_FACE_LIST";
    private long startTime;

    public VideoFaceEngine(Context context, DetectFaceConf detectFaceConf) {
        super(context);
        init(context, detectFaceConf);
    }

    private void init(Context context, DetectFaceConf detectFaceConf){
        this.dConfig = detectFaceConf;
        ParamEngine pm = new ParamEngine();
        pm.poolnum = detectFaceConf.poolNum;
        pm.w = detectFaceConf.width;
        pm.h = detectFaceConf.height;
        pm.detscale = 1;
        pm.usedetscale = 0;
        pm.useroi = 0;
        pm.usemovedetect = 0;
        engineId = faceSDK.createVideoEngin(pm);

        ParamDet paramDet = new ParamDet();
        paramDet.keep = ParamDet.KEEPSTRATEGY.CENTER_PRIORITY;
        paramDet.minsz = detectFaceConf.minSize;
        paramDet.maxsz = detectFaceConf.maxSize;
        paramDet.maxface = detectFaceConf.detectMaxFace;
        paramDet.with_pts = 0;
        ParamIQA paramIQA = new ParamIQA();
        paramIQA.type = ParamIQA.IQA_SHARPNESS | ParamIQA.IQA_HEADPOSE;
        ParamTrack paramTrack = new ParamTrack();
        ParamFaceInfo paramFaceInfo = new ParamFaceInfo();
        paramFaceInfo.need_iqa = true;
        paramFaceInfo.topk = 1;
//        paramFaceInfo.type = ParamFaceInfo.INFOTYPE_RECG;
        faceSDK.setParam(paramDet, paramIQA, paramFaceInfo, paramTrack);

        FaceLogger.i(TAG,"init param dconfig:"+ detectFaceConf);

//        trackKernel = new TrackKernel(dFaceConf,faceSDK);
//        alignKernel = new AlignKernel(dFaceConf,faceSDK);

        //识别设置
        lruCache = new LruCache<>(detectFaceConf.poolNum);
    }



    @Override
    public void dconfig(DetectFaceConf config) {
        dConfig = config;
        FaceLogger.i(TAG,"dconfig:"+config);
    }


    private long index = 0;

    /**
     * detect不能连续调用两次，中间需要间隔一次track
     */
    private boolean tracked = false;
    @Override
    public void setData(byte[] data) {

        startTime = SystemClock.elapsedRealtime();
        if (faceSDK != null) {
            if(cMat == null){
                cMat = new CMat();
            }
            if(RokidFace.npuModel){
                cMat.setWithRoi(data,dConfig.width,dConfig.height,1,1,(byte)0,0,0,1,1);
            }else{
                cMat.setWithRoi(data,dConfig.width,dConfig.height,1,1,(byte)0,dConfig.xywh[0],dConfig.xywh[1],dConfig.xywh[2],dConfig.xywh[3]);
            }

//            //1.track
//            faceList =  trackKernel.track(cMat);
//            //2.FaceAlign
//            alignKernel.setFaceList(faceList);
//            alignKernel.setFaceCaches(lruCache);
//
//            faceList = alignKernel.faceAlign();
//
//            if(sConfig!=null&&sConfig.isRecog){
//
//                if(recogKernel == null) {
//                    recogKernel = new RecogKernel(sConfig, faceSDK, searchEngine);
//                }
//
//                if(faceList!=null&&faceList.size()>0) {
//                    lruCache = alignKernel.updateCache();
//                }
//            }

            synchronized (SYN_FACE_LIST) {
                faceList = faceSDK.Track(cMat);
                tracked = true;
                if(RokidFace.npuModel){
                    RectF roiRectF = new RectF(dConfig.xywh[0],dConfig.xywh[1],dConfig.xywh[0]+dConfig.xywh[2],dConfig.xywh[1]+dConfig.xywh[3]);
                    faceList = VideoFaceEngineTools.filterRoi(faceList,roiRectF);
                }
//                faceList = VideoFaceEngineTools.sortFacesByPreAlignId(preAlignIds,faceList);
                faceList = VideoFaceEngineTools.sortFacesByArea(faceList);
            }

            //如果是单人模式，则每帧对最大人脸做faceAlign
            if(dConfig!=null && dConfig.singleRecogModel){
                faceAlignMaxFace();
            }

            if(sConfig!=null&&sConfig.isRecog&&faceList!=null&&faceList.size()>0){
                Log.i("engine","ENGINE IQA TEST frame_index:"+data[0]);
                updateLru();
            }

//            //计算超时
//            Map<Integer,FaceCache> map= lruCache.snapshot();
//            for (Map.Entry<Integer, FaceCache> entry : map.entrySet()){
//                FaceCache faceCache= entry.getValue();
////            sb_ids.append(faceCache.face.getTrackid()+",");
////            keys.append(entry.getKey()+",");
//                if(sConfig!=null&&sConfig.isRecog&&searchEngine!=null){
//                    boolean noface = faceCache.searchResult==null;
//                    if(faceCache.createTime == 0){
//                        faceCache.outTime = false;
//                    }else {
//                        if (noface && (SystemClock.elapsedRealtime() - faceCache.createTime) > sConfig.outTime) {
//                            faceCache.outTime = true;
//                        }
//
//                        if ((SystemClock.elapsedRealtime() - faceCache.createTime) > sConfig.recogInterval) {
//                            faceCache.outTime = false;
//                            faceCache.createTime = 0;
//                            faceCache.faceRecogTime = 0;
//                        }
//                    }
//                }
//            }
        }
    }

    private void faceAlignMaxFace() {
        if(faceList==null||faceList.size()==0){
            return;
        }
        Face maxFace = null;
        for(Face face:faceList){
            if(maxFace == null){
                maxFace = face;
            }else{
                if(face.getBox().area()>maxFace.getBox().area()){
                    maxFace = face;
                }
            }
        }
        faceAlign(maxFace);
        isFaceAlign = false;
    }

    /**
     * 当前帧的人脸中有一张人脸质量不好
     */
    private volatile boolean oneFaceBadQulity = false;

    /**
     * 正在做faceAlign
     */
    private boolean isFaceAlign = false;

    private void updateLru(){
        if(alignList == null){
            alignList = new ArrayList<>();
        }else {
            alignList.clear();
        }
        for (Face face : faceList) {
            FaceCache faceCache = lruCache.get(face.getTrackid());
            if(faceCache!=null && (faceCache.searchResult!=null||faceCache.qualityGoodEnough)){
                FaceLogger.i(TAG,"qualityGoodEnough");
                continue;
            }
            alignList.add(face);

            /**
             * 1.将faceList中的face包装后添加到LRUcache
             * 2.对适合的人脸做faceAlign
             */

            if(faceCache == null){
                faceCache = new FaceCache();
                faceCache.face = face;
                faceCache.createTime = 0;
            }
            if(face.isGoodQuality() == 1){
                faceCache.readyRecog = true;
            }else{
                oneFaceBadQulity = true;
            }
            lruCache.put(face.getTrackid(),faceCache);
        }

//        List<Face> alignList = faceList;


        synchronized (SYN_FACE_LIST) {
            if(FaceLogger.LOG_LEVEL<= FaceLogger.LOG_LEVEL_INFO) {
                StringBuilder trackids = new StringBuilder();
                for (Face face : faceList) {
                    trackids.append(face.getTrackid()).append(" box:").append(face.getBox()).append("    ");
                }
                FaceLogger.i(TAG, "faceList ids:" + trackids);
            }

            if(alignList.size()>2){
                bigFaceList = alignList.subList(0,alignList.size()/2);
                bigFaceList = VideoFaceEngineTools.sortFacesByPreAlignId(preAlignIds,bigFaceList);
                smallFaceList = alignList.subList(alignList.size()/2+1,alignList.size());
                smallFaceList = VideoFaceEngineTools.sortFacesByPreAlignId(preAlignIds,smallFaceList);
            }

            if(FaceLogger.LOG_LEVEL<= FaceLogger.LOG_LEVEL_INFO) {
                StringBuilder strAlign = new StringBuilder();
                for (Face face : alignList) {
                    strAlign.append(face.getTrackid()).append(",");
                }
                FaceLogger.i(TAG, "alignList ids:" + strAlign);
            }


            /**
             * 三帧数据中有两帧数据分给大人脸list，一帧分给小人脸list。
             */
            List<Face> finalList = alignList.size() > 2 ? (index++ % 3 == 1 ? smallFaceList : bigFaceList) : alignList;

            assert finalList != null;
            for (Face face : finalList) {
                long alignSpendTime = SystemClock.elapsedRealtime() - startTime;
                if (alignSpendTime < 25 || !isFaceAlign) {
                    faceAlign(face);
                }
            }
            isFaceAlign = false;
        }


//        StringBuilder sb_ids  = new StringBuilder();
//        StringBuilder keys  = new StringBuilder();

        Map<Integer,FaceCache> map= lruCache.snapshot();
        for (Map.Entry<Integer, FaceCache> entry : map.entrySet()){
            FaceCache faceCache= entry.getValue();
//            sb_ids.append(faceCache.face.getTrackid()+",");
//            keys.append(entry.getKey()+",");
            if(sConfig!=null&&sConfig.isRecog&&searchEngine!=null){
                boolean noface = faceCache.searchResult==null;
                if(faceCache.createTime == 0){
                    faceCache.outTime = false;
                }else {
                    if (noface && (SystemClock.elapsedRealtime() - faceCache.createTime) > sConfig.outTime) {
                        faceCache.outTime = true;
                    }

                    if ((SystemClock.elapsedRealtime() - faceCache.createTime) > sConfig.recogInterval) {
                        faceCache.outTime = false;
                        faceCache.createTime = 0;
                        faceCache.faceRecogTime = 0;
                    }
                }
            }
        }
//        Log.i(TAG,"lruCache ids:"+sb_ids.toString());
//        Log.i(TAG,"lruCache keys:"+keys.toString());
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
            FaceCache faceCache = lruCache.snapshot().get(face.getTrackid());
            if(faceCache!=null){
                item.recogOutTime = faceCache.outTime;
                item.faceAlignTime = faceCache.faceAlignTime;
                item.faceRecogTime = faceCache.faceRecogTime;
                item.qualityGoodEnough = faceCache.qualityGoodEnough;
                List<Pair<String, Float>> pairList = faceCache.searchResult;
                if (pairList != null) {
                    item.setSearchResult(pairList);
                    if(item.recogImage == null) {
                        FaceLogger.i(TAG,"item.recogImage == null");
                        item.recogImage = face.getImage();

                        if(FaceLogger.SAVE_DEBUG_BITMAP_RCOG && item.recogImage!=null){
                            Bitmap bm = FaceBitmapUtils.convertGreyImage(item.recogImage.data,item.recogImage.width,item.recogImage.height);
                            File file  = new File(FaceLogger.RECOG_BM_PATH);
                            if(!file.exists()){
                                file.mkdirs();
                            }
                            FaceFileUtils.saveBitmap(bm,FaceLogger.RECOG_BM_PATH+pairList.get(0).first+"-"+pairList.get(0).second+".png");
                        }

                    }
                }
            }
            item.setFace(face);
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

    volatile boolean detect;
    @Override
    public void detect() {
        if (faceSDK != null && cMat!=null ) {
            detect = true;
            if(tracked) {
                faceSDK.detectVideo(cMat);
                tracked = false;
            }
            detect = false;
        }
    }


//    int recogTime = 0;
    public void recog(Face face) {
        synchronized (face.getTrackid()+"") {
            if (faceSDK != null && faceList.contains(face) && face.isGoodQuality() == 1) {
//            long time = SystemClock.elapsedRealtime();
                FaceCache faceCache = lruCache.get(face.getTrackid());
//            Bitmap bm = FaceBitmapUtils.convertGreyImage(faceCache.recogImage.data, faceCache.recogImage.width, faceCache.recogImage.height);
//            FaceFileUtils.saveBitmap(bm,"/sdcard/test/"+(recogTime++)+".png");
                //防止连续两次识别
                if(faceCache!=null && faceCache.aligned && faceCache.updated){
                    if(faceCache.searchResult != null|| faceCache.qualityGoodEnough){
                        return;
                    }

//                    if(faceSDK.UpdateFace(face)!= 0){
//                        return;
//                    }
                    if (faceSDK.ExtractFeature(face) != 0) {
                        return;
                    }
                    long curTime = System.currentTimeMillis();
    //            LogWriter.getInstance().write("trackId:"+face.getTrackid()+"ExtractFeature spend time:"+(SystemClock.elapsedRealtime()-time)+"\n");
    //            time = SystemClock.elapsedRealtime();
                    List<Pair<String, Float>> pairList = searchEngine.Search(face, 1, 5);
                    FaceLogger.i(TAG,"----------search spend:"+(System.currentTimeMillis() - curTime));
    //            LogWriter.getInstance().write("trackId:"+face.getTrackid()+"Search spend time:"+(SystemClock.elapsedRealtime()-time)+"\n");
                    if (pairList != null && pairList.size() > 0) {
                        FaceLogger.i(TAG, "--------------- pairList:" + pairList.get(0));
                        //判断阈值

//                        Bitmap bmp = FaceBitmapUtils.convertGreyImage(faceCache.recogImage.data, faceCache.recogImage.width, faceCache.recogImage.height);
//                        Canvas canvas = new Canvas(bmp);
//                        Paint paint = new Paint();
//                        paint.setColor(Color.BLUE);
//                        float[] pts = face.getPTS();
//                        for (int i=0;i<pts.length;i=i+2) {
//                            canvas.drawCircle(pts[i],pts[i+1],2,paint);
//                        }
//                        FaceFileUtils.saveBitmap(bmp, "/sdcard/facelibTest/Recg/" + pairList.get(0).second + "-" + "goodPose:" + face.isGoodQuality() + ".png");


                        if (pairList.get(0).second > sConfig.targetScore) {
                            FaceLogger.i(TAG, "------- recog result > targetScore-------- trackid:" + face.getTrackid() + " uuid:" + pairList.get(0).first + " score:" + pairList.get(0).second+" faceFrameId:"+faceCache.recogFrameId);
                            faceCache.searchResult = pairList;
                            lruCache.put(face.getTrackid(), faceCache);
                        }else{
                            FaceLogger.i(TAG, "------- recog result < targetScore-------- trackid:" + face.getTrackid() + " uuid:" + pairList.get(0).first + " score:" + pairList.get(0).second+" faceFrameId:"+faceCache.recogFrameId);
                        }
                        faceCache.faceRecogTime++;
                        if (faceCache.faceRecogTime == 1) {
                            //第一次识别，开始计时
                            faceCache.createTime = SystemClock.elapsedRealtime();
                        }
                    } else {
                        FaceLogger.i(TAG, "--------------- pairList: null");
                    }

                    faceCache.qualityGoodEnough = face.getIQA()>=QUALITY_GOOD_ENOUGH;

                    faceCache.aligned = false;
                }
            }
        }
    }

    volatile boolean recog;
    @Override
    public void recogAll() {
        if (faceList == null || faceList.size() == 0
//                || recogKernel == null
        ) {
            return;
        }
        recog = true;
        for(Face face:faceList){
            recog(face);
        }

//        Map<Integer,FaceCache> map= lruCache.snapshot();
//        for (Map.Entry<Integer, FaceCache> entry : map.entrySet()){
//            FaceCache faceCache= entry.getValue();
////            getIds();
//            if(
////                    faceCache.readyRecog
////                    && ids.contains(entry.getKey())
////                    &&
//            faceCache.searchResult==null && !faceCache.outTime
//                    && faceCache.aligned){
//                    recog(faceCache.face);
//                faceCache.aligned = false;
//                Log.i(TAG,"----debug----face recog id:"+entry.getKey());
//            }
//            Log.i(TAG,"-----------recog id:");
//        }
        recog = false;
        oneFaceBadQulity = false;
    }


    @Override
    public void destroy() {
        FaceLogger.i(TAG,"destroy");
        while (recog || detect){
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(faceList!=null) {
            faceList.clear();
        }

        if (faceSDK != null) {
            faceSDK.Release();
//            faceSDK.Destroy();
            faceSDK = null;
        }
        if(searchEngine!=null){
            searchEngine.Destroy();
        }
    }

    @Override
    public void stop() {
//        if(faceList!=null && faceList.size()>0){
//            for(Face face:faceList){
//                face.clear();
//            }
//        }
        faceSDK.stop();
    }

    @Override
    public void reStart() {
        faceSDK.reStart();
    }


//    /**
//     * 对某一个id的Face做FaceALign
//     * @param trackId
//     * @return
//     */
//    @Override
//    public FaceDO faceAlign(int trackId) {
//        if(faceSDK == null||faceList.size()==0){
//            return null;
//        }
//        for(Face face : faceList){
//            if(face.getTrackid() == trackId){
//                if(faceSDK.PrepareAna(face)!=0){
//                    return null;
//                }
//                if(faceSDK.FaceQuality(face)!=0){
//                    return null;
//                }
//                FaceDO faceDO = new FaceDO();
//                float left = face.getBox().x;
//                float top = face.getBox().y;
//                float right = face.getBox().x+face.getBox().width;
//                float bottom = face.getBox().y+face.getBox().height;
////                Log.i(TAG,"roiTest1  left:"+left+" top:"+top+" right:"+right+" bottom:"+bottom);
//                //相对于原图区域
//                if(dConfig!=null&&dConfig.roiRect!=null&&!RokidFace.npuModel){
//                    left = (left*dConfig.roiRect.width()+dConfig.roiRect.left)/(float) dConfig.width;
//                    top = (top*dConfig.roiRect.height()+dConfig.roiRect.top)/(float) dConfig.height;
//                    right = (right*dConfig.roiRect.width()+dConfig.roiRect.left)/(float) dConfig.width;
//                    bottom = (bottom*dConfig.roiRect.height()+dConfig.roiRect.top)/(float) dConfig.height;
////                    Log.i(TAG,"roiTest2  left:"+left+" top:"+top+" right:"+right+" bottom:"+bottom);
//                }
//                faceDO.faceRectF = new RectF(left,top,right,bottom);
//                faceDO.trackId = face.getTrackid();
//                faceDO.goodQuality = face.isGoodQuality()==1;
//                faceDO.goodPose = face.isGoodHeadPose()==1;
//                Log.i(TAG,"goodPose:"+faceDO.goodPose);
//                faceDO.goodSharpness = face.isGoodSharpness()==1;
//                faceDO.pose = face.getPose();
//                faceDO.pts = face.getPTS();
//                return faceDO;
//            }
//        }
//        return null;
//    }


//    long alignIndex;
    private void faceAlign(Face face){
        synchronized (face.getTrackid()+"") {
            FaceCache faceCache = lruCache.get(face.getTrackid());
//        if(!isFaceAlign ) {
            //单人识别，每帧对该人脸做faceAlign
            boolean singleModel = dConfig != null && dConfig.singleRecogModel;
//            //该人脸badQuality,且已有较大变化
//            boolean badFace = face.isGoodQuality() == 0 && face.canReExtract() == 1;
//            //所有人脸都是goodFace，且该人脸没有识别结果
//            boolean noResult = !oneFaceBadQulity && faceCache != null && faceCache.searchResult == null;


            boolean noResult = faceCache != null && (faceCache.searchResult == null && !faceCache.qualityGoodEnough);
            if (singleModel ||  noResult) {
//                Log.i(TAG,"----debug----------faceAlign-----id:"+face.getTrackid());
                isFaceAlign = true;
                if (faceSDK != null) {
                    FaceLogger.i(TAG, "------- align start------ id:" + face.getTrackid());
//                    alignIndex++;
                    addPreAlignId(face.getTrackid());

                    if (faceSDK.Process(cMat, face, (byte) 0) != 0) {
                        FaceLogger.i(TAG,"faceAlign  PrepareAna error");
                        if (faceCache != null) {
                            faceCache.readyRecog = false;
                        }
                        return;
                    }
                    float bestIqa = face.getIQA();

                    if (faceSDK.FaceQuality(face) != 0) {
                        FaceLogger.i(TAG,"faceAlign  FaceQuality error");
                        if (faceCache != null) {
                            faceCache.readyRecog = false;
                        }
                        return;
                    }
//                    Log.i("test VideoFaceEngine","rect:"+face.getBox() +" iqa:"+face.getIQA());
                    Log.i("engine","ENGINE IQA TEST:"+face.getIQA()+" BOX:"+face.getBox()+" face id:"+face.getTrackid() + ";   face isGoodHeadPose = " + face.isGoodHeadPose());

                    //上一次Align时的识别次数 和 现在的识别次数相同,则取更好的人脸质量update
                    if(faceCache!=null ){
                        if(faceCache.lastALignRecogTime == faceCache.faceRecogTime){
                            if(face.getIQA()>bestIqa){
                                faceCache.recogFrameId = face.getFrameId();
                                faceSDK.UpdateFace(face);
                                faceCache.updated = true;
                            }else{
                                faceCache.updated = false;
                            }
                        }else{
                            faceCache.recogFrameId = face.getFrameId();
                            faceSDK.UpdateFace(face);
                            faceCache.updated = true;
                        }
                        faceCache.faceAlignTime++;
                        faceCache.lastALignRecogTime = faceCache.faceRecogTime;
                        faceCache.aligned = true;
                    }else{
                        faceSDK.UpdateFace(face);
                    }


                    FaceLogger.i(TAG, "------- align end------ id:" + face.getTrackid());

                    if (face.isGoodQuality() == 0) {
                        FaceLogger.saveBadFace(face.getImage());
                    }else{
                        FaceLogger.saveGoodFace(face.getImage());
                    }

//                    Log.i(TAG,"faceAlign id:"+face.getTrackid()+" isGoodQuality:"+face.isGoodQuality());

                }
            }
        }
//        }
    }

    private void addPreAlignId(int id){
        if(preAlignIds == null){
            preAlignIds = new ArrayList<>();
        }
        if(!preAlignIds.contains(id)) {
            preAlignIds.add(id);
        }
        if(preAlignIds.size()>dConfig.poolNum){
            preAlignIds.remove(0);
        }
    }
}