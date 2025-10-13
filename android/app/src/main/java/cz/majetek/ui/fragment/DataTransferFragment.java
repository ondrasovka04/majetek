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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

        btnDownload.setOnClickListener(view -> startDownload());
        btnUpload.setOnClickListener(view -> startUpload());
        btnClear.setOnClickListener(view -> confirmClearInventory());
        return v;
    }

    private void confirmClearInventory() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Vymazat inventuru")
                .setMessage("Opravdu smazat všechna inventurní data?")
                .setPositiveButton("Ano", (d, w) -> clearInventory())
                .setNegativeButton("Ne", null)
                .show();
    }

    private void clearInventory() {
        setLoading(true, "Mažu inventuru...");
        io.execute(() -> {
            AppDatabase db = ((MajetekApp) requireActivity().getApplication()).getDatabase();
            db.inventoryAuditDao().clear();
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                setLoading(false, "Inventurní data vymazána");
                Toast.makeText(getContext(), "Inventurní data vymazána", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void startDownload() {
        if (isWifiDisconnected()) {
            Toast.makeText(getContext(), "Není připojení k Wi‑Fi", Toast.LENGTH_SHORT).show();
            return;
        }
        setLoading(true, "Stahuji data...");
        repo.downloadToDevice(requireContext().getApplicationContext(), new DataSyncRepository.Callback() {
            @Override
            public void onSuccess(int cardCount) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    setLoading(false, "Staženo " + cardCount + " karet");
                    Toast.makeText(getContext(), "Staženo: " + cardCount + " karet", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String msg) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    setLoading(false, "Chyba: " + msg);
                    Toast.makeText(getContext(), "Chyba: " + msg, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void startUpload() {
        if (isWifiDisconnected()) {
            Toast.makeText(getContext(), "Není připojení k Wi‑Fi", Toast.LENGTH_SHORT).show();
            return;
        }
        setLoading(true, "Odesílám inventuru...");
        repo.uploadInventoryAudit(requireContext().getApplicationContext(), new DataSyncRepository.AuditUploadCallback() {
            @Override
            public void onSuccess(int uploadedCount) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    String msg = uploadedCount == 0 ? "Žádná data k odeslání" : ("Odesláno " + uploadedCount + " záznamů");
                    setLoading(false, msg);
                    Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String msg) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    setLoading(false, "Chyba odeslání: " + msg);
                    Toast.makeText(getContext(), "Chyba: " + msg, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void setLoading(boolean loading, String status) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnDownload.setEnabled(!loading);
        btnUpload.setEnabled(!loading);
        btnClear.setEnabled(!loading);
        txtStatus.setText(status);
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
