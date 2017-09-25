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

**Aliases:** `/signedit`, `/editsign`, `/se`

#### `/sign set <line> [<text>]`

Change the line *line* of the selected sign to *text*.  If *text* is blank, erase the line *line*.

#### `/sign clear <line>`

Erase the line *line*.

## Permissions

SignEdit for Bukkit will only work if the player has the following permission enabled:

    SignEdit.use

## Configuration

SignEdit for Bukkit currently supports one legacy feature known as "clicking".  By default, you edit signs by looking at them and then typing a `/signedit` command.  In the legacy "clicking" mode, you edit signs by typing a `/signedit` command and then right-mouse clicking a sign.

To enable "clicking" mode, open the file `plugins/SignEdit/config.yml` and set this setting:

    clicking: true
    
To revert to the default behavior, delete the `clicking` setting or write this setting instead:

    clicking: false

## Features

### Currently Implemented

* *(`clicking: false` mode only)* Look at a sign and edit one line with `/sign set`.
* *(`clicking: true` mode only)* Edit a sign with `/sign set` and then right-mouse clicking the target sign.

### Planned

* `/sign {1,2,3,4} [<text>]` as a shorthand for `/sign set {1,2,3,4} [<text>]`
* Default `clicking: auto` mode, which will edit a sign if the player is looking at one or will prompt for a right-click on a sign if the player is not looking at a sign.
* `/sign ui` invokes the native Minecraft text editor for signs on the target sign.

## Compatibility

This plugin is a fork of [Omel's SignEdit](https://www.spigotmc.org/resources/signedit.25485/) v1.3.  Omel's SignEdit v1.3 and older are not compatible with Bukkit v1.12 and newer because a deprecated API method was removed ([see related issue in MyPet](https://github.com/xXKeyleXx/MyPet/issues/1033)).

Since this plugin does not use deprecated methods, it is expected to be compatible with Bukkit v1.7 and newer.  Unit tests and continuous integration have not been implemented in this project yet.

### Backwards-Incompatible Changes

* Sign line numbers range from 1 to 4 in this plugin instead of 0 to 3 in the original plugin.
  **Note:** In the future, a `lineStartsAt` configuration option may be added to restore backwards compatibility.
