package com.mortrag.ut.wasabi;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class OrthographicCameraController implements ApplicationListener {

	static final int WIDTH  = 250;
	static final int HEIGHT = 250;

	private OrthographicCamera      cam;
	private Texture                 texture;
	private Sprite                  sprite;
	private SpriteBatch             spriteBatch;
	private Rectangle               glViewport;
	private float                   rotationSpeed;

	@Override
	public void create() {		
		texture = new Texture(Gdx.files.internal("data/sc_map.png"));		
		sprite = new Sprite(texture);
		spriteBatch = new SpriteBatch();

		rotationSpeed = 0.5f;
		cam = new OrthographicCamera(50, 50);            
		cam.position.set(WIDTH / 2, HEIGHT / 2, 0);
		glViewport = new Rectangle(0, 0, WIDTH, HEIGHT);
	}

	@Override
	public void render() {
		handleInput();
		GL20 gl = Gdx.graphics.getGL20();

		// Camera --------------------- /
		gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
			
		
		cam.update();
		gl.glViewport((int) glViewport.x, (int) glViewport.y,
				(int) glViewport.width, (int) glViewport.height);
		
		spriteBatch.setProjectionMatrix(cam.combined);
		spriteBatch.begin();
		sprite.draw(spriteBatch);
		spriteBatch.end();	
		
		// Texturing --------------------- /
//		gl.glActiveTexture(GL20.GL_TEXTURE0);
//		gl.glEnable(GL20.GL_TEXTURE_2D);
//		texture.bind();


	}

	private void handleInput() {
		if(Gdx.input.isKeyPressed(Input.Keys.A)) {
			cam.zoom += 0.02;
		}
		if(Gdx.input.isKeyPressed(Input.Keys.Q)) {
			cam.zoom -= 0.02;
		}
		if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
			if (cam.position.x > 0)
				cam.translate(-3, 0, 0);
		}
		if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
			if (cam.position.x < 1024)
				cam.translate(3, 0, 0);
		}
		if(Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
			if (cam.position.y > 0)
				cam.translate(0, -3, 0);
		}
		if(Gdx.input.isKeyPressed(Input.Keys.UP)) {
			if (cam.position.y < 1024)
				cam.translate(0, 3, 0);
		}
		if(Gdx.input.isKeyPressed(Input.Keys.W)) {
			cam.rotate(-rotationSpeed, 0, 0, 1);
		}
		if(Gdx.input.isKeyPressed(Input.Keys.E)) {
			cam.rotate(rotationSpeed, 0, 0, 1);
		}
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void resume() {
		// TODO Auto-generated method stub
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
	}
}