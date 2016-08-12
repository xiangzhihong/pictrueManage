package com.xzh.picturesmanager.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.xzh.picturesmanager.R;
import com.xzh.picturesmanager.base.BasicAdapter;
import com.xzh.picturesmanager.utils.Utils;
import com.xzh.picturesmanager.utils.YMTImageLoader;

/**
 * Created by xiangzhihong on 2016/4/13 on 11:14.
 */
public class PicturesAdapter extends BasicAdapter<String> {

    private final Activity context;
    private View itemView = null;
    private View deleteView = null;
    private ImageView picture = null;
    private RelativeLayout.LayoutParams pictureLayoutParams = null;

    public PicturesAdapter(final Activity context) {
        int cell = (Utils.getScreenWidth(context) - Utils.dip2px(context,30)) / 3;
        pictureLayoutParams = new RelativeLayout.LayoutParams(cell, cell);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = initItem(position);
        return convertView;
    }

    private View initItem(int position) {
        if (position == getCount() - 1 && getRealCount() < 6) {
            itemView = View.inflate(context, R.layout.item_layout_add_picture, null);
            View contentView = itemView.findViewById(R.id.content_view);
            contentView.setLayoutParams(pictureLayoutParams);
        } else {
            itemView = View.inflate(context, R.layout.item_choose_picture_layout, null);
            picture = (ImageView) itemView.findViewById(R.id.item_picture);
            YMTImageLoader.loaderRoundImage(getItem(position), picture, 3);
            picture.setLayoutParams(pictureLayoutParams);
        }
        return itemView;
    }

    public int getRealCount() {
        return super.getCount();
    }

    @Override
    public int getCount() {
        if (getRealCount() < 6) {
            return getRealCount() + 1;
        }
        return getRealCount();
    }
}
