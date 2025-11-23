package prts.user.authorization0;

// SecondaryStartup.java (使用XML布局版本)
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.util.Log;

public class SecondaryStartup extends Activity {

    private static final int SPLASH_DELAY = 11000; // 11秒延迟
    private long firstBackTime;
    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - firstBackTime > 0) {
            firstBackTime = System.currentTimeMillis();
            return;
        }

        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_startup);
        BootLogView bootLogView = new BootLogView(this);
        bootLogView.setBootCompleteListener(this);
        setContentView(bootLogView);

        // 3秒后跳转到 VideoPlayerActivity
        new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    jumpToVideoPlayer();
                }
            }, SPLASH_DELAY);
    }

    private void jumpToVideoPlayer() {
        try {
            Intent intent = new Intent(SecondaryStartup.this, VideoPlayerActivity.class);
            startActivity(intent);
            finish(); // 结束当前Activity
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }
    public void onBootComplete() {
        
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        new Handler().removeCallbacksAndMessages(null);
    }
}
