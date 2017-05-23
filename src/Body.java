class Body {
	double mass;
	double xPos, yPos;
	double xVel, yVel;

	public Body() {

	}

	public Body(Body b) {
		this.mass = b.mass;
		this.xPos = b.xPos;
		this.yPos = b.yPos;
		this.xVel = b.xVel;
		this.yVel = b.yVel;
	}
}
