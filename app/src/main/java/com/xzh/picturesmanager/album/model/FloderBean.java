package com.xzh.picturesmanager.album.model;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiangzhihong on 2016/4/15 on 17:12.
 */
public class FloderBean {
    private String floderName = null;
    public String floderTag = null;
    private List<PictureBean> pictureList = null;
    public int selectedCount = 0;

    public FloderBean(String floderName, String floderTag) {
        this.floderName = floderName;
        this.floderTag = floderTag;
    }

    public String getFloderName() {
        return floderName + "";
    }

    public List<PictureBean> getPictureList() {
        if (pictureList == null) {
            pictureList = new ArrayList<>();
        }
        return pictureList;
    }

    public String getFloderCover() {
        String path = "";
        if (!getPictureList().isEmpty()) {
            path = getPictureList().get(0).path + "";
        }
        return path;
    }

    public void addPicture(PictureBean pictureBean) {
        getPictureList().add(pictureBean);
    }

    @Override
    public int hashCode() {
        if (floderTag == null) {
            return super.hashCode();
        } else {
            return floderTag.hashCode();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && (o instanceof FloderBean)) {
            return TextUtils.equals(floderTag, ((FloderBean) o).floderTag);
        }
        return false;
    }

}
