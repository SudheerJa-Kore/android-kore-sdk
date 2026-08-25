package kore.botssdk.view.viewUtils;

/*
 * Copyright (c) 2014 Kore Inc. All rights reserved.
 */
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.security.MessageDigest;


public class RoundedCornersTransform extends BitmapTransformation {
    public void setRadius(float radius) {
        this.radius = radius;
    }

    private float radius = 0;

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap source, int outWidth, int outHeight) {
        try {
            int size = Math.min(source.getWidth(), source.getHeight());

            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;

            // Crop the image to a square
            Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);

            // Ensure the config is valid
            Bitmap.Config config = source.getConfig() != null ? source.getConfig() : Bitmap.Config.ARGB_8888;

            // Create the output bitmap from pool if possible
            Bitmap bitmap = pool.get(size, size, config);

            // Set up canvas and paint
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            paint.setAntiAlias(true);

            // Create the shader
            BitmapShader shader = new BitmapShader(squaredBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            paint.setShader(shader);

            // Corner radius
            float r = radius > 0 ? radius : size / 8f;
            canvas.drawRoundRect(new RectF(0f, 0f, size, size), r, r, paint);

            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return source;
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update(("rounded_corners" + radius).getBytes(CHARSET));
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof RoundedCornersTransform && ((RoundedCornersTransform) o).radius == radius;
    }

    @Override
    public int hashCode() {
        return ("rounded_corners" + radius).hashCode();
    }
}