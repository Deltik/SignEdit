package org.deltik.mc.signedit;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.entity.Player;
import org.deltik.mc.signedit.exceptions.*;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.bukkit.Bukkit.getLogger;

public class ChatComms {
    private final Player player;
    private final Configuration config;
    private final ResourceBundle phrases;
    private final MessageFormat messageFormatter;

    @Inject
    public ChatComms(Player player, Configuration config, UserComms userComms) {
        this(player, config, userComms.getClassLoader());
    }

    public ChatComms(Player player, Configuration config) {
        this(player, config, ChatComms.class.getClassLoader());
    }

    public ChatComms(Player player, Configuration config, ClassLoader classLoader) {
        this.player = player;
        this.config = config;

        Locale locale = getSensibleLocale(player, config);
        this.phrases = ResourceBundle.getBundle(
                "Comms",
                locale,
                classLoader,
                new UTF8ResourceBundleControl(config.getLocale())
        );
        this.messageFormatter = new MessageFormat("");
        this.messageFormatter.setLocale(locale);
    }

    private Locale getSensibleLocale(Player player, Configuration config) {
        if (config.getforceLocale()) return config.getLocale();
        try {
            return new Locale.Builder().setLanguageTag(player.getLocale().replace('_', '-')).build();
        } catch (IllformedLocaleException | NullPointerException e) {
            return config.getLocale();
        }
    }

    public void tellPlayer(String message) {
        player.sendMessage(prefix(message));
    }

    public String t(String key, Object... messageArguments) {
        String phrase = phrases.getString(key);

        phrase = replaceFormattingCodes(phrase);

        messageFormatter.applyPattern(phrase);
        return messageFormatter.format(messageArguments);
    }

    private String replaceFormattingCodes(String phrase) {
        Set<String> formattingCodeSet = Stream.of(
                "reset",
                "primary", "primaryDark", "primaryLight",
                "secondary",
                "highlightBefore", "highlightAfter",
                "strong", "italic", "strike", "error"
        ).collect(Collectors.toSet());
        String formattingCodeReplacementPattern =
                "\\{(" + StringUtils.join(formattingCodeSet, "|") + ")}";
        Pattern pattern = Pattern.compile(formattingCodeReplacementPattern);
        Matcher matcher = pattern.matcher(phrase);
        StringBuffer stringBuffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(stringBuffer, phrases.getString(matcher.group(1)));
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
        tellPlayer(t("you_cannot_use", "/" + command + " " + subcommand));
    }

    public void showHelpFor(String cmdString) {
        if (cmdString.equals("sign")) {
            tellPlayer(t("usage_section"));
            showSubcommandSyntax(cmdString, "[set]", "<lines>", "[<text>]");
            showSubcommandSyntax(cmdString, "[clear]", "<lines>");
            showSubcommandSyntax(cmdString, "ui");
            showSubcommandSyntax(cmdString, "cancel");
            showSubcommandSyntax(cmdString, "{copy,cut}", "[<lines>]");
            showSubcommandSyntax(cmdString, "paste");
            showSubcommandSyntax(cmdString, "status");
            showSubcommandSyntax(cmdString, "version");
            tellPlayer(t("online_documentation", t("online_documentation_url")));
        }
    }

    public void showSubcommandSyntax(String command) {
        showSubcommandSyntax(command, "");
    }

    public void showSubcommandSyntax(String command, String subcommand) {
        showSubcommandSyntax(command, subcommand, "");
    }

    public void showSubcommandSyntax(String command, String subcommand, String... parameters) {
        String parametersJoined = String.join(" ", parameters);
        tellPlayer(t("print_subcommand_usage", command, subcommand, parametersJoined));
    }

    public void compareSignText(SignText signText) {
        if (!signText.signChanged()) {
            tellPlayer(t("sign_did_not_change"));
        } else {
            String[] beforeHighlights = new String[4];
            String[] afterHighlights = new String[4];
            for (int i = 0; i < 4; i++) {
                if (!Objects.equals(signText.getBeforeLine(i), signText.getAfterLine(i))) {
                    beforeHighlights[i] = phrases.getString("highlightBefore");
                    afterHighlights[i] = phrases.getString("highlightAfter");
                }
            }
            tellPlayer(t("before_section"));
            dumpLines(signText.getBeforeLines(), beforeHighlights);
            tellPlayer(t("after_section"));
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
                highlight = phrases.getString("secondary");
            }
            if (line == null) {
                line = "";
                highlight = phrases.getString("primaryDark") + phrases.getString("strike");
            }
            tellPlayer(t("print_line", highlight, relativeLineNumber, line));
        }
    }

    public void reportException(Exception e) {
        if (e instanceof ForbiddenSignEditException) {
            tellPlayer(t("forbidden_sign_edit"));
        } else if (e instanceof MissingLineSelectionException) {
            tellPlayer(t("missing_line_selection_exception"));
        } else if (e instanceof NumberParseLineSelectionException) {
            tellPlayer(t("number_parse_line_selection_exception", e.getMessage()));
        } else if (e instanceof OutOfBoundsLineSelectionException) {
            tellPlayer(t(
                    "out_of_bounds_line_selection_exception",
                    config.getMinLine(), config.getMaxLine(), Integer.valueOf(e.getMessage())
                    )
            );
        } else if (e instanceof RangeOrderLineSelectionException) {
            int lower = Integer.parseInt(((RangeOrderLineSelectionException) e).getInvalidLowerBound());
            int upper = Integer.parseInt(((RangeOrderLineSelectionException) e).getInvalidUpperBound());
            tellPlayer(t("range_order_line_selection_exception", lower, upper, e.getMessage()));
        } else if (e instanceof RangeParseLineSelectionException) {
            String badRange = ((RangeParseLineSelectionException) e).getBadRange();
            tellPlayer(t("range_parse_line_selection_exception", badRange, e.getMessage()));
        } else if (e instanceof SignTextHistoryStackBoundsException) {
            tellPlayer(t(e.getMessage()));
        } else if (e instanceof BlockStateNotPlacedException) {
            tellPlayer(t("block_state_not_placed_exception"));
        } else if (e instanceof NullClipboardException) {
            tellPlayer(t("null_clipboard_exception"));
        } else if (e instanceof SignEditorInvocationException) {
            Exception originalException = ((SignEditorInvocationException) e).getOriginalException();
            tellPlayer(t("cannot_open_sign_editor"));
            tellPlayer(t("likely_cause", t("minecraft_server_api_changed")));
            tellPlayer(t("to_server_admin", t("check_for_updates_to_this_plugin")));
            tellPlayer(t("error_code", originalException.toString()));
            tellPlayer(t("hint_more_details_with_server_admin"));
            getLogger().severe(ExceptionUtils.getStackTrace(originalException));
        } else {
            tellPlayer(t("uncaught_error", e.toString()));
            tellPlayer(t("hint_more_details_with_server_admin"));
            getLogger().severe(ExceptionUtils.getStackTrace(e));
        }
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
                    Class var8 = var4.loadClass(var6);
                    if (!ResourceBundle.class.isAssignableFrom(var8)) {
                        throw new ClassCastException(var8.getName() + " cannot be cast to ResourceBundle");
                    }

                    var7 = var8.newInstance();
                } catch (ClassNotFoundException var19) {
                }
            } else {
                if (!var3.equals("java.properties")) {
                    throw new IllegalArgumentException("unknown format: " + var3);
                }

                final String var20 = this.toResourceName0(var6, "properties");
                if (var20 == null) {
                    return (ResourceBundle) var7;
                }

                final ClassLoader var9 = var4;
                final boolean var10 = var5;
                InputStream var11 = null;

                try {
                    var11 = AccessController.doPrivileged((PrivilegedExceptionAction<InputStream>) () -> {
                        InputStream var12 = null;
                        if (var10) {
                            URL var21 = var9.getResource(var20);
                            if (var21 != null) {
                                URLConnection var31 = var21.openConnection();
                                if (var31 != null) {
                                    var31.setUseCaches(false);
                                    var12 = var31.getInputStream();
                                }
                            }
                        } else {
                            var12 = var9.getResourceAsStream(var20);
                        }

                        return var12;
                    });
                } catch (PrivilegedActionException var18) {
                    throw (IOException) var18.getException();
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

        private String toResourceName0(String var1, String var2) {
            return var1.contains("://") ? null : this.toResourceName(var1, var2);
        }
    }
}
