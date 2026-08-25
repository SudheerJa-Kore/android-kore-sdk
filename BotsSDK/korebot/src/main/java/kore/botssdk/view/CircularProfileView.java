package kore.botssdk.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.File;

import kore.botssdk.R;
import kore.botssdk.drawables.ProfileDrawable;
import kore.botssdk.utils.StringConstants;
import kore.botssdk.view.viewUtils.DimensionUtil;

/*
 * Copyright (c) 2014 Kore Inc. All rights reserved.
 */
@SuppressWarnings("UnKnownNullness")
public class CircularProfileView extends RoundedImageView {
    private Paint borderPaint;
    private float width, height;
    int dp1, SDK;
    private int DEFAULT_HEIGHT;
    private int DEFAULT_WIDTH;
    private int CPV_TEXT_SIZE;
    int borderStrokeWidth = 0;
    int borderColor = 0xffffffff;
    boolean hasBorder;
    String initials;
    int profileColor;

    public CircularProfileView(Context context) {
        super(context);
        init(null, context);
    }

    public CircularProfileView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, context);
    }

    public CircularProfileView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, context);
    }

    private void init(AttributeSet attrs, Context context) {

        //Essentials
        if (!isInEditMode()) {
            dp1 = (int) DimensionUtil.dp1;
            SDK = android.os.Build.VERSION.SDK_INT;
            DEFAULT_HEIGHT = dp1 * 52;
            DEFAULT_WIDTH = dp1 * 52;
            borderPaint = new Paint();
        }

        int PROFILE_DRAWABLE_PADDING;
        if (attrs != null) {
            //init attrs
            TypedArray attr = context.obtainStyledAttributes(attrs, R.styleable.CircularProfileView, 0, 0);
            CPV_TEXT_SIZE = attr.getDimensionPixelSize(R.styleable.CircularProfileView_text_size, dp1 * 17);
            PROFILE_DRAWABLE_PADDING = (int) attr.getDimension(R.styleable.CircularProfileView_profile_drawable_padding, 0);
            hasBorder = attr.getBoolean(R.styleable.CircularProfileView_has_border, false);
            borderColor = attr.getColor(R.styleable.CircularProfileView_border_color, 0xffffffff);
            borderStrokeWidth = attr.getInt(R.styleable.CircularProfileView_border_width, 0);
            attr.recycle();
        } else {
            CPV_TEXT_SIZE = 17 * dp1;
            PROFILE_DRAWABLE_PADDING = 0;
        }

        setPadding(PROFILE_DRAWABLE_PADDING, PROFILE_DRAWABLE_PADDING, PROFILE_DRAWABLE_PADDING, PROFILE_DRAWABLE_PADDING);

        int[] attrsArray = new int[]{
                android.R.attr.id, // 0
                android.R.attr.layout_width, // 1
                android.R.attr.layout_height // 2
        };
        TypedArray ta = context.obtainStyledAttributes(attrs, attrsArray);
        width = ta.getDimensionPixelSize(1, DEFAULT_WIDTH);
        height = ta.getDimensionPixelSize(2, DEFAULT_HEIGHT);
        ta.recycle();

        setDimens(width, height);

        //initialize kore presence background paint
        Paint mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int childWidthSpec = MeasureSpec.makeMeasureSpec((int) width, MeasureSpec.EXACTLY);
        int childHeightSpec = MeasureSpec.makeMeasureSpec((int) height, MeasureSpec.EXACTLY);
        int zeroSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY);
        if (getVisibility() == View.VISIBLE || getVisibility() == View.INVISIBLE) {
            super.onMeasure(childWidthSpec, childHeightSpec);
        } else {
            super.onMeasure(zeroSpec, zeroSpec);
        }
    }

    public void setDefaultBackground(int color, String initials) {
        setDefaultBackground(color, initials, CPV_TEXT_SIZE);
    }

    public void setDefaultBackground(int color, String initials, float textSize) {
        ProfileDrawable profileDrawable = new ProfileDrawable(color, initials, textSize);
        profileDrawable.mutate();
        setBackgroundDrawable(profileDrawable);
    }

    public void setProfileImageUrl(String url, boolean applyRoundTransform) {
        if (applyRoundTransform) {
            if (url.startsWith(StringConstants.HTTP_SCHEME)) {
                Glide.with(getContext())
                        .asBitmap()
                        .load(url)
                        .transform(new CircleCrop())
                        .into(viewTarget);
            } else {
                Glide.with(getContext())
                        .asBitmap()
                        .load(new File(url))
                        .transform(new CircleCrop())
                        .into(viewTarget);
            }

        } else {
            if (url.startsWith(StringConstants.HTTP_SCHEME)) {
                Glide.with(getContext())
                        .asBitmap()
                        .load(url)
                        .into(viewTarget);
            } else {
                Glide.with(getContext())
                        .asBitmap()
                        .load(new File(url))
                        .into(viewTarget);
            }
        }
    }

    public void populateLayout(String nameInitials, String url, Drawable d, int imageRes,
                               int color, boolean b) {
        populateLayout(nameInitials, null, url, d, imageRes, color, b, -1, -1);
    }

    public void populateLayout(String nameInitials, String filePath, String imageUrl, Drawable d,
                               int imageResource, int color, boolean applyRoundTransform,
                               float width, float height) {

        if (color == 0) {
            color = getResources().getColor(R.color.bgBlueSignup);
        }

        this.profileColor = color;

        Glide.with(getContext()).clear(viewTarget);
        if (nameInitials != null) {
            nameInitials = nameInitials.toUpperCase();
        }

        this.initials = nameInitials;

        //Set the imageURL
        setDefaultBackground(color, ""); // draw initials only if there is no image drawable

        if (imageResource != -1) {
            setDefaultBackground(color, "");
            setImageResource(imageResource);
        } else if (d != null) {
            setDefaultBackground(color, "");
            setImageDrawable(d);
        } else if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.equalsIgnoreCase("no_avatar")) {
            setProfileImageUrl(imageUrl, applyRoundTransform);
        } else {
            setImageDrawable(null);
            setDefaultBackground(color, nameInitials); // drawing initials.
        }

        if (width != -1 && height != -1) {
            this.width = width;
            this.height = height;
        }
    }

    public void setDimens(float width, float height) {
        if (this.width != width || this.height != height) {
            this.width = width;
            this.height = height;
            createPath();
        }
    }

    private void createPath() {
        float xCenter = (width - (getPaddingLeft() + getPaddingRight())) / 2f;
        float yCenter = (height - (getPaddingTop() + getPaddingBottom())) / 2f;
        Path circlePath = new Path();
        circlePath.addCircle(xCenter + getPaddingLeft(), yCenter + getPaddingTop(), width / 2f, Path.Direction.CCW);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (hasBorder) {
            drawBorder(canvas);
        }
    }

    private void drawBorder(Canvas canvas) {
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setAntiAlias(true);
        borderPaint.setStrokeWidth(borderStrokeWidth);
        borderPaint.setColor(borderColor);
        int centerX = (getMeasuredWidth() / 2);
        int centerY = (getMeasuredHeight() / 2);
        int radius = (getMeasuredWidth() / 2 - borderStrokeWidth / 2);
        canvas.drawCircle(centerX, centerY, radius, borderPaint);
    }

    void updateWithPic(Bitmap bitmap) {
        if (bitmap != null) {
            setImageBitmap(bitmap);
        }
    }

    @Override
    public void setBorderColor(int borderColor) {
        this.borderColor = borderColor;
    }

    final CustomTarget<Bitmap> viewTarget = new CustomTarget<Bitmap>() {
        @Override
        public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
            setImageDrawable(null);
            updateWithPic(resource);
            setBackgroundResource(0);
        }

        @Override
        public void onLoadCleared(Drawable placeholder) {
            setImageDrawable(null);
            setBackgroundResource(0);
        }

        @Override
        public void onLoadFailed(Drawable errorDrawable) {
            setImageDrawable(null);
            setBackgroundResource(0);
            setDefaultBackground(profileColor, initials);
        }
    };

}
