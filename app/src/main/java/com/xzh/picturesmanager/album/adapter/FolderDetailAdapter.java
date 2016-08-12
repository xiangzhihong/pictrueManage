package com.xzh.picturesmanager.album.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.xzh.picturesmanager.R;
import com.xzh.picturesmanager.album.model.FloderBean;
import com.xzh.picturesmanager.album.model.PictureBean;
import com.xzh.picturesmanager.base.BasicAdapter;
import com.xzh.picturesmanager.utils.DeviceUtil;
import com.xzh.picturesmanager.utils.ImageUtils;
import com.xzh.picturesmanager.utils.Utils;
import com.xzh.picturesmanager.utils.YMTImageLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by xiangzhihong on 2016/4/13 on 11:34.
 */
public class FolderDetailAdapter extends BasicAdapter<PictureBean> {
    private FloderBean floderBean = null;
    private FloderListAdapter mFloderListAdapter = null;
    private int maxCount = Integer.MAX_VALUE;
    private RelativeLayout.LayoutParams mLayoutParams = null;
    private List<String> mCheckedPictures = new ArrayList<>();

    public FolderDetailAdapter(Context context) {
        super(context);
        int cell = (DeviceUtil.getScreenWidth(context) - Utils.dip2px(context,16 + 3 * 8)) / 4;
        mLayoutParams = new RelativeLayout.LayoutParams(cell, cell);
    }

    public void setFloderBean(FloderBean floderBean) {
        this.floderBean = floderBean;
        List<PictureBean> list = floderBean.getPictureList();
        Collections.sort(list);
        setList(list);
    }

    public FloderBean getFloderBean() {
        return floderBean;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    public void setFloderListAdapter(FloderListAdapter floderListAdapter) {
        this.mFloderListAdapter = floderListAdapter;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = inflate(R.layout.item_picture_layout);
            viewHolder = new ViewHolder(convertView);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        initItemView(viewHolder, getItem(position));
        return convertView;
    }

    private void initItemView(final ViewHolder viewHolder, final PictureBean data) {
        if (data == null || viewHolder == null) return;
        viewHolder.albumItemImage.setLayoutParams(mLayoutParams);
        imageloader(viewHolder.albumItemImage, data.path);
        viewHolder.selectTag.setChecked(data.isChecked);

        viewHolder.selectTag.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CheckBox checkBox = (CheckBox) v;
                if (checkBox.isChecked() && mCheckedPictures.size() >= maxCount) {
                    checkBox.setChecked(false);
                    Toast.makeText(mContext,"无法添加更多图片",Toast.LENGTH_LONG).show();
                    return;
                }
                data.isChecked = checkBox.isChecked();
                if (data.isChecked) {
                    addCheckedPicture(data.path);
                    floderBean.selectedCount++;
                } else {
                    removeCheckedPicture(data.path);
                    floderBean.selectedCount--;
                }
                if (mFloderListAdapter != null) {
                    mFloderListAdapter.notifyDataSetChanged();
                }
            }
        });
    }


    private void imageloader(ImageView imageView, String path) {
        if (!path.startsWith("http://") && !path.startsWith("https://") && !path.startsWith("file://")) {
            path = "file://" + path;
        }
        YMTImageLoader.imageloader(path, imageView);
    }

    public void addCheckedPicture(String path) {
        mCheckedPictures.add(path);
        notifyDataSetChanged();
    }

    public void removeCheckedPicture(String path) {
        mCheckedPictures.remove(path);
    }

    public void addCheckedPictures(List<String> list) {
        if (list == null) return;
        mCheckedPictures.addAll(list);
    }

    public int getCheckedCount() {
        return mCheckedPictures.size();
    }

    public List<String> getCheckedPictures() {
        return mCheckedPictures;
    }

    class ViewHolder {
        @butterknife.InjectView(R.id.album_item_image)
        ImageView albumItemImage;
        @butterknife.InjectView(R.id.select_tag)
        CheckBox selectTag;

        ViewHolder(View view) {
            ButterKnife.inject(this, view);
            view.setTag(this);
        }
    }
}
