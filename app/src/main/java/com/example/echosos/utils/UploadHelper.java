package com.example.echosos.utils;

import android.content.Context;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.File;

public class UploadHelper {
    public interface Callback {
        void onSuccess(String url);
        void onError(Exception e);
    }

    public static void uploadToFirebase(Context ctx, File file, Callback cb) {
        StorageReference ref = FirebaseStorage.getInstance().getReference()
                .child("audio_chunks/" + file.getName());
        UploadTask task = ref.putFile(android.net.Uri.fromFile(file));
        task.addOnSuccessListener(snapshot ->
                ref.getDownloadUrl().addOnSuccessListener(uri -> cb.onSuccess(uri.toString()))
                        .addOnFailureListener(cb::onError)
        ).addOnFailureListener(cb::onError);
    }
}