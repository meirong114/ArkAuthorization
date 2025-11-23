package prts.user.authorization0;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class StartMainService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        Intent intenbt = new Intent(StartMainService.this, VideoPlayerActivity.class);
        startActivity(intenbt);
        return null;
    }
    
}
