package com.mortrag.ut.wasabi.serialization;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.mortrag.ut.wasabi.util.Debug;

public class MapObjectsSerializer extends Serializer<MapObjects> {

	@Override
	public void write(Kryo kryo, Output output, MapObjects mapObjects) {
		// write length
		int len = mapObjects.getCount();
		output.writeInt(len);
		Debug.print("Writing MapObjects with " + len + " object(s).");
		
		// write objects
		for (int i = 0; i < len; i++) {
			// We write which class it is (TextureMapObject? WasabiTextureMapObject? ...other?) and
			// write the object itself. This is inefficient but makes it we can use the type-
			// specific serializer when reading.
			//
			// Here's some old code that I'm proud of but isn't quite what we want:
			//
			// Class<? extends MapObject> c = m.getClass();
			// Serializer<? extends MapObject> ser = kryo.getSerializer(c);
			// 
			// because even if that does write using the correct serializer, it is then a mystery
			// for the de-serializer when reading: which class is the next object? Which serializer
			// do I use to read it? We avoid all this by writing the class name, and the object.
			kryo.writeClassAndObject(output, mapObjects.get(i));
		}
	}

	@Override
	public MapObjects read(Kryo kryo, Input input, Class<MapObjects> type) {
		// read length
		int len = input.readInt();
		Debug.print("Reading MapObjects with " + len + " object(s).");
		
		// read objects
		MapObjects ret = new MapObjects();
		for (int i = 0; i < len; i++) {
			MapObject obj = (MapObject) kryo.readClassAndObject(input);
			ret.add(obj);
		}		
		return ret;
	}

}
