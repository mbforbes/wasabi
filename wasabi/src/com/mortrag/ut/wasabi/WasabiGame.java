package com.mortrag.ut.wasabi;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.mortrag.ut.wasabi.input.WasabiInput;
import com.mortrag.ut.wasabi.leveleditor.LevelEditor;

public class WasabiGame extends Game implements ApplicationListener {

	private Map<String, Screen> screenMap;
	
	public Screen getScreen(String screenName) {
		return screenMap.get(screenName);
	}
	
	public void getAndSetScreen(String screenName) {
		Screen nextScreen = getScreen(screenName);
		if (nextScreen != null) {
			setScreen(nextScreen);
		}
	}
	
	public void addScreen(Screen screen, String screenName) {
		screenMap.put(screenName, screen);
	}
	
	public boolean screenLoaded(String screenName) {
		return screenMap.containsKey(screenName);
	}
	
	@Override
	public void create() {
		// Make the screen map
		screenMap = new HashMap<String, Screen>();
		
		// Set up input processing
		InputMultiplexer inputMultiplexer = new InputMultiplexer();
		// mainInput is the input for the screen (e.g. behind the GUI)
		WasabiInput mainInput = new WasabiInput();
		inputMultiplexer.addProcessor(mainInput);
		Gdx.input.setInputProcessor(inputMultiplexer);
		
		// Create and start the "first level" (level editor)
		addScreen(new LevelEditor(this, inputMultiplexer), LevelEditor.NAME);
		getAndSetScreen(LevelEditor.NAME);
	}
	
	@Override
	public void dispose() {
		super.dispose();
		
		// Don't actually do cleanup because it's slow as hell (OK 5 seconds, but still...) to
		// regenerate every time.
		
//		// clean up generated atlas files
//		int atlasNum = 2;
//		String atlasBase = "../wasabi-android/assets/wasabi-atlas";
//		FileHandle atlasFile = Gdx.files.local(atlasBase + ".atlas");
//		FileHandle atlasPng = Gdx.files.local(atlasBase + ".png");
//		boolean deleteSuccess = atlasPng.delete() && atlasFile.delete();
//		while (deleteSuccess) {
//			String filename = atlasBase + atlasNum++ + ".png";
//			atlasPng = Gdx.files.local(filename);
//			deleteSuccess = atlasPng.delete();
//		}
//		// delete empty directories (for cooper's run)
//		Gdx.files.local("../wasabi-android/assets/").delete();
//		Gdx.files.local("../wasabi-android/").delete();
	}

}
