package com.mortrag.ut.wasabi;

import com.badlogic.gdx.Input;

public interface Controls {
	
	/**
	 * 
	 * @param k Should be a key as indexed in gdx.Input.Keys. Would have preferred to
	 * use that type, but their input processing methods don't return these; they
	 * just return ints. Awesome. 
	 *  
	 * @return
	 */
	public Commands getCommand(int key);
	public String getControlsList();
}
