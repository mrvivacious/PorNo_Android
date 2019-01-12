package us.mrvivacio.porno;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

public class about extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Thank you, https://stackoverflow.com/questions/39052127/how-to-add-an-actionbar-in-android-studio-for-beginners
        getSupportActionBar().setTitle("About PorNo!"); // for set actionbar title
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // for add back arrow in action bar
    }

    // Thank you, https://stackoverflow.com/questions/26651602/display-back-arrow-on-toolbar
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
