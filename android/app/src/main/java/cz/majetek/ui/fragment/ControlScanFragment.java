package cz.majetek.ui.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
        return v;
    }

    private void setupInput() {
        input.setOnEditorActionListener((tv, actionId, event) -> {
            boolean isEnter = (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) || actionId == EditorInfo.IME_ACTION_DONE;
            if (isEnter) {
                handleScanned(tv.getText().toString().trim());
                return true;
            }
            return false;
        });

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && s.length() > 0 && s.charAt(s.length() - 1) == '\n') {
                    String cleaned = s.toString().trim();
                    handleScanned(cleaned);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void handleScanned(String code) {
        if (code.isEmpty()) return;
        input.setText("");
        txtStatus.setText("Zpracovávám: " + code);

        if (code.startsWith("L")) {
            showNotFound("Načtena lokace: " + code + " – naskenujte majetek");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(code);
        } catch (NumberFormatException nfe) {
            showNotFound("Neplatný kód: " + code);
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
        txtId.setText("ID: " + c.id);
        txtName.setText("Název: " + c.name);
        txtPrice.setText("Cena: " + c.price);
        txtQuantity.setText("Množství: " + c.quantity);
        txtLocation.setText("OL: " + c.locationId);
        txtStatus.setText("Hotovo.");

        input.requestFocus();
    }

    private void showNotFound(String msg) {
        txtId.setText("ID: -");
        txtName.setText("Název: -");
        txtPrice.setText("Cena: -");
        txtQuantity.setText("Množství: -");
        txtLocation.setText("OL: -");
        txtStatus.setText(msg);
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();

        input.requestFocus();
    }

    private void postUI(Runnable r) {
        if (getActivity() != null) getActivity().runOnUiThread(r);
    }
}
