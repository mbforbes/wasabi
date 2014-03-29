package com.mortrag.ut.wasabi.leveleditor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class LevelEditorGui {
	
	// MEMBERS
	private Stage stage;
	private LevelEditor levelEditor;
	
	// CONSTRUCTORS
	public LevelEditorGui(LevelEditor levelEditor, Stage stage) {
		this.levelEditor = levelEditor;
		this.stage = stage;
		
		// Do the setup!
		Skin skin = new Skin(Gdx.files.internal("skins/default/uiskin.json"));
		TextButton newLayerButton = new TextButton("new layer", skin);
		newLayerButton.setX(10.0f);
		newLayerButton.setY(levelEditor.h - 40);
		newLayerButton.addListener(new ClickListener() {
			public void clicked (InputEvent event, float x, float y) {
				addLayer();
			}
		});
		stage.addActor(newLayerButton);
	}
	
	// PRIVATE
	private void addLayer() {
		levelEditor.addLayer();
	}
	
	// PUBLIC API
}
