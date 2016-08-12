package com.xzh.picturesmanager.album.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;


import com.xzh.picturesmanager.R;
import com.xzh.picturesmanager.base.BasicAdapter;
import com.xzh.picturesmanager.utils.DeviceUtil;
import com.xzh.picturesmanager.utils.Utils;
import com.xzh.picturesmanager.utils.YMTImageLoader;
import com.xzh.picturesmanager.view.DragGridView;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ProductThumbnailAdater extends BasicAdapter<String> implements DragGridView.DragGridBaseAdapter {
    private Context mContext;
    private final int MAX_NUM = 12;
    private AbsListView.LayoutParams layoutParams = null;
    private int currentHidePosition = -1;
    private String selectedItem = "";

    public ProductThumbnailAdater(Context context) {
        super(context);
        this.mContext=context;
        int screenWidth = DeviceUtil.getScreenWidth(context);
        int cell = (screenWidth - Utils.dip2px(mContext,8) * 2 - Utils.dip2px(mContext,10) * 5) / 6;
        layoutParams = new AbsListView.LayoutParams(cell, cell);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflate(R.layout.item_product_thumbnail_layout);
        convertView.setLayoutParams(layoutParams);
        ViewHolder viewHolder = new ViewHolder(convertView);
        String path = isAddPicture(position) ?
                "drawable://" + R.drawable.product_picture_add :
                getItem(position);
        imageloader(path, viewHolder.imageview);
        if((TextUtils.isEmpty(selectedItem) || selectedItem.equals(path) && !isAddPicture(position))){
            convertView.setBackgroundResource(R.drawable.c9_rect_shape);
            selectedItem = path;
        }else{
            convertView.setBackgroundColor(0x00ffffff);
        }
        convertView.setVisibility(position == currentHidePosition ? View.GONE : View.VISIBLE);
        return convertView;
    }

    private void imageloader(String path, ImageView imageView) {
        String url = path.startsWith("http://") || path.startsWith("https://") || path.startsWith("drawable://") ?
                path : "file://" + path;
        YMTImageLoader.imageloader(url, imageView);
    }

    public boolean isAddPicture(int position) {
        return position == getRealCount() && getRealCount() <= MAX_NUM;
    }

    @Override
    public int getCount() {
        int realCount = getRealCount();
        if (realCount >= MAX_NUM) return realCount;
        return realCount + 1;
    }

    public int getRealCount() {
        return super.getCount();
    }

    @Override
    public void reorderItems(int oldPosition, int newPosition) {
        if (!checkBoundary(oldPosition) || !checkBoundary(newPosition)) return;
        String temp = getItem(oldPosition);
        mList.remove(temp);
        mList.add(newPosition,temp);
    }

    public boolean checkBoundary(int position) {
        return position < getRealCount() && position >= 0;
    }

    @Override
    public void setHideItem(int hidePosition) {
        this.currentHidePosition = hidePosition;
        if(checkBoundary(hidePosition)) selectedItem = getItem(hidePosition);
        notifyDataSetChanged();
    }

    public void setSelectedItem(String item) {
        this.selectedItem = item;
        notifyDataSetChanged();
    }

    public int getCurrentIndex(){
        return mList.indexOf(selectedItem);
    }

    static class ViewHolder {
        @InjectView(R.id.imageView)
        ImageView imageview;

        ViewHolder(View view) {
            ButterKnife.inject(this, view);
            view.setTag(this);
        }

        public void reset() {
            imageview.setImageBitmap(null);
            imageview.setImageDrawable(null);
        }
    }
}
