package com.xzh.picturesmanager.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;


import com.xzh.picturesmanager.R;
import com.xzh.picturesmanager.adapter.PicturesAdapter;
import com.xzh.picturesmanager.base.BaseActivity;
import com.xzh.picturesmanager.manager.ProductPictureManager;
import com.xzh.picturesmanager.view.FixedGridView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemClick;

/**
 * Created by xiangzhihong on 2016/4/13 on 11:14.
 */
public class AlbumTestActivity extends BaseActivity {

    @InjectView(R.id.pictures)
    FixedGridView pictures;
    @InjectView(R.id.pic_count)
    TextView picCount;

    private List<String> imageList;
    private PicturesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albums_test);
        ButterKnife.inject(this);
        init();
    }

    private void init() {
        adapter=new PicturesAdapter(this);
        pictures.setAdapter(adapter);
    }

    @OnItemClick(R.id.pictures)
    public void addClick(View view) {
        ProductPictureManager.open(this);
    }

    @OnClick(R.id.back)
    public void backClick(View view) {
       finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (resultCode != Activity.RESULT_OK) return;
         if(requestCode == ManagerPictureActivity.PICTURE_MANAGER_CODE){
             ArrayList<String> list = data.getStringArrayListExtra(ManagerPictureActivity.PICTURE_LIST);
             if (null != list && !list.isEmpty()) {
                 imageList = list;
                 setImageData();
             }
         }
    }

    private void setImageData() {
        if (imageList!=null&&imageList.size()>0&&adapter!=null)
        adapter.setList(imageList);

        picCount.setText(imageList.size()+"/12");
    }
}
