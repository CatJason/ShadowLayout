package com.lihang;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

/**
 * Created by leo
 * on 2020/8/3.
 */
class GlideRoundUtils {

    // Add this constant if you don't have it in your R.id
    private static final int ACTION_CONTAINER_ID = android.R.id.content; // Replace with your actual ID

    public static void setRoundCorner(final View view, final Drawable resourceId, final float cornerDipValue, final String currentTag) {
        if (view == null || resourceId == null) {
            return;
        }

        final float cornerRadiusPx = cornerDipValue * view.getResources().getDisplayMetrics().density;

        view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                view.removeOnLayoutChangeListener(this);
                applyBackground(view, resourceId, cornerRadiusPx, currentTag);
            }
        });

        if (view.isLaidOut()) {
            applyBackground(view, resourceId, cornerRadiusPx, currentTag);
        }
    }

    private static void applyBackground(View view, Drawable drawable, float cornerRadiusPx, String currentTag) {
        String lastTag = (String) view.getTag(ACTION_CONTAINER_ID);
        if (lastTag != null && !lastTag.equals(currentTag)) {
            return;
        }

        try {
            Drawable background = drawable;

            if (cornerRadiusPx > 0) {
                Bitmap bitmap = drawableToBitmap(drawable, view.getMeasuredWidth(), view.getMeasuredHeight());
                if (bitmap != null) {
                    RoundedBitmapDrawable roundedDrawable = RoundedBitmapDrawableFactory.create(
                            view.getResources(), bitmap);
                    roundedDrawable.setCornerRadius(cornerRadiusPx);
                    roundedDrawable.setAntiAlias(true);
                    background = roundedDrawable;
                }
            }

            setBackgroundCompat(view, background);
        } catch (OutOfMemoryError e) {
            // Fall back to original drawable if OOM occurs
            setBackgroundCompat(view, drawable);
        }
    }

    private static Bitmap drawableToBitmap(Drawable drawable, int width, int height) {
        if (drawable == null || width <= 0 || height <= 0) {
            return null;
        }

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        try {
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

    public static void setCorners(final View view, final Drawable resourceId,
                                  final float leftTop_corner, final float leftBottom_corner,
                                  final float rightTop_corner, final float rightBottom_corner,
                                  final String currentTag) {
        if (view == null || resourceId == null) {
            return;
        }

        final float leftTopPx = leftTop_corner * view.getResources().getDisplayMetrics().density;
        final float leftBottomPx = leftBottom_corner * view.getResources().getDisplayMetrics().density;
        final float rightTopPx = rightTop_corner * view.getResources().getDisplayMetrics().density;
        final float rightBottomPx = rightBottom_corner * view.getResources().getDisplayMetrics().density;

        view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                view.removeOnLayoutChangeListener(this);
                applyCornersBackground(view, resourceId, leftTopPx, leftBottomPx,
                        rightTopPx, rightBottomPx, currentTag);
            }
        });

        if (view.isLaidOut()) {
            applyCornersBackground(view, resourceId, leftTopPx, leftBottomPx,
                    rightTopPx, rightBottomPx, currentTag);
        }
    }

    private static void applyCornersBackground(View view, Drawable drawable,
                                               float leftTop, float leftBottom,
                                               float rightTop, float rightBottom,
                                               String currentTag) {
        String lastTag = (String) view.getTag(ACTION_CONTAINER_ID);
        if (lastTag != null && !lastTag.equals(currentTag)) {
            return;
        }

        if (leftTop == 0 && leftBottom == 0 && rightTop == 0 && rightBottom == 0) {
            setBackgroundCompat(view, drawable);
            return;
        }

        try {
            Bitmap bitmap = drawableToBitmap(drawable, view.getMeasuredWidth(), view.getMeasuredHeight());
            if (bitmap == null) {
                setBackgroundCompat(view, drawable);
                return;
            }

            Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setShader(new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

            Path path = new Path();
            float[] radii = {
                    leftTop, leftTop,
                    rightTop, rightTop,
                    rightBottom, rightBottom,
                    leftBottom, leftBottom
            };

            RectF rect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
            path.addRoundRect(rect, radii, Path.Direction.CW);

            canvas.drawPath(path, paint);

            BitmapDrawable roundedDrawable = new BitmapDrawable(view.getResources(), output);
            setBackgroundCompat(view, roundedDrawable);
        } catch (OutOfMemoryError e) {
            // Fall back to original drawable if OOM occurs
            setBackgroundCompat(view, drawable);
        }
    }

    private static void setBackgroundCompat(View view, Drawable drawable) {
        if (view == null || drawable == null) {
            return;
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackgroundDrawable(drawable);
        } else {
            view.setBackground(drawable);
        }
    }
}