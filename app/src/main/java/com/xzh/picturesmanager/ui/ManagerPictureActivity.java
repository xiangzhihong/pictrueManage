package com.xzh.picturesmanager.ui;

import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;


import com.xzh.picturesmanager.R;
import com.xzh.picturesmanager.album.adapter.PreviewPictureAdapter;
import com.xzh.picturesmanager.album.adapter.ProductThumbnailAdater;
import com.xzh.picturesmanager.base.BaseActivity;
import com.xzh.picturesmanager.utils.Constant;
import com.xzh.picturesmanager.utils.SampleOnPageChangeListener;
import com.xzh.picturesmanager.view.DragGridView;

import java.io.Serializable;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class ManagerPictureActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    @InjectView(R.id.confirm_button)
    TextView confirmButton;
    @InjectView(R.id.viewPager)
    ViewPager viewPager;
    @InjectView(R.id.indicator_view)
    TextView indicatorView;
    @InjectView(R.id.gridView)
    DragGridView gridView;

    private static int MAX_NUM= Constant.MAX_NUM;
    private PreviewPictureAdapter mPreviewAdapter = null;
    private ProductThumbnailAdater mThumbnailAdater = null;
    private List<String> mPictures = null;
    private String mProductId = null;
    private boolean isDeletePicture = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        flagBarTint(false);
        setContentView(R.layout.activity_product_picture_manager_layout);
        ButterKnife.inject(this);
        init();
    }

    private void init() {
        initParam();
        initView();
        if (mPictures == null || mPictures.isEmpty()) {
            AlbumActivity.open(this, MAX_NUM);
        }
    }

    private void initParam() {
        Intent intent = getIntent();
        mProductId = intent.getStringExtra(PRODUCT_ID);
        mPictures = intent.getStringArrayListExtra(PICTURE_LIST);
        isDeletePicture = intent.getBooleanExtra(DELETE_PICTURE, true);
    }

    private void initView() {
        mThumbnailAdater = new ProductThumbnailAdater(this);
        mThumbnailAdater.addList(mPictures);
        gridView.setAdapter(mThumbnailAdater);
        gridView.setOnItemClickListener(this);
        gridView.setOnMoveListener(new DragGridView.OnMoveListener() {
            public void startMove() {}
            public void cancleMove() {}

            public void finishMove() {
                mPreviewAdapter.notifyDataSetChanged();
                viewPager.setCurrentItem(mThumbnailAdater.getCurrentIndex());
            }
        });

        mPreviewAdapter = new PreviewPictureAdapter(this, isDeletePicture);
        mPreviewAdapter.registerDataSetObserver(dataSetObserver);
        mPreviewAdapter.setList(mThumbnailAdater.getList());
        viewPager.setAdapter(mPreviewAdapter);
        viewPager.setOffscreenPageLimit(2);
        viewPager.setOnPageChangeListener(new SampleOnPageChangeListener() {
            public void onPageSelected(int position) {
                updateIndicator();
                mThumbnailAdater.setSelectedItem(mThumbnailAdater.getItem(position));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;
        if (requestCode == PICTURE_MANAGER_CODE) {
            List<String> list = data.getStringArrayListExtra(PICTURE_LIST);
            mPreviewAdapter.clear();
            mPreviewAdapter.addList(list);
            mThumbnailAdater.notifyDataSetChanged();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mThumbnailAdater.isAddPicture(position)) {
            AlbumActivity.open(this,mPreviewAdapter.getList(), 12);
        } else {
            mThumbnailAdater.setSelectedItem(mThumbnailAdater.getItem(position));
            viewPager.setCurrentItem(position);
        }
    }

    @OnClick(R.id.confirm_button)
    public void confirm() {
      setPicResult(mPreviewAdapter.getList());
        finish();
    }

    public void deletePicture(int position) {
        mPreviewAdapter.remove(position);
        int index = viewPager.getCurrentItem();
        if(index !=  mThumbnailAdater.getCurrentIndex()){
            mThumbnailAdater.setSelectedItem(mPreviewAdapter.getItem(index));
        }
    }

    private void updateIndicator() {
        int indicator = Math.min(viewPager.getCurrentItem() + 1, mPreviewAdapter.getCount());
        indicatorView.setText(indicator + "/" + mPreviewAdapter.getCount());
    }

    private void setPicResult(List<String> list) {
        Intent intent = getIntent();
        intent.putExtra(PICTURE_LIST, (Serializable) list);
        setResult(RESULT_OK, intent);
    }

    private DataSetObserver dataSetObserver = new DataSetObserver() {
        public void onChanged() {
            updateIndicator();
            confirmButton.setEnabled(mPreviewAdapter.getCount() != 0);
        }
    };


    @OnClick(R.id.back_button)
    public void onBackPressed() {
        super.onBackPressed();
    }

    public static final String PRODUCT_ID = "PRODUCT_ID";
    public static final String PICTURE_LIST = "PICTURE_LIST";
    public static final String DELETE_PICTURE = "CAN_DELETE_PICTURE";
    public static final int PICTURE_MANAGER_CODE = 0X123;
}
