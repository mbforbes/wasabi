package com.mortrag.ut.wasabi;

import java.util.Iterator;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class LevelEditor implements Screen {

	// --------------------------------------------------------------------------------------------
	// CONSTANTS
	// --------------------------------------------------------------------------------------------
	private static final int GRID_SPACING = 20;
	private static final float ZOOM_DELTA = 0.02f;
	private static final float ZOOM_LIMIT = 0.1f;
	private static final float CAM_MOVE_SPEED = 5.0f;
	private static final float SPRITE_MOVE_SPEED = 6.0f;
	
	private static final float MAIN_VIEWPORT_WIDTH_FRAC = 0.75f; 

	// --------------------------------------------------------------------------------------------
	// MEMBERS
	// --------------------------------------------------------------------------------------------

	private SpriteBatch batch;
	private TextureAtlas atlas;
	private Array<Sprite> sprites;
	private Array<Sprite> placedSprites;
	private Texture texture;
	private ShapeRenderer shapeRenderer;
	
	private Sprite curSprite;
	private int curSpriteNum;
	private boolean nLastRender;
	private boolean spaceLastRender;
	
	// TODO(max): Change to WasabiInput extends InputProcessor
	// - create key map so a new one can be made per level
	// - create a command object per level so that commands can be enumed and they can have 
	// a string map
	// - make it so that listing keys are automatic, and that keys cannot be added without 
	// descriptions (edit once, cannot partially edit, update all)
	
	// viewports
	private Rectangle overall_viewport, main_viewport, minimap_viewport, detail_viewport;
	private OrthographicCamera overall_cam, main_cam, minimap_cam, detail_cam;	


	//Window w/h, level w/h, main camera (viewport) w/h. All units in pixels.
	float w, h, level_width, level_height, main_width, minimap_height;	
	
	Array<Commands> commands;

	
	public LevelEditor(Game g) {
		w = Gdx.graphics.getWidth();
		h = Gdx.graphics.getHeight();

		// TODO(max): Set these in config.
		level_width = 2000;
		level_height = 1500;
		
		// Viewports are areas of the java window that have stuff rendered in them.
		// Cameras project things to viewports. They can be zoomed and moved.
		// -----------------------------------------------------------------------------------------
		// Overall (whole window)
		overall_viewport = new Rectangle(0, 0, w, h);
		overall_cam = new OrthographicCamera(w, h);
		overall_cam.translate(w / 2.0f, h / 2.0f, 0.0f);
		
		// Main (editor area).
		main_width = w * MAIN_VIEWPORT_WIDTH_FRAC;
		main_viewport = new Rectangle(0, 0, main_width, h);
		main_cam = new OrthographicCamera(main_width, h);
		main_cam.translate(main_width / 2.0f, h / 2.0f, 0.0f);
		
		// Minimap
		minimap_height = (w - main_width) * (level_height / level_width);
		minimap_viewport = new Rectangle(main_width, h - minimap_height, w - main_width,
				minimap_height);
		minimap_cam = new OrthographicCamera(level_width, level_height);
		minimap_cam.translate(level_width / 2.0f, level_height / 2.0f, 0.0f);

		// Detail
		detail_viewport = new Rectangle(main_width, 0, w - main_width, h - minimap_height);
		detail_cam = new OrthographicCamera(w, h); // not sure what to set this to...
		
		// Drawing (sprite batches, textures, ...)
		batch = new SpriteBatch();

		atlas = new TextureAtlas(Gdx.files.internal("../wasabi-android/assets/wasabi-atlas.atlas"));
		
		sprites = atlas.createSprites();
		placedSprites = new Array<Sprite>();
		curSpriteNum = 0;
		curSprite = sprites.get(curSpriteNum);
		curSprite.setPosition(0.0f, 0.0f);
		
		//texture = new Texture(Gdx.files.internal("data/libgdx.png"));
		//texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		shapeRenderer = new ShapeRenderer();
		commands = new Array<Commands>();
		
		print(Input.Keys.DOWN);
	}

	private void renderSprites(Camera c) {
		batch.setProjectionMatrix(c.combined);
		batch.begin();
		// batch drawing group
		{
			// the one we're moving around
			curSprite.draw(batch);
			
			// the ones we've placed
			Iterator<Sprite> sit = placedSprites.iterator();
			while (sit.hasNext()) {
				sit.next().draw(batch);
			}
		}
		batch.end();
	}
	
	@Override
	public void render(float delta) {
		
		// input
		handleInput();
		
		// handle GL stuff
		GL20 gl = Gdx.graphics.getGL20();
		gl.glClearColor(1, 1, 1, 1);
		gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		// update cameras
		overall_cam.update();
		main_cam.update();
		minimap_cam.update();
		detail_cam.update();
		
		// Draw main sprites and grid
		gl.glViewport((int) main_viewport.x, (int) main_viewport.y,
				(int) main_viewport.width, (int) main_viewport.height);
		renderSprites(main_cam);
		drawGrid(main_cam);
		
		// Draw minimap sprites
		gl.glViewport((int) minimap_viewport.x, (int) minimap_viewport.y,
				(int) minimap_viewport.width, (int) minimap_viewport.height);
		renderSprites(minimap_cam);
		
		// Draw overall lines
		gl.glViewport((int) overall_viewport.x, (int) overall_viewport.y,
				(int) overall_viewport.width, (int) overall_viewport.height);		
		drawEditorLines(overall_cam);
	}
	
	private void drawEditorLines(Camera c) {
		// setup
		shapeRenderer.setProjectionMatrix(c.combined);
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(Color.BLACK); // alpha doesn't do anything...
		
		// Line to separate main window
		shapeRenderer.line(main_width, 0, main_width, h);
		
		// Line to separate minimap from detail view.
		shapeRenderer.line(main_width, h - minimap_height, w, h - minimap_height);
		
		// end
		shapeRenderer.end();
	}

	private void drawGrid(Camera c) {
		// set up renderer
		shapeRenderer.setProjectionMatrix(c.combined);
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 0.0f); // alpha doesn't do anything...

		// vertical lines
		for (int i = 0; i <= level_width; i += GRID_SPACING) {
			shapeRenderer.line(i, level_height, i, 0);
		}

		// horizontal lines
		for (int i = 0; i <= level_height; i += GRID_SPACING) {
			shapeRenderer.line(0, i, level_width, i);
		}
		shapeRenderer.end();		
	}

	public enum Commands {
		CAMERA_RIGHT,
		CAMERA_LEFT,
		CAMERA_UP,
		CAMERA_DOWN,
		CAMERA_ZOOM_IN,
		CAMERA_ZOOM_OUT,

		MOVE_RIGHT,
		MOVE_LEFT,
		MOVE_UP,
		MOVE_DOWN,

		PAUSE,
		NEXT_SPRITE,
		PLACE_SPRITE;
	}
	
	/**
	 * Convenience method for printing.
	 * @param o  thing to print
	 */
	private void print(Object o) {
		System.out.println(o);
	}
	
	private void handleInput() {
		// Sets commands array.
		handleInputRaw();

		Iterator<Commands> cit = commands.iterator();
		boolean nThisRender = false;
		boolean spaceThisRender = false;
		while (cit.hasNext()) {
			// TODO(max): Filter out all these magic numbers into settings.
			switch(cit.next()) {
			case CAMERA_RIGHT:
				// TODO(max): Check all these checks.				
				main_cam.translate(CAM_MOVE_SPEED, 0, 0);	
				break;
			case CAMERA_LEFT:
				// TODO(max): Check all these checks.
				main_cam.translate(-CAM_MOVE_SPEED, 0, 0);					
				break;
			case CAMERA_UP:
				// TODO(max): Check all these checks.
				main_cam.translate(0, CAM_MOVE_SPEED, 0);
				break;
			case CAMERA_DOWN:
				// TODO(max): Check all these checks.
				main_cam.translate(0, -CAM_MOVE_SPEED, 0);
				break;
			case CAMERA_ZOOM_IN:				
				if (main_cam.zoom >= ZOOM_LIMIT) {
					main_cam.zoom -= ZOOM_DELTA;
				}
				break;
			case CAMERA_ZOOM_OUT:
				// TODO(max): Make a check.
				main_cam.zoom += ZOOM_DELTA;
				break;	
				
			case MOVE_RIGHT:
				if (curSprite.getX() + curSprite.getWidth() + SPRITE_MOVE_SPEED < level_width) {
					curSprite.translateX(SPRITE_MOVE_SPEED);	
				}
				break;
			case MOVE_LEFT:
				if (curSprite.getX() - SPRITE_MOVE_SPEED > 0) {
					curSprite.translateX(-SPRITE_MOVE_SPEED);
				}
				break;
			case MOVE_UP:
				if (curSprite.getY() + curSprite.getHeight() + SPRITE_MOVE_SPEED < level_height) {
					curSprite.translateY(SPRITE_MOVE_SPEED);
				}
				break;
			case MOVE_DOWN:
				if (curSprite.getY() - SPRITE_MOVE_SPEED > 0) {
					curSprite.translateY(-SPRITE_MOVE_SPEED);
				}
			case PAUSE:
				pause();
				break;
			case NEXT_SPRITE:
				nThisRender = true;
				if (!nLastRender) {
					float newx = curSprite.getX();
					float newy = curSprite.getY();
					curSpriteNum  = (curSpriteNum + 1) % (sprites.size - 1);
					curSprite = sprites.get(curSpriteNum);
					curSprite.setPosition(newx, newy);
				}
				break;
			case PLACE_SPRITE:
				spaceThisRender = true;
				if (!spaceLastRender) {
					Sprite placedSprite = new Sprite(curSprite);
					placedSprites.add(placedSprite);
				}
				break;
			default:
				// Do nothing.
				break;
			}
		}	
		nLastRender = nThisRender;	
		spaceLastRender = spaceThisRender;
	}

	private void handleInputRaw() {
		commands.clear();

		// camera
		if(Gdx.input.isKeyPressed(Input.Keys.E)) {
			commands.add(Commands.CAMERA_ZOOM_IN);
		}
		if(Gdx.input.isKeyPressed(Input.Keys.Q)) {
			commands.add(Commands.CAMERA_ZOOM_OUT);
		}
		if(Gdx.input.isKeyPressed(Input.Keys.A)) {
			commands.add(Commands.CAMERA_LEFT);
		}
		if(Gdx.input.isKeyPressed(Input.Keys.D)) {
			commands.add(Commands.CAMERA_RIGHT);
		}
		if(Gdx.input.isKeyPressed(Input.Keys.W)) {
			commands.add(Commands.CAMERA_UP);
		}
		if(Gdx.input.isKeyPressed(Input.Keys.S)) {
			commands.add(Commands.CAMERA_DOWN);
		}
		
		// sprite movement
		if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
			commands.add(Commands.MOVE_LEFT);
		}
		if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
			commands.add(Commands.MOVE_RIGHT);
		}
		if(Gdx.input.isKeyPressed(Input.Keys.UP)) {
			commands.add(Commands.MOVE_UP);
		}
		if(Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
			commands.add(Commands.MOVE_DOWN);
		}		
		
		// game commands
		if (Gdx.input.isKeyPressed(Input.Keys.P)) {
			commands.add(Commands.PAUSE);
		}
		if (Gdx.input.isKeyPressed(Input.Keys.N)) {
			commands.add(Commands.NEXT_SPRITE);
		}
		if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
			commands.add(Commands.PLACE_SPRITE);
		}		
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
		// so this never happens...
		batch.dispose();
		texture.dispose();
	}

}
