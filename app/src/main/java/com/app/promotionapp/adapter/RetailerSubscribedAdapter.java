package com.app.promotionapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.app.promotionapp.R;
import com.app.promotionapp.model.SubscribedStore;
import com.app.promotionapp.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class RetailerSubscribedAdapter extends BaseAdapter {
    List<SubscribedStore> storeList;

    public RetailerSubscribedAdapter(List<SubscribedStore> storeList) {
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

        FirebaseFirestore.getInstance().collection("Users")
                .document(store.getSubscribedUserID())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                shopLocation.setText("Subscribed by: "+documentSnapshot.toObject(User.class).getName());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
        return view;
    }
}
