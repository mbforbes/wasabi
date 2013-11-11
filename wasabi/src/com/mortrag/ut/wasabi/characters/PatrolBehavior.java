package com.mortrag.ut.wasabi.characters;

public class PatrolBehavior implements Behavior {

	private Behaviorable b;
	private float timeThisDirection, timeEachDirection, moveAccel;
	
	public PatrolBehavior(Behaviorable b, float timeEachDirection, float moveAccel) {
		this.b = b;
		this.timeEachDirection = timeEachDirection;
		this.moveAccel = moveAccel;
		
		// default--go right
		b.setGoingRight(true);
		
		timeThisDirection = 0.0f;
	}
	
	@Override
	public void tick(float delta) {
		if (timeThisDirection > timeEachDirection) {
			// abooooout, face!
			b.setFacingLeft(!b.getFacingLeft());
			b.setGoingLeft(b.getFacingLeft());
			b.setGoingRight(!b.getFacingLeft());
			timeThisDirection = 0.0f;
		}
		float directionMod = b.getFacingLeft() ? -1.0f : 1.0f;
		b.getA().x += moveAccel * directionMod;
		timeThisDirection += delta;
	}

}
