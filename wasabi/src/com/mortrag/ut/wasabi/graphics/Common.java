package com.mortrag.ut.wasabi.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class Common {	
	
	private static final String PAUSED_TEXT = "THIS SHIT'S PAUSED";
	private static final float TEXT_MARGIN = 10.0f; // spacing we give b/w "paused" and commands.
	private static final int ANIMATION_START_NUM = 0; // how Cooper numbers animations
	
	private static final BitmapFont fpsFont = makeFont(Color.BLACK, 1.2f), pausedFont =
			makeFont(Color.GRAY, 10.0f), controlsFont = makeFont(Color.WHITE, 1.2f);
	private static float w = Gdx.graphics.getWidth(), h = Gdx.graphics.getHeight();	
	private static Sprite pauseOverlaySprite = makePauseOverlaySprite();
	
	private static BitmapFont makeFont(Color color, float scale) {
		BitmapFont font = new BitmapFont();
		font.setColor(color);
		font.setScale(scale);
		return font;
	}	
	
	private static Sprite makePauseOverlaySprite() {
		// precompute pause overlay into sprite
		Pixmap pixmap = new Pixmap( (int)w, (int)h, Format.RGBA8888 );
		pixmap.setColor( 0f, 0.5f, 1f, 0.8f );
		pixmap.fillRectangle(0, 0, (int)w, (int)h);
		Texture pixmaptex = new Texture( pixmap );
		pixmap.dispose();
		Sprite s = new Sprite(pixmaptex, (int)w, (int)h);
		s.setPosition(0.0f, 0.0f);
		return s;
	}
	
	private static void recomputePauseOverlaySprite() {
		// update w and h, and set a new sprite
		w = Gdx.graphics.getWidth();
		h = Gdx.graphics.getHeight();
		pauseOverlaySprite = makePauseOverlaySprite();				
	}
	
	private static void maybeRecomputePauseOverlaySprite() {
		// only recompute if w or h has changed 
		if (w != Gdx.graphics.getWidth() || h != Gdx.graphics.getHeight()) {
			recomputePauseOverlaySprite();
		}
	}

	public static void drawPauseOverlay(Camera camera, SpriteBatch batch, String controlsList) {
		// ensure pause overlay is up to date
		maybeRecomputePauseOverlaySprite();
		
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		
		// pixmap!?		
		pauseOverlaySprite.draw(batch);

		// draw paused txt
		pausedFont.drawWrapped(batch, PAUSED_TEXT, 0.0f, h, w, HAlignment.CENTER);
		float pausedTxtHeight = pausedFont.getBounds(PAUSED_TEXT).height;
		
		// draw controls
		controlsFont.drawWrapped(batch, controlsList, 0.0f, h - pausedTxtHeight -
				TEXT_MARGIN, w);
		
		batch.end();		
	}
	
	/**
	 * Displays FPS in top-let corner. No idea if works when camera's not overall.
	 * @param batch
	 * @param c
	 */
	public static void displayFps(Camera camera, SpriteBatch batch) {
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		fpsFont.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 0.0f,
				Gdx.graphics.getHeight());
		batch.end();
	}	
	
	/**
	 * Creates new array each time--careful!
	 * @param atlas All 'dem sprites.
	 * @param name String to match filename of. Appends 0, 1, ... until it can't find any more.
	 * @return
	 */
	public static Array<TextureRegion> getFrames(TextureAtlas atlas, String name) {
		int i = ANIMATION_START_NUM;
		Array<TextureRegion> frames = new Array<TextureRegion>();
		while(true) {
			TextureRegion frame = atlas.findRegion(name + i++);	 
			if (frame == null) {
				break;
			}
			frames.add(frame);
		}
		return frames;
	}
}
