package kore.botssdk.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;

import com.google.gson.Gson;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import kore.botssdk.listener.ComposeFooterInterface;
import kore.botssdk.listener.InvokeGenericWebViewInterface;
import kore.botssdk.models.BotButtonModel;
import kore.botssdk.models.PayloadInner;
import kore.botssdk.net.SDKConfiguration;
import kore.botssdk.utils.BundleConstants;

@RunWith(ParameterizedRobolectricTestRunner.class)
@Config(sdk = 28)
@SuppressWarnings({"rawtypes", "unchecked"})
public class ButtonTemplateBehaviorTest {
    @ParameterizedRobolectricTestRunner.Parameters(name = "linkTemplate={0}")
    public static Collection<Object[]> templates() {
        return Arrays.asList(new Object[][]{{false}, {true}});
    }

    private final boolean linkTemplate;
    private Context context;
    private ComposeFooterInterface footer;
    private InvokeGenericWebViewInterface webView;
    private PayloadInner payload;

    public ButtonTemplateBehaviorTest(boolean linkTemplate) {
        this.linkTemplate = linkTemplate;
    }

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.setTheme(androidx.appcompat.R.style.Theme_AppCompat);
        footer = mock(ComposeFooterInterface.class);
        webView = mock(InvokeGenericWebViewInterface.class);
        payload = new PayloadInner();
        ArrayList<BotButtonModel> buttons = new ArrayList<>();
        buttons.add(button("First", "first"));
        buttons.add(button("Second", "second"));
        payload.setButtons(buttons);
    }

    @After
    public void resetFlags() {
        SDKConfiguration.OverrideKoreConfig.enableButtonClicksOnAnyPosition = false;
    }

    @Test
    public void defaultPreservesLastItemRestrictionAndPreventsRepeatSubmissions() {
        assertFalse(SDKConfiguration.OverrideKoreConfig.enableButtonClicksOnAnyPosition);
        bind(adapter(payload, false), 0).performClick();
        verifyNoInteractions(footer);

        View first = bind(adapter(payload, true), 0);
        first.performClick();
        first.performClick();
        verify(footer).onSendClick("First", "first", false);
    }

    @Test
    public void positionOverrideAllowsOlderTemplateSubmissionOnlyOnce() {
        SDKConfiguration.OverrideKoreConfig.enableButtonClicksOnAnyPosition = true;
        View first = bind(adapter(payload, false), 0);
        assertTrue(first.isEnabled());
        first.performClick();
        first.performClick();
        verify(footer).onSendClick("First", "first", false);
    }

    @Test
    public void singleClickGuardBlocksSiblingsAndSurvivesAdapterRecreation() {
        SDKConfiguration.OverrideKoreConfig.enableButtonClicksOnAnyPosition = true;
        RecyclerView.Adapter adapter = adapter(payload, false);
        View first = bind(adapter, 0);
        View second = bind(adapter, 1);
        first.performClick();
        first.performClick();
        second.performClick();
        verify(footer).onSendClick("First", "first", false);
        verify(footer, times(0)).onSendClick("Second", "second", false);

        assertFalse(bind(adapter, 0).isEnabled());
        assertFalse(bind(adapter, 1).isEnabled());
        RecyclerView.Adapter rebound = adapter(payload, true);
        View reboundButton = bind(rebound, 1);
        assertFalse(reboundButton.isEnabled());
        assertEquals(0.5f, reboundButton.getAlpha(), 0.0f);
        reboundButton.performClick();
        verify(footer, times(0)).onSendClick("Second", "second", false);
    }

    @Test
    public void singleClickRestrictionDoesNotBypassPositionRestriction() {
        bind(adapter(payload, false), 0).performClick();
        verifyNoInteractions(footer);
        assertFalse(payload.isButtonTemplateSubmitted());
        bind(adapter(payload, true), 0).performClick();
        assertTrue(payload.isButtonTemplateSubmitted());
    }

    @Test
    public void submissionOnlyConsumesItsOwnTemplate() {
        bind(adapter(payload, true), 0).performClick();
        PayloadInner other = new PayloadInner();
        other.setButtons(payload.getButtons());
        View otherButton = bind(adapter(other, true), 1);
        assertTrue(otherButton.isEnabled());
        otherButton.performClick();
        verify(footer).onSendClick("Second", "second", false);
    }

    @Test
    public void urlNavigationDoesNotConsumeTemplateButIsBlockedAfterSubmission() {
        BotButtonModel url = payload.getButtons().get(1);
        url.setType(BundleConstants.BUTTON_TYPE_WEB_URL);
        url.setUrl("https://example.com");
        RecyclerView.Adapter adapter = adapter(payload, true);
        View urlButton = bind(adapter, 1);
        urlButton.performClick();
        assertFalse(payload.isButtonTemplateSubmitted());
        bind(adapter, 0).performClick();
        urlButton.performClick();
        verify(webView).invokeGenericWebView("https://example.com");
        assertFalse(bind(adapter, 1).isEnabled());
    }

    @Test
    public void reentrantClickDuringDispatchCannotSubmitSibling() {
        RecyclerView.Adapter adapter = adapter(payload, true);
        View second = bind(adapter, 1);
        doAnswer(invocation -> {
            second.performClick();
            assertFalse(bind(adapter(payload, true), 1).isEnabled());
            return null;
        }).when(footer).onSendClick("First", "first", false);
        bind(adapter, 0).performClick();
        verify(footer).onSendClick("First", "first", false);
        verify(footer, times(0)).onSendClick("Second", "second", false);
    }

    @Test
    public void synchronousDispatchFailureAllowsRetry() {
        doThrow(new IllegalStateException("Dispatch failed"))
                .when(footer).onSendClick(anyString(), anyString(), anyBoolean());
        try {
            bind(adapter(payload, true), 0).performClick();
        } catch (IllegalStateException expected) {
            // The link adapter propagates callback errors; the regular adapter logs them.
        }
        assertFalse(payload.isButtonTemplateSubmitted());
        assertTrue(bind(adapter(payload, true), 0).isEnabled());
    }

    @Test
    public void missingSendCallbackDoesNotConsumeTemplate() {
        footer = null;
        bind(adapter(payload, true), 0).performClick();
        assertFalse(payload.isButtonTemplateSubmitted());
    }

    @Test
    public void titleFallbackAlsoConsumesTemplate() {
        payload.getButtons().get(0).setPayload(null);
        RecyclerView.Adapter adapter = adapter(payload, true);
        bind(adapter, 0).performClick();
        if (linkTemplate) verify(footer).onSendClick("First", (String) null, false);
        else verify(footer).onSendClick("First", false);
        assertFalse(bind(adapter, 1).isEnabled());
    }

    @Test
    public void interactionStateIsNotPartOfBotJson() {
        payload.setButtonTemplateSubmitted(true);
        Gson gson = new Gson();
        String json = gson.toJson(payload);
        assertFalse(json.contains("buttonTemplateSubmitted"));
        assertFalse(gson.fromJson(json, PayloadInner.class).isButtonTemplateSubmitted());
    }

    private RecyclerView.Adapter adapter(PayloadInner data, boolean lastItem) {
        if (linkTemplate) {
            ButtonLinkTemplateAdapter adapter = new ButtonLinkTemplateAdapter(context, data.getButtons(), lastItem, data);
            adapter.setComposeFooterInterface(footer);
            adapter.setInvokeGenericWebViewInterface(webView);
            return adapter;
        }
        ButtonTemplateAdapter adapter = new ButtonTemplateAdapter(context, data.getButtons(), lastItem, false, false, "", data);
        adapter.setComposeFooterInterface(footer);
        adapter.setInvokeGenericWebViewInterface(webView);
        return adapter;
    }

    private View bind(RecyclerView.Adapter adapter, int position) {
        RecyclerView.ViewHolder holder = adapter.onCreateViewHolder(new FrameLayout(context), 0);
        adapter.onBindViewHolder(holder, position);
        return holder.itemView;
    }

    private static BotButtonModel button(String title, String value) {
        BotButtonModel button = new BotButtonModel();
        button.setType("postback");
        button.setTitle(title);
        button.setPayload(value);
        return button;
    }
}
