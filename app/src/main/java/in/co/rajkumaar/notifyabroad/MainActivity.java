package in.co.rajkumaar.notifyabroad;

import static android.Manifest.permission.READ_CALL_LOG;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECEIVE_SMS;
import static in.co.rajkumaar.notifyabroad.Utils.hasPermissions;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    private TextView permissionsNotGranted;
    private LinearLayout settingsLayout;
    private EditText botToken;
    private CheckBox notifyCalls;
    private CheckBox notifySMS;

    private final String TELEGRAM_BOT_TOKEN = "telegram_bot_token";
    private final String NOTIFY_CALLS = "notify_calls";
    private final String NOTIFY_SMS = "notify_sms";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissionsNotGranted = findViewById(R.id.permissions_not_granted);
        settingsLayout = findViewById(R.id.settings_layout);
        Button saveSettings = findViewById(R.id.saveSettings);
        botToken = findViewById(R.id.telegramToken);
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
            String enteredBotToken = botToken.getText().toString();
            TelegramAPI api = new TelegramAPI(MainActivity.this, enteredBotToken);
            ProgressDialog dialog =
                    ProgressDialog.show(
                            MainActivity.this,
                            "",
                            "Validating your token...",
                            true
                    );
            api.getMe(new TelegramAPIResponse() {
                @Override
                public void onSuccess(JSONObject response) {
                    dialog.dismiss();
                    Toast.makeText(MainActivity.this, "Token validated successfully", Toast.LENGTH_SHORT).show();
                    sharedPreferencesEditor.putString(TELEGRAM_BOT_TOKEN, enteredBotToken);
                    sharedPreferencesEditor.putBoolean(NOTIFY_CALLS, notifyCalls.isChecked());
                    sharedPreferencesEditor.putBoolean(NOTIFY_SMS, notifySMS.isChecked());
                    sharedPreferencesEditor.apply();
                    Log.v("MainActivity", response.toString());
                }

                @Override
                public void onFailure(Exception exception) {
                    dialog.dismiss();
                    Toast.makeText(MainActivity.this, "Token seems invalid", Toast.LENGTH_SHORT).show();
                    exception.printStackTrace();
                }
            });
        });
    }

    private void initSettingsLayout() {
        permissionsNotGranted.setVisibility(View.GONE);
        settingsLayout.setVisibility(View.VISIBLE);
        botToken.setText(sharedPreferences.getString(TELEGRAM_BOT_TOKEN, ""));
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