package cz.majetek.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cz.majetek.MajetekApp;
import cz.majetek.R;
import cz.majetek.data.db.AppDatabase;
import cz.majetek.data.entity.CardEntity;
import cz.majetek.util.ScanInputHelper;

/**
 * Fragment for control scanning – card lookup by scanned ID.
 */
public class ControlScanFragment extends Fragment {

    private EditText input;
    private TextView txtId;
    private TextView txtName;
    private TextView txtPrice;
    private TextView txtQuantity;
    private TextView txtLocation;
    private TextView txtStatus;

    private final ExecutorService io = Executors.newSingleThreadExecutor();

    private static final Locale CZECH_LOCALE = Locale.forLanguageTag("cs-CZ");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_control_scan, container, false);
        input = v.findViewById(R.id.input_scan);
        txtId = v.findViewById(R.id.txt_card_id);
        txtName = v.findViewById(R.id.txt_card_name);
        txtPrice = v.findViewById(R.id.txt_card_price);
        txtQuantity = v.findViewById(R.id.txt_card_quantity);
        txtLocation = v.findViewById(R.id.txt_card_location);
        txtStatus = v.findViewById(R.id.txt_status);

        setupInput();
        setupFocusWithoutKeyboard();
        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        io.shutdownNow();
    }

    /**
     * Suppresses the software keyboard – input comes from Zebra scanner as HW keyboard.
     */
    private void setupFocusWithoutKeyboard() {
        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        input.setShowSoftInputOnFocus(false);

        input.post(() -> {
            input.requestFocus();
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
        });

        input.setOnClickListener(v -> {
            input.setShowSoftInputOnFocus(true);
            input.post(() -> {
                InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
            });
        });
    }

    /**
     * Sets up listeners for Enter/Tab from scanner + IME.
     */
    private void setupInput() {
        ScanInputHelper.attachScanListeners(input, this::handleScanned);
    }

    /**
     * Distinguishes location code (L-prefix) from card code (number), looks up in DB.
     */
    private void handleScanned(String code) {
        if (code.isEmpty()) return;
        input.setText("");
        txtStatus.setText(getString(R.string.control_processing_code, code));
        txtStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.statusNeutral));

        if (code.startsWith("L")) {
            postUI(() -> showNotFound(getString(R.string.control_location_scanned, code.substring(1))));
            return;
        }

        int id;
        try {
            id = Integer.parseInt(code);
        } catch (NumberFormatException nfe) {
            postUI(() -> showNotFound(getString(R.string.control_invalid_code, code)));
            return;
        }

        final int finalId = id;
        MajetekApp app = (MajetekApp) requireActivity().getApplication();
        io.execute(() -> {
            AppDatabase db = app.getDatabase();
            CardEntity card = db.cardDao().getById(finalId);
            if (card == null) {
                postUI(() -> showNotFound(getString(R.string.control_not_found_id, finalId)));
            } else {
                postUI(() -> showCard(card));
            }
        });
    }

    private void showCard(CardEntity c) {
        if (!isAdded()) return;
        txtId.setText(String.format(Locale.getDefault(), "%04d", c.getId()));
        txtName.setText(c.getName());
        txtPrice.setText(String.format(CZECH_LOCALE, "%,.2f", c.getPrice()));
        txtQuantity.setText(String.valueOf((int) c.getQuantity()));
        txtLocation.setText(String.valueOf(c.getLocationId()));
        txtStatus.setText(getString(R.string.status_ok));
        txtStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.statusOk));

        input.requestFocus();
    }

    private void showNotFound(String msg) {
        if (!isAdded()) return;
        txtId.setText("");
        txtName.setText("");
        txtPrice.setText("");
        txtQuantity.setText("");
        txtLocation.setText("");
        txtStatus.setText(msg);
        txtStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.statusError));

        input.requestFocus();
    }

    private void postUI(Runnable r) {
        Activity a = getActivity();
        if (a != null) a.runOnUiThread(() -> {
            if (isAdded()) r.run();
        });
    }
}
