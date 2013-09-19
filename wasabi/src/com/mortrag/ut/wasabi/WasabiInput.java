package com.mortrag.ut.wasabi;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.Array;

public class WasabiInput implements InputProcessor {

	private Controls controls;
	private Array<Commands> commandList;
	
	public WasabiInput() {	
	}
	
	public void setControls(Controls controls, Array<Commands> commandList) {
		this.controls = controls;
		this.commandList = commandList;
	}
	
	public void clearPress() {
		Iterator<Commands> cit = commandList.iterator();
		while (cit.hasNext()) {
			Commands c = cit.next();
			if (c.getType() == Commands.Type.PRESS) {
				cit.remove();
			}
		}
	}
	
	@Override
	public boolean keyDown(int keycode) {
		Commands command = controls.getCommand(keycode);
		if (command == null) {
			return false;
		}
		commandList.add(command);
		return true;
	}

	@Override
	public boolean keyUp(int keycode) {
		Commands command = controls.getCommand(keycode);
		if (command == null) {
			return false;
		}
		commandList.removeValue(command, true);
		return true;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}

}
