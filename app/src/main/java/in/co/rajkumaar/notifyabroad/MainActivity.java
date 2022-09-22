package in.co.rajkumaar.notifyabroad;

import static android.Manifest.permission.READ_CALL_LOG;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECEIVE_SMS;
import static in.co.rajkumaar.notifyabroad.Utils.hasPermissions;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView permissionsNotGranted = findViewById(R.id.permissions_not_granted);
        LinearLayout settingsLayout = findViewById(R.id.settings_layout);

        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {RECEIVE_SMS, READ_PHONE_STATE, READ_CALL_LOG};
        if (!hasPermissions(this, PERMISSIONS)) {
            permissionsNotGranted.setVisibility(View.VISIBLE);
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        } else {
            permissionsNotGranted.setVisibility(View.GONE);
            settingsLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "Permissions updated : " + Arrays.toString(permissions));
    }
}