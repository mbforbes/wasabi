package com.mortrag.ut.wasabi.input;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.utils.Array;
import com.mortrag.ut.wasabi.util.Constants;

public abstract class Controls {
	
	// ------------------------------------------------------------------------------
	// INNER CLASSES (organized constants)
	// ------------------------------------------------------------------------------
			
	/**
	 * Use these because it's hard to remember what's up and what's down.
	 * @author max
	 */
	public static final class ScrollOptions {
		
		/**
		 * Scrolling up (down on new Mac).
		 */
		public static final int UP = -1;
		
		/**
		 * Scrolling down (up on new Mac).
		 */
		public static final int DOWN = 1;
		
		/**
		 * We're only covering up and down.
		 */		
		public static final int NUM_SCROLL_OPTIONS = 2;
	}
	
	/**
	 * These are the types of mouse events that libGDX InputProcessors handle by default.
	 * @author max
	 */
	public static final class MouseOptions {
		
		/**
		 * When you've clicked down.
		 */
		public static final int CLICK_DOWN = 0;
		
		/**
		 * When you've clicked up. Note that the MouseClicks constant will match that of the 
		 * CLICK_DOWN event.
		 */
		public static final int CLICK_UP = 1;
		
		/**
		 * A click and drag. Haven't used this yet. Probably will have to.
		 */
		public static final int DRAGGED = 2;
		
		/**
		 * Whenever the mouse is moved along the screen. This happens a lot.
		 */
		public static final int MOVED = 3;
		
		/**
		 * How many mouse options/events avove.
		 */
		public static final int NUM_MOUSE_OPTIONS = 4;
	}
	
	/**
	 * Use these instead of trying to remember what the clicks constants are.
	 * @author max
	 */
	public static final class MouseClicks {
		
		/**
		 * Left click, or single-finger click with Mac.
		 */
		public static final int LEFT_CLICK = 0;
		
		/**
		 * Right click, or double-finger click with Mac.
		 */
		public static final int RIGHT_CLICK = 1;
		
		/**
		 * Just left and right, baby.
		 */
		public static final int NUM_MOUSE_CLICKS = 2;
	}
	
	/**
	 * These are the "modifiers" we support for changing what key presses mean. SHIFT+key and 
	 * CONTROL+key seem common enough, so added those as well as NONE, both (SHIFT+CONTROL+key), and
	 * ANY for when it doesn't matter. ANY is always checked first in the code.
	 * @author max
	 */
	public static final class KeyModifier {
		/**
		 * Key only fired when pressed without other modifiers.
		 */
		public static final int NONE = 0;
		
		/**
		 * Key only fired when pressed with shift (right or left).
		 */
		public static final int SHIFT = 1;
		
		/**
		 * Key only fired when pressed with control (command on Mac).
		 */
		public static final int CONTROL = 2;
		
		/**
		 * Key only fired when pressed with control (command on Mac).
		 */
		public static final int SHIFT_CONTROL = 3;		
			
		/**
		 * Key fired when pressed; ignores modifiers.
		 */
		public static final int ANY = 4;
		
		/**
		 * Used only as a number to calculate the number of above modifiers.
		 */
		public static final int NUM_MODIFIERS = 5;
	}
	
	// ------------------------------------------------------------------------------
	// MEMBERS
	// ------------------------------------------------------------------------------
	
	/**
	 * A map of key->command for all KeyModifiers (NUM_MODIFIERS).
	 */
	private Map<Integer, Command>[] keyCommandMaps;
	
	/**
	 * A map of scroll->command for all ScrollOptions (NUM_SCROLL_OPTIONS), and
	 * a map mousePress->command for all MouseOptions (NUM_MOUSE_OPTIONS).
	 */
	private Command[] scrollCommandMap, mouseCommandMap;
	
	/**
	 * Whether we need to re-compute the commands String.
	 */
	private boolean dirty = true;
	
	/**
	 * Human-visible list of controls.
	 */
	private Array<String> controlsList;
	
	/**
	 * We don't want to have to recompute this so often, so we cache it. It's computed from
	 * controlsList.
	 */
	private String controlsStringCache;

	// ------------------------------------------------------------------------------
	// CONSTRUCTORS
	// ------------------------------------------------------------------------------	
	
	@SuppressWarnings("unchecked")
	public Controls() {
		// Key command maps
		keyCommandMaps = (Map<Integer, Command>[]) new Map[KeyModifier.NUM_MODIFIERS];
		for (int mod = 0; mod < KeyModifier.NUM_MODIFIERS; mod++) {
			keyCommandMaps[mod] = new HashMap<Integer, Command>();
		}
		
		// Scroll command map
		scrollCommandMap = new Command[ScrollOptions.NUM_SCROLL_OPTIONS];
		for (int i = 0; i < ScrollOptions.NUM_SCROLL_OPTIONS; i++) {
			scrollCommandMap[i] = null;
		}
		
		// Mouse X,Y are set via shared state between the WasabiInput and the Screen. However,
		// commands are specific to each screen. (Specific might be an over statement; they are of
		// that screen's "Screen_Commands" class because the Screen only expects Commands of its
		// type. Thus, "general" Commands don't work.
		mouseCommandMap = new Command[MouseOptions.NUM_MOUSE_OPTIONS];
		for (int i = 0; i < MouseOptions.NUM_MOUSE_OPTIONS; i++) {
			mouseCommandMap[i] = null;
		}
		
		// Create description array
		controlsList = new Array<String>();
		
		// Forces call to these in subclass.
		addKeyCommands();
		addScrollCommands();
		addMouseCommands();
	}
	
	// ------------------------------------------------------------------------------
	// ABSTRACT METHODS (to be limplemented)
	// ------------------------------------------------------------------------------
	public abstract void addKeyCommands();
	public abstract void addScrollCommands();
	public abstract void addMouseCommands();
	
	// ------------------------------------------------------------------------------
	// PROTECTED METHODS (call these in other controls implementations)
	// ------------------------------------------------------------------------------	
	
	/**
	 * Add a key command. Take care that adding a KeyModifier.ANY command will be fired in lieu of
	 * the more specific modifier match registered for that key (if any).
	 * 
	 * @param keyCode From gdx.Input.Keys
	 * @param keyModifier From KeyModifier
	 * @param c From the current screen's Commands
	 * @param desc Describes the key mapping just added; displayed to user.
	 */
	protected final void addKeyCommand(int keyCode, int keyModifier, Command c, String desc) {
		keyCommandMaps[keyModifier].put(keyCode, c);
		addControlText(quickDesc(desc, c));
	}
	
	/**
	 * Add a scroll command.
	 * @param scrollOption from ScrollOptions
	 * @param c  From the current screen's commands.
	 * @param desc Describes the scroll mapping just added; displayed to user.
	 */
	protected final void addScrollCommand(int scrollOption, Command c, String desc) {
		// map amt: -1 --> 0, 1 --> 1.
		scrollCommandMap[(scrollOption + 2) / 2] = c;
		addControlText(quickDesc(desc, c));
	}
	
	/**
	 * Add a mouse command. These do not have descriptions because these are "raw" commands, and the
	 * actual behavior depends on the view (where the user has clicked), which is decoupled from
	 * this input module.
	 * 
	 * Instead, add a control text description describing to the user how to use the mouse with
	 * addControlText(...).
	 * @param  mouseOption from MouseOptions
	 * @param c  From the current screen's commands.
	 * 
	 */
	protected final void addMouseCommand(int mouseOption, Command c) {
		mouseCommandMap[mouseOption] = c;
	}	
	
	/**
	 * For adding info to the controls list.
	 * @param desc The description to add.
	 */
	protected final void addControlText(String desc) {
		dirty = true;
		controlsList.add(desc);
	}
	
	protected final String quickDesc(String inputDesc, Command c) {
		return " - " + inputDesc + ": " + c;
	}
	
	// ------------------------------------------------------------------------------
	// PUBLIC METHODS (for accessing externally what was set)
	// ------------------------------------------------------------------------------		
	
	/**
	 * Gets command associated with key press, or null if none. Always checks the KeyModifier.ANY
	 * array first, and if none is found, then checks the specific combination that was given. 
	 * 
	 * @param keyCode A key code as indexed in gdx.Input.Keys
	 * 
	 * @param keyModifier One of the constants in KeyModifier (except NUM_MODIFIERS).
	 *  
	 * @return Command c if this key is mapped to a command, or null if not.
	 */
	public final Command getKeyCommand(int keyCode, int keyModifier) {
		// check KeyModifier.ANY first; if no hit, return keyModifer's mapping (may not exist).
		Command anyCommand = keyCommandMaps[KeyModifier.ANY].get(keyCode);
		return anyCommand != null ? anyCommand : keyCommandMaps[keyModifier].get(keyCode);
	}
	
	/**
	 * Gets command associated with scrolling.
	 * 
	 * @param scrollOption: from ScrollOptionss
	 *                    
	 * @return Command c if this is mapped to a command, or null if not
	 */
	public final Command getScrollCommand(int scrollOption) {
		// map scrollOption: -1 --> 0, 1 --> 1.
		return scrollCommandMap[(scrollOption + 2) / 2]; 
	}
	
	/**
	 * Gets command associated with current mouse action.
	 * @param mouseOption from MouseOptions
	 * @return Command c if this is mapped to a command, or null if not
	 */
	public final Command getMouseCommand(int mouseOption) {
		return mouseCommandMap[mouseOption];
	}
	
	/**
	 * 
	 * @return human readable representation of all controls set.
	 */
	public final String getControlsList() {
		if(dirty) {
			controlsStringCache = controlsList.toString(Constants.NL);
		}
		dirty = false;
		return controlsStringCache;
	}
}
