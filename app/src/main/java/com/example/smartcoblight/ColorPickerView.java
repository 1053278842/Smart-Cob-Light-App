package com.example.smartcoblight;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class ColorPickerView extends View {
    
    private Paint colorPaint;
    private Paint centerPaint;
    private Paint borderPaint;
    private int centerX, centerY;
    private int radius;
    private int selectedColor = Color.RED;
    private OnColorSelectedListener colorSelectedListener;
    
    // 色盘颜色数组
    private int[] colors = {
        Color.RED, Color.MAGENTA, Color.BLUE, Color.CYAN,
        Color.GREEN, Color.YELLOW, Color.RED
    };
    
    public interface OnColorSelectedListener {
        void onColorSelected(int color);
    }
    
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
        colorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4);
        borderPaint.setColor(Color.WHITE);
        
        centerPaint.setStyle(Paint.Style.FILL);
        centerPaint.setColor(Color.TRANSPARENT);
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w / 2;
        centerY = h / 2;
        radius = Math.min(w, h) / 2 - 20;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // 绘制色盘
        drawColorWheel(canvas);
        
        // 绘制中心透明区域
        canvas.drawCircle(centerX, centerY, radius * 0.4f, centerPaint);
        
        // 绘制边框
        canvas.drawCircle(centerX, centerY, radius, borderPaint);
        canvas.drawCircle(centerX, centerY, radius * 0.4f, borderPaint);
    }
    
    private void drawColorWheel(Canvas canvas) {
        int numColors = colors.length - 1; // 减1因为首尾颜色相同
        float angleStep = 360f / numColors;
        
        for (int i = 0; i < numColors; i++) {
            float startAngle = i * angleStep;
            float endAngle = (i + 1) * angleStep;
            
            // 创建扇形路径
            Path path = new Path();
            path.moveTo(centerX, centerY);
            path.arcTo(centerX - radius, centerY - radius, 
                      centerX + radius, centerY + radius, 
                      startAngle, endAngle - startAngle, false);
            path.close();
            
            // 设置颜色
            colorPaint.setColor(colors[i]);
            colorPaint.setStyle(Paint.Style.FILL);
            
            // 绘制扇形
            canvas.drawPath(path, colorPaint);
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();
            
            // 计算触摸点距离中心的距离
            float distance = (float) Math.sqrt((x - centerX) * (x - centerX) + (y - centerY) * (y - centerY));
            
            // 检查是否在色盘范围内
            if (distance >= radius * 0.4f && distance <= radius) {
                // 计算角度
                double angle = Math.atan2(y - centerY, x - centerX);
                if (angle < 0) angle += 2 * Math.PI;
                
                // 转换为度数
                float degrees = (float) Math.toDegrees(angle);
                
                // 根据角度选择颜色
                int colorIndex = (int) (degrees / (360f / (colors.length - 1)));
                if (colorIndex >= colors.length - 1) colorIndex = colors.length - 2;
                
                selectedColor = colors[colorIndex];
                
                // 通知监听器
                if (colorSelectedListener != null) {
                    colorSelectedListener.onColorSelected(selectedColor);
                }
                
                invalidate();
                return true;
            }
        }
        return super.onTouchEvent(event);
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
}
