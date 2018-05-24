package com.helix.smartbluelock;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

import static com.helix.smartbluelock.MainActivity.MyPass;
import static com.helix.smartbluelock.SplashActivity.MyCode;
import static com.helix.smartbluelock.SplashActivity.MyPIN;

public class Registration extends AppCompatActivity {
    int lockid,lockPin,confirmLockPin;
    String pass =" ",confirmPass =" ",code,bahar;
    boolean result=false;

    Button register;
    RadioButton pRadio,sRadio;
    EditText time;
    TextView load;

    private static final String TAG = "Firestore";
    public static final String My_Id = "LOGIN" ;

    SharedPreferences sharedPreferences,shared_preferences,shared,prefCode;
    private FirebaseFirestore mDocRef = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        pRadio=findViewById(R.id.radioprimary);
        sRadio=findViewById(R.id.radiosecondary);
        register=findViewById(R.id.buttonRegister);
        register.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view){

                EditText editText =findViewById(R.id.lockid);

                String slockid=editText.getText().toString();

                editText=findViewById(R.id.password);
                pass=editText.getText().toString();

                editText=findViewById(R.id.confirmpassword);
                confirmPass=editText.getText().toString();

                editText=findViewById(R.id.lockpin);
                String slockPin=editText.getText().toString();

                editText=findViewById(R.id.confirmLockPin);
                String sconfirmLockPin=editText.getText().toString();

                editText=findViewById(R.id.code);
                code=editText.getText().toString();

                load=findViewById(R.id.load);
                time=findViewById(R.id.time);

              //  register.setVisibility(INVISIBLE);
                load.setVisibility(View.VISIBLE);
                time.setVisibility(View.VISIBLE);

                new CountDownTimer(4000, 1000) {

                    public void onTick(long millisUntilFinished) {
                        time.setText("" + millisUntilFinished / 1000);
                    }

                    public void onFinish() {
                    }
                }.start();

                if((pRadio.isChecked()&&pass.isEmpty())||(sRadio.isChecked()&&code.isEmpty())||slockPin.isEmpty()||sconfirmLockPin.isEmpty())
//                if(slockid.isEmpty()||pass.isEmpty()||slockPin.isEmpty()||sconfirmLockPin.isEmpty())
                    Toast.makeText(Registration.this, "Please Fill All Fields", Toast.LENGTH_SHORT).show();
                else{

                    lockid=Integer.parseInt(slockid);lockPin=Integer.parseInt(slockPin);confirmLockPin=Integer.parseInt(sconfirmLockPin);

                    if (validate()) {

                        if (!pass.equals(confirmPass)&&pRadio.isChecked()) {
                            Toast.makeText(Registration.this, "Passwords Do Not Match", Toast.LENGTH_SHORT).show();
                        } else if (lockPin != confirmLockPin) {
                            Toast.makeText(Registration.this, "Pin Do Not Match", Toast.LENGTH_SHORT).show();

                        }
                       else if (!haveNetworkConnection()) {
                            Toast.makeText(Registration.this, "Please Connect Internet First :)", Toast.LENGTH_SHORT).show();
                        }
                        else if (sRadio.isChecked()){
                                forSecondary();
                                setLoading();
                            new Handler().postDelayed(new Runnable() {

                                @Override
                                public void run() {
                                    if(result){
                                        sharedPreferences = getSharedPreferences(MyPIN, MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putInt("lockpin", lockPin);
                                        editor.apply();
                                        shared_preferences = getSharedPreferences(My_Id, MODE_PRIVATE);
                                        SharedPreferences.Editor editor1 = shared_preferences.edit();
                                        editor1.putInt("lockid", lockid);
                                        editor1.apply();
                                        shared=getSharedPreferences(MyPass,MODE_PRIVATE);
                                        SharedPreferences.Editor editor2 = shared.edit();
                                        editor2.putString("lockpass",bahar);
                                        editor2.apply();
                                        prefCode=getSharedPreferences(MyCode,MODE_PRIVATE);
                                        mDocRef.collection("checkuser").document(String.valueOf(lockid))
                                                .delete()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        SharedPreferences.Editor e=prefCode.edit();
                                                        e.putString("code",null);
                                                        e.apply();
                                                             }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(Registration.this, "Something Went Wrong!!", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                       finish();

                                    }

                                }

                            }, 5000);


                        }
                        else {

                            sharedPreferences = getSharedPreferences(MyPIN, MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putInt("lockpin", lockPin);
                            editor.apply();
                            shared_preferences = getSharedPreferences(My_Id, MODE_PRIVATE);
                            SharedPreferences.Editor editor1 = shared_preferences.edit();
                            editor1.putInt("lockid", lockid);
                            editor1.apply();
                            forPrimary();
                            Intent startAct2 = new Intent(Registration.this, SplashActivity.class);
                            startActivity(startAct2);

                        }
                    }
               }
                register.setVisibility(View.VISIBLE);
                load.setVisibility(View.INVISIBLE);
                time.setVisibility(View.INVISIBLE);
        }

            });
}

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radioprimary:
                if (checked) {
                    EditText epass = findViewById(R.id.password);
                    epass.setVisibility(View.VISIBLE);
                    EditText econfirmpass = findViewById(R.id.confirmpassword);
                    econfirmpass.setVisibility(View.VISIBLE);
                    EditText ecode=findViewById(R.id.code);
                    ecode.setVisibility(View.INVISIBLE);
                }
                    break;
            case R.id.radiosecondary:
                if (checked) {
                    EditText epass = findViewById(R.id.password);
                    epass.setVisibility(View.INVISIBLE);
                    EditText econfirmpass = findViewById(R.id.confirmpassword);
                    econfirmpass.setVisibility(View.INVISIBLE);
                    EditText ecode=findViewById(R.id.code);
                    ecode.setVisibility(View.VISIBLE);
                }
                    break;

        }
    }
    boolean validate(){

            if (pass.length() < 8&& pRadio.isChecked()) {
                Toast.makeText(this, "Password Is Too Short", Toast.LENGTH_SHORT).show();
                return false;
            } else if (notHaveChar() && pRadio.isChecked()) {
                Toast.makeText(this, "Password Should Contain Character", Toast.LENGTH_SHORT).show();
                return false;
            }

            if(lockPin<1000){
                Toast.makeText(this, "Pin Should Not Start with 0", Toast.LENGTH_SHORT).show();
                return false; }
            return true;
    }
    boolean notHaveChar(){
        for(int i=0;i<pass.length();i++){
            if('a'<=pass.charAt(i)&&pass.charAt(i)<='z'||'A'<=pass.charAt(i)&&pass.charAt(i)<='Z')return false;
        }
        return true;
    }
    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;



        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }
    void forPrimary(){

        Map<String, Object> primary = new HashMap<>();
        //primary.put("lock_pass", lockPin);
        primary.put("password", pass);

        mDocRef.collection("primaryuser").document(String.valueOf(lockid)).set(primary, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "success");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "fail");
            }
        });
    }

    void forSecondary(){

        getData("checkuser");

        getData("primaryuser");


    }
boolean matchCode(String data) {
    String temp = "";
    if (data.contains("code")) {
        for (int i = 6; data.charAt(i) != '}'; i++) temp = temp + data.charAt(i);

        if (temp.equals(code)) {
            Toast.makeText(this, "Code Validated", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            Toast.makeText(this, "Code Is Invalid", Toast.LENGTH_SHORT).show();
            return false;
        }
    } else if (data.contains("lock_pass")) {
            for (int i = 30; data.charAt(i) != '}'; i++) temp = temp + data.charAt(i);
            bahar=temp;
    }
    return true;
}

void getData(String doc){
    DocumentReference mDocRef1=mDocRef.collection(doc).document(String.valueOf(lockid));
    mDocRef1.get()
            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                //boolean result;

                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            String data=""+document.getData();
                           if(matchCode(data))
                            result=true;
                        } else {
                            Log.d(TAG, "No such document");
                            Toast.makeText(Registration.this,"Permission Not Granted/LockId Invalid ", Toast.LENGTH_LONG).show();
                           result=false;
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                        Toast.makeText(Registration.this,"Your Phone Is Offline ", Toast.LENGTH_LONG).show();
                        result=false;
                    }
                }

            });


}

    void setLoading(){
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("Registering....");
        progress.setMessage("Please Wait While We Register Your Device...");
        progress.show();

        Runnable progressRunnable = new Runnable() {

            @Override
            public void run() {
                progress.cancel();
            }
        };

        Handler pdCanceller = new Handler();
        pdCanceller.postDelayed(progressRunnable, 5000);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        finish();
        super.onDestroy();

    }
}