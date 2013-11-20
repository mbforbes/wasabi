package com.mortrag.ut.wasabi.map;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.mortrag.ut.wasabi.map.Initializable.ObjectInitializable;
import com.mortrag.ut.wasabi.util.Constants;

public class WasabiTextureMapObject extends TextureMapObject implements ObjectInitializable {
	private float width;
	private float height;
	private BoundingBox boundingBox;
	private String regionName;
	
	// for kryo
	private transient boolean initialized = false;
	
	public WasabiTextureMapObject(TextureRegion textureRegion, String regionName, float x, float y,
			float width, float height) {
		super(textureRegion);		
		setX(x);
		setY(y);
		setWidth(width);
		setHeight(height);
		setRegionName(regionName);
		boundingBox = new BoundingBox(new Vector3(x, y, 0.0f),
				new Vector3(x + width, y + height, Constants.BB_ZMAX));
		initialized = true;
	}
	
	// begin KRYO-specific
	// --------------------------------------------------------------------------------------------
	/**
	 * No-arg constructor for kryo
	 */
	public WasabiTextureMapObject() {
		super(); // this already happens--just doing it explicitly as a reminder.
	}
	
	/**
	 * Call after kryo'd.
	 * @param atlas
	 */
	public void initialize(TextureAtlas atlas) {
		if (initialized) {
			return;
		}
		
		// Check kind of redundant because of initialized check, but just keeping pattern.
		if (this.getTextureRegion() == null) {
			this.setTextureRegion(atlas.findRegion(regionName));
		}
		
		initialized = true;
	}
	
	// end KRYO-specific
	// --------------------------------------------------------------------------------------------	
	
	public float getWidth() {
		return this.width;
	}
	
	public float getHeight() {
		return this.height;
	}
	
	public String getRegionName() {
		return this.regionName;
	}
	
	/**
	 * Note: have to be careful, as must recompute every time one of the x,y or dimensions of this
	 * sprite changes... we're storing it now (and you're retrieving it) under the assumption that
	 * it is not changing because it's part of the map.
	 * @return the original, non-updated BoundingBox
	 */
	public BoundingBox getBoundingBox() {
		return boundingBox;
	}
	
	public void setWidth(float width) {
		this.width = width;
	}
	
	public void setHeight(float height) {
		this.height = height;
	}
	
	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}
}
