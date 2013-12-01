package com.mortrag.ut.wasabi;

import java.awt.Toolkit;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.tools.imagepacker.TexturePacker2;
import com.badlogic.gdx.tools.imagepacker.TexturePacker2.Settings;
import com.mortrag.ut.wasabi.test.KryoTest;

public class Main {
	public static void main(String[] args) {
		// Texture packing ... shouldn't do this on the real game, but OK now
		// for debug.
		Settings packSettings = new Settings();
		// Cooper default; run in Dropbox
		String textureInputDir = "../0_graphics/"; 
		if (args.length > 0) {
			// Max defualt; run in specified directory
			textureInputDir = args[0];
		}
		packSettings.stripWhitespaceX = true;
		packSettings.stripWhitespaceY = true;
		TexturePacker2.processIfModified(packSettings, textureInputDir, "../wasabi-android/assets",
				"wasabi-atlas.atlas");

		// Set up LWJGL config
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "Wasabi DEV";
		cfg.useGL20 = true;
		cfg.vSyncEnabled = true;

		// Window size
		if (args.length > 1 && args[1].equals("fullscreen")) {
			// Fullscreen (Max default)
			cfg.width = Toolkit.getDefaultToolkit().getScreenSize().width;
			cfg.height = Toolkit.getDefaultToolkit().getScreenSize().height;			
		} else {
			// Smaller window (Cooper default)
			cfg.width = 1024;
			cfg.height = 768;			
		}
		
		new LwjglApplication(new WasabiGame(), cfg);
//		new LwjglApplication(new KryoTest(), cfg);
		
	}
}
