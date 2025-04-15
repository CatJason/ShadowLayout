package com.lihang;


import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

/**
 * Created by leo
 * on 2019/7/9.
 * 阴影控件
 */

public class ShadowLayout extends FrameLayout {
    /***
     * Shadow
     */
    private Paint shadowPaint;//阴影画笔
    private int mShadowColor;//阴影颜色
    private float mShadowLimit;//阴影扩散区域大小
    //阴影x,y偏移量
    private float mDx;
    private float mDy;
    //阴影圆角属性，也是shape的圆角属性
    private float mCornerRadius;//圆角大小，如四角没有单独设置，则为大小为mCornerRadius
    private float mCornerRadius_leftTop;//单独设置左上角圆角大小。（同理以下）
    private float mCornerRadius_rightTop;
    private float mCornerRadius_leftBottom;
    private float mCornerRadius_rightBottom;
    //阴影四边可见属性
    private boolean leftShow;//false表示左边不可见
    private boolean rightShow;
    private boolean topShow;
    private boolean bottomShow;
    //子布局与父布局的padding（即通过padding来实现mShadowLimit的大小和阴影展示）
    private int leftPadding;
    private int topPadding;
    private int rightPadding;
    private int bottomPadding;
    private RectF rectf = new RectF();//阴影布局子空间区域
    private View firstView;//如有子View则为子View，否则为ShadowLayout本身
    //
    private boolean isSym;//控件区域是否对称，如不对称则区域跟随阴影走
    private boolean isShowShadow = true;//是否使用阴影，可能存在不使用阴影只使用shape


    /**
     * Shape
     */
    private static final int MODE_PRESSED = 1;
    private static final int MODE_SELECTED = 2;
    private static final int MODE_RIPPLE = 3;
    private static final int MODE_DASHLINE = 4;
    //
    private int shapeModeType;//ShadowLayout的shapeMode，默认是pressed.
    GradientDrawable gradientDrawable;//shape功能最终用系统类GradientDrawable代替
    private Drawable layoutBackground;//正常情况下的drawable（与mBackGroundColor不可共存）
    private Drawable layoutBackground_true;
    private int mBackGroundColor;//正常情况下的color（默认为白色，与layoutBackground不可共存）
    private int mBackGroundColor_true = -101;
    //填充渐变色
    private int startColor;
    private int centerColor;
    private int endColor;
    private int angle;
    //边框画笔
    private int current_stroke_color;
    private float stroke_with;
    private int stroke_color;
    private int stroke_color_true;
    private float stroke_dashWidth = -1;
    private float stroke_dashGap = -1;

    /**
     * ClickAble
     */
    private boolean isClickable;//是否可点击
    private Drawable clickAbleFalseDrawable;//不可点击状态下的drawable（与clickAbleFalseColor不可共存）
    private int clickAbleFalseColor = -101;//不可点击状态下的填充color（与clickAbleFalseDrawable不可共存）

    /**
     * ShadowLayout绑定的textView
     */
    private int mTextViewResId = -1;
    private TextView mTextView;
    private int textColor;
    private int textColor_true;
    private String text;
    private String text_true;

    /**
     * 虚线
     */
    private Paint mPaintDash;
    private Path dashPath;


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public ShadowLayout(Context context) {
        this(context, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public ShadowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public ShadowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    /**
     * 初始化视图
     * 功能：根据自定义属性初始化View的各种绘制参数和对象
     * 适用版本：Android 4.1(JELLY_BEAN)及以上
     *
     * @param context 上下文对象
     * @param attrs XML属性集合
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void initView(Context context, AttributeSet attrs) {
        // 1. 初始化自定义属性
        initAttributes(attrs);

        // 2. 如果是虚线模式则直接返回
        if (isDashLine()) {
            return;
        }

        // 3. 初始化阴影画笔
        shadowPaint = new Paint();
        shadowPaint.setAntiAlias(true); // 开启抗锯齿
        shadowPaint.setStyle(Paint.Style.FILL); // 设置填充样式

        // 4. 初始化渐变Drawable
        gradientDrawable = new GradientDrawable();
        // 设置默认背景色（双色相同表示纯色）
        gradientDrawable.setColors(new int[]{mBackGroundColor, mBackGroundColor});

        // 5. 如果有描边色则设置当前描边色
        if (stroke_color != -101) {
            current_stroke_color = stroke_color;
        }

        // 6. 设置内边距
        setPadding();
    }

    /**
     * 判断是否为虚线模式
     * @return true-虚线模式 false-非虚线模式
     */
    private boolean isDashLine() {
        return shapeModeType == ShadowLayout.MODE_DASHLINE;
    }

    /**
     * 初始化虚线绘制参数
     * 功能：设置虚线画笔和路径对象
     */
    private void initDashLine() {
        // 1. 创建虚线画笔
        mPaintDash = new Paint();
        mPaintDash.setAntiAlias(true); // 开启抗锯齿
        mPaintDash.setColor(stroke_color); // 设置虚线颜色
        mPaintDash.setStyle(Paint.Style.STROKE); // 设置描边样式

        // 2. 设置虚线效果
        // 参数1: 虚线模式数组（实线长度，间隔长度）
        // 参数2: 相位偏移量
        mPaintDash.setPathEffect(new DashPathEffect(
                new float[]{stroke_dashWidth, stroke_dashGap}, 0));

        // 3. 创建路径对象
        dashPath = new Path();
    }

    /**
     * 初始化自定义属性
     * 功能：从XML属性中读取并初始化各种样式参数
     * 适用版本：Android 4.1(JELLY_BEAN)及以上
     *
     * @param attrs XML属性集合
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void initAttributes(AttributeSet attrs) {
        // 获取属性数组
        TypedArray attr = getContext().obtainStyledAttributes(attrs, R.styleable.ShadowLayout);

        // ========== 基本模式设置 ==========
        // 获取形状模式，默认为按压模式(MODE_PRESSED)
        shapeModeType = attr.getInt(R.styleable.ShadowLayout_hl_shapeMode, ShadowLayout.MODE_PRESSED);

        // ========== 虚线模式特殊处理 ==========
        if (isDashLine()) {
            // 获取虚线相关属性
            stroke_color = attr.getColor(R.styleable.ShadowLayout_hl_strokeColor, -101); // 虚线颜色
            stroke_dashWidth = attr.getDimension(R.styleable.ShadowLayout_hl_stroke_dashWidth, -1); // 虚线宽度
            stroke_dashGap = attr.getDimension(R.styleable.ShadowLayout_hl_stroke_dashGap, -1); // 虚线间隔

            // 参数校验
            if (stroke_color == -101) {
                throw new UnsupportedOperationException("shapeMode为MODE_DASHLINE,需设置stroke_color值");
            }
            if (stroke_dashWidth == -1) {
                throw new UnsupportedOperationException("shapeMode为MODE_DASHLINE,需设置stroke_dashWidth值");
            }
            if ((stroke_dashWidth == -1 && stroke_dashGap != -1) ||
                    (stroke_dashWidth != -1 && stroke_dashGap == -1)) {
                throw new UnsupportedOperationException("使用了虚线边框,必须同时设置以下2个属性：" +
                        "ShadowLayout_hl_stroke_dashWidth，ShadowLayout_hl_stroke_dashGap");
            }

            initDashLine(); // 初始化虚线绘制参数
            attr.recycle(); // 释放资源
            return; // 虚线模式不需要后续处理
        }

        // ========== 阴影相关属性 ==========
        isShowShadow = !attr.getBoolean(R.styleable.ShadowLayout_hl_shadowHidden, false); // 是否显示阴影
        // 各边阴影显示控制
        leftShow = !attr.getBoolean(R.styleable.ShadowLayout_hl_shadowHiddenLeft, false); // 左边阴影
        rightShow = !attr.getBoolean(R.styleable.ShadowLayout_hl_shadowHiddenRight, false); // 右边阴影
        bottomShow = !attr.getBoolean(R.styleable.ShadowLayout_hl_shadowHiddenBottom, false); // 底部阴影
        topShow = !attr.getBoolean(R.styleable.ShadowLayout_hl_shadowHiddenTop, false); // 顶部阴影

        // ========== 圆角相关属性 ==========
        mCornerRadius = attr.getDimension(R.styleable.ShadowLayout_hl_cornerRadius,
                getResources().getDimension(R.dimen.dp_0)); // 统一圆角半径
        // 各角单独设置的圆角半径(-1表示未设置)
        mCornerRadius_leftTop = attr.getDimension(R.styleable.ShadowLayout_hl_cornerRadius_leftTop, -1);
        mCornerRadius_leftBottom = attr.getDimension(R.styleable.ShadowLayout_hl_cornerRadius_leftBottom, -1);
        mCornerRadius_rightTop = attr.getDimension(R.styleable.ShadowLayout_hl_cornerRadius_rightTop, -1);
        mCornerRadius_rightBottom = attr.getDimension(R.styleable.ShadowLayout_hl_cornerRadius_rightBottom, -1);

        // ========== 阴影效果参数 ==========
        mShadowLimit = attr.getDimension(R.styleable.ShadowLayout_hl_shadowLimit, 0); // 阴影扩散范围
        if (mShadowLimit == 0) {
            isShowShadow = false; // 阴影范围为0时不显示阴影
        }
        mDx = attr.getDimension(R.styleable.ShadowLayout_hl_shadowOffsetX, 0); // X轴偏移
        mDy = attr.getDimension(R.styleable.ShadowLayout_hl_shadowOffsetY, 0); // Y轴偏移
        mShadowColor = attr.getColor(R.styleable.ShadowLayout_hl_shadowColor,
                getResources().getColor(R.color.default_shadow_color)); // 阴影颜色
        isSym = attr.getBoolean(R.styleable.ShadowLayout_hl_shadowSymmetry, true); // 是否对称阴影

        // ========== 背景相关属性 ==========
        // 默认背景色(白色)
        mBackGroundColor = getResources().getColor(R.color.default_shadowback_color);
        // 获取背景Drawable
        Drawable background = attr.getDrawable(R.styleable.ShadowLayout_hl_layoutBackground);
        if (background != null) {
            if (background instanceof ColorDrawable) {
                // 背景是颜色Drawable
                mBackGroundColor = ((ColorDrawable) background).getColor();
            } else {
                // 背景是图片Drawable
                layoutBackground = background;
            }
        }

        // 获取选中状态背景
        Drawable trueBackground = attr.getDrawable(R.styleable.ShadowLayout_hl_layoutBackground_true);
        if (trueBackground != null) {
            if (trueBackground instanceof ColorDrawable) {
                mBackGroundColor_true = ((ColorDrawable) trueBackground).getColor();
            } else {
                layoutBackground_true = trueBackground;
            }
        }

        // 背景属性校验
        if (mBackGroundColor_true != -101 && layoutBackground != null) {
            throw new UnsupportedOperationException("使用了选中状态背景色时，必须同时设置默认背景色");
        }
        if (layoutBackground == null && layoutBackground_true != null) {
            throw new UnsupportedOperationException("使用了选中状态背景图时，必须同时设置默认背景图");
        }

        // ========== 描边相关属性 ==========
        stroke_color = attr.getColor(R.styleable.ShadowLayout_hl_strokeColor, -101); // 默认描边色
        stroke_color_true = attr.getColor(R.styleable.ShadowLayout_hl_strokeColor_true, -101); // 选中状态描边色
        if (stroke_color == -101 && stroke_color_true != -101) {
            throw new UnsupportedOperationException("使用了选中状态描边色时，必须同时设置默认描边色");
        }
        stroke_with = attr.getDimension(R.styleable.ShadowLayout_hl_strokeWith, dip2px(1)); // 描边宽度(默认1dp)

        // 虚线描边属性
        stroke_dashWidth = attr.getDimension(R.styleable.ShadowLayout_hl_stroke_dashWidth, -1);
        stroke_dashGap = attr.getDimension(R.styleable.ShadowLayout_hl_stroke_dashGap, -1);
        if ((stroke_dashWidth == -1 && stroke_dashGap != -1) ||
                (stroke_dashWidth != -1 && stroke_dashGap == -1)) {
            throw new UnsupportedOperationException("使用虚线描边时必须同时设置虚线段长度和间隔");
        }

        // ========== 不可点击状态背景 ==========
        Drawable clickAbleFalseBackground = attr.getDrawable(R.styleable.ShadowLayout_hl_layoutBackground_clickFalse);
        if (clickAbleFalseBackground != null) {
            if (clickAbleFalseBackground instanceof ColorDrawable) {
                clickAbleFalseColor = ((ColorDrawable) clickAbleFalseBackground).getColor();
            } else {
                clickAbleFalseDrawable = clickAbleFalseBackground;
            }
        }

        // ========== 渐变背景相关属性 ==========
        startColor = attr.getColor(R.styleable.ShadowLayout_hl_startColor, -101); // 渐变起始色
        centerColor = attr.getColor(R.styleable.ShadowLayout_hl_centerColor, -101); // 渐变中间色
        endColor = attr.getColor(R.styleable.ShadowLayout_hl_endColor, -101); // 渐变结束色
        if (startColor != -101 && endColor == -101) {
            throw new UnsupportedOperationException("设置渐变起始色时必须同时设置结束色");
        }

        // 渐变角度(必须是45的倍数)
        angle = attr.getInt(R.styleable.ShadowLayout_hl_angle, 0);
        if (angle % 45 != 0) {
            throw new IllegalArgumentException("渐变角度必须是45的倍数");
        }

        // ========== 水波纹模式校验 ==========
        if (shapeModeType == ShadowLayout.MODE_RIPPLE) {
            if (mBackGroundColor == -101 || mBackGroundColor_true == -101) {
                throw new NullPointerException("水波纹模式必须设置默认和选中状态背景色");
            }
            if (layoutBackground != null) {
                shapeModeType = ShadowLayout.MODE_PRESSED; // 背景是图片时降级为按压模式
            }
        }

        // ========== 文本相关属性 ==========
        mTextViewResId = attr.getResourceId(R.styleable.ShadowLayout_hl_bindTextView, -1); // 绑定TextView ID
        textColor = attr.getColor(R.styleable.ShadowLayout_hl_textColor, -101); // 默认文本颜色
        textColor_true = attr.getColor(R.styleable.ShadowLayout_hl_textColor_true, -101); // 选中状态文本颜色
        text = attr.getString(R.styleable.ShadowLayout_hl_text); // 默认文本
        text_true = attr.getString(R.styleable.ShadowLayout_hl_text_true); // 选中状态文本

        // ========== 可点击状态 ==========
        isClickable = attr.getBoolean(R.styleable.ShadowLayout_clickable, true); // 是否可点击
        setClickable(isClickable);

        // 释放TypedArray资源
        attr.recycle();
    }

    /**
     * View尺寸变化回调方法
     * 功能：当View尺寸发生变化时，重新设置背景和渐变效果
     * 适用版本：Android 4.1(JELLY_BEAN)及以上
     *
     * @param w    新的宽度
     * @param h    新的高度
     * @param oldw 旧的宽度
     * @param oldh 旧的高度
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh); // 调用父类方法

        // ========== 虚线模式特殊处理 ==========
        if (isDashLine()) {
            // 设置透明背景解决虚线模式下onDraw不执行的问题
            this.setBackgroundColor(Color.parseColor("#00000000"));
            return; // 虚线模式不需要后续处理
        }

        // ========== 正常模式处理 ==========
        // 确保新尺寸有效（宽高都大于0）
        if (w > 0 && h > 0) {
            // 1. 设置兼容性背景（处理阴影等效果）
            setBackgroundCompat(w, h);

            // 2. 如果设置了渐变起始色（-101表示未设置），则应用渐变效果
            if (startColor != -101) {
                gradient(gradientDrawable);
            }
        }
    }


    /**
     * 当View从布局文件加载完成后调用的方法（在Inflater.inflate()执行后触发）
     * 主要完成以下工作：
     * 1. 检查虚线模式下的子View限制
     * 2. 处理绑定的TextView及其文本样式
     * 3. 处理背景和阴影的显示逻辑
     * 4. 根据点击状态设置不同的背景
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();  // 调用父类方法完成基础初始化

        // ========== 虚线模式处理 ==========
        if (isDashLine()) {
            // 虚线模式下不允许有子View
            if (getChildAt(0) != null) {
                throw new UnsupportedOperationException("shapeMode为MODE_DASHLINE，不支持子view");
            }
            return;  // 虚线模式不需要后续处理
        }

        // ========== 绑定TextView处理 ==========
        if (mTextViewResId != -1) {  // 如果设置了绑定的TextView资源ID
            mTextView = findViewById(mTextViewResId);  // 查找对应的TextView

            if (mTextView == null) {
                throw new NullPointerException("ShadowLayout找不到hl_bindTextView，请确保绑定的资源id在ShadowLayout内");
            } else {
                // 处理文本颜色（如果未设置则使用TextView的当前颜色）
                if (textColor == -101) {  // -101表示未设置颜色
                    textColor = mTextView.getCurrentTextColor();  // 获取当前文本颜色
                }

                // 处理选中状态文本颜色（如果未设置则使用TextView的当前颜色）
                if (textColor_true == -101) {
                    textColor_true = mTextView.getCurrentTextColor();
                }

                // 应用文本颜色
                mTextView.setTextColor(textColor);

                // 如果设置了默认文本，则应用
                if (!TextUtils.isEmpty(text)) {
                    mTextView.setText(text);
                }
            }
        }

        // ========== 子View处理 ==========
        // 获取第一个子View（索引为0）
        firstView = getChildAt(0);

        // 检查背景和阴影的兼容性
        if (layoutBackground != null) {
            // 如果同时启用了阴影效果且有阴影范围，但没有子View，则抛出异常
            if (isShowShadow == true && mShadowLimit > 0 && getChildAt(0) == null) {
                throw new UnsupportedOperationException("使用了图片又加上阴影的情况下，必须加上子view才会生效!~");
            }
        }

        // 如果没有子View
        if (firstView == null) {
            // 将ShadowLayout自身作为firstView
            firstView = ShadowLayout.this;
            // 没有子View时默认不显示阴影
            isShowShadow = false;
        }

        // ========== 背景设置处理 ==========
        if (firstView != null) {
            // 处理选择器模式（selector样式）
            if (shapeModeType == ShadowLayout.MODE_SELECTED) {
                // 选择器模式不受clickable影响，直接设置背景
                setmBackGround(layoutBackground, "onFinishInflate");
            } else {
                // 非选择器模式根据可点击状态设置不同背景
                if (isClickable) {
                    // 可点击状态使用正常背景
                    setmBackGround(layoutBackground, "onFinishInflate");
                } else {
                    // 不可点击状态使用特殊背景
                    setmBackGround(clickAbleFalseDrawable, "onFinishInflate");

                    // 如果设置了不可点击状态的颜色，则更新渐变Drawable的颜色
                    if (clickAbleFalseColor != -101) {
                        gradientDrawable.setColors(new int[]{clickAbleFalseColor, clickAbleFalseColor});
                    }
                }
            }
        }
    }

    /**
     * 重写触摸事件处理逻辑
     * 主要处理两种模式下的触摸反馈：
     * 1. 水波纹模式(MODE_RIPPLE)：使用系统自带的水波纹效果
     * 2. 按压模式(MODE_PRESSED)：自定义按压状态的颜色变化效果
     *
     * @param event 触摸事件对象
     * @return 返回事件处理结果，通常继续父类处理流程
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // ========== 水波纹模式处理 ==========
        if (shapeModeType == ShadowLayout.MODE_RIPPLE) {
            // 水波纹模式下直接使用系统效果，只需处理文本状态变化
            if (isClickable) {  // 仅在可点击状态下处理
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:  // 按下事件
                        if (mTextView != null) {
                            // 切换到按下状态文本颜色
                            mTextView.setTextColor(textColor_true);
                            // 如果有按下状态文本则切换
                            if (!TextUtils.isEmpty(text_true)) {
                                mTextView.setText(text_true);
                            }
                        }
                        break;

                    case MotionEvent.ACTION_CANCEL:  // 取消事件
                    case MotionEvent.ACTION_UP:     // 抬起事件
                        if (mTextView != null) {
                            // 恢复默认文本颜色
                            mTextView.setTextColor(textColor);
                            // 恢复默认文本内容
                            if (!TextUtils.isEmpty(text)) {
                                mTextView.setText(text);
                            }
                        }
                        break;
                }
            }
            return super.onTouchEvent(event);  // 继续父类处理流程
        }

        // ========== 按压模式处理 ==========
        // 检查是否需要处理按压效果（设置了按压状态颜色或背景）
        if (mBackGroundColor_true != -101 || stroke_color_true != -101 || layoutBackground_true != null) {
            // 仅在可点击且为按压模式下处理
            if (isClickable && shapeModeType == ShadowLayout.MODE_PRESSED) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:  // 按下事件
                        // 背景色变化处理
                        if (mBackGroundColor_true != -101) {  // -101表示未设置
                            // 切换到按压状态背景色（单色）
                            gradientDrawable.setColors(new int[]{mBackGroundColor_true, mBackGroundColor_true});
                        }
                        // 描边色变化处理
                        if (stroke_color_true != -101) {
                            current_stroke_color = stroke_color_true;  // 更新当前描边色
                        }
                        // 背景图片变化处理
                        if (layoutBackground_true != null) {
                            setmBackGround(layoutBackground_true, "onTouchEvent");
                        }
                        postInvalidate();  // 请求重绘

                        // 文本状态变化处理
                        if (mTextView != null) {
                            mTextView.setTextColor(textColor_true);  // 按压状态文本色
                            if (!TextUtils.isEmpty(text_true)) {    // 按压状态文本内容
                                mTextView.setText(text_true);
                            }
                        }
                        break;

                    case MotionEvent.ACTION_CANCEL:  // 取消事件
                    case MotionEvent.ACTION_UP:     // 抬起事件
                        // 恢复默认背景色（单色）
                        gradientDrawable.setColors(new int[]{mBackGroundColor, mBackGroundColor});
                        // 如果设置了渐变背景则重新应用渐变
                        if (startColor != -101) {
                            gradient(gradientDrawable);
                        }
                        // 恢复默认描边色
                        if (stroke_color != -101) {
                            current_stroke_color = stroke_color;
                        }
                        // 恢复默认背景图片
                        if (layoutBackground != null) {
                            setmBackGround(layoutBackground, "onTouchEvent");
                        }
                        postInvalidate();  // 请求重绘

                        // 恢复默认文本状态
                        if (mTextView != null) {
                            mTextView.setTextColor(textColor);  // 默认文本色
                            if (!TextUtils.isEmpty(text)) {     // 默认文本内容
                                mTextView.setText(text);
                            }
                        }
                        break;
                }
            }
        }

        // 继续父类的触摸事件处理
        return super.onTouchEvent(event);
    }

    /**
     * 设置选中状态切换效果
     * 功能：处理选中状态下的UI变化，包括背景色、描边色、背景图片和文本的切换
     * 适用版本：Android 4.1(JELLY_BEAN)及以上
     *
     * @param selected 是否选中
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected); // 调用父类方法设置选中状态

        // 检查View是否已完成布局（宽度不为0）
        if (getWidth() != 0) {
            // 只有在SELECTED模式下才处理选中状态变化
            if (shapeModeType == ShadowLayout.MODE_SELECTED) {
                if (selected) {
                    // ========== 选中状态 ==========
                    // 1. 背景色处理：如果设置了选中状态背景色
                    if (mBackGroundColor_true != -101) { // -101表示未设置
                        gradientDrawable.setColors(new int[]{mBackGroundColor_true, mBackGroundColor_true});
                    }

                    // 2. 描边色处理：如果设置了选中状态描边色
                    if (stroke_color_true != -101) {
                        current_stroke_color = stroke_color_true;
                    }

                    // 3. 背景图片处理：如果设置了选中状态背景图
                    if (layoutBackground_true != null) {
                        setmBackGround(layoutBackground_true, "setSelected");
                    }

                    // 4. 文本处理：如果有绑定TextView
                    if (mTextView != null) {
                        mTextView.setTextColor(textColor_true); // 设置选中文本颜色
                        if (!TextUtils.isEmpty(text_true)) {   // 设置选中文本内容
                            mTextView.setText(text_true);
                        }
                    }

                } else {
                    // ========== 非选中状态 ==========
                    // 1. 恢复默认背景色
                    gradientDrawable.setColors(new int[]{mBackGroundColor, mBackGroundColor});
                    // 如果有渐变效果则重新应用
                    if (startColor != -101) {
                        gradient(gradientDrawable);
                    }

                    // 2. 恢复默认描边色
                    if (stroke_color != -101) {
                        current_stroke_color = stroke_color;
                    }

                    // 3. 恢复默认背景图片
                    if (layoutBackground != null) {
                        setmBackGround(layoutBackground, "setSelected");
                    }

                    // 4. 恢复默认文本样式
                    if (mTextView != null) {
                        mTextView.setTextColor(textColor); // 恢复默认文本颜色
                        if (!TextUtils.isEmpty(text)) {    // 恢复默认文本内容
                            mTextView.setText(text);
                        }
                    }
                }
                // 请求重绘View
                postInvalidate();
            }
        } else {
            // 如果View还未完成布局，添加布局监听器，在布局完成后再次设置选中状态
            addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                           int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    removeOnLayoutChangeListener(this); // 移除监听器避免重复调用
                    setSelected(isSelected());         // 重新设置选中状态
                }
            });
        }
    }

    //解决xml设置clickable = false时。代码设置true时，点击事件无效的bug
    private OnClickListener onClickListener;

    /**
     * 重写系统setOnClickListener,以配合自定义属性isClickable实现是否可点击事件
     *
     * @param l
     */
    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        this.onClickListener = l;
        if (isClickable) {
            super.setOnClickListener(l);
        }
    }

    /**
     * clickable发生变化时，shadowLayout样式切换的方法。
     * 由public改为private不向用户提供，只能在内部使用
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void changeSwitchClickable() {
        //不可点击的状态只在press mode的模式下生效
        if (shapeModeType == ShadowLayout.MODE_PRESSED && firstView != null) {

            //press mode
            if (!isClickable) {
                //不可点击的状态。
                if (clickAbleFalseColor != -101) {
                    //说明设置了颜色
                    if (layoutBackground != null) {
                        //说明此时是设置了图片的模式
                        firstView.getBackground().setAlpha(0);
                    }
                    gradientDrawable.setColors(new int[]{clickAbleFalseColor, clickAbleFalseColor});
                    postInvalidate();


                } else if (clickAbleFalseDrawable != null) {
                    //说明设置了背景图
                    setmBackGround(clickAbleFalseDrawable, "changeSwitchClickable");
                    gradientDrawable.setColors(new int[]{Color.parseColor("#00000000"), Color.parseColor("#00000000")});
                    postInvalidate();
                }
            } else {
                //可点击的状态
                if (layoutBackground != null) {
                    setmBackGround(layoutBackground, "changeSwitchClickable");
                } else {
                    if (firstView.getBackground() != null) {
                        firstView.getBackground().setAlpha(0);
                    }
                }
                gradientDrawable.setColors(new int[]{mBackGroundColor, mBackGroundColor});
                postInvalidate();
            }
        }
    }


    /**
     * 重写dispatchDraw方法，用于对子View进行裁剪绘制
     * 主要功能：根据圆角设置裁剪子View的显示区域，实现圆角效果
     * 适用于Android 5.0(LOLLIPOP)及以上版本
     *
     * @param canvas 用于绘制的画布对象
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void dispatchDraw(Canvas canvas) {
        // 计算实际高度（去除padding后的高度）
        int trueHeight = (int) (rectf.bottom - rectf.top);

        // 检查是否存在子View
        if (getChildAt(0) != null) {
            // 判断是否设置了任何特殊圆角（四个角都为-1表示未设置特殊圆角）
            if (mCornerRadius_leftTop == -1 && mCornerRadius_leftBottom == -1
                    && mCornerRadius_rightTop == -1 && mCornerRadius_rightBottom == -1) {

                // 情况1：未设置特殊圆角，使用统一圆角处理
                // 检查圆角半径是否超过高度的一半（即是否要绘制半圆效果）
                if (mCornerRadius > trueHeight / 2) {
                    // 创建半圆路径（圆角半径取高度的一半）
                    Path path = new Path();
                    path.addRoundRect(rectf,
                            trueHeight / 2,  // x方向圆角半径
                            trueHeight / 2,  // y方向圆角半径
                            Path.Direction.CW); // 顺时针方向绘制
                    // 裁剪画布为半圆形状
                    canvas.clipPath(path);
                } else {
                    // 创建普通圆角路径（使用预设的统一圆角半径）
                    Path path = new Path();
                    path.addRoundRect(rectf,
                            mCornerRadius,  // x方向圆角半径
                            mCornerRadius,  // y方向圆角半径
                            Path.Direction.CW); // 顺时针方向绘制
                    // 裁剪画布为圆角矩形
                    canvas.clipPath(path);
                }
            } else {
                // 情况2：设置了特殊圆角（四个角可能有不同的圆角半径）
                // 获取各角的实际圆角值（考虑高度限制）
                float[] outerR = getCornerValue(trueHeight);

                // 创建自定义圆角路径
                Path path = new Path();
                // 计算实际绘制区域（考虑padding）
                float left = leftPadding;
                float top = topPadding;
                float right = getWidth() - rightPadding;
                float bottom = getHeight() - bottomPadding;

                // 添加自定义圆角矩形路径
                path.addRoundRect(left, top, right, bottom,
                        outerR,         // 各角圆角半径数组
                        Path.Direction.CW); // 顺时针方向绘制
                // 裁剪画布为自定义圆角形状
                canvas.clipPath(path);
            }
        }

        // 调用父类方法继续绘制流程（在裁剪后的画布上绘制子View）
        super.dispatchDraw(canvas);
    }

    /**
     * 自定义绘制方法，处理不同模式下的View绘制逻辑
     * 适用版本：Android 5.0(LOLLIPOP)及以上
     *
     * @param canvas 用于绘制的画布对象
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas); // 先调用父类绘制方法

        // ========== 虚线模式处理 ==========
        if (isDashLine()) {
            // 如果是虚线模式，直接绘制虚线并返回
            drawLine(canvas);
            return;
        }

        // ========== 计算绘制区域 ==========
        // 设置绘制矩形区域（考虑padding）
        rectf.left = leftPadding;                     // 左边界（含左padding）
        rectf.top = topPadding;                       // 上边界（含上padding）
        rectf.right = getWidth() - rightPadding;      // 右边界（减去右padding）
        rectf.bottom = getHeight() - bottomPadding;   // 下边界（减去下padding）

        // 计算实际内容高度（去除padding后的高度）
        int trueHeight = (int) (rectf.bottom - rectf.top);

        // ========== 描边处理 ==========
        // 检查是否设置了描边（-101表示未设置）
        if (stroke_color != -101) {
            // 如果描边宽度超过内容高度的一半，则限制为高度的一半
            if (stroke_with > trueHeight / 2) {
                stroke_with = trueHeight / 2;
            }
        }

        // ========== 背景绘制处理 ==========
        // 如果没有设置背景图片（普通背景和按压状态背景都为空）
        if (layoutBackground == null && layoutBackground_true == null) {
            // 获取圆角半径数组（已处理特殊角情况）
            float[] outerR = getCornerValue(trueHeight);

            // 根据模式选择不同的绘制方式
            if (shapeModeType != ShadowLayout.MODE_RIPPLE) {
                // 非水波纹模式：直接绘制渐变背景
                drawGradientDrawable(canvas, rectf, outerR);
            } else {
                // 水波纹模式：设置波纹效果
                ripple(outerR);
            }
        }
    }

    /**
     * 绘制虚线分割线
     * 根据View的宽高比例自动判断绘制横向或纵向的虚线
     *
     * @param canvas 用于绘制的画布对象
     */
    public void drawLine(Canvas canvas) {
        // 获取当前View的宽度和高度
        int currentWidth = getWidth();
        int currentHeight = getHeight();

        // 判断View的宽高比例决定绘制方向
        if (currentWidth > currentHeight) {
            // ========== 横向虚线绘制 ==========
            // 设置画笔宽度为View的高度（使线条填满高度）
            mPaintDash.setStrokeWidth(currentHeight);

            // 重置路径并设置起点和终点
            dashPath.reset();
            // 起点：左侧中点 (0, height/2)
            dashPath.moveTo(0, currentHeight/2);
            // 终点：右侧中点 (width, height/2)
            dashPath.lineTo(currentWidth, currentHeight/2);
        } else {
            // ========== 纵向虚线绘制 ==========
            // 设置画笔宽度为View的宽度（使线条填满宽度）
            mPaintDash.setStrokeWidth(currentWidth);

            // 重置路径并设置起点和终点
            dashPath.reset();
            // 起点：顶部中点 (width/2, 0)
            dashPath.moveTo(currentWidth/2, 0);
            // 终点：底部中点 (width/2, height)
            dashPath.lineTo(currentWidth/2, currentHeight);
        }

        // 在画布上绘制虚线路径
        canvas.drawPath(dashPath, mPaintDash);
    }

    /**
     * 绘制渐变Drawable到指定Canvas
     * 适用于Android 4.1(JELLY_BEAN)及以上版本
     *
     * @param canvas 目标画布，用于绘制渐变效果
     * @param rectf 绘制区域的矩形范围（包含left,top,right,bottom坐标）
     * @param cornerRadiusArr 圆角半径数组，用于定义四个角的圆角大小
     *      数组包含8个元素，分别表示4个角的x和y半径
     *      顺序：左上x,左上y,右上x,右上y,右下x,右下y,左下x,左下y
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void drawGradientDrawable(Canvas canvas, RectF rectf, float[] cornerRadiusArr) {

        // ========== 设置Drawable绘制边界 ==========
        // 将矩形区域的浮点坐标转换为整型坐标，设置Drawable的绘制范围
        // 注意：这里进行了强制类型转换，可能会丢失精度
        gradientDrawable.setBounds(
                (int) rectf.left,   // 左边界
                (int) rectf.top,    // 上边界
                (int) rectf.right,  // 右边界
                (int) rectf.bottom  // 下边界
        );

        // ========== 描边处理 ==========
        // 检查是否设置了描边颜色（-101表示未设置描边颜色）
        if (stroke_color != -101) {
            // 检查是否设置了虚线描边（stroke_dashWidth != -1表示需要绘制虚线）
            if (stroke_dashWidth != -1) {
                // 设置虚线描边效果：
                // 参数1: 描边宽度（四舍五入取整）
                // 参数2: 描边颜色（当前描边颜色）
                // 参数3: 虚线段的长度
                // 参数4: 虚线间隔的长度
                gradientDrawable.setStroke(
                        Math.round(stroke_with),  // 描边宽度（取整）
                        current_stroke_color,     // 描边颜色
                        stroke_dashWidth,         // 虚线长度
                        stroke_dashGap            // 虚线间隔
                );
            } else {
                // 设置实线描边效果：
                // 参数1: 描边宽度（四舍五入取整）
                // 参数2: 描边颜色
                gradientDrawable.setStroke(
                        Math.round(stroke_with),  // 描边宽度
                        current_stroke_color      // 描边颜色
                );
            }
        }

        // ========== 圆角处理 ==========
        // 设置圆角半径，cornerRadiusArr数组已经经过处理：
        // 1. 如果没有特殊角设置，也会返回一个统一圆角的数组
        // 2. 如果设置了特殊角，则包含各个角的独立圆角值
        gradientDrawable.setCornerRadii(cornerRadiusArr);

        // ========== 执行绘制 ==========
        // 将渐变Drawable绘制到指定的Canvas上
        gradientDrawable.draw(canvas);
    }

    private float[] getCornerValue(int trueHeight) {
        int leftTop;
        int rightTop;
        int rightBottom;
        int leftBottom;
        if (mCornerRadius_leftTop == -1) {
            leftTop = (int) mCornerRadius;
        } else {
            leftTop = (int) mCornerRadius_leftTop;
        }

        if (leftTop > trueHeight / 2) {
            leftTop = trueHeight / 2;
        }

        if (mCornerRadius_rightTop == -1) {
            rightTop = (int) mCornerRadius;
        } else {
            rightTop = (int) mCornerRadius_rightTop;
        }

        if (rightTop > trueHeight / 2) {
            rightTop = trueHeight / 2;
        }

        if (mCornerRadius_rightBottom == -1) {
            rightBottom = (int) mCornerRadius;
        } else {
            rightBottom = (int) mCornerRadius_rightBottom;
        }

        if (rightBottom > trueHeight / 2) {
            rightBottom = trueHeight / 2;
        }


        if (mCornerRadius_leftBottom == -1) {
            leftBottom = (int) mCornerRadius;
        } else {
            leftBottom = (int) mCornerRadius_leftBottom;
        }

        if (leftBottom > trueHeight / 2) {
            leftBottom = trueHeight / 2;
        }

        float[] outerR = new float[]{leftTop, leftTop, rightTop, rightTop, rightBottom, rightBottom, leftBottom, leftBottom};//左上，右上，右下，左下
        return outerR;
    }


    /**
     * 创建并设置波纹(Ripple)效果
     * 适用于Android 5.0(LOLLIPOP)及以上版本
     *
     * @param outRadius 圆角半径数组，用于定义波纹效果的形状边界
     *      数组包含8个元素，分别表示4个角的x和y半径
     *      顺序：左上x,左上y,右上x,右上y,右下x,右下y,左下x,左下y
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void ripple(float[] outRadius) {
        // ========== 状态和颜色定义 ==========
        // 定义视图的不同状态数组（按压、聚焦、激活、默认）
        int[][] stateList = new int[][]{
                new int[]{android.R.attr.state_pressed},  // 按压状态
                new int[]{android.R.attr.state_focused},  // 聚焦状态
                new int[]{android.R.attr.state_activated}, // 激活状态
                new int[]{}                                // 默认状态
        };

        // 定义不同状态对应的背景颜色
        int normalColor = mBackGroundColor;      // 默认状态颜色
        int pressedColor = mBackGroundColor_true; // 按压/聚焦/激活状态颜色

        // 状态颜色列表，顺序与stateList对应
        int[] stateColorList = new int[]{
                pressedColor,  // 按压状态颜色
                pressedColor,  // 聚焦状态颜色
                pressedColor,  // 激活状态颜色
                normalColor    // 默认状态颜色
        };

        // 创建颜色状态列表，将状态与颜色关联
        ColorStateList colorStateList = new ColorStateList(stateList, stateColorList);

        // ========== 波纹形状定义 ==========
        // 创建圆角矩形形状，用于限制波纹效果的范围
        // outRadius: 定义圆角半径
        // null: 表示不设置内矩形
        // null: 表示不设置内圆角半径
        RoundRectShape roundRectShape = new RoundRectShape(outRadius, null, null);

        // 创建形状Drawable作为波纹的遮罩
        ShapeDrawable maskDrawable = new ShapeDrawable();
        maskDrawable.setShape(roundRectShape);  // 设置形状
        maskDrawable.getPaint().setStyle(Paint.Style.FILL);  // 设置填充样式

        // ========== 描边处理 ==========
        // 如果设置了描边颜色（-101表示未设置）
        if (stroke_color != -101) {
            // 检查是否设置了虚线描边（stroke_dashWidth != -1表示设置了虚线）
            if (stroke_dashWidth != -1) {
                // 设置虚线描边：宽度、颜色、虚线长度、虚线间隔
                gradientDrawable.setStroke(
                        Math.round(stroke_with),     // 描边宽度（四舍五入取整）
                        current_stroke_color,        // 描边颜色
                        stroke_dashWidth,            // 虚线长度
                        stroke_dashGap               // 虚线间隔
                );
            } else {
                // 设置实线描边：宽度、颜色
                gradientDrawable.setStroke(
                        Math.round(stroke_with),     // 描边宽度
                        current_stroke_color         // 描边颜色
                );
            }
        }

        // ========== 圆角处理 ==========
        // 设置渐变Drawable的圆角半径（无论是否有特殊角都使用传入的outRadius数组）
        gradientDrawable.setCornerRadii(outRadius);

        // 如果设置了渐变起始颜色（-101表示未设置）
        if (startColor != -101) {
            // 应用渐变效果
            gradient(gradientDrawable);
        }

        // ========== 波纹效果创建 ==========
        // 创建波纹Drawable：
        // 参数1: 颜色状态列表（定义不同状态下的波纹颜色）
        // 参数2: 内容Drawable（默认状态下显示的内容）
        // 参数3: 遮罩Drawable（限制波纹显示范围的形状）
        RippleDrawable rippleDrawable = new RippleDrawable(
                colorStateList,    // 状态颜色列表
                gradientDrawable,  // 内容Drawable（背景）
                maskDrawable       // 遮罩Drawable（限制波纹范围）
        );

        // 将波纹效果设置为第一个子View的背景
        firstView.setBackground(rippleDrawable);
    }

    /**
     * 设置渐变背景效果
     * 功能：根据配置的渐变参数（颜色、角度）设置GradientDrawable的渐变效果
     * 适用版本：Android 4.1(JELLY_BEAN)及以上
     *
     * @param gradientDrawable 需要设置渐变效果的GradientDrawable对象
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void gradient(GradientDrawable gradientDrawable) {
        // 如果不可点击，则不设置渐变效果
        if (!isClickable) {
            return;
        }

        // ========== 渐变颜色配置 ==========
        // 根据是否设置了中间色(centerColor)决定使用两色还是三色渐变
        int[] colors;
        if (centerColor == -101) { // -101表示未设置中间色
            // 双色渐变：起始色 -> 结束色
            colors = new int[]{startColor, endColor};
        } else {
            // 三色渐变：起始色 -> 中间色 -> 结束色
            colors = new int[]{startColor, centerColor, endColor};
        }
        // 设置渐变颜色数组
        gradientDrawable.setColors(colors);

        // ========== 渐变角度处理 ==========
        // 处理负角度（转换为等效的正角度）
        if (angle < 0) {
            int trueAngle = angle % 360; // 取模得到-359~0之间的值
            angle = trueAngle + 360;    // 转换为0~359之间的正角度
        }

        // 计算实际角度对应的45度区间（0-7分别代表0°-315°，每45°一个区间）
        int trueAngle = angle % 360;    // 确保角度在0-359范围内
        int angleFlag = trueAngle / 45; // 计算属于哪个45度区间（0-7）

        // 根据角度区间设置渐变方向
        switch (angleFlag) {
            case 0: // 0°-44° -> 从左到右渐变
                gradientDrawable.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
                break;

            case 1: // 45°-89° -> 从左下到右上渐变
                gradientDrawable.setOrientation(GradientDrawable.Orientation.BL_TR);
                break;

            case 2: // 90°-134° -> 从下到上渐变
                gradientDrawable.setOrientation(GradientDrawable.Orientation.BOTTOM_TOP);
                break;

            case 3: // 135°-179° -> 从右下到左上渐变
                gradientDrawable.setOrientation(GradientDrawable.Orientation.BR_TL);
                break;

            case 4: // 180°-224° -> 从右到左渐变
                gradientDrawable.setOrientation(GradientDrawable.Orientation.RIGHT_LEFT);
                break;

            case 5: // 225°-269° -> 从右上到左下渐变
                gradientDrawable.setOrientation(GradientDrawable.Orientation.TR_BL);
                break;

            case 6: // 270°-314° -> 从上到下渐变
                gradientDrawable.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
                break;

            case 7: // 315°-359° -> 从左上到右下渐变
                gradientDrawable.setOrientation(GradientDrawable.Orientation.TL_BR);
                break;
        }
    }

    private int dip2px(float dipValue) {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    private void setPadding() {
        if (isShowShadow && mShadowLimit > 0) {
            //控件区域是否对称，默认是对称。不对称的话，那么控件区域随着阴影区域走
            if (isSym) {
                int xPadding = (int) (mShadowLimit + Math.abs(mDx));
                int yPadding = (int) (mShadowLimit + Math.abs(mDy));

                if (leftShow) {
                    leftPadding = xPadding;
                } else {
                    leftPadding = 0;
                }

                if (topShow) {
                    topPadding = yPadding;
                } else {
                    topPadding = 0;
                }


                if (rightShow) {
                    rightPadding = xPadding;
                } else {
                    rightPadding = 0;
                }

                if (bottomShow) {
                    bottomPadding = yPadding;
                } else {
                    bottomPadding = 0;
                }


            } else {
                if (Math.abs(mDy) > mShadowLimit) {
                    if (mDy > 0) {
                        mDy = mShadowLimit;
                    } else {
                        mDy = 0 - mShadowLimit;
                    }
                }


                if (Math.abs(mDx) > mShadowLimit) {
                    if (mDx > 0) {
                        mDx = mShadowLimit;
                    } else {
                        mDx = 0 - mShadowLimit;
                    }
                }

                if (topShow) {
                    topPadding = (int) (mShadowLimit - mDy);
                } else {
                    topPadding = 0;
                }

                if (bottomShow) {
                    bottomPadding = (int) (mShadowLimit + mDy);
                } else {
                    bottomPadding = 0;
                }


                if (rightShow) {
                    rightPadding = (int) (mShadowLimit - mDx);
                } else {
                    rightPadding = 0;
                }


                if (leftShow) {
                    leftPadding = (int) (mShadowLimit + mDx);
                } else {
                    leftPadding = 0;
                }
            }
            setPadding(leftPadding, topPadding, rightPadding, bottomPadding);
        }
    }

    /**
     * 设置View背景的兼容方法，处理不同API版本的背景设置
     * 主要功能：
     * 1. 显示阴影效果时创建阴影Bitmap并设置为背景
     * 2. 不显示阴影时根据条件设置普通背景或透明背景
     *
     * @param w View的宽度
     * @param h View的高度
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @SuppressWarnings("deprecation") // 抑制setBackgroundDrawable已弃用的警告
    private void setBackgroundCompat(int w, int h) {
        // ========== 显示阴影效果处理 ==========
        if (isShowShadow) {
            // 检查阴影颜色是否有透明度，如果没有则添加默认透明度
            isAddAlpha(mShadowColor);

            // 创建阴影Bitmap：
            // 参数说明：宽度、高度、圆角半径、阴影范围、X偏移、Y偏移、阴影颜色、填充色(透明)
            Bitmap bitmap = createShadowBitmap(w, h, mCornerRadius, mShadowLimit,
                    mDx, mDy, mShadowColor, Color.TRANSPARENT);

            // 将Bitmap转换为Drawable
            BitmapDrawable drawable = new BitmapDrawable(bitmap);

            // 根据API版本选择设置背景的方法
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
                setBackgroundDrawable(drawable); // JELLY_BEAN及以下使用旧方法
            } else {
                setBackground(drawable); // JELLY_BEAN以上使用新方法
            }
        }
        // ========== 不显示阴影效果处理 ==========
        else {
            // 检查是否有子View
            if (getChildAt(0) == null) {
                // 没有子View时处理
                if (layoutBackground != null) {
                    // 将当前View设为firstView
                    firstView = ShadowLayout.this;

                    // 根据可点击状态设置背景
                    if (isClickable) {
                        setmBackGround(layoutBackground, "setBackgroundCompat");
                    } else {
                        changeSwitchClickable(); // 不可点击时切换状态
                    }
                } else {
                    // 没有设置背景图片时，设置透明背景
                    // 解决不执行onDraw方法的bug
                    this.setBackgroundColor(Color.parseColor("#00000000"));
                }
            } else {
                // 有子View时直接设置透明背景
                this.setBackgroundColor(Color.parseColor("#00000000"));
            }
        }
    }

    /**
     * 创建带有阴影效果的Bitmap
     *
     * @param shadowWidth 阴影宽度（原始尺寸）
     * @param shadowHeight 阴影高度（原始尺寸）
     * @param cornerRadius 圆角半径（原始值）
     * @param shadowRadius 阴影模糊半径（原始值）
     * @param dx 阴影水平偏移量（原始值）
     * @param dy 阴影垂直偏移量（原始值）
     * @param shadowColor 阴影颜色
     * @param fillColor 填充颜色
     * @return 生成的阴影Bitmap
     */
    private Bitmap createShadowBitmap(
            int shadowWidth,
            int shadowHeight,
            float cornerRadius,
            float shadowRadius,
            float dx,
            float dy,
            int shadowColor,
            int fillColor
    ) {
        // ========== 尺寸优化部分 ==========
        // 为了优化性能，将阴影Bitmap的尺寸缩小至原来的1/4
        // 同时按比例调整所有相关参数

        // 调整阴影偏移量
        dx = dx / 4;
        dy = dy / 4;

        // 调整阴影尺寸（确保至少为1像素）
        shadowWidth = shadowWidth / 4 == 0 ? 1 : shadowWidth / 4;
        shadowHeight = shadowHeight / 4 == 0 ? 1 : shadowHeight / 4;

        // 调整圆角和阴影模糊半径
        cornerRadius = cornerRadius / 4;
        shadowRadius = shadowRadius / 4;

        // ========== 创建画布和Bitmap ==========
        // 使用ARGB_4444配置创建Bitmap（更节省内存）
        Bitmap output = Bitmap.createBitmap(shadowWidth, shadowHeight, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(output);

        // ========== 计算阴影矩形区域 ==========
        // 根据各边是否显示阴影，计算实际的矩形绘制区域

        float rect_left = 0;
        float rect_right = 0;
        float rect_top = 0;
        float rect_bottom = 0;

        // 左边阴影处理
        if (leftShow) {
            // 如果显示左边阴影，留出阴影半径的空间
            rect_left = shadowRadius;
        } else {
            // 如果不显示左边阴影，计算最大圆角半径
            float maxLeftTop = Math.max(cornerRadius, mCornerRadius_leftTop);
            float maxLeftBottom = Math.max(cornerRadius, mCornerRadius_leftBottom);
            float maxLeft = Math.max(maxLeftTop, maxLeftBottom);
            // 取圆角半径和阴影半径中的较大值
            float trueMaxLeft = Math.max(maxLeft, shadowRadius);
            // 最终左边距取计算值的一半（经验值，可能需要调整）
            rect_left = trueMaxLeft / 2;
        }

        // 上边阴影处理（逻辑与左边类似）
        if (topShow) {
            rect_top = shadowRadius;
        } else {
            float maxLeftTop = Math.max(cornerRadius, mCornerRadius_leftTop);
            float maxRightTop = Math.max(cornerRadius, mCornerRadius_rightTop);
            float maxTop = Math.max(maxLeftTop, maxRightTop);
            float trueMaxTop = Math.max(maxTop, shadowRadius);
            rect_top = trueMaxTop / 2;
        }

        // 右边阴影处理
        if (rightShow) {
            // 如果显示右边阴影，留出阴影半径的空间
            rect_right = shadowWidth - shadowRadius;
        } else {
            // 如果不显示右边阴影，计算最大圆角半径
            float maxRightTop = Math.max(cornerRadius, mCornerRadius_rightTop);
            float maxRightBottom = Math.max(cornerRadius, mCornerRadius_rightBottom);
            float maxRight = Math.max(maxRightTop, maxRightBottom);
            float trueMaxRight = Math.max(maxRight, shadowRadius);
            // 最终右边距为宽度减去计算值的一半
            rect_right = shadowWidth - trueMaxRight / 2;
        }

        // 下边阴影处理（逻辑与右边类似）
        if (bottomShow) {
            rect_bottom = shadowHeight - shadowRadius;
        } else {
            float maxLeftBottom = Math.max(cornerRadius, mCornerRadius_leftBottom);
            float maxRightBottom = Math.max(cornerRadius, mCornerRadius_rightBottom);
            float maxBottom = Math.max(maxLeftBottom, maxRightBottom);
            float trueMaxBottom = Math.max(maxBottom, shadowRadius);
            rect_bottom = shadowHeight - trueMaxBottom / 2;
        }

        // 创建最终的阴影矩形区域
        RectF shadowRect = new RectF(
                rect_left,
                rect_top,
                rect_right,
                rect_bottom);

        // ========== 偏移量处理 ==========
        // 根据对称性设置调整矩形区域

        if (isSym) {
            // 对称模式：在矩形四周均匀应用偏移量
            if (dy > 0) {
                shadowRect.top += dy;
                shadowRect.bottom -= dy;
            } else if (dy < 0) {
                shadowRect.top += Math.abs(dy);
                shadowRect.bottom -= Math.abs(dy);
            }

            if (dx > 0) {
                shadowRect.left += dx;
                shadowRect.right -= dx;
            } else if (dx < 0) {
                shadowRect.left += Math.abs(dx);
                shadowRect.right -= Math.abs(dx);
            }
        } else {
            // 非对称模式：直接应用偏移量
            shadowRect.top -= dy;
            shadowRect.bottom -= dy;
            shadowRect.right -= dx;
            shadowRect.left -= dx;
        }

        // ========== 绘制阴影 ==========
        // 设置填充颜色
        shadowPaint.setColor(fillColor);

        // 如果不是在编辑模式下，设置阴影层
        if (!isInEditMode()) {
            // 注意：这里shadowRadius除以2是经验值，可能需要根据实际效果调整
            shadowPaint.setShadowLayer(shadowRadius / 2, dx, dy, shadowColor);
        }

        // ========== 圆角处理 ==========
        // 检查是否设置了各角的独立圆角半径
        if (mCornerRadius_leftBottom == -1 && mCornerRadius_leftTop == -1
                && mCornerRadius_rightTop == -1 && mCornerRadius_rightBottom == -1) {
            // 如果没有设置独立圆角，使用统一的圆角半径绘制圆角矩形
            canvas.drawRoundRect(shadowRect, cornerRadius, cornerRadius, shadowPaint);
        } else {
            // 如果设置了独立圆角，需要分别处理每个角的圆角半径

            // 设置抗锯齿
            shadowPaint.setAntiAlias(true);

            // 处理左上角圆角半径
            int leftTop;
            if (mCornerRadius_leftTop == -1) {
                leftTop = (int) mCornerRadius / 4;  // 使用默认值
            } else {
                leftTop = (int) mCornerRadius_leftTop / 4;  // 使用自定义值
            }

            // 处理左下角圆角半径
            int leftBottom;
            if (mCornerRadius_leftBottom == -1) {
                leftBottom = (int) mCornerRadius / 4;
            } else {
                leftBottom = (int) mCornerRadius_leftBottom / 4;
            }

            // 处理右上角圆角半径
            int rightTop;
            if (mCornerRadius_rightTop == -1) {
                rightTop = (int) mCornerRadius / 4;
            } else {
                rightTop = (int) mCornerRadius_rightTop / 4;
            }

            // 处理右下角圆角半径
            int rightBottom;
            if (mCornerRadius_rightBottom == -1) {
                rightBottom = (int) mCornerRadius / 4;
            } else {
                rightBottom = (int) mCornerRadius_rightBottom / 4;
            }

            // 创建圆角数组（每对值表示一个角的x和y半径）
            // 顺序：左上、右上、右下、左下
            float[] outerR = new float[]{
                    leftTop, leftTop,     // 左上
                    rightTop, rightTop,   // 右上
                    rightBottom, rightBottom, // 右下
                    leftBottom, leftBottom    // 左下
            };

            // 使用Path绘制自定义圆角的矩形
            Path path = new Path();
            path.addRoundRect(shadowRect, outerR, Path.Direction.CW);
            canvas.drawPath(path, shadowPaint);
        }

        return output;
    }

    /**
     * 检查并添加颜色透明度
     * 功能：当颜色没有透明度(alpha)值时，自动添加默认透明度(0x2A)
     *
     * @param color 要检查的颜色值
     */
    private void isAddAlpha(int color) {
        // 检查当前颜色的alpha通道值(255表示完全不透明)
        if (Color.alpha(color) == 255) {
            // 分解RGB通道值并转为16进制字符串
            String red = Integer.toHexString(Color.red(color));   // 红色通道
            String green = Integer.toHexString(Color.green(color)); // 绿色通道
            String blue = Integer.toHexString(Color.blue(color));  // 蓝色通道

            // 处理单字符的16进制值（补前导0）
            if (red.length() == 1) {
                red = "0" + red;
            }
            if (green.length() == 1) {
                green = "0" + green;
            }
            if (blue.length() == 1) {
                blue = "0" + blue;
            }

            // 组合新的颜色值（添加默认透明度0x2A）
            String endColor = "#2a" + red + green + blue;

            // 转换回颜色整型并保存到成员变量
            mShadowColor = convertToColorInt(endColor);
        }
    }

    /**
     * 将颜色字符串转换为颜色整型值
     * 功能：处理带或不带#号的颜色字符串，并转换为ColorInt
     *
     * @param argb 颜色字符串(支持格式：#RRGGBB #AARRGGBB)
     * @return 颜色整型值
     * @throws IllegalArgumentException 当颜色字符串格式错误时抛出
     */
    private static int convertToColorInt(String argb) throws IllegalArgumentException {
        // 自动补全#前缀
        if (!argb.startsWith("#")) {
            argb = "#" + argb;
        }
        // 使用系统方法解析颜色
        return Color.parseColor(argb);
    }

    /**
     * 设置View的背景并处理圆角
     * 功能：根据圆角设置自动选择统一圆角或各角独立圆角的背景设置方式
     *
     * @param drawable 要设置的背景Drawable
     * @param currentTag 当前操作标识(用于日志跟踪)
     */
    private void setmBackGround(Drawable drawable, String currentTag) {
        // 设置操作标识到View的Tag中
        firstView.setTag(R.id.action_container, currentTag);

        // 检查View和Drawable有效性
        if (firstView != null && drawable != null) {
            // 判断是否设置了特殊圆角(-1表示未设置)
            if (mCornerRadius_leftTop == -1 && mCornerRadius_leftBottom == -1
                    && mCornerRadius_rightTop == -1 && mCornerRadius_rightBottom == -1) {
                // 情况1：未设置特殊圆角，使用统一圆角
                GlideRoundUtils.setRoundCorner(firstView, drawable, mCornerRadius, currentTag);
            } else {
                // 情况2：设置了特殊圆角，分别处理每个角的圆角半径

                // 左上角圆角处理：未设置则使用统一圆角
                int leftTop = (mCornerRadius_leftTop == -1)
                        ? (int) mCornerRadius
                        : (int) mCornerRadius_leftTop;

                // 左下角圆角处理
                int leftBottom = (mCornerRadius_leftBottom == -1)
                        ? (int) mCornerRadius
                        : (int) mCornerRadius_leftBottom;

                // 右上角圆角处理
                int rightTop = (mCornerRadius_rightTop == -1)
                        ? (int) mCornerRadius
                        : (int) mCornerRadius_rightTop;

                // 右下角圆角处理
                int rightBottom = (mCornerRadius_rightBottom == -1)
                        ? (int) mCornerRadius
                        : (int) mCornerRadius_rightBottom;

                // 使用各角独立圆角设置方法
                GlideRoundUtils.setCorners(
                        firstView,       // 目标View
                        drawable,        // 背景Drawable
                        leftTop,         // 左上角半径
                        leftBottom,      // 左下角半径
                        rightTop,        // 右上角半径
                        rightBottom,     // 右下角半径
                        currentTag);     // 操作标识
            }
        }
    }

    /**
     * 设置View的可点击状态
     * 功能：控制View的点击交互状态，并同步更新UI样式
     * 适用版本：Android 4.1(JELLY_BEAN)及以上
     *
     * @param clickable 是否可点击
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void setClickable(boolean clickable) {
        // 1. 检查是否为虚线模式（虚线模式不支持点击状态变化）
        isExceptionByDashLine();

        // 2. 调用父类方法设置基础点击状态
        super.setClickable(clickable);

        // 3. 更新当前点击状态标志
        this.isClickable = clickable;

        // 4. 根据点击状态切换UI样式
        changeSwitchClickable();

        // 5. 处理点击事件监听器
        if (isClickable) {
            super.setOnClickListener(onClickListener);
        }

        // 6. 渐变背景特殊处理
        if (gradientDrawable != null) {
            if (startColor != -101 && endColor != -101) { // -101表示未设置颜色
                gradient(gradientDrawable); // 重新应用渐变效果
            }
        }
    }

    /**
     * 设置渐变颜色和渐变方向
     *
     * @param startColor
     * @param endColor
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public ShadowLayout setGradientColor(int startColor, int endColor) {
        setGradientColor(angle, startColor, endColor);
        return this;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public ShadowLayout setGradientColor(int angle, int startColor, int endColor) {

        setGradientColor(angle, startColor, -101, endColor);
        return this;
    }

    /**
     * 设置渐变色背景
     * 功能：配置渐变色的角度和颜色参数，并立即应用渐变效果
     * 适用版本：Android 4.1(JELLY_BEAN)及以上
     *
     * @param angle 渐变角度（必须为45的倍数）
     * @param startColor 渐变起始色（ARGB格式）
     * @param centerColor 渐变中间色（ARGB格式，可选，传0忽略）
     * @param endColor 渐变结束色（ARGB格式）
     * @return 返回当前ShadowLayout实例（支持链式调用）
     * @throws IllegalArgumentException 当角度不是45的倍数时抛出
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public ShadowLayout setGradientColor(int angle, int startColor, int centerColor, int endColor) {
        // 1. 检查是否为虚线模式（虚线模式不支持渐变色）
        isExceptionByDashLine();

        // 2. 验证渐变角度有效性（必须是45的倍数）
        if (angle % 45 != 0) {
            throw new IllegalArgumentException("Linear gradient requires 'angle' attribute to be a multiple of 45");
        }

        // 3. 保存渐变参数到成员变量
        this.angle = angle;
        this.startColor = startColor;
        this.centerColor = centerColor;
        this.endColor = endColor;

        // 4. 应用渐变效果到Drawable
        gradient(gradientDrawable);

        // 5. 请求重绘View
        postInvalidate();

        // 6. 返回当前实例支持链式调用
        return this;
    }

    private void isExceptionByDashLine() {
        if (isDashLine()) {
            throw new RuntimeException("shapeMode为MODE_DASHLINE,不允许设置此属性");
        }
    }

    /**
     * 是否隐藏阴影
     *
     * @param isShowShadow
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public ShadowLayout setShadowHidden(boolean isShowShadow) {
        isExceptionByDashLine();
        this.isShowShadow = !isShowShadow;
        setPadding();
        if (getWidth() != 0 && getHeight() != 0) {
            setBackgroundCompat(getWidth(), getHeight());
        }
        return this;
    }

    /**
     * 设置x轴阴影的偏移量
     *
     * @param mDx
     */
    public ShadowLayout setShadowOffsetX(float mDx) {
        isExceptionByDashLine();
        if (isShowShadow) {
            if (Math.abs(mDx) > mShadowLimit) {
                if (mDx > 0) {
                    this.mDx = mShadowLimit;
                } else {
                    this.mDx = -mShadowLimit;
                }
            } else {
                this.mDx = mDx;
            }
            setPadding();
        }
        return this;
    }

    /**
     * 设置y轴阴影的偏移量
     *
     * @param mDy
     */
    public ShadowLayout setShadowOffsetY(float mDy) {
        isExceptionByDashLine();
        if (isShowShadow) {
            if (Math.abs(mDy) > mShadowLimit) {
                if (mDy > 0) {
                    this.mDy = mShadowLimit;
                } else {
                    this.mDy = -mShadowLimit;
                }
            } else {
                this.mDy = mDy;
            }
            setPadding();
        }

        return this;
    }

    /**
     * 获取当前的圆角值
     *
     * @return
     */
    public float getCornerRadius() {
        return mCornerRadius;
    }

    /**
     * 设置shadowLayout圆角
     *
     * @param mCornerRadius
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public ShadowLayout setCornerRadius(int mCornerRadius) {
        isExceptionByDashLine();
        this.mCornerRadius = mCornerRadius;
        if (getWidth() != 0 && getHeight() != 0) {
            setBackgroundCompat(getWidth(), getHeight());
        }
        return this;
    }

    /**
     * 获取阴影扩散区域值
     *
     * @return
     */
    public float getShadowLimit() {
        return mShadowLimit;
    }

    /**
     * 设置阴影扩散区域
     *
     * @param mShadowLimit
     */
    public ShadowLayout setShadowLimit(int mShadowLimit) {
        isExceptionByDashLine();
        this.mShadowLimit = mShadowLimit;
        if (isShowShadow) {
            setPadding();
        }
        return this;
    }

    /**
     * 设置阴影颜色值
     *
     * @param mShadowColor
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public ShadowLayout setShadowColor(int mShadowColor) {
        isExceptionByDashLine();
        this.mShadowColor = mShadowColor;
        if (getWidth() != 0 && getHeight() != 0) {
            setBackgroundCompat(getWidth(), getHeight());
        }
        return this;
    }


    /**
     * 单独设置4个圆角属性
     *
     * @param leftTop
     * @param rightTop
     * @param leftBottom
     * @param rightBottom
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public ShadowLayout setSpecialCorner(int leftTop, int rightTop, int leftBottom, int rightBottom) {
        isExceptionByDashLine();
        mCornerRadius_leftTop = leftTop;
        mCornerRadius_rightTop = rightTop;
        mCornerRadius_leftBottom = leftBottom;
        mCornerRadius_rightBottom = rightBottom;
        if (getWidth() != 0 && getHeight() != 0) {
            setBackgroundCompat(getWidth(), getHeight());
        }
        return this;
    }


    /**
     * 单独隐藏某边
     * setShadowHiddenTop：是否隐藏阴影的上边部分
     *
     * @param topShow
     */
    public ShadowLayout setShadowHiddenTop(boolean topShow) {
        isExceptionByDashLine();
        this.topShow = !topShow;
        setPadding();
        return this;
    }

    public ShadowLayout setShadowHiddenBottom(boolean bottomShow) {
        isExceptionByDashLine();
        this.bottomShow = !bottomShow;
        setPadding();
        return this;
    }


    public ShadowLayout setShadowHiddenRight(boolean rightShow) {
        isExceptionByDashLine();
        this.rightShow = !rightShow;
        setPadding();
        return this;
    }


    public ShadowLayout setShadowHiddenLeft(boolean leftShow) {
        isExceptionByDashLine();
        this.leftShow = !leftShow;
        setPadding();
        return this;
    }


    /**
     * 设置背景颜色值
     *
     * @param color
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public ShadowLayout setLayoutBackground(int color) {
        isExceptionByDashLine();
        //如果设置了clickable为false，那么不允许动态更换背景
        if (!isClickable) {
            return this;
        }

        if (layoutBackground_true != null) {
            throw new UnsupportedOperationException("使用了ShadowLayout_hl_layoutBackground_true属性，要与ShadowLayout_hl_layoutBackground属性统一为颜色");
        }
        mBackGroundColor = color;
        //代码设置背景色后，将渐变色重置
        this.startColor = -101;
        this.centerColor = -101;
        this.endColor = -101;
        if (shapeModeType == ShadowLayout.MODE_SELECTED) {
            //select模式
            if (!this.isSelected()) {
                gradientDrawable.setColors(new int[]{mBackGroundColor, mBackGroundColor});
            }
        } else {
            gradientDrawable.setColors(new int[]{mBackGroundColor, mBackGroundColor});
        }
        postInvalidate();

        return this;
    }

    /**
     * 设置选中背景颜色值
     *
     * @param color
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public ShadowLayout setLayoutBackgroundTrue(int color) {
        isExceptionByDashLine();
        if (layoutBackground != null) {
            throw new UnsupportedOperationException("使用了ShadowLayout_hl_layoutBackground属性，要与ShadowLayout_hl_layoutBackground_true属性统一为颜色");
        }
        mBackGroundColor_true = color;
        if (shapeModeType == ShadowLayout.MODE_SELECTED) {
            //select模式
            if (this.isSelected()) {
                gradientDrawable.setColors(new int[]{mBackGroundColor_true, mBackGroundColor_true});
            }
        }
        postInvalidate();
        return this;
    }


    /**
     * 设置边框颜色值
     *
     * @param color
     */
    public ShadowLayout setStrokeColor(int color) {
        isExceptionByDashLine();
        stroke_color = color;
        if (shapeModeType == ShadowLayout.MODE_SELECTED) {
            //select模式
            if (!this.isSelected()) {
                current_stroke_color = stroke_color;
            }
        } else {
            current_stroke_color = stroke_color;
        }
        postInvalidate();
        return this;
    }


    /**
     * 设置选中边框颜色值
     *
     * @param color
     */
    public ShadowLayout setStrokeColorTrue(int color) {
        isExceptionByDashLine();
        stroke_color_true = color;
        if (shapeModeType == ShadowLayout.MODE_SELECTED) {
            //select模式
            if (this.isSelected()) {
                current_stroke_color = stroke_color_true;
            }
        }
        postInvalidate();
        return this;
    }

    /**
     * 设置边框宽度
     *
     * @param stokeWidth
     */
    public ShadowLayout setStrokeWidth(float stokeWidth) {
        isExceptionByDashLine();
        this.stroke_with = stokeWidth;
        postInvalidate();
        return this;
    }
}
