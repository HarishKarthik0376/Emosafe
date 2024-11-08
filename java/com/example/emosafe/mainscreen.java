package com.example.emosafe;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emosafe.adapter.recycleradapter;
import com.example.emosafe.models.patients;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Objects;

public class mainscreen extends AppCompatActivity implements recycleradapter.OnPatientClickListener {
    VideoView view, nofile;
    ArrayList<patients> arrayList = new ArrayList<patients>();
    SharedPreferences sharedPreferences,sp;
    SharedPreferences.Editor edit,esp;
    RecyclerView recyclerView;
    FirebaseAuth mauth;
    TextView textView,patientname;
    FirebaseDatabase db;
    String ud;
    ConstraintLayout constraintLayout;
    ConstraintLayout loadingLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mainscreen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainsa), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        textView = findViewById(R.id.textView);
        patientname = findViewById(R.id.patienname);
        constraintLayout = findViewById(R.id.constrainttobehidden);
        sp  = getSharedPreferences("data",MODE_PRIVATE);
        esp = sp.edit();
        String rec = sp.getString("recentname","NO DATA");
        String ress = sp.getString("res","NO DATA");
        textView.setText(ress.toUpperCase());
        patientname.setText("Name: "+rec);
        view = findViewById(R.id.videofile);
        nofile = findViewById(R.id.notfile);
        view.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.cubeeee);
        nofile.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.newnotfound);
        view.start();
        nofile.start();
        loadingLayout = findViewById(R.id.loading);
        mauth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        view.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                view.start();
            }
        });
        nofile.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                view.start();
            }
        });
        view.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                view.seekTo(0);
                view.start();
            }
        });
        nofile.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                nofile.seekTo(0);
                nofile.start();
            }
        });
        sharedPreferences = getSharedPreferences("patientdetails", MODE_PRIVATE);
        edit = sharedPreferences.edit();
        recyclerView = findViewById(R.id.mainrecycler);
        loadDatanew();
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), LinearLayoutManager.VERTICAL);
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.dividerline));
        recycleradapter recyleradapter = new recycleradapter(arrayList, mainscreen.this,this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mainscreen.this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(recyleradapter);
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyleradapter.notifyDataSetChanged();
        MaterialButton addpatient = findViewById(R.id.addemotion);
        addpatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog another = new Dialog(mainscreen.this);
                another.setContentView(R.layout.dialogtoadddetials);
                another.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                another.getWindow().setGravity(Gravity.BOTTOM);
                another.show();
                EditText name, bloodgroup, age, email;
                name = another.findViewById(R.id.name);
                age = another.findViewById(R.id.age);
                email = another.findViewById(R.id.emailid);
                bloodgroup = another.findViewById(R.id.bloodgroup);
                MaterialButton addpatientbtn = another.findViewById(R.id.addpatient);
                ImageView closepatientdialog = another.findViewById(R.id.closepatientdialog);
                closepatientdialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        another.dismiss();
                    }
                });

                addpatientbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        patients patients = new patients(name.getText().toString(), bloodgroup.getText().toString(), age.getText().toString(), email.getText().toString());
                        arrayList.add(patients);
                        Gson gson = new Gson();
                        String value = gson.toJson(arrayList);
                        edit.putString("arrayvalue", value);
                        edit.apply();
                        recyleradapter.notifyDataSetChanged();
                        another.dismiss();
                        recyclerView.addItemDecoration(dividerItemDecoration);
                        recyclerView.setAdapter(recyleradapter);
                        mauth.createUserWithEmailAndPassword(email.getText().toString(), name.getText().toString() + age.getText().toString())
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        db.getReference().child("users").child(mauth.getUid()).setValue(patients);
                                    }
                                });
                        if (arrayList != null) {
                            constraintLayout.setVisibility(View.INVISIBLE);
                        }
                    }
                });

            }
        });

    }

    private void loadDatanew() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString("arrayvalue", null);
        Type type = new TypeToken<ArrayList<patients>>() {
        }.getType();
        arrayList = gson.fromJson(json, type);
        if (arrayList == null) {
            arrayList = new ArrayList<>();
            constraintLayout.setVisibility(View.VISIBLE);
        } else {
            constraintLayout.setVisibility(View.INVISIBLE);
        }


    }

    public void onPatientClick(String uid) {
        loadingLayout.setVisibility(View.VISIBLE);
        view.pause();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadingLayout.setVisibility(View.INVISIBLE);
                view.start();
                Intent redirect = new Intent(mainscreen.this,personalscreen.class);
//                redirect.putExtra("uid",uid);
                esp.putString("uid",uid);
                esp.apply();
                startActivity(redirect);
            }
        },5000);

    }
}