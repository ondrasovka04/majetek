package cz.majetek.ui.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import cz.majetek.data.entity.InventoryAuditEntity;
import cz.majetek.data.entity.LocationEntity;
import cz.majetek.ui.view.CircularInventoryProgressView;

public class InventoryScanFragment extends Fragment {

    private EditText inputLocation, inputAsset;
    private TextView txtLocationName, txtCardId, txtCardName, txtCardPrice, txtCardQuantity, txtCardOl, txtStatus;
    private CircularInventoryProgressView progressCircle;
    private LinearLayout cardContainer;
    private Button btnDelete, btnLeave;

    private final ExecutorService io = Executors.newSingleThreadExecutor();

    private String currentLocationId = null;
    private int lastScannedCardId = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_inventory_scan, container, false);
        bindViews(v);
        setupInputs();
        setupButtons();
        updateProgressUI(0, 0); // initial
        return v;
    }

    private void bindViews(View v) {
        inputLocation = v.findViewById(R.id.input_location);
        inputAsset = v.findViewById(R.id.input_asset);
        txtLocationName = v.findViewById(R.id.txt_location_name);
        txtCardId = v.findViewById(R.id.txt_card_id);
        txtCardName = v.findViewById(R.id.txt_card_name);
        txtCardPrice = v.findViewById(R.id.txt_card_price);
        txtCardQuantity = v.findViewById(R.id.txt_card_quantity);
        txtCardOl = v.findViewById(R.id.txt_card_ol);
        txtStatus = v.findViewById(R.id.txt_status);
        btnDelete = v.findViewById(R.id.btn_delete);
        btnLeave = v.findViewById(R.id.btn_leave);
        progressCircle = v.findViewById(R.id.progress_circle);
        cardContainer = v.findViewById(R.id.card_container);
    }

    private void setupInputs() {
        // Location input scan handling
        inputLocation.setOnEditorActionListener((tv, actionId, event) -> {
            boolean isEnter = (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) || actionId == EditorInfo.IME_ACTION_DONE;
            if (isEnter) {
                handleLocationScanned(tv.getText().toString().trim());
                return true;
            }
            return false;
        });
        inputLocation.addTextChangedListener(new TerminalEnterWatcher(this::handleLocationScanned));

        // Asset input scan handling
        inputAsset.setOnEditorActionListener((tv, actionId, event) -> {
            boolean isEnter = (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) || actionId == EditorInfo.IME_ACTION_DONE;
            if (isEnter) {
                handleAssetScanned(tv.getText().toString().trim());
                return true;
            }
            return false;
        });
        inputAsset.addTextChangedListener(new TerminalEnterWatcher(this::handleAssetScanned));
    }

    private void setupButtons() {
        btnDelete.setOnClickListener(v -> {
            if (lastScannedCardId <= 0) {
                Toast.makeText(getContext(), "Žádný majetek ke smazání", Toast.LENGTH_SHORT).show();
                return;
            }
            new AlertDialog.Builder(requireContext()).setTitle("Smazat").setMessage("Opravdu odstranit majetek " + lastScannedCardId + " z inventury?").setPositiveButton("Ano", (d, w) -> deleteLast()).setNegativeButton("Ne", null).show();
        });

        btnLeave.setOnClickListener(v -> {
            if (currentLocationId == null) {
                Toast.makeText(getContext(), "Nejste v místnosti", Toast.LENGTH_SHORT).show();
                return;
            }
            new AlertDialog.Builder(requireContext()).setTitle("Odejít z místnosti").setMessage("Smazat SL a pokračovat?").setPositiveButton("Ano", (d, w) -> clearLocation()).setNegativeButton("Ne", null).show();
        });
    }

    private void handleLocationScanned(String code) {
        if (code.isEmpty()) return;
        inputLocation.setText("");

        if (!code.startsWith("L") || code.length() == 1) {
            String msg = "Kód lokace musí začínat 'L'";
            txtStatus.setText(msg);
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            inputLocation.requestFocus();
            return;
        }
        String locId = code.substring(1);

        io.execute(() -> {
            AppDatabase db = ((MajetekApp) requireActivity().getApplication()).getDatabase();
            LocationEntity le = db.locationDao().getById(locId);
            if (le == null) {
                postUI(() -> {
                    String msg = "Lokace nenalezena: " + locId;
                    txtStatus.setText(msg);
                    Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                    inputLocation.requestFocus();
                });
                return;
            }

            int scannedInLoc = db.inventoryAuditDao().countCardsInLocation(locId);
            int totalInLoc = db.cardDao().countByLocation(locId);

            postUI(() -> {
                currentLocationId = locId; // nastavíme až po úspěšném nalezení
                txtStatus.setText("Načtena lokace: L" + locId + " – naskenujte majetek");
                txtLocationName.setText("SL: " + locId + " – " + (le.name != null ? le.name : "(bez názvu)"));
                updateProgressUI(scannedInLoc, totalInLoc);
                inputAsset.requestFocus();
                Toast.makeText(getContext(), "Lokace nastavena", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void handleAssetScanned(String code) {
        if (code.isEmpty()) return;
        inputAsset.setText("");
        if (code.startsWith("L")) {
            Toast.makeText(getContext(), "Načtena lokace v poli pro majetek – použijte horní pole", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentLocationId == null) {
            txtStatus.setText("Nejdříve naskenujte místnost (SL)");
            Toast.makeText(getContext(), "Nejdříve naskenujte místnost (SL)", Toast.LENGTH_SHORT).show();
            return;
        }
        int id;
        try {
            id = Integer.parseInt(code);
        } catch (NumberFormatException e) {
            showUnknown(code);
            return;
        }
        final int cardId = id;
        io.execute(() -> {
            AppDatabase db = ((MajetekApp) requireActivity().getApplication()).getDatabase();
            CardEntity card = db.cardDao().getById(cardId);
            long now = System.currentTimeMillis();
            // Upsert do auditu i pro neznámé ID – uložíme cardId a currentLocationId
            db.inventoryAuditDao().upsert(new InventoryAuditEntity(cardId, currentLocationId, now));

            int scannedInLoc = db.inventoryAuditDao().countCardsInLocation(currentLocationId);
            int totalInLoc = db.cardDao().countByLocation(currentLocationId);

            if (card == null) {
                postUI(() -> {
                    showUnknown(String.valueOf(cardId));
                    updateProgressUI(scannedInLoc, totalInLoc);
                });
            } else {
                postUI(() -> {
                    showCard(card);
                    updateProgressUI(scannedInLoc, totalInLoc);
                });
            }
        });
    }

    private void showCard(CardEntity c) {
        lastScannedCardId = c.id;
        txtCardId.setText("ID: " + c.id);
        txtCardName.setText("Název: " + c.name);
        txtCardPrice.setText("Cena: " + c.price);
        txtCardQuantity.setText("Množství: " + c.quantity);
        txtCardOl.setText("OL: " + c.locationId);
        if (currentLocationId != null) {
            if (c.locationId == null) {
                cardContainer.setBackgroundColor(0xFFFFCDD2); // red missing OL
                txtStatus.setText("Chybí OL");
            } else if (c.locationId.equals(currentLocationId)) {
                cardContainer.setBackgroundColor(0xFFC8E6C9); // green
                txtStatus.setText("OK – uloženo");
            } else {
                cardContainer.setBackgroundColor(0xFFFFE0B2); // orange mismatch
                txtStatus.setText("Nesoulad OL vs SL");
            }
        }
        inputAsset.requestFocus();
    }

    private void showUnknown(String code) {
        lastScannedCardId = -1;
        txtCardId.setText("ID: -");
        txtCardName.setText("Název: -");
        txtCardPrice.setText("Cena: -");
        txtCardQuantity.setText("Množství: -");
        txtCardOl.setText("OL: -");
        cardContainer.setBackgroundColor(0xFFFFCDD2); // red
        txtStatus.setText("Neznámé ID: " + code);
        Toast.makeText(getContext(), "Neznámé ID: " + code, Toast.LENGTH_SHORT).show();
        inputAsset.requestFocus();
    }

    private void deleteLast() {
        if (lastScannedCardId <= 0) return;
        io.execute(() -> {
            AppDatabase db = ((MajetekApp) requireActivity().getApplication()).getDatabase();
            db.inventoryAuditDao().deleteByCardId(lastScannedCardId);
            int scannedInLoc = currentLocationId != null ? db.inventoryAuditDao().countCardsInLocation(currentLocationId) : 0;
            int totalInLoc = currentLocationId != null ? db.cardDao().countByLocation(currentLocationId) : 0;
            postUI(() -> {
                lastScannedCardId = -1;
                resetCardDisplay();
                updateProgressUI(scannedInLoc, totalInLoc);
                txtStatus.setText("Záznam odstraněn");
            });
        });
    }

    private void clearLocation() {
        currentLocationId = null;
        txtLocationName.setText("SL: -");
        txtStatus.setText("SL vymazána");
        updateProgressUI(0, 0);
        resetCardDisplay();
        inputLocation.requestFocus();
    }

    private void updateProgressUI(int scannedInLoc, int totalInLoc) {
        if (progressCircle != null) {
            progressCircle.setData(scannedInLoc, totalInLoc);
        }
    }

    private void resetCardDisplay() {
        txtCardId.setText("ID: -");
        txtCardName.setText("Název: -");
        txtCardPrice.setText("Cena: -");
        txtCardQuantity.setText("Množství: -");
        txtCardOl.setText("OL: -");
        cardContainer.setBackgroundColor(0xFFEEEEEE);
    }

    private void postUI(Runnable r) {
        if (getActivity() != null) getActivity().runOnUiThread(r);
    }

    private static class TerminalEnterWatcher implements TextWatcher {
        interface Callback {
            void onCode(String code);
        }

        private final Callback callback;

        TerminalEnterWatcher(Callback cb) {
            this.callback = cb;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s != null && s.length() > 0 && s.charAt(s.length() - 1) == '\n') {
                String cleaned = s.toString().trim();
                if (!cleaned.isEmpty()) callback.onCode(cleaned);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }
}

