XmasFun
=======

Add some christmas spirit to your server. This plugin affects blocks being sent to the client, causing them to be oh so Christmasy.


[Screenshot](http://i.imgur.com/CoUGjJc)


Includes 4 different patterns, customizable per-user and with a configurable default for the whole server.


# Usage
### As a user
`/xmas`: Shows the user's current patterns, as well as a list of all available patterns
`/xmas <pattern>`: Switches the user's current pattern to `pattern`. Initiates a refresh of all the chunks around the user, but suggests they log off and back on instead.
Once a user sets a patten, its saved for them, and they will always use that pattern by default when they log in.

### As the console
`/xmas`: Shows the server default pattern, as well as a list of all available patterns. The server default is used only when a user hasn't ever specified a pattern themselves.
`/xmas <pattern>`: Switcher the server default pattern to `pattern`. Does not initiate any refresh, as this change will only affect new users logging in.



