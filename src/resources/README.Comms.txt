This plugin's text (colors and languages) displayed to players can be
customized.

To customize the text, create your own locale files in the ./overrides/ folder.

Note that as the locale files are standard Java ResourceBundles, any script,
region, and variants in the language tag should be delimited by underscores
("_") instead of the standard hyphen delimiter ("-").  For example, "zh-TW"
should be in a file called "Comms_zh_TW.properties".

The ./originals/ folder contains a read-only copy of the locale files used by
this plugin.  You should copy the locale files you want to customize from
./originals/ into ./overrides/ to get a full working copy of the text.  Then,
you can edit your copy or copies in ./overrides/ to set your own colors and
translations.

This file and everything in ./originals/ is managed by ${pluginName}. Any
changes will be overwritten when the plugin is reloaded, so only make your
changes in the ./overrides/ folder.
