package com.mortrag.ut.wasabi;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class WasabiGame extends Game implements ApplicationListener {

	@Override
	public void create() {
		WasabiInput input = new WasabiInput();
		Screen levelEditor = new LevelEditor(this, input);
		setScreen(levelEditor);
	}
	
	@Override
	public void dispose() {
		super.dispose();
		
		// Don't actually do cleanup because it's slow as hell to regenerate every time.
		
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
