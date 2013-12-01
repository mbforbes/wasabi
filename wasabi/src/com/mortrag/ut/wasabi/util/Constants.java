package com.mortrag.ut.wasabi.util;

public class Constants {
	public static final String NL = System.getProperty("line.separator"); // newline character
	public static final float BB_ZMAX = 0.01f;
	public static final float HERO_CAM_OFFSET_Y = 170f;
	
	// These two arrays are linked; they correspond to the frame prefixes and object names for
	// the AnimatedMapObjects that mark map positions for the hero and enemies.
	public static final String[] CHARACTERS = {"s_wasIdle", "s_keiMarch"}; // TODO load from file.
	public static final String[] CHAR_ONAMES = {ON.SPAWN_POINT, ON.ARMOR_ENEMY};// TODO load from file.
	
	/**
	 * File Data. Concerns properties of real files on the disk, like prefixes (s_*.pn, o_*.png) and
	 * extensions (.map). 
	 * @author max
	 */
	public static final class FD {
		public static final String REGION_PREFIX = "REGION_PREFIX"; // -> SPRITE_PREFIX, OBJ_PREFIX, ...
		public static final String SPRITE_PREFIX = "s_";
		public static final String OBJ_PREFIX = "o_";
		public static final String CHAR_PREFIX = "char_";
	}
	
	
	/**
	 * Object Names (the String name field of a MapObject)
	 * @author max
	 *
	 */
	public static final class ON {
		public static final String SPAWN_POINT = "SPAWN_POINT";
		public static final String ARMOR_ENEMY = "ARMOR_ENEMY";
		
	}
	
	/**
	 * Map Properties
	 * @author max
	 */
	public static final class MP {
		// map
		public static final String LEVEL_WIDTH = "LEVEL_WIDTH"; // -> int
		public static final String LEVEL_HEIGHT = "LEVEL_HEIGHT"; // -> int
		
		// layer
		public static final String LAYER_TYPE = "LAYER_TYPE"; // -> LayerType
		public static enum LayerType {
			BG ("Background"),
			COLLISION_FG ("Foreground, collidable"),
			FG ("Foreground, non-collidable"),
			CHARACTERS ("Spawn positions");
			
			private final String desc;
			private LayerType(String desc) {
				this.desc = desc;
			}
			public String toString() {
				return desc;
			}
		}
	}
	
}
