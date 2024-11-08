package com.example.emosafe.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emosafe.R;
import com.example.emosafe.models.scanhis;

import java.util.ArrayList;

public class anotherrecycler extends RecyclerView.Adapter<anotherrecycler.ViewHolder> {
    public anotherrecycler(ArrayList<scanhis> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }

    Context context;
    ArrayList<scanhis> arrayList;
    @NonNull
    @Override
    public anotherrecycler.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.anotherrecyclerlayout,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull anotherrecycler.ViewHolder holder, int position) {
        int adapos = holder.getAdapterPosition();
        scanhis scanhis = arrayList.get(adapos);
        holder.res.setText(scanhis.getRes());
        holder.date.setText(scanhis.getDate());
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView res,date;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            res = itemView.findViewById(R.id.resshow);
            date = itemView.findViewById(R.id.dateshow);
        }
    }
}
