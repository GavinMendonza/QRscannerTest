package com.example.qrscannertest;
//v1: built the scanner
//v1.2: adding login and DB writing
//v1.3: added a score entry and update db
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity
{

    TextView name;
    Button logoutBtn;

    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    DatabaseReference myRef;
    FirebaseDatabase database;
    HashMap dataSet;

    int points;
    EditText edtxtPoints;

    Button btn_scan;
    char ch;
    int cnt;
    String scannedData;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtxtPoints=findViewById(R.id.editTextForPoints);
        logoutBtn=findViewById(R.id.button2);
        name=findViewById(R.id.textView);
        gso=new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        gsc= GoogleSignIn.getClient(this,gso);
        GoogleSignInAccount account=GoogleSignIn.getLastSignedInAccount(this);


//        start working on sxc check here
        database = FirebaseDatabase.getInstance("https://eccloginmoduletest-default-rtdb.asia-southeast1.firebasedatabase.app/");
        myRef = database.getReference("users");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                HashMap value = (HashMap) snapshot.getValue();

//                inefficient method, change this later
                int flag=0;
                for(Object s: value.keySet()){
                    HashMap switchMap = (HashMap) value.get(s);
                    if(switchMap.containsValue(account.getEmail())){
                        if ((Long)switchMap.get("volunteer") == 1){
                            name.setText("Welcome "+switchMap.get("name"));
                            flag=1;
                        }
                    }
                }
                if (flag==0){
                    Toast.makeText(getApplicationContext(), "not a valid sxc acc", Toast.LENGTH_SHORT).show();
                    logOut();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w("database error", "Failed to read value.", error.toException());
            }
        });
//        firebase assistant code for reading ends here

//        end working on sxc check here


        logoutBtn.setOnClickListener(view -> {
            logOut();
        });


        btn_scan =findViewById(R.id.btn_scan);
        btn_scan.setOnClickListener(v->
        {
            scanCode();
        });
    }

    private void logOut() {
        gsc.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                finish();
                startActivity(new Intent(getApplicationContext(),login.class));
            }
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

            String[] arrOfStr = scannedData.split("#", 0);
            if(cnt==2) {
//                for (String a : arrOfStr)    //debug
//                    Log.d("data ", "data is: "+a);
                scannedData = "Name: " + arrOfStr[0] + "\n" + "UID: " + arrOfStr[1] + "\n" + "Class: " + arrOfStr[2];
            }else{
                scannedData="This is not an official SXC ECC QRcode... Please try again or contact the admin";
            }

//            builder.setMessage(result.getContents());
//            builder.setMessage("this is the msg");
            builder.setMessage(scannedData);

            builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());


            builder.setPositiveButton("Submit", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
//                    dialogInterface.dismiss();
                    updateDB(arrOfStr[1], String.valueOf(edtxtPoints.getText()));
                    dialogInterface.dismiss();

                }
            }).show();
        }
    });

    private void updateDB(String uid, String points) {
        // Read from the database


        myRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    Log.d("firebase", String.valueOf(task.getResult().getValue()));
                    dataSet = (HashMap) task.getResult().getValue();
                    Log.d("firebase", "onComplete: "+((HashMap)dataSet.get(uid)).get("score"));

                    myRef.child(uid).child("score").setValue(Integer.parseInt(String.valueOf(((HashMap)dataSet.get(uid)).get("score")))+Integer.parseInt(points));
                }
            }
        });

        Log.d("firebase", "updateDB: "+dataSet);
//        HashMap dataSet2= dataSet.get("205009");
//        dataSet2.get("score");
//        myRef.child(uid).child("score").setValue(dataSet2.get("score")+points);
    }

}