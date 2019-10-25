package org.deltik.mc.signedit;

import org.junit.Assert;
import org.junit.Test;

import java.util.Locale;
import java.util.ResourceBundle;

public class ChatCommsTest {
    @Test
    public void resourceBundleControlFallbackLocale() {
        Locale fallbackLocale = new Locale.Builder().setLanguageTag("el-GR").build();
        Locale badLocale = new Locale.Builder().setLanguageTag("hu-HU").build();
        ResourceBundle.Control control = new ChatComms.UTF8ResourceBundleControl(fallbackLocale);

        Assert.assertEquals(
                fallbackLocale,
                control.getFallbackLocale("Comms", badLocale)
        );
    }

    @Test
    public void resourceBundleControlFallbackLocaleNoInfiniteLoop() {
        Locale fallbackLocale = new Locale.Builder().setLanguageTag("en-US").build();
        Locale badLocale = new Locale.Builder().setLanguageTag("en-US").build();
        ResourceBundle.Control control = new ChatComms.UTF8ResourceBundleControl(fallbackLocale);

        Assert.assertNotEquals(
                fallbackLocale,
                control.getFallbackLocale("Comms", badLocale)
        );
    }
}
