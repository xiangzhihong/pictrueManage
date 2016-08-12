package com.xzh.picturesmanager.manager;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;


import com.xzh.picturesmanager.ui.ManagerPictureActivity;

import java.util.ArrayList;
import java.util.List;

public class ProductPictureManager {

    public static void open(Activity context) {
        open(context, null);
    }

    public static void open(Activity context, List<String> pictures) {
        open(context, null, pictures);
    }

    public static void open(Activity context, String productId, List<String> pictures) {
        Intent intent = new Intent(context, ManagerPictureActivity.class);
        if (pictures != null) {
            intent.putStringArrayListExtra(ManagerPictureActivity.PICTURE_LIST,(ArrayList<String>)pictures);
        }
        context.startActivityForResult(intent, ManagerPictureActivity.PICTURE_MANAGER_CODE);
    }
}
