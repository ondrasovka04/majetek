package cz.majetek.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CircularInventoryProgressView extends View {
    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcRect = new RectF();

    private int scanned = 0;
    private int total = 0;
    private static final float STROKE_WIDTH = 14f;

    public CircularInventoryProgressView(Context context) {
        super(context);
        init();
    }

    public CircularInventoryProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircularInventoryProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        bgPaint.setStyle(Paint.Style.STROKE);
        bgPaint.setStrokeCap(Paint.Cap.ROUND);
        bgPaint.setStrokeWidth(STROKE_WIDTH);
        bgPaint.setColor(0xFFDDDDDD);

        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setStrokeWidth(STROKE_WIDTH);
        progressPaint.setColor(0xFF9E9E9E);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(0xFF333333);
        textPaint.setTextSize(36f);
    }

    public void setData(int scanned, int total) {
        if (scanned < 0) scanned = 0;
        if (total < 0) total = 0;
        this.scanned = scanned;
        this.total = total;
        setContentDescription(scanned + " / " + total);
        invalidate();
    }

    private int chooseColor(float percent) {
        if (percent < 25f) return 0xFFF44336;
        if (percent >= 75f) return 0xFF4CAF50;
        return 0xFFFF9800;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desired = 200;
        int w = resolveSize(desired, widthMeasureSpec);
        int h = resolveSize(desired, heightMeasureSpec);
        int size = Math.min(w, h);
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        float pad = STROKE_WIDTH / 2f + 4f;
        arcRect.set(pad, pad, getWidth() - pad, getHeight() - pad);

        canvas.drawOval(arcRect, bgPaint);

        float sweep = 0f;
        float percent = 0f;
        if (total > 0) {
            percent = (scanned * 100f) / total;
            sweep = 360f * scanned / total;
        }
        progressPaint.setColor(chooseColor(percent));
        if (sweep > 0f) {
            canvas.drawArc(arcRect, -90f, sweep, false, progressPaint);
        }

        String text = scanned + "/" + total;
        textPaint.setTextSize(getWidth() * 0.22f);
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float cy = getHeight() / 2f - (fm.ascent + fm.descent) / 2f;
        canvas.drawText(text, getWidth() / 2f, cy, textPaint);
    }
}

