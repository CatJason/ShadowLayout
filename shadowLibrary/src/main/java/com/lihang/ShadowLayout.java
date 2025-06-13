package com.lihang;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

public class ShadowLayout extends FrameLayout {
    // Shadow properties
    private Paint shadowPaint;
    private int mShadowColor;
    private float mShadowLimit;
    private float mDx;
    private float mDy;
    private float mCornerRadius;
    private float mCornerRadiusLeftTop = -1;
    private float mCornerRadiusRightTop = -1;
    private float mCornerRadiusLeftBottom = -1;
    private float mCornerRadiusRightBottom = -1;
    private boolean leftShow = true;
    private boolean rightShow = true;
    private boolean topShow = true;
    private boolean bottomShow = true;
    private boolean isSym = true;
    private boolean isShowShadow = true;
    private RectF rectF = new RectF();

    // Shape properties
    private GradientDrawable gradientDrawable;
    private Drawable layoutBackground;
    private Drawable layoutBackgroundTrue;
    private int mBackGroundColor = Color.WHITE;
    private int mBackGroundColorTrue = -101;
    private int strokeColor = -1;
    private int strokeColorTrue = -1;
    private float strokeWidth;
    private float strokeDashWidth = -1;
    private float strokeDashGap = -1;
    private int currentStrokeColor;

    // Gradient properties
    private int startColor = -1;
    private int centerColor = -1;
    private int endColor = -1;
    private int angle = 0;

    // Clickable properties
    private boolean isClickable = true;
    private Drawable clickAbleFalseDrawable;
    private int clickAbleFalseColor = -101;

    // TextView binding
    private int mTextViewResId = -1;
    private TextView mTextView;
    private int textColor;
    private int textColorTrue;
    private String text;
    private String textTrue;

    public ShadowLayout(Context context) {
        this(context, null);
    }

    public ShadowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShadowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttributes(context, attrs);
        initView();
    }

    private void initAttributes(Context context, AttributeSet attrs) {
        TypedArray attr = context.obtainStyledAttributes(attrs, R.styleable.ShadowLayout);

        isShowShadow = !attr.getBoolean(R.styleable.ShadowLayout_hl_shadowHidden, false);
        leftShow = !attr.getBoolean(R.styleable.ShadowLayout_hl_shadowHiddenLeft, false);
        rightShow = !attr.getBoolean(R.styleable.ShadowLayout_hl_shadowHiddenRight, false);
        bottomShow = !attr.getBoolean(R.styleable.ShadowLayout_hl_shadowHiddenBottom, false);
        topShow = !attr.getBoolean(R.styleable.ShadowLayout_hl_shadowHiddenTop, false);
        mCornerRadius = attr.getDimension(R.styleable.ShadowLayout_hl_cornerRadius, 0);
        mCornerRadiusLeftTop = attr.getDimension(R.styleable.ShadowLayout_hl_cornerRadius_leftTop, -1);
        mCornerRadiusLeftBottom = attr.getDimension(R.styleable.ShadowLayout_hl_cornerRadius_leftBottom, -1);
        mCornerRadiusRightTop = attr.getDimension(R.styleable.ShadowLayout_hl_cornerRadius_rightTop, -1);
        mCornerRadiusRightBottom = attr.getDimension(R.styleable.ShadowLayout_hl_cornerRadius_rightBottom, -1);
        mShadowLimit = attr.getDimension(R.styleable.ShadowLayout_hl_shadowLimit, 0);
        mDx = attr.getDimension(R.styleable.ShadowLayout_hl_shadowOffsetX, 0);
        mDy = attr.getDimension(R.styleable.ShadowLayout_hl_shadowOffsetY, 0);
        mShadowColor = attr.getColor(R.styleable.ShadowLayout_hl_shadowColor, Color.parseColor("#2a000000"));
        isSym = attr.getBoolean(R.styleable.ShadowLayout_hl_shadowSymmetry, true);

        Drawable background = attr.getDrawable(R.styleable.ShadowLayout_hl_layoutBackground);
        if (background != null) {
            if (background instanceof ColorDrawable) {
                mBackGroundColor = ((ColorDrawable) background).getColor();
            } else {
                layoutBackground = background;
            }
        }

        Drawable trueBackground = attr.getDrawable(R.styleable.ShadowLayout_hl_layoutBackground_true);
        if (trueBackground != null) {
            if (trueBackground instanceof ColorDrawable) {
                mBackGroundColorTrue = ((ColorDrawable) trueBackground).getColor();
            } else {
                layoutBackgroundTrue = trueBackground;
            }
        }

        strokeColor = attr.getColor(R.styleable.ShadowLayout_hl_strokeColor, -1);
        strokeColorTrue = attr.getColor(R.styleable.ShadowLayout_hl_strokeColor_true, -1);
        strokeWidth = attr.getDimension(R.styleable.ShadowLayout_hl_strokeWith, dip2px(1));
        strokeDashWidth = attr.getDimension(R.styleable.ShadowLayout_hl_stroke_dashWidth, -1);
        strokeDashGap = attr.getDimension(R.styleable.ShadowLayout_hl_stroke_dashGap, -1);

        startColor = attr.getColor(R.styleable.ShadowLayout_hl_startColor, -1);
        centerColor = attr.getColor(R.styleable.ShadowLayout_hl_centerColor, -1);
        endColor = attr.getColor(R.styleable.ShadowLayout_hl_endColor, -1);
        angle = attr.getInt(R.styleable.ShadowLayout_hl_angle, 0);

        isClickable = attr.getBoolean(R.styleable.ShadowLayout_clickable, true);
        Drawable clickAbleFalseBackground = attr.getDrawable(R.styleable.ShadowLayout_hl_layoutBackground_clickFalse);
        if (clickAbleFalseBackground != null) {
            if (clickAbleFalseBackground instanceof ColorDrawable) {
                clickAbleFalseColor = ((ColorDrawable) clickAbleFalseBackground).getColor();
            } else {
                clickAbleFalseDrawable = clickAbleFalseBackground;
            }
        }

        mTextViewResId = attr.getResourceId(R.styleable.ShadowLayout_hl_bindTextView, -1);
        textColor = attr.getColor(R.styleable.ShadowLayout_hl_textColor, -1);
        textColorTrue = attr.getColor(R.styleable.ShadowLayout_hl_textColor_true, -1);
        text = attr.getString(R.styleable.ShadowLayout_hl_text);
        textTrue = attr.getString(R.styleable.ShadowLayout_hl_text_true);

        attr.recycle();
    }

    private void initView() {
        shadowPaint = new Paint();
        shadowPaint.setAntiAlias(true);
        shadowPaint.setStyle(Paint.Style.FILL);

        gradientDrawable = new GradientDrawable();
        gradientDrawable.setColors(new int[]{mBackGroundColor, mBackGroundColor});

        if (strokeColor != -1) {
            currentStrokeColor = strokeColor;
            if (strokeDashWidth != -1) {
                gradientDrawable.setStroke((int)strokeWidth, strokeColor, strokeDashWidth, strokeDashGap);
            } else {
                gradientDrawable.setStroke((int)strokeWidth, strokeColor);
            }
        }

        setClickable(isClickable);
        setWillNotDraw(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 1. 测量子View
        int childWidth = 0;
        int childHeight = 0;
        if (getChildCount() > 0) {
            View child = getChildAt(0);
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
            childWidth = child.getMeasuredWidth();
            childHeight = child.getMeasuredHeight();
        }

        // 2. 根据显示方向计算阴影需要的空间
        float horizontalPadding = (leftShow ? mShadowLimit : 0) + (rightShow ? mShadowLimit : 0);
        float verticalPadding = (topShow ? mShadowLimit : 0) + (bottomShow ? mShadowLimit : 0);

        // 3. 设置最终尺寸
        setMeasuredDimension(
                resolveSize(childWidth + (int)horizontalPadding, widthMeasureSpec),
                resolveSize(childHeight + (int)verticalPadding, heightMeasureSpec)
        );
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (getChildCount() > 0) {
            View child = getChildAt(0);
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            // 根据阴影显示方向计算位置
            int childLeft = leftShow ? (int)mShadowLimit : 0;
            int childTop = topShow ? (int)mShadowLimit : 0;

            child.layout(
                    childLeft,
                    childTop,
                    childLeft + childWidth,
                    childTop + childHeight
            );
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() <= 0 || getHeight() <= 0) return;

        // 计算实际生效的偏移量（当某方向不可见时对应偏移归零）
        float effectiveDx = leftShow || rightShow ? mDx : 0;
        float effectiveDy = topShow || bottomShow ? mDy : 0;

        // 内容区域计算（考虑可见方向）
        float shadowLeft = leftShow ? mShadowLimit : 0;
        float shadowTop = topShow ? mShadowLimit : 0;
        float shadowRight = getWidth() - (rightShow ? mShadowLimit : 0);
        float shadowBottom = getHeight() - (bottomShow ? mShadowLimit : 0);

        rectF.set(shadowLeft, shadowTop, shadowRight, shadowBottom);

        if (isShowShadow && mShadowLimit > 0) {
            shadowPaint.setColor(Color.TRANSPARENT);
            // 使用经过方向过滤的偏移量
            shadowPaint.setShadowLayer(mShadowLimit/2, effectiveDx, effectiveDy, mShadowColor);

            if (hasSpecialCorner()) {
                Path path = new Path();
                path.addRoundRect(rectF, getCornerRadii(), Path.Direction.CW);
                canvas.drawPath(path, shadowPaint);
            } else {
                canvas.drawRoundRect(rectF, mCornerRadius, mCornerRadius, shadowPaint);
            }
        }

        gradientDrawable.setBounds((int)shadowLeft, (int)shadowTop,
                (int)shadowRight, (int)shadowBottom);
        gradientDrawable.setCornerRadii(getCornerRadii());
        gradientDrawable.draw(canvas);
    }

    private float[] getCornerRadii() {
        float leftTop = mCornerRadiusLeftTop == -1 ? mCornerRadius : mCornerRadiusLeftTop;
        float rightTop = mCornerRadiusRightTop == -1 ? mCornerRadius : mCornerRadiusRightTop;
        float rightBottom = mCornerRadiusRightBottom == -1 ? mCornerRadius : mCornerRadiusRightBottom;
        float leftBottom = mCornerRadiusLeftBottom == -1 ? mCornerRadius : mCornerRadiusLeftBottom;

        return new float[]{
                leftTop, leftTop,
                rightTop, rightTop,
                rightBottom, rightBottom,
                leftBottom, leftBottom
        };
    }

    private boolean hasSpecialCorner() {
        return mCornerRadiusLeftTop != -1 || mCornerRadiusRightTop != -1 ||
                mCornerRadiusLeftBottom != -1 || mCornerRadiusRightBottom != -1;
    }

    private float dip2px(float dipValue) {
        return dipValue * getResources().getDisplayMetrics().density + 0.5f;
    }

    // Public methods for dynamic modification
    public float getCornerRadius() {
        return mCornerRadius;
    }

    public void setCornerRadius(float cornerRadius) {
        this.mCornerRadius = cornerRadius;
        invalidate();
    }

    public float getShadowLimit() {
        return mShadowLimit;
    }

    public void setShadowLimit(float shadowLimit) {
        this.mShadowLimit = shadowLimit;
        requestLayout();
    }

    public float getShadowOffsetX() {
        return mDx;
    }

    public void setShadowOffsetX(float dx) {
        if (this.mDx != dx) {
            this.mDx = dx;
            invalidate();
        }
    }

    public float getShadowOffsetY() {
        return mDy;
    }

    public void setShadowOffsetY(float dy) {
        if (this.mDy != dy) {
            this.mDy = dy;
            invalidate();
        }
    }

    public int getShadowColor() {
        return mShadowColor;
    }

    public void setShadowColor(int shadowColor) {
        this.mShadowColor = shadowColor;
        invalidate();
    }

    public boolean isShadowHidden() {
        return !isShowShadow;
    }

    public void setShadowHidden(boolean shadowHidden) {
        this.isShowShadow = !shadowHidden;
        requestLayout();
    }

    public boolean isShadowHiddenLeft() {
        return !leftShow;
    }

    public void setShadowHiddenLeft(boolean shadowHiddenLeft) {
        if (this.leftShow == shadowHiddenLeft) {
            this.leftShow = !shadowHiddenLeft;
            requestLayout();
            invalidate();
        }
    }

    public boolean isShadowHiddenRight() {
        return !rightShow;
    }

    public void setShadowHiddenRight(boolean shadowHiddenRight) {
        if (this.rightShow == shadowHiddenRight) {
            this.rightShow = !shadowHiddenRight;
            requestLayout();
            invalidate();
        }
    }

    public boolean isShadowHiddenTop() {
        return !topShow;
    }

    public void setShadowHiddenTop(boolean shadowHiddenTop) {
        if (this.topShow == shadowHiddenTop) {
            this.topShow = !shadowHiddenTop;
            requestLayout();
            invalidate();
        }
    }

    public boolean isShadowHiddenBottom() {
        return !bottomShow;
    }

    public void setShadowHiddenBottom(boolean shadowHiddenBottom) {
        if (this.bottomShow == shadowHiddenBottom) {
            this.bottomShow = !shadowHiddenBottom;
            requestLayout();
            invalidate();
        }
    }

    public void setLayoutBackground(int color) {
        this.mBackGroundColor = color;
        gradientDrawable.setColors(new int[]{mBackGroundColor, mBackGroundColor});
        invalidate();
    }

    public void setStrokeColor(int color) {
        this.strokeColor = color;
        this.currentStrokeColor = color;
        gradientDrawable.setStroke((int)strokeWidth, color);
        invalidate();
    }

    public void setStrokeWidth(float width) {
        this.strokeWidth = width;
        gradientDrawable.setStroke((int)width, currentStrokeColor);
        invalidate();
    }

    public void setStartColor(int color) {
        this.startColor = color;
        setupGradient();
        invalidate();
    }

    public void setCenterColor(int color) {
        this.centerColor = color;
        setupGradient();
        invalidate();
    }

    public void setEndColor(int color) {
        this.endColor = color;
        setupGradient();
        invalidate();
    }

    public void setAngle(int angle) {
        this.angle = angle;
        setupGradient();
        invalidate();
    }

    private void setupGradient() {
        if (startColor == -1) return;

        int[] colors;
        if (centerColor == -1) {
            colors = new int[]{startColor, endColor};
        } else {
            colors = new int[]{startColor, centerColor, endColor};
        }

        gradientDrawable.setColors(colors);

        int trueAngle = angle % 360;
        GradientDrawable.Orientation orientation;

        switch (trueAngle / 45) {
            case 0: orientation = GradientDrawable.Orientation.LEFT_RIGHT; break;
            case 1: orientation = GradientDrawable.Orientation.BL_TR; break;
            case 2: orientation = GradientDrawable.Orientation.BOTTOM_TOP; break;
            case 3: orientation = GradientDrawable.Orientation.BR_TL; break;
            case 4: orientation = GradientDrawable.Orientation.RIGHT_LEFT; break;
            case 5: orientation = GradientDrawable.Orientation.TR_BL; break;
            case 6: orientation = GradientDrawable.Orientation.TOP_BOTTOM; break;
            case 7: orientation = GradientDrawable.Orientation.TL_BR; break;
            default: orientation = GradientDrawable.Orientation.LEFT_RIGHT;
        }

        gradientDrawable.setOrientation(orientation);
    }
}