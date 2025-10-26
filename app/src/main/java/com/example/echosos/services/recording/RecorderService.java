package com.example.echosos.services.recording;

import android.app.*;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.media.MediaRecorder;
import android.os.*;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.example.echosos.R;
import com.example.echosos.data.dao.RecordingChunkDao;
import com.example.echosos.data.local.DatabaseHelper;
import com.example.echosos.data.model.RecordingChunk;
import com.example.echosos.utils.UploadHelper;
import com.example.echosos.utils.Prefs;

import java.io.File;

public class RecorderService extends Service {
    public static final String CHANNEL_ID = "safety_recorder";
    private static final int NOTI_ID = 1201;
    public static final String EXTRA_EVENT_ID = "event_id";
    public static final String EXTRA_USER_ID  = "user_id";

    private MediaRecorder recorder;
    private Handler handler;
    private long userId, eventId;

    @Override public void onCreate() {
        super.onCreate();
        createChannel();
        handler = new Handler(Looper.getMainLooper());
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        // SAFE MODE GUARD
        if (Prefs.isSafeMode(getApplicationContext())) {
            stopSelf();
            return START_NOT_STICKY;
        }

        userId = intent != null ? intent.getLongExtra(EXTRA_USER_ID, 0) : 0;
        eventId = intent != null ? intent.getLongExtra(EXTRA_EVENT_ID, 0) : 0;

        Notification n = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Đang ghi âm an toàn…")
                .setOngoing(true).build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTI_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE);
        } else startForeground(NOTI_ID, n);

        startChunkLoop(); // bắt đầu vòng ghi 5s
        return START_STICKY;
    }

    private void startChunkLoop() {
        recordNewChunk(); // ghi ngay chunk đầu
        handler.postDelayed(loop, 5000L); // mỗi 5s tạo chunk mới
    }

    private final Runnable loop = new Runnable() {
        @Override public void run() {
            // Nếu trong lúc chạy người dùng bật Safe Mode -> dừng vòng lặp
            if (Prefs.isSafeMode(getApplicationContext())) {
                stopSelf();
                return;
            }
            recordNewChunk();
            handler.postDelayed(this, 5000L);
        }
    };

    private void recordNewChunk() {
        stopRecorderSafe();

        try {
            File dir = new File(getFilesDir(), "recordings");
            if (!dir.exists()) dir.mkdirs();
            String name = "rec_" + System.currentTimeMillis() + ".m4a";
            File out = new File(dir, name);

            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setAudioEncodingBitRate(96_000);
            recorder.setAudioSamplingRate(44_100);
            recorder.setOutputFile(out.getAbsolutePath());
            recorder.prepare();
            recorder.start();

            // Lên lịch stop sau 5s, enqueue + upload
            handler.postDelayed(() -> {
                stopRecorderSafe();
                // Nếu người dùng vừa bật Safe Mode trong khi chờ -> không enqueue/upload
                if (Prefs.isSafeMode(getApplicationContext())) {
                    // Xoá file rác nếu có
                    try { if (out.exists()) out.delete(); } catch (Exception ignored) {}
                    stopSelf();
                    return;
                }
                enqueueAndUpload(out);
            }, 5000L);

        } catch (Exception e) {
            stopSelf(); // lỗi thiết bị/permission -> dừng
        }
    }

    private void enqueueAndUpload(File out) {
        RecordingChunkDao dao = new RecordingChunkDao(new DatabaseHelper(this).getWritableDatabase());
        RecordingChunk ch = new RecordingChunk();
        ch.setUserId(userId);
        ch.setEventId(eventId);
        ch.setLocalPath(out.getAbsolutePath());
        ch.setStatus("queued");
        ch.setCreatedAt(System.currentTimeMillis());
        long id = dao.enqueue(ch);

        UploadHelper.uploadToFirebase(this, out, new UploadHelper.Callback() {
            @Override public void onSuccess(String url) {
                dao.markUploaded(id, url);
                // chunk đầu: có thể cập nhật SosHistory.audio_url ở nơi gọi service
            }
            @Override public void onError(Exception e) {
                dao.markFailed(id);
            }
        });
    }

    private void stopRecorderSafe() {
        try { if (recorder != null) { recorder.stop(); } } catch (Exception ignored) {}
        try { if (recorder != null) { recorder.release(); } } catch (Exception ignored) {}
        recorder = null;
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                nm.createNotificationChannel(new NotificationChannel(
                        CHANNEL_ID, "Safe Recorder", NotificationManager.IMPORTANCE_LOW));
            }
        }
    }

    @Override public void onDestroy() {
        if (handler != null) handler.removeCallbacksAndMessages(null);
        stopRecorderSafe();
        super.onDestroy();
    }

    @Nullable @Override public IBinder onBind(Intent intent) { return null; }
}
