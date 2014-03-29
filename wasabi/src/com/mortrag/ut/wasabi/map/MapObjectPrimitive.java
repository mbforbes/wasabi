package com.mortrag.ut.wasabi.map;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.mortrag.ut.wasabi.graphics.Common;
import com.mortrag.ut.wasabi.util.Debug;
import com.mortrag.ut.wasabi.util.Constants.MP.ObjectType;

public interface MapObjectPrimitive {
	
	public void renderEditor(SpriteBatch batch, float x, float y);
	public WasabiMapObject getWasabiMapObject(ObjectType type, float x, float y);
	public float getWidth();
	public float getHeight();
	
	// IMPLEMENTING CLASSES
	
	/**
	 * For objects that are textures as primitives.
	 * @author max
	 *
	 */
	public static class TextureMapObjectPrimitive implements MapObjectPrimitive{
		private TextureRegion textureRegion;
		private String textureName;
		private float w, h;
		
		private static Vector2 cachedWHCalculator = new Vector2(); 
		
		public TextureMapObjectPrimitive(TextureRegion textureRegion, String textureName) {
			this.textureRegion = textureRegion;
			this.textureName = textureName;
			Common.getTextureWH(textureRegion, cachedWHCalculator);
			this.w = cachedWHCalculator.x;
			this.h = cachedWHCalculator.y;
		}
		
		public void renderEditor(SpriteBatch batch, float x, float y) {
			batch.draw(this.textureRegion, x, y, w, h);
		}
		
		/**
		 * TODO TBD how to do this.
		 */
		public WasabiMapObject getWasabiMapObject(ObjectType type, float x, float y) {
			if (type == ObjectType.NORMAL) {
				// -> WasabiTextureMapObject
				Common.getTextureWH(textureRegion, cachedWHCalculator);
				return new WasabiTextureMapObject(textureRegion, textureName, x, y,
						cachedWHCalculator.x, cachedWHCalculator.y);
			} else {
				Debug.print("Warning: TextureMapObjectPrimitive -> " + type.toString() +
					" not implemented!");
				return null;
			}
		}
		
	
		public float getWidth() {
			// shouldn't change so just caching
			return w;
		}
		
		public float getHeight() {
			// shouldn't change so just caching
			return h;
		}
		
		// TODO is this the API we want?
		public String getTextureName() {
			return this.textureName;
		}
		
		public TextureRegion getTextureRegion() {
			return this.textureRegion;
		}
	}
	
	/**
	 * For objects that are animated as primitives.
	 * @author max
	 *
	 */
	public static class AnimatedMapObjectPrimitive implements MapObjectPrimitive{
		private WasabiAnimation wasabiAnimation; 
		private static Vector2 cachedWHCalculator = new Vector2(); 
		private float w, h;
		
		public AnimatedMapObjectPrimitive(WasabiAnimation wasabiAnimation) {
			this.wasabiAnimation = wasabiAnimation;
			Common.getTextureWH(wasabiAnimation.getAnimation().getKeyFrame(0.0f), cachedWHCalculator);
			this.w = cachedWHCalculator.x;
			this.h = cachedWHCalculator.y;			
		}
		
		public void renderEditor(SpriteBatch batch, float x, float y) {
			
		}
		
		public WasabiMapObject getWasabiMapObject(ObjectType type, float x, float y) {
			Debug.print("Warning: AnimatedMapObjectPrimitive -> " + type.toString() +
					" not implemented!");
				return null;			
		}
		public String getFilePrefix() {
			return this.wasabiAnimation.getFramePrefix();
		}
		
		public WasabiAnimation getWasabiAnimation() {
			return this.wasabiAnimation;
		}
		
		
		public float getWidth() {
			// shouldn't change so just caching
			return w;
		}
		
		public float getHeight() {
			// shouldn't change so just caching
			return h;
		}		
	}	
}
