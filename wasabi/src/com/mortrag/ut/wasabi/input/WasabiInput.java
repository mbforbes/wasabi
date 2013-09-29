package com.mortrag.ut.wasabi.input;

import java.util.Iterator;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.Array;
import com.mortrag.ut.wasabi.input.Controls.KeyModifier;
import com.mortrag.ut.wasabi.input.Controls.MouseOptions;

public class WasabiInput implements InputProcessor {

	private Controls controls; // shared
	private Array<Command> commandList; // shared
	private int activeKeyModifier;
	private MouseState mouseState; // shared
	
	public class MouseState {
		public int x;
		public int y;
	}
	
	public WasabiInput() {
		activeKeyModifier = KeyModifier.NONE;
		mouseState = new MouseState();
	}
	
	public MouseState setControls(Controls controls, Array<Command> commandList) {
		this.controls = controls;
		this.commandList = commandList;
		return mouseState;
	}
	
	public void clearPress() {
		Iterator<Command> cit = commandList.iterator();
		while (cit.hasNext()) {
			Command c = cit.next();
			if (c.getType() == Command.Type.PRESS) {
				cit.remove();
			}
		}
	}
	
	/**
	 * Good to call in show() to clean up any left-over state.
	 */
	public void clearAll() {
		this.commandList.clear();
	}
	
	private boolean isShift(int keycode) {
		return keycode == Keys.SHIFT_LEFT || keycode == Keys.SHIFT_RIGHT;
	}
	
	private boolean isControl(int keycode) {
		// NOTE(max): Currently maps system key (command on Mac, Windows on Windows) to this as
		// well for Macs.
		return keycode == Keys.CONTROL_LEFT || keycode == Keys.CONTROL_RIGHT || keycode == Keys.SYM;
	}
	
	private void adjustModifiersKeyDown(int keycode) {
		// NOTE(max): Keep in sync with Controls.KeyModifier.
		switch (activeKeyModifier) {
		case KeyModifier.NONE:
			if (isShift(keycode)) {
				activeKeyModifier = KeyModifier.SHIFT;
			} else if (isControl(keycode)) {
				activeKeyModifier = KeyModifier.CONTROL;
			}
			break;
		case KeyModifier.SHIFT:
			if (isControl(keycode)) {
				activeKeyModifier = KeyModifier.SHIFT_CONTROL;
			}
			break;
		case KeyModifier.CONTROL:
			if (isShift(keycode)){
				activeKeyModifier = KeyModifier.SHIFT_CONTROL;
			}
			break;
		default:
			// SHIFT_CONTROL don't need to do anything. ANY/NUM_MODIFIERS make no logical sense.
			break;
		}
	}
	
	private void adjustModifiersKeyUp(int keycode) {
		// NOTE(max): Keep in sync with Controls.KeyModifier.
		switch (activeKeyModifier) {
		case KeyModifier.SHIFT_CONTROL:
			if (isShift(keycode)) {
				activeKeyModifier = KeyModifier.CONTROL;
			} else if (isControl(keycode)) {
				activeKeyModifier = KeyModifier.SHIFT;
			}
			break;
		case KeyModifier.SHIFT:
			if (isShift(keycode)) {
				activeKeyModifier = KeyModifier.NONE;
			}
			break;
		case KeyModifier.CONTROL:
			if (isControl(keycode)){
				activeKeyModifier = KeyModifier.NONE;
			}
			break;
		default:
			// NONE don't need to do anything. ANY/NUM_MODIFIERS make no logical sense.
			break;
		}
	}	
	
	@Override
	public boolean keyDown(int keycode) {
		adjustModifiersKeyDown(keycode);
		Command command = controls.getKeyCommand(keycode, activeKeyModifier);
		if (command == null) {
			return false;
		}
		commandList.add(command);
		return true;
	}

	@Override
	public boolean keyUp(int keycode) {
		adjustModifiersKeyUp(keycode);
		Command command = controls.getKeyCommand(keycode, activeKeyModifier);
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
		Command c = controls.getMouseCommand(MouseOptions.CLICK_DOWN);
		if (c == null) {
			return false;
		}
		commandList.add(c);
		return true;
	}

	@Override
	// Note(max): This isn't actually smart enough to detect when you press down with both fingers
	// and let up with only one-- the touchUp will still have a "both fingers". Put simply, the
	// 'button' in touchUp always matches what was called in touchDown.
	// 
	// ...
	// 
	// TOOOUUUCCCCCCHHHHDDDOOOOOOWWWWWNNNNNNNNNNNNNNNNNN *spikes ball*
	//
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		Command c = controls.getMouseCommand(MouseOptions.CLICK_UP);
		if (c == null) {
			return false;
		}
		commandList.add(c);
		return true;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		Command c = controls.getMouseCommand(MouseOptions.DRAGGED);
		if (c == null) {
			return false;
		}
		commandList.add(c);
		return true;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		Command c = controls.getMouseCommand(MouseOptions.MOVED);
		if (c == null) {
			return false;
		}
		commandList.add(c);
		
		// track movement
		mouseState.x = screenX;
		mouseState.y = screenY;
		
		return true;
	}

	@Override
	public boolean scrolled(int amount) {
		Command command = controls.getScrollCommand(amount);
		if (command == null) {
			return false;
		}
		commandList.add(command);
		return true;
	}
}
