package com.mortrag.ut.wasabi.test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.maps.Map;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.mortrag.ut.wasabi.map.WasabiTextureMapObject;
import com.mortrag.ut.wasabi.util.Debug;

public class KryoTest extends Game implements ApplicationListener {
	
//	/**
//	 * This class doesn't have a no-arg constructor so can't be de/serialized.
//	 * @author max
//	 *
//	 */
//	public static class ComplexObj {
//		private String s;
//		private int i;
//		private boolean b;
//		
//		public ComplexObj(String s, int i, Boolean b) {
//			this.s = s;
//			this.i = i;
//			this.b = b;
//		}
//	}
//	
//	/**
//	 * This class is serializable, but only if we don't de/serialize ComplexObj c.
//	 * @author max
//	 *
//	 */
//	public static class SerializeMe {
//		private ComplexObj c;
//		private int i;
//		
//		public SerializeMe(ComplexObj c, int i) {
//			this.c = c;
//			this.i = i;
//		}
//		
//		/**
//		 * No-arg constuctor for Kryo.
//		 */
//		public SerializeMe() {
//		}
//	}
//	
//	public static class SerializeMeChild extends SerializeMe {
//		public SerializeMeChild(ComplexObj c, int i) {
//			super(c, i);
//		}
//		
//		/**
//		 * No-arg constuctor for Kryo.
//		 */
//		public SerializeMeChild() {
//		}		
//	}
	
	/**
	 * Demonstrates that removeField(...) doesn't work.
	 */
	public static void testRemove() {
		// create our obj to serialize
//		SerializeMe s = new SerializeMeChild(new ComplexObj("foo", 5, true), 10);
		FileHandle fh = new FileHandle("../wasabi-android/assets/wasabi-atlas.atlas");
		TextureAtlas atlas = new TextureAtlas(fh);
		Array<AtlasRegion> regions = atlas.getRegions();
		AtlasRegion r = regions.first();
		Map m = new Map();
		MapLayer layer = new MapLayer();
		TextureMapObject obj = new TextureMapObject();
		obj.setTextureRegion(r);
		layer.getObjects().add(obj);
		m.getLayers().add(layer);
		
		// setup custom serializer that doesn't try to serialize the complex obj
		Kryo kryo = new Kryo();
		FieldSerializer<TextureMapObject> ser = new FieldSerializer<TextureMapObject>(kryo, TextureMapObject.class);
		ser.removeField("textureRegion");
//		kryo.register(WasabiTextureMapObject.class, ser);
		kryo.register(TextureMapObject.class, ser);
		
		// Serialize to file
		String filename = "test1.file";
		Output output;
		try {
			output = new Output(new FileOutputStream(filename));
			kryo.writeObject(output, layer);
			output.close();				
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		// De-serialize from file
		Input input;
		try {
			input = new Input(new FileInputStream(filename));
			MapLayer l2 = kryo.readObject(input, MapLayer.class);
			input.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}				
	
		// If we made it here, ohmygosh.
		Debug.print("Success!");	
	}
	
	@Override
	public void create() {
		testRemove();
		
	}
}
