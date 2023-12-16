package in.co.rajkumaar.notifyabroad;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
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

import java.util.ArrayList;

public class FiltersActivity extends AppCompatActivity {

    private CheckBox checkBoxEnableFilters;
    private ListView listViewFilters;

    private ArrayList<String> filtersList;
    private ArrayAdapter<String> filtersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filters);

        checkBoxEnableFilters = findViewById(R.id.checkBoxEnableFilters);
        listViewFilters = findViewById(R.id.listViewFilters);
        Button btnAddFilter = findViewById(R.id.btnAddFilter);

        filtersList = new ArrayList<>();
        filtersAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, filtersList);
        listViewFilters.setAdapter(filtersAdapter);

        checkBoxEnableFilters.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                listViewFilters.setVisibility(View.VISIBLE);
            } else {
                listViewFilters.setVisibility(View.GONE);
            }
        });

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

    private void addNewFilter() {
        Dialog dialog = new Dialog(FiltersActivity.this);
        dialog.setContentView(R.layout.add_new_filter_dialog);

        final EditText filterText = dialog.findViewById(R.id.filter_text);
        Button bt = dialog.findViewById(R.id.submit);
        bt.setOnClickListener(view -> {
            String newFilterText = filterText.getText().toString().trim();
            if (!newFilterText.isEmpty()) {
                filtersList.add(newFilterText.trim());
                filtersAdapter.notifyDataSetChanged();
                dialog.cancel();
            } else {
                Toast.makeText(FiltersActivity.this, "Filter text cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}