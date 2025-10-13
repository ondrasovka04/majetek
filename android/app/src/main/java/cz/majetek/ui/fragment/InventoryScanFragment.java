package cz.majetek.ui.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import cz.majetek.data.entity.InventoryAuditEntity;
import cz.majetek.data.entity.LocationEntity;
import cz.majetek.ui.view.CircularInventoryProgressView;

public class InventoryScanFragment extends Fragment {

    private EditText inputLocation, inputAsset;
    private TextView txtLocationName, txtCardId, txtCardName, txtCardPrice, txtCardQuantity, txtCardOu, txtStatus;
    private CircularInventoryProgressView progressCircle, progressCircleTotal;
    private LinearLayout cardContainer;
    private Button btnDelete, btnLeave;

    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final static Locale CZECH_LOCALE = new Locale("cs", "CZ");

    private String currentLocationId = null;
    private int lastScannedCardId = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_inventory_scan, container, false);
        bindViews(v);
        setupFocusBehavior();
        setupInputs();
        setupButtons();
        setStatusNeutral("Připraveno");
        updateLocationProgressUI(0, 0);
        refreshTotalProgress();
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
        txtCardOu = v.findViewById(R.id.txt_card_location);
        txtStatus = v.findViewById(R.id.txt_status);
        btnDelete = v.findViewById(R.id.btn_delete);
        btnLeave = v.findViewById(R.id.btn_leave);
        progressCircle = v.findViewById(R.id.progress_circle);
        cardContainer = v.findViewById(R.id.card_container);
        progressCircleTotal = v.findViewById(R.id.progress_circle_total);
    }

    private void setupFocusBehavior() {
        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        inputLocation.setShowSoftInputOnFocus(false);
        inputAsset.setShowSoftInputOnFocus(false);

        inputLocation.setOnClickListener(v -> enableAndShowIme(inputLocation));
        inputAsset.setOnClickListener(v -> enableAndShowIme(inputAsset));

        inputLocation.post(() -> {
            inputLocation.requestFocus();
            hideIme(inputLocation);
        });
    }

    private void enableAndShowIme(EditText et) {
        et.setShowSoftInputOnFocus(true);
        et.post(() -> {
            et.requestFocus();
            showIme(et);
        });
    }

    private void hideIme(View v) {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    private void showIme(View v) {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
    }

    private void setupInputs() {
        inputLocation.setOnEditorActionListener((tv, actionId, event) -> {
            boolean isEnter = (event != null && event.getAction() == KeyEvent.ACTION_DOWN && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER || event.getKeyCode() == KeyEvent.KEYCODE_TAB)) || actionId == EditorInfo.IME_ACTION_DONE;
            if (isEnter) {
                handleLocationScanned(tv.getText().toString().trim());
                return true;
            }
            return false;
        });
        inputLocation.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_TAB)) {
                handleLocationScanned(inputLocation.getText().toString().trim());
                return true;
            }
            return false;
        });

        inputAsset.setOnEditorActionListener((tv, actionId, event) -> {
            boolean isEnter = (event != null && event.getAction() == KeyEvent.ACTION_DOWN && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER || event.getKeyCode() == KeyEvent.KEYCODE_TAB)) || actionId == EditorInfo.IME_ACTION_DONE;
            if (isEnter) {
                handleAssetScanned(tv.getText().toString().trim());
                return true;
            }
            return false;
        });
        inputAsset.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_TAB)) {
                handleAssetScanned(inputAsset.getText().toString().trim());
                return true;
            }
            return false;
        });
    }

    private void setupButtons() {
        btnDelete.setOnClickListener(v -> {
            if (lastScannedCardId <= 0) {
                setStatusWarn("Žádný majetek k smazání");
                return;
            }
            new AlertDialog.Builder(requireContext()).setTitle("Smazat").setMessage("Opravdu odstranit tento majetek z inventury?").setPositiveButton("Ano", (d, w) -> deleteLast()).setNegativeButton("Ne", null).show();
        });

        btnLeave.setOnClickListener(v -> {
            if (currentLocationId == null) {
                setStatusWarn("Nebylo vybráno umístění");
                return;
            }
            new AlertDialog.Builder(requireContext()).setTitle("Odejít z místnosti").setMessage("Opravdu odejít z místnosti?").setPositiveButton("Ano", (d, w) -> clearLocation()).setNegativeButton("Ne", null).show();
        });
    }

    private void handleLocationScanned(String code) {
        if (code.isEmpty()) {
            inputLocation.requestFocus();
            return;
        }
        if (currentLocationId != null) {
            inputLocation.setText("");
            inputAsset.requestFocus();
            return;
        }

        inputLocation.setText("");

        if (!code.startsWith("L") || code.length() == 1) {
            setStatusWarn("Kód umístění musí začínat 'L'");
            inputLocation.requestFocus();
            return;
        }
        String locId = code.substring(1);

        io.execute(() -> {
            AppDatabase db = ((MajetekApp) requireActivity().getApplication()).getDatabase();
            LocationEntity le = db.locationDao().getById(locId);
            if (le == null) {
                postUI(() -> {
                    setStatusError("Umístění nenalezeno: " + locId);
                    inputLocation.requestFocus();
                });
                return;
            }

            int scannedInLoc = db.inventoryAuditDao().countCardsInLocation(locId);
            int totalInLoc = db.cardDao().countByLocation(locId);
            int scannedTotal = db.inventoryAuditDao().countCardsTotal();
            int totalAll = db.cardDao().countAll();

            postUI(() -> {
                currentLocationId = locId;
                txtLocationName.setText("SU: " + locId + " – " + (le.name != null ? le.name : ""));
                setStatusNeutral("Načteno umístění: L" + locId + " – naskenujte majetek");
                updateLocationProgressUI(scannedInLoc, totalInLoc);
                updateTotalProgressUI(scannedTotal, totalAll);
                inputLocation.setEnabled(false);
                inputAsset.requestFocus();
            });
        });
    }

    private void handleAssetScanned(String code) {
        if (code.isEmpty()) {
            inputAsset.requestFocus();
            return;
        }
        if (code.startsWith("L")) {
            setStatusWarn("Sken umístění patří do horního pole");
            inputAsset.setText("");
            inputLocation.requestFocus();
            return;
        }

        if (currentLocationId == null) {
            setStatusWarn("Nejdříve naskenujte umístění (SU)");
            inputAsset.setText("");
            inputLocation.requestFocus();
            return;
        }

        inputAsset.setText("");

        final int cardId;
        try {
            cardId = Integer.parseInt(code);
        } catch (NumberFormatException e) {
            showUnknown(code);
            refreshTotalProgress();
            return;
        }

        io.execute(() -> {
            AppDatabase db = ((MajetekApp) requireActivity().getApplication()).getDatabase();
            CardEntity card = db.cardDao().getById(cardId);
            long now = System.currentTimeMillis();
            db.inventoryAuditDao().upsert(new InventoryAuditEntity(cardId, currentLocationId, now));

            int scannedInLoc = db.inventoryAuditDao().countCardsInLocation(currentLocationId);
            int totalInLoc = db.cardDao().countByLocation(currentLocationId);
            int scannedTotal = db.inventoryAuditDao().countCardsTotal();
            int totalAll = db.cardDao().countAll();

            if (card == null) {
                postUI(() -> {
                    showUnknown(String.valueOf(cardId));
                    updateLocationProgressUI(scannedInLoc, totalInLoc);
                    updateTotalProgressUI(scannedTotal, totalAll);
                });
            } else {
                postUI(() -> {
                    showCard(card);
                    updateLocationProgressUI(scannedInLoc, totalInLoc);
                    updateTotalProgressUI(scannedTotal, totalAll);
                });
            }
        });
    }

    private void showCard(CardEntity c) {
        lastScannedCardId = c.id;

        String idStr = String.format(Locale.US, "%04d", c.id);
        txtCardId.setText(idStr);
        txtCardName.setText(c.name != null ? c.name : "");
        txtCardPrice.setText(String.format(CZECH_LOCALE, "%,.2f", c.price));
        txtCardQuantity.setText(String.valueOf((int) c.quantity));
        txtCardOu.setText(c.locationId != null ? c.locationId : "");

        if (currentLocationId != null) {
            if (c.locationId == null) {
                cardContainer.setBackgroundColor(0xFFFFCDD2);
                setStatusError("Majetek očekáván v jiném umístění");
            } else if (c.locationId.equals(currentLocationId)) {
                cardContainer.setBackgroundColor(0xFFC8E6C9);
                setStatusOk("Uloženo");
            } else {
                cardContainer.setBackgroundColor(0xFFFFE0B2);
                setStatusWarn("Majetek očekáván v jiném umístění");
            }
        } else {
            setStatusNeutral("Připraveno");
        }
        inputAsset.requestFocus();
    }

    private void showUnknown(String code) {
        lastScannedCardId = -1;
        txtCardId.setText("");
        txtCardName.setText("");
        txtCardPrice.setText("");
        txtCardQuantity.setText("");
        txtCardOu.setText("");
        cardContainer.setBackgroundColor(0xFFFFCDD2);
        setStatusError("Neznámé ID majetku: " + code);
        inputLocation.requestFocus();
    }

    private void deleteLast() {
        if (lastScannedCardId <= 0) return;
        io.execute(() -> {
            AppDatabase db = ((MajetekApp) requireActivity().getApplication()).getDatabase();
            db.inventoryAuditDao().deleteByCardId(lastScannedCardId);
            int scannedInLoc = currentLocationId != null ? db.inventoryAuditDao().countCardsInLocation(currentLocationId) : 0;
            int totalInLoc = currentLocationId != null ? db.cardDao().countByLocation(currentLocationId) : 0;
            int scannedTotal = db.inventoryAuditDao().countCardsTotal();
            int totalAll = db.cardDao().countAll();
            postUI(() -> {
                lastScannedCardId = -1;
                resetCardDisplay();
                updateLocationProgressUI(scannedInLoc, totalInLoc);
                updateTotalProgressUI(scannedTotal, totalAll);
                setStatusNeutral("Záznam odstraněn");
                inputAsset.requestFocus();
            });
        });
    }

    private void clearLocation() {
        currentLocationId = null;
        txtLocationName.setText("SU: ");
        updateLocationProgressUI(0, 0);
        resetCardDisplay();
        inputLocation.setEnabled(true);
        inputLocation.requestFocus();
        setStatusNeutral("Připraveno");
    }

    private void updateLocationProgressUI(int scannedInLoc, int totalInLoc) {
        if (progressCircle != null) {
            progressCircle.setData(scannedInLoc, totalInLoc);
        }
    }

    private void updateTotalProgressUI(int scannedTotal, int totalAll) {
        if (progressCircleTotal != null) {
            progressCircleTotal.setData(scannedTotal, totalAll);
        }
    }

    private void refreshTotalProgress() {
        io.execute(() -> {
            AppDatabase db = ((MajetekApp) requireActivity().getApplication()).getDatabase();
            int scannedTotal = db.inventoryAuditDao().countCardsTotal();
            int totalAll = db.cardDao().countAll();
            postUI(() -> updateTotalProgressUI(scannedTotal, totalAll));
        });
    }

    private void resetCardDisplay() {
        txtCardId.setText("");
        txtCardName.setText("");
        txtCardPrice.setText("");
        txtCardQuantity.setText("");
        txtCardOu.setText("");
        cardContainer.setBackgroundColor(0xFFEEEEEE);
    }

    private void postUI(Runnable r) {
        if (getActivity() != null) getActivity().runOnUiThread(r);
    }

    private void setStatusOk(String msg) {
        txtStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.statusOk));
        txtStatus.setText(msg);
    }

    private void setStatusWarn(String msg) {
        txtStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.statusWarn));
        txtStatus.setText(msg);
    }

    private void setStatusError(String msg) {
        txtStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.statusError));
        txtStatus.setText(msg);
    }

    private void setStatusNeutral(String msg) {
        txtStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.statusNeutral));
        txtStatus.setText(msg);
    }
}
