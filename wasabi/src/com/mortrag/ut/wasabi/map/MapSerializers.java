package com.mortrag.ut.wasabi.map;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.mortrag.ut.wasabi.leveleditor.LevelEditor_MapLayer;
import com.mortrag.ut.wasabi.util.Debug;

public class MapSerializers {
	
	private static boolean setup = false;
	
	public static void setup(Kryo kryo) {
		if (!setup) {
			Debug.print("Setting up.");
			
			// leveleditor_maplayer just uses maplayer serializer
			FieldSerializer<LevelEditor_MapLayer> layerSer =
					new FieldSerializer<LevelEditor_MapLayer>(kryo, MapLayer.class);
			kryo.addDefaultSerializer(LevelEditor_MapLayer.class, layerSer);
			
			// WasabiTextureMapObject uses TextureMapObject serializer, but both need to have the
			// TextureRegion field removed
			FieldSerializer<TextureMapObject> objSer =
					new FieldSerializer<TextureMapObject>(kryo, TextureMapObject.class);
			// TODO(max) Curspot: this doesn't work.
			objSer.removeField("textureRegion");
			kryo.register(WasabiTextureMapObject.class, objSer);
//			kryo.register(TextureMapObject.class, objSer);
			
			setup = true;
		}
	}
}
