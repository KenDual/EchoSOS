package com.example.echosos.services.recording;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.echosos.data.dao.RecordingChunkDao;
import com.example.echosos.data.local.DatabaseHelper;
import com.example.echosos.data.model.RecordingChunk;
import com.example.echosos.utils.NetworkUtils;
import com.example.echosos.utils.UploadHelper;

import java.io.File;
import java.util.List;

public class UploadRetryService extends Service {

    public static final String ACTION_RETRY_UPLOAD = "com.example.echosos.action.RETRY_UPLOAD";
    private static final String TAG = "UploadRetryService";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!NetworkUtils.isOnline(getApplicationContext())) {
            Log.d(TAG, "Offline -> skip retry");
            stopSelf();
            return START_NOT_STICKY;
        }

        Intent signal = new Intent(ACTION_RETRY_UPLOAD);
        signal.putExtra("trigger", intent != null ? intent.getStringExtra("reason") : "manual");
        sendBroadcast(signal);

        new Thread(() -> {
            try {
                RecordingChunkDao dao =
                        new RecordingChunkDao(new DatabaseHelper(getApplicationContext()).getWritableDatabase());
                final RecordingChunkDao daoRef = dao;

                // Lấy danh sách chunk đang "queued"
                List<RecordingChunk> queued = daoRef.getPending();
                Log.d(TAG, "Queued count = " + queued.size());

                for (RecordingChunk ch : queued) {
                    File f = new File(ch.getLocalPath());
                    final long chunkId = ch.getId();
                    final File fileRef = f;

                    if (!fileRef.exists()) {
                        daoRef.markFailed(chunkId);
                        continue;
                    }

                    UploadHelper.uploadToFirebase(getApplicationContext(), fileRef, new UploadHelper.Callback() {
                        @Override public void onSuccess(String url) {
                            try {
                                daoRef.markUploaded(chunkId, url);
                            } catch (Exception e) {
                                Log.e(TAG, "markUploaded error", e);
                            }
                        }

                        @Override public void onError(Exception e) {
                            try {
                                daoRef.markFailed(chunkId);
                            } catch (Exception ex) {
                                Log.e(TAG, "markFailed error", ex);
                            }
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "retry error", e);
            } finally {
                stopSelf();
            }
        }).start();

        return START_NOT_STICKY;
    }

    @Nullable @Override
    public IBinder onBind(Intent intent) { return null; }
}
