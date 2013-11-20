package com.mortrag.ut.wasabi.leveleditor;

import com.badlogic.gdx.Input.Keys;
import com.mortrag.ut.wasabi.input.Controls;

public class LevelEditor_Controls extends Controls {

	@Override
	public void addKeyCommands() {
		addControlText("Camera:");
		addKeyCommand(Keys.W, KeyModifier.ANY, LevelEditor_Commands.CAMERA_UP, "W");
		addKeyCommand(Keys.S, KeyModifier.NONE, LevelEditor_Commands.CAMERA_DOWN, "S");
		addKeyCommand(Keys.A, KeyModifier.ANY, LevelEditor_Commands.CAMERA_LEFT, "A");
		addKeyCommand(Keys.D, KeyModifier.ANY, LevelEditor_Commands.CAMERA_RIGHT, "D");
		addKeyCommand(Keys.Q, KeyModifier.ANY, LevelEditor_Commands.CAMERA_ZOOM_OUT_HOLD, "Q");
		addKeyCommand(Keys.E, KeyModifier.ANY, LevelEditor_Commands.CAMERA_ZOOM_IN_HOLD, "E");
		
		addControlText("");
		addControlText("Sprite movement / placement:");
		addKeyCommand(Keys.UP, KeyModifier.ANY, LevelEditor_Commands.MOVE_UP, "up");
		addKeyCommand(Keys.DOWN, KeyModifier.ANY, LevelEditor_Commands.MOVE_DOWN, "down");
		addKeyCommand(Keys.LEFT, KeyModifier.ANY, LevelEditor_Commands.MOVE_LEFT, "left");
		addKeyCommand(Keys.RIGHT, KeyModifier.ANY, LevelEditor_Commands.MOVE_RIGHT, "right");
		addKeyCommand(Keys.T, KeyModifier.ANY, LevelEditor_Commands.TOGGLE_SNAP_TO_GRID, "T");
		addKeyCommand(Keys.N, KeyModifier.ANY, LevelEditor_Commands.NEXT_SPRITE, "N");
		addKeyCommand(Keys.P, KeyModifier.SHIFT, LevelEditor_Commands.PREVIOUS_SPRITE, "SHIFT + P");
		addKeyCommand(Keys.SPACE, KeyModifier.ANY, LevelEditor_Commands.PLACE_SPRITE, "space bar");
		
		addControlText("");
		addControlText("Layer navigation:");
		addKeyCommand(Keys.EQUALS, KeyModifier.ANY, LevelEditor_Commands.NEXT_LAYER, "= or +");
		addKeyCommand(Keys.MINUS, KeyModifier.ANY, LevelEditor_Commands.PREV_LAYER, "- or _");
		
		addControlText("");
		addControlText("Display:");		
		addKeyCommand(Keys.G, KeyModifier.ANY, LevelEditor_Commands.TOGGLE_GRID, "G");
		addKeyCommand(Keys.B, KeyModifier.ANY, LevelEditor_Commands.BOUNDING_BOXES, "B");
		
		addControlText("");
		addControlText("Save/Test/Export:");			
		addKeyCommand(Keys.X, KeyModifier.ANY, LevelEditor_Commands.TEST_MAP, "X");
		addKeyCommand(Keys.S, KeyModifier.CONTROL, LevelEditor_Commands.SAVE_MAP, "S");
		addKeyCommand(Keys.O, KeyModifier.CONTROL, LevelEditor_Commands.LOAD_MAP, "O");
		
		addControlText("");
		addControlText("Other:");
		addKeyCommand(Keys.P, KeyModifier.NONE, LevelEditor_Commands.PAUSE, "P");
	}

	@Override
	public void addScrollCommands() {
		addControlText("");
		addControlText("Mouse:");
		addScrollCommand(ScrollOptions.UP, LevelEditor_Commands.CAMERA_ZOOM_OUT_PRESS, "scroll up");
		addScrollCommand(ScrollOptions.DOWN, LevelEditor_Commands.CAMERA_ZOOM_IN_PRESS, "scroll down");		
	}
	
	@Override
	public void addMouseCommands() {
		addMouseCommand(MouseOptions.MOVED, LevelEditor_Commands.CURSOR_MOVED);
		addControlText(" - move: adjust sprite position");
		addMouseCommand(MouseOptions.CLICK_DOWN, LevelEditor_Commands.PRESS_DOWN);
		addControlText(" - click: place current sprite");
	}
}
