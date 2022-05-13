package com.urjalusa.presec;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

class RecyclerAdapterAndViewHolder extends RecyclerView.Adapter<RecyclerViewHolder> {

    private final Context context;
    private final ArrayList<PrescriptionCardView> prescriptionList;

    RecyclerAdapterAndViewHolder(Context context, ArrayList<PrescriptionCardView> prescriptionList) {
        this.context = context;
        this.prescriptionList = prescriptionList;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.prescription_card_view, null);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
        holder.displayPrescriptionId.setText(String.valueOf(prescriptionList.get(position).getPrescriptionId()));
        holder.displayName.setText(String.valueOf(prescriptionList.get(position).getDisplayName()));
        holder.displayDate.setText(String.valueOf(prescriptionList.get(position).getDate()));
    }

    @Override
    public int getItemCount() {
        return prescriptionList.size();
    }
}

class RecyclerViewHolder extends RecyclerView.ViewHolder {

    final TextView displayPrescriptionId;
    final TextView displayName;
    final TextView displayDate;

    RecyclerViewHolder(@NonNull View itemView) {
        super(itemView);
        displayPrescriptionId = itemView.findViewById(R.id.textViewPrescriptionId_CardView);
        displayName = itemView.findViewById(R.id.textViewNameDisplay_CardView);
        displayDate = itemView.findViewById(R.id.textViewDateDisplay_CardView);
    }
}