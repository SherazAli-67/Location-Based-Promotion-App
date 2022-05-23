package com.app.promotionapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.promotionapp.R;
import com.app.promotionapp.model.Store;

import java.util.List;

public class RetailerAdapter extends RecyclerView.Adapter<RetailerAdapter.RetailerStoreViewholder> {
    List<Store> storeList;
    public RetailerAdapter(List<Store> storeList) {
        this.storeList = storeList;
    }
    @NonNull
    @Override
    public RetailerStoreViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.retailer_store_layout, null, false);
        return new RetailerStoreViewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RetailerStoreViewholder holder, int position) {
        Store store = storeList.get(position);
        holder.shopTitle.setText(store.getStore_title());
        holder.shopLocation.setText(store.getLocation_subtitle());
    }

    @Override
    public int getItemCount() {
        return storeList.size();
    }


    public class RetailerStoreViewholder extends RecyclerView.ViewHolder {
        TextView shopTitle;
        TextView shopLocation;
        ImageView shopImg;

        public RetailerStoreViewholder(@NonNull View itemView) {
            super(itemView);
            shopImg = itemView.findViewById(R.id.retailer_shopImg);
            shopTitle = itemView.findViewById(R.id.retailer_shopTitle);
            shopLocation = itemView.findViewById(R.id.retailer_shopArea);
        }
    }
}
