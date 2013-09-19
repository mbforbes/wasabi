package com.mortrag.ut.wasabi;

public enum LevelEditor_Commands implements Commands {
	CAMERA_RIGHT("move camera right", Type.HOLD),
	CAMERA_LEFT("move camera left", Type.HOLD),
	CAMERA_UP("move camera up", Type.HOLD),
	CAMERA_DOWN("move camera down", Type.HOLD),
	CAMERA_ZOOM_IN("zoom camera in", Type.HOLD),
	CAMERA_ZOOM_OUT("zoom camera out", Type.HOLD),
	
	MOVE_RIGHT("move current sprite right", Type.HOLD),
	MOVE_LEFT("move current sprite left", Type.HOLD),
	MOVE_UP("move current sprite up", Type.HOLD),
	MOVE_DOWN("move current sprite down", Type.HOLD),
	
	PAUSE("pause level editing, showing controls", Type.PRESS),
	NEXT_SPRITE("selects next sprite in list", Type.PRESS),
	PLACE_SPRITE("places current sprite at current position", Type.PRESS);
	
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
