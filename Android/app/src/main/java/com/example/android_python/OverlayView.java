package com.example.android_python;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class OverlayView extends View {

    private Paint paint;
    private float strokeWidth = 8f;
    private float boxSizePercent = 0.17f; // Kích thước ô vuông là 15% chiều rộng/cao

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();

        // Tính toán kích thước ô vuông dựa trên kích thước View thực tế
        float size = w * boxSizePercent;

        // Vẽ 4 ô vuông tại 4 góc của View (lúc này View đã khớp 3:4)

        // Góc trái trên
        canvas.drawRect(strokeWidth, strokeWidth, size, size, paint);

        // Góc phải trên
        canvas.drawRect(w - size, strokeWidth, w - strokeWidth, size, paint);

        // Góc trái dưới
        canvas.drawRect(strokeWidth, h - size, size, h - strokeWidth, paint);

        // Góc phải dưới
        canvas.drawRect(w - size, h - size, w - strokeWidth, h - strokeWidth, paint);
    }
}