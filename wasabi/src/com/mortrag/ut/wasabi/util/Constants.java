package com.mortrag.ut.wasabi.util;

public class Constants {
	// general purpose
	public static final String NL = System.getProperty("line.separator"); // newline character
	public static final float BB_ZMAX = 0.01f; // bounding boxes are 3D; this is for Z coord
	public static final float HERO_CAM_OFFSET_Y = 170f; // how much the camera is moved down
	
	// level editor
	public static final class LE {
		public static final int GRID_SPACING = 50;
		
		public static final float ZOOM_DELTA = 0.05f;
		public static final float ZOOM_LIMIT = 0.1f;
		public static final float CAM_MOVE_SPEED = 10.0f;
		public static final float OBJECT_MOVE_SPEED = 1.0f; // for pixel-perfect nudging
		public static final float MAIN_VIEWPORT_WIDTH_FRAC = 0.75f;
		
		// how large the layer box should be
		public static final float LAYER_BOX_HEIGHT = 150f;
		// how much margin around the rendered layer images to give
		public static final float LAYER_BOX_MARGIN = 20f;
		 // how much space on left to give layer buttons
		public static final float LAYER_BUTTONS_AREA_WIDTH = 100f;
		// how much to draw around the layer box
		public static final int LAYER_BOX_BORDER = 2;
	}

	
	
	/**
	 * File Data. Concerns properties of real files on the disk, like prefixes (s_*.pn, o_*.png) and
	 * extensions (.map). 
	 * @author max
	 */
	public static final class FD {
//		public static final String REGION_PREFIX = "REGION_PREFIX"; // -> SPRITE_PREFIX, OBJ_PREFIX, ...
//		public static final String SPRITE_PREFIX = "s_";
		public static final String OBJ_PREFIX = "o_";
//		public static final String CHAR_PREFIX = "char_";
	}
	
	// TODO we probably need more state here to fullow allow characters to be reconstructured...
	// maybe a lot...
	public static final String[] CHAR_ANIM_PREFIXES = {
		"s_wasIdle",
		"s_keiMarch"
	};
	
	// Types of primitives. Different than types of WasabiMapObjects; these are primitive assets,
	// for the level editor, whereas the WasabiMapOjects are actual game objects, built from these.
	public static final String MAP_OBJECT_PRIMITIVE_TYPE = "MAP_OBJECT_PRIMITIVE_TYPE "; // -> MapObjectPrimitiveType  
	public static enum MapObjectPrimitiveType {
		OBJECTS ("OBJECTS"),
		CHARACTERS ("CHARACTERS");
		
		private String desc;
		private MapObjectPrimitiveType(String desc) {
			this.desc = desc;
		}
		public String toString() {
			return desc;
		}
	}	
	
	/**
	 * Map Properties
	 * @author max
	 */
	public static final class MP {
		// map
		public static final String LEVEL_WIDTH = "LEVEL_WIDTH"; // -> int
		public static final String LEVEL_HEIGHT = "LEVEL_HEIGHT"; // -> int
		
		// object
		
		// texture objects
		public static final String OBJ_TEXTURE_NAME= "OBJ_TEXTURE_NAME"; // -> file name in atlas
		
		// animated objects
		public static final String ANIM_FILE_PREFIX= "ANIM_FILE_PREFIX"; // -> file prefix in atlas
		public static final String ANIM_FRAME_DURATION = "ANIM_FRAME_DURATION"; // -> float
		public static final String ANIM_TYPE = "ANIM_TYPE"; // -> int
		
		public static final String OBJECT_TYPE = "OBJECT_TYPE"; // -> ObjectType
		/**
		 * Types of WasabiMapObjects. Note that all can have properties like collisions applied to
		 * them. In fact conceptually, collision is really just the existence of bounding boxes, and
		 * belongs in the physics/collision subsystem. Creating bounding boxes that match objects'
		 * sizes is just convenient.
		 * @author max
		 *
		 */
		public static enum ObjectType {
			NORMAL ("Normal"), // background, foreground, etc. Just objects on the map. Texture or animated.
			LAYERED ("LayeredGradient"), // should be drawn several times to layer (statically), like gradient
			STATIC ("BackgroundStatic"), // should be drawn 'statically' without moving
			PARALLAX ("Parallax"), // drawing depends on character movement
			EVENT ("Event"); // Event region
			
			private final String desc;
			private ObjectType(String desc) {
				this.desc = desc;
			}
			public String toString() {
				return desc;
			}
		}
		
		// sub-object
		public static final String CHAR_OBJECT_TYPE = "CHAR_OBJECT_TYPE"; // -> CharObjectType
		public static enum CharObjectType {
			SPAWN_POINT ("Spawn point", "s_wasIdle"),
			ARMOR_ENEMY ("Armor enemy", "s_keiMarch");
			
			// TODO see if we need filePrefix. Using CHARACTERS above to separate primitives from
			// true WasabiMapObjects.
			private final String desc, filePrefix;
			private CharObjectType(String desc, String filePrefix) {
				this.desc = desc;
				this.filePrefix = filePrefix;
			}
			public String toString() {
				return desc;
			}
			public String getFilePrefix() {
				return filePrefix;
			}
		}
	}
	
}
