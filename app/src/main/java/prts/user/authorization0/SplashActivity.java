package prts.user.authorization0;

// SplashActivity.java
// SecondaryStartup.java (使用XML布局版本)
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

public class SplashActivity extends Activity {

    private static final int SPLASH_DELAY = 3000; // 3秒延迟

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_startup);

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
            Intent intent = new Intent(SplashActivity.this, SecondaryStartup.class);
            startActivity(intent);
            finish(); // 结束当前Activity
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        new Handler().removeCallbacksAndMessages(null);
    }
}
