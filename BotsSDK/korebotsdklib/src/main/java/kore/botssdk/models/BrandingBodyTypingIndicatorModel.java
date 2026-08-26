package kore.botssdk.models;

import java.io.Serializable;

import kore.botssdk.utils.StringUtils;

public class BrandingBodyTypingIndicatorModel implements Serializable {
    private String icon;
    private Boolean show;

    public BrandingBodyTypingIndicatorModel updateWith(
            BrandingBodyTypingIndicatorModel configModel
    ) {
        icon = !StringUtils.isNullOrEmpty(configModel.icon)
                ? configModel.icon
                : icon;
        show = configModel.show != null ? configModel.show : show;
        return this;
    }

    public String getIcon() {
        return icon;
    }

    public boolean isShow() {
        return show == null || show;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setShow(boolean show) {
        this.show = show;
    }
}
