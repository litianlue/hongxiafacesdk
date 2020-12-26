package faceapi.engine;


import com.rokid.citrus.citrusfacesdk.Face;

public interface IImageDbEngine extends IRokidFaceEngine {

    int imageEngineDBAdd(Face face, String UUID);


}
