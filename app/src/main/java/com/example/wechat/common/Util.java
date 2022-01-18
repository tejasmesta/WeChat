package com.example.wechat.common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Util {

    public static boolean connectionAvailable(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if(connectivityManager!=null && connectivityManager.getActiveNetworkInfo()!=null)
        {
            return connectivityManager.getActiveNetworkInfo().isAvailable();
        }
        else
        {
            return false;
        }
    }

    public static void deviceToken(Context context,String token)
    {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if(currentUser!=null) {

            DatabaseReference rootref = FirebaseDatabase.getInstance("https://wechat-da973-default-rtdb.asia-southeast1.firebasedatabase.app").getReference();

            DatabaseReference databaseReference = rootref.child(NodeNames.TOKEN).child(currentUser.getUid());

            HashMap<String,String> hashMap = new HashMap<>();

            hashMap.put(NodeNames.DEVICE_TOKEN,token);

            databaseReference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(!task.isSuccessful())
                    {
                        Toast.makeText(context, "Failed to store device token "+task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }
}
