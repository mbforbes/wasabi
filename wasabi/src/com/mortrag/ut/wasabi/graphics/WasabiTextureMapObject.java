package com.mortrag.ut.wasabi.graphics;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.objects.TextureMapObject;

public class WasabiTextureMapObject extends TextureMapObject {
	public float width;
	public float height;
	
	public WasabiTextureMapObject(TextureRegion textureRegion, float x, float y,
			float width, float height) {
		super(textureRegion);
		setX(x);
		setY(y);
		setWidth(width);
		setHeight(height);
	}	
	
	public float getWidth() {
		return this.width;
	}
	
	public float getHeight() {
		return this.height;
	}
	
	public void setWidth(float width) {
		this.width = width;
	}
	
	public void setHeight(float height) {
		this.height = height;
	}
	

}
