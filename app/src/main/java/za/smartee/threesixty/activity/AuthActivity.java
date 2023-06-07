package za.smartee.threesixty.activity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.SignInUIOptions;

import java.util.ArrayList;

import za.smartee.threesixty.BuildConfig;
import za.smartee.threesixty.R;

public class AuthActivity extends BaseActivity {
    TextView tv1;
    TextView tv2;
    TextView tv3;
    EditText tve1;
    private ProgressBar spinner;
    Long scannerSetTime;
    public static final int REQUEST_ENABLE_BT = 4001;
    Button loginButton;
    Spinner userDD;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Log.i("S360Screen","AuthCreate");
        Intent intent = getIntent();
        TextView vCode = (TextView) findViewById(R.id.vCode);
        vCode.setText(BuildConfig.VERSION_NAME);
//        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//        wifiManager.setWifiEnabled(true);
        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        Boolean donePressedFlag = getIntent().getBooleanExtra("donePressedFlag",false);
        Boolean scanCheckFlag = getIntent().getBooleanExtra("scanCheckFlag",false);
        tve1 = (EditText) findViewById(R.id.userCheck);
        tv1 = (TextView) findViewById(R.id.textView2);
        tv2 = (TextView) findViewById(R.id.textView3);
        tv3 = (TextView) findViewById(R.id.textView4);
        tve1.setVisibility(View.INVISIBLE);
        tv1.setVisibility(View.INVISIBLE);
        tv2.setVisibility(View.INVISIBLE);
        tv3.setVisibility(View.INVISIBLE);
        spinner = (ProgressBar) findViewById(R.id.progressBar);
        spinner.setVisibility(View.GONE);



        //Array List to store the users info for manual verification and Login button
        ArrayList<String> userList = new ArrayList<String>();
        Resources res = getResources();
        String[] userArray = res.getStringArray(R.array.user_list);
        userList.clear();
        int userLen=userArray.length;
        for (int r = 0; r < userLen; r++) {
            userList.add(userArray[r]);
                   }
        Spinner userDD = (Spinner) findViewById(R.id.userSpinner);
        ArrayAdapter<String> UserDDAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, userList);
        UserDDAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        UserDDAdapter.notifyDataSetChanged();
        userDD.setAdapter(UserDDAdapter);
        Button loginButton = (Button) findViewById(R.id.signInButton);

        loginButton.setVisibility(View.INVISIBLE);
        userDD.setVisibility(View.INVISIBLE);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userText = userDD.getSelectedItem().toString();
                verifyuser(userText);
                //showSignIn();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Button loginButton = (Button) findViewById(R.id.signInButton);
        Spinner userDD = (Spinner) findViewById(R.id.userSpinner);
        Log.i("S360Screen","Auth Resume");
        Boolean donePressedFlag = getIntent().getBooleanExtra("donePressedFlag",false);
        Boolean scanCheckFlag = getIntent().getBooleanExtra("scanCheckFlag",false);
        if (donePressedFlag) {
            Log.i("S360","Done Pressed " + donePressedFlag);
            try {
                String appUser = getIntent().getStringExtra("appUser");
                Intent iStart = new Intent(AuthActivity.this, GuideActivity.class);
                iStart.putExtra("appUser",appUser);
                startActivity(iStart);
                AuthActivity.this.finish();
            } catch (ActivityNotFoundException e){
                Log.i("Msg","App Not Found");
            }


        } else {
            Log.i("S360","VSC Login");
            // ATTENTION: This was auto-generated to handle app links.
            Intent appLinkIntent = getIntent();
            String appLinkAction = appLinkIntent.getAction();
            Uri appLinkData = appLinkIntent.getData();
            //Determine if the app was started from VSc or manually
            Log.i("S360","App Link Data " + appLinkData);
            if (appLinkData == null){
                Log.i("S360","Applink Data null");
                tve1.setVisibility(View.VISIBLE);
                tv1.setVisibility(View.VISIBLE);
                tv2.setVisibility(View.VISIBLE);
                tv3.setVisibility(View.VISIBLE);
                loginButton.setVisibility(View.VISIBLE);
                userDD.setVisibility(View.VISIBLE);
            } else {
                Log.i("S360","Applink Data NOT null");
                Intent iStart = new Intent(AuthActivity.this, GuideActivity.class);
                iStart.putExtra("appUser",appLinkData.toString());
                startActivity(iStart);
                AuthActivity.this.finish();
            }
        }
    }

    private void verifyuser(String text){
        Log.i("S360", "Verify User");
        Intent iStart = new Intent(AuthActivity.this, GuideActivity.class);
        TextView textCheck = (EditText) findViewById(R.id.userCheck);
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
        dlgAlert.setMessage("Please retry login details.");
        dlgAlert.setTitle("Incorrect Login Details");
        dlgAlert.setPositiveButton("OK", null);
        dlgAlert.setCancelable(true);
        dlgAlert.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent rs = new Intent(AuthActivity.this, AuthActivity.class);
                        startActivity(rs);
                    }
                });
        switch (text) {
            case "Spar Admin - NR":
                if (textCheck.getText().toString().equals(getResources().getString(R.string.sparAdminCheck))){
                    iStart.putExtra("appUser","Spar Admin - NR");
                    startActivity(iStart);
                } else {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            dlgAlert.create().show();
                        }
                    });
                }
                break;
            case "Spar Driver":
                if (textCheck.getText().toString().equals(getResources().getString(R.string.sparDriverCheck))){
                    iStart.putExtra("appUser","Spar Driver");
                    startActivity(iStart);
                } else {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            dlgAlert.create().show();
                        }
                    });
                }
                break;
            case "Smartee Admin - NR":
                if (textCheck.getText().toString().equals(getResources().getString(R.string.smarteeAdminCheck))){
                    iStart.putExtra("appUser","Smartee Admin - NR");
                    startActivity(iStart);
                } else {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            dlgAlert.create().show();
                        }
                    });
                }
                break;
            case "Other":
                if (textCheck.getText().toString().equals(getResources().getString(R.string.otherCheck))){
                    iStart.putExtra("appUser","Other");
                    startActivity(iStart);
                } else {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            dlgAlert.create().show();
                        }
                    });
                }
                break;
            case "Reverse Logistics - NR":
                if (textCheck.getText().toString().equals(getResources().getString(R.string.reverseLogisticsCheck))){
                    iStart.putExtra("appUser","Reverse Logistics - NR");
                    startActivity(iStart);
                } else {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            dlgAlert.create().show();
                        }
                    });
                }
                break;
            case "Assets - NR":
                if (textCheck.getText().toString().equals(getResources().getString(R.string.assetsCheck))){
                    iStart.putExtra("appUser","Assets - NR");
                    startActivity(iStart);
                } else {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            dlgAlert.create().show();
                        }
                    });
                }
                break;
        }
    }

    private void showSignIn() {
        try {
            AWSMobileClient.getInstance().showSignIn(this,
                    SignInUIOptions.builder().nextActivity(GuideActivity.class).build());
        } catch (Exception e) {
            Log.e("TAG", e.toString());
        }
    }
}