package leveleditor;

import com.badlogic.gdx.Input.Keys;
import com.mortrag.ut.wasabi.input.Controls;

public class LevelEditor_Controls extends Controls {

	@Override
	public void addKeyCommands() {
		addControlText("Keyboard:");
		addKeyCommand(Keys.W, KeyModifier.ANY, LevelEditor_Commands.CAMERA_UP, "W");
		addKeyCommand(Keys.S, KeyModifier.ANY, LevelEditor_Commands.CAMERA_DOWN, "S");
		addKeyCommand(Keys.A, KeyModifier.ANY, LevelEditor_Commands.CAMERA_LEFT, "A");
		addKeyCommand(Keys.D, KeyModifier.ANY, LevelEditor_Commands.CAMERA_RIGHT, "D");
		addControlText("");
		addKeyCommand(Keys.Q, KeyModifier.ANY, LevelEditor_Commands.CAMERA_ZOOM_OUT, "Q");
		addKeyCommand(Keys.E, KeyModifier.ANY, LevelEditor_Commands.CAMERA_ZOOM_IN, "E");
		addControlText("");
		addKeyCommand(Keys.UP, KeyModifier.ANY, LevelEditor_Commands.MOVE_UP, "up");
		addKeyCommand(Keys.DOWN, KeyModifier.ANY, LevelEditor_Commands.MOVE_DOWN, "down");
		addKeyCommand(Keys.LEFT, KeyModifier.ANY, LevelEditor_Commands.MOVE_LEFT, "left");
		addKeyCommand(Keys.RIGHT, KeyModifier.ANY, LevelEditor_Commands.MOVE_RIGHT, "right");
		addControlText("");
		addKeyCommand(Keys.N, KeyModifier.ANY, LevelEditor_Commands.NEXT_SPRITE, "N");
		addKeyCommand(Keys.P, KeyModifier.SHIFT, LevelEditor_Commands.PREVIOUS_SPRITE, "SHIFT + P");
		addKeyCommand(Keys.SPACE, KeyModifier.ANY, LevelEditor_Commands.PLACE_SPRITE, "space bar");
		addKeyCommand(Keys.G, KeyModifier.ANY, LevelEditor_Commands.TOGGLE_GRID, "G");
		addKeyCommand(Keys.T, KeyModifier.ANY, LevelEditor_Commands.TOGGLE_SNAP_TO_GRID, "T");
		addKeyCommand(Keys.P, KeyModifier.NONE, LevelEditor_Commands.PAUSE, "P");
		addKeyCommand(Keys.X, KeyModifier.ANY, LevelEditor_Commands.TEST_MAP, "X");
	}

	@Override
	public void addScrollCommands() {
		addControlText("");
		addControlText("Mouse:");
		addScrollCommand(ScrollOptions.UP, LevelEditor_Commands.NEXT_SPRITE, "scroll up");
		addScrollCommand(ScrollOptions.DOWN, LevelEditor_Commands.PREVIOUS_SPRITE, "scroll down");		
	}
	
	@Override
	public void addMouseCommands() {
		addMouseCommand(MouseOptions.MOVED, LevelEditor_Commands.CURSOR_MOVED);
		addControlText(" - move: adjust sprite position");
		addMouseCommand(MouseOptions.CLICK_DOWN, LevelEditor_Commands.PRESS_DOWN);
		addControlText(" - click: place current sprite");
	}
}
