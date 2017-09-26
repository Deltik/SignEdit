# SignEdit for Bukkit

**SignEdit for Bukkit** is a [Bukkit plugin](https://www.spigotmc.org/resources/categories/bukkit.4/) that allows players to edit sign objects by looking at them or clicking on them and typing in a SignEdit command.

## Installation

1. Download the latest `.jar` file from [the Releases page](https://github.com/Deltik/SignEdit/releases) and upload it to your CraftBukkit/Spigot `plugins/` folder.
2. Do one of the following:
   - Restart your server.
   - Run the `reload` command from the server console.
   - Say `/reload` as an _op_ player.

## Usage

### `/sign`

(_since `v1.4.0`_) **Aliases:** `/signedit`, `/editsign`, `/se`

#### `/sign set <line> [<text>]`

Change the line *line* of the selected sign to *text*.  All `&` characters are replaced with `§` for formatting codes.  (_since `v1.4.0`_) If *text* is blank, erase the line *line*.  

#### `/sign clear <line>`

(_since `v1.4.0`_) Erase the line *line* of the selected sign.

## Permissions

SignEdit for Bukkit will only work if the player has the following permission enabled:

    SignEdit.use

## Configuration

All configuration is in the file `plugins/SignEdit/config.yml`.

(_since `v1.1`_) The configuration file is created with default values when the plugin is loaded and the file does not already exist.

(_since `v1.5.0`_) The configuration file is sanitized when the plugin is loaded and unloaded and rewritten when the plugin is unloaded.

### `clicking: [auto|false|true]`

_Added in `v1.1`_

(_since `v1.7.0`_) **auto** (default): Behave like `clicking: true` when you are not looking at a sign and behave like `clicking: false` when you are looking at a sign.

**false** (default _until `v1.7.0`_): Edit signs by looking at them and then typing a `/sign` command, which will then instantly edit the sign you are looking at.

**true**: Edit signs by typing a `/sign` command and then right-mouse clicking a sign.

### `line-starts-at: [1|0]`

_Added in `v1.5.0`_

**1** (default): Line number 1 corresponds to the top-most line of sign blocks.

**0**: Line number 0 corresponds to the top-most line of sign blocks.

## Features

### Currently Implemented

* *(`clicking: false` mode only)* Look at a sign and edit one line with `/sign set`.
* *(`clicking: true` mode only)* Edit a sign with `/sign set` and then right-mouse clicking the target sign.

### Planned

* (_`v1.6.0`_) `/sign <line> [<text>]` as a shorthand for `/sign set <line> [<text>]`
* (_`v1.7.0`_) Default `clicking: auto` mode, which will edit a sign if the player is looking at one or will prompt for a right-click on a sign if the player is not looking at a sign.
* `/sign ui` invokes the native Minecraft text editor for signs on the target sign.

## Compatibility

This plugin is a fork of [Omel's SignEdit](https://www.spigotmc.org/resources/signedit.25485/) v1.3.  Omel's SignEdit v1.3 and older are not compatible with Bukkit v1.12 and newer because a deprecated API method was removed ([see related issue in MyPet](https://github.com/xXKeyleXx/MyPet/issues/1033)).

Since this plugin does not use deprecated methods, it is expected to be compatible with Bukkit v1.7 and newer.  Unit tests and continuous integration have not been implemented in this project yet.

### Backwards Compatibility with Omel's SignEdit v1.3

SignEdit for Bukkit versions `>= 1.5.0, < 2.0.0` are backwards-compatible with Omel's SignEdit v1.3, but the following caveats apply:

* (_since `v1.5.0`_) By default, sign line numbers range from 1 to 4 in this plugin instead of 0 to 3 in the original plugin.

  To restore the original behavior and start line numbers at 0, set `line-starts-at: 0` in `plugins/SignEdit/config.yml`.
* (_since `v1.7.0`_) By default, `clicking` mode is activated when the player is not looking at a sign.

  To force `clicking` mode on at all times, set `clicking: true` in `plugins/SignEdit/config.yml`.

  To force `clicking` mode off at all times, set `clicking: false`.
* (_`v1.4.0` only_) Sign line numbers range from 1 to 4, whereas they ranged from 0 to 3 in older versions.

  Upgrade to SignEdit for Bukkit v1.5.0 to have the possibility of restoring the original line number range.
