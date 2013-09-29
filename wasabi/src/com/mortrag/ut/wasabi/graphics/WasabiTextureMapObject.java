package com.mortrag.ut.wasabi.graphics;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

public class WasabiTextureMapObject extends TextureMapObject {
	private float width;
	private float height;
	private BoundingBox boundingBox;
	
	public WasabiTextureMapObject(TextureRegion textureRegion, float x, float y,
			float width, float height) {
		super(textureRegion);
		setX(x);
		setY(y);
		setWidth(width);
		setHeight(height);
		boundingBox = new BoundingBox(new Vector3(x, y, 0.0f),
				new Vector3(x + width, y + height, 0.0f));
	}	
	
	public float getWidth() {
		return this.width;
	}
	
	public float getHeight() {
		return this.height;
	}
	
	/**
	 * Note: have to be careful, as must recompute every time one of the x,y or dimensions of this
	 * sprite changes... we're storing it now (and you're retrieving it) under the assumption that
	 * it is not changing because it's part of the map.
	 * @return
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
}
