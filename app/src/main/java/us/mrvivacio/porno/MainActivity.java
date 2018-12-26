package us.mrvivacio.porno;

import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ArrayList<String> items;
    private ArrayAdapter<String> itemsAdapter;
    private ListView lvItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Remind user to enable PorNo! service
        // Lmao thank you, https://stackoverflow.com/questions/3500197/how-to-display-toast-in-android
        Toast.makeText(this, "Yo ~~ Plz check if PorNo! is on first ~ ",
                Toast.LENGTH_LONG).show();

        // Tutorial code
        // Thank you, https://guides.codepath.com/android/Basic-Todo-App-Tutorial#configuring-android-studio
        lvItems = (ListView) findViewById(R.id.lv_Items);
        items = new ArrayList<String>();

        readItems();
        itemsAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, items);

        lvItems.setAdapter(itemsAdapter);
        setupListViewListener();
    }

    public void onEnableAccClick(View view) {
        startActivityForResult(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), 1);
    }


    // Attaches a long click listener
    private void setupListViewListener() {
        lvItems.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long l) {
                        // Remove the item within array at position
                        items.remove(pos);

                        // Refresh the adapter
                        itemsAdapter.notifyDataSetChanged();

                        // Return true consumes the long click event (marks it handled)
                        writeItems();
                        return true;
                    }
                }
        );
    }

    public void onAddItem(View v) {
        EditText url = (EditText) findViewById(R.id.et_NewItem);
        EditText name = (EditText) findViewById(R.id.et_NewItem2);

        String urlText = url.getText().toString().trim();
        String nameText = name.getText().toString().trim();

        urlText += " | " + nameText;

        itemsAdapter.add(urlText);
        url.setText("");
        name.setText("");

        writeItems();
    }

    private void readItems() {
        File filesDir = getFilesDir();
        Log.d("dawg", filesDir.toString());
        File todoFile = new File(filesDir, "todo.txt");

        try {
            items = new ArrayList<String>(FileUtils.readLines(todoFile));
        } catch (IOException ioe) {
            items = new ArrayList<String>();
        }
    }

    private void writeItems() {
        File filesDir = getFilesDir();
        File todoFile = new File(filesDir, "todo.txt");

        try {
            FileUtils.writeLines(todoFile, items);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

}
