package za.smartee.threesixty.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.provider.Settings;
import android.util.Log;

import za.smartee.threesixty.AppConstants;
import za.smartee.threesixty.R;
import za.smartee.threesixty.utils.Utils;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import butterknife.ButterKnife;



public class GuideActivity extends BaseActivity {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        Log.i("S360Screen","GuideCreate");
        ButterKnife.bind(this);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!isWriteStoragePermissionOpen()) {
                showRequestPermissionDialog();
                return;
            }
        }

        delayGotoMain();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case AppConstants.PERMISSION_REQUEST_CODE: {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                        boolean shouldShowRequest = shouldShowRequestPermissionRationale(permissions[0]);
                        if (shouldShowRequest) {
                            if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                                showRequestPermissionDialog2();
                            } else {
                                showRequestPermissionDialog();
                            }
                        } else {
                            if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                                showOpenSettingsDialog2();
                            } else {
                                showOpenSettingsDialog();
                            }
                        }
                    } else {
                        delayGotoMain();
                    }
                }
            }
        }
    }

    private void delayGotoMain() {
        if (!Utils.isLocServiceEnable(this)) {
            showOpenLocationDialog();
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!isLocationPermissionOpen()) {
                showRequestPermissionDialog2();
                return;
            } else {
                AppOpsManager appOpsManager = (AppOpsManager) getSystemService(APP_OPS_SERVICE);
                int checkOp = appOpsManager.checkOp(AppOpsManager.OPSTR_FINE_LOCATION, Process.myUid(), getPackageName());
                if (checkOp != AppOpsManager.MODE_ALLOWED) {
                    showOpenSettingsDialog2();
                    return;
                }
            }
        }
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent scanStart = new Intent(GuideActivity.this, ScanActivity.class);
                        scanStart.putExtra("appUser",getIntent().getStringExtra("appUser"));
                        scanStart.putExtra("appStore",getIntent().getStringExtra("appStore"));
                        scanStart.putExtra("appStoreCode",getIntent().getStringExtra("appStoreCode"));
                        scanStart.putExtra("loginType",getIntent().getStringExtra("loginType"));
                        startActivity(scanStart);
                        //GuideActivity.this.finish();
                    }
                });
            }
        }.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppConstants.REQUEST_CODE_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!isWriteStoragePermissionOpen()) {
                    showOpenSettingsDialog();
                } else {
                    delayGotoMain();
                }
            }
        }
        if (requestCode == AppConstants.REQUEST_CODE_PERMISSION_2) {
            delayGotoMain();
        }
        if (requestCode == AppConstants.REQUEST_CODE_LOCATION_SETTINGS) {
            if (!Utils.isLocServiceEnable(this)) {
                showOpenLocationDialog();
            } else {
                delayGotoMain();
            }
        }
    }

    private void showOpenSettingsDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.permission_storage_close_title)
                .setMessage(R.string.permission_storage_close_content)
                .setPositiveButton(getString(R.string.permission_open), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, AppConstants.REQUEST_CODE_PERMISSION);
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        return;
                    }
                }).create();
        dialog.show();
    }

    private void showRequestPermissionDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.permission_storage_need_title)
                .setMessage(R.string.permission_storage_need_content)
                .setPositiveButton(getString(R.string.permission_open), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(GuideActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, AppConstants.PERMISSION_REQUEST_CODE);
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        return;
                    }
                }).create();
        dialog.show();
    }

    private void showOpenLocationDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.location_need_title)
                .setMessage(R.string.location_need_content)
                .setPositiveButton(getString(R.string.permission_open), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, AppConstants.REQUEST_CODE_LOCATION_SETTINGS);
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        return;
                    }
                }).create();
        dialog.show();
    }

    private void showOpenSettingsDialog2() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.permission_location_close_title)
                .setMessage(R.string.permission_location_close_content)
                .setPositiveButton(getString(R.string.permission_open), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, AppConstants.REQUEST_CODE_PERMISSION_2);
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        return;
                    }
                }).create();
        dialog.show();
    }

    private void showRequestPermissionDialog2() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.permission_location_need_title)
                .setMessage(R.string.permission_location_need_content)
                .setPositiveButton(getString(R.string.permission_open), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(GuideActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, AppConstants.PERMISSION_REQUEST_CODE);
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        return;
                    }
                }).create();
        dialog.show();
    }
}
