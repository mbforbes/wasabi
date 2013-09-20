#Wasabi

## Features

### 09/20/2013
 * rewrote input management again; supports control and/or shift modifiers for keys and scrolling.
 * added toogle grid lines (G)
 * added snap-to-grid functionality (T)
 * changed keyboard based active sprite movement to "press" rather than "press-and-hold"; also changed to 1px adjustments for pixel-perfect nudging
 * implemented mouse functionality (move to move current sprite, click to place it)
	 * note that the mouse also behavs according to the snap-to-grid setting (T). So, zoom in and turn off snap-to-grid for pixel perfect adjustments with your mouse!
 * biggest remaining issue: sprite whitespace...

### 09/19/2013
 * rewrote input management to support key and action descriptions, PRESS/HOLD keys, additional layers of (possibly useless) abstraction (check/display key mappings, ...)
 * fixed drawing order of sprites (currently selected sprite now drawn on top)
 * implemented pausing (P)
 * implemented controls listing on pause screen
 * implemented resizing (IT'S SO FLEXIBLE)
 * texture packer now only runs if textures have been modified: much faster load time (no cleanup though)
 * implemented FPS display
 * fixing some of the bugs I introduced while making shitty versions of the above


### 09/18/2013
 * `level editor' base with main map and mini map
 * WSAD moves camera; QE zoom
 * arrow keys move sprite; n cycles sprite
 * space places sprite
 * loads textures in passed directory or `../0_graphics` if none provided, packs in atlas, cleans up when done
