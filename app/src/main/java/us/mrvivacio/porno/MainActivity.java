package us.mrvivacio.porno;

import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Remind user to enable PorNo! service
        // Lmao thank you, https://stackoverflow.com/questions/3500197/how-to-display-toast-in-android
        Toast.makeText(this, "Yo ~~ Plz check if PorNo! is on first ~ ",
                Toast.LENGTH_LONG).show();
    }

    public void onEnableAccClick(View view) {
        startActivityForResult(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), 1);
    }

}
