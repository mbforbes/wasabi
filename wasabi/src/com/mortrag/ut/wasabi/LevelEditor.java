package com.mortrag.ut.wasabi;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.mortrag.ut.wasabi.util.Debug;

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
	
	private static final float TEXT_MARGIN = 10.0f; // spacing we give b/w "paused" and commands.
	

	// --------------------------------------------------------------------------------------------
	// MEMBERS
	// --------------------------------------------------------------------------------------------
	// Textures, sprites, shapes, fonts
	private TextureAtlas atlas;
	private Texture texture;
	private SpriteBatch batch;
	private Array<Sprite> sprites, placedSprites;
	private Sprite curSprite, pauseSprite;
	private int curSpriteNum;
	private ShapeRenderer shapeRenderer;
	private BitmapFont fpsFont, pausedFont, controlsFont;
	private String pausedText = "THIS SHIT'S PAUSED";
	
	// Viewports, Cameras, Window sizes
	private Rectangle overall_viewport, main_viewport, minimap_viewport, detail_viewport;
	private OrthographicCamera overall_cam, main_cam, minimap_cam, detail_cam;	
	// Window w/h, level w/h, main camera (viewport) w, minimap (viewport) h. All units in pixels.
	float w, h, level_width, level_height, main_width, minimap_height;		
	
	// Game, input, controls, commands
	private Game game;
	private WasabiInput input;
	private Controls controls;
	private Array<Commands> commandList;
	
	// State
	private boolean paused = false;

	
	// --------------------------------------------------------------------------------------------
	// CONSTRUCTORS
	// --------------------------------------------------------------------------------------------

	public LevelEditor(Game game, WasabiInput input) {
		this.game = game;
		this.input = input;

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
		// -----------------------------------------------------------------------------------------		
		batch = new SpriteBatch();
		atlas = new TextureAtlas(Gdx.files.internal("../wasabi-android/assets/wasabi-atlas.atlas"));
		sprites = atlas.createSprites();
		placedSprites = new Array<Sprite>();
		curSpriteNum = 0;
		curSprite = sprites.get(curSpriteNum);
		curSprite.setPosition(0.0f, 0.0f);

		
		// Bit shapes
		// -----------------------------------------------------------------------------------------
		shapeRenderer = new ShapeRenderer();
		// precompute pause overlay into sprite
		Pixmap pixmap = new Pixmap( (int)w, (int)h, Format.RGBA8888 );
		pixmap.setColor( 0f, 0.5f, 1f, 0.8f );
		pixmap.fillRectangle(0, 0, (int)w, (int)h);
		Texture pixmaptex = new Texture( pixmap );
		pixmap.dispose();
		pauseSprite = new Sprite(pixmaptex, (int)w, (int)h);
		pauseSprite.setPosition(0.0f, 0.0f);
		fpsFont = new BitmapFont();
		fpsFont.setColor(Color.BLACK);
		fpsFont.setScale(1.2f);
		pausedFont = new BitmapFont();
		pausedFont.setColor(Color.GRAY);
		pausedFont.setScale(10.0f);
		controlsFont = new BitmapFont();
		controlsFont.setColor(Color.WHITE);
		controlsFont.setScale(1.2f);
		
		
		// Input
		// -----------------------------------------------------------------------------------------
		commandList = new Array<Commands>();
		controls = new LevelEditor_Controls();
		input.setControls(controls, commandList);
		Gdx.input.setInputProcessor(input);
	}
	
	
	// --------------------------------------------------------------------------------------------
	// PRIVATE METHODS
	// --------------------------------------------------------------------------------------------

	private void renderSprites(Camera c) {
		batch.setProjectionMatrix(c.combined);
		batch.begin();
		// batch drawing group
		{
			// the ones we've placed
			Iterator<Sprite> sit = placedSprites.iterator();
			while (sit.hasNext()) {
				sit.next().draw(batch);
			}

			// the one we're moving around
			curSprite.draw(batch);
		}
		batch.end();
	}
	
	private void displayFps(Camera c) {
		batch.setProjectionMatrix(c.combined);
		batch.begin();
		fpsFont.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 0.0f, h);
		batch.end();
	}
	
	private void drawPauseOverlay(Camera c) {		
		batch.setProjectionMatrix(c.combined);
		batch.begin();
		
		// pixmap!?		
		pauseSprite.draw(batch);

		// draw paused txt
		pausedFont.drawWrapped(batch, pausedText, 0.0f, h, w, HAlignment.CENTER);
		float pausedTxtHeight = pausedFont.getBounds(pausedText).height;
		
		// draw controls
		controlsFont.drawWrapped(batch, controls.getControlsList(), 0.0f, h - pausedTxtHeight -
				TEXT_MARGIN, w);
		
		batch.end();		
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

	/**
	 * Convenience method for printing.
	 * @param o  thing to print
	 */
	@SuppressWarnings("unused")
	private void print(Object o) {
		System.out.println(o);
	}

	private void handleCommands() {

		Iterator<Commands> cit = commandList.iterator();
		while (cit.hasNext()) {
			LevelEditor_Commands c = (LevelEditor_Commands) cit.next();
			if (paused) {
				// Game is paused command interpretation
				switch(c) {
				case PAUSE:
					resume();
					break;
				default:
					// do nothing
					break;
				}
			} else {
				// Normal Level Editor command interpretation				
				switch(c) {
				case CAMERA_RIGHT:
					main_cam.translate(CAM_MOVE_SPEED, 0, 0);	
					break;
				case CAMERA_LEFT:
					main_cam.translate(-CAM_MOVE_SPEED, 0, 0);					
					break;
				case CAMERA_UP:
					main_cam.translate(0, CAM_MOVE_SPEED, 0);
					break;
				case CAMERA_DOWN:
					main_cam.translate(0, -CAM_MOVE_SPEED, 0);
					break;
				case CAMERA_ZOOM_IN:				
					if (main_cam.zoom >= ZOOM_LIMIT) {
						main_cam.zoom -= ZOOM_DELTA;
					}
					break;
				case CAMERA_ZOOM_OUT:
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
					break;
				case PAUSE:
					pause();
					break;
				case NEXT_SPRITE:
					float newx = curSprite.getX();
					float newy = curSprite.getY();
					curSpriteNum  = (curSpriteNum + 1) % (sprites.size - 1);
					curSprite = sprites.get(curSpriteNum);
					curSprite.setPosition(newx, newy);
					break;
				case PLACE_SPRITE:
					Sprite placedSprite = new Sprite(curSprite);
					placedSprites.add(placedSprite);
					break;
				default:
					// Do nothing.
					break;
				}
			}	
		} // else (if paused)

		// removes the inputs that were just PRESS actions
		input.clearPress();
	}

	
	// --------------------------------------------------------------------------------------------
	// PUBLIC METHODS
	// --------------------------------------------------------------------------------------------	
	
	@Override
	public void render(float delta) {

		// input
		handleCommands();

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
		
		// paused overlay
		if (paused) {
			drawPauseOverlay(overall_cam);
		}
		if (Debug.DEBUG) {
			displayFps(overall_cam);
		}
	}
	
	@Override
	public void resize(int width, int height) {
		// only resize if anything has actually changed
		if (width == (int) w && height == (int) h) {
			return;
		}
		w = width;
		h = height;
		
		// reconfig overall viewport/cam
		overall_viewport.width = w;
		overall_viewport.height = h;
		overall_cam.viewportWidth = w;
		overall_cam.viewportHeight = h;
		overall_cam.position.mul(0.0f);
		overall_cam.translate(w / 2.0f, h / 2.0f, 0.0f);

		// reconfig main viewport/cam
		main_width = w * MAIN_VIEWPORT_WIDTH_FRAC;
		main_viewport.width = main_width;
		main_viewport.height = h;
		main_cam.viewportWidth = main_width;
		main_cam.viewportHeight = h;
		main_cam.position.mul(0.0f);
		main_cam.translate(main_width / 2.0f, h / 2.0f, 0.0f);

		// reconfig minimap viewport/ cam
		minimap_height = (w - main_width) * (level_height / level_width);
		minimap_viewport.x = main_width;
		minimap_viewport.y = h - minimap_height;
		minimap_viewport.width = w - main_width;
		minimap_viewport.height = minimap_height;
		//minimap_cam = new OrthographicCamera(level_width, level_height);
		minimap_cam.position.mul(0.0f);
		minimap_cam.translate(level_width / 2.0f, level_height / 2.0f, 0.0f);

		// reconfig detail viewport / cam
		detail_viewport.x = main_width;
		detail_viewport.width = w - main_width;
		detail_viewport.height = h - minimap_height;
		// NOTE(max): Update this once it's been actually set.
		// detail_cam = new OrthographicCamera(...); // not sure what to set this to...		
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
		paused = true;

	}

	@Override
	public void resume() {
		paused = false;
	}

	@Override
	public void dispose() {
		// so this never happens...
		batch.dispose();
		texture.dispose();
	}

}
