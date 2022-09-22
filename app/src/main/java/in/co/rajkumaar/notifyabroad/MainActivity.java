package in.co.rajkumaar.notifyabroad;

import static android.Manifest.permission.READ_CALL_LOG;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECEIVE_SMS;
import static in.co.rajkumaar.notifyabroad.Utils.hasPermissions;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    private TextView permissionsNotGranted;
    private LinearLayout settingsLayout;
    private EditText botURL;
    private CheckBox notifyCalls;
    private CheckBox notifySMS;

    private final String TELEGRAM_BOT_URL = "telegram_bot_url";
    private final String NOTIFY_CALLS = "notify_calls";
    private final String NOTIFY_SMS = "notify_sms";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissionsNotGranted = findViewById(R.id.permissions_not_granted);
        settingsLayout = findViewById(R.id.settings_layout);
        Button saveSettings = findViewById(R.id.saveSettings);
        botURL = findViewById(R.id.telegramURL);
        notifyCalls = findViewById(R.id.notifyCalls);
        notifySMS = findViewById(R.id.notifySMS);

        String prefName = "settings";
        sharedPreferences = getSharedPreferences(prefName, MODE_PRIVATE);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();

        String[] PERMISSIONS = {RECEIVE_SMS, READ_PHONE_STATE, READ_CALL_LOG};
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, 1);
        } else {
            initSettingsLayout();
        }

        saveSettings.setOnClickListener(view -> {
            // TODO - Add bot URL validation by using getMe
            sharedPreferencesEditor.putString(TELEGRAM_BOT_URL, botURL.getText().toString());
            sharedPreferencesEditor.putBoolean(NOTIFY_CALLS, notifyCalls.isChecked());
            sharedPreferencesEditor.putBoolean(NOTIFY_SMS, notifySMS.isChecked());
            sharedPreferencesEditor.apply();
        });
    }

    private void initSettingsLayout() {
        permissionsNotGranted.setVisibility(View.GONE);
        settingsLayout.setVisibility(View.VISIBLE);
        botURL.setText(sharedPreferences.getString(TELEGRAM_BOT_URL, ""));
        notifyCalls.setChecked(sharedPreferences.getBoolean(NOTIFY_CALLS, false));
        notifySMS.setChecked(sharedPreferences.getBoolean(NOTIFY_SMS, false));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean allPermissionsGranted = true;
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                allPermissionsGranted = false;
                break;
            }
        }
        if (allPermissionsGranted) {
            initSettingsLayout();
        }
    }
}