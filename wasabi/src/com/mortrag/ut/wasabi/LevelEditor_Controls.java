package com.mortrag.ut.wasabi;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.mortrag.ut.wasabi.ScreenKeys;
import com.mortrag.ut.wasabi.util.Constants;
import com.mortrag.ut.wasabi.util.Debug;

public class LevelEditor_Controls implements Controls {
	private String controlsList;
	private Map<ScreenKeys.Keys, Commands> commandsMap;
	private Array<String> commandsDisplay;
	
	// Possible future feature: read from config file? (Worth it?)
	public LevelEditor_Controls() {
		commandsMap = new HashMap<ScreenKeys.Keys, Commands>();
		commandsDisplay = new Array<String>();
		
		add(ScreenKeys.Keys.W, LevelEditor_Commands.CAMERA_UP);
		add(ScreenKeys.Keys.S, LevelEditor_Commands.CAMERA_DOWN);
		add(ScreenKeys.Keys.A, LevelEditor_Commands.CAMERA_LEFT);
		add(ScreenKeys.Keys.D, LevelEditor_Commands.CAMERA_RIGHT);
		addBreak();
		add(ScreenKeys.Keys.Q, LevelEditor_Commands.CAMERA_ZOOM_OUT);
		add(ScreenKeys.Keys.E, LevelEditor_Commands.CAMERA_ZOOM_IN);
		addBreak();
		add(ScreenKeys.Keys.UP, LevelEditor_Commands.MOVE_UP);
		add(ScreenKeys.Keys.DOWN, LevelEditor_Commands.MOVE_DOWN);
		add(ScreenKeys.Keys.LEFT, LevelEditor_Commands.MOVE_LEFT);
		add(ScreenKeys.Keys.RIGHT, LevelEditor_Commands.MOVE_RIGHT);
		addBreak();
		add(ScreenKeys.Keys.N, LevelEditor_Commands.NEXT_SPRITE);
		add(ScreenKeys.Keys.SPACE, LevelEditor_Commands.PLACE_SPRITE);
		addBreak();
		add(ScreenKeys.Keys.P, LevelEditor_Commands.PAUSE);
		
		if(Debug.DEBUG) {
			checkControls();
		}
	}
	
	/**
	 * Adds entry to map and list. Should only be called @ creatoin.
	 */
	private void add(ScreenKeys.Keys key, Commands command) {
		commandsMap.put(key, command);
		commandsDisplay.add(key + " : " + command);
	}		
	
	/**
	 * Puts a line break in the commands display. Should only be called @ creation.
	 */
	private void addBreak() {
		commandsDisplay.add(Constants.NL);
	}
	
	private void checkControls() {
		for (Commands c : LevelEditor_Commands.values()) {
			if (!commandsMap.containsValue(c)) {
				throw new GdxRuntimeException("Need key mapping for " + c.toString());
			}
		}
	}

	
	/**
	 * @return Commands c if this key is mapped to a command, or null if not.
	 */
	@Override
	public Commands getCommand(int k) {
		ScreenKeys.Keys key = ScreenKeys.getMap().get(k);
		if (key == null) {
			return null;
		}
		return commandsMap.get(key);		
	}

	@Override
	public String getControlsList() {
		if (controlsList == null) {
			controlsList = commandsDisplay.toString(Constants.NL);
		}
		return controlsList;
	}
}
