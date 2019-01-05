# SignEdit for Bukkit

[![GitHub releases](https://img.shields.io/github/release/Deltik/SignEdit.svg)](https://github.com/Deltik/SignEdit/releases)
[![CircleCI](https://img.shields.io/circleci/project/github/Deltik/SignEdit.svg)](https://circleci.com/gh/Deltik/SignEdit)

**SignEdit for Bukkit** is a [Bukkit plugin](https://www.spigotmc.org/resources/categories/bukkit.4/) that allows players to edit sign objects by looking at them or clicking on them and typing in a SignEdit command.

![Screenshot of usage help in SignEdit for Bukkit v1.9.2](https://i.imgur.com/HSNhFPB.png)

## Installation

1. Download the latest `.jar` file from [the Releases page](https://github.com/Deltik/SignEdit/releases) and upload it to your CraftBukkit/Spigot `plugins/` folder.
2. Do one of the following:
   - Restart your server.
   - Run the `reload` command from the server console.
   - Say `/reload` as an _op_ player.

## Usage

### Commands

| Command | Usage | Version |
| --- | --- | --- |
| `/sign` | Show the usage of the `/sign` subcommands. | `>= 1.0` |
| `/sign ui` | Invoke the native Minecraft sign editor on the targeted sign. | `>= 1.8` |
| `/sign set <line> <text>` | Change the line `<line>` of the targeted sign to `<text>`.  All `&` characters are replaced with `§` for formatting codes. | `>= 1.0, < 1.4` |
| `/sign set <line> [<text>]` | Change the line `<line>` of the targeted sign to `<text>`.  All `&` characters are replaced with `§` for formatting codes. If `<text>` is blank, erase the line `<line>`. | `>= 1.4, < 1.10` |
| `/sign set <lines> [<text>]` | Change each of the lines `<lines>` of the targeted sign to `<text>`.  All `&` characters are replaced with `§` for formatting codes. If `<text>` is blank, erase the lines `<lines>`. | `>= 1.10` |
| `/sign clear <line>` | Erase the line `<line>` of the targeted sign. | `>= 1.4, < 1.10` |
| `/sign clear <lines>` | Erase the lines `<lines>` of the targeted sign. | `>= 1.10` |
| `/sign cancel` | Abort your pending right-mouse click sign edit action. | `>= 1.9` |
| `/sign status` | Show the pending action and what is in the copy buffer. | `>= 1.10` |
| `/sign copy [<lines>]` | Copy the targeted sign's text.  If `<lines>` is specified, only those lines are copied. | `>= 1.10` |
| `/sign cut [<lines>]` | Copy the targeted sign's text and remove it from the sign.  If `<lines>` is specified, only those lines are cut. | `>= 1.10` |
| `/sign paste` | Paste the lines buffered by the previous `/sign copy` command onto the targeted sign. | `>= 1.10` |
| `/sign undo` | Revert the previous sign change. | `>= 1.10` |
| `/sign redo` | Restore the most recent sign change that was undone by `/sign undo`. | `>= 1.10` |
| `/sign version` | Show the installed version of this plugin. | `>= 1.9.3` |

### Aliases

| Alias | Command | Version |
| --- | --- | --- |
| `/signedit` | `/sign` | `>= 1.4` |
| `/editsign` | `/sign` | `>= 1.4` |
| `/se` | `/sign` | `>= 1.4` |
| `/sign <line> [<text>]` | `/sign set <line> [<text>]` | `>= 1.6, < 1.10` |
| `/sign <lines> [<text>]` | `/sign set <lines> [<text>]` | `>= 1.10` |
| `/sign <line>` | `/sign clear <line>` | `>= 1.6, < 1.10` |
| `/sign <lines>` | `/sign clear <lines>` | `>= 1.10` |

### Visual Examples

#### `/sign ui`

![GIF of `/sign ui` in action](https://i.imgur.com/TnVpy2v.gif)

#### `/sign 2 Deltik's`

![GIF of `/sign <line> <text>` in action](https://i.imgur.com/B1MNGXv.gif)

#### `/sign clear 4`

![GIF of `/sign clear <line>` in action](https://i.imgur.com/iVTGLra.gif)

## Syntax

### Line Ranges

(`>= 1.10`)

For commands that take a *lines* argument, the specified lines can be any combination of a comma-delimited list (e.g. `1,3` selects lines 1 and 3) and a hyphenated range (e.g. `1-3` selects lines 1, 2, and 3).

#### Examples

- `1` selects line 1.
- `1-1` selects line 1.
- `1,3` selects lines 1 and 3.
- `1-3` selects lines 1, 2, and 3.
- `1-2,4` selects lines 1, 2, and 4.
- `1,2,4` selects lines 1, 2, and 4.
- `2-3,1,4` selects lines 1, 2, 3, and 4.
- `2,2,2,3,3` selects lines 2 and 3.

## Permissions

All features of SignEdit for Bukkit will be available if the player has the following permission:

    SignEdit.use

(`>= 1.8`) More refined permissions are available in this format:

    signedit.COMMAND.SUBCOMMAND

Examples:

    signedit.sign.cancel
    signedit.sign.clear
    signedit.sign.set
    signedit.sign.ui
    signedit.sign.version

## Configuration

All configuration is in the file `plugins/SignEdit/config.yml`.

(`>= 1.1`) The configuration file is created with default values when the plugin is loaded and the file does not already exist.

(`>= 1.5`) The configuration file is sanitized when the plugin is loaded and unloaded and rewritten when the plugin is unloaded.

### `clicking: [auto|false|true]`

(`>= 1.1`)

**false** (default `< 1.7`): Edit signs by looking at them and then typing a `/sign` command, which will then instantly edit the sign you are looking at.

**true**: Edit signs by typing a `/sign` command and then right-mouse clicking a sign.

(`>= 1.7`) **auto** (default): Behave like `clicking: true` when you are not looking at a sign and behave like `clicking: false` when you are looking at a sign.

### `line-starts-at: [1|0]`

(`>= 1.5`)

**1** (default): Line number 1 corresponds to the top-most line of sign blocks.

**0**: Line number 0 corresponds to the top-most line of sign blocks.

## Features

* (`>= 1.8`) Edit the targeted sign in the native Minecraft sign editor with `/sign ui`.
  * No dependencies!
* Edit the line `<line>` of the targeted sign to be `<text>` with `/sign set <line> [<text>]` or (`>= 1.6`) `/sign <line> [<text>]`.
* All editing functions support formatting codes (`&` turns into `§`)
  ![Screenshot of color code in `/sign <line> <text>`](https://i.imgur.com/Cdv7p2t.png)
* Targeting a sign works as follows:
  * In `clicking: false` mode or in version `= 1.0`, the sign you are looking at is edited.
  * In `clicking: true` mode, after running the `/sign` command, right-click a sign to edit it.
  * (`>= 1.7`) In `clicking: auto` mode, the behavior is the same as `clicking: false` if you are looking at a sign and `clicking: true` if you are not looking at a sign.
* (`>= 1.8`) Before editing a sign, this plugin checks if the player is allowed to edit the sign by pretending to blank out the sign and seeing if the corresponding [SignChangeEvent](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/block/SignChangeEvent.html) is cancelled.

## Compatibility

This plugin is a fork of [Omel's SignEdit](https://www.spigotmc.org/resources/signedit.25485/) v1.3.  Omel's SignEdit v1.3 and older are not compatible with Bukkit v1.12 and newer because a deprecated API method was removed ([see related issue in MyPet](https://github.com/xXKeyleXx/MyPet/issues/1033)).

Since this plugin does not use deprecated methods, it is expected to be compatible with Bukkit v1.7 and newer.

### Backwards Compatibility with Omel's SignEdit v1.3

SignEdit for Bukkit versions `~> 1.5` are backwards-compatible with Omel's SignEdit v1.3, but the following caveats apply:

* (`~> 1.5`) By default, sign line numbers range from 1 to 4 in this plugin instead of 0 to 3 in the original plugin.

  To restore the original behavior and start line numbers at 0, set `line-starts-at: 0` in `plugins/SignEdit/config.yml`.
* (`~> 1.7`) By default, `clicking` mode is activated when the player is not looking at a sign.

  To force `clicking` mode on at all times, set `clicking: true` in `plugins/SignEdit/config.yml`.

  To force `clicking` mode off at all times, set `clicking: false`.
* (`~> 1.4.0`) Sign line numbers range from 1 to 4, whereas they ranged from 0 to 3 in older versions.

  Upgrade to SignEdit for Bukkit version `~> 1.5` to have the possibility of restoring the original line number range.