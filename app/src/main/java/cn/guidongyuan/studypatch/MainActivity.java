package cn.guidongyuan.studypatch;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int MY_PERMISSIONS_REQUEST_STORAGE = 1;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView textView = findViewById(R.id.sample_text);
        textView.setText(BuildConfig.VERSION_NAME);
    }


    /**
     * 更新
     */
    public void update(View view) {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_STORAGE);
        }else {
            downLoadApkPatch();
        }

    }

    /**
     * 模拟下载，合并应用包，跳转安装
     */
    private void downLoadApkPatch() {
        new AsyncTask<Void,Void,File>() {

            @Override
            protected File doInBackground(Void... voids) {
                // 获取当前应用的路径
                String oldPath = getApplication().getApplicationInfo().sourceDir;
                String newPath = Environment.getExternalStorageDirectory().getPath() + File.separator + "new.apk";
                String patchPath = Environment.getExternalStorageDirectory().getPath() + File.separator + "patch.diff";

                if (new File(patchPath).exists()) {
                    native_bspatch(oldPath,newPath,patchPath);
                    return new File(newPath);
                }else {
                    Log.w(TAG, "patch file is not exists");
                    return null;
                }
            }

            @Override
            protected void onPostExecute(File file) {
                if (file != null) {
                    installNewApk(file);
                }
            }
        }.execute();
    }

    /**
     * 跳转到安装页面
     * @param file 新包路径
     */
    private void installNewApk(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }else {
            // 声明需要的临时权限
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            String packageName = getApplication().getPackageName();
            Uri contentUri = FileProvider.getUriForFile(MainActivity.this, packageName + ".fileProvider", file);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        }
        startActivity(intent);
    }

    /**
     * 合并差分包
     * @param oldApk 当前项目路径
     * @param newApk 差分包路径
     * @param patchFile 合并后新包路径
     */
    private native void native_bspatch(String oldApk,String newApk,String patchFile);

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_STORAGE:{
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    downLoadApkPatch();
                }
            }
            break;
            default:
                break;
        }
    }
}
