package faceapi.live;


import android.content.res.AssetManager;

import com.mv.engine.FaceBox;
import com.mv.engine.FaceDetector;
import com.mv.engine.Live;

import java.util.List;

public final class EngineWrapper {
    private FaceDetector faceDetector;
    private Live live;
    private AssetManager assetManager;

    public  boolean init() {
        int ret = this.faceDetector.loadModel(this.assetManager);
        if (ret == 0) {
            ret = this.live.loadModel(this.assetManager);
            return ret == 0;
        } else {
            return false;
        }
    }

    public final void destroy() {
        this.faceDetector.destroy();
        this.live.destroy();
    }

    public List<FaceBox> detectFace(byte[] yuv, int width, int height, int orientation) {
        return this.faceDetector.detect(yuv, width, height, orientation);
    }

    public  float detectLive(byte[] yuv, int width, int height, int orientation, FaceBox faceBox) {
        return this.live.detect(yuv, width, height, orientation, faceBox);
    }

    public EngineWrapper(AssetManager assetManager) {
        this.assetManager = assetManager;
        this.faceDetector = new FaceDetector();
        this.live = new Live();
    }
}
