package com.helix.smartbluelock;



import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.felipecsl.gifimageview.library.GifImageView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static android.view.View.INVISIBLE;
import static com.helix.smartbluelock.MainActivity.MyPass;
import static com.helix.smartbluelock.Registration.My_Id;

public class SplashActivity extends AppCompatActivity {

    Animation FabOpen, Fabclose,Rotateclockwise,Rotateanticlockwise;
    boolean isOpen =false,result,flag=false;
    String password,lock_pass;
    public static final String MyPIN = "MyPrefs",MyCode="mycode";
    private static final String TAG = "Firestore";

    EditText ET,timer,dlogin,dpass;
    TextView t1,t2,loading,dtextview;
    Button newUser,existingUser,send,dsend;
    ImageButton submitButton;
    FloatingActionButton menu,reset,changePin;

    InputStream inputStream;
    GifImageView gifImageView;
    ProgressBar progressBar;

    SharedPreferences sharedId,prefsPass,prefsPin,prefsCode;


    private FirebaseFirestore mDocRef = FirebaseFirestore.getInstance();



    String jump(){

         prefsPin = getSharedPreferences(MyPIN, MODE_PRIVATE);
         int retPin=prefsPin.getInt("lockpin",0);
         return(String.valueOf(retPin));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        gifImageView=findViewById(R.id.splashGif);
        progressBar=findViewById(R.id.progBar);
        newUser=findViewById(R.id.newUser);
        submitButton = findViewById(R.id.submit);
        ET = findViewById(R.id.pin);
        send=(Button)findViewById(R.id.sendAuth);
        existingUser=(Button)findViewById(R.id.alreadyregis);
        menu=(FloatingActionButton)findViewById(R.id.menu);
        reset=(FloatingActionButton)findViewById(R.id.menuitem1);
        changePin=(FloatingActionButton)findViewById(R.id.menuitem2);
        FabOpen= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_open);
        Fabclose= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);
        Rotateclockwise= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_rotate_clockwise);
        Rotateanticlockwise= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_rotate_anticlockwise);
        t1=(TextView)findViewById(R.id.textReset);
        t2=(TextView)findViewById(R.id.textPin);

        prefsPin = getSharedPreferences(MyPIN, MODE_PRIVATE);
        final int lockPin =prefsPin.getInt("lockpin",0);

        prefsPass=getSharedPreferences(MyPass,MODE_PRIVATE);
        String lockPass = prefsPass.getString("lockpass",null);


        if(lockPin!=0 ){
            newUser.setVisibility(INVISIBLE);
            existingUser.setVisibility(INVISIBLE);
        if(lockPass != null)
            send.setVisibility(View.VISIBLE);
        }


        submitButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (lockPin == 0) {
                            Intent i = new Intent(SplashActivity.this, Registration.class);
                            startActivity(i);
                        }else{


                            String[] t={ET.getText().toString()};

                            if(t[0].equalsIgnoreCase(jump())){
                                Intent startAct =new Intent(SplashActivity.this, MainActivity.class);
                                startActivity(startAct);
                            }
                            else
                            Toast.makeText(SplashActivity.this,"Wrong Pin",Toast.LENGTH_SHORT).show();


                        }
                    }


                }  );
        newUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startAct=new Intent(SplashActivity.this,Registration.class);
                startActivity(startAct);


            }
        });

        existingUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(SplashActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.dialogfragment_existinguser, null);
                final EditText malockId = (EditText) mView.findViewById(R.id.mlockid);
                final EditText maPassword = (EditText) mView.findViewById(R.id.mPassword);
                final EditText maLockPin =(EditText)mView.findViewById(R.id.mPin);
                final EditText maConfirmLockPin =(EditText)mView.findViewById(R.id.mConfirmPin);
                Button maLogin = (Button) mView.findViewById(R.id.btnExistingUser);
                //final Registration obj=new Registration();
                mBuilder.setView(mView);
                final AlertDialog dialog = mBuilder.create();
                dialog.show();


                maLogin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        flag=true;
                        final String lockid=malockId.getText().toString();
                        password=maPassword.getText().toString();
                        final int mnLockpin = Integer.parseInt(maLockPin.getText().toString());
                        final int mnConfirmLockPin =Integer.parseInt(maConfirmLockPin.getText().toString());
                        if(mnConfirmLockPin !=mnLockpin)
                            Toast.makeText(SplashActivity.this, "Pin do not match", Toast.LENGTH_SHORT).show();

                        else if (!malockId.getText().toString().isEmpty() && !maPassword.getText().toString().isEmpty() && !maLockPin.getText().toString().isEmpty() && !maConfirmLockPin.getText().toString().isEmpty()) {
                            verifyPassword(lockid);
                            setLoading();
                            new Handler().postDelayed(new Runnable() {

                                @Override
                                public void run() {

                                    if(result){
                                        Toast.makeText(SplashActivity.this, "Verified", Toast.LENGTH_SHORT).show();
                                        prefsPass=getSharedPreferences(MyPass,MODE_PRIVATE);
                                        SharedPreferences.Editor editor2 = prefsPass.edit();
                                        editor2.putString("lockpass",lock_pass);
                                        editor2.apply();
                                        sharedId = getSharedPreferences(My_Id, MODE_PRIVATE);
                                        SharedPreferences.Editor editor1 = sharedId.edit();
                                        editor1.putInt("lockid",Integer.parseInt(lockid));
                                        editor1.apply();

                                        prefsPin = getSharedPreferences(MyPIN, MODE_PRIVATE);
                                        SharedPreferences.Editor editor = prefsPin.edit();
                                        editor.putInt("lockpin", mnLockpin);
                                        editor.apply();
                                        dialog.dismiss();
                                        result=false;
                                        Intent intent = getIntent();
                                        finish();
                                        startActivity(intent);

                                    }else{
                                        Toast.makeText(SplashActivity.this, "Wrong Password", Toast.LENGTH_SHORT).show();}
                                    dialog.dismiss();
                                }

                            }, 5000);

                        } else {
                            Toast.makeText(SplashActivity.this,
                                    "Please fill any empty field",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                flag=false;
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(SplashActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.dialogfragment_sendauth, null);

                dlogin=(EditText) mView.findViewById(R.id.elockid);
                dpass=(EditText)mView.findViewById(R.id.etPassword);
                dsend=(Button) mView.findViewById(R.id.btnSend);
                 loading=(TextView)mView.findViewById(R.id.loading);
                timer=(EditText)mView.findViewById(R.id.timer);
                dtextview=(TextView) mView.findViewById(R.id.textView);
                prefsCode=getSharedPreferences(MyCode,MODE_PRIVATE);
                mBuilder.setView(mView);
                final AlertDialog dialog = mBuilder.create();
                dialog.show();
                final String tempCode=getSaltString();
                dsend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (!dlogin.getText().toString().isEmpty() && !dpass.getText().toString().isEmpty()) {
                            verifyPassword(dlogin.getText().toString());

                            dlogin.setVisibility(INVISIBLE);
                            dpass.setVisibility(INVISIBLE);
                            dsend.setVisibility(INVISIBLE);
                            dtextview.setVisibility(INVISIBLE);
                            loading.setVisibility(View.VISIBLE);
                            timer.setVisibility(View.VISIBLE);
                            password=dpass.getText().toString();

                            new CountDownTimer(4000, 1000) {

                                public void onTick(long millisUntilFinished) {
                                    timer.setText("" + millisUntilFinished / 1000);
                                }

                                public void onFinish() {

                                }
                            }.start();
                            prefsCode=getSharedPreferences(MyCode,MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefsCode.edit();
                            editor.putString("code", tempCode);
                            editor.apply();
                            new Handler().postDelayed(new Runnable() {

                                @Override
                                public void run() {
                                    if(result){
                                       // Toast.makeText(SplashActivity.this, "3="+result+"4", Toast.LENGTH_SHORT).show();
                                        store(dlogin.getText().toString(),tempCode);
                                        dialog.dismiss();
                                        result=false;
                                      }

                                        dialog.dismiss();
                                    }

                            }, 3000);

                        } else {
                            Toast.makeText(SplashActivity.this,
                                    "Please fill any empty field",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });




        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isOpen){
                    changePin.startAnimation(Fabclose);
                    reset.startAnimation(Fabclose);
                    menu.startAnimation(Rotateanticlockwise);
                    changePin.setClickable(false);
                    reset.setClickable(false);
                    isOpen=false;
                    t1.setVisibility(INVISIBLE);
                    t2.setVisibility(INVISIBLE);
                }else{
                   t1.setVisibility(View.VISIBLE);
                   t2.setVisibility(View.VISIBLE);
                   changePin.startAnimation(FabOpen);
                   reset.startAnimation(FabOpen);
                   menu.startAnimation(Rotateclockwise);
                   changePin.setClickable(true);
                   reset.setClickable(true);
                   isOpen =true;
                }
            }
        });
        reset.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View view) {

     resetApp();

            }
        });
        changePin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(SplashActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.changepin, null);

                final EditText old =(EditText)mView.findViewById(R.id.oldpin);
                final EditText newPin =(EditText)mView.findViewById(R.id.changePin);
                final EditText newConfirmPin =(EditText)mView.findViewById(R.id.changeConfirmPin);
                Button setPin = (Button) mView.findViewById(R.id.setPin);
                mBuilder.setView(mView);
                final AlertDialog dialog = mBuilder.create();
                dialog.show();
                setPin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        prefsPin = getSharedPreferences(MyPIN,MODE_PRIVATE);
                        int tempPin = prefsPin.getInt("lockpin",0);
                        if(tempPin ==0){
                            Toast.makeText(SplashActivity.this, "Please Register Your App", Toast.LENGTH_SHORT).show();
                        }else if(tempPin == Integer.parseInt(old.getText().toString())){
                            if(Integer.parseInt(newPin.getText().toString()) != Integer.parseInt(newConfirmPin.getText().toString())){
                                Toast.makeText(SplashActivity.this, "Pins Do Not Match", Toast.LENGTH_SHORT).show();
                            }else{
                                prefsPin =getSharedPreferences(MyPIN,MODE_PRIVATE);
                                SharedPreferences.Editor e =prefsPin.edit();
                                e.putInt("lockpin",Integer.parseInt(newConfirmPin.getText().toString()));
                                e.apply();
                                dialog.dismiss();
                                Toast.makeText(SplashActivity.this, "Pin Changed Successfully", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else Toast.makeText(SplashActivity.this, "Wrong Pin", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        try{
            inputStream=getAssets().open("Gif.gif");
            byte[] bytes = IOUtils.toByteArray(inputStream);
            gifImageView.setBytes(bytes);
            gifImageView.startAnimation();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }





    void store(String zLockid,String temp){
        Map<String, Object> primary = new HashMap<>();
        primary.put("code", temp);
        mDocRef.collection("checkuser").document(String.valueOf(zLockid)).set(primary).addOnSuccessListener(new OnSuccessListener<Void>() {
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

   void verifyPassword(String id){
        getData("primaryuser",id);
    }


    boolean matchCode(String data) {
        String temp="";
        for (int i = 10; data.charAt(i) != ','; i++) temp = temp + data.charAt(i);
        if(temp.equals(password)){
           if(flag){
            temp="";
                for(int i=30;data.charAt(i) != '}'; i++) temp = temp + data.charAt(i);
                lock_pass=temp;
               // Toast.makeText(this, "sinha="+lock_pass, Toast.LENGTH_SHORT).show();
          }
            return true;
        }

        else{
            Toast.makeText(this, "Wrong Password", Toast.LENGTH_SHORT).show();
            return false;
        }

    }

    void getData(String doc,String lockid){

        DocumentReference mDocRef1=mDocRef.collection("primaryuser").document(lockid);
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
                                {
                                    result=true;

                                }
                                else{
                                    result=false;
                                }
                            } else {
                                Log.d(TAG, "No such document");
                                Toast.makeText(SplashActivity.this,"Permission Not Granted/LockId Invalid ", Toast.LENGTH_LONG).show();
                                result=false;
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                            Toast.makeText(SplashActivity.this,"Your Phone Is Offline", Toast.LENGTH_LONG).show();
                            result=false;
                        }
                    }

                });


    }
    protected String getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 8) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Really Exit?")
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        SplashActivity.super.onBackPressed();
                    }
                }).create().show();
    }
    void resetApp(){
        new AlertDialog.Builder(this)
                .setTitle("Reset App")
                .setMessage("Are you sure you want to reset app?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        prefsPass = getSharedPreferences(MyPIN, MODE_PRIVATE);
                        SharedPreferences.Editor e1 = prefsPass.edit();
                        e1.putString("lockpass",null);
                        e1.apply();
                        prefsPin = getSharedPreferences(MyPIN, MODE_PRIVATE);
                        SharedPreferences.Editor e2 = prefsPin.edit();
                        e2.putInt("lockpin", 0);
                        e2.apply();
                        prefsCode=getSharedPreferences(MyCode,MODE_PRIVATE);
                        SharedPreferences.Editor e3=prefsCode.edit();
                        e3.putString("code",null);
                        e3.apply();
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                        Toast.makeText(SplashActivity.this, "done", Toast.LENGTH_SHORT).show();
                    }
                }).create().show();

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

}

