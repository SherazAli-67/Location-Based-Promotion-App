package com.app.promotionapp.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.app.promotionapp.R;
import com.app.promotionapp.model.Store;
import com.app.promotionapp.model.SubscribedStore;
import com.app.promotionapp.model.User;
import com.app.promotionapp.retailer.DetailedInfoActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class UserSubscribedAdapter extends BaseAdapter {
    List<SubscribedStore> storeList;

    public UserSubscribedAdapter(List<SubscribedStore> storeList) {
        this.storeList = storeList;
    }

    @Override
    public int getCount() {
        return storeList.size();
    }

    @Override
    public Object getItem(int position) {
        return storeList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_subscribed_layout, null, false);
        TextView shopTitle = view.findViewById(R.id.userSubscribed_shopTitle);
        TextView shopLocation = view.findViewById(R.id.userSubscribed_location);
        SubscribedStore store = storeList.get(position);
        shopTitle.setText(store.getStore_title());

        FirebaseFirestore.getInstance().collection("Stores")
                .whereEqualTo("store_id",store.getStore_id())
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> documentSnapshots = queryDocumentSnapshots.getDocuments();
                for(DocumentSnapshot documentSnapshot: documentSnapshots){
                    shopLocation.setText(documentSnapshot.toObject(Store.class).getLocation_subtitle());
                }
            }
        });
        return view;
    }
}
