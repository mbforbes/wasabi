package com.mortrag.ut.wasabi;

import java.awt.Toolkit;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.tools.imagepacker.TexturePacker2;
//import com.badlogic.gdx.tools.imagepacker.TexturePacker2.Settings;

public class Main {
	public static void main(String[] args) {
		// Texture packing ... shouldn't do this on the real game, but OK now
		// for debug.
		//Settings packSettings = new Settings();
		String textureInputDir = "../0_graphics/";
		//boolean skip
		if (args.length > 0) {
			textureInputDir = args[0];
		}
		TexturePacker2.processIfModified(textureInputDir, "../wasabi-android/assets",
				"wasabi-atlas.atlas");

		// Set up LWJGL config
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "Wasabi DEV";
		cfg.useGL20 = true;
		cfg.vSyncEnabled = true;

		// Fullscreen
		cfg.width = Toolkit.getDefaultToolkit().getScreenSize().width;
		cfg.height = Toolkit.getDefaultToolkit().getScreenSize().height;

		// Smaller window
		//cfg.width = 1024;
		//cfg.height = 768;

		new LwjglApplication(new WasabiGame(), cfg);
		// For camera testing
		//new LwjglApplication(new OrthographicCameraController(), cfg);
	}
}
