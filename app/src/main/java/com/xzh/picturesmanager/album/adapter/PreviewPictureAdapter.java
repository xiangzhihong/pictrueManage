package com.xzh.picturesmanager.album.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import com.xzh.picturesmanager.R;
import com.xzh.picturesmanager.ui.ManagerPictureActivity;
import com.xzh.picturesmanager.utils.Convert;
import com.xzh.picturesmanager.utils.UIUtils;
import com.xzh.picturesmanager.utils.YMTImageLoader;

import java.util.ArrayList;
import java.util.List;

public class PreviewPictureAdapter extends PagerAdapter {

    private List<String> mList = new ArrayList<>();
    private Context mContext = null;
    protected LayoutInflater inflaterFactory = null;
    private boolean isDeletePicture = true;

    public PreviewPictureAdapter(Context context, boolean isDeletePicture) {
        this.mContext = context;
        this.isDeletePicture = isDeletePicture;
        inflaterFactory = LayoutInflater.from(mContext);
    }

    public void setList(List<String> list) {
        if (list != null) {
            mList = list;
            notifyDataSetChanged();
        }
    }

    public boolean addList(List<String> list) {
        if (list != null && list.size() > 0) {
            mList.addAll(list);
            notifyDataSetChanged();
            return true;
        }
        return false;
    }

    public boolean add(String t) {
        if (t != null) {
            mList.add(t);
            notifyDataSetChanged();
            return true;
        }
        return false;
    }

    public void remove(int index) {
        if (index >= 0 && index < mList.size()) {
            mList.remove(index);
            notifyDataSetChanged();
        }
    }

    public void clear(){
        mList.clear();
    }

    public String getItem(int index) {
        if (index >= 0 && index < mList.size()) {
           return mList.get(index);
        }
        return "";
    }

    public List<String> getList() {
        return mList;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public View instantiateItem(ViewGroup container, int position) {
        View convertView = inflaterFactory.inflate(R.layout.item_product_preview_picture_layout, null);
        ImageView photoView = UIUtils.getView(convertView, R.id.photo_view);
        imageloader(photoView, getItem(position));
        View deleteButton = UIUtils.getView(convertView, R.id.delete_button);
        deleteButton.setVisibility(isDeletePicture ? View.VISIBLE : View.GONE);
        deleteButton.setTag(position);
        deleteButton.setOnClickListener(onClickDeleteListener);
        container.addView(convertView);
        return convertView;
    }

    private View.OnClickListener onClickDeleteListener = new View.OnClickListener() {
        public void onClick(View v) {
            ((ManagerPictureActivity)mContext).deletePicture(Convert.toInteger(v.getTag()));
        }
    };

    private void imageloader(ImageView photoView, String url) {
        boolean bool = url.startsWith("http://") || url.startsWith("https://") || url.startsWith("file://") || url.startsWith("drawable://");
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
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}