package kore.botssdk.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.datepicker.DayViewDecorator;

/** Applies runtime bot-branding colors to MaterialDatePicker selection circles. */
public class BrandedDayViewDecorator extends DayViewDecorator {
    private final int selectedBackgroundColor;
    private final int selectedTextColor;

    public BrandedDayViewDecorator(int selectedBackgroundColor, int selectedTextColor) {
        this.selectedBackgroundColor = selectedBackgroundColor;
        this.selectedTextColor = selectedTextColor;
    }

    private BrandedDayViewDecorator(Parcel source) {
        selectedBackgroundColor = source.readInt();
        selectedTextColor = source.readInt();
    }

    @Nullable
    @Override
    public ColorStateList getBackgroundColor(
            @NonNull Context context,
            int year,
            int month,
            int day,
            boolean valid,
            boolean selected) {
        return selected ? ColorStateList.valueOf(selectedBackgroundColor) : null;
    }

    @Nullable
    @Override
    public ColorStateList getTextColor(
            @NonNull Context context,
            int year,
            int month,
            int day,
            boolean valid,
            boolean selected) {
        return selected ? ColorStateList.valueOf(selectedTextColor) : null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel destination, int flags) {
        destination.writeInt(selectedBackgroundColor);
        destination.writeInt(selectedTextColor);
    }

    public static final Parcelable.Creator<BrandedDayViewDecorator> CREATOR =
            new Parcelable.Creator<>() {
                @Override
                public BrandedDayViewDecorator createFromParcel(Parcel source) {
                    return new BrandedDayViewDecorator(source);
                }

                @Override
                public BrandedDayViewDecorator[] newArray(int size) {
                    return new BrandedDayViewDecorator[size];
                }
            };
}
