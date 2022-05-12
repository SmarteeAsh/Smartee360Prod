package za.smartee.threesixty.activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.SignOutOptions;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.AWSDataStorePlugin;
import com.amplifyframework.datastore.DataStoreChannelEventName;
import com.amplifyframework.datastore.DataStoreConfiguration;
import com.amplifyframework.datastore.DataStoreConflictHandler;
import com.amplifyframework.datastore.DataStoreException;
import com.amplifyframework.datastore.events.NetworkStatusEvent;
import com.amplifyframework.datastore.generated.model.Assets;
import com.amplifyframework.datastore.generated.model.Locations;
import com.amplifyframework.datastore.syncengine.PendingMutation;
import com.amplifyframework.hub.HubChannel;
import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.enums.Display;
import com.github.javiersantos.appupdater.enums.UpdateFrom;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import za.smartee.threesixty.BuildConfig;
import za.smartee.threesixty.R;

public class ScanActivity extends BaseActivity {
    Button scanButton;
    Button signOutButton;
    TextView infoText1;
    TextView infoText2;
    TextView infoText3;
    TextView infoHeader;
    Button doneButton;

    List<Map<String, String>> locationDetailInfo;
    ArrayList<String> locDdData = new ArrayList<String>();
    List<Map<String, String>> assetDetailInfo;
    ArrayList<String> assetItems = new ArrayList<String>();
    private boolean loadingChecked = false;
    private boolean DSSuccess = true;
    public boolean networkConnectStatus;
    public int mutationCounter;
    private ProgressBar spinner;
    Long scannerSetTime;
    Long waitSetTime;
    public Boolean mutationCompleteFlag = false;
    public Boolean processingFlag = false;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Log.i("S360Screen","ScanCreate");
        //Get the app user who initiatied the start of teh app
        String appUser = getIntent().getStringExtra("appUser");

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
        AppUpdater appUpdater = new AppUpdater(this)
                .setDisplay(Display.DIALOG)
                .setUpdateFrom(UpdateFrom.JSON)
                .setCancelable(false)
                .setUpdateJSON("https://s360rellog.s3.amazonaws.com/update-changelog.json");
        appUpdater.start();
        Log.i("VCheck","ProdAutoUpdatev6");


        scanButton = (Button) findViewById(R.id.btnScan);
        signOutButton = (Button) findViewById(R.id.btnSignOut);
        TextView answer1 = (TextView) findViewById(R.id.scanInfo);
        TextView answer2 = (TextView) findViewById(R.id.scanInfo2);
        TextView answer3 = (TextView) findViewById(R.id.scanInfo3);
        infoHeader = (TextView) findViewById(R.id.infoHeader);
        TextView answer4 = (TextView) findViewById(R.id.TextViewSelectedStore);
        doneButton = (Button) findViewById(R.id.DONE);
        Switch loadingSwitch = (Switch) findViewById(R.id.switchLoading);
        TextView vCode = (TextView) findViewById(R.id.vCode);
        vCode.setText(BuildConfig.VERSION_NAME);
        loadingSwitch.setVisibility(View.INVISIBLE);
        scanButton.setVisibility(View.INVISIBLE);
        doneButton.setVisibility(View.INVISIBLE);
        loadingSwitch.setChecked(false);

        //Alert Dialog for no network connection
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
        dlgAlert.setMessage("No Network Available, please connect and retry");
        dlgAlert.setTitle("Internet Connection Error");
        dlgAlert.setPositiveButton("OK", null);
        dlgAlert.setCancelable(true);
        dlgAlert.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent rs = new Intent(ScanActivity.this, AuthActivity.class);
                        startActivity(rs);
                        Log.i("dialog msg","clicked");
                    }
                });



        //Confirm Scan Error Handling and display scanned information
        Boolean scanHistFlag = getIntent().getBooleanExtra("scanHistFlag",false);
        if (scanHistFlag){
            String text = getIntent().getStringExtra("selectedLocation");
            String scanTime = getIntent().getStringExtra("scanTime");
            String numberNewAssets = getIntent().getStringExtra("scannedAssets");
            String numberExistingAssets = getIntent().getStringExtra("assetsInStore");
            answer1.setVisibility(View.VISIBLE);
            answer1.setText("Last Scanned Time: " + scanTime);
            answer2.setVisibility(View.VISIBLE);
            answer2.setText("Number of Assets in Store: " + numberExistingAssets);
            answer3.setVisibility(View.VISIBLE);
            answer3.setText("Number of Scanned Assets:" + numberNewAssets);
            answer4.setVisibility(View.VISIBLE);
            answer4.setText("Selected Location:" + text);
        }

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    loadingChecked = loadingSwitch.isChecked();
                    processingFlag = false;
                    Intent i = new Intent(ScanActivity.this, ScanConfirmActivity.class);
                    i.putExtra("appUser",appUser);
                    i.putExtra("loadingFlag", loadingChecked);
                    startActivity(i);
            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               onDonePressed();
            }
        });

        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AWSMobileClient.getInstance().signOut(SignOutOptions.builder().signOutGlobally(true).build(), new Callback<Void>() {
                    @Override
                    public void onResult(final Void result) {
                        Log.d("Signout Msg", "signed-out");
                        Intent i = new Intent(ScanActivity.this, AuthActivity.class);
                        i.putExtra("appUser",appUser);
                        startActivity(i);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("Signout Msg", "sign-out error", e);
                        Intent i = new Intent(ScanActivity.this, AuthActivity.class);
                        startActivity(i);
                    }
                });

            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();

        Log.i("S360Screen","ScanResume");

        //Start Datastore observations for Assets and Locations
        Amplify.DataStore.observe(Assets.class,
                cancelable -> Log.i("S360", "Observation began."),
                postChanged -> {
                    Assets post = postChanged.item();
                    //Log.i("MyAmplifyApp", "Post: " + post);
                },
                failure -> {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            infoHeader.setText("SCANNER INFORMATION");
                            spinner = (ProgressBar) findViewById(R.id.progressBar);
                            spinner.setVisibility(View.GONE);
                            doneButton.setVisibility(View.VISIBLE);
                            scanButton.setVisibility(View.VISIBLE);
                        }
                    });

                    Log.e("S360", "Observation failed.", failure);
                },
                () -> Log.i("S360", "Observation complete.")
        );

        Amplify.DataStore.observe(Locations.class,
                cancelable -> Log.i("MyAmplifyApp", "Observation began."),
                locPostChanged -> {
                    Locations post2 = locPostChanged.item();
                   // Log.i("MyAmplifyApp", "Post2: " + post2);
                },
                failure -> {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            infoHeader.setText("SCANNER INFORMATION");
                            spinner = (ProgressBar) findViewById(R.id.progressBar);
                            spinner.setVisibility(View.GONE);
                            doneButton.setVisibility(View.VISIBLE);
                            scanButton.setVisibility(View.VISIBLE);
                        }
                    });
                    Log.e("S360", "Observation failed.", failure);
                },
                () -> Log.i("S360", "Observation complete.")
        );

        //Display the spinner and indicate to the users that synchronization is in progress
        spinner = (ProgressBar) findViewById(R.id.progressBar);
        spinner.setVisibility(View.VISIBLE);
        doneButton.setVisibility(View.INVISIBLE);
        scanButton.setVisibility(View.INVISIBLE);
        infoHeader.setText("Sync In Progress");

        Boolean scanHistFlag = getIntent().getBooleanExtra("scanHistFlag",false);
        if (isNetworkAvailable()){
            //If this page is generated without a scanConfirmation being completed
            if (!scanHistFlag){
                Amplify.Hub.subscribe(
                        HubChannel.DATASTORE,
                        hubEvent -> DataStoreChannelEventName.SYNC_QUERIES_READY.toString().equals(hubEvent.getName()),
                        hubEvent -> {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    infoHeader.setText("SCANNER INFORMATION");
                                    spinner = (ProgressBar) findViewById(R.id.progressBar);
                                    spinner.setVisibility(View.GONE);
                                    doneButton.setVisibility(View.VISIBLE);
                                    scanButton.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                );
            }
            //Scan confirmation has been completed, check for any hub events regarding mutations
            else {
                Log.i("S360","Scan Hist Flag True");
                infoHeader.setText("SCANNER INFORMATION");
                spinner = (ProgressBar) findViewById(R.id.progressBar);
                spinner.setVisibility(View.GONE);
                doneButton.setVisibility(View.VISIBLE);
                scanButton.setVisibility(View.VISIBLE);

            }
        }
        // If no network connection then just display the buttons for offline operations
        else {
            Log.i("S360","No Network Connection");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    infoHeader.setText("SCANNER INFORMATION");
                    spinner = (ProgressBar) findViewById(R.id.progressBar);
                    spinner.setVisibility(View.GONE);
                    doneButton.setVisibility(View.VISIBLE);
                    scanButton.setVisibility(View.VISIBLE);
                }
            });
        }

        if (!scanHistFlag){
            //Start the default countdown for 60 seconds, if there are any failures allow offline operations upon app startup
            scannerSetTime = (new Double(60000)).longValue();
            new CountDownTimer(scannerSetTime, 1000) {
                public void onTick(long millisUntilFinished) {
                }
                public void onFinish() {
                    Log.i("S360","Default Timer Executed");
                    infoHeader.setText("SCANNER INFORMATION");
                    spinner = (ProgressBar) findViewById(R.id.progressBar);
                    spinner.setVisibility(View.GONE);
                    doneButton.setVisibility(View.VISIBLE);
                    scanButton.setVisibility(View.VISIBLE);
                }
            }.start();
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Boolean donePressedFlag = getIntent().getBooleanExtra("donePressedFlag",false);
        Boolean scanCheckFlag = getIntent().getBooleanExtra("scanCheckFlag",false);
        try {
            Log.i("S360Screen","ScanDestroy");
            Intent i = new Intent();
            i.setAction("za.smartee.threeSixty");
            if (scanCheckFlag){
                i.putExtra("data","s360success");
                Log.i("Msg","Intent success");
            } else {
                i.putExtra("data","s360failure");
                Log.i("Msg","Intent failure");
            }

            sendBroadcast(i);
            Log.i("Msg","Intent Sent");
            finishAffinity();
            ScanActivity.this.finish();
            System.exit(0);
        } catch (ActivityNotFoundException e){
            Log.i("Msg","App Not Found");
        }

        finishAffinity();
        ScanActivity.this.finish();
    }

    public void onDonePressed() {
        Log.i("S360Scan","Done Pressed Function");
        Boolean scanCheckFlag = getIntent().getBooleanExtra("scanHistFlag",false);
        final Integer[] scanCount = {getIntent().getIntExtra("scanCount", 1)};
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Is the Smartee Scan Complete?");
        alertDialogBuilder
                .setMessage("Click Yes to Confirm and Exit!")
                .setCancelable(false)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent d = new Intent(ScanActivity.this, AuthActivity.class);
                                if (scanCheckFlag){
                                    d.putExtra("scanCheckFlag", true);
                                } else {
                                    d.putExtra("scanCheckFlag", false);
                                }
                                d.putExtra("donePressedFlag",true);
                                Amplify.DataStore.query(PendingMutation.PersistentRecord.class,
                                        results -> {
                                            Log.i("S360","Results");
                                            if (!results.hasNext()) {
                                                startActivity(d);
                                                Log.i("S360Scan","Start Activity Sent - No Pending Mutations");
                                            } else {
                                                if (!processingFlag){
                                                    Log.i("S360","Processing Mutations");
                                                    Log.i("S360", "Scan Count: " + String.valueOf(scanCount[0]));
                                                    // The queue has outstanding records waiting to be processed.
                                                    if (scanCount[0] > 500) {
                                                        scanCount[0] = 380;
                                                    }
                                                    waitSetTime = (new Double(1500)).longValue()* scanCount[0];
                                                    Log.i("S360WaitTime", String.valueOf(waitSetTime));
                                                    runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            new CountDownTimer(waitSetTime, 1000) {
                                                                public void onTick(long millisUntilFinished) {
                                                                    spinner = (ProgressBar) findViewById(R.id.progressBar);
                                                                    spinner.setVisibility(View.VISIBLE);
                                                                    doneButton.setVisibility(View.INVISIBLE);
                                                                    scanButton.setVisibility(View.INVISIBLE);
                                                                    infoHeader.setText("Processing Data - Approx " + waitSetTime/1000 + " secs");
                                                                    processingFlag = true;
                                                                }
                                                                public void onFinish() {
                                                                    infoHeader.setText("SCANNER INFORMATION");
                                                                    spinner = (ProgressBar) findViewById(R.id.progressBar);
                                                                    spinner.setVisibility(View.GONE);
                                                                    startActivity(d);
                                                                    processingFlag = false;
                                                                }
                                                            }.start();
                                                        }
                                                    });
                                                }
                                            }
                                        }, failure -> {
                                            Log.i("S360","Failure");
                                            startActivity(d);
                                            Log.i("S360Scan","Check failure");
                                        }
                                );


                                //ScanActivity.this.finish();
                            }
                        })

                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
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
            Amplify.DataStore.start(
                    () -> Log.i("S360", "DataStore started"),
                    error -> Log.e("S360", "Error starting DataStore", error)
            );
            Amplify.DataStore.observe(Assets.class,
                    cancelable -> Log.i("S360", "Observation began."),
                    postChanged -> {
                        Assets post = postChanged.item();
                        //Log.i("MyAmplifyApp", "Post: " + post);
                    },
                    failure -> Log.e("S360", "Observation failed.", failure),
                    () -> Log.i("S360", "Observation complete.")
            );

            Amplify.DataStore.observe(Locations.class,
                    cancelable -> Log.i("S360", "Observation began."),
                    locPostChanged -> {
                        Locations post2 = locPostChanged.item();
                        //Log.i("MyAmplifyApp", "Post2: " + post2);
                    },
                    failure -> Log.e("S360", "Observation failed.", failure),
                    () -> Log.i("S360", "Observation complete.")
            );
        }

        @Override
        public void onLost(@NonNull Network network) {
            super.onLost(network);
        }

        @Override
        public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities);
            final boolean unmetered = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED);
        }
    };
}