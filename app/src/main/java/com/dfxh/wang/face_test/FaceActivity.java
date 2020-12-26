package com.dfxh.wang.face_test;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.dfxh.wang.serialport_test.R;


import faceapi.CameraUtil;
import faceapi.HXFaceHelper;
import faceapi.live.EngineWrapper;

import com.mv.engine.FaceBox;
import com.rokid.camerakit.cameralibrary.view.DefaultCameraView;
import com.rokid.facelib.ImageRokidFace;
import com.rokid.facelib.RokidFace;
import com.rokid.facelib.VideoRokidFace;
import com.rokid.facelib.api.IImageRokidFace;
import com.rokid.facelib.api.IVideoRokidFace;
import com.rokid.facelib.api.ImageFaceCallback;
import com.rokid.facelib.conf.DetectFaceConf;
import com.rokid.facelib.conf.RecogFaceConf;
import com.rokid.facelib.conf.VideoDetectFaceConf;

import com.rokid.facelib.input.BitmapInput;
import com.rokid.facelib.input.VideoInput;
import com.rokid.facelib.model.FaceDO;
import com.rokid.facelib.model.FaceModel;

import java.util.List;
import java.util.UUID;

import faceapi.FaceDbHelper;
import faceapi.NV21ToBitmap;
import faceapi.PermissionUtils;

import faceapi.userdb.UserDatabase;
import faceapi.userdb.UserInfoDao;
import faceapi.view.FaceModelView;
public class FaceActivity extends AppCompatActivity {
    private static final String TAG = "FaceActivity";
    private FaceModelView faceModelView;

    private DefaultCameraView cameraView;
    private int PREVIEW_WIDTH = 1280;
    private int PREVIEW_HEIGHT = 720;
    boolean stop;
    private NV21ToBitmap nv21ToBitmap;
    private ImageView imageView;
    private IImageRokidFace imageFace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face);
        nv21ToBitmap = new NV21ToBitmap(this);
        PermissionUtils.permissionCheck(this);

        initView();
        init();

    }

    private void initView() {
        imageView = findViewById(R.id.img);
        faceModelView = findViewById(R.id.faceModelView);
        cameraView = findViewById(R.id.cameraview);
    }

    private void init() {
        HXFaceHelper.getInstance(this).initSDK();
        initCam();
        HXFaceHelper.getInstance(this).setFaceModelCallBack(new HXFaceHelper.FaceModelCallBack() {
            @Override
            public void ModelCallBack(FaceModel faceModel, boolean isLive) {

                faceModelView.setFaceModel(faceModel,isLive);
            }
        });

    }

    /**
     * 处理相机返回数据
     */
    private void initCam() {
        cameraView.addPreviewCallBack((bytes, camera) -> {
            long currentTimeMillis = System.currentTimeMillis();
            byte[] data = CameraUtil.rotateYUV420Degree90(bytes, PREVIEW_WIDTH, PREVIEW_HEIGHT);
            Log.d(TAG,"TIME:"+(System.currentTimeMillis()-currentTimeMillis));
            if(bytes!=null){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bitmap = nv21ToBitmap.nv21ToBitmap(data, PREVIEW_HEIGHT, PREVIEW_WIDTH);
                        imageView.setImageBitmap(bitmap);
                    }
                });
            }
            HXFaceHelper.getInstance(this).setData(data,null);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        cameraView.onDestroy();
    }

}
