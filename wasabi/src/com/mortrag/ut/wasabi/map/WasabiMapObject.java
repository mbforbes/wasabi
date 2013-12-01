package com.mortrag.ut.wasabi.map;

public interface WasabiMapObject {
	public float getX();
	public float getY();
	public float getWidth();
	public float getHeight();
	public String getName();
	
//	/**
//	 * Called to change underlying object; this happens when the user scrolls/ changes objects.
//	 * We do this because of the "temporarily add cur object to layer" approach.
//	 * 
//	 * @param guts may be WasabiAnimation or WasabiTextureMapObject or ...
//	 */
//	public void changeObject(Object guts);
}
