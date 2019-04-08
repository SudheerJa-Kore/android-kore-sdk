package kore.botssdk.view.viewUtils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;

import com.squareup.picasso.Transformation;

import kore.botssdk.application.AppControl;

import static kore.botssdk.view.viewUtils.DimensionUtil.dp1;

/**
 * Created by Shiva Krishna on 3/14/2018.
 */

public class KaRoundedCornersTransform implements Transformation {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    @Override
    public Bitmap transform(Bitmap source) {
        if(source == null){
            source = Bitmap.createBitmap((int)AppControl.getInstance().getDimensionUtil().dp1*40,
                    (int)AppControl.getInstance().getDimensionUtil().dp1*40, Bitmap.Config.ARGB_8888);
            source.eraseColor(Color.DKGRAY);
        }
        Bitmap result = Bitmap.createBitmap(source.getWidth(), source.getHeight(), source.getConfig());
        Canvas canvas = new Canvas(result);

        paint.setXfermode(null);
        paint.setAlpha(255);

        RectF rectF = new RectF(0, 0, source.getWidth(), source.getHeight());
        canvas.drawRoundRect(rectF, 4 * dp1, 4 * dp1, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(source, 0, 0, paint);

        source.recycle();
        return result;
    }

    @Override
    public String key() {
        return "RoundedTransformation()";
    }
}