package cz.majetek.ui.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class DataTransferFragment extends Fragment {

    private Button btnDownload;
    private Button btnUpload;
    private Button btnClear;
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
        progress = v.findViewById(R.id.progress);
        txtStatus = v.findViewById(R.id.txt_status);

        setStatusNeutral(getString(R.string.status_ready));

        btnDownload.setOnClickListener(view -> startDownload());
        btnUpload.setOnClickListener(view -> startUpload());
        btnClear.setOnClickListener(view -> confirmClearInventory());
        return v;
    }

    private void confirmClearInventory() {
        new AlertDialog.Builder(requireContext()).setTitle(R.string.dialog_clear_inventory_title).setMessage(R.string.dialog_clear_inventory_message).setPositiveButton(R.string.common_yes, (d, w) -> new AlertDialog.Builder(requireContext()).setTitle(R.string.dialog_confirmation_title).setMessage(R.string.dialog_confirmation_message_irreversible).setPositiveButton(R.string.common_yes, (d2, w2) -> clearInventory()).setNegativeButton(R.string.common_no, null).show()).setNegativeButton(R.string.common_no, null).show();
    }

    private void clearInventory() {
        setStatusNeutral(getString(R.string.status_clearing_inventory));
        setLoading(true);
        io.execute(() -> {
            AppDatabase db = ((MajetekApp) requireActivity().getApplication()).getDatabase();
            db.inventoryAuditDao().clear();
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                setLoading(false);
                setStatusOk(getString(R.string.status_inventory_cleared));
            });
        });
    }

    private void startDownload() {
        if (isWifiDisconnected()) {
            setStatusWarn(getString(R.string.status_wifi_disconnected));
            return;
        }
        prepareDownload();
        io.execute(this::checkAuditState);
    }

    private void prepareDownload() {
        setStatusNeutral(getString(R.string.status_checking_state));
        setLoading(true);
    }

    private void checkAuditState() {
        AppDatabase db = ((MajetekApp) requireActivity().getApplication()).getDatabase();
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
        if (getActivity() == null) return;
        getActivity().runOnUiThread(r);
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
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
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

    private boolean isWifiDisconnected() {
        ConnectivityManager cm = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return true;
        Network network = cm.getActiveNetwork();
        if (network == null) return true;
        NetworkCapabilities caps = cm.getNetworkCapabilities(network);
        return caps == null || !caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
    }
}
