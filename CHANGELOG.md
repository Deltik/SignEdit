# Changelog

[![GitHub releases](https://img.shields.io/github/release/Deltik/SignEdit.svg)](https://github.com/Deltik/SignEdit/releases)

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## v1.14.3 (UNRELEASED)

### Fixed

* `/sign ui` could be invoked by right-clicking a sign in Minecraft 1.20 when the player did not have the `signedit.sign.ui` permission.

## v1.14.2 (2023-07-22)

### Fixed

* `/sign ui` and the equivalent right-click override may incorrectly be hiding issues with the player opening the sign editor. (#42)

### Under the Hood

* Remove ProGuard to improve the readability of stack traces
* Narrow down potential `/sign ui` issues by reducing the `catch` scopes to just methods and fields that are missing when opening the native sign editor GUI

## v1.14.1 (2023-07-20)

### Fixed

* Dyes could not be used on editable signs due to an overly aggressive override. (#41)
* Ink sacs and glow ink sacs could not be used on editable signs due to an overly aggressive override. (#41)
* Item usages on signs should be ignored when the player is sneaking.
  This is the native Minecraft behavior.

## v1.14.0 (2023-07-17)

### Added

* Support for Minecraft 1.20 sign sides (front and back)
* Support for Minecraft 1.20 honeycomb wax on signs
* On Minecraft 1.20+, opening an editable sign (without honeycomb wax) by right-click will open a sign editor handled by this plugin.
* (Minecraft 1.20+ only) `/sign wax` and `/sign unwax` commands and permissions to apply and remove honeycomb wax from signs, respectively
* New locale strings:
  * `unwax_sign` – `/sign unwax` pending action description
  * `wax_sign` – `/sign wax` pending action description
  * `wax_removed` – Completed `/sign unwax` action
  * `wax_applied` – Completed `/sign wax` action
  * `bypass_wax_cannot_rewax` – Warning after a `/sign` text change that wax was not reapplied because the player lacks permission to `/sign wax`
  * `forbidden_waxed_sign_edit` – Error that a `/sign` text change was blocked because the target sign is waxed and the player lacks permission to `/sign unwax`

### Changed

* The documentation of `/sign undo` and `/sign redo` has been clarified to indicate that they affect only the sign text, not the dyes or wax applied to the sign.

### Fixed

* Failure and error when editing [hanging signs and wall hanging signs](https://web.archive.org/web/20230615204117/https://minecraft.fandom.com/wiki/Sign#Hanging_signs) from Minecraft 1.20 when using `compatibility.edit-validation: Extra` (#34)

  ![`/se paste` with SignEdit for Bukkit v1.13.9](https://i.imgur.com/1qNBnID.png)
* Workaround for a Spigot 1.20 bug where using the stable API to have a player open a sign does not make the sign editable ([SPIGOT-7391](https://hub.spigotmc.org/jira/browse/SPIGOT-7391))
* Error `java.lang.IllegalArgumentException: Invalid page number (1)` when using `compatibility.sign-ui: EditableBook` and saving a blank book

### Under the Hood

* Tacked on some adapters to make the plugin compatible with Bukkit 1.20 [`org.bukkit.block.sign.SignSide`](https://web.archive.org/web/20230622054725/https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/block/sign/SignSide.html)
* Increased the right-click event handling priority to the highest level to avoid interfering with other plugins that take over right-click events on signs (#36)
* Permission processing moved from `SignCommand.permitted(Player, String)` to `InteractionCommand.isPermitted()`
* `SignText.signChanged()` renamed to `SignText.signTextChanged()` as part of clarification that only text changes count in `/sign undo` and `/sign redo`
* Started pulling [`org.bukkit.entity.Player`](https://web.archive.org/web/20230622081000/https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/Player.html) out of constructor injections, as various classes are being used in wider scopes.
* Gradle 9.0 compatibility:
  * Replaced Gradle 8.2 deprecations with recommended replacements
  * Updated Gradle to version 8.2.1
  * Added JDK toolchain resolver plugin to download Java 1.8
* Skip ProGuard for development builds
* Eliminated the dependency on `commons-lang:commons-lang`

## v1.13.8 (2023-04-02)

### Fixed

* The German (`de`) locale pluralized 0 incorrectly in the `history_have` string.

  Credit to [@phpwutz](https://github.com/phpwutz)
* A minor typo in the file that gets deployed to `./plugins/SignEdit/locales/README.txt`
* A race condition that assumed that a block is still a sign when using `compatibility.edit-validation: Extra` (#33)

### Changed

* If the file modification time of `./plugins/SignEdit/config.yml` is not changed, the configuration will no longer be reloaded. (#29)

  This means that merely changing the file's ownership or permissions will not trigger the configuration reload anymore.

### Under the Hood

* Updated copyright year 2023
* Restructured `SignSubcommand` inheritance to make more sense
* Replaced constructor injection of `ChatComms` with a factory to avoid nesting assisted dependency injections
* Extracted line selection parsing to new class `LineSelectorParser` to improve separation of concerns
* Gradle has been updated from version 7.3.1 to version 8.1-rc-2.

## v1.13.7 (2021-12-18)

### Fixed

* Show the "Operation failed: Sign no longer exists!" error message if another plugin removes the targeted sign during a sign change validation. (#27)

  Previously, this was an unhandled error.

## v1.13.6 (2021-12-08)

### Added

* Limited support for Bukkit 1.8.3 through 1.12.2 in addition to the existing full support for Bukkit 1.13 through 1.18

### Fixed

* Unhandled Java `Error`s are now caught and more gracefully logged.

### Changed

* If Bukkit is too old (versions 1.8.3 through 1.11.2), fall back to [the configured default locale](https://github.com/Deltik/SignEdit/blob/v1.13.6/README.md#locale-en) instead of erroring out and preventing the plugin from being used.
* If a Java `Error` or any other unhandled `Exception` is encountered while validating a `/sign ui` edit, the sign edit is now cancelled.  Previously, the edit would not be cancelled.

## v1.13.5 (2021-12-08)

### Fixed

* `/sign ui` compatibility with Bukkit 1.18

### Under the Hood

* Now that Bukkit 1.18 has introduced a stable API for opening the sign editor ([`org.bukkit.entity.Player#openSign(org.bukkit.block.Sign)`](https://web.archive.org/web/20211208030659/https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/Player.html#openSign(org.bukkit.block.Sign))), we reap the following benefits:
  * **No more dependency on CraftBukkit** – `/sign ui` previously tried various reflection workarounds to invoke the sign editor and always assumed that the server was running CraftBukkit without too many changes.  SignEdit for Bukkit `>= 1.12.7, < 1.13.4` settled on a minimally intrusive unstable API call that worked for CraftBukkit 1.8 through CraftBukkit 1.17.1, but CraftBukkit stopped mapping method names starting in CraftBukkit 1.18, which broke `/sign ui`.
  * **`/sign ui` won't break in future versions of Bukkit** – As SignEdit for Bukkit `= 1.13.5` now prefers to use the stable Bukkit API, `/sign ui` will keep working on future major versions of Bukkit.
* For improved compatibility, the old reflection-based sign editor open call―now named `net.deltik.mc.signedit.interactions.UiSignEditInteraction.openSignEditorWithReflection()`―will try to find the unstable API ("NMS") `EntityHuman` method to open the sign editor based on the `TileEntitySign` passed into that method rather than by the name `EntityHuman.openSign()`.  [CraftBukkit no longer maps the NMS method names.](https://web.archive.org/web/20211206215150/https://www.spigotmc.org/threads/spigot-bungeecord-1-17-1-17-1.510208/#post-4184317)
  This fix is moot for Bukkit 1.18 and newer, but it may be less likely to break other CraftBukkit variants.
* The plugin will now build on Java 17.  CI will continue to build on Java 1.8 for compatibility with Bukkit 1.13 servers.
* Gradle will now use Java 1.8 to build the plugin.
  Redistributors or future versions of this plugin intending to build on newer versions of Java can change the Java toolchain language version in the `build.gradle` file to override this behavior.
* CI now uses a newer Java 17 image for build-time dependencies.
  The resulting build artifact will remain compatible with Java 1.8.

## v1.13.4 (2021-11-16)

### Added

* `/sign help` now tab-completes page numbers when there is more than one help page.

### Changed

* A player's pending sign interaction will only be cancelled if they perform a sign edit (`SignChangeEvent`) outside of this plugin rather than when they place a block.

  This reverts the change from SignEdit for Bukkit `= 1.13.3`.
  This new approach should be less prone to bugs.

### Fixed

* When the plugin is configured with `compatibility.edit-validation: Extra`, `/sign ui` should not skip the extra checks. (#26)

### Under the Hood

* All mentions of `deltik.org` have been changed to `deltik.net`.
* The plugin package was renamed from `org.deltik.mc.signedit` to `net.deltik.mc.signedit`.
* Most code inspection warnings have been fixed.
* Gradle has been updated from version 7.0.2 to version 7.3.
* Version numbers are now generated automatically from Git tags.

## v1.13.3 (2021-09-24)

### Changed

* A player's pending sign interaction will be cancelled if they place a block. (#25)

### Fixed

* `java.lang.NullPointerException` logged in console if `/sign ui` is attempted on a sign placed after the command was pending (#25)

## v1.13.2 (2021-09-20)

### Added

* Native translation for the Hong Kong Cantonese (`zh-HK`) locale

  Credit to eason329 (#24)

### Fixed

* Translation improvements for the Simplified Chinese (`zh`) and Traditional Chinese (`zh-TW`) locales

  Credit to eason329 (#24)

## v1.13.1 (2021-07-11)

### Fixed

* `org.bukkit.plugin.IllegalPluginAccessException` warning while unloading the plugin ([reported in Spigot forum thread](https://www.spigotmc.org/threads/signedit-for-bukkit.275199/page-3#post-4212562)) prevents unload-time configuration file validation

## v1.13.0 (2021-07-10)

### Added

* Changes to the configuration file (`plugins/SignEdit/config.yml`) are now detected and reloaded at runtime, so a CraftBukkit server restart is no longer needed to make the new settings take effect.
* New locale strings:
  * `usage_page_heading` – Styling for the heading of the redesigned `/sign help`
  * `usage_page_info` – Contents of the heading of `/sign help`
  * `usage_page_numbering` – Shows the page number of the `/sign help` page

### Changed

* The `<lines>` argument of `/sign clear [<lines>]` is now optional.
  If `<lines>` is not provided, all lines on the targeted sign will be blanked out.
* Reworked `/sign help` so that it will only display `/sign` subcommands to which the player has access. (#23)
* Tab completion now will only suggest for `/sign` subcommands to which the player has access.
* Removed locale strings:
  * `usage_section` – This string is no longer used due to the rework of `/sign help`.

### Fixed

* If another plugin modified the lines in a `SignChangeEvent`, SignEdit for Bukkit may ignore the updated lines and apply the staged lines, bypassing other plugins' changes. (#22)

  This defect is a regression from SignEdit for Bukkit `= 1.12.2`.
  The cause of this defect was an incorrect assumption that the sign lines array passed in the `SignChangeEvent` would be mutated in place by the Bukkit event handler and other listening plugins.
* `/sign undo` and `/sign redo` missing from `/sign help` (#23)
* Internal error while tab-completing and not looking at a sign

### Under the Hood

* Added documentation of the arguments in the locale strings
* Simplified dependency injection for `SignSubcommand` subclasses
* Event listeners split up between `CoreSignEditListener`, which is always used, and `BookUiSignEditListener`, which is only used when the editable book alternative sign editor GUI is enabled
* The event listeners pertaining to the editable book alternative sign editor GUI are no longer registered if it is disabled.
* `SignEditInteractionManager` extracted out of `SignEditListener`
* Suppressed `JavaReflectionMemberAccess` warning in `org.deltik.mc.signedit.commands.SignCommand.getTargetBlockOfPlayer()` because we develop against Bukkit 1.13 before they added `org.bukkit.entity.LivingEntity.getTargetBlockExact()`

## v1.12.8 (2021-06-21)

> ## Major Bug Notice (#22)
>
> ### Affected Versions
>
> * v1.12.2
> * v1.12.3
> * v1.12.4
> * v1.12.5
> * v1.12.6
> * v1.12.7
> * v1.12.8
>
> ### Upgrade Paths
>
> * v1.12.x → v1.13.4
>
> ### Description
>
> On affected versions, Standard or Extra sign edit permission validation (`compatibility.edit-validation: Standard` or `compatibility.edit-validation: Extra`) may ignore the result of changed sign lines by other plugins.
>
> This bug bypasses sign content/text restrictions imposed by other plugins for anyone using `/sign` subcommands that change the text of a sign.
>
> All users who run affected versions and have enabled sign edit permission validation (this is the default behavior) should upgrade to v1.13.4 immediately to prevent exploitation of this bug.
>
> ### Workaround
>
> If you are unable to upgrade to v1.13.4, you should downgrade to v1.12.1 or disable SignEdit for Bukkit.

### Fixed

* `/sign ui` compatibility with Paper 1.17 (#21)

## v1.12.7 (2021-06-13)

> ## Major Bug Notice (#22)
>
> ### Affected Versions
>
> * v1.12.2
> * v1.12.3
> * v1.12.4
> * v1.12.5
> * v1.12.6
> * v1.12.7
> * v1.12.8
>
> ### Upgrade Paths
>
> * v1.12.x → v1.13.4
>
> ### Description
>
> On affected versions, Standard or Extra sign edit permission validation (`compatibility.edit-validation: Standard` or `compatibility.edit-validation: Extra`) may ignore the result of changed sign lines by other plugins.
>
> This bug bypasses sign content/text restrictions imposed by other plugins for anyone using `/sign` subcommands that change the text of a sign.
>
> All users who run affected versions and have enabled sign edit permission validation (this is the default behavior) should upgrade to v1.13.4 immediately to prevent exploitation of this bug.
>
> ### Workaround
>
> If you are unable to upgrade to v1.13.4, you should downgrade to v1.12.1 or disable SignEdit for Bukkit.

### Fixed

* `/sign ui` compatibility with Minecraft 1.17

### Under the Hood

* `UiSignEditInteraction` now uses much less reflection, which should hopefully make it more resistant to breaking changes in the unstable Minecraft server API.
* `MinecraftReflector` has been renamed to `CraftBukkitReflector` and no longer tries to access `net.minecraft.…` packages by name.
  This plugin now performs reflection name lookups against CraftBukkit, which is hopefully more stable.
* Java 1.8 (bytecode version 52.0) remains the minimum version for broader compatibility, even though Minecraft 1.17 itself requires Java 16.
* Gradle has been updated from version 6.5.1 to version 7.0.2.

## v1.12.6 (2020-11-17)

> ## Major Bug Notice (#22)
>
> ### Affected Versions
>
> * v1.12.2
> * v1.12.3
> * v1.12.4
> * v1.12.5
> * v1.12.6
> * v1.12.7
> * v1.12.8
>
> ### Upgrade Paths
>
> * v1.12.x → v1.13.4
>
> ### Description
>
> On affected versions, Standard or Extra sign edit permission validation (`compatibility.edit-validation: Standard` or `compatibility.edit-validation: Extra`) may ignore the result of changed sign lines by other plugins.
>
> This bug bypasses sign content/text restrictions imposed by other plugins for anyone using `/sign` subcommands that change the text of a sign.
>
> All users who run affected versions and have enabled sign edit permission validation (this is the default behavior) should upgrade to v1.13.4 immediately to prevent exploitation of this bug.
>
> ### Workaround
>
> If you are unable to upgrade to v1.13.4, you should downgrade to v1.12.1 or disable SignEdit for Bukkit.

### Fixed

* Regression from version `= 1.12.5` caused `Uncaught error: java.lang.NullPointerException` when trying to target air

## v1.12.5 (2020-11-17)

> ## Major Bug Notice (#22)
>
> ### Affected Versions
>
> * v1.12.2
> * v1.12.3
> * v1.12.4
> * v1.12.5
> * v1.12.6
> * v1.12.7
> * v1.12.8
>
> ### Upgrade Paths
>
> * v1.12.x → v1.13.4
>
> ### Description
>
> On affected versions, Standard or Extra sign edit permission validation (`compatibility.edit-validation: Standard` or `compatibility.edit-validation: Extra`) may ignore the result of changed sign lines by other plugins.
>
> This bug bypasses sign content/text restrictions imposed by other plugins for anyone using `/sign` subcommands that change the text of a sign.
>
> All users who run affected versions and have enabled sign edit permission validation (this is the default behavior) should upgrade to v1.13.4 immediately to prevent exploitation of this bug.
>
> ### Workaround
>
> If you are unable to upgrade to v1.13.4, you should downgrade to v1.12.1 or disable SignEdit for Bukkit.

### Changed

* Increased sign targeting distance from 10 to 20

### Fixed

* Improved sign targeting precision for Minecraft 1.13.2+ (#20)

## v1.12.4 (2020-09-06)

> ## Major Bug Notice (#22)
>
> ### Affected Versions
>
> * v1.12.2
> * v1.12.3
> * v1.12.4
> * v1.12.5
> * v1.12.6
> * v1.12.7
> * v1.12.8
>
> ### Upgrade Paths
>
> * v1.12.x → v1.13.4
>
> ### Description
>
> On affected versions, Standard or Extra sign edit permission validation (`compatibility.edit-validation: Standard` or `compatibility.edit-validation: Extra`) may ignore the result of changed sign lines by other plugins.
>
> This bug bypasses sign content/text restrictions imposed by other plugins for anyone using `/sign` subcommands that change the text of a sign.
>
> All users who run affected versions and have enabled sign edit permission validation (this is the default behavior) should upgrade to v1.13.4 immediately to prevent exploitation of this bug.
>
> ### Workaround
>
> If you are unable to upgrade to v1.13.4, you should downgrade to v1.12.1 or disable SignEdit for Bukkit.

### Added

* Tab completion of existing sign text when using `/sign set` and looking at a sign (#19)
  ![`/sign set 1-4 <tab>`](https://i.imgur.com/sZCRS5E.png)

### Changed

* Undoing and redoing sign text will now only change the sign text.
  Previously, the sign orientation and dye color would also be modified to what was remembered in the history stack.
* Undoing and redoing sign text is now possible even if the sign is replaced with one of a new material (e.g. a spruce sign in place of an oak sign) as long as the new sign is in the same position.
* `/sign redo` now puts the current sign text into the history, so undoing the redo will now restore the latest sign text instead of the staged (remembered at the time of the first undo) text in the history.

### Fixed

* When undoing and redoing, the "before" section of the comparison now uses the current text/state of the sign rather than the remembered state from when the edit was made, which may be outdated.
* `/sign undo` after using `/sign ui` would show "Sign did not change".

### Under the Hood

* Migrated tests from JUnit 4 to JUnit 5
* `SignText.verifyBlockPlaced()` no longer tries to update the block's state to check if the block is placed.  It is now called `SignText.reloadTargetSign()` and uses the non-invasive `BlockState.isPlaced()` method.

## v1.12.3 (2020-08-19)

> ## Major Bug Notice (#22)
>
> ### Affected Versions
>
> * v1.12.2
> * v1.12.3
> * v1.12.4
> * v1.12.5
> * v1.12.6
> * v1.12.7
> * v1.12.8
>
> ### Upgrade Paths
>
> * v1.12.x → v1.13.4
>
> ### Description
>
> On affected versions, Standard or Extra sign edit permission validation (`compatibility.edit-validation: Standard` or `compatibility.edit-validation: Extra`) may ignore the result of changed sign lines by other plugins.
> 
> This bug bypasses sign content/text restrictions imposed by other plugins for anyone using `/sign` subcommands that change the text of a sign.
> 
> All users who run affected versions and have enabled sign edit permission validation (this is the default behavior) should upgrade to v1.13.4 immediately to prevent exploitation of this bug.
>
> ### Workaround
>
> If you are unable to upgrade to v1.13.4, you should downgrade to v1.12.1 or disable SignEdit for Bukkit.

### Added

* Tab completion for multi-line selection (e.g. `/sign 1,2,…` and `/sign cut 3-4,1-…`)
* If another plugin changes the sign text after using this plugin to edit a sign, a warning will be displayed.
  The example below shows the warning when another plugin strips out formatting codes from the execution of `/sign set 3 &b&lSignEdit`:
  !["Modified by another plugin" warning](https://i.imgur.com/nv2Utnl.png)
* New locale strings:
  * `modified_by_another_plugin` – Warning to the player that another plugin changed their applied sign text
  * `section_decorator` – Theme to apply to the text shown at the end of `before_section` and `after_section`

### Changed

* `/sign` subcommands are now case-insensitive.  For example, `/sign ClEaR` is now the same as `/sign clear`.
  Previously, capitalized subcommands would not be understood as valid subcommands.
* `/sign ui` with the native sign editor no longer modifies the real sign on the server to present human-editable formatting codes.
  Only the player who invoked the command will see the reformatted sign.
* If a different plugin undoes the sign edit but does not cancel the corresponding `SignChangeEvent`, this plugin will now tell the player "Sign edit forbidden by policy or other plugin" instead of "Sign did not change".
* Changed locale strings:
  * `before_section` now takes one argument (`{0}`) for optional text that may be shown at the end of the same line as the section.
  * `after_section` now takes one argument (`{0}`) for optional text that may be shown at the end of the same line as the section.

### Fixed

* `/sign cut` only put empty lines into the clipboard (#17)
* `/sign ui` with the native sign editor incorrectly puts the de-formatted sign text into the history (regression from `= 1.12.2`)
* `/sign ui` on Minecraft 1.16.2+ loses all formatting codes when opening the default (native) sign editor GUI.
* `/sign ui` with the native sign editor would not show the actual "after" sign lines if another plugin modified the `SignChangeEvent` originating from the Minecraft client.
* Line selection validation for `/sign cut` and `/sign copy`
* Line selection validation should not accept empty delimiters at the end (e.g. `/sign set 1,3,,,,`)

## v1.12.2 (2020-08-10)

> ## Major Bug Notice (#22)
>
> ### Affected Versions
>
> * v1.12.2
> * v1.12.3
> * v1.12.4
> * v1.12.5
> * v1.12.6
> * v1.12.7
> * v1.12.8
>
> ### Upgrade Paths
>
> * v1.12.x → v1.13.4
>
> ### Description
>
> On affected versions, Standard or Extra sign edit permission validation (`compatibility.edit-validation: Standard` or `compatibility.edit-validation: Extra`) may ignore the result of changed sign lines by other plugins.
>
> This bug bypasses sign content/text restrictions imposed by other plugins for anyone using `/sign` subcommands that change the text of a sign.
>
> All users who run affected versions and have enabled sign edit permission validation (this is the default behavior) should upgrade to v1.13.4 immediately to prevent exploitation of this bug.
>
> ### Workaround
>
> If you are unable to upgrade to v1.13.4, you should downgrade to v1.12.1 or disable SignEdit for Bukkit.
> 
> ## Major Bug Notice (#17)
>
> ### Affected Versions
>
> * v1.12.2
>
> ### Upgrade Paths
>
> * v1.12.2 → v1.12.3
>
> ### Description
>
> On affected versions, `/sign cut` will only put empty lines into the clipboard.
>
> All affected users are encouraged to upgrade to v1.12.3 or newer to resolve this bug.
>
> ### Workaround
>
> Run `/sign copy` followed by `/sign 1-4`, which is functionally equivalent to the intended behavior of `/sign cut`.

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
  ![SignEdit for Bukkit: Support for web colors](https://i.imgur.com/0HxGwpz.png)
* [Alternative sign editor GUI for Minecraft 1.16+](https://git.io/SignEdit-README#minecraft-116-sign-editor-gui) implemented as a [writable book](https://minecraft.gamepedia.com/Book_and_Quill) to work around [a Minecraft 1.16 sign editor regression](https://web.archive.org/web/20200901000000/https://bugs.mojang.com/browse/MC-192263?focusedCommentId=755369&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#comment-755369) that [Mojang refused to fix](https://web.archive.org/web/20200714051840/https://bugs.mojang.com/browse/MC-192263?focusedCommentId=759126&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#comment-759126):
  ![Sign editor GUI implemented as a writable book](https://i.imgur.com/KAetJWB.png)

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
  ![Locale_en](https://i.imgur.com/sCFaPVG.png)
* Native translation for German (`de`) locale
  ![Locale_de](https://i.imgur.com/j89WCaZ.png)
* Machine translation for Simplified Chinese (`zh`) locale
  ![Locale_zh](https://i.imgur.com/XlGkhyv.png)

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
