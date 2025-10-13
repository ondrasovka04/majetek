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

        setStatusNeutral("Připraveno");

        btnDownload.setOnClickListener(view -> startDownload());
        btnUpload.setOnClickListener(view -> startUpload());
        btnClear.setOnClickListener(view -> confirmClearInventory());
        return v;
    }

    private void confirmClearInventory() {
        new AlertDialog.Builder(requireContext()).setTitle("Vymazat inventuru").setMessage("Opravdu smazat všechna nasnímaná data ve čtečce?").setPositiveButton("Ano", (d, w) -> new AlertDialog.Builder(requireContext()).setTitle("Potvrzení").setMessage("Tato akce je nevratná. Pokračovat?").setPositiveButton("Ano", (d2, w2) -> clearInventory()).setNegativeButton("Ne", null).show()).setNegativeButton("Ne", null).show();
    }

    private void clearInventory() {
        setStatusNeutral("Mažu inventuru...");
        setLoading(true);
        io.execute(() -> {
            AppDatabase db = ((MajetekApp) requireActivity().getApplication()).getDatabase();
            db.inventoryAuditDao().clear();
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                setLoading(false);
                setStatusOk("Inventurní data vymazána");
            });
        });
    }

    private void startDownload() {
        if (isWifiDisconnected()) {
            setStatusWarn("Není připojení k Wi-Fi");
            return;
        }
        setStatusNeutral("Kontroluji stav...");
        setLoading(true);
        io.execute(() -> {
            AppDatabase db = ((MajetekApp) requireActivity().getApplication()).getDatabase();
            int auditCount = db.inventoryAuditDao().countCardsTotal();
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                if (auditCount > 0) {
                    setLoading(false);
                    setStatusWarn("Ve čtečce jsou nasnímaná data, nejprve je odešlete nebo smažte.");
                } else {
                    setStatusNeutral("Stahuji data...");
                    repo.downloadToDevice(requireContext().getApplicationContext(), new DataSyncRepository.Callback() {
                        @Override
                        public void onSuccess(int cardCount) {
                            if (getActivity() == null) return;
                            getActivity().runOnUiThread(() -> {
                                setLoading(false);
                                setStatusOk("Načteno " + cardCount + " karet");

                            });
                        }

                        @Override
                        public void onError(String msg) {
                            if (getActivity() == null) return;
                            getActivity().runOnUiThread(() -> {
                                setLoading(false);
                                setStatusError("Chyba: " + msg);
                            });
                        }
                    });
                }
            });
        });
    }

    private void startUpload() {
        if (isWifiDisconnected()) {
            setStatusWarn("Není připojení k Wi-Fi");
            return;
        }
        setStatusNeutral("Odesílám inventuru...");
        setLoading(true);
        repo.uploadInventoryAudit(requireContext().getApplicationContext(), new DataSyncRepository.AuditUploadCallback() {
            @Override
            public void onSuccess(int uploadedCount) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    setLoading(false);
                    if (uploadedCount == 0) {
                        setStatusWarn("Žádná data k odeslání");
                    } else {
                        setStatusOk("Odesláno " + uploadedCount + " záznamů");
                    }
                });
            }

            @Override
            public void onError(String msg) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    setLoading(false);
                    setStatusError("Chyba odeslání: " + msg);
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
