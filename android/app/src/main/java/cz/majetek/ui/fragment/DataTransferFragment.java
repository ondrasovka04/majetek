package cz.majetek.ui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cz.majetek.MajetekApp;
import cz.majetek.R;
import cz.majetek.data.db.AppDatabase;
import cz.majetek.data.repository.DataSyncRepository;
import cz.majetek.util.BaseUrlStorage;
import cz.majetek.util.TokenStorage;

/**
 * Fragment for data transfer – download/upload/clear inventory, URL configuration.
 */
public class DataTransferFragment extends Fragment {

    private Button btnDownload;
    private Button btnUpload;
    private Button btnClear;
    private Button btnConfigureUrl;
    private ProgressBar progress;
    private TextView txtStatus;
    private final DataSyncRepository repo = new DataSyncRepository();
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_data_transfer, container, false);
        btnDownload = v.findViewById(R.id.btn_download_to_device);
        btnUpload = v.findViewById(R.id.btn_upload_from_device);
        btnClear = v.findViewById(R.id.btn_clear_inventory);
        btnConfigureUrl = v.findViewById(R.id.btn_configure_base_url);
        progress = v.findViewById(R.id.progress);
        txtStatus = v.findViewById(R.id.txt_status);

        setStatusNeutral(getString(R.string.status_ready));

        btnDownload.setOnClickListener(view -> startDownload());
        btnUpload.setOnClickListener(view -> startUpload());
        btnClear.setOnClickListener(view -> confirmClearInventory());
        btnConfigureUrl.setOnClickListener(view -> showConfigureServerDialog());
        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        repo.shutdown();
        io.shutdownNow();
    }

    /**
     * Shows a dialog to configure the backend URL and login credentials.
     */
    private void showConfigureServerDialog() {
        Context ctx = requireContext().getApplicationContext();

        EditText inputUrl = new EditText(requireContext());
        inputUrl.setText(BaseUrlStorage.getBaseUrl(ctx));
        inputUrl.setHint(getString(R.string.hint_base_url));
        inputUrl.setSingleLine(true);

        EditText inputUser = new EditText(requireContext());
        String savedUser = TokenStorage.getUsername(ctx);
        if (savedUser != null) inputUser.setText(savedUser);
        inputUser.setHint(getString(R.string.hint_username));
        inputUser.setSingleLine(true);

        EditText inputPass = new EditText(requireContext());
        String savedPass = TokenStorage.getPassword(ctx);
        if (savedPass != null) inputPass.setText(savedPass);
        inputPass.setHint(getString(R.string.hint_password));
        inputPass.setSingleLine(true);
        inputPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
        container.setPadding(pad, pad, pad, pad);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = pad / 2;
        container.addView(inputUrl, lp);
        container.addView(inputUser, new LinearLayout.LayoutParams(lp));
        container.addView(inputPass, new LinearLayout.LayoutParams(lp));

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_server_title)
                .setView(container)
                .setPositiveButton(R.string.common_set, (d, w) -> {
                    String url = inputUrl.getText() != null ? inputUrl.getText().toString().trim() : "";
                    String user = inputUser.getText() != null ? inputUser.getText().toString().trim() : "";
                    String pass = inputPass.getText() != null ? inputPass.getText().toString().trim() : "";
                    if (!BaseUrlStorage.setBaseUrl(ctx, url)) {
                        setStatusError(getString(R.string.error_invalid_base_url));
                        return;
                    }
                    TokenStorage.saveCredentials(ctx, user, pass);
                    setStatusOk(getString(R.string.status_server_configured));
                })
                .setNegativeButton(R.string.common_cancel, null)
                .show();
    }

    /**
     * Double confirmation dialog – irreversible deletion of inventory records.
     */
    private void confirmClearInventory() {
        new AlertDialog.Builder(requireContext()).setTitle(R.string.dialog_clear_inventory_title).setMessage(R.string.dialog_clear_inventory_message).setPositiveButton(R.string.common_yes, (d, w) -> new AlertDialog.Builder(requireContext()).setTitle(R.string.dialog_confirmation_title).setMessage(R.string.dialog_confirmation_message_irreversible).setPositiveButton(R.string.common_yes, (d2, w2) -> clearInventory()).setNegativeButton(R.string.common_no, null).show()).setNegativeButton(R.string.common_no, null).show();
    }

    private void clearInventory() {
        setStatusNeutral(getString(R.string.status_clearing_inventory));
        setLoading(true);
        MajetekApp app = (MajetekApp) requireActivity().getApplication();
        io.execute(() -> {
            AppDatabase db = app.getDatabase();
            db.inventoryAuditDao().clear();
            Activity a = getActivity();
            if (a == null) return;
            a.runOnUiThread(() -> {
                if (!isAdded()) return;
                setLoading(false);
                setStatusOk(getString(R.string.status_inventory_cleared));
            });
        });
    }

    /**
     * Checks WiFi/Ethernet → audit state → downloads cards from API.
     */
    private void startDownload() {
        if (isWifiDisconnected()) {
            setStatusWarn(getString(R.string.status_wifi_disconnected));
            return;
        }
        prepareDownload();
        MajetekApp app = (MajetekApp) requireActivity().getApplication();
        io.execute(() -> checkAuditState(app.getDatabase()));
    }

    private void prepareDownload() {
        setStatusNeutral(getString(R.string.status_checking_state));
        setLoading(true);
    }

    /**
     * Checks whether the device has unsynchronized data.
     */
    private void checkAuditState(AppDatabase db) {
        int auditCount = db.inventoryAuditDao().countCardsTotal();
        runOnUiThreadSafe(() -> handleAuditCount(auditCount));
    }

    private void handleAuditCount(int auditCount) {
        if (auditCount > 0) {
            setLoading(false);
            setStatusWarn(getString(R.string.status_data_present));
            return;
        }
        startRemoteDownload();
    }

    private void startRemoteDownload() {
        setStatusNeutral(getString(R.string.status_downloading_data));
        repo.downloadToDevice(requireContext().getApplicationContext(), new DataSyncRepository.Callback() {
            @Override
            public void onSuccess(int cardCount) {
                runOnUiThreadSafe(() -> {
                    setLoading(false);
                    setStatusOk(getString(R.string.status_downloaded_cards, cardCount));
                });
            }

            @Override
            public void onError(String msg) {
                runOnUiThreadSafe(() -> {
                    setLoading(false);
                    setStatusError(getString(R.string.status_error_generic, msg));
                });
            }
        });
    }

    private void runOnUiThreadSafe(Runnable r) {
        Activity a = getActivity();
        if (a != null) a.runOnUiThread(() -> {
            if (isAdded()) r.run();
        });
    }

    private void startUpload() {
        if (isWifiDisconnected()) {
            setStatusWarn(getString(R.string.status_wifi_disconnected));
            return;
        }
        setStatusNeutral(getString(R.string.status_uploading_inventory));
        setLoading(true);
        repo.uploadInventoryAudit(requireContext().getApplicationContext(), new DataSyncRepository.AuditUploadCallback() {
            @Override
            public void onSuccess(int uploadedCount) {
                runOnUiThreadSafe(() -> {
                    setLoading(false);
                    if (uploadedCount == 0) {
                        setStatusWarn(getString(R.string.status_no_data_to_upload));
                    } else {
                        setStatusOk(getString(R.string.status_uploaded_records, uploadedCount));
                    }
                });
            }

            @Override
            public void onError(String msg) {
                Activity a = getActivity();
                if (a == null) return;
                a.runOnUiThread(() -> {
                    if (!isAdded()) return;
                    setLoading(false);
                    setStatusError(getString(R.string.status_upload_error, msg));
                });
            }
        });
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnDownload.setEnabled(!loading);
        btnUpload.setEnabled(!loading);
        btnClear.setEnabled(!loading);
        btnConfigureUrl.setEnabled(!loading);
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

    private boolean isWifiDisconnected() {
        ConnectivityManager cm = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return true;
        Network network = cm.getActiveNetwork();
        if (network == null) return true;
        NetworkCapabilities caps = cm.getNetworkCapabilities(network);
        return caps == null || (!caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) && !caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
    }
}
