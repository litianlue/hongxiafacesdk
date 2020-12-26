package faceapi.engine;

import com.rokid.citrus.citrusfacesdk.Face;
import com.rokid.facelib.conf.DetectFaceConf;
import com.rokid.facelib.conf.RecogFaceConf;
import com.rokid.facelib.model.FaceRecogResult;

import java.util.List;

public interface IRokidFaceEngine {

    int getWidth();

    int getHeight();

    void dconfig(DetectFaceConf config);

    void sconfig(RecogFaceConf config);

    void setData(byte[] data);

    List<Face> getFaceList();

    FaceRecogResult getFaceSearchResult(Face face);

    List<FaceRecogResult> getFaceSearchResultList();

    void detect();

//    void recog(Face face);

    void recogAll();

    void destroy();

    void stop();

    void reStart();
}
