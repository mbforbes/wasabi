package com.mortrag.ut.wasabi.testchamber;

import com.mortrag.ut.wasabi.input.Command;

public enum TestChamber_Commands implements Command {
	MOVE_RIGHT("move hero right", Type.HOLD),
	MOVE_LEFT("move hero left", Type.HOLD),
	JUMP("make hero jump", Type.PRESS),
	
	BOUNDING_BOXES("toggle drawing bounding boxes on/off", Type.PRESS),
	FRAME_BY_FRAME("toggle frame-by-frame mode", Type.PRESS),
	NEXT_FRAME("advance frame in frame-by-frame mode", Type.PRESS),
	BACK_TO_EDITOR("go back to the level editor", Type.PRESS),
	PAUSE("pause", Type.PRESS);

	private final String desc;
	private final Type type;
	private TestChamber_Commands(String desc, Type type) {
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
