# SignEdit for Bukkit

[![GitHub releases](https://img.shields.io/github/v/release/Deltik/SignEdit)](https://github.com/Deltik/SignEdit/releases)
[![Total downloads](https://img.shields.io/github/downloads/Deltik/SignEdit/total)](https://github.com/Deltik/SignEdit/releases)
[![Latest version downloads](https://img.shields.io/github/downloads/Deltik/SignEdit/latest/total)](https://github.com/Deltik/SignEdit/releases)
[![Spigot rating](https://img.shields.io/spiget/rating/47604)](https://www.spigotmc.org/resources/signedit-for-bukkit.47604/rate)
[![CircleCI](https://img.shields.io/circleci/project/github/Deltik/SignEdit)](https://circleci.com/gh/Deltik/SignEdit)

**SignEdit for Bukkit** is a [Bukkit plugin](https://www.spigotmc.org/resources/categories/bukkit.4/) that allows players to edit sign objects by looking at them or clicking on them and typing in a SignEdit command.

![Screenshot of usage help in SignEdit for Bukkit v1.13.0](https://i.imgur.com/CoSsFoe.png)

## Table of Contents

   * [SignEdit for Bukkit](#signedit-for-bukkit)
      * [Table of Contents](#table-of-contents)
      * [Installation](#installation)
      * [Usage](#usage)
         * [Commands](#commands)
            * [Commands from Older Versions](#commands-from-older-versions)
         * [Aliases](#aliases)
      * [Syntax](#syntax)
         * [Formatting Codes](#formatting-codes)
            * [Examples](#examples)
         * [Language Tags](#language-tags)
            * [Examples](#examples-1)
         * [Selecting Multiple Lines](#selecting-multiple-lines)
            * [Examples](#examples-2)
         * [Versioning](#versioning)
            * [Examples](#examples-3)
      * [Permissions](#permissions)
      * [Configuration](#configuration)
         * [`clicking: [auto|false|true]`](#clicking-autofalsetrue)
         * [`force-locale: [false|true]`](#force-locale-falsetrue)
         * [`line-starts-at: [1|0]`](#line-starts-at-10)
         * [`locale: [en|…]`](#locale-en)
         * [`compatibility.sign-ui: [Auto|EditableBook|Native]`](#compatibilitysign-ui-autoeditablebooknative)
         * [`compatibility.edit-validation: [Standard|Extra|None]`](#compatibilityedit-validation-standardextranone)
      * [Features](#features)
         * [Features from Older Versions](#features-from-older-versions)
         * [Supported Locales](#supported-locales)
         * [Visual Examples](#visual-examples)
            * [`/sign ui`](#sign-ui)
            * [`/sign 2 Deltik's`](#sign-2-deltiks)
            * [`/sign clear 1`](#sign-clear-1)
            * [`/sign set 1,4 ===============`](#sign-set-14-)
            * [`/sign set 1,4 &1#&2#&3#&4#&5#&6#&7#&8#&9#&a#&b#&c#&d#&e#&f#`](#sign-set-14-123456789abcdef)
            * [`/se set 3 &4CONSTRUCTION`](#se-set-3-4construction)
            * [`/se set 1-4 Arts\&Crafts`](#se-set-1-4-artscrafts)
            * [`/sign copy`](#sign-copy)
            * [`/sign <tab>`](#sign-tab)
      * [Advanced Customization](#advanced-customization)
         * [Theming and Colors](#theming-and-colors)
         * [Custom Translations](#custom-translations)
      * [Compatibility](#compatibility)
         * [Version Compatibility Table](#version-compatibility-table)
         * [Backwards Compatibility with Omel's SignEdit v1.3](#backwards-compatibility-with-omels-signedit-v13)
         * [Compatibility with Permissions Plugins](#compatibility-with-permissions-plugins)
         * [Minecraft 1.16.1 Sign Editor GUI](#minecraft-1161-sign-editor-gui)

## Installation

1. Download the latest `.jar` file from [the Releases page](https://github.com/Deltik/SignEdit/releases) and upload it to your CraftBukkit (Spigot, PaperMC, etc.) `plugins/` folder.
2. Restart your CraftBukkit server.

## Usage

### Commands

| Command | Usage | [Version](#versioning) |
| --- | --- | --- |
| `/? sign` | Show detailed help about the `/sign` command. | `>= 1.0` |
| `/sign` | Show the first page of the usage syntax of the `/sign` subcommands. | `>= 1.0` |
| `/sign help [<page>]` | Show the usage syntax of the `/sign` subcommands.  Specify a `<page>` number to view a specific page. | `>= 1.13` |
| `/sign ui` | Open the native Minecraft sign editor on the targeted sign. | `>= 1.8` |
| `/sign [set] <lines> [<text>]` | Change each of the [lines](#selecting-multiple-lines) `<lines>` of the targeted sign to `<text>`.  If `<text>` is blank, erase the lines `<lines>`.  `set` can be omitted. | `>= 1.10` |
| `/sign clear [<lines>]` | Erase the text on the targeted sign.  If `<lines>` is specified, only those lines are blanked out. | `>= 1.13` |
| `/sign cancel` | Abort your pending sign edit action. | `>= 1.9` |
| `/sign status` | Show the pending action, what is in the copy buffer, and an overview of the undo/redo history stack. | `>= 1.10` |
| `/sign copy [<lines>]` | Copy the targeted sign's text.  If `<lines>` is specified, only those lines are copied. | `>= 1.10` |
| `/sign cut [<lines>]` | Copy the targeted sign's text and remove it from the sign.  If `<lines>` is specified, only those lines are cut. | `>= 1.10` |
| `/sign paste` | Paste the lines buffered by the previous `/sign copy` or `/sign cut` command onto the targeted sign. | `>= 1.10` |
| `/sign undo` | Revert the previous sign change. | `>= 1.10` |
| `/sign redo` | Restore the most recent sign change that was undone by `/sign undo`. | `>= 1.10` |
| `/sign version` | Show the installed version of this plugin. | `>= 1.9.3` |

#### Commands from Older Versions

These commands no longer apply to the latest version of this plugin:

| Command | Usage | [Version](#versioning) |
| --- | --- | --- |
| `/sign [set] <line> [<text>]` | Change the line `<line>` of the targeted sign to `<text>`.  All `&` characters are replaced with `§` for formatting codes. If `<text>` is blank, erase the line `<line>`. `set` can be omitted. | `>= 1.6, < 1.10` |
| `/sign set <line> [<text>]` | Change the line `<line>` of the targeted sign to `<text>`.  All `&` characters are replaced with `§` for formatting codes. If `<text>` is blank, erase the line `<line>`. | `>= 1.4, < 1.6` |
| `/sign set <line> <text>` | Change the line `<line>` of the targeted sign to `<text>`.  All `&` characters are replaced with `§` for formatting codes. | `>= 1.0, < 1.4` |
| `/sign clear <lines>` | Erase the lines `<lines>` of the targeted sign. | `>= 1.10, < 1.13` |
| `/sign clear <line>` | Erase the line `<line>` of the targeted sign. | `>= 1.4, < 1.10` |

### Aliases

| Alias | Command | [Version](#versioning) |
| --- | --- | --- |
| `/signedit` | `/sign` | `>= 1.4` |
| `/editsign` | `/sign` | `>= 1.4` |
| `/se` | `/sign` | `>= 1.4` |
| `/sign <line> [<text>]` | `/sign set <line> [<text>]` | `>= 1.6, < 1.10` |
| `/sign <lines> [<text>]` | `/sign set <lines> [<text>]` | `>= 1.10` |
| `/sign <line>` | `/sign clear <line>` | `>= 1.6, < 1.10` |
| `/sign <lines>` | `/sign clear <lines>` | `>= 1.10` |

## Syntax

### Formatting Codes

(`>= 1.10`)

Only ampersands (`&`) that precede a [Minecraft formatting code](https://minecraft.gamepedia.com/Formatting_codes) character turn into section signs (`§`).
If you want to type a literal ampersand, escape it with a backslash like so: `\&a`

(`>= 1.12`)

In Minecraft 1.16+, the client supports text in sRGB (standard Red Green Blue – 24-bit color depth, true color, or 16 million colors) by prepending colored text with `§x§A§B§C§D§E§F`, where `A`, `B`, `C`, `D`, `E`, and `F` are the [hexadecimal representation of the color](https://en.wikipedia.org/wiki/Web_colors).

This plugin supports three ways to resolve `§x§A§B§C§D§E§F`-style true color from user input:

* `&#ABCDEF` – Six-digit form, similar to the representation in HTML/CSS/SVG
* `&#ACE` – Abbreviated three-digit form, which is equivalent to the six-digit form `&#AACCEE`
* `&x&A&B&C&D&E&F` – Expanded form, resembling how true color formatting is stored in Minecraft

Regardless of which form the user inputs, when this plugin converts the raw Minecraft true color formatting to become user-editable, the six-digit form (`&#ABCDEF`) is returned.

All of these forms can also be escaped by prepending the ampersands with a backslash like so: `\&#ABCDEF`, `\&#ACE`, and `\&x\&A\&B\&C\&D\&E\&F`

The hex digits are case-insensitive, but the Minecraft server may internally convert them to uppercase.

(`>= 1.0, < 1.10`)

All ampersands (`&`) are replaced with section signs (`§`) for [Minecraft formatting codes](https://minecraft.gamepedia.com/Formatting_codes).

It is not possible to type a literal ampersand in versions `>= 1.0, < 1.10`.

#### Examples

(`>= 1.10`)

| Input | Output |
| --- | --- |
| `&bHELLO` | `§bHELLO` |
| `a&b` | `a§b` |
| `a\&b` | `a&b` |
| `x&y` | `x&y` |
| `x\&y` | `x\&y` |
| `\&d &e &f &g` | `&d §e §f &g` |
| `Arts & Crafts` | `Arts & Crafts` |
| `Arts&Crafts` | `Arts§Crafts` |
| `Arts\&Crafts` | `Arts&Crafts` |

(`>= 1.12`)

| Input | Output |
| --- | --- |
| `&#abcdef` | `§x§A§B§C§D§E§F` |
| `&#AbCdEf` | `§x§A§B§C§D§E§F` |
| `\&#abcdef` | `&#abcdef` |
| `\&#AbCdEf` | `&#AbCdEf` |
| `&#08F` | `§x§0§0§8§8§F§F` |
| `\&#08f` | `&#08f` |
| `&x&0&1&2&3&4&5` | `§x§0§1§2§3§4§5` |
| `\&x\&0\&1\&2\&3\&4\&5` | `&x&0&1&2&3&4&5` |

### Language Tags

(`>= 1.10.2`)

Locales are identified by their [IETF BCP 47](https://tools.ietf.org/html/bcp47) language tag.
For the practical scope of this plugin, only the two-letter language code plus an optional two-letter region code joined by a hyphen (`-`) are used.

Go to [Supported Locales](#supported-locales) for a list of languages supported by this plugin.

#### Examples

| Tag | Locale |
| --- | --- |
| de | German |
| de-AT | German (Austria)  |
| en | English |
| en-GB | English (United Kingdom) |
| zh | Chinese |
| zh-CN | Chinese (China) |

### Selecting Multiple Lines

(`>= 1.10`)

For commands that take a *lines* argument, the specified lines can be any combination of a comma-delimited list (e.g. `1,3` selects lines 1 and 3) and a hyphenated range (e.g. `1-3` selects lines 1, 2, and 3).

#### Examples

* `1` selects line 1.
* `1-1` selects line 1.
* `1,3` selects lines 1 and 3.
* `1-3` selects lines 1, 2, and 3.
* `1-2,4` selects lines 1, 2, and 4.
* `1,2,4` selects lines 1, 2, and 4.
* `2-3,1,4` selects lines 1, 2, 3, and 4.
* `2,2,2,3,3` selects lines 2 and 3.

### Versioning

This plugin uses [semantic versioning](https://semver.org/).  From a version number `MAJOR.MINOR.PATCH`:

* `MAJOR` increases when behavior changes that breaks how previous versions worked,
* `MINOR` increases when new features are added while being compatible with how previous versions worked, and
* `PATCH` increases when bugs are fixed without breaking how previous versions worked.

In the documentation, version constraints are used to indicate to which versions the piece of documentation applies:

* `>=` means starting from this version,
* `>` means after this version,
* `<=` means up to and including this version,
* `<` means up to but not including this version,
* `=` means this version only, and
* [`~>`](https://thoughtbot.com/blog/rubys-pessimistic-operator) means this version and any newer versions after the last point (`.`).

#### Examples

* `>= 1.5` matches version `1.5.0` and higher.
* `> 1.4` matches version `1.4.1` and higher.
* `<= 1.3.1` matches version `1.3.1`, `1.3.0`, `1.3`, and lower.
* `< 1.4` matches any version lower than `1.4`.
* `= 1.0` matches version `1.0` and `1.0.0`.
* `~> 1.4` is the same as `>= 1.4, < 2`.
* `~> 1.3.0` is the same as `>= 1.3.0, < 1.4`.
* `~> 2` is the same as `>= 2.0.0`.
* `= 1.0, = 1.1, = 1.2, = 1.3` matches only versions `1.0`, `1.1`, `1.2`, `1.3`, and their `.0` `PATCH` versions.

## Permissions

All features of this plugin will be available if the player has the following permission:

    signedit.use

(`>= 1.8`) More refined permissions are available in this format:

    signedit.COMMAND.SUBCOMMAND

Examples:

    signedit.sign.cancel
    signedit.sign.clear
    signedit.sign.copy
    signedit.sign.cut
    signedit.sign.paste
    signedit.sign.redo
    signedit.sign.set
    signedit.sign.status
    signedit.sign.ui
    signedit.sign.undo
    signedit.sign.version

## Configuration

All configuration is in the file `plugins/SignEdit/config.yml`.

(`>= 1.1`) The configuration file is created with default values when the plugin is loaded and the file does not already exist.

(`>= 1.5`) The configuration file is sanitized when the plugin is loaded and unloaded and rewritten when the plugin is unloaded.

(`>= 1.10`) During plugin unloading, the configuration file is reloaded before being rewritten.

(`>= 1.13`) Changes to the configuration file while the plugin is active in a running server are detected and applied at runtime.
The server does not need to be restarted for configuration changes to take effect.

### `clicking: [auto|false|true]`

(`>= 1.1`)

(`>= 1.7`) **auto** (default): Behave like `clicking: true` when you are not looking at a sign and behave like `clicking: false` when you are looking at a sign.

**false** (default `< 1.7`): Edit signs by looking at them and then typing a `/sign` command, which will then instantly edit the sign you are looking at.

**true**: Edit signs by typing a `/sign` command and then right-mouse clicking a sign.

### `force-locale: [false|true]`

(`>= 1.10.2`)

**false** (default): Does not change the behavior of the `locale` configuration option.

**true**: Force every player to use the locale specified in the `locale` configuration option, regardless of the players' actual locales.

### `line-starts-at: [1|0]`

(`>= 1.5`)

**1** (default): Line number 1 corresponds to the top-most line of sign blocks.

**0**: Line number 0 corresponds to the top-most line of sign blocks.

### `locale: [en|…]`

(`>= 1.10.2`)

The value of `locale` is an IETF BCP 47 [language tag](#language-tags).

(`> 1.10.2`) It is the locale used to display text to the player when the player's locale cannot be determined.
If the value is not supported, English will be used as the fallback locale.

(`= 1.10.2`) This option has no effect if [`force-locale`](#force-locale-falsetrue) is false (due to an implementation bug).

**en** (default): Set the default/fallback locale to `en` (English).

**…**: Set the default/fallback locale to `…`.  Go to [Supported Locales](#supported-locales) for a table of supported languages.

### `compatibility.sign-ui: [Auto|EditableBook|Native]`

(`>= 1.12.2`)

Choose which visual sign editor to show for all players that run `/sign ui`.

**Auto** (default): Shows the native Minecraft sign editor GUI, except for on a Minecraft 1.16.1 server, which shows the book and quill alternative sign editor GUI instead.

**EditableBook**: Shows the book and quill alternative sign editor GUI regardless of Minecraft server version

**Native**: Shows the native Minecraft sign editor GUI regardless of Minecraft server version

### `compatibility.edit-validation: [Standard|Extra|None]`

(`>= 1.12.2`)

Decide what events to send to other plugins for sign edit permission validation when a player edits a sign with this plugin.

**Standard** (default): Only emit [`SignChangeEvent`](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/block/SignChangeEvent.html).  This option is the most compliant with the Bukkit API.

**Extra**: Emit [`BlockBreakEvent`](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/block/BlockBreakEvent.html), [`BlockPlaceEvent`](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/block/BlockPlaceEvent.html), and [`SignChangeEvent`](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/block/SignChangeEvent.html) (in that order).  The edited sign block will not actually be broken and replaced.  This option improves compatibility with permissions plugins that don't handle `SignChangeEvent` correctly, but it may conflict with plugins that expect block breaking or block placement behavior.

**None** (not recommended): Bypass all permission validation by not sending any events.  All players with access to `/sign` modification commands will be able to edit all signs on the server.  This option matches the behavior of this plugin version `< 1.8`.

## Features

* (`>= 1.8`) Edit the targeted sign with [`/sign ui`](#sign-ui) in the native Minecraft sign editor ([except for Minecraft 1.16.1](#minecraft-1161-sign-editor-gui)).
  * No dependencies!
* (`>= 1.10`) Change all the lines `<lines>` of the targeted sign to be `<text>` with [`/sign set <lines> [<text>]`](#sign-set-14-) or [`/sign <lines> [<text>]`](#sign-2-deltiks).
* (`>= 1.10`) [See the sign text before and after in chat.](#se-set-3-4construction)
* Targeting a sign works as follows:
  * In `clicking: false` mode or in version `= 1.0`, the sign you are looking at is edited.
  * In `clicking: true` mode, after running the `/sign` command, right-click a sign to edit it.
  * (`>= 1.7`) In `clicking: auto` mode, the behavior is the same as `clicking: false` if you are looking at a sign and `clicking: true` if you are not looking at a sign.
* All editing functions support [formatting codes](#formatting-codes) ([`&` turns into `§`](#se-set-3-4construction))
  * (`>= 1.10`) Escape formatting codes with a backslash (e.g. [`\&C` turns into literal `&C`](#se-set-1-4-artscrafts))
  * (`>= 1.12`) Since Minecraft 1.16: RGB text color (e.g. `&#800000` makes the text maroon)
* (`>= 1.10`) [Tab completion for `/sign` subcommands](#sign-tab)
* (`>= 1.10`) Copy, cut, and paste sign lines with `/sign copy`, `/sign cut`, and `/sign paste`, respectively.
* (`>= 1.10`) Undo and redo sign changes with `/sign undo` and `/sign redo`, respectively.
* (`>= 1.13`) Other plugins can [validate](#compatibilityedit-validation-standardextranone) any attempted sign edits as if the player was filling out a new sign.  Permissions plugins can block the edit, and censorship plugins can modify the inputted text before it is saved.
* (`>= 1.10.2`) Automatically uses the player's language, [if supported](#supported-locales).
* (`>= 1.11`) Fully customizable plugin text [theming and localization/translations](#advanced-customization)

### Features from Older Versions
These features no longer apply to the latest version of this plugin:

* (`< 1.10`) Edit the line `<line>` of the targeted sign to be `<text>` with `/sign set <line> [<text>]` or (`>= 1.6`) `/sign <line> [<text>]`.
* (`>= 1.8, < 1.10`) Before editing a sign, this plugin checks if the player is allowed to edit the sign by pretending to blank out the sign and seeing if the corresponding [`SignChangeEvent`](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/block/SignChangeEvent.html) is cancelled.
* (`>= 1.10, < 1.12.2`) Players cannot edit signs that they do not have permission to edit.  Every attempted edit is validated through a [`SignChangeEvent`](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/block/SignChangeEvent.html) and will not succeed if another plugin or policy cancels the `SignChangeEvent`.
* (`~> 1.12.2`) Players cannot edit signs that they do not have permission to edit.  Every attempted edit is validated through an [admin-configurable chain of events](#compatibilityedit-validation-standardextranone) and will not succeed if another plugin or policy cancels any of the events.

### Supported Locales

| Language Tag | Language | Proficiency | Maintainer(s) | [Version](#versioning) |
| --- | --- | --- | --- | --- |
| `en` | English | Native | [Deltik](https://git.io/Deltik) | `>= 1.4` |
| `en` | English | Intermediate | [Omel](https://www.spigotmc.org/members/omel.85850/) | `>= 1.0, < 1.4` |
| `de` | German | Native | [bleeding182](https://github.com/bleeding182) | `>= 1.10.2` |
| `nl` | Dutch | Native | [SBDeveloper](https://github.com/stijnb1234), \_\_Dutch\_\_ | `>= 1.11.1` |
| `zh` | Simplified Chinese | Intermediate | [eason329](https://github.com/eason329) | `>= 1.11.4` |
| `zh` | Simplified Chinese | Machine Translation | [Deltik](https://git.io/Deltik) | `>= 1.10.2, < 1.11.4` |
| `zh-CN` | Simplified Chinese | Intermediate | [eason329](https://github.com/eason329) | `>= 1.11.4` |
| `zh-HK` | Hong Kong Cantonese | Native | [eason329](https://github.com/eason329) | `not released` |
| `zh-TW` | Traditional Chinese | Intermediate | [eason329](https://github.com/eason329) | `>= 1.11.4` |

### Visual Examples

#### `/sign ui`

![GIF of `/sign ui` in action](https://i.imgur.com/IaZ1Pry.gif)

#### `/sign 2 Deltik's`

![GIF of `/sign <line> <text>` in action](https://i.imgur.com/zrPKLaY.gif)

#### `/sign clear 1`

![GIF of `/sign clear <line>` in action](https://i.imgur.com/U5b1z1Z.gif)

#### `/sign set 1,4 ===============`

![GIF of `/sign set <lines> <text>` in action](https://i.imgur.com/5PrG83O.gif)

#### `/sign set 1,4 &1#&2#&3#&4#&5#&6#&7#&8#&9#&a#&b#&c#&d#&e#&f#`

![GIF of `/sign set <lines> <text>` in action](https://i.imgur.com/STpR13s.gif)

#### `/se set 3 &4CONSTRUCTION`

![Screenshot of color code in `/sign <line> <text>`](https://i.imgur.com/De8137B.png)

#### `/se set 1-4 Arts\&Crafts`

![Screenshot of formatting code escaping in `/sign <lines> <text>`](https://i.imgur.com/ovZ2ZmI.png)

#### `/sign copy`

![Screenshot of `/sign copy`](https://i.imgur.com/F5LCdsP.png)

#### `/sign <tab>`

![Screenshot of tab completion under `/sign`](https://i.imgur.com/l0QWG0R.png)

## Advanced Customization

(`>= 1.11`)

You can customize the text displayed to the player to say whatever you want and look however you like!

After starting up the plugin, a read-only copy of the theme and translation files will be copied to the plugin's data directory at `plugins/SignEdit/locales/originals/`.

Your customizations are loaded from `plugins/SignEdit/locales/overrides/`, which as the folder name suggests, overrides the built-in locale files.

The locale files are standard [Java resource bundles](https://docs.oracle.com/javase/8/docs/api/java/util/ResourceBundle.html).

**Important caveat:** Your string (text) customizations will not update automatically in newer versions of this plugin.
Please see the [release notes](CHANGELOG.md) under:
* the "Added" section for new strings and
* the "Changed" section for modified strings.

If the plugin detects that you are missing a translation key during a message, a warning will be sent to the console:

> Please update your SignEdit locale override! It is missing this key: …

and the plugin default translation will be used for the message.

### Theming and Colors

The color scheme can be changed by copying the `plugins/SignEdit/locales/originals/Comms.properties` file into the `plugins/SignEdit/locales/overrides/` folder (keeping the same file name) and then editing the copy's "Theme" section.

These are the possible theme properties and their defaults:

```ini
# Theme
reset=§r           # Reset formatting after this to the client default. Not recommended to change
primary=§6         # The primary color of the theme
primaryDark=§7     # The dark variant of the primary color
primaryLight=§e    # The light variant of the primary color
secondary=§f       # The secondary color of the theme
highlightBefore=§4 # The color to highlight the "before" line of a sign change
highlightAfter=§2  # The color to highlight the "after" line of a sign change
strong=§l          # Bold
italic=§o          # Emphasis
strike=§m          # Strikethrough
error=§c           # The error text color
```

These properties can then be used on any other string by inserting them as `{PROPERTY}`, where `PROPERTY` is the name of the property.
Example:

```ini
you_cannot_use={error}You are not allowed to use {primaryLight}{0}{error}.
```

### Custom Translations

You can add support for a custom language by making a new file `plugins/SignEdit/locales/overrides/Comms_LANGUAGE.properties`, where `LANGUAGE` is a [language tag](#language-tags).

In that new file, add keys from `plugins/SignEdit/locales/originals/Comms.properties` to translate those strings.
Any keys that you do not add and translate will continue to use the [configured default locale](#locale-en).

By default, this plugin will use the player's locale if possible.
See the [Configuration](#configuration) section for how to override the player's locale for all players.

If you have translated the plugin to your language, please help the development of this plugin by submitting your translation as [a GitHub issue](https://github.com/Deltik/SignEdit/issues/new) or [a GitHub pull request](https://github.com/Deltik/SignEdit/compare)!

## Compatibility

This plugin is a fork of [Omel's SignEdit](https://www.spigotmc.org/resources/signedit.25485/) v1.3.  Omel's SignEdit v1.3 and older are not compatible with Bukkit v1.12 and newer because a deprecated API method was removed ([see related issue in MyPet](https://github.com/xXKeyleXx/MyPet/issues/1033)).

Since this plugin does not use deprecated methods, it is expected to be compatible with Bukkit v1.8.3 and newer.

Support for Bukkit v1.8 and lower was dropped in plugin version `>= 1.3.1`.  This is because plugin version `>= 1.3.1` compiles with a method signature introduced in Bukkit commit [e1f54099](https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/commits/e1f54099c8d6ba708c2895803464a0b89cacd3b9#src/main/java/org/bukkit/entity/LivingEntity.java), which landed in [Bukkit v1.8.3](https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/commits/ca4fd8554d297f3922d36328efd4612b05f9d8aa#pom.xml).

### Version Compatibility Table

|Plugin [Version](#versioning)|Minimum Minecraft Version|Maximum Minecraft Version|Cause of Compatibility Change|
|---|---|---|---|
|`<= 1.3`|1.2.2|1.11.2|Deprecated API removed in Bukkit v1.12|
|`>= 1.3.1, < 1.8`|1.8.3|_No known incompatibility_|[Switched to the non-deprecated Bukkit v1.8.3 API method](https://github.com/Deltik/SignEdit/commit/f1ca24893b3a0099da846f1dbd4b7770c2821c4a)|
|`>= 1.8, < 1.10.2`|1.8.3|1.15.2|[Native sign editor defect in Minecraft 1.16.1](#minecraft-1161-sign-editor-gui)|
|`>= 1.10.2, < 1.12`|1.12|1.15.2|[Localization features](#supported-locales) only available starting in Bukkit v1.12|
|`>= 1.12, < 1.12.7`|1.13|1.16.5|Workaround for [native sign editor defect in Minecraft 1.16.1](#minecraft-1161-sign-editor-gui) uses Bukkit v1.13 materials|
|`>= 1.12.7`|1.13|_No known incompatibility_|Plugin updated to tolerate [a Minecraft unstable API (`net.minecraft` code) obfuscation requirement for Minecraft 1.17+](https://web.archive.org/web/20210613005238/https://www.spigotmc.org/threads/spigot-bungeecord-1-17.510208/#post-4184317)|

### Backwards Compatibility with Omel's SignEdit v1.3

SignEdit for Bukkit versions `~> 1.5` are backwards-compatible with Omel's SignEdit v1.3, but the following caveats apply:

* (`~> 1.5`) By default, sign line numbers range from 1 to 4 in this plugin instead of 0 to 3 in the original plugin.

  To restore the original behavior and start line numbers at 0, set [`line-starts-at: 0`](#line-starts-at-10) in `plugins/SignEdit/config.yml`.
* (`~> 1.7`) By default, `clicking` mode is activated when the player is not looking at a sign.

  To force `clicking` mode on at all times, set `clicking: true` in `plugins/SignEdit/config.yml`.

  To force `clicking` mode off at all times, set `clicking: false`.
* (`~> 1.4.0`) Sign line numbers range from 1 to 4, whereas they ranged from 0 to 3 in older versions.

  Upgrade to SignEdit for Bukkit version `~> 1.5` to have the possibility of restoring the original line number range.

### Compatibility with Permissions Plugins

Since `>= 1.8`, other plugins can receive a [`SignChangeEvent`](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/block/SignChangeEvent.html) from SignEdit for Bukkit and cancel the event to deny the player from editing a sign through this plugin.

In `>= 1.8, < 1.10`, this plugin emitted `SignChangeEvent`s with blank lines.  This is incompatible with plugins that validate the contents of sign changes (e.g. censorship or "bad word" plugins).  In `>= 1.10`, the new sign contents are sent with the `SignChangeEvent`, which allows other plugins to validate the text of the sign change.

Some permissions plugins that check if a player can break or place a block do not also check if the player can edit signs.  It is typically undesirable for a player to be able to edit a sign that they cannot place or break.  To improve compatibility with these plugins, SignEdit for Bukkit `>= 1.12.2` offers [a configuration option](#compatibilityedit-validation-standardextranone) to send a [`BlockBreakEvent`](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/block/BlockBreakEvent.html) and a [`BlockPlaceEvent`](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/block/BlockPlaceEvent.html) before the `SignChangeEvent` as if the player broke, replaced, and rewrote the targeted sign.  The extra events will not reflect the actual condition of the sign block; it will not be broken and replaced―only changed.

### Minecraft 1.16.1 Sign Editor GUI

In Minecraft 1.16.1, invoking the native sign editor GUI with `/sign ui` (`>= 1.8, < 1.12`) will open a blank sign editor without the existing sign contents.
This is [a regression (bug) in the Minecraft client](https://web.archive.org/web/20200901000000/https://bugs.mojang.com/browse/MC-192263?focusedCommentId=755369&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#comment-755369); there is no way to get the native sign editor to open correctly from the Bukkit v1.16.1 server.

Despite [Mojang's refusal to fix the bug](https://web.archive.org/web/20200714051840/https://bugs.mojang.com/browse/MC-192263?focusedCommentId=759126&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#comment-759126), [it was fixed in Minecraft 1.16.2](https://minecraft.gamepedia.com/Java_Edition_20w30a).

(`>= 1.12.0`)

Plugin version `= 1.12.0` introduces a clunky workaround that offers a sign editor GUI via a [book and quill](https://minecraft.gamepedia.com/Book_and_Quill) (AKA writable book):

![Sign editor GUI implemented as a writable book](https://i.imgur.com/KAetJWB.png)

The workaround is applicable to these Minecraft versions:

|Plugin [Version](#versioning)|Minecraft Version|Rationale|
|---|---|---|
|`>= 1.12.2`|[_Admin's choice_](#compatibilitysign-ui-autoeditablebooknative)|To offer the choice of which sign editor GUI implementation to use|
|`= 1.12.1`|1.16.1 only|The bug was [unexpectedly fixed in Minecraft 1.16.2](https://minecraft.gamepedia.com/Java_Edition_20w30a).|
|`= 1.12.0`|1.16 and higher|[MC-192263 was closed as invalid](https://web.archive.org/web/20200901000000/https://bugs.mojang.com/browse/MC-192263?focusedCommentId=755369&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#comment-755369), suggesting the bug was here to stay.|
|`< 1.12`|_Not applicable_|The bug was unknown at the time these plugin versions were released.|

When using the workaround, instead of opening the native sign editor after the player runs `/sign ui`, this plugin places a temporary book and quill in their hand.
To open the alternative sign editor, the player must look away from the sign and then right-mouse click.
The writable book opens, and the first four lines represent the four lines on the sign.
Once the player commits the changed text, the book is removed, the sign is updated, and the item they were originally holding is restored.

As this implementation is a hacky workaround, note these caveats:
* If the Bukkit server crashes or otherwise uncleanly unloads the plugin while the player is using this alternative sign editor GUI, the player will be given the book and quill, and the item originally in their hand will be destroyed.
* If the player is in [Creative mode](https://minecraft.gamepedia.com/Creative) and selects the sign editor item in their inventory, the item will be cloned.

If the caveats are too problematic, you should disable the `/sign ui` subcommand by revoking the `signedit.sign.ui` [permission](#permissions).