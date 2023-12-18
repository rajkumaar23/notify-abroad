package in.co.rajkumaar.notifyabroad;

import static in.co.rajkumaar.notifyabroad.Constants.ARE_FILTERS_ENABLED;
import static in.co.rajkumaar.notifyabroad.Constants.FILTERS_LIST;
import static in.co.rajkumaar.notifyabroad.Constants.SMS_FILTERS;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class FiltersActivity extends AppCompatActivity {

    private CheckBox checkBoxEnableFilters;
    private ListView listViewFilters;

    private ArrayList<String> filtersList;
    private ArrayAdapter<String> filtersAdapter;
    private SharedPreferences.Editor sharedPreferencesEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filters);

        checkBoxEnableFilters = findViewById(R.id.checkBoxEnableFilters);
        listViewFilters = findViewById(R.id.listViewFilters);
        Button btnAddFilter = findViewById(R.id.btnAddFilter);

        SharedPreferences sharedPreferences = getSharedPreferences(SMS_FILTERS, MODE_PRIVATE);
        sharedPreferencesEditor = sharedPreferences.edit();

        filtersList = new ArrayList<>();
        String storedFilters = sharedPreferences.getString(FILTERS_LIST, "");
        if (!storedFilters.isEmpty()) {
            try {
                JSONArray storedFiltersArray = new JSONArray(storedFilters);
                for (int i = 0; i < storedFiltersArray.length(); i++) {
                    filtersList.add(storedFiltersArray.getString(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("SMS FILTERS", String.format("parsing failed : %s", e));
            }
        }
        filtersAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, filtersList);
        listViewFilters.setAdapter(filtersAdapter);

        boolean areFiltersEnabled = sharedPreferences.getBoolean(ARE_FILTERS_ENABLED, false);
        checkBoxEnableFilters.setChecked(areFiltersEnabled);
        toggleFiltersListVisibility(areFiltersEnabled);
        checkBoxEnableFilters.setOnCheckedChangeListener((buttonView, isChecked) -> toggleFiltersListVisibility(isChecked));

        btnAddFilter.setOnClickListener(v -> {
            if (checkBoxEnableFilters.isChecked()) {
                addNewFilter();
            } else {
                Toast.makeText(this, "Enable filters to add a new filter", Toast.LENGTH_SHORT).show();
            }
        });

        listViewFilters.setOnItemClickListener((parent, view, position, id) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Confirmation");
            builder.setMessage(String.format("Are you sure you want to delete the filter? (%s)", filtersList.get(position)));
            builder.setPositiveButton("Yes", (dialogInterface, i) -> {
                filtersList.remove(position);
                filtersAdapter.notifyDataSetChanged();
            });
            builder.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.cancel());
            builder.show();
        });
    }

    private void toggleFiltersListVisibility(boolean isChecked) {
        sharedPreferencesEditor.putBoolean(ARE_FILTERS_ENABLED, isChecked);
        listViewFilters.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        sharedPreferencesEditor.commit();
    }

    private void addNewFilter() {
        Dialog dialog = new Dialog(FiltersActivity.this);
        dialog.setContentView(R.layout.add_new_filter_dialog);

        final EditText filterText = dialog.findViewById(R.id.filter_text);
        Button bt = dialog.findViewById(R.id.submit);
        bt.setOnClickListener(view -> {
            String newFilterText = filterText.getText().toString().trim();
            if (newFilterText.isEmpty()) {
                Toast.makeText(FiltersActivity.this, "Filter text cannot be empty", Toast.LENGTH_SHORT).show();
            } else if (filtersList.contains(newFilterText)) {
                Toast.makeText(FiltersActivity.this, "Filter already exists", Toast.LENGTH_SHORT).show();
            } else{
                filtersList.add(newFilterText.trim());
                filtersAdapter.notifyDataSetChanged();
                dialog.cancel();
                JSONArray filtersToStore = new JSONArray(filtersList);
                sharedPreferencesEditor.putString(FILTERS_LIST, filtersToStore.toString());
                sharedPreferencesEditor.commit();
            }
        });
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}