package za.smartee.threesixty.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
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
import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.enums.Display;
import com.github.javiersantos.appupdater.enums.UpdateFrom;

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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allocations);
        Button saveButton = (Button) findViewById(R.id.saveBut);
        Button resetButton = (Button) findViewById(R.id.resetButton);
        Button nextButton = (Button) findViewById(R.id.nextButton);

        saveButton.setVisibility(View.INVISIBLE);
        resetButton.setVisibility(View.INVISIBLE);
        nextButton.setVisibility(View.INVISIBLE);

        TextView selectedStoreHeader = (TextView) findViewById(R.id.selectedStoreHeader);
        TextView assetBarcodeHeader = (TextView) findViewById(R.id.assetBarcodeHeader);
        TextView storeCodeHeader = (TextView) findViewById(R.id.storeCodeHeader);
        EditText storeCode = (EditText) findViewById(R.id.storeCode);
        TextView selectedStore = (TextView) findViewById(R.id.selectedStore);
        EditText assetBarcode = (EditText) findViewById(R.id.assetBarcode);

        spinner = (ProgressBar) findViewById(R.id.progressBar);
        //Make all views invisible until dataset is loaded
        selectedStoreHeader.setVisibility(View.INVISIBLE);
        assetBarcodeHeader.setVisibility(View.INVISIBLE);
        selectedStore.setVisibility(View.INVISIBLE);
        assetBarcode.setVisibility(View.INVISIBLE);
        storeCodeHeader.setVisibility(View.INVISIBLE);
        storeCode.setVisibility(View.INVISIBLE);

        //Auto Update Check
        AppUpdater appUpdater = new AppUpdater(this)
                .setDisplay(Display.DIALOG)
                .setUpdateFrom(UpdateFrom.JSON)
                .setCancelable(false)
                .setUpdateJSON("https://s360rellog.s3.amazonaws.com/update-changelogAssetAllocations.json");
        appUpdater.start();
        Log.i("VCheck","ProdAutoUpdatev6");
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

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean validatedFlag = false;
                String assetId = assetBarcode.getText().toString();
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
}