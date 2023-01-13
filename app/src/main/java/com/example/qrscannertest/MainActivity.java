package com.example.qrscannertest;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class MainActivity extends AppCompatActivity
{
    Button btn_scan;
    char ch;
    int cnt;
    String scannedData;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_scan =findViewById(R.id.btn_scan);
        btn_scan.setOnClickListener(v->
        {
            scanCode();
        });
    }

    private void scanCode()
    {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLaucher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> barLaucher = registerForActivityResult(new ScanContract(), result->
    {
        if(result.getContents() !=null)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Result");

//            reading and converting the string
            scannedData=result.getContents();

//            counting the number of times # appears to make sure data is valid
            ch = '#';
            cnt = 0;
            for ( int i = 0; i < scannedData.length(); i++) {
                if (scannedData.charAt(i) == ch)
                    cnt++;
            }
            if(cnt==2) {
                String[] arrOfStr = scannedData.split("#", 0);
//                for (String a : arrOfStr)    //debug
//                    Log.d("data ", "data is: "+a);
                scannedData = "Name: " + arrOfStr[0] + "\n" + "UID: " + arrOfStr[1] + "\n" + "Class: " + arrOfStr[2];
            }else{
                scannedData="This is not an official SXC ECC QRcode... Please try again or contact the admin";
            }

//            builder.setMessage(result.getContents());
//            builder.setMessage("this is the msg");
            builder.setMessage(scannedData);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    dialogInterface.dismiss();
                }
            }).show();
        }
    });

}