This plugin's text (colors and languages) displayed to players can be
customized.

To customize the text, create your own locale files in the ./overrides/ folder.

The ./originals/ folder contains a read-only copy of the locale files used by
this plugin.  You should copy the locale files you want to customize from
./originals/ into ./overrides/ to get a full working copy of the text.  Then,
you can edit your copy or copies in ./overrides/ to set your own colors and
translations.

This file and everything in ./originals/ is managed by ${pluginName}. Any
changes will be overwritten when the the plugin is reloaded, so only make your
changes in the ./overrides/ folder.
