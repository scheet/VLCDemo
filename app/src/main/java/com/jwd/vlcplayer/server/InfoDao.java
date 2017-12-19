package com.jwd.vlcplayer.server;

/**
 * Created by scheet on 2017/12/8.
 */

public class InfoDao {
    public String id;
    public String user;
    public String data;
    public String type;

    public InfoDao(String id,String user,String data,String type){
        this.id = id;
        this.user= user;
        this.data = data;
        this.type = type;

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
