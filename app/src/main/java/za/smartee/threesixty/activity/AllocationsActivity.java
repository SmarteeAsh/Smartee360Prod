package za.smartee.threesixty.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.FileProvider;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.amplifyframework.datastore.generated.model.Assets;
import com.amplifyframework.datastore.generated.model.AuditLog;
import com.amplifyframework.datastore.generated.model.Locations;
import com.amplifyframework.datastore.generated.model.ScannedAssetsAuditLog;
import com.amplifyframework.datastore.generated.model.AssetAllocationStores;
import com.amplifyframework.rx.RxAmplify;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.AppUpdaterUtils;
import com.github.javiersantos.appupdater.enums.AppUpdaterError;
import com.github.javiersantos.appupdater.enums.Display;
import com.github.javiersantos.appupdater.enums.UpdateFrom;
import com.github.javiersantos.appupdater.objects.Update;
import com.google.zxing.Result;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import za.smartee.threesixty.R;


public class AllocationsActivity extends AppCompatActivity {
    TextView storeCodeHeader;
    TextView storeCode;

    ArrayList<Map<String, String>> locationDetailInfo;
    ArrayList<Map<String, String>> assetDetailInfo;
    ArrayList<String> assets = new ArrayList<>();
    String storeName;
    String assetDevId;
    private ProgressBar spinner;

    private CodeScanner mCodeScanner;
    String dataInfo;
    Boolean decodedFlag;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allocations);
        Button saveButton = (Button) findViewById(R.id.saveBut);
        Button resetButton = (Button) findViewById(R.id.resetButton);
        Button nextButton = (Button) findViewById(R.id.nextButton);
        Button scannerActivate = (Button) findViewById(R.id.barcodeScanner);



        saveButton.setVisibility(View.INVISIBLE);
        resetButton.setVisibility(View.INVISIBLE);
        nextButton.setVisibility(View.INVISIBLE);

        TextView selectedStoreHeader = (TextView) findViewById(R.id.selectedStoreHeader);
        TextView assetBarcodeHeader = (TextView) findViewById(R.id.assetBarcodeHeader);
        TextView storeCodeHeader = (TextView) findViewById(R.id.storeCodeHeader);
        EditText storeCode = (EditText) findViewById(R.id.storeCode);
        TextView selectedStore = (TextView) findViewById(R.id.selectedStore);
        EditText assetBarcode = (EditText) findViewById(R.id.assetBarcode);
        TextView sysStatus = (TextView) findViewById(R.id.systemStatus);
        sysStatus.setText("Status - Online");
        sysStatus.setBackgroundColor(0xff00ff00);

        decodedFlag = false;
        CodeScannerView scannerView = findViewById(R.id.scanner_view);
        scannerActivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                scannerView.setVisibility(View.VISIBLE);
            }
        });
        mCodeScanner = new CodeScanner(this, scannerView);
        scannerView.setVisibility(View.INVISIBLE);
        scannerActivate.setVisibility(View.INVISIBLE);
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        assetBarcode.setText(result.getText());
                        scannerView.setVisibility(View.INVISIBLE);
//                        Toast.makeText(MainActivity.this, result.getText(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCodeScanner.startPreview();
            }
        });

        spinner = (ProgressBar) findViewById(R.id.progressBar);
        //Make all views invisible until dataset is loaded
        selectedStoreHeader.setVisibility(View.INVISIBLE);
        assetBarcodeHeader.setVisibility(View.INVISIBLE);
        selectedStore.setVisibility(View.INVISIBLE);
        assetBarcode.setVisibility(View.INVISIBLE);
        storeCodeHeader.setVisibility(View.INVISIBLE);
        storeCode.setVisibility(View.INVISIBLE);

        //Setup a subscription which checks for changes in network connection
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build();

        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(ConnectivityManager.class);
        connectivityManager.requestNetwork(networkRequest, networkCallback);


        //Auto Update Check
        AppUpdaterUtils appUpdaterUtils = new AppUpdaterUtils(this)
                .setUpdateFrom(UpdateFrom.JSON)
                .setUpdateJSON("https://s360rellog.s3.amazonaws.com/update-changelog-warehouseallocations-nr.json")
                .withListener(new AppUpdaterUtils.UpdateListener() {
                    @Override
                    public void onSuccess(Update update, Boolean isUpdateAvailable) {
//                        Log.d("Latest Version", update.getLatestVersion());
//                        Log.d("Latest Version Code", String.valueOf(update.getLatestVersionCode()));
//                        Log.d("Release notes", update.getReleaseNotes());
//                        Log.d("URL", String.valueOf(update.getUrlToDownload()));
//                        Log.d("Is update available?", Boolean.toString(isUpdateAvailable));
                        if (isUpdateAvailable) {
                            new DownloadFileFromURL().execute(String.valueOf(update.getUrlToDownload()));
                        }
                    }

                    @Override
                    public void onFailed(AppUpdaterError error) {
                        Log.d("AppUpdater Error", "Something went wrong");
                    }
                });
        appUpdaterUtils.start();


        queryAssets();

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedStoreCode = storeCode.getText().toString();
                Boolean foundFlag = false;
                for (int i = 0; i < locationDetailInfo.size(); i++){
                    if (locationDetailInfo.get(i).get("StoreCode").equals(selectedStoreCode)){
                        foundFlag = true;
                        selectedStoreHeader.setVisibility(View.VISIBLE);
                        selectedStore.setText(locationDetailInfo.get(i).get("storeName"));
                        selectedStore.setVisibility(View.VISIBLE);
                        assetBarcode.setVisibility(View.VISIBLE);
                        assetBarcodeHeader.setVisibility(View.VISIBLE);
                        scannerActivate.setVisibility(View.VISIBLE);
                        storeName = locationDetailInfo.get(i).get("storeName");
                        saveButton.setVisibility(View.VISIBLE);
                        nextButton.setVisibility(View.INVISIBLE);
                    }
                }
                if (!foundFlag){
                    Toast.makeText(AllocationsActivity.this,"Could not find store, please retry",Toast.LENGTH_LONG).show();
                    storeCode.setText("");
                }
            }
        });

        scannerActivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!decodedFlag) {
                    scannerView.setVisibility(View.VISIBLE);
                    mCodeScanner.startPreview();
                    decodedFlag = true;
                } else {
                    scannerView.setVisibility(View.INVISIBLE);
                    mCodeScanner.stopPreview();
                    decodedFlag = false;
                }

            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            String assetId;
            @Override
            public void onClick(View v) {
                Boolean validatedFlag = false;
                String assetIdTemp = assetBarcode.getText().toString();
                if (assetIdTemp.startsWith("NR")){
                    assetId = assetIdTemp.replace("NR", "99999");
                } else {
                    assetId = assetIdTemp;
                }
                for (int r = 0; r < assetDetailInfo.size(); r++){
                    if (assetDetailInfo.get(r).get("assetName").equals(assetId)){
                        validatedFlag = true;
                        assetDevId = assetDetailInfo.get(r).get("assetId");
                    }
                }
                if (validatedFlag){
                    String storeCodeString = storeCode.getText().toString();
                    saveAllocation(storeName,assetDevId, storeCodeString);
                    saveButton.setVisibility(View.INVISIBLE);
                    nextButton.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(AllocationsActivity.this,"Could not find asset, please retry",Toast.LENGTH_LONG).show();
                }

            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetForm();
            }
        });

    }

    void queryStoreDetails(){
        TextView storeCodeHeader = (TextView) findViewById(R.id.storeCodeHeader);
        TextView storeCode = (EditText) findViewById(R.id.storeCode);
        locationDetailInfo = new ArrayList<Map<String, String>>();
        RxAmplify.DataStore.query(AssetAllocationStores.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(() -> {

                    spinner = (ProgressBar) findViewById(R.id.progressBar);
                    spinner.setVisibility(View.GONE);
                    storeCodeHeader.setVisibility(View.VISIBLE);
                    storeCode.setVisibility(View.VISIBLE);
                    Button saveButton = (Button) findViewById(R.id.saveBut);
                    Button resetButton = (Button) findViewById(R.id.resetButton);
                    Button nextButton = (Button) findViewById(R.id.nextButton);
                    resetButton.setVisibility(View.VISIBLE);
                    nextButton.setVisibility(View.VISIBLE);
                    Log.i("S360", String.valueOf(locationDetailInfo));
                })
                .doOnError( error -> {
                    Log.i("S360","DS Error");
                })
                .subscribe(
                        locResponse -> {
                            Map<String, String> locationDetailInfo1 = new HashMap<String, String>();
                            locationDetailInfo1.put("StoreCode", locResponse.getStoreCode());
                            locationDetailInfo1.put("id", locResponse.getId());
                            locationDetailInfo1.put("storeName", locResponse.getStoreName());
                            locationDetailInfo1.put("status", locResponse.getStatus());
                            locationDetailInfo1.put("baseAllocationType", locResponse.getBaseAllocationType());
                            locationDetailInfo.add(locationDetailInfo1);
                        });
    }

    void saveAllocation(String storeName, String assetID, String storeCode){

        //Alert Dialog for no network connection
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
        dlgAlert.setMessage("Save Failed, Please Retry");
        dlgAlert.setTitle("Failure");
        dlgAlert.setPositiveButton("OK", null);
        dlgAlert.setCancelable(true);
        dlgAlert.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        resetForm();
                    }
                });
        if (storeName.equals("")){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dlgAlert.show();
                }
            });
        }
        else if (assetID.equals("")){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dlgAlert.show();
                }
            });
        }
        else {
                AuditLog assetData = AuditLog.builder()
                        .baseActionType("Asset Allocation")
                        .device(assetID)
                        .storeName(storeName)
                        .selectedStoreName(storeCode)
                        .scanTime(String.valueOf(Calendar.getInstance().getTime()))
                        .build();

                RxAmplify.DataStore.save(assetData)
                        .subscribe(
                                () ->
                                {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(AllocationsActivity.this, "Saved Successfully", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                },
                                failure -> {
                                    Log.e("S360 Failure","save Failed");
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            dlgAlert.show();
                                        }
                                    });
                                }
                        );
                resetForm();


        }

    }

    void resetForm(){
        TextView selectedStoreHeader = (TextView) findViewById(R.id.selectedStoreHeader);
        TextView assetBarcodeHeader = (TextView) findViewById(R.id.assetBarcodeHeader);
        TextView storeCodeHeader = (TextView) findViewById(R.id.storeCodeHeader);
        EditText storeCode = (EditText) findViewById(R.id.storeCode);
        TextView selectedStore = (TextView) findViewById(R.id.selectedStore);
        EditText assetBarcode = (EditText) findViewById(R.id.assetBarcode);
        //Make all views invisible until dataset is loaded
        selectedStoreHeader.setVisibility(View.INVISIBLE);
        assetBarcodeHeader.setVisibility(View.INVISIBLE);
        selectedStore.setVisibility(View.INVISIBLE);
        assetBarcode.setVisibility(View.INVISIBLE);
        Button saveButton = (Button) findViewById(R.id.saveBut);
        Button nextButton = (Button) findViewById(R.id.nextButton);
        saveButton.setVisibility(View.INVISIBLE);
        nextButton.setVisibility(View.VISIBLE);
        storeCode.setText("");
        selectedStore.setText("");
        assetBarcode.setText("");


    }

    void queryAssets(){
        assetDetailInfo = new ArrayList<Map<String, String>>();
        RxAmplify.DataStore.query(Assets.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(()-> {
                    queryStoreDetails();
                })
                .subscribe(
                        assetResponse -> {
                            Map<String, String> assetDetailInfo1 = new HashMap<String, String>();
                            assetDetailInfo1.put("assetId", assetResponse.getAssetId());
                            assetDetailInfo1.put("id", assetResponse.getId());
                            assetDetailInfo1.put("assetName", assetResponse.getAssetName());
                            assetDetailInfo.add(assetDetailInfo1);
                        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(AuthActivity.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(@NonNull Network network) {
            super.onAvailable(network);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView sysStatus = (TextView) findViewById(R.id.systemStatus);
                    sysStatus.setText("Status - Online");
                    sysStatus.setBackgroundColor(0xff00ff00);
                }
            });

        }

        @Override
        public void onLost(@NonNull Network network) {
            super.onLost(network);


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView sysStatus = (TextView) findViewById(R.id.systemStatus);
                    sysStatus.setText("Ready - Working Offline");
                    sysStatus.setBackgroundColor(0xffff0000);
                }
            });

        }

        @Override
        public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities);
            final boolean unmetered = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED);
        }
    };

    class DownloadFileFromURL extends AsyncTask<String, String, String> {
        ProgressDialog pd;
        String pathFolder = "";
        String pathFile = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(AllocationsActivity.this);
            pd.setTitle("Update Downloading...");
            pd.setMessage("Please wait.");
            pd.setMax(100);
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pd.setCancelable(true);
            pd.show();
        }

        @Override
        protected String doInBackground(String... f_url) {
            int count;

            try {
//                pathFolder = Environment.getExternalStorageDirectory() + "/YourAppDataFolder";
                pathFolder = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
                pathFile = pathFolder + "/s360auto.apk";
                File futureStudioIconFile = new File(pathFolder);
                if (!futureStudioIconFile.exists()) {
                    futureStudioIconFile.mkdirs();
                }

                File apkFileName = new File(pathFile);
                if (apkFileName.exists()){
                    File file = new File(pathFolder, "s360auto.apk");
                    boolean deleted = file.delete();
                }

                URL url = new URL(f_url[0]);
                URLConnection connection = url.openConnection();
                connection.connect();

                // this will be useful so that you can show a tipical 0-100%
                // progress bar
                int lengthOfFile = connection.getContentLength();

                // download the file
                InputStream input = new BufferedInputStream(url.openStream());
                FileOutputStream output = new FileOutputStream(pathFile);

                byte data[] = new byte[1024]; //anybody know what 1024 means ?
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lengthOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();


            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return pathFile;
        }

        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            pd.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String file_url) {
            if (pd != null) {
                pd.dismiss();
            }
            File toInstall = new File(file_url);
            Intent intent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri apkUri = FileProvider.getUriForFile(AllocationsActivity.this, "za.smartee.threeSixty", toInstall);
                intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                intent.setData(apkUri);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Log.i("S360","Veriosn N");
            } else {
                Uri apkUri = Uri.fromFile(toInstall);
                intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Log.i("S360","Veriosn < N");
            }
            AllocationsActivity.this.startActivity(intent);
        }

    }

}