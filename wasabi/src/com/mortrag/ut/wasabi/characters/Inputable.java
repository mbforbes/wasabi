package com.mortrag.ut.wasabi.characters;

public interface Inputable {
	public enum Input {
		LEFT,
		RIGHT,
		UP;
	}
	
	public void input(Input i);
}
