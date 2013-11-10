package com.mortrag.ut.wasabi.characters;

public class PatrolBehavior implements Behavior {

	private float timeThisDirection, timeEachDirection, moveAccel;
	
	public PatrolBehavior(float timeEachDirection, float moveAccel) {
		timeThisDirection = 0.0f;
		this.timeEachDirection = timeEachDirection;
		this.moveAccel = moveAccel;
	}
	
	@Override
	public void tick(Behaviorable b, float delta) {
		if (timeThisDirection > timeEachDirection) {
			// abooooout, face!
			b.setFacingLeft(!b.getFacingLeft());
			timeThisDirection = 0.0f;
		}
		float directionMod = b.getFacingLeft() ? -1.0f : 1.0f;
		b.getA().x += moveAccel * directionMod;
		timeThisDirection += delta;
	}

}
