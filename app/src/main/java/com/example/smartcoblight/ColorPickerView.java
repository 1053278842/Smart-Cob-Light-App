package com.example.smartcoblight;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class ColorPickerView extends View {

    private Paint colorWheelPaint;
    private Paint centerPaint;
    private Paint borderPaint;
    private Paint selectorPaint;
    private int centerX, centerY;
    private int radius;
    private int selectedColor = Color.RED;
    private OnColorSelectedListener colorSelectedListener;

    // 色环颜色数组 - 更丰富的颜色集，用于创建平滑渐变
    private int[] colorWheelColors = {
            Color.RED,        // 红色
            Color.MAGENTA,    // 洋红色
            Color.BLUE,       // 蓝色
            Color.CYAN,       // 青色
            Color.GREEN,      // 绿色
            Color.YELLOW,     // 黄色
            Color.RED         // 回到红色，形成闭环
    };

    public ColorPickerView(Context context) {
        super(context);
        init();
    }

    public ColorPickerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ColorPickerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 初始化色环画笔
        colorWheelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        colorWheelPaint.setStyle(Paint.Style.FILL);

        // 初始化中心画笔
        centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerPaint.setStyle(Paint.Style.FILL);

        // 初始化边框画笔
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4);
        borderPaint.setColor(Color.WHITE);

        // 初始化选择器画笔
        selectorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectorPaint.setStyle(Paint.Style.STROKE);
        selectorPaint.setStrokeWidth(3);
        selectorPaint.setColor(Color.WHITE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w / 2;
        centerY = h / 2;
        radius = Math.min(w, h) / 2 - 20;

        // 设置色环的渐变着色器
        Shader sweepGradient = new SweepGradient(centerX, centerY, colorWheelColors, null);
        colorWheelPaint.setShader(sweepGradient);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 绘制色环
        canvas.drawCircle(centerX, centerY, radius, colorWheelPaint);

        // 绘制中心白色圆形作为亮度调节
        int centerRadius = (int) (radius * 0.4f);
        RadialGradient radialGradient = new RadialGradient(
                centerX, centerY, centerRadius,
                selectedColor, Color.WHITE,
                Shader.TileMode.CLAMP
        );
        centerPaint.setShader(radialGradient);
        canvas.drawCircle(centerX, centerY, centerRadius, centerPaint);

        // 绘制边框
        canvas.drawCircle(centerX, centerY, radius, borderPaint);
        canvas.drawCircle(centerX, centerY, centerRadius, borderPaint);

        // 绘制选中颜色的指示器
        drawColorSelector(canvas);
    }

    private void drawColorSelector(Canvas canvas) {
        // 计算选中颜色在色环上的位置
        float hue = Color.red(selectedColor) / 255f; // 简化示例，实际应计算色相
        float angle = hue * 360;

        // 计算指示器位置
        int indicatorRadius = (int) (radius * 1.05f);
        double rad = Math.toRadians(angle);
        float x = (float) (centerX + indicatorRadius * Math.cos(rad));
        float y = (float) (centerY + indicatorRadius * Math.sin(rad));

        // 绘制指示器
        canvas.drawCircle(x, y, 10, selectorPaint);
        canvas.drawCircle(x, y, 6, centerPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN ||
                event.getAction() == MotionEvent.ACTION_MOVE) {

            float x = event.getX();
            float y = event.getY();

            // 计算触摸点距离中心的距离
            float distance = (float) Math.sqrt((x - centerX) * (x - centerX) + (y - centerY) * (y - centerY));
            int centerRadius = (int) (radius * 0.4f);

            // 检查是否在色环范围内
            if (distance >= centerRadius && distance <= radius) {
                // 计算角度
                double angle = Math.atan2(y - centerY, x - centerX);
                if (angle < 0) angle += 2 * Math.PI;

                // 从角度获取颜色
                selectedColor = getColorFromAngle(angle);

                // 通知监听器
                if (colorSelectedListener != null) {
                    colorSelectedListener.onColorSelected(selectedColor);
                }

                invalidate();
                return true;
            }
            // 检查是否在中心区域（用于调节亮度）
            else if (distance < centerRadius) {
                // 计算亮度比例 (中心最亮，边缘按色环颜色)
                float brightness = 1 - (distance / centerRadius);
                selectedColor = adjustColorBrightness(selectedColor, brightness);

                if (colorSelectedListener != null) {
                    colorSelectedListener.onColorSelected(selectedColor);
                }

                invalidate();
                return true;
            }
        }
        return super.onTouchEvent(event);
    }

    // 根据角度计算颜色
    private int getColorFromAngle(double angle) {
        float hue = (float) (angle / (2 * Math.PI)); // 0-1范围
        return Color.HSVToColor(new float[]{hue * 360, 1f, 1f});
    }

    // 调整颜色亮度
    private int adjustColorBrightness(int color, float brightness) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] = brightness; // 调整明度 (0-1)
        return Color.HSVToColor(hsv);
    }

    public void setOnColorSelectedListener(OnColorSelectedListener listener) {
        this.colorSelectedListener = listener;
    }

    public int getSelectedColor() {
        return selectedColor;
    }

    public void setSelectedColor(int color) {
        this.selectedColor = color;
        invalidate();
    }

    public interface OnColorSelectedListener {
        void onColorSelected(int color);
    }
}
