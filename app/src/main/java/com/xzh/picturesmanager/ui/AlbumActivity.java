package com.xzh.picturesmanager.ui;

import android.app.Activity;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.xzh.picturesmanager.R;
import com.xzh.picturesmanager.album.adapter.FloderListAdapter;
import com.xzh.picturesmanager.album.adapter.FolderDetailAdapter;
import com.xzh.picturesmanager.album.model.FloderBean;
import com.xzh.picturesmanager.album.model.PictureBean;
import com.xzh.picturesmanager.album.utils.AlbumFactory;
import com.xzh.picturesmanager.album.utils.AlbumUtils;
import com.xzh.picturesmanager.base.BaseActivity;
import com.xzh.picturesmanager.base.YmatouApplication;
import com.xzh.picturesmanager.utils.ImageUtils;
import com.xzh.picturesmanager.utils.OnInteractionListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by xiangzhihong on 2016/4/13 on 10:55.
 */
public class AlbumActivity extends BaseActivity {

    @InjectView(R.id.select_album_title)
    TextView selectAlbumTitle;
    @InjectView(R.id.album_girds)
    GridView albumGirds;
    @InjectView(R.id.album_listview)
    ListView albumListView;
    @InjectView(R.id.albun_ok)
    TextView albunOk;
    @InjectView(R.id.take_photo_iv)
    ImageView takePhoto;

    private FolderDetailAdapter floderDetailAdapter = null;
    private FloderListAdapter floderListAdapter = null;
    //管理图片选中
    private int maxCount = 0;
    private ArrayList<String> mPreCheckedList = null;
    private AlbumFactory albumFactory = null;
    private String imagePath = null;

    private PictureBean currentPicture = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albums);
        ButterKnife.inject(this);
        init();
    }

    private void init() {
        initParam();
        initView();
    }

    private void initView() {
        albumFactory = new AlbumFactory(this);
        floderListAdapter = new FloderListAdapter(this);
        floderDetailAdapter = new FolderDetailAdapter(this);
        albumListView.setAdapter(floderListAdapter);
        floderDetailAdapter.addCheckedPictures(mPreCheckedList);
        floderDetailAdapter.setFloderListAdapter(floderListAdapter);
        floderDetailAdapter.setMaxCount(maxCount);
        floderListAdapter.registerDataSetObserver(dataSetObservable);
        albumGirds.setAdapter(floderDetailAdapter);

        albumGirds.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentPicture = floderDetailAdapter.getItem(position);
                BrowsePictureActivity.open(AlbumActivity.this, maxCount, floderDetailAdapter.getCheckedCount(), currentPicture);
            }
        });
        albumListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                albumListView.setVisibility(View.GONE);
                albumGirds.setVisibility(View.VISIBLE);
                selectAlbumTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.expanded_arrow, 0);
                FloderBean floderBean = floderListAdapter.getItem(position);
                selectAlbumTitle.setText(floderBean.getFloderName());
                floderDetailAdapter.setFloderBean(floderBean);
            }
        });
        initAlbumFactory();
    }

    private void initAlbumFactory() {
        albumFactory.builder(new OnInteractionListener<List<FloderBean>>() {
            public void onInteraction(List<FloderBean> floders) {
                checkPrePicture(floders);
                floderListAdapter.setList(floders);
                if (floderListAdapter.getCount() > 0) {
                    floderDetailAdapter.setFloderBean(floderListAdapter.getItem(0));
                }
            }
        });
    }

    private DataSetObserver dataSetObservable = new DataSetObserver() {
        public void onChanged() {
            albunOk.setText("确定(" + floderDetailAdapter.getCheckedCount() + "/" + maxCount + ")");
        }
    };

    private void checkPrePicture(List<FloderBean> floders) {
        if (floderDetailAdapter.getCheckedPictures() == null) return;
        for (FloderBean floder : floders) {
            for (PictureBean picture : floder.getPictureList()) {
                for (String path : floderDetailAdapter.getCheckedPictures())
                    if (path.equals(picture.path)) {
                        picture.isChecked = true;
                        ++floder.selectedCount;
                        break;
                    }
            }
        }
    }

    private void initParam() {
        maxCount = getIntent().getIntExtra(MAX_PICTURE_COUNT, 12);
        mPreCheckedList = getIntent().getStringArrayListExtra(PICTURE_LIST);
    }

    @OnClick(R.id.cancel_view)
    void cancelClick(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @OnClick(R.id.take_photo_iv)
    void photoClick(View view) {
        if (floderDetailAdapter.getCheckedCount()>=maxCount){
            Toast.makeText(this,"无法添加更多图片",Toast.LENGTH_LONG).show();
            return;
        }else{
            openCamera();
        }
    }

    @OnClick(R.id.select_album_title)
    void titleClick(View view) {
        if (albumGirds.getVisibility()==View.VISIBLE){
            selectAlbumTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.expanded_arrow_up, 0);
            albumGirds.setVisibility(View.GONE);
            albumListView.setVisibility(View.VISIBLE);
        }else {
            selectAlbumTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.expanded_arrow, 0);
            albumGirds.setVisibility(View.VISIBLE);
            albumListView.setVisibility(View.GONE);
            FloderBean floderBean = floderListAdapter.getItem(0);
            selectAlbumTitle.setText(floderBean.getFloderName());
            floderDetailAdapter.setFloderBean(floderBean);
        }
    }

    @OnClick(R.id.albun_ok)
    void okClick(View view) {
        setResult(floderDetailAdapter.getCheckedPictures());
    }

    private void setResult(List<String> list) {
        if (list != null && list.size() > 0) {
            Intent intent = getIntent();
            intent.putStringArrayListExtra(ManagerPictureActivity.PICTURE_LIST, (ArrayList<String>) list);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            Toast.makeText(this,"您未选择任何图片",Toast.LENGTH_LONG).show();
        }

    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        imagePath = ImageUtils.getImagePath();
        File out = new File(imagePath);
        Uri uri = Uri.fromFile(out);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, CAMERA_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode ==Activity.RESULT_OK&&requestCode == CAMERA_CODE) {//拍照刷新图库
            if (imagePath != null) {
                MediaScannerConnection.scanFile(YmatouApplication.getInstance(),
                        new String[]{imagePath}, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            @Override
                            public void onScanCompleted(String path, Uri uri) {

                            }
                        }
                );
                List<String> list = floderDetailAdapter.getCheckedPictures();
                list.add(imagePath);
                setResult(list);
            }
        } else if (resultCode ==Activity.RESULT_OK&&requestCode == BrowsePictureActivity.BROWSE_PICTURE_CODE) {//单图浏览界面刷新
            PictureBean pictureBean = (PictureBean) data.getSerializableExtra(BrowsePictureActivity.PICTURE_BEAN);
            if(!currentPicture.isChecked && pictureBean.isChecked){
                floderDetailAdapter.addCheckedPicture(currentPicture.path);
            }else{
//                floderDetailAdapter.removeCheckedPicture(currentPicture.path);
            }
            setResult(floderDetailAdapter.getCheckedPictures());

        }else if (resultCode==RESULT_FIRST_USER){
            PictureBean pictureBean = (PictureBean) data.getSerializableExtra(BrowsePictureActivity.PICTURE_BEAN);
            if (pictureBean.isChecked&&!currentPicture.isChecked) {
                currentPicture.isChecked = pictureBean.isChecked;
                floderDetailAdapter.getFloderBean().selectedCount++;
                floderDetailAdapter.addCheckedPicture(currentPicture.path);
            } else if (!pictureBean.isChecked&&currentPicture.isChecked){
                currentPicture.isChecked = pictureBean.isChecked;
                floderDetailAdapter.getFloderBean().selectedCount--;
                floderDetailAdapter.removeCheckedPicture(currentPicture.path);
            }
            floderDetailAdapter.notifyDataSetChanged();
            floderListAdapter.notifyDataSetChanged();
        }
    }

    //管理图片相册图片选中
    public static void open(Activity context, int maxCount) {
        open(context, null, maxCount);
    }

    public static void open(Activity context, List<String> list, int maxCount) {
        Intent intent = new Intent(context, AlbumActivity.class);
        if (list != null) {
            intent.putStringArrayListExtra(PICTURE_LIST, (ArrayList<String>) list);
        }
        intent.putExtra(MAX_PICTURE_COUNT, maxCount);
        context.startActivityForResult(intent, ManagerPictureActivity.PICTURE_MANAGER_CODE);
    }

    public static final String MAX_PICTURE_COUNT = "MAX_PICTURE_COUNT";
    public static final String PICTURE_LIST = "PICTURE_LIST";
    private static final int CAMERA_CODE = 0x008;

}
