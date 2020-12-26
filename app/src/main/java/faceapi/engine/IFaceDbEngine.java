package faceapi.engine;

import android.graphics.Bitmap;

import com.rokid.citrus.citrusfacesdk.Face;


public interface IFaceDbEngine {

    /**
     * 配置数据库，数据库路径
     *
     */
    FaceDbEngine init();

    /**
     * 删除引擎数据库
     */
    void delDb();

    /**
     * 添加人脸 setData后调用
     *
     * @param UUID
     * @return
     */

    Face add(String UUID, Bitmap bm);

    /**
     * 删除人脸
     * @param uuid
     * @return
     */
    boolean remove(String uuid);

    /**
     * 移除UUID对应特征库
     *
     * @param UUID
     * @return
     */
    boolean update(String UUID, Bitmap bm);

    /**
     * 数据库是否有这个UUID
     *
     * @param UUID
     * @return
     */
    boolean contain(String UUID);

    /**
     * 数据库大小
     *
     * @return
     */
    int dbSize();

    /**
     * 获取数据库对应index UUID
     *
     * @param index
     * @return
     */
    String getUUID(int index);

    void destroy();

    void save();

    void save(int maxFace);

}
