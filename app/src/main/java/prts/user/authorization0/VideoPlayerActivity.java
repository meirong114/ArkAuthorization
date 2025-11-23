package prts.user.authorization0;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.VideoView;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import android.os.Handler;
import android.os.Build;
import android.util.Log;

public class VideoPlayerActivity extends Activity {
    private static final int REQUEST_READ_STORAGE = 1;
    private static final String TAG = "VideoPlayer";
    private VideoView videoView;
    private List<String> videoPaths = new ArrayList<String>();
    private Random random = new Random();
    private Handler handler = new Handler();
    private Runnable gcRunnable = new Runnable() {
        @Override
        public void run() {
            // 尝试触发垃圾回收
            System.gc();
            // 重新调度下一次GC
            // 改为10-15分钟一次
            handler.postDelayed(this, 10 * 60 * 1000);
        }
    };
    private long firstBackTime;
    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - firstBackTime > 0) {
            firstBackTime = System.currentTimeMillis();
            return;
        }
        System.gc();
        super.onBackPressed();
        System.gc();
    }
    private static final String TARGET_FOLDER = "Authorization";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler.post(gcRunnable);

        // 设置全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_video_player);
        videoView = (VideoView) findViewById(R.id.videoView);

        // 检查权限
        if (checkPermission()) {
            loadVideos();
        }

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    // 循环播放当前视频
                playVideo(0);
            }
    });
    }

        private boolean checkPermission() {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) 
            != PackageManager.PERMISSION_GRANTED) {
                               requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 
            REQUEST_READ_STORAGE);
        return false;
        }
    return true;
    }

    @Override
        public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
            if (requestCode == REQUEST_READ_STORAGE && grantResults.length > 0
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            // 在 Android 6.0 之前，权限是安装时授予的
            // 可以添加版本判断
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 使用运行时权限
                } else {
                // 直接加载视频
            loadVideos();
            }
        //loadVideos();
    }
    }

        private void loadVideos() {
        File dcimDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File targetDir = new File(dcimDir, TARGET_FOLDER);

            if (targetDir.exists() && targetDir.isDirectory()) {
        scanVideos(targetDir);
        }

            if (!videoPaths.isEmpty()) {
            Collections.shuffle(videoPaths);
        playVideo(0);
    }
    }

        private void scanVideos(File directory) {
        File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                scanVideos(file);
                    } else {
                    String path = file.getAbsolutePath().toLowerCase();
                        if (path.endsWith(".mp4") || path.endsWith(".3gp") || 
                        path.endsWith(".mkv") || path.endsWith(".webm")) {
                    videoPaths.add(file.getAbsolutePath());
                }
            }
        }
    }
    }

        private void playVideo(int index) {
        if (videoPaths.isEmpty()) return;
            try {
            Thread.sleep(100);
        Log.d(TAG, "Sleep Success");
            } catch (InterruptedException e) {
            String log = "" + e;
        Log.d(TAG, log);
        }
        String videoPath = videoPaths.get(index);
        videoView.setVideoURI(Uri.parse(videoPath));
    videoView.start();
    }

        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
    switch (keyCode) {
    case KeyEvent.KEYCODE_VOLUME_DOWN:
    return false;
    case KeyEvent.KEYCODE_VOLUME_UP:
        playNextRandomVideo();
            return true;
                }
            return super.onKeyDown(keyCode, event);
                }

        private void playNextRandomVideo() {
        if (videoPaths.isEmpty()) return;
    playVideo(random.nextInt(videoPaths.size()));
    }

    @Override
        protected void onPause() {
        super.onPause();
            if (videoView != null) {
        videoView.pause();
    }
    }

    @Override
        protected void onResume() {
        super.onResume();
            if (videoView != null && !videoView.isPlaying()) {
        videoView.start();
    }
    }

    @Override
        protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(gcRunnable);
            if (videoView != null) {
            videoView.stopPlayback();
        handler.removeCallbacks(gcRunnable);
    }
}
}

