package faceapi.userdb;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.text.TextUtils;

@Entity(tableName = "user_info", indices = {@Index(value = {"uuid"}, unique = true)})
public class UserInfo {

//    @PrimaryKey(autoGenerate = true)
//    public int _id;

    /**
     * 特征库UUID
     */
    @PrimaryKey
    @NonNull
    public String uuid;

    /**
     * user info
     */
    public String name="";
    public String cardno="";
    public String nativeplace;
    public String checkcode;

    public UserInfo() {

    }

    public UserInfo(String n, String card) {
        name = n;
        cardno = card;
    }

    public UserInfo setUUID(String uuid) {
        this.uuid = uuid;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("name " + name);
        builder.append(" carno " + cardno);
        builder.append(" uuid " + (!TextUtils.isEmpty(uuid) ? new String(uuid): "null"));
        return builder.toString();
    }
}
