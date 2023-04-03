/*
 * Copyright (C) 2017-2023 Deltik <https://www.deltik.net/>
 *
 * This file is part of SignEdit for Bukkit.
 *
 * SignEdit for Bukkit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SignEdit for Bukkit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SignEdit for Bukkit.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.deltik.mc.signedit;

import net.deltik.mc.signedit.exceptions.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.bukkit.Bukkit.getLogger;

public class ChatComms {
    static final Set<String> FORMATTING_CODE_SET = Stream.of(
            "reset",
            "primary", "primaryDark", "primaryLight",
            "secondary",
            "highlightBefore", "highlightAfter",
            "strong", "italic", "strike", "error"
    ).collect(Collectors.toSet());
    private CommandSender commandSender;
    private Configuration config;
    private ResourceBundle phrases;
    private MessageFormat messageFormatter;

    @Inject
    public ChatComms(CommandSender commandSender, Configuration config, UserComms userComms) {
        this(commandSender, config, userComms.getClassLoader());
    }

    public ChatComms(CommandSender commandSender, Configuration config) {
        initialize(commandSender, config);
    }

    public ChatComms(CommandSender commandSender, Configuration config, ClassLoader classLoader) {
        initialize(commandSender, config, classLoader);
    }

    private void initialize(CommandSender commandSender, Configuration config) {
        initialize(commandSender, config, ChatComms.class.getClassLoader());
    }

    private void initialize(CommandSender commandSender, Configuration config, ClassLoader classLoader) {
        this.commandSender = commandSender;
        this.config = config;

        Locale locale = getSensibleLocale(commandSender);
        this.phrases = ResourceBundle.getBundle(
                "Comms",
                locale,
                classLoader,
                new UTF8ResourceBundleControl(config.getLocale())
        );
        this.messageFormatter = new MessageFormat("");
        this.messageFormatter.setLocale(locale);
    }

    private Locale getSensibleLocale(CommandSender commandSender) {
        if (config.getForceLocale()) return config.getLocale();
        try {
            return new Locale
                    .Builder()
                    .setLanguageTag(
                            ((Player) commandSender).getLocale().replace('_', '-')
                    )
                    .build();
        } catch (IllformedLocaleException | NullPointerException | NoSuchMethodError | ClassCastException e) {
            return config.getLocale();
        }
    }

    public void tell(String message) {
        commandSender.sendMessage(prefix(message));
    }

    public String t(String key, Object... messageArguments) {
        String phrase = keyToPhrase(key);

        phrase = replaceFormattingCodes(phrase);

        messageFormatter.applyPattern(phrase);
        return messageFormatter.format(messageArguments);
    }

    private String replaceFormattingCodes(String phrase) {
        String formattingCodeReplacementPattern =
                "\\{(" + StringUtils.join(FORMATTING_CODE_SET, "|") + ")}";
        Pattern pattern = Pattern.compile(formattingCodeReplacementPattern);
        Matcher matcher = pattern.matcher(phrase);
        StringBuffer stringBuffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(stringBuffer, keyToPhrase(matcher.group(1)));
        }
        matcher.appendTail(stringBuffer);
        phrase = stringBuffer.toString();
        return phrase;
    }

    public String t(String key) {
        return t(key, new Object[]{});
    }

    private String prefix(String message) {
        return t("prefix", t("plugin_name"), message);
    }

    public void informForbidden(String command, String subcommand) {
        tell(t("you_cannot_use", "/" + command + " " + subcommand));
    }

    public void compareSignText(SignText signText) {
        boolean textModifiedByOtherPlugin = !SignText.linesMatch(signText.getStagedLines(), signText.getAfterLines());
        if (!signText.signChanged()) {
            if (textModifiedByOtherPlugin) {
                tell(t("forbidden_sign_edit"));
            } else {
                tell(t("sign_did_not_change"));
            }
        } else {
            String[] beforeHighlights = new String[4];
            String[] afterHighlights = new String[4];
            for (int i = 0; i < 4; i++) {
                if (!Objects.equals(signText.getBeforeLine(i), signText.getAfterLine(i))) {
                    beforeHighlights[i] = keyToPhrase("highlightBefore");
                    afterHighlights[i] = keyToPhrase("highlightAfter");
                }
            }

            String beforeSectionSummary = "";
            String afterSectionSummary = "";
            if (textModifiedByOtherPlugin) {
                afterSectionSummary = t("section_decorator", t("modified_by_another_plugin"));
            }

            tell(t("before_section", beforeSectionSummary));
            dumpLines(signText.getBeforeLines(), beforeHighlights);
            tell(t("after_section", afterSectionSummary));
            dumpLines(signText.getAfterLines(), afterHighlights);
        }
    }

    public void dumpLines(String[] lines) {
        dumpLines(lines, new String[4]);
    }

    public void dumpLines(String[] lines, String[] highlights) {
        for (int i = 0; i < 4; i++) {
            int relativeLineNumber = config.getLineStartsAt() + i;
            String line = lines[i];
            String highlight = highlights[i];
            if (highlight == null) {
                highlight = keyToPhrase("secondary");
            }
            if (line == null) {
                line = "";
                highlight = keyToPhrase("primaryDark") + keyToPhrase("strike");
            }
            tell(t("print_line", highlight, relativeLineNumber, line));
        }
    }

    public void reportException(Throwable e) {
        if (e instanceof ForbiddenSignEditException) {
            tell(t("forbidden_sign_edit"));
        } else if (e instanceof MissingLineSelectionException) {
            tell(t("missing_line_selection_exception"));
        } else if (e instanceof NumberParseLineSelectionException) {
            tell(t("number_parse_line_selection_exception", e.getMessage()));
        } else if (e instanceof OutOfBoundsLineSelectionException) {
            tell(t(
                    "out_of_bounds_line_selection_exception",
                    config.getMinLine(), config.getMaxLine(), Integer.valueOf(e.getMessage())
            ));
        } else if (e instanceof RangeOrderLineSelectionException) {
            int lower = Integer.parseInt(((RangeOrderLineSelectionException) e).getInvalidLowerBound());
            int upper = Integer.parseInt(((RangeOrderLineSelectionException) e).getInvalidUpperBound());
            tell(t("range_order_line_selection_exception", lower, upper, e.getMessage()));
        } else if (e instanceof RangeParseLineSelectionException) {
            String badRange = ((RangeParseLineSelectionException) e).getBadRange();
            tell(t("range_parse_line_selection_exception", badRange, e.getMessage()));
        } else if (e instanceof SignTextHistoryStackBoundsException) {
            tell(t(e.getMessage()));
        } else if (e instanceof BlockStateNotPlacedException) {
            tell(t("block_state_not_placed_exception"));
        } else if (e instanceof NullClipboardException) {
            tell(t("null_clipboard_exception"));
        } else if (e instanceof SignEditorInvocationException) {
            Exception originalException = ((SignEditorInvocationException) e).getOriginalException();
            tell(t("cannot_open_sign_editor"));
            tell(t("likely_cause", t("minecraft_server_api_changed")));
            tell(t("to_server_admin", t("check_for_updates_to_this_plugin")));
            tell(t("error_code", originalException.toString()));
            tell(t("hint_more_details_with_server_admin"));
            getLogger().severe(ExceptionUtils.getStackTrace(originalException));
        } else {
            tell(t("uncaught_error", e.toString()));
            tell(t("hint_more_details_with_server_admin"));
            getLogger().severe(ExceptionUtils.getStackTrace(e));
        }
    }

    private String keyToPhrase(String key) {
        String phrase;
        try {
            phrase = phrases.getString(key);
        } catch (MissingResourceException e) {
            initialize(commandSender, config);
            getLogger().warning("Please update your SignEdit locale override! It is missing this key: " + key);
            phrase = phrases.getString(key);
        }
        return phrase;
    }

    static class UTF8ResourceBundleControl extends ResourceBundle.Control {
        private final Locale fallbackLocale;
        private boolean failedFallbackLocale = false;

        public UTF8ResourceBundleControl(Locale fallbackLocale) {
            this.fallbackLocale = fallbackLocale;
        }

        @Override
        public Locale getFallbackLocale(String s, Locale locale) {
            if (locale.equals(fallbackLocale))
                failedFallbackLocale = true;
            if (failedFallbackLocale)
                return super.getFallbackLocale(s, locale);
            return fallbackLocale;
        }

        @Override
        public ResourceBundle newBundle(String var1, Locale var2, String var3, ClassLoader var4, boolean var5)
                throws IllegalAccessException, InstantiationException, IOException {
            String var6 = this.toBundleName(var1, var2);
            Object var7 = null;
            if (var3.equals("java.class")) {
                try {
                    Class<?> var8 = var4.loadClass(var6);
                    if (!ResourceBundle.class.isAssignableFrom(var8)) {
                        throw new ClassCastException(var8.getName() + " cannot be cast to ResourceBundle");
                    }

                    var7 = var8.getDeclaredConstructor().newInstance();
                } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException ignored) {
                }
            } else {
                if (!var3.equals("java.properties")) {
                    throw new IllegalArgumentException("unknown format: " + var3);
                }

                final String var20 = var6.contains("://") ? null : this.toResourceName(var6, "properties");
                if (var20 == null) {
                    return null;
                }

                InputStream var11 = null;

                if (var5) {
                    URL var21 = var4.getResource(var20);
                    if (var21 != null) {
                        URLConnection var31 = var21.openConnection();
                        if (var31 != null) {
                            var31.setUseCaches(false);
                            var11 = var31.getInputStream();
                        }
                    }
                } else {
                    var11 = var4.getResourceAsStream(var20);
                }

                if (var11 != null) {
                    try {
                        var7 = new PropertyResourceBundle(new InputStreamReader(var11, StandardCharsets.UTF_8));
                    } finally {
                        var11.close();
                    }
                }
            }

            return (ResourceBundle) var7;
        }
    }
}
