package za.smartee.threesixty.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.DataStoreChannelEventName;
import com.amplifyframework.datastore.generated.model.Assets;
import com.amplifyframework.datastore.generated.model.Locations;
import com.amplifyframework.datastore.syncengine.PendingMutation;
import com.amplifyframework.hub.HubChannel;
import com.github.javiersantos.appupdater.AppUpdaterUtils;
import com.github.javiersantos.appupdater.enums.AppUpdaterError;
import com.github.javiersantos.appupdater.enums.UpdateFrom;
import com.github.javiersantos.appupdater.objects.Update;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import za.smartee.threesixty.BuildConfig;
import za.smartee.threesixty.R;

public class ScanActivity extends BaseActivity{
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
    LocationManager locationManager;
    LocationListener locationListener;
    LocationListener networkLocationListener;
    LocationListener gpsLocationListener;
    //Setup the device GPS variables
    final double[] devLat = new double[1];
    final double[] devLng = new double[1];
    Boolean locationFoundFlag = false;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Log.i("S360Screen","ScanCreate");
        //Get the app user who initiatied the start of teh app
        String appUser = getIntent().getStringExtra("appUser");
        String appStore = getIntent().getStringExtra("appStore");
        String appStoreCode = getIntent().getStringExtra("appStoreCode");
        String loginType = getIntent().getStringExtra("loginType");

        //Setup a subscription which checks for changes in network connection
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build();

        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(ConnectivityManager.class);
        connectivityManager.requestNetwork(networkRequest, networkCallback);
        getLocation();

        AppUpdaterUtils appUpdaterUtils = new AppUpdaterUtils(this)
                .setUpdateFrom(UpdateFrom.JSON)
                .setUpdateJSON("https://s360rellog.s3.amazonaws.com/update-changelog-test.json")
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
                    i.putExtra("appStore",appStore);
                    i.putExtra("appStoreCode",appStoreCode);
                    i.putExtra("loadingFlag", loadingChecked);
                    i.putExtra("loginType", loginType);
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
                        Intent i = new Intent(ScanActivity.this, AdminActivity.class);
                        i.putExtra("appUser",appUser);
                        startActivity(i);

//                AWSMobileClient.getInstance().signOut(SignOutOptions.builder().signOutGlobally(true).build(), new Callback<Void>() {
//                    @Override
//                    public void onResult(final Void result) {
//                        Log.d("Signout Msg", "signed-out");
//                        Intent i = new Intent(ScanActivity.this, AuthActivity.class);
//                        i.putExtra("appUser",appUser);
//                        startActivity(i);
//                    }
//
//                    @Override
//                    public void onError(Exception e) {
//                        Log.e("Signout Msg", "sign-out error", e);
//                        Intent i = new Intent(ScanActivity.this, AuthActivity.class);
//                        startActivity(i);
//                    }
//                });

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



    class DownloadFileFromURL extends AsyncTask<String, String, String> {
        ProgressDialog pd;
        String pathFolder = "";
        String pathFile = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(ScanActivity.this);
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
                Uri apkUri = FileProvider.getUriForFile(ScanActivity.this, "za.smartee.threeSixty", toInstall);
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
            ScanActivity.this.startActivity(intent);
        }

        }

    private void getLocation(){
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Boolean hasGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        Boolean hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        final Location[] locationByGps = new Location[1];
        final Location[] locationByNetwork = new Location[1];

        gpsLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                locationByGps[0] = location;
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
                Log.i("location", "provider");
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                Log.i("location", "provider disabled");
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.i("location", "status changed");
            }
        };
        networkLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                locationByNetwork[0] = location;
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
                Log.i("location", "provider");
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                Log.i("location", "provider disabled");
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.i("location", "status changed");
            }
        };



        if (hasGPS) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(ScanActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0F, gpsLocationListener);
        }
        if (hasNetwork) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(ScanActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0F, networkLocationListener);
        }
        Location lastKnownLocationByGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location lastKnownLocationByNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (lastKnownLocationByGPS != null && lastKnownLocationByNetwork != null){
            if (lastKnownLocationByGPS.getAccuracy() < lastKnownLocationByNetwork.getAccuracy()){
                devLat[0] = lastKnownLocationByGPS.getLatitude();
                devLng[0] = lastKnownLocationByGPS.getLongitude();
                locationFoundFlag = true;
            }
        }
        else if (lastKnownLocationByNetwork != null) {
            devLat[0] = lastKnownLocationByNetwork.getLatitude();
            devLng[0] = lastKnownLocationByNetwork.getLongitude();
            locationFoundFlag=true;

        }
        if (!locationFoundFlag) {
            devLat[0] = 0;
            devLng[0] = 0;
        }
    }
    }