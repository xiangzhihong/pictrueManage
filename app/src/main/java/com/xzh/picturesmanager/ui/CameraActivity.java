package com.xzh.picturesmanager.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xzh.picturesmanager.R;
import com.xzh.picturesmanager.album.camera.CameraHelper;
import com.xzh.picturesmanager.album.utils.AlbumUtils;
import com.xzh.picturesmanager.album.view.ProgressWheel;
import com.xzh.picturesmanager.base.BaseActivity;
import com.xzh.picturesmanager.utils.ImageUtils;
import com.xzh.picturesmanager.utils.Utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


/**
 * Created by xiangzhihong on 2016/4/13 on 18:03.
 * 对原来的相机修改，拍照后自动保存
 */
public class CameraActivity extends BaseActivity {

    @InjectView(R.id.album_surfaceview)
    SurfaceView albumSurfaceview;
    @InjectView(R.id.back)
    ImageView back;
    @InjectView(R.id.flash_iv)
    ImageView flashIv;
    @InjectView(R.id.flash_tv)
    TextView flashTv;
    @InjectView(R.id.camera_top)
    LinearLayout cameraTop;
    @InjectView(R.id.take_photo)
    ImageView takePhoto;
    @InjectView(R.id.pb_web_loading)
    ProgressWheel pbWebLoading;


    private Context mContext;
    private CameraHelper mCameraHelper;
    private Camera.Parameters parameters = null;
    private Camera cameraInst = null;
    private float pointX, pointY;
    static final int FOCUS = 1;            // 聚焦
    static final int ZOOM = 2;            // 缩放
    private int mode;                      //0是聚焦 1是放大
    private float dist;
    private int PHOTO_SIZE = 800;
    private int mCurrentCameraId = 0;  //1是前置 0是后置
    private static List<String> deviceModel = new ArrayList<>();

    static {
        deviceModel.add("SCH-N719");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        flagBarTint(false);
        setContentView(R.layout.activity_take_photo);
        ButterKnife.inject(this);
        mContext = CameraActivity.this;
        initView();
        initEvent();
    }

    private void initView() {
        mCameraHelper = new CameraHelper(this);
        SurfaceHolder surfaceHolder = albumSurfaceview.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.setKeepScreenOn(true);
        albumSurfaceview.setFocusable(true);
        albumSurfaceview.setBackgroundColor(TRIM_MEMORY_BACKGROUND);
        albumSurfaceview.getHolder().addCallback(new SurfaceCallback());//为SurfaceView的句柄添加一个回调函数
    }

    @OnClick(R.id.back)
    void backClick(View view){
      finish();
    }

    //拍照
    @OnClick(R.id.take_photo)
    void cameraClick(View view){
        try {
            takePhoto.setEnabled(false);
            cameraInst.takePicture(null, null, new MyPictureCallback());
        } catch (Throwable e) {
            e.printStackTrace();
            takePhoto.setEnabled(true);
            Utils.shortToast( "拍照失败，请重试！");
            try {
                cameraInst.startPreview();
            } catch (Throwable t) {
            }
        }
    }

    @OnClick(R.id.album_surfaceview)
    void surfaceview(View view){
        try {
            pointFocus((int) pointX, (int) pointY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

   //闪光灯
   @OnClick(R.id.flash_view)
   void lightClick(View view){
       try {
           turnLight(cameraInst);
       } catch (Exception e) {
           e.printStackTrace();
           Utils.shortToast( "切换闪光灯模式失败");
       }
   }

    //前后摄像头
    @OnClick(R.id.change_iv)
    void preClick(View view){
        boolean canSwitch = false;
        try {
            canSwitch = mCameraHelper.hasFrontCamera() && mCameraHelper.hasBackCamera();
        } catch (Exception e) {
           e.printStackTrace();
        }
        if (canSwitch){
            switchCamera();
        }else {
            Utils.shortToast("没法切换摄像头哦");
        }
    }

    private void initEvent() {
        albumSurfaceview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        pointX = event.getX();
                        pointY = event.getY();
                        mode = FOCUS;
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        dist = spacing(event);
                        if (spacing(event) > 10f) {
                            mode = ZOOM;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                        mode = FOCUS;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (mode == FOCUS) {
                            //pointFocus((int) event.getRawX(), (int) event.getRawY());
                        } else if (mode == ZOOM) {
                            float newDist = spacing(event);
                            if (newDist > 10f) {
                                float tScale = (newDist - dist) / dist;
                                if (tScale < 0) {
                                    tScale = tScale * 10;
                                }
                                addZoomIn((int) tScale);
                            }
                        }
                        break;
                }
                return false;
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    protected void onDestroy() {
        if (albumSurfaceview != null) {
            albumSurfaceview.getHolder().getSurface().release();
        }
        Runtime.getRuntime().gc();
        finish();
        super.onDestroy();
    }

    private float spacing(MotionEvent event) {
        if (event == null) {
            return 0;
        }
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    //放大缩小
    int curZoomValue = 0;

    private void addZoomIn(int delta) {

        try {
            Camera.Parameters params = cameraInst.getParameters();
            Log.d("Camera", "Is support Zoom " + params.isZoomSupported());
            if (!params.isZoomSupported()) {
                return;
            }
            curZoomValue += delta;
            if (curZoomValue < 0) {
                curZoomValue = 0;
            } else if (curZoomValue > params.getMaxZoom()) {
                curZoomValue = params.getMaxZoom();
            }

            if (!params.isSmoothZoomSupported()) {
                params.setZoom(curZoomValue);
                cameraInst.setParameters(params);
                return;
            } else {
                cameraInst.startSmoothZoom(curZoomValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //定点对焦的代码
    private void pointFocus(int x, int y) {
        cameraInst.cancelAutoFocus();
        parameters = cameraInst.getParameters();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            showPoint(x, y);
        }
        cameraInst.setParameters(parameters);
        autoFocus();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void showPoint(int x, int y) {
        if (parameters.getMaxNumMeteringAreas() > 0) {
            List<Camera.Area> areas = new ArrayList<Camera.Area>();
            //xy变换了
            int rectY = -x * 2000 /Utils.getScreenWidth(this) + 1000;
            int rectX = y * 2000 / Utils.getScreenHeight(this) - 1000;

            int left = rectX < -900 ? -1000 : rectX - 100;
            int top = rectY < -900 ? -1000 : rectY - 100;
            int right = rectX > 900 ? 1000 : rectX + 100;
            int bottom = rectY > 900 ? 1000 : rectY + 100;
            Rect area1 = new Rect(left, top, right, bottom);
            areas.add(new Camera.Area(area1, 800));
            parameters.setMeteringAreas(areas);
        }

        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
    }

    private final class MyPictureCallback implements Camera.PictureCallback {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            new SavePicTask(data).execute();
            camera.startPreview();
        }
    }

    private class SavePicTask extends AsyncTask<Void, Void, String> {
        private byte[] data;

        protected void onPreExecute() {
            pbWebLoading.setVisibility(View.VISIBLE);
        }


        SavePicTask(byte[] data) {
            this.data = data;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                return saveToSDCard(data);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (!TextUtils.isEmpty(result)) {
                cancelLoadingDialog();
                //这里需要做修改，这里跳转到预览页面
               AlbumUtils.openBrowseCamera(CameraActivity.this,result);
            } else {
                takePhoto.setEnabled(true);
                Utils.shortToast( "拍照失败，请稍后重试！");
            }
        }
    }


    private final class SurfaceCallback implements SurfaceHolder.Callback {

        public void surfaceDestroyed(SurfaceHolder holder) {
            try {
                if (cameraInst != null) {
                    cameraInst.stopPreview();
                    cameraInst.release();
                    cameraInst = null;
                }
            } catch (Exception e) {
               e.printStackTrace();
            }
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (null == cameraInst) {
                try {
                    cameraInst = Camera.open();
                    cameraInst.setPreviewDisplay(holder);
                    initCamera();
                    cameraInst.startPreview();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            autoFocus();
        }
    }

    //实现自动对焦
    private void autoFocus() {
        new Thread() {
            @Override
            public void run() {
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (cameraInst == null) {
                    return;
                }
                cameraInst.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        if (success) {
                            initCamera();
                        }
                    }
                });
            }
        };
    }

    private Camera.Size adapterSize = null;
    private Camera.Size previewSize = null;


    private void initCamera() {
        parameters = cameraInst.getParameters();
        parameters.setPictureFormat(PixelFormat.JPEG);
        //if (adapterSize == null) {
        setUpPicSize(parameters);
        setUpPreviewSize(parameters);
        //}
        if (adapterSize != null) {
            parameters.setPictureSize(adapterSize.width, adapterSize.height);
        }
        if (previewSize != null) {
            parameters.setPreviewSize(previewSize.width, previewSize.height);
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);//1连续对焦
        } else {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }
        setDisplay(parameters, cameraInst);
        try {
            cameraInst.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
        cameraInst.startPreview();
        cameraInst.cancelAutoFocus();// 2如果要实现连续的自动对焦，这一句必须加上
    }

    private void setUpPicSize(Camera.Parameters parameters) {

        if (adapterSize != null) {
            return;
        } else {
            adapterSize = findBestPictureResolution();
            return;
        }
    }

    private void setUpPreviewSize(Camera.Parameters parameters) {

        if (previewSize != null) {
            return;
        } else {
            previewSize = findBestPreviewResolution();
        }
    }

    private static final int MIN_PREVIEW_PIXELS = 480 * 320;

    private Camera.Size findBestPreviewResolution() {
        Camera.Parameters cameraParameters = cameraInst.getParameters();
        Camera.Size defaultPreviewResolution = cameraParameters.getPreviewSize();

        List<Camera.Size> rawSupportedSizes = cameraParameters.getSupportedPreviewSizes();
        if (rawSupportedSizes == null) {
            return defaultPreviewResolution;
        }

        // 按照分辨率从大到小排序
        List<Camera.Size> supportedPreviewResolutions = new ArrayList<Camera.Size>(rawSupportedSizes);
        Collections.sort(supportedPreviewResolutions, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size a, Camera.Size b) {
                int aPixels = a.height * a.width;
                int bPixels = b.height * b.width;
                if (bPixels < aPixels) {
                    return -1;
                }
                if (bPixels > aPixels) {
                    return 1;
                }
                return 0;
            }
        });

        StringBuilder previewResolutionSb = new StringBuilder();
        for (Camera.Size supportedPreviewResolution : supportedPreviewResolutions) {
            previewResolutionSb.append(supportedPreviewResolution.width).append('x').append(supportedPreviewResolution.height)
                    .append(' ');
        }
        Log.v(TAG, "Supported preview resolutions: " + previewResolutionSb);

        double screenAspectRatio = (double) Utils.getScreenWidth(this)
                / (double) Utils.getScreenHeight(this);
        Iterator<Camera.Size> it = supportedPreviewResolutions.iterator();
        while (it.hasNext()) {
            Camera.Size supportedPreviewResolution = it.next();
            int width = supportedPreviewResolution.width;
            int height = supportedPreviewResolution.height;

            if (width * height < MIN_PREVIEW_PIXELS) {
                it.remove();
                continue;
            }
            boolean isCandidatePortrait = width > height;
            int maybeFlippedWidth = isCandidatePortrait ? height : width;
            int maybeFlippedHeight = isCandidatePortrait ? width : height;
            double aspectRatio = (double) maybeFlippedWidth / (double) maybeFlippedHeight;
            double distortion = Math.abs(aspectRatio - screenAspectRatio);
            if (distortion > MAX_ASPECT_DISTORTION) {
                it.remove();
                continue;
            }

            // 找到与屏幕分辨率完全匹配的预览界面分辨率直接返回
            if (maybeFlippedWidth == Utils.getScreenWidth(this)
                    && maybeFlippedHeight == Utils.getScreenHeight(this)) {
                return supportedPreviewResolution;
            }
        }

        // 如果没有找到合适的，并且还有候选的像素，则设置其中最大比例的，对于配置比较低的机器不太合适
        if (!supportedPreviewResolutions.isEmpty()) {
            Camera.Size largestPreview = supportedPreviewResolutions.get(0);
            return largestPreview;
        }
        return defaultPreviewResolution;
    }

    /**
     * 最大宽高比差
     */
    private static final double MAX_ASPECT_DISTORTION = 0.15;
    private static final String TAG = "Camera";

    /**
     * 找出最适合的预览界面分辨率
     * @return
     */
    private Camera.Size findBestPictureResolution() {
        Camera.Parameters cameraParameters = cameraInst.getParameters();
        List<Camera.Size> supportedPicResolutions = cameraParameters.getSupportedPictureSizes(); // 至少会返回一个值

        StringBuilder picResolutionSb = new StringBuilder();
        for (Camera.Size supportedPicResolution : supportedPicResolutions) {
            picResolutionSb.append(supportedPicResolution.width).append('x')
                    .append(supportedPicResolution.height).append(" ");
        }
        Log.d(TAG, "Supported picture resolutions: " + picResolutionSb);

        Camera.Size defaultPictureResolution = cameraParameters.getPictureSize();
        Log.d(TAG, "default picture resolution " + defaultPictureResolution.width + "x"
                + defaultPictureResolution.height);

        // 排序
        List<Camera.Size> sortedSupportedPicResolutions = new ArrayList<Camera.Size>(
                supportedPicResolutions);
        Collections.sort(sortedSupportedPicResolutions, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size a, Camera.Size b) {
                int aPixels = a.height * a.width;
                int bPixels = b.height * b.width;
                if (bPixels < aPixels) {
                    return -1;
                }
                if (bPixels > aPixels) {
                    return 1;
                }
                return 0;
            }
        });

        // 移除不符合条件的分辨率
        double screenAspectRatio = (double) Utils.getScreenWidth(this)
                / (double) Utils.getScreenHeight(this);
        Iterator<Camera.Size> it = sortedSupportedPicResolutions.iterator();
        while (it.hasNext()) {
            Camera.Size supportedPreviewResolution = it.next();
            int width = supportedPreviewResolution.width;
            int height = supportedPreviewResolution.height;

            // 在camera分辨率与屏幕分辨率宽高比不相等的情况下，找出差距最小的一组分辨率
            // 由于camera的分辨率是width>height，我们设置的portrait模式中，width<height
            // 因此这里要先交换然后在比较宽高比
            boolean isCandidatePortrait = width > height;
            int maybeFlippedWidth = isCandidatePortrait ? height : width;
            int maybeFlippedHeight = isCandidatePortrait ? width : height;
            double aspectRatio = (double) maybeFlippedWidth / (double) maybeFlippedHeight;
            double distortion = Math.abs(aspectRatio - screenAspectRatio);
            if (distortion > MAX_ASPECT_DISTORTION) {
                it.remove();
                continue;
            }
        }
        if (!sortedSupportedPicResolutions.isEmpty()) {
            return sortedSupportedPicResolutions.get(0);
        }
        return defaultPictureResolution;
    }

    //控制图像的正确显示方向
    private void setDisplay(Camera.Parameters parameters, Camera camera) {
        if (Build.VERSION.SDK_INT >= 8) {
            setDisplayOrientation(camera, 90);
        } else {
            parameters.setRotation(90);
        }
    }

    //实现的图像的正确显示
    private void setDisplayOrientation(Camera camera, int i) {
        Method downPolymorphic;
        try {
            downPolymorphic = camera.getClass().getMethod("setDisplayOrientation",
                    int.class);
            if (downPolymorphic != null) {
                downPolymorphic.invoke(camera, i);
            }
        } catch (Exception e) {
            Log.e("Came_e", "图像出错");
        }
    }

    public String saveToSDCard(byte[] data) throws IOException {
        Bitmap croppedImage;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);
        options.inJustDecodeBounds = false;
        options.inSampleSize = 2;
        options.inPurgeable = true;
        Bitmap tempBitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        tempBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        data = baos.toByteArray();
        PHOTO_SIZE = Math.min(tempBitmap.getHeight(), tempBitmap.getWidth());
        int height = Math.max(tempBitmap.getHeight(), tempBitmap.getWidth());
        int offset;//消除偏差
        if (deviceModel.contains(Build.MODEL)) {
            offset = 0;
        } else {
            float rate = (Utils.dip2px(mContext,50) + 0.001f) / (Utils.getScreenWidth(mContext) + 0.001f);
            offset = Math.round(rate * PHOTO_SIZE);
        }

        Rect r;
        if (mCurrentCameraId == 1) {
            r = new Rect(height - PHOTO_SIZE - offset, 0, height - offset, PHOTO_SIZE);
        } else {
            r = new Rect(offset, 0, PHOTO_SIZE + offset, PHOTO_SIZE);
        }

        try {
            croppedImage = decodeRegionCrop(data, r);
        } catch (Exception e) {
            return null;
        }

        String imagePath = ImageUtils.saveBitmap(croppedImage);
        croppedImage.recycle();
        baos.close();
        return imagePath;
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
    private Bitmap decodeRegionCrop(byte[] data, Rect rect) {
        System.gc();
        Bitmap croppedImage = null;
        InputStream inputStream = new ByteArrayInputStream(data);
        try {
            BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(inputStream, false);
            try {
                croppedImage = decoder.decodeRegion(rect, null);
                inputStream.close();
            } catch (IllegalArgumentException e) {
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
        }


        Matrix m = new Matrix();
        m.setRotate(90, PHOTO_SIZE / 2, PHOTO_SIZE / 2);

        if (mCurrentCameraId == 1) {
            m.postScale(1, -1);
        }
        WeakReference<Bitmap> bitmapWeakReference = new WeakReference<Bitmap>(croppedImage);

        Bitmap rotatedImage = Bitmap.createBitmap(bitmapWeakReference.get(), 0, 0, PHOTO_SIZE, PHOTO_SIZE, m, true);
        if (rotatedImage != croppedImage)
            croppedImage.recycle();
        return rotatedImage;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    //切换前后置摄像头
    private void switchCamera() {
        mCurrentCameraId = (mCurrentCameraId + 1) % mCameraHelper.getNumberOfCameras();
        releaseCamera();
        setUpCamera(mCurrentCameraId);
    }

    private void turnLight(Camera mCamera) {
        if (mCamera == null || mCamera.getParameters() == null
                || mCamera.getParameters().getSupportedFlashModes() == null) {
            return;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        String flashMode = mCamera.getParameters().getFlashMode();
        List<String> supportedModes = mCamera.getParameters().getSupportedFlashModes();
        if (Camera.Parameters.FLASH_MODE_OFF.equals(flashMode)
                && supportedModes.contains(Camera.Parameters.FLASH_MODE_ON)) {//关闭状态
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
            mCamera.setParameters(parameters);
            flashIv.setImageResource(R.drawable.camera_flash_on);
            flashTv.setText("打开");
        } else if (Camera.Parameters.FLASH_MODE_ON.equals(flashMode)) {//开启状态
            if (supportedModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                mCamera.setParameters(parameters);
                flashIv.setImageResource(R.drawable.camera_flash_auto);
                flashTv.setText("自动");
            } else if (supportedModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(parameters);
                flashIv.setImageResource(R.drawable.camera_flash_off);
                flashTv.setText("关闭");
            }
        } else if (Camera.Parameters.FLASH_MODE_AUTO.equals(flashMode)
                && supportedModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(parameters);
            flashIv.setImageResource(R.drawable.camera_flash_off);
            flashTv.setText("关闭");
        }
    }

    private void releaseCamera() {

        if (cameraInst != null) {
            cameraInst.setPreviewCallback(null);
            cameraInst.release();
            cameraInst = null;
        }
        adapterSize = null;
        previewSize = null;
    }

    private void setUpCamera(int otherCamera) {
        cameraInst = getCameraInstance(otherCamera);
        if (cameraInst != null) {
            try {
                cameraInst.setPreviewDisplay(albumSurfaceview.getHolder());
                initCamera();
                cameraInst.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
           Utils.shortToast( "切换失败，请重试！");

        }
    }

    private Camera getCameraInstance(final int id) {
        Camera c = null;
        try {
            c = mCameraHelper.openCamera(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    public static void open(Activity context) {
        Intent intent = new Intent(context, CameraActivity.class);
        context.startActivity(intent);
    }

}
