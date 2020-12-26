package faceapi.userdb;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface UserInfoDao {

    @Query("SELECT * FROM user_info")
    List<UserInfo> getList();

    @Query("SELECT * FROM user_info WHERE uuid LIKE :id LIMIT 1")
    UserInfo getUserInfo(String id);

    @Query("SELECT * FROM user_info WHERE name LIKE :n")
    List<UserInfo> getUserInfoByName(String n);

    @Query("SELECT * FROM user_info LIMIT :begin,:count")
    List<UserInfo> getUserInfoByIndex(String begin, String count);

    @Query("SELECT * FROM user_info WHERE cardno LIKE :card")
    UserInfo getUserInfoByCarno(String card);

    @Insert()
    void addUserInfo(UserInfo userInfo);

    @Insert()
    void addUserInfoAll(List<UserInfo> userInfoList);

    @Update
    void updateUserInfo(UserInfo userInfo);

//    @Query("UPDATE user_info SET uuid = : WHERE uuid = :userInfo.uuid")
//    void updateUserInfo(UserInfo userInfo);

    @Delete
    void removeUserInfo(UserInfo userInfo);
}
