package kore.botssdk.fragment.header;

import android.content.res.Configuration;

import androidx.fragment.app.Fragment;

import java.util.Locale;

import kore.botssdk.R;
import kore.botssdk.listener.ComposeFooterInterface;
import kore.botssdk.listener.InvokeGenericWebViewInterface;
import kore.botssdk.models.BrandingHeaderModel;
import kore.botssdk.net.SDKConfiguration;

public abstract class BaseHeaderFragment extends Fragment {
    protected ComposeFooterInterface composeFooterInterface;
    protected InvokeGenericWebViewInterface invokeGenericWebViewInterface;
    public void setComposeFooterInterface(ComposeFooterInterface composeFooterInterface){
        this.composeFooterInterface =composeFooterInterface;
    }

    public void setInvokeGenericWebViewInterface(InvokeGenericWebViewInterface invokeGenericWebViewInterface){
        this.invokeGenericWebViewInterface = invokeGenericWebViewInterface;
    }

    protected String getOnlineStatusText() {
        Locale preferredLocale = SDKConfiguration.getDeviceLocale();
        if (preferredLocale == null) {
            return getString(R.string.online);
        }

        Configuration localizedConfiguration =
                new Configuration(getResources().getConfiguration());
        localizedConfiguration.setLocale(preferredLocale);
        return requireContext()
                .createConfigurationContext(localizedConfiguration)
                .getString(R.string.online);
    }

    public abstract void setBrandingDetails(BrandingHeaderModel brandingModel);
}
