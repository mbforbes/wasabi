package com.mortrag.ut.wasabi.input;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Input;

public class FullKeys {
	
	//only ever one
	private static Map<Integer, Keys> screenKeysMap = null;
	
	public static Map<Integer, Keys> getMap() {
		// make it if it doesn't exist, and return it
		if (screenKeysMap == null) {
			screenKeysMap = new HashMap<Integer, Keys>();
			for (Keys k : Keys.values()) {
				screenKeysMap.put(k.keyNum(), k);
			}
		}
		return screenKeysMap;
	}
	
	public enum Keys {
		
		A(Input.Keys.A, "A"),
		B(Input.Keys.B, "B"),
		C(Input.Keys.C, "C"),
		D(Input.Keys.D, "D"),
		E(Input.Keys.E, "E"),
		F(Input.Keys.F, "F"),
		G(Input.Keys.G, "G"),
		H(Input.Keys.H, "H"),
		I(Input.Keys.I, "I"),
		J(Input.Keys.J, "J"),
		K(Input.Keys.K, "K"),
		L(Input.Keys.L, "L"),
		M(Input.Keys.M, "M"),
		N(Input.Keys.N, "N"),
		O(Input.Keys.O, "O"),
		P(Input.Keys.P, "P"),
		Q(Input.Keys.Q, "Q"),
		R(Input.Keys.R, "R"),
		S(Input.Keys.S, "S"),
		T(Input.Keys.T, "T"),
		U(Input.Keys.U, "U"),
		V(Input.Keys.V, "V"),
		W(Input.Keys.W, "W"),
		X(Input.Keys.X, "X"),
		Y(Input.Keys.Y, "Y"),
		Z(Input.Keys.Z, "Z"),
		
		UP(Input.Keys.UP, "up arrow key"),
		DOWN(Input.Keys.DOWN, "down arrow key"),
		LEFT(Input.Keys.LEFT, "left arrow key"),
		RIGHT(Input.Keys.RIGHT, "right arrow key"),
		
		SPACE(Input.Keys.SPACE, "space bar"),
		ENTER(Input.Keys.ENTER, "enter key"),
		SHIFT_LEFT(Input.Keys.SHIFT_LEFT, "left shift"),
		SHIFT_RIGHT(Input.Keys.SHIFT_RIGHT, "right shift"),
		TAB(Input.Keys.TAB, "tab");
		
		
		private final int keyNum;
		private final String desc;
		
		private Keys(int keyNum, String desc){
			this.keyNum = keyNum;
			this.desc = desc;
		}
		
		
		/**
		 * @return a human-readable description of the key
		 */
		public String toString() {
			return desc;
		}
		
		/**
		 * 
		 * @return a computer-readable key code
		 */
		public int keyNum() {
			return keyNum;
		}
	}
}


