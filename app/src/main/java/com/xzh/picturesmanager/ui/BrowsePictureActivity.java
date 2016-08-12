package com.xzh.picturesmanager.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xzh.picturesmanager.R;
import com.xzh.picturesmanager.album.model.PictureBean;
import com.xzh.picturesmanager.base.BaseActivity;
import com.xzh.picturesmanager.utils.UIUtil;
import com.xzh.picturesmanager.utils.YMTImageLoader;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by xiangzhihong on 2016/4/14 on 10:05.
 * 浏览大图，暂时只对发商品预览做支持，其他方式的大图预览请用BrowsePictruesActivty
 */
public class BrowsePictureActivity extends BaseActivity {

    @InjectView(R.id.album_browse_view)
    LinearLayout albumBrowseView;
    @InjectView(R.id.browse_checkbox)
    CheckBox browseCheckbox;
    @InjectView(R.id.browse_image)
    ImageView browseImage;
    @InjectView(R.id.browse_count)
    TextView browseCount;

    private PictureBean pictureBean = null;
    private int maxCount = 0, selectedCount = 0;
    private boolean isChecked=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        flagBarTint(false);
        setContentView(R.layout.activity_album_browse);
        ButterKnife.inject(this);
        UIUtil.initTintMgr(this, albumBrowseView).setStatusBarTintColor(Color.parseColor("#000000"));
        init();
    }

    @OnClick(R.id.browse_back)
    void back(View v) {
        Intent intent = getIntent();
        intent.putExtra(PICTURE_BEAN, pictureBean);
        pictureBean.isChecked = browseCheckbox.isChecked();
        intent.putExtra(SELECTED_FLAG,isChecked);
        setResult(RESULT_FIRST_USER, intent);
        finish();
    }

    @OnClick(R.id.browse_checkbox)
    void check(View v) {
        if (browseCheckbox.isChecked() && selectedCount > maxCount) {
            browseCheckbox.setChecked(false);
            Toast.makeText(this,"无法添加更多图片",Toast.LENGTH_LONG).show();
            return;
        }

    }

    @OnClick(R.id.browse_count)
    void submit(View view) {
        if (selectedCount > 0) {
            Intent intent = getIntent();
            intent.putExtra(PICTURE_BEAN, pictureBean);
            pictureBean.isChecked = browseCheckbox.isChecked();
            setResult(RESULT_OK, intent);
            finish();
        } else {
            Toast.makeText(this,"您未选择任何图片",Toast.LENGTH_LONG).show();
        }

    }

    private void init() {
        initParam();
        initView();
    }

    private void initParam() {
        Intent intent = getIntent();
        pictureBean = (PictureBean) intent.getSerializableExtra(PICTURE_BEAN);
        maxCount = intent.getIntExtra(MAX_COUNTS, 0);
        selectedCount = intent.getIntExtra(SELECTED_COUNT, 0);
        isChecked=pictureBean.isChecked;
    }

    private void initView() {
        browseCheckbox.setChecked(pictureBean.isChecked);
        YMTImageLoader.imageloader("file://" + pictureBean.path, browseImage);
        browseCount.setText("确定(" + selectedCount + "/" + maxCount + ")");
        browseCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    pictureBean.isChecked = isChecked;
                    selectedCount++;
                } else {
                    pictureBean.isChecked = isChecked;
                    selectedCount--;
                }
                browseCount.setText("确定(" + selectedCount + "/" + maxCount + ")");
            }
        });
    }

    public static void open(Activity context, int count, int selectedCount, PictureBean pictureBean) {
        Intent intent = new Intent(context, BrowsePictureActivity.class);
        intent.putExtra(MAX_COUNTS, count);
        intent.putExtra(SELECTED_COUNT, selectedCount);
        intent.putExtra(PICTURE_BEAN, pictureBean);
        context.startActivityForResult(intent, BROWSE_PICTURE_CODE);
    }

    private static final String MAX_COUNTS = "MAX_COUNTS";
    public static final String PICTURE_BEAN = "PICTURE_BEAN";
    private static final String SELECTED_COUNT = "SELECT_COUNT";
    public static final String SELECTED_FLAG = "SELECTED_FLAG";
    public static final int BROWSE_PICTURE_CODE = 0x132;
}
