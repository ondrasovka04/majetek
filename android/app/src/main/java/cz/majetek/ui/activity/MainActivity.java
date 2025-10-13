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

public class MainActivity extends AppCompatActivity {

    private Fragment controlFragment;
    private Fragment inventoryFragment;
    private Fragment transferFragment;
    private Fragment activeFragment;

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


        if (savedInstanceState == null) {
            controlFragment = new ControlScanFragment();
            inventoryFragment = new InventoryScanFragment();
            transferFragment = new DataTransferFragment();

            getSupportFragmentManager().beginTransaction().add(R.id.nav_host_fragment_container, transferFragment, "transfer").hide(transferFragment).add(R.id.nav_host_fragment_container, inventoryFragment, "inventory").hide(inventoryFragment).add(R.id.nav_host_fragment_container, controlFragment, "control").commit();

            activeFragment = controlFragment;
        } else {
            controlFragment = getSupportFragmentManager().findFragmentByTag("control");
            inventoryFragment = getSupportFragmentManager().findFragmentByTag("inventory");
            transferFragment = getSupportFragmentManager().findFragmentByTag("transfer");

            if (controlFragment != null && controlFragment.isVisible())
                activeFragment = controlFragment;
            else if (inventoryFragment != null && inventoryFragment.isVisible())
                activeFragment = inventoryFragment;
            else activeFragment = transferFragment;
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_control_scan) {
                switchTo(controlFragment);
                return true;
            } else if (id == R.id.navigation_inventory_scan) {
                switchTo(inventoryFragment);
                return true;
            } else if (id == R.id.navigation_data_transfer) {
                switchTo(transferFragment);
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.navigation_control_scan);
        }
    }

    private void switchTo(Fragment target) {
        if (activeFragment == target) return;
        getSupportFragmentManager().beginTransaction().hide(activeFragment).show(target).commit();
        activeFragment = target;
    }
}