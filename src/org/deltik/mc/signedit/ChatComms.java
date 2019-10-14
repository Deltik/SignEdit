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
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bukkit.Bukkit.getLogger;

public class ChatComms {
    private final Player player;
    private final Configuration config;
    private final ResourceBundle phrases;
    private final MessageFormat messageFormatter;
    private final NumberFormat numberFormatter;

    @Inject
    public ChatComms(Player player, Configuration config) {
        this.player = player;
        this.config = config;

        Locale locale = getSensibleLocale(player, config);
        this.phrases = ResourceBundle.getBundle("Comms", locale, new UTF8ResourceBundleControl());
        this.messageFormatter = new MessageFormat("");
        this.messageFormatter.setLocale(locale);

        this.numberFormatter = NumberFormat.getInstance(locale);
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
        Map<String, String> formattingCodeReplacements = new HashMap<String, String>() {{
            put("reset", reset());
            put("primary", primary());
            put("primaryLight", primaryLight());
            put("primaryDark", primaryDark());
            put("secondary", secondary());
            put("highlightBefore", highlightBefore());
            put("highlightAfter", highlightAfter());
            put("strong", strong());
            put("italic", italic());
            put("strike", strike());
            put("error", error());
        }};
        String formattingCodeReplacementPattern =
                "\\{(" + StringUtils.join(formattingCodeReplacements.keySet(), "|") + ")\\}";
        Pattern pattern = Pattern.compile(formattingCodeReplacementPattern);
        Matcher matcher = pattern.matcher(phrase);
        StringBuffer stringBuffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(stringBuffer, formattingCodeReplacements.get(matcher.group(1)));
        }
        matcher.appendTail(stringBuffer);
        phrase = stringBuffer.toString();
        return phrase;
    }

    public String t(String key) {
        return t(key, new Object[]{});
    }

    public String nf(Number number) {
        return numberFormatter.format(number);
    }

    public String nf(String integer) {
        return numberFormatter.format(Integer.parseInt(integer));
    }

    private String prefix(String message) {
        return t("prefix", t("plugin_name"), message);
    }

    public String reset() {
        return "§r";
    }

    public String primary() {
        return "§6";
    }

    public String primaryLight() {
        return "§e";
    }

    public String primaryDark() {
        return "§7";
    }

    public String secondary() {
        return "§f";
    }

    public String highlightBefore() {
        return "§4";
    }

    public String highlightAfter() {
        return "§2";
    }

    public String strong() {
        return "§l";
    }

    public String italic() {
        return "§o";
    }

    public String strike() {
        return "§m";
    }

    public String error() {
        return "§c";
    }

    public void informForbidden(String command, String subcommand) {
        String formattedCommandAndSubcommand = primaryLight() + "/" + command + " " + subcommand + error();
        tellPlayer(error() + t("you_cannot_use", formattedCommandAndSubcommand));
    }

    public void showHelpFor(String cmdString) {
        if (cmdString.equals("sign")) {
            tellPlayer(secondary() + strong() + t("usage_section"));
            showSubcommandSyntax(cmdString, "[set]", "<lines> [<text>]");
            showSubcommandSyntax(cmdString, "[clear]", "<lines>");
            showSubcommandSyntax(cmdString, "ui");
            showSubcommandSyntax(cmdString, "cancel");
            showSubcommandSyntax(cmdString, "{copy,cut}", "[<lines>]");
            showSubcommandSyntax(cmdString, "paste");
            showSubcommandSyntax(cmdString, "status");
            showSubcommandSyntax(cmdString, "version");
            String onlineDocsLink = reset() + "https://git.io/SignEdit-README";
            tellPlayer(secondary() + strong() + t("online_documentation", onlineDocsLink));
        }
    }

    public void showSubcommandSyntax(String command) {
        showSubcommandSyntax(command, "");
    }

    public void showSubcommandSyntax(String command, String subcommand) {
        showSubcommandSyntax(command, subcommand, "");
    }

    public void showSubcommandSyntax(String command, String subcommand, String parameters) {
        String output = primary() + "/" + command;
        if (!subcommand.isEmpty()) {
            output += reset() + " " +
                    primaryLight() + subcommand;
        }
        if (!parameters.isEmpty()) {
            output += reset() + " " +
                    primaryDark() + parameters;
        }
        tellPlayer(output);
    }

    public void compareSignText(SignText signText) {
        if (!signText.signChanged()) {
            tellPlayer(primary() + t("sign_did_not_change"));
        } else {
            String[] beforeHighlights = new String[4];
            String[] afterHighlights = new String[4];
            for (int i = 0; i < 4; i++) {
                if (!Objects.equals(signText.getBeforeLine(i), signText.getAfterLine(i))) {
                    beforeHighlights[i] = highlightBefore();
                    afterHighlights[i] = highlightAfter();
                }
            }
            tellPlayer(primary() + strong() + t("before_section"));
            dumpLines(signText.getBeforeLines(), beforeHighlights);
            tellPlayer(primary() + strong() + t("after_section"));
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
                highlight = secondary();
            }
            if (line == null) {
                line = "";
                highlight = primaryDark() + strike();
            }
            tellPlayer(t("line_print", highlight, nf(relativeLineNumber), line));
        }
    }

    public void reportException(Exception e) {
        if (e instanceof ForbiddenSignEditException) {
            tellPlayer(t("forbidden_sign_edit"));
        } else if (e instanceof MissingLineSelectionException) {
            tellPlayer(t("missing_line_selection_exception"));
        } else if (e instanceof NumberParseLineSelectionException) {
            tellPlayer(t("number_parse_line_selection_exception", nf(e.getMessage())));
        } else if (e instanceof OutOfBoundsLineSelectionException) {
            tellPlayer(t(
                    "out_of_bounds_line_selection_exception",
                    nf(config.getMinLine()), nf(config.getMaxLine()), nf(e.getMessage())
                    )
            );
        } else if (e instanceof RangeOrderLineSelectionException) {
            String lower = ((RangeOrderLineSelectionException) e).getInvalidLowerBound();
            String upper = ((RangeOrderLineSelectionException) e).getInvalidUpperBound();
            tellPlayer(t("range_order_line_selection_exception", nf(lower), nf(upper), e.getMessage()));
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

    class UTF8ResourceBundleControl extends ResourceBundle.Control {
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
