package com.slightsite.tutorialuploadimage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.slightsite.tutorialuploadimage.controllers.PictureController;
import com.slightsite.tutorialuploadimage.fragments.CameraFragment;
import com.slightsite.tutorialuploadimage.fragments.LibraryFragment;
import com.slightsite.tutorialuploadimage.utils.Database;
import com.slightsite.tutorialuploadimage.utils.DatabaseHelper;
import com.slightsite.tutorialuploadimage.utils.DateTimeStrategy;

public class MainActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener {

    private androidx.appcompat.app.ActionBar actionBar;
    public Database database;

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;

        switch (item.getItemId()) {
            case R.id.navigation_library:
                fragment = new LibraryFragment();
                actionBar.setTitle(getResources().getString(R.string.title_library));
                break;

            case R.id.navigation_camera:
                fragment = new CameraFragment();
                actionBar.hide();
                break;
        }

        return loadFragment(fragment);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init core apps
        initCoreApp();

        //loading the default fragment
        loadFragment(new CameraFragment());

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(this);

        actionBar = getSupportActionBar();

        navigation.setSelectedItemId(R.id.navigation_camera);
        navigation.getMenu().getItem(1).setChecked(true);
    }

    private void initCoreApp() {
        database = new DatabaseHelper(this);
        PictureController.setDatabase(database);

        DateTimeStrategy.setLocale("id", "ID");
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}
