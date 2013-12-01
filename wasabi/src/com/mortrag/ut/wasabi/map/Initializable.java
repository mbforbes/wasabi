package com.mortrag.ut.wasabi.map;

import java.util.Map;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;

/**
 * For MapLayer's and MapObject's that are saved/loaded through Kryo, they need to be hooked up
 * again to their parent assets (textures). This allows them to be so.
 * 
 * @author max
 */
public interface Initializable {
	
	/**
	 * For MapLayers
	 * @author max
	 */
	public interface LayerInitializable {

		/**
		 * Call to ensure this MapLayer has the textures it needs associated with it. This should
		 * also call initializeObject on all of its MapObjects.
		 * @param atlas passed to the MapObjects
		 * @param regionMap for this MapLayer
		 */
		public void initialize(TextureAtlas atlas, Map<String, Array<AtlasRegion>> regionMap,
				Array<WasabiAnimation> wasabiAnimations);
	}
	
	/**
	 * For MapObjects
	 * @author max
	 */
	public interface ObjectInitializable {
		/**
		 * Call to ensure this MapObject has the texture it needs associated with it.
		 * @param atlas
		 */
		public void initialize(TextureAtlas atlas);
	}
}
