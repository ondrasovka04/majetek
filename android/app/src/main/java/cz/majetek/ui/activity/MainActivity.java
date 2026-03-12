package cz.majetek.ui.activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import cz.majetek.R;
import cz.majetek.ui.fragment.ControlScanFragment;
import cz.majetek.ui.fragment.DataTransferFragment;
import cz.majetek.ui.fragment.InventoryScanFragment;

/**
 * Single Activity – BottomNavigationView switches between 3 fragments.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sys.left, sys.top, sys.right, 0);
            bottomNav.setPadding(0, 0, 0, sys.bottom);
            return insets;
        });


        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_control_scan) {
                fragment = new ControlScanFragment();
            } else if (itemId == R.id.navigation_inventory_scan) {
                fragment = new InventoryScanFragment();
            } else if (itemId == R.id.navigation_data_transfer) {
                fragment = new DataTransferFragment();
            }
            if (fragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment_container, fragment).commit();
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.navigation_control_scan);
        }
    }
}