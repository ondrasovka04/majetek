package cz.majetek.util;

import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import java.util.function.Consumer;

/**
 * Attaches dual listeners (EditorAction + Key) to an EditText so that
 * hardware scanner input (Enter/Tab) is reliably captured.
 */
public final class ScanInputHelper {

    private ScanInputHelper() {
    }

    /**
     * Registers both {@link TextView.OnEditorActionListener}
     * and {@link View.OnKeyListener} on the given input field.
     * The handler is invoked with the trimmed text whenever Enter or Tab is detected.
     */
    public static void attachScanListeners(EditText input, Consumer<String> handler) {
        input.setOnEditorActionListener((tv, actionId, event) -> {
            if (isConfirmAction(event, actionId)) {
                handler.accept(tv.getText().toString().trim());
                return true;
            }
            return false;
        });
        input.setOnKeyListener((v, keyCode, event) -> {
            if (isHardwareEnter(event, keyCode)) {
                handler.accept(input.getText().toString().trim());
                return true;
            }
            return false;
        });
    }

    private static boolean isConfirmAction(KeyEvent event, int actionId) {
        return (event != null
                && event.getAction() == KeyEvent.ACTION_DOWN
                && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                || event.getKeyCode() == KeyEvent.KEYCODE_TAB))
                || actionId == EditorInfo.IME_ACTION_DONE;
    }

    private static boolean isHardwareEnter(KeyEvent event, int keyCode) {
        return event.getAction() == KeyEvent.ACTION_DOWN
                && (keyCode == KeyEvent.KEYCODE_ENTER
                || keyCode == KeyEvent.KEYCODE_TAB);
    }
}
