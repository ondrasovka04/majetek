package cz.majetek.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
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

public class ControlScanFragment extends Fragment {

    private EditText input;
    private TextView txtId, txtName, txtPrice, txtQuantity, txtLocation, txtStatus;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final static Locale CZECH_LOCALE = Locale.forLanguageTag("cs-CZ");

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

    private void setupInput() {
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        input.setOnEditorActionListener((tv, actionId, event) -> {
            boolean isEnter = (event != null && event.getAction() == KeyEvent.ACTION_DOWN && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER || event.getKeyCode() == KeyEvent.KEYCODE_TAB)) || actionId == EditorInfo.IME_ACTION_DONE;

            if (isEnter) {
                handleScanned(tv.getText().toString().trim());
                return true;
            }
            return false;
        });

        input.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_TAB)) {
                handleScanned(input.getText().toString().trim());
                return true;
            }
            return false;
        });
    }

    private void handleScanned(String code) {
        if (code.isEmpty()) return;
        input.setText("");
        txtStatus.setText("Zpracovávám: " + code);
        txtStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.statusNeutral));

        if (code.startsWith("L")) {
            postUI(() -> showNotFound("Načtena lokace: " + code + " – naskenujte majetek"));
            return;
        }

        int id;
        try {
            id = Integer.parseInt(code);
        } catch (NumberFormatException nfe) {
            postUI(() -> showNotFound("Neplatný kód: " + code));
            return;
        }

        io.execute(() -> {
            AppDatabase db = ((MajetekApp) requireActivity().getApplication()).getDatabase();
            CardEntity card = db.cardDao().getById(id);
            if (card == null) {
                postUI(() -> showNotFound("Nenalezeno ID " + id));
            } else {
                postUI(() -> showCard(card));
            }
        });
    }

    private void showCard(CardEntity c) {
        txtId.setText(String.format(Locale.getDefault(), "%04d", c.id));
        txtName.setText(c.name);
        txtPrice.setText(String.format(CZECH_LOCALE, "%,.2f", c.price));
        txtQuantity.setText(String.valueOf((int) c.quantity));
        txtLocation.setText(String.valueOf(c.locationId));
        txtStatus.setText("V pořádku");
        txtStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.statusOk));

        input.requestFocus();
    }

    private void showNotFound(String msg) {
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
        if (getActivity() != null) getActivity().runOnUiThread(r);
    }
}
