package com.xzh.picturesmanager.album.model;


import java.io.Serializable;

/**
 * Created by xiangzhihong on 2016/4/15 on 17:13.
 */
public class PictureBean implements Serializable,Comparable<PictureBean>{
    public PictureBean(String path,long date) {
        this.path = path;
        this.date=date;
    }

    public String path = null;
    public boolean isChecked = false;
    public long date;

    @Override
    public int compareTo(PictureBean another) {
        if (another == null) {
            return 1;
        }
        return (int) ((another.date -date) / 1000);
    }
}
