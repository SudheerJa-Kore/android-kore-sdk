package kore.botssdk.models;

@SuppressWarnings("UnKnownNullness")
public class BrandingWidgetPanelModel
{
    private BrandingWidgetPanelColorsModel colors;

    public BrandingWidgetPanelColorsModel getColors() {
        return colors;
    }
}

class BrandingWidgetPanelColorsModel {
    private String bg_color;
    private String color;
    private String sel_bg_color;
    private String sel_color;

    public String getColor() {
        return color;
    }

    public String getBg_color() {
        return bg_color;
    }

    public String getSel_bg_color() {
        return sel_bg_color;
    }

    public String getSel_color() {
        return sel_color;
    }
}
