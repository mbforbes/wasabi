package com.mortrag.ut.wasabi;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class LevelEditor implements Screen {

	// --------------------------------------------------------------------------------------------
	// CONSTANTS
	// --------------------------------------------------------------------------------------------
	private static final int GRID_SPACING = 10;
	
	
	// --------------------------------------------------------------------------------------------
	// MEMBERS
	// --------------------------------------------------------------------------------------------
	
	//private OrthographicCamera camera;
	private SpriteBatch batch;
	private Texture texture;
	private Sprite sprite;	
	private ShapeRenderer shapeRenderer;
	
	float w;
	float h;
	
	public LevelEditor(Game g) {
		w = Gdx.graphics.getWidth();
		h = Gdx.graphics.getHeight();
		
		//camera = new OrthographicCamera(1, h/w);
		batch = new SpriteBatch();
		

		texture = new Texture(Gdx.files.internal("data/libgdx.png"));
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		TextureRegion region = new TextureRegion(texture, 0, 0, 512, 275);
		
		sprite = new Sprite(region);
		sprite.setSize(0.9f, 0.9f * sprite.getHeight() / sprite.getWidth());
		sprite.setPosition(0, 0);
		sprite.setOrigin(sprite.getWidth()/2, sprite.getHeight()/2);
		sprite.setPosition(-sprite.getWidth()/2, -sprite.getHeight()/2);
		
		shapeRenderer = new ShapeRenderer();
	}
	
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		//batch.setProjectionMatrix(camera.combined);
		batch.begin();
		sprite.draw(batch);
		batch.end();	
		
		drawGrid(GRID_SPACING);
		
	}
	
	private void drawGrid(int spacing) {
		// set up renderer
		//shapeRenderer.setProjectionMatrix(camera.combined);
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 0.0f); // alpha doesn't do anything...

		// vertical lines
		shapeRenderer.line(0, 100, 200, 300);
//		for (int i = 0; i <= w; i += spacing) {
//			shapeRenderer.line(i, top, i * 1.0f, bottom);
//		}

		// horizontal lines
//		for (int i = bottom; i <= top; i += (int)(inc *= spacing)) {
//			shapeRenderer.line(left, i, right, i);
//		}
		shapeRenderer.end();		
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		batch.dispose();
		texture.dispose();
	}

}
