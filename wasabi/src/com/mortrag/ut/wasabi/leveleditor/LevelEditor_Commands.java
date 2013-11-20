package com.mortrag.ut.wasabi.leveleditor;

import com.mortrag.ut.wasabi.input.Command;

public enum LevelEditor_Commands implements Command {
	// Camera
	CAMERA_RIGHT("move camera right", Type.HOLD),
	CAMERA_LEFT("move camera left", Type.HOLD),
	CAMERA_UP("move camera up", Type.HOLD),
	CAMERA_DOWN("move camera down", Type.HOLD),
	CAMERA_ZOOM_IN_PRESS("zoom camera in", Type.PRESS),
	CAMERA_ZOOM_OUT_PRESS("zoom camera out", Type.PRESS),
	CAMERA_ZOOM_IN_HOLD("zoom camera in", Type.HOLD),
	CAMERA_ZOOM_OUT_HOLD("zoom camera out", Type.HOLD),
		
	// Moving and placing
	MOVE_RIGHT("move current sprite right", Type.PRESS),
	MOVE_LEFT("move current sprite left", Type.PRESS),
	MOVE_UP("move current sprite up", Type.PRESS),
	MOVE_DOWN("move current sprite down", Type.PRESS),
	CURSOR_MOVED("RAW: cursor moved command", Type.PRESS),
	PREVIOUS_SPRITE("selects previous sprite in list", Type.PRESS),
	NEXT_SPRITE("selects next sprite in list", Type.PRESS),
	PLACE_SPRITE("places current sprite at current position", Type.PRESS),
	PRESS_DOWN("RAW: cursor pressed command", Type.PRESS),
	
	// Options / do stuff
	BOUNDING_BOXES("toggle drawing bounding boxes on/off", Type.PRESS),	
	TOGGLE_GRID("toggle grid", Type.PRESS),
	TOGGLE_SNAP_TO_GRID("toggle snap-to-grid", Type.PRESS),
	PAUSE("pause level editing, showing controls", Type.PRESS),
	TEST_MAP("loads the current map into the test chamber", Type.PRESS),
	SAVE_MAP("save the current map", Type.PRESS),
	LOAD_MAP("load a saved map", Type.PRESS),
	
	// Layer navigation
	NEXT_LAYER("switch to next layer", Type.PRESS),
	PREV_LAYER("switch to previous layer", Type.PRESS);
	
	private final String desc;
	private final Type type;
	private LevelEditor_Commands(String desc, Type type) {
		this.desc = desc;
		this.type = type;
	}
	public String toString() {
		return desc;
	}
	public Type getType() {
		return type;
	}

}
