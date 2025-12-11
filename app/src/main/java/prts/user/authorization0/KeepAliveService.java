package prts.user.authorization0;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.view.WindowManager;
import android.view.View;
import android.view.LayoutInflater;
import android.os.Build;
import android.graphics.PixelFormat;
import android.view.Gravity;

public class KeepAliveService extends Service {
    private WindowManager windowManager;
    private View floatingView;

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_window, null);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            20, 20,  // 设置悬浮窗的宽高为 20dp x 20dp
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;  // 设置悬浮窗在屏幕顶部左侧
        params.x = 0;
        params.y = 0;

        windowManager.addView(floatingView, params);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingView != null) {
            windowManager.removeView(floatingView);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

