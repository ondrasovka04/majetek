package cz.majetek.ui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cz.majetek.MajetekApp;
import cz.majetek.R;
import cz.majetek.data.db.AppDatabase;
import cz.majetek.data.db.LocationCardRow;
import cz.majetek.data.entity.CardEntity;
import cz.majetek.data.entity.InventoryAuditEntity;
import cz.majetek.data.entity.LocationEntity;
import cz.majetek.ui.view.CircularInventoryProgressView;
import cz.majetek.util.LocationFormat;
import cz.majetek.util.ScanInputHelper;

/**
 * Fragment for inventory – scan location, then assets with progress indication.
 */
public class InventoryScanFragment extends Fragment {

    private EditText inputLocation;
    private EditText inputAsset;
    private TextView txtLocationName;
    private TextView txtCardId;
    private TextView txtCardName;
    private TextView txtCardPrice;
    private TextView txtCardQuantity;
    private TextView txtCardOu;
    private TextView txtStatus;
    private CircularInventoryProgressView progressCircle;
    private CircularInventoryProgressView progressCircleTotal;
    private LinearLayout cardContainer;
    private Button btnDelete;
    private Button btnLeave;

    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private static final Locale CZECH_LOCALE = Locale.forLanguageTag("cs-CZ");

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
        setStatusNeutral(getString(R.string.status_ready));
        updateLocationProgressUI(0, 0);
        refreshTotalProgress();
        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        io.shutdownNow();
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
        ScanInputHelper.attachScanListeners(inputLocation, this::handleLocationScanned);
        ScanInputHelper.attachScanListeners(inputAsset, this::handleAssetScanned);
    }

    private void setupButtons() {
        progressCircle.setOnClickListener(v -> showLocationCards());

        btnDelete.setOnClickListener(v -> {
            if (lastScannedCardId <= 0) {
                setStatusWarn(getString(R.string.warn_no_card_to_delete));
                return;
            }
            new AlertDialog.Builder(requireContext()).setTitle(R.string.dialog_delete_title).setMessage(R.string.dialog_delete_message).setPositiveButton(R.string.common_yes, (d, w) -> deleteLast()).setNegativeButton(R.string.common_no, null).show();
        });

        btnLeave.setOnClickListener(v -> {
            if (currentLocationId == null) {
                setStatusWarn(getString(R.string.warn_no_location_selected));
                return;
            }
            new AlertDialog.Builder(requireContext()).setTitle(R.string.dialog_leave_location_title).setMessage(R.string.dialog_leave_location_message).setPositiveButton(R.string.common_yes, (d, w) -> clearLocation()).setNegativeButton(R.string.common_no, null).show();
        });
    }

    /**
     * Handles scanned location code – L-prefix validation, DB lookup, context setup.
     */
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
            setStatusWarn(getString(R.string.warn_location_code_format));
            inputLocation.requestFocus();
            return;
        }
        String locId = code.substring(1);

        MajetekApp app = (MajetekApp) requireActivity().getApplication();
        io.execute(() -> {
            AppDatabase db = app.getDatabase();
            LocationEntity le = db.locationDao().getById(locId);
            if (le == null) {
                postUI(() -> {
                    setStatusError(getString(R.string.error_location_not_found, locId));
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
                txtLocationName.setText(getString(R.string.format_su_with_name, locId, le.getName() != null ? le.getName() : ""));
                setStatusNeutral(getString(R.string.status_location_scanned, locId));
                updateLocationProgressUI(scannedInLoc, totalInLoc);
                updateTotalProgressUI(scannedTotal, totalAll);
                inputLocation.setEnabled(false);
                inputAsset.requestFocus();
            });
        });
    }

    /**
     * Handles scanned asset – inventory audit record upsert, progress update.
     */
    private void handleAssetScanned(String code) {
        if (code.isEmpty()) {
            inputAsset.requestFocus();
            return;
        }
        if (code.startsWith("L")) {
            setStatusWarn(getString(R.string.warn_location_scan_in_asset_field));
            inputAsset.setText("");
            inputLocation.requestFocus();
            return;
        }

        if (currentLocationId == null) {
            setStatusWarn(getString(R.string.warn_scan_location_first));
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

        MajetekApp app = (MajetekApp) requireActivity().getApplication();
        io.execute(() -> {
            AppDatabase db = app.getDatabase();
            CardEntity card = db.cardDao().getById(cardId);
            LocationEntity expectedLocation = null;
            if (card != null) {
                long now = System.currentTimeMillis();
                db.inventoryAuditDao().upsert(new InventoryAuditEntity(cardId, currentLocationId, now));
                if (card.getLocationId() != null) {
                    expectedLocation = db.locationDao().getById(card.getLocationId());
                }
            }
            final String expectedLocationName = expectedLocation != null ? expectedLocation.getName() : null;

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
                    showCard(card, expectedLocationName);
                    updateLocationProgressUI(scannedInLoc, totalInLoc);
                    updateTotalProgressUI(scannedTotal, totalAll);
                });
            }
        });
    }

    private void showCard(CardEntity c, @Nullable String locationName) {
        if (!isAdded()) return;
        lastScannedCardId = c.getId();

        String idStr = String.format(Locale.US, "%04d", c.getId());
        txtCardId.setText(idStr);
        txtCardName.setText(c.getName() != null ? c.getName() : "");
        txtCardPrice.setText(String.format(CZECH_LOCALE, "%,.2f", c.getPrice()));
        txtCardQuantity.setText(String.valueOf((int) c.getQuantity()));
        txtCardOu.setText(LocationFormat.format(requireContext(), c.getLocationId(), locationName));

        if (currentLocationId != null) {
            if (c.getLocationId() == null) {
                cardContainer.setBackgroundColor(color(R.color.cardBgError));
                setStatusError(getString(R.string.error_asset_expected_elsewhere));
            } else if (c.getLocationId().equals(currentLocationId)) {
                cardContainer.setBackgroundColor(color(R.color.cardBgOk));
                setStatusOk(getString(R.string.status_saved));
            } else {
                cardContainer.setBackgroundColor(color(R.color.cardBgWarn));
                setStatusWarn(getString(R.string.error_asset_expected_elsewhere));
            }
        } else {
            setStatusNeutral(getString(R.string.status_ready));
        }
        inputAsset.requestFocus();
    }

    private void showUnknown(String code) {
        if (!isAdded()) return;
        lastScannedCardId = -1;
        txtCardId.setText("");
        txtCardName.setText("");
        txtCardPrice.setText("");
        txtCardQuantity.setText("");
        txtCardOu.setText("");
        cardContainer.setBackgroundColor(color(R.color.cardBgError));
        String displayCode = code;
        try {
            displayCode = String.format(Locale.getDefault(), "%04d", Integer.parseInt(code));
        } catch (NumberFormatException ignored) {
            // keep original code if parsing fails
        }
        setStatusError(getString(R.string.error_unknown_asset_id, displayCode));
        if (inputLocation.isEnabled()) {
            inputLocation.requestFocus();
        } else {
            inputAsset.requestFocus();
        }
    }

    private void deleteLast() {
        if (lastScannedCardId <= 0) return;
        MajetekApp app = (MajetekApp) requireActivity().getApplication();
        io.execute(() -> {
            AppDatabase db = app.getDatabase();
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
                setStatusNeutral(getString(R.string.status_record_deleted));
                inputAsset.requestFocus();
            });
        });
    }

    private void clearLocation() {
        currentLocationId = null;
        txtLocationName.setText(getString(R.string.label_su_prefix));
        updateLocationProgressUI(0, 0);
        resetCardDisplay();
        inputLocation.setEnabled(true);
        inputLocation.requestFocus();
        setStatusNeutral(getString(R.string.status_ready));
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
        MajetekApp app = (MajetekApp) requireActivity().getApplication();
        io.execute(() -> {
            AppDatabase db = app.getDatabase();
            int scannedTotal = db.inventoryAuditDao().countCardsTotal();
            int totalAll = db.cardDao().countAll();
            postUI(() -> updateTotalProgressUI(scannedTotal, totalAll));
        });
    }

    /**
     * Shows cards of the current location – scanned correctly (green), location mismatch (orange), not found yet (red).
     */
    private void showLocationCards() {
        if (currentLocationId == null) {
            setStatusWarn(getString(R.string.warn_no_location_selected));
            return;
        }
        final String locId = currentLocationId;
        MajetekApp app = (MajetekApp) requireActivity().getApplication();
        io.execute(() -> {
            List<LocationCardRow> rows = app.getDatabase().inventoryAuditDao().listCardsForLocation(locId);
            rows.sort(Comparator.comparing((LocationCardRow row) -> row.statusFor(locId)).thenComparingInt(row -> row.cardId));
            postUI(() -> showLocationCardsDialog(locId, rows));
        });
    }

    private void showLocationCardsDialog(String locId, List<LocationCardRow> rows) {
        if (!isAdded()) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext()).setTitle(getString(R.string.dialog_location_cards_title, locId)).setPositiveButton(R.string.common_close, null).setOnDismissListener(d -> inputAsset.requestFocus());
        if (rows.isEmpty()) {
            builder.setMessage(R.string.location_cards_empty);
        } else {
            ListView list = new ListView(requireContext());
            int padH = dp(20);
            list.setPadding(padH, dp(12), padH, 0);
            list.setClipToPadding(false);
            list.setDivider(new ColorDrawable(Color.TRANSPARENT));
            list.setDividerHeight(dp(4));
            list.setAdapter(new LocationCardAdapter(locId, rows));
            builder.setView(list);
        }
        builder.show();
    }

    private class LocationCardAdapter extends ArrayAdapter<LocationCardRow> {
        private final String locId;

        LocationCardAdapter(String locId, List<LocationCardRow> rows) {
            super(requireContext(), 0, rows);
            this.locId = locId;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view = convertView != null ? convertView : LayoutInflater.from(getContext()).inflate(R.layout.item_location_card, parent, false);
            LocationCardRow row = getItem(position);
            if (row == null) return view;

            TextView title = view.findViewById(R.id.txt_row_title);
            TextView detail = view.findViewById(R.id.txt_row_detail);
            title.setText(String.format(Locale.US, "%04d – %s", row.cardId, row.name != null ? row.name : ""));

            String detailText = null;
            switch (row.statusFor(locId)) {
                case OK -> view.setBackgroundColor(color(R.color.cardBgOk));
                case MISSING -> view.setBackgroundColor(color(R.color.cardBgError));
                case MISPLACED -> {
                    view.setBackgroundColor(color(R.color.cardBgWarn));
                    detailText = locId.equals(row.realLocationId)
                            ? getString(R.string.location_card_belongs_to, LocationFormat.format(requireContext(), row.expectedLocationId, row.expectedLocationName))
                            : getString(R.string.location_card_found_in, LocationFormat.format(requireContext(), row.realLocationId, row.realLocationName));
                }
            }
            detail.setText(detailText);
            detail.setVisibility(detailText != null ? View.VISIBLE : View.GONE);
            return view;
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private int color(int resId) {
        return ContextCompat.getColor(requireContext(), resId);
    }

    private void resetCardDisplay() {
        txtCardId.setText("");
        txtCardName.setText("");
        txtCardPrice.setText("");
        txtCardQuantity.setText("");
        txtCardOu.setText("");
        cardContainer.setBackgroundColor(color(R.color.cardBgNeutral));
    }

    private void postUI(Runnable r) {
        Activity a = getActivity();
        if (a != null) a.runOnUiThread(() -> {
            if (isAdded()) r.run();
        });
    }

    private void setStatusOk(String msg) {
        if (!isAdded()) return;
        txtStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.statusOk));
        txtStatus.setText(msg);
    }

    private void setStatusWarn(String msg) {
        if (!isAdded()) return;
        txtStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.statusWarn));
        txtStatus.setText(msg);
    }

    private void setStatusError(String msg) {
        if (!isAdded()) return;
        txtStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.statusError));
        txtStatus.setText(msg);
    }

    private void setStatusNeutral(String msg) {
        if (!isAdded()) return;
        txtStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.statusNeutral));
        txtStatus.setText(msg);
    }
}
