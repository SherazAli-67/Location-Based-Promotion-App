package com.app.promotionapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.promotionapp.R;
import com.app.promotionapp.model.Store;
import com.app.promotionapp.model.SubscribedStore;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class RetailerSettingFragAdapter extends RecyclerView.Adapter<RetailerSettingFragAdapter.UserSubscribedStoresViewholder> {
    List<SubscribedStore> storeList;
    FirebaseFirestore firestore;

    public RetailerSettingFragAdapter(List<SubscribedStore> storeList) {
        this.storeList = storeList;
        firestore = FirebaseFirestore.getInstance();

    }

    @NonNull
    @Override
    public UserSubscribedStoresViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.retailer_statistics_layout, null, false);
        return new UserSubscribedStoresViewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserSubscribedStoresViewholder holder, int position) {
        SubscribedStore store = storeList.get(position);
        holder.shopTitle.setText(store.getStore_title());

        firestore.collection("Stores")
                .whereEqualTo("store_id", store.getStore_id())
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> documentSnapshots = queryDocumentSnapshots.getDocuments();
                for(DocumentSnapshot documentSnapshot:documentSnapshots){
                    holder.shopLocation.setText(documentSnapshot.toObject(Store.class).getLocation_subtitle());
                }
            }
        });

        firestore.collection("Subscribed")
                .whereEqualTo("store_title", store.getStore_title())
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(queryDocumentSnapshots.getDocuments().size() == 0)
                    holder.shopSubscribers.setText("No Subscriber !");
                else{
                    holder.shopSubscribers.setText(queryDocumentSnapshots.getDocuments().size()+" subscriber");
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return storeList.size();
    }


    public class UserSubscribedStoresViewholder extends RecyclerView.ViewHolder{
        TextView shopTitle;
        TextView shopLocation;
        TextView shopSubscribers;

        public UserSubscribedStoresViewholder(@NonNull View itemView) {
            super(itemView);

            shopTitle = itemView.findViewById(R.id.retailerSetting_shopTitle);
            shopLocation = itemView.findViewById(R.id.retailerSetting_shopLocation);
            shopSubscribers = itemView.findViewById(R.id.retailerSetting_subscribers);
        }
    }
}
