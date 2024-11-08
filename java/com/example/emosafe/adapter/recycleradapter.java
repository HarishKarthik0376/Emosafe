package com.example.emosafe.adapter;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emosafe.R;
import com.example.emosafe.mainscreen;
import com.example.emosafe.models.patients;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class recycleradapter extends RecyclerView.Adapter<recycleradapter.ViewHolder> {
    Context context;
    FirebaseAuth mauth;
    FirebaseDatabase database;
    String uid;
    private final OnPatientClickListener listener;
    public recycleradapter(ArrayList<patients> arrayList, Context context,OnPatientClickListener listener) {
        this.arrayList = arrayList;
        this.context = context;
        this.listener = listener;
    }

    ArrayList<patients> arrayList;
    @NonNull
    @Override
    public recycleradapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recyclerlayout,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        mauth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull recycleradapter.ViewHolder holder,int position) {
        int adapterposition = holder.getAdapterPosition();
        patients patients = arrayList.get(adapterposition);
        holder.name.setText(patients.getName());
        holder.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                patients pts = arrayList.get(adapterposition);
                database.getReference().child("users").orderByChild("name").equalTo(pts.getName().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            uid = snapshot.getKey();
                        }
                        Toast.makeText(context, String.valueOf(uid), Toast.LENGTH_SHORT).show();
                        listener.onPatientClick(uid);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        MaterialButton btn;
        ConstraintLayout loading;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.nameshow);
            btn = itemView.findViewById(R.id.scan1);
            loading = itemView.findViewById(R.id.loading);

        }
    }
    public interface OnPatientClickListener {
        void onPatientClick(String uid);
    }
}
