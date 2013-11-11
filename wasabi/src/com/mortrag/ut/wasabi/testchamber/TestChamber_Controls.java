package com.mortrag.ut.wasabi.testchamber;

import com.badlogic.gdx.Input.Keys;
import com.mortrag.ut.wasabi.input.Controls;

public class TestChamber_Controls extends Controls {

	@Override
	public void addKeyCommands() {
		addControlText("Keyboard:");
		addKeyCommand(Keys.W, KeyModifier.ANY, TestChamber_Commands.JUMP, "W");
		addKeyCommand(Keys.A, KeyModifier.ANY, TestChamber_Commands.MOVE_LEFT, "A");
		addKeyCommand(Keys.D, KeyModifier.ANY, TestChamber_Commands.MOVE_RIGHT, "D");
		addControlText("(also)");
		addKeyCommand(Keys.UP, KeyModifier.ANY, TestChamber_Commands.JUMP, "up");
		addKeyCommand(Keys.LEFT, KeyModifier.ANY, TestChamber_Commands.MOVE_LEFT, "left");
		addKeyCommand(Keys.RIGHT, KeyModifier.ANY, TestChamber_Commands.MOVE_RIGHT, "right");
		addKeyCommand(Keys.SPACE, KeyModifier.ANY, TestChamber_Commands.JUMP, "space bar");
		addControlText("");
		addKeyCommand(Keys.B, KeyModifier.ANY, TestChamber_Commands.BOUNDING_BOXES, "B");
		addKeyCommand(Keys.F, KeyModifier.ANY, TestChamber_Commands.FRAME_BY_FRAME, "F");
		addKeyCommand(Keys.N, KeyModifier.ANY, TestChamber_Commands.NEXT_FRAME, "N");
		addKeyCommand(Keys.P, KeyModifier.ANY, TestChamber_Commands.PAUSE, "P");
		addKeyCommand(Keys.X, KeyModifier.ANY, TestChamber_Commands.BACK_TO_EDITOR, "X");

	}

	@Override
	public void addScrollCommands() {
		// None for now
	}

	@Override
	public void addMouseCommands() {
		// None for now
	}

}
