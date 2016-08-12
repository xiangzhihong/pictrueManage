package com.xzh.picturesmanager.album.adapter;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;


import com.xzh.picturesmanager.utils.YMTImageLoader;
import com.xzh.picturesmanager.view.photoview.PhotoView;
import com.xzh.picturesmanager.view.photoview.PhotoViewAttacher;

import java.util.List;

public class PicturesPagerAdapter extends PagerAdapter implements PhotoViewAttacher.OnPhotoTapListener {

    private List<String> mList = null;

    public PicturesPagerAdapter(List<String> mList) {
        this.mList = mList;
        /*FIXME 发现线上报 java.lang.IllegalStateException: ImageLoader must be init with configuration before using 做兼容处理   后续观察*/
        YMTImageLoader.init();
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public View instantiateItem(ViewGroup container, int position) {
        PhotoView photoView = new PhotoView(container.getContext());
        container.addView(photoView, ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.MATCH_PARENT);
        imageloader(photoView,mList.get(position));
        photoView.setOnPhotoTapListener(this);
        return photoView;
    }

    private void imageloader(PhotoView photoView, String url) {
        boolean bool = url.startsWith("http://") || url.startsWith("https://") || url.startsWith("file://")
                || url.startsWith("content://") || url.startsWith("assets://") || url.startsWith("drawable://");
        if (!bool) url = "file://" + url;
        YMTImageLoader.imageloader(url, photoView);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void onPhotoTap(View view, float x, float y) {
        ((Activity) view.getContext()).finish();
    }

    @Override
    public int getItemPosition(Object object) {
        return super.getItemPosition(object);
    }
}