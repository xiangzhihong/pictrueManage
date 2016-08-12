package com.xzh.picturesmanager.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import com.xzh.picturesmanager.R;
import com.xzh.picturesmanager.album.utils.AlbumUtils;
import com.xzh.picturesmanager.base.BaseActivity;
import com.xzh.picturesmanager.utils.ImageUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by xiangzhihong on 2016/4/14 on 19:35.
 * 预览拍照结果
 */
public class BrowseCameraActivity extends BaseActivity {
    @InjectView(R.id.browse_image)
    ImageView browseImage;
    @InjectView(R.id.retake_camera)
    TextView retakeCamera;
    @InjectView(R.id.use_photo)
    TextView usePhoto;

    private String bitmapPath=null;
    private Bitmap preview=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        flagBarTint(false);
        setContentView(R.layout.activity_album_browse_photo);
        ButterKnife.inject(this);
        init();
    }

    private void init() {
      initParam();
      intiView();
    }

    private void initParam() {
        bitmapPath = getIntent().getStringExtra(CAMERA_PATH);
    }

    private void intiView() {
         preview= ImageUtils.getBitmap(bitmapPath,600,800);
        if (preview!=null)
            browseImage.setImageBitmap(preview);
    }
    @OnClick(R.id.retake_camera)
    public void take(View view) {
        CameraActivity.open(this);
    }

    @OnClick(R.id.use_photo)
    public void save(View view) {
       String imagePath= ImageUtils.saveBitmap(preview);

    }

    public static void open(Context context, String path) {
       if (path!=null){
           Intent intent = new Intent(context, BrowseCameraActivity.class);
           intent.putExtra(CAMERA_PATH,path);
           context.startActivity(intent);
       }
    }

    private static final String CAMERA_PATH = "CAMERA_PATH";
}
