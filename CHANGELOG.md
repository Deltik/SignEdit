# Changelog

[![GitHub releases](https://img.shields.io/github/release/Deltik/SignEdit.svg)](https://github.com/Deltik/SignEdit/releases)

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## v1.12.3 (UNRELEASED)

### Added

* Tab completion for multi-line selection (e.g. `/sign 1,2,…` and `/sign cut 3-4,1-…`)

### Changed

* `/sign` subcommands are now case-insensitive.  For example, `/sign ClEaR` is now the same as `/sign clear`.
  Previously, capitalized subcommands would not be understood as valid subcommands.
* `/sign ui` with the native sign editor no longer modifies the real sign on the server to present human-editable formatting codes.
  Only the player who invoked the command will see the reformatted sign.

### Fixed

* `/sign cut` only put empty lines into the clipboard (#17)
* `/sign ui` with the native sign editor incorrectly puts the de-formatted sign text into the history (regression from `= 1.12.2`)
* `/sign ui` on Minecraft 1.16.2+ loses all formatting codes when opening the default (native) sign editor GUI.
* Line selection validation for `/sign cut` and `/sign copy`
* Line selection validation accepts empty delimiters at the end (e.g. `/sign set 1,3,,,,`) but should not

## v1.12.2 (2020-08-10)

### Added

* Allow the administrator to opt in or out of the alternative sign editor GUI introduced in `= 1.12.0` with the new configuration option `compatibility.sign-ui: [Auto|EditableBook|Native]`
* New configuration option to send extra events (`BlockBreakEvent` and `BlockPlaceEvent`) on sign edit: `compatibility.edit-validation: [Standard|Extra|None]`

### Fixed

* If there was an error while editing a sign in two or more steps (e.g. right-mouse click action, sign editor GUI), the error won't be shown to the player, and the server console shows a "Cannot pass event … to SignEdit v…" error and stack trace. (#16)
* The `SignChangeEvent` emitted natively by the native sign editor GUI is no longer sent twice.

### Under the Hood

* Removed the confusing concept of "in progress" interactions.
  These are no different from "pending" interactions.
* Cleaned up dependencies passed from `SignSubcommand` to `SignEditInteraction` by letting Dagger 2 handle them
* `Configuration` getters now validate their options and return the default if the existing value is invalid.
* Builds now use the Bukkit v1.13 library to limit feature development to the minimum version supported

## v1.12.1 (2020-07-23)

### Changed

* The alternative sign editor GUI introduced in `= 1.12.0` has been restricted to Minecraft 1.16.1 only.  Previously, the GUI was present on Minecraft 1.16 and above, but the bug that necessitated it was [fixed in Minecraft 1.16.2](https://minecraft.gamepedia.com/Java_Edition_20w30a).  Using this version of the plugin on Minecraft 1.16.2 and higher will open the native sign editor GUI again.

### Under the Hood

* Build and test dependencies updated to their latest versions

## v1.12.0 (2020-07-14)

### Added

* Support for Minecraft 1.16 sRGB [text formatting](https://git.io/SignEdit-README#formatting-codes):
  ![SignEdit for Bukkit: Support for web colors](https://user-images.githubusercontent.com/1364268/87382418-80653180-c55c-11ea-9b26-539868e6ba11.png)
* [Alternative sign editor GUI for Minecraft 1.16+](https://git.io/SignEdit-README#minecraft-116-sign-editor-gui) implemented as a [writable book](https://minecraft.gamepedia.com/Book_and_Quill) to work around [a Minecraft 1.16 sign editor regression](https://web.archive.org/web/20200901000000/https://bugs.mojang.com/browse/MC-192263?focusedCommentId=755369&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#comment-755369) that [Mojang refused to fix](https://web.archive.org/web/20200714051840/https://bugs.mojang.com/browse/MC-192263?focusedCommentId=759126&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#comment-759126):
  ![Sign editor GUI implemented as a writable book](https://user-images.githubusercontent.com/1364268/87382228-1056ab80-c55c-11ea-8cf6-54e63d8c94dd.png)

* New locale strings:
  * `sign_editor_item_name` – The display name of the ephemeral book and quill used in Minecraft 1.16+ as an alternative for the broken native sign editor
  * `right_click_air_to_open_sign_editor` – Tells the player to look away from the targeted sign and right-mouse click to open the sign editor (Minecraft 1.16+)
  * `right_click_air_to_apply_action_hint` – Right-mouse click air action hint shown in `/sign status`, currently only used for `/sign ui` on Minecraft 1.16

### Changed

* Minimum Bukkit version is now v1.13 (was v1.12) due to the usage of Bukkit v1.13 materials for the alternative sign editor GUI
* Changed locale strings:
  * `cancelled_pending_right_click_action` has been renamed to `cancelled_pending_action` and no longer explicates the right-mouse click.
  * `no_right_click_action_to_cancel` has been renamed to `no_pending_action_to_cancel`, and the associated action is now modified by "pending" rather than "right-click".

### Fixed

* Translation improvements for the Dutch (`nl`) locale
  Credit to SBDeveloper (#14)
* Translation improvements for the Simplified Chinese (`zh`) and Traditional Chinese (`zh-TW`) locales
  Credit to Aobi (#15)

## v1.11.4 (2020-03-05)

### Added

* Intermediate translation for Simplified Chinese (`zh` and `zh-CN`) locale replaces the machine translation of Chinese introduced in `= 1.10.2`.
  Credit to Aobi for the translation (#13)
* Intermediate translation for Traditional Chinese (`zh-TW`) locale
  Credit to Aobi for the translation (#13)

### Under the Hood

* Gradle has been updated from version 6.1.1 to version 6.2.2.

## v1.11.3 (2020-02-23)

### Fixed

* On Microsoft Windows, SignEdit `>= 1.11.0, < 1.11.3` could not load if its JAR path contains any spaces or special URL characters. (#12)

### Under the Hood

* Prevent potential type coersion of version string in `plugin.yml`
* Resource value replacements in `processResources` Gradle task now invalidate cached values if they have changed.

## v1.11.2 (2020-02-23)

### Fixed

* SignEdit `>= 1.11.0, < 1.11.2` was broken on Microsoft Windows due to a path separator oversight. (#11)

## v1.11.1 (2020-01-28)

### Added

* Native translation for Dutch (`nl`) locale<br />
  Credit to SBDeveloper (#10)

### Fixed

* In the English (`en`/default), German (`de`), and Dutch (`nl`) locales, if there is a pending sign action and the player runs `/sign status`, Minecraft 1.15+ displayed a non-breaking space block.  This character has been changed to a normal space.

## v1.11.0 (2020-01-28)

### Added

* The theme (colors) and translations of this plugin can now be customized in the locale files! (Fixes #9)

### Fixed

* The `locale` configuration option was not working as documented.  In `= 1.10.2`, if the player's locale was not supported, English would always be used as the fallback.  This release follows the documented behavior: the fallback language is what is specified in the `locale` configuration option, and the fallback for that fallback is English.
* The `force-locale` configuration option is now sanitized; a boolean value is now enforced.
* Concatenation eliminated from localized messages, which increases flexibility for future language translations
* Numbers are now correctly localized
* Chinese punctuation
* Chinese grammar?  (Still machine-translated)
* The Spigot `api-version` warning is now suppressed, at least from Spigot 1.13 through Spigot 1.15.2.
* The configuration handler may excuse input/output (I/O) errors when it should not be continuing to run the plugin.  I/O errors while enabling the plugin now prevent the plugin from starting.
* Resolved a `java.lang.NullPointerException` in the configuration handler due to some mismanaged state when the `./plugins/SignEdit/` folder doesn't exist.

### Changed

* The configuration file `config.yml` is now rendered from a template.  The template includes comments to make configuration easier to understand without consulting the online documentation.

### Under the Hood

* Builds now use ProGuard to shrink the JAR file size.
* Each source code file now contains the proper copyright and license notice.
* Replaced various method signatures containing the `SignEditPlugin` type with the `Plugin` interface
* Gradle now supplies the plugin name to the resource templates.

## v1.10.2 (2019-04-11)

### Added

* Localization for all chat messages
* Native translation for English (`en`) locale
  ![Locale_en](https://user-images.githubusercontent.com/1364268/55949780-02a7cd00-5c19-11e9-83b5-a23da3a60c8d.png)
* Native translation for German (`de`) locale
  ![Locale_de](https://user-images.githubusercontent.com/1364268/55949779-02a7cd00-5c19-11e9-9e2a-b09b69700218.png)
* Machine translation for Simplified Chinese (`zh`) locale
  ![Locale_zh](https://user-images.githubusercontent.com/1364268/55949781-02a7cd00-5c19-11e9-80a8-57c37e3a6a99.png)

### Under the Hood

* All user-facing text has been encapsulated in a call to `ChatComms.t()` for translations.
* Translations come from localized `Comms` ResourceBundles.

## v1.10.1 (2019-04-09)

### Fixed

* Compatibility with PaperMC 1.13.2 #547 and newer
  * PaperMC patch that broke `/sign ui`: https://github.com/PaperMC/Paper/commit/906684ff4f9413fda228122315fdf0fffa674a42
  * Our workaround: https://github.com/Deltik/SignEdit/commit/040cbd0c8005d336fb534a7427c0731137de384a

## v1.10.0 (2019-04-07)

### Added

* Tab completion of `/sign` subcommands
* Ability to manipulate more than one line in a `/sign` command
* Sign changes now show the full set of changes before and after.
* New `/sign` subcommands:
  * `/sign status` – Show the player's pending action and what is in their copy buffer.
  * `/sign copy` – Copy the targeted sign's text. (Fixes #8)
  * `/sign cut` – Copy the targeted sign's text and remove it from the sign.
  * `/sign paste` – Paste the lines buffered by the previous `/sign copy` command onto the targeted sign.
  * `/sign undo` – Revert the previous sign change.
  * `/sign redo` – Restore the most recent sign change that was undone by `/sign undo`.
* Plugin usage help vanity URL
  * Old URL: https://github.com/Deltik/SignEdit/blob/master/README.md
  * New URL: https://git.io/SignEdit-README

### Fixed

* Permission inheritance of `/sign version` was not defined correctly in `plugin.yml`
* Permission inheritance of `/sign cancel` was not defined correctly in `plugin.yml`
* In `clicking` mode `false`, some subcommands would spit out usage text if the player wasn't looking at a sign, but this should not happen.
* It is now possible to input literal ampersand (`&`) characters using `/sign` subcommands thanks to smarter parsing of sign text and an escape sequence (`\&`).

### Changed

* The README should be more readable now.
* The single line selector has been replaced with a multiple line selector.
* When the plugin is unloading, the configuration file is now reloaded before being rewritten, so changes made while the plugin is loaded will now persist.
* Players cannot edit signs that they do not have permission to edit. Every attempted edit is validated through a [SignChangeEvent](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/block/SignChangeEvent.html) and will not succeed if another plugin or policy cancels the SignChangeEvent.

### Under the Hood

* Gradle has been updated from version 3.5rc2 to version 5.3.
* A concept of "selected lines", which are parsed for multi-line interactions in `/sign` subcommands
* Some dependencies are now injected by [Dagger 2](https://github.com/google/dagger)
* Player chat interactions now happen through a `ChatComms` class.
* Added a set of Exceptions for things that can go wrong in this plugin
* Exceptions are centrally reported in `ChatComms`.
* New model `SignText` handles the state of a single sign edit and can import, back up, restore, and change Bukkit `Sign` blocks.
* New singleton `SignTextClipboardManager` facilitates cut, copy, and paste for each player.
* New singleton `SignTextHistoryManager` maps players to their undo/redo history and tracks their state globally.
* `ArgStruct` has been replaced with a more extensible `ArgParser`, including support for multiple absolute selected lines instead of a single relative selected line.
* The ["pending right-click" `Interact` class](https://github.com/Deltik/SignEdit/blob/v1.3.1/EventHandler/Interact.java#L20) has been renamed to `SignEditListener` to be less confusing.
* `SignEditCommit` has been renamed to `SignEditInteraction` to reduce confusion.  Its subclasses have also been renamed correspondingly.
* Various other methods and classes have been renamed to make more sense.
* Tests have been moved into packages corresponding to the classes that they test.
* Tests rewritten to be more unit-y

## v1.9.3 (2018-07-25)

### Added

* `/sign version` subcommand shows the version of the plugin currently installed.

### Fixed

* `/sign ui` now works in Spigot 1.13.

### Under the Hood

* Code readability improvements in `UiSignEditCommit`
* `/sign ui` should now be more resistant to breaking private API changes at the Bukkit and CraftBukkit level.

## v1.9.2 (2018-06-15)

### Fixed

* Major bug causing the last `/sign ui` edit to be wiped out when the player disconnects from the server. (Fixes #7)

## v1.9.1 (2018-06-15)

> ## Major Bug Notice (#7)
>
> ### Affected Versions
>
> * v1.8.0
> * v1.9.0
> * v1.9.1
>
> ### Upgrade Paths
>
> * v1.8.0 → v1.9.2
> * v1.9.0 → v1.9.2
> * v1.9.1 → v1.9.2
>
> ### Description
>
> On affected versions, the last sign edit a player performed with `/sign ui` will be rolled back when the player disconnects.
>
> All affected users are encouraged to upgrade to v1.9.2 or newer to resolve this bug.
>
> ### Workaround
>
> Invoke `/sign ui` on any sign before logging out.

### Fixed

* `/sign ui` now works in Spigot 1.8.8, including [Paper](https://github.com/PaperMC/Paper) 1.8.8. (Fixes #6)

## v1.9.0 (2018-06-12)

> ## Major Bug Notice (#7)
>
> ### Affected Versions
>
> * v1.8.0
> * v1.9.0
> * v1.9.1
>
> ### Upgrade Paths
>
> * v1.8.0 → v1.9.2
> * v1.9.0 → v1.9.2
> * v1.9.1 → v1.9.2
>
> ### Description
>
> On affected versions, the last sign edit a player performed with `/sign ui` will be rolled back when the player disconnects.
>
> All affected users are encouraged to upgrade to v1.9.2 or newer to resolve this bug.
>
> ### Workaround
>
> Invoke `/sign ui` on any sign before logging out.

### Added

* `/sign cancel` subcommand cancels the player's pending right-mouse click action when "clicking" mode is in effect. (Fixes #5)
* If the permissions system forbids the player from running a `/sign` command or subcommand, the player will see an error message instead of the plugin silently ignoring the command.

### Fixed

* When the player types an out-of-bounds line number in `/sign set <line>`, Bukkit no longer vomits the full plugin usage help.

### Changed

* The plugin's general messages to the player were in red, but now they're gold to distinguish them from error messages, which are still red.

## v1.8.0 (2017-12-08)

> ## Major Bug Notice (#7)
>
> ### Affected Versions
>
> * v1.8.0
> * v1.9.0
> * v1.9.1
>
> ### Upgrade Paths
>
> * v1.8.0 → v1.9.2
> * v1.9.0 → v1.9.2
> * v1.9.1 → v1.9.2
>
> ### Description
>
> On affected versions, the last sign edit a player performed with `/sign ui` will be rolled back when the player disconnects.
>
> All affected users are encouraged to upgrade to v1.9.2 or newer to resolve this bug.
>
> ### Workaround
>
> Invoke `/sign ui` on any sign before logging out.

### Added

* `/sign ui` command opens the native Minecraft sign editor on the target sign. (Fixes #3)
* More fine-grained permissions in the format of `signedit.COMMAND.SUBCOMMAND`
* Right before editing a sign, SignEdit simulates blanking out the targeted sign to see if another plugin is forbidding the player from editing the sign.  SignEdit will no longer edit a sign that the player isn't allowed to edit.

### Fixed

* Error messages to the user are now more consistent.

### Under The Hood

* The plugin package was renamed from `org.deltik.mc.SignEdit` to `org.deltik.mc.signedit`.
* The handling of "clicking" mode is now standardized for all sign editing methods.
* Somewhat improved variable naming
* `SignSubcommand` is the abstract for `/sign` subcommands.
* `plugin.yml` is now much more descriptive
* Tests were reorganized, but they're still not that great.
* Eliminated some unnecessary injected dependencies

## v1.7.0 (2017-10-10)

### Added

* `clicking: auto` mode, which combines the best of the `clicking: false` and `clicking: true` modes.  Now, the player can edit a sign if the player is looking at a sign or SignEdit will prompt for a right-click on a sign if the player is not looking at a sign.  (Fixes #2)
* `clicking: auto` is now the default mode.  Before `v1.7.0`, the default was `clicking: false`.

### Under The Hood

* Minor changes to improve code quality

## v1.6.0 (2017-10-04)

### Added
- The "set" or "clear" part of `/sign {set,clear} <line> [<text>]` is now optional because behavior can be inferred from `<line>`. (Fixes #1)

### Fixed
- Removed ambiguity from right-click instruction to player in `clicking: true` mode
  **Before:** `§cNow right-click a block to set the line`
  **After:** `§cNow right-click a sign to set the line`
- Before a configuration file is written, it is now checked for sanity.
- Possible crash if `clicking` config option is not properly set
- Plugin errors out when running `/sign {set,clear} <line> [<text>]` and `<line>` is not an integer

### Under The Hood
- [Builds of SignEdit for Bukkit are now tested automatically](https://circleci.com/gh/Deltik/SignEdit), which reduces the possibility of unplanned regressions in future releases.
- SignEdit for Bukkit now compiles successfully on Bukkit versions older than v1.12.
- `Configuration.setClicking()` sets clicking mode
- `Configuration.setLineStartsAt()` sets line starts at value

## v1.5.0 (2017-09-26)

### Added
* Configuration option `line-starts-at` (fixes #4)
* The configuration file is sanitized when the plugin is loaded and unloaded and rewritten when the plugin is unloaded.

### Fixed
* When a sign is edited in `clicking: true` mode, the output is now the same as in `clicking: false` mode.
* More code cleanup

## v1.4.0 (2017-09-25)

> ## Major Bug Notice (#4)
>
> ### Affected Versions
>
> * v1.4.0
>
> ### Upgrade Paths
>
> * v1.4.0 → v1.5.0
>
> ### Description
>
> Affected versions are backwards-incompatible with previous versions due to a change in line selection behavior.
>
> All affected users are encouraged to upgrade to version v1.5.0 and add the configuration option `line-starts-at: 0` to restore the version `< 1.4` line selection behavior.
>
> ### Workaround
>
> Add 1 to every line selection to select the intended line.

### Added
* When a sign edit is committed, the return will indicate whether the line was blanked, changed, or unmodified
* When a sign line is changed, the return shows the text before and the text after
* Aliases for `/sign`: `/signedit`, `/editsign`, and `/se`
* Usage notes when not enough arguments are passed to `/sign`

### Fixed
* Bukkit v1.12 compatibility: Plugin no longer uses deprecated and removed `LivingEntity.getTargetBlock(HashSet<Byte>, int)` method
* Miscellaneous code cleanup
* Grammar, spelling, and wording

### Changed
* Line numbers now begin with 1 instead of 0

## v1.3.1 (2018-06-13)

> ## End of Life Notice
>
> ### Affected Versions
>
> * v1.3.1 and older
>
> ### Upgrade Paths
>
> * v1.0 → v1.3.1
> * v1.1 → v1.3.1
> * v1.2 → v1.3.1
> * v1.3 → v1.3.1
> * v1.3.1 → latest v1.x release
>
> ### Description
>
> This release is a hotfix applied to Omel's SignEdit v1.3 to make it compatible with Bukkit 1.12.
>
> Deltik will no longer release any more updates of any kind, including hotfixes, to Omel's SignEdit, so if this release stops working in newer versions of Bukkit, Deltik will not fix it.
>
> Users who want to use Omel's SignEdit on Bukkit 1.12 must upgrade to v1.3.1.
>
> All users are encouraged to upgrade from v1.3.1 to the latest v1.x release for improved features and functionality while retaining backwards compatibility with Omel's SignEdit.

### Fixed

* The original Omel's SignEdit v1.3 has been updated to be compatible with Bukkit 1.12 and future Bukkit versions in the foreseeable future.

### Changed

* Internal plugin version number bumped from 0.2 to 1.3.1

## v1.3 (2016-07-04)

[Changelog from upstream](https://www.spigotmc.org/resources/signedit.25485/updates):

> ### Sign Text set Updated
> Now works with a StringBuilder which deletes the last char.

## v1.2 (2016-07-04)

[Changelog from upstream](https://www.spigotmc.org/resources/signedit.25485/updates):

> ### Console Exception Spam Fix
> Fixed Console Spam Exceptions

## v1.1 (2016-07-02)

[Changelog from upstream](https://www.spigotmc.org/resources/signedit.25485/updates):

> ### Added Config
> Added Config.

## v1.0 (2016-06-26)

Initial release

*No changelog provided from upstream*
