package com.androidcourse.remindyou;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.os.EnvironmentCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

//没有用到，仅留作参考
public class EditActivity extends AppCompatActivity {
    private EditText et_title, et_content;
    private ImageView iv_preview;
    private Button btn_camera, btn_photo;
    private String content;
    private String time;

    //用于保存拍照图片的uri
    private Uri mCameraUri;

    // 用于保存图片的文件路径，Android 10以下使用图片路径访问图片
    private static String mCameraImagePath = "0";

    // 是否是Android 10以上手机
    private boolean isAndroidQ = Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q;

    private static final int CAMERA_REQUEST_CODE = 11;

    //判断返回到的Activity
    private static final int IMAGE_REQUEST_CODE = 0;

    private String old_title = "";
    private String old_content = "";
    private String old_time = "";
    private String old_path = "";
    private int old_Tag = 1;
    private long id = 0;
    private int openMode = 0;
    private int tag = 1;

    public Intent intent = new Intent();
    private Boolean tagchange = false;

    private Toolbar toolbar;

    //定位
    public LocationClient mLocationClient;
    private TextView positionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        et_title = (EditText) findViewById(R.id.et_title);
        et_content = (EditText) findViewById(R.id.et_content);
        iv_preview = (ImageView) findViewById(R.id.iv_preview);
        btn_camera = (Button) findViewById(R.id.btn_camera);
        btn_photo = (Button) findViewById(R.id.btn_photo);

        btnOnClick();

        Intent getIntent = getIntent();
        openMode = getIntent.getIntExtra("mode", 0);
        String string = String.valueOf(openMode);
        Log.d("testx", string);

        if (openMode == 4) {
            mCameraImagePath = getIntent().getStringExtra("path_0");
            Log.e("path_0", mCameraImagePath);
        }

        //设置toolbar
        toolbar = findViewById(R.id.editToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //设置返回键
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autoSetMessage();
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        if (openMode == 3) {
            id = getIntent.getLongExtra("id", 0);
            old_title = getIntent.getStringExtra("title");
            old_content = getIntent.getStringExtra("content");
            old_time = getIntent.getStringExtra("time");
            old_Tag = getIntent.getIntExtra("tag", 1);
            old_path = getIntent.getStringExtra("path");
            et_title.setText(old_title);
            et_title.setSelection(old_title.length());
            et_content.setText(old_content);
            et_content.setSelection(old_content.length());
            Log.e("old_path", old_path);
            Uri uri = getMediaUriFromPath(this, old_path);
            mCameraImagePath = old_path;
            iv_preview.setImageURI(uri);
        }

        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new EditActivity.MyLocationListener());
        positionText = (TextView) findViewById(R.id.position_text_view1);
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(EditActivity.this, Manifest.
                permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(EditActivity.this, Manifest.
                permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(EditActivity.this, Manifest.
                permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(EditActivity.this, permissions, 1);
        } else {
            requestLocation();
        }

    }

    public static Uri getMediaUriFromPath(Context context, String path) {
        Uri mediaUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = context.getContentResolver().query(mediaUri,
                null,
                MediaStore.Images.Media.DISPLAY_NAME + "= ?",
                new String[]{path.substring(path.lastIndexOf("/") + 1)},
                null);

        Uri uri = null;
        if (cursor.moveToFirst()) {
            uri = ContentUris.withAppendedId(mediaUri,
                    cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID)));
        }
        cursor.close();
        return uri;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_HOME) {
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            autoSetMessage();
            setResult(RESULT_OK, intent);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void autoSetMessage() {
        if (openMode == 4) {
            Log.d("123", "goNew");
            if (et_title.getText().toString().length() == 0) {
                intent.putExtra("mode", -1);
            } else {
                intent.putExtra("mode", 0);
                intent.putExtra("title", et_title.getText().toString());
                intent.putExtra("content", et_content.getText().toString());
                intent.putExtra("time", dataToStr());
                intent.putExtra("tag", tag);
                intent.putExtra("loca", positionText.getText().toString());
                intent.putExtra("path", mCameraImagePath);
                Log.e("intent4_path", mCameraImagePath);
            }
        } else {
            Log.d("123", "goEdit");
//            if (et_title.getText().toString().equals(old_title) && !tagchange) {
//                intent.putExtra("mode", -1);
//            } else {
            intent.putExtra("mode", 1);
            intent.putExtra("title", et_title.getText().toString());
            intent.putExtra("content", et_content.getText().toString());
            intent.putExtra("time", dataToStr());
            intent.putExtra("id", id);
            intent.putExtra("tag", tag);
            intent.putExtra("loca", positionText.getText().toString());
            intent.putExtra("path", mCameraImagePath);
            Log.e("intent_path", mCameraImagePath);
//            }
        }
    }

    //获取时间
    public String dataToStr() {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(date);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                new AlertDialog.Builder(EditActivity.this)
                        .setMessage("确定删除吗？")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (openMode == 4) {
                                    intent.putExtra("mode", -1);
                                    setResult(RESULT_OK, intent);
                                } else {
                                    intent.putExtra("mode", 2);
                                    intent.putExtra("id", id);
                                    setResult(RESULT_OK, intent);
                                }
                                finish();
                            }
                        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create().show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(5000);
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }

    private void requestLocation() {
        mLocationClient.start();
        initLocation();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(final BDLocation location) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    StringBuilder currentPosition = new StringBuilder();
                    currentPosition.append("").append(location.getCountry()).append("");
                    currentPosition.append("").append(location.getProvince()).append("");
                    currentPosition.append("").append(location.getCity()).append("");
                    currentPosition.append("").append(location.getDistrict()).append("");
//                    currentPosition.append("定位方式：");
//                    if (location.getLocType() == BDLocation.TypeGpsLocation) {
//                        currentPosition.append("GPS");
//                    } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
//                        currentPosition.append("网络");
//                    }
                    positionText.setText(currentPosition);
                }
            });
        }


    }

    private void openCamera() {
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 判断是否有相机
        if (captureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            Uri photoUri = null;

            if (isAndroidQ) {
                // 适配android 10
                photoUri = createImageUri();
            } else {
                try {
                    photoFile = createImageFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (photoFile != null) {
                    mCameraImagePath = photoFile.getAbsolutePath();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        //适配Android 7.0文件权限，通过FileProvider创建一个content类型的Uri
                        photoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
                    } else {
                        photoUri = Uri.fromFile(photoFile);
                    }
                }
            }

            mCameraUri = photoUri;
            if (photoUri != null) {
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                captureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivityForResult(captureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

    /**
     * 创建图片地址uri,用于保存拍照后的照片 Android 10以后使用这种方法
     */
    private Uri createImageUri() {
        String status = Environment.getExternalStorageState();
        // 判断是否有SD卡,优先使用SD卡存储,当没有SD卡时使用手机存储
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            return getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
        } else {
            return getContentResolver().insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, new ContentValues());
        }
    }

    /**
     * 创建保存图片的文件
     */
    private File createImageFile() throws IOException {
        String imageName = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }
        File tempFile = new File(storageDir, imageName);
        if (!Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(tempFile))) {
            return null;
        }
        return tempFile;
    }

    private void btnOnClick() {
        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //拍照
                openCamera();
            }
        });
        btn_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(EditActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(EditActivity.this, new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, 1);
                }
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, IMAGE_REQUEST_CODE);
            }

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    if (isAndroidQ) {
                        // Android 10 使用图片uri加载
                        iv_preview.setImageURI(mCameraUri);
                        mCameraImagePath = getFilePath(this, mCameraUri);
                        Log.e("uri_path", mCameraImagePath);
                    } else {
                        // 使用图片路径加载
                        iv_preview.setImageBitmap(BitmapFactory.decodeFile(mCameraImagePath));
                    }
                    Log.e("result_path", mCameraImagePath);
                } else {
                    iv_preview.setVisibility(View.INVISIBLE);
                    Toast.makeText(this, "取消", Toast.LENGTH_LONG).show();
                }
                break;
            case IMAGE_REQUEST_CODE:
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(getApplication(), "点击取消从相册选择", Toast.LENGTH_LONG).show();
                    return;
                }
                try {
                    mCameraUri = data.getData();
                    Log.e("TAG", mCameraUri.toString());
                    iv_preview.setImageURI(mCameraUri);
                    mCameraImagePath = getFilePath(this, mCameraUri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    public static String getFilePath(final Context context, final Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null) data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }


    public String getRealPathFromURI(Uri contentUri) {
        Cursor cursor = null;
        try {

            String[] proj = {MediaStore.Images.Media.DATA};
            Context context = getApplicationContext();
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            cursor.moveToFirst();
            int column_indenx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            return cursor.getString(column_indenx);
        } finally {

            if (cursor != null) {

                cursor.close();
            }
        }
    }

    public Uri queryUriforAudio(String path, Uri databaseUri) {
        File file = new File(path);
        final String where = databaseUri + "='" + file.getAbsolutePath() + "'";
        Cursor cursor = this.getContentResolver().query(databaseUri, null, where, null, null);
        if (cursor == null) {
            Log.d("uritest", "queryUriforAudio: uri为空 1");
            return null;
        }
        int id = -1;
        if (cursor != null) {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                id = cursor.getInt(0);
            }
            cursor.close();
        }
        if (id == -1) {
            Log.d("uritest", "queryUriforAudio: uri为空 2");
            return null;
        }
        return Uri.withAppendedPath(databaseUri, String.valueOf(id));
    }


    private Bitmap getresizePhoto(String ImagePath) {
        if (ImagePath != null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(ImagePath, options);
            double ratio = Math.max(options.outWidth * 1.0d / 1024f, options.outHeight * 1.0d / 1024);
            options.inSampleSize = (int) Math.ceil(ratio);
            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeFile(ImagePath, options);
            Log.e("1", "bitmap");
            return bitmap;
        }
        return null;
    }

}
