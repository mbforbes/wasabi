package com.mortrag.ut.wasabi.util;

public class Constants {
	public static final String NL = System.getProperty("line.separator"); // newline character
	public static final float BB_ZMAX = 0.01f;
	public static final float HERO_CAM_OFFSET_Y = 170f;
	
	/**
	 * File Data. Concerns properties of real files on the disk, like prefixes (s_*.pn, o_*.png) and
	 * extensions (.map). 
	 * @author max
	 */
	public static final class FD {
		public static final String REGION_PREFIX = "REGION_PREFIX"; // -> SPRITE_PREFIX, OBJ_PREFIX, ...
		public static final String SPRITE_PREFIX = "s_";
		public static final String OBJ_PREFIX = "o_";
	}
	
	/**
	 * Map Properties
	 * @author max
	 */
	public static final class MP {
		// map
		public static final String LEVEL_WIDTH = "LEVEL_WIDTH"; // -> int
		public static final String LEVEL_HEIGHT = "LEVEL_HEIGHT"; // -> int
		public static final String SPAWN_POINT = "SPAWN_POINT"; // -> Vector2 
		
		// layer
		public static final String LAYER_TYPE = "LAYER_TYPE"; // -> LayerType
		public static enum LayerType {
			BG,
			COLLISION_FG,
			FG;			
		}
//		public static final String COLLIDABLE = "COLLIDABLE"; // -> boolean
//		public static final String LAYER_BG = "LAYER_FG"; // -> boolean
//		public static final String LAYER_FG = "LAYER_BG"; // -> boolean
	}
	
}
