package in.co.rajkumaar.notifyabroad;

import static android.Manifest.permission.READ_CALL_LOG;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECEIVE_SMS;
import static in.co.rajkumaar.notifyabroad.Constants.NOTIFY_CALLS;
import static in.co.rajkumaar.notifyabroad.Constants.NOTIFY_SMS;
import static in.co.rajkumaar.notifyabroad.Constants.SHARED_PREFERENCES_KEY;
import static in.co.rajkumaar.notifyabroad.Constants.TELEGRAM_BOT_TOKEN;
import static in.co.rajkumaar.notifyabroad.Constants.TELEGRAM_CHAT_ID;
import static in.co.rajkumaar.notifyabroad.Utils.hasPermissions;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
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

import org.json.JSONException;
import org.json.JSONObject;

import in.co.rajkumaar.notifyabroad.api.GitHubAPI;
import in.co.rajkumaar.notifyabroad.api.GitHubAPIResponse;
import in.co.rajkumaar.notifyabroad.api.TelegramAPI;
import in.co.rajkumaar.notifyabroad.api.TelegramAPIResponse;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    private TextView permissionsNotGranted;
    private LinearLayout settingsLayout;
    private EditText botToken;
    private EditText chatID;
    private CheckBox notifyCalls;
    private CheckBox notifySMS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissionsNotGranted = findViewById(R.id.permissions_not_granted);
        settingsLayout = findViewById(R.id.settings_layout);
        Button saveSettings = findViewById(R.id.saveSettings);
        Button howToUse = findViewById(R.id.howToUse);
        botToken = findViewById(R.id.telegramToken);
        chatID = findViewById(R.id.telegramChatID);
        notifyCalls = findViewById(R.id.notifyCalls);
        notifySMS = findViewById(R.id.notifySMS);
        TextView version = findViewById(R.id.version);
        TextView about = findViewById(R.id.about);

        version.setText(String.format("v%s", BuildConfig.VERSION_NAME));
        about.setOnClickListener(view -> {
            String url = getString(R.string.online_home_of_rajkumar);
            startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url)));
        });

        sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_KEY, MODE_PRIVATE);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();

        String[] PERMISSIONS = {RECEIVE_SMS, READ_PHONE_STATE, READ_CALL_LOG};
        if (!hasPermissions(this, PERMISSIONS)) {
            settingsLayout.setVisibility(View.GONE);
            permissionsNotGranted.setVisibility(View.VISIBLE);
            ActivityCompat.requestPermissions(this, PERMISSIONS, 1);
        } else {
            initSettingsLayout();
            checkForUpdates();
        }

        saveSettings.setOnClickListener(view -> {
            try {
                String enteredBotToken = botToken.getText().toString();
                String enteredChatID = chatID.getText().toString();
                if (enteredBotToken.isEmpty() || enteredChatID.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please fill in both the inputs", Toast.LENGTH_SHORT).show();
                    return;
                }
                TelegramAPI api = new TelegramAPI(MainActivity.this, enteredBotToken);
                ProgressDialog dialog =
                        ProgressDialog.show(
                                MainActivity.this,
                                "",
                                "Validating your token and ID...",
                                true
                        );
                JSONObject requestBody = new JSONObject();
                requestBody.put("text", "Hope you have fun abroad!");
                requestBody.put("chat_id", enteredChatID);
                api.sendMessage(requestBody, new TelegramAPIResponse() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        dialog.dismiss();
                        sharedPreferencesEditor.putString(TELEGRAM_BOT_TOKEN, enteredBotToken);
                        sharedPreferencesEditor.putString(TELEGRAM_CHAT_ID, enteredChatID);
                        sharedPreferencesEditor.putBoolean(NOTIFY_CALLS, notifyCalls.isChecked());
                        sharedPreferencesEditor.putBoolean(NOTIFY_SMS, notifySMS.isChecked());
                        sharedPreferencesEditor.apply();

                        new AlertDialog.Builder(MainActivity.this)
                                .setMessage("Verify if you have received a test message from your bot in your Telegram chat.")
                                .setPositiveButton("Okay", (dialogInterface, i) -> dialogInterface.dismiss())
                                .show();
                    }

                    @Override
                    public void onFailure(Exception exception) {
                        dialog.dismiss();
                        exception.printStackTrace();
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Verification failed")
                                .setMessage("Enter valid token & ID and ensure you have started the bot from your chat.")
                                .setPositiveButton("Okay", (dialogInterface, i) -> dialogInterface.dismiss())
                                .show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Unexpected error occurred", Toast.LENGTH_SHORT).show();
            }
        });
        howToUse.setOnClickListener(view -> {
            String url = getString(R.string.how_to_use_link);
            startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url)));
        });
    }

    private void checkForUpdates() {
        String repoOwnerUsername = getString(R.string.repo_owner_username);
        String repoName = getString(R.string.repo_name);
        GitHubAPI api = new GitHubAPI(MainActivity.this, repoOwnerUsername, repoName);
        String currentVersion = "v" + BuildConfig.VERSION_NAME;
        api.getLatestReleaseVersion(new GitHubAPIResponse() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    String latestVersion = response.getString("name");
                    if (currentVersion.compareTo(latestVersion) < 0) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Update available")
                                .setMessage("A newer version to the app is available for download.")
                                .setPositiveButton("Update now", (dialogInterface, i) -> {
                                    String url = "https://github.com/" + repoOwnerUsername + "/" + repoName + "/releases/" + latestVersion;
                                    startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url)));
                                })
                                .setNegativeButton("Later", (dialogInterface, i) -> dialogInterface.dismiss()).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Exception exception) {
                exception.printStackTrace();
            }
        });
    }

    private void initSettingsLayout() {
        permissionsNotGranted.setVisibility(View.GONE);
        settingsLayout.setVisibility(View.VISIBLE);
        botToken.setText(sharedPreferences.getString(TELEGRAM_BOT_TOKEN, ""));
        chatID.setText(sharedPreferences.getString(TELEGRAM_CHAT_ID, ""));
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