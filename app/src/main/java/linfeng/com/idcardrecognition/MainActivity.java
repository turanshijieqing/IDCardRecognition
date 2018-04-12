package linfeng.com.idcardrecognition;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final int PICK_IMAGE_REQUEST = 0x11;
    private final int REQ_READ_EXTERNAL_STORAGE = 0x12;

    private TextView mTvName;
    private TextView mTvBirth;
    private TextView mTvSexType;
    private TextView mTvNation;
    private TextView mTvIdNumber;
    private TextView mTvAddress;
    private TextView mTvValidData;
    private TextView mTvIssue;
    private Button mBtnFaceID;
    private Button mBtnBackID;

    private String ID_TYPE;
    private final String ID_BACK = "back";
    private final String ID_FACE = "face";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTvName = findViewById(R.id.tv_name);
        mTvBirth = findViewById(R.id.tv_birth);
        mTvSexType = findViewById(R.id.tv_sex_type);
        mTvNation = findViewById(R.id.tv_nation);
        mTvIdNumber = findViewById(R.id.tv_id_number);
        mTvAddress = findViewById(R.id.tv_address);
        mTvValidData = findViewById(R.id.tv_valid_data);
        mTvIssue = findViewById(R.id.tv_issue);
        mBtnFaceID = findViewById(R.id.btn_face_id);
        mBtnBackID = findViewById(R.id.btn_back_id);

        mBtnFaceID.setOnClickListener(this);
        mBtnBackID.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_face_id:
                ID_TYPE = ID_FACE;
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    //申请WRITE_EXTERNAL_STORAGE权限
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQ_READ_EXTERNAL_STORAGE);
                } else {
                    startActivityForResult(getPickImageChooserIntent(), PICK_IMAGE_REQUEST);
                }
                break;
            case R.id.btn_back_id:
                ID_TYPE = ID_BACK;
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    //申请WRITE_EXTERNAL_STORAGE权限
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQ_READ_EXTERNAL_STORAGE);
                } else {
                    startActivityForResult(getPickImageChooserIntent(), PICK_IMAGE_REQUEST);
                }
                break;
        }
    }

    private void recognitionIDCard(Bitmap bitmap) {
        byte[] bitmapBtyeArray;
        try {
            File file = new File(getCacheDir(), "IDCard.png");
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(file));
            FileInputStream in = new FileInputStream(file);
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] data = new byte[512];
            int count;
            while ((count = in.read(data, 0, 512)) != -1) {
                outStream.write(data, 0, count);
            }
            bitmapBtyeArray = outStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "图片读取失败", Toast.LENGTH_SHORT).show();
            return;
        }
        String bitmapEncodeStr = android.util.Base64.encodeToString(bitmapBtyeArray, android.util.Base64.DEFAULT);
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", "APPCODE 88fa0abd50a8489b823c3c8e181e1119");
        headers.put("Content-Type", "application/json; charset=UTF-8");

        RequestObj testBody = new RequestObj();
        testBody.configure = new RequestObj.Configure();
        testBody.image = bitmapEncodeStr;
        testBody.configure.side = ID_TYPE;
        String s = new Gson().toJson(testBody);

        ServiceApiHelper.getApiService().recordCD(headers, testBody)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(ResponseBody value) {
                        try {
                            IDCardRecognitionObj idCardRecognitionObj = new Gson().fromJson(value.string(), IDCardRecognitionObj.class);
                            if (idCardRecognitionObj.success) {
                                if (ID_TYPE == ID_FACE) {
                                    mTvAddress.setText(idCardRecognitionObj.address);
                                    mTvBirth.setText(idCardRecognitionObj.birth);
                                    mTvIdNumber.setText(idCardRecognitionObj.num);
                                    mTvName.setText(idCardRecognitionObj.name);
                                    mTvNation.setText(idCardRecognitionObj.nationality);
                                    mTvSexType.setText(idCardRecognitionObj.sex);
                                } else {
                                    mTvIssue.setText(idCardRecognitionObj.issue);
                                    mTvValidData.setText(idCardRecognitionObj.start_date + "-" + idCardRecognitionObj.end_date);
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {

            if (data.getExtras() != null && data.getExtras().get("data") != null) {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                recognitionIDCard(bitmap);
            } else {
                Uri uri = data.getData();
                Log.e("test", "onActivityResult: " + uri);
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    recognitionIDCard(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQ_READ_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivityForResult(getPickImageChooserIntent(), PICK_IMAGE_REQUEST);
            }
        }
    }

    public Intent getPickImageChooserIntent() {
        // Determine Uri of camera image to save.
        //Uri outputFileUri = getCaptureImageOutputUri();

        List allIntents = new ArrayList();
        PackageManager packageManager = getPackageManager();

        //添加所有相机
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        List listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (Object res : listCam) {
            if (res instanceof ResolveInfo) {
                ResolveInfo resolveInfo = (ResolveInfo) res;
                Intent intent = new Intent(captureIntent);
                intent.setComponent(new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name));
                intent.setPackage(resolveInfo.activityInfo.packageName);
                allIntents.add(intent);
            }
        }

        //添加所有相册
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        List listGallery = packageManager.queryIntentActivities(galleryIntent, 0);
        for (Object res : listGallery) {
            if (res instanceof ResolveInfo) {
                ResolveInfo resolveInfo = (ResolveInfo) res;
                Intent intent = new Intent(galleryIntent);
                intent.setComponent(new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name));
                intent.setPackage(resolveInfo.activityInfo.packageName);
                allIntents.add(intent);
            }
        }

        // the main intent is the last in the list (fucking android) so pickup the useless one
        Intent mainIntent = (Intent) allIntents.get(allIntents.size() - 1);
        for (Object intent : allIntents) {
            if (intent instanceof Intent) {
                Intent i = (Intent) intent;
                if (i.getComponent().getClassName().equals("com.android.documentsui.DocumentsActivity")) {
                    mainIntent = i;
                    break;
                }
            }
        }
        allIntents.remove(mainIntent);

        //创建选择器
        Intent chooserIntent = Intent.createChooser(mainIntent, "选择照片");

        //添加所有Intent
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toArray(new Parcelable[allIntents.size()]));
        return chooserIntent;
    }
}
