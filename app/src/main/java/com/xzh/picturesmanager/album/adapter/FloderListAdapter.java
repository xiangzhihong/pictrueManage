package com.xzh.picturesmanager.album.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.xzh.picturesmanager.R;
import com.xzh.picturesmanager.album.model.FloderBean;
import com.xzh.picturesmanager.base.BasicAdapter;
import com.xzh.picturesmanager.utils.YMTImageLoader;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by xiangzhihong on 2016/4/13 on 16:17.
 */
public class FloderListAdapter extends BasicAdapter<FloderBean> {


    public FloderListAdapter(Context context) {
        super(context);
    }

    public void addFloderList(FloderBean floderBean) {
        getList().add(floderBean);
        notifyDataSetChanged();
    }

    public void removeFloderList(FloderBean  floderBean) {
        getList().remove(floderBean);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = inflate(R.layout.item_floder_layout);
            viewHolder = new ViewHolder(convertView);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        bindData(viewHolder, getItem(position));
        return convertView;
    }

    private void bindData(ViewHolder viewHolder, FloderBean data) {
        if (data == null || viewHolder == null) return;
        viewHolder.itemAlbumName.setText(data.getFloderName());
        viewHolder.itemPicNum.setText("(" + data.getPictureList().size() + ")");
        imageloader(viewHolder.itemConver, data.getFloderCover());
        viewHolder.itemChooseNum.setVisibility(data.selectedCount > 0 ? View.VISIBLE : View.GONE);
        viewHolder.itemChooseNum.setText(data.selectedCount + "");
    }

    private void imageloader(ImageView imageView, String path) {
        if (!path.startsWith("http://") && !path.startsWith("https://") && !path.startsWith("file://")) {
            path = "file://" + path;
        }
        YMTImageLoader.imageloader(path, imageView);
    }


    static class ViewHolder {
        @InjectView(R.id.album_list_layout)
        RelativeLayout albumListLayout;
        @InjectView(R.id.item_conver)
        ImageView itemConver;
        @InjectView(R.id.item_album_name)
        TextView itemAlbumName;
        @InjectView(R.id.item_pic_num)
        TextView itemPicNum;
        @InjectView(R.id.item_choose_num)
        TextView itemChooseNum;

        ViewHolder(View view) {
            ButterKnife.inject(this, view);
            view.setTag(this);
        }
    }
}
