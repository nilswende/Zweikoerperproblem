import java.util.Observable;

class Model extends Observable {
	private static final double GRAV_CONST = 6.67408e-11;

	private Body body1, body2;
	private Body body1_half, body2_half;
	private Body barycenter;
	private double area1, area2;

	// Step 1: choose a small time interval for calculation
	private final double deltaT = 3600;    // one hour step
	private final int iterationsPerFrame = 6;        // new positions every 6 hours

	// Step 2: get initial values
	void initializeBodies(Body body1, Body body2) {
		if (oneBodyIsNull(body1, body2)) {
			throw new IllegalArgumentException("One of the bodies is null.");
		}
		this.body1 = body1;
		this.body2 = body2;
		this.body1_half = body1;
		this.body2_half = body2;

		barycenter = new Body();
		setBarycenterPosition();

		this.setChanged();
		this.notifyObservers();
	}

	private boolean oneBodyIsNull(Body body1, Body body2) {
		return body1 == null || body2 == null;
	}

	private void setBarycenterPosition() {
		final double rBodies, rBary;
		rBodies = getDistanceOfBodies();
		rBary = rBodies * body2.mass / (body1.mass + body2.mass);

		double delta = rBary * (body2.xPos - body1.xPos) / rBodies;
		barycenter.xPos = body1.xPos + delta;

		delta = rBary * (body2.yPos - body1.yPos) / rBodies;
		barycenter.yPos = body1.yPos + delta;
	}

	private double getDistanceOfBodies() {
		final double deltaX = body2.xPos - body1.xPos;
		final double deltaY = body2.yPos - body1.yPos;
		return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
	}


	Body[] getBodies() {
		return new Body[]{body1, body2, barycenter};
	}

	double[] getAreas() {
		return new double[]{area1, area2};
	}


	void simulateInitialHalfstep() {
		final double a1x, a1y, a2x, a2y;
		try {
			// Step 3: initial accelerations
			a1x = getAcceleration(body2.mass, body2.xPos, body1.xPos);
			a1y = getAcceleration(body2.mass, body2.yPos, body1.yPos);
			a2x = getAcceleration(body1.mass, body1.xPos, body2.xPos);
			a2y = getAcceleration(body1.mass, body1.yPos, body2.yPos);
		}
		catch (DivisionByZeroException ex) {
			throw ex;
		}
		// Step 4: initial half-step
		body1_half.xPos = getInitialPosition(body1.xPos, body1.xVel, a1x);
		body1_half.yPos = getInitialPosition(body1.yPos, body1.yVel, a1y);
		body2_half.xPos = getInitialPosition(body2.xPos, body2.xVel, a2x);
		body2_half.yPos = getInitialPosition(body2.yPos, body2.yVel, a2y);

		this.setChanged();
		this.notifyObservers();
	}


	void simulateNextStep() {
		Body body1before = new Body(body1);
		Body body2before = new Body(body2);

		double a1x, a1y, a2x, a2y;
		for (int i = 0; i < iterationsPerFrame; ++i) {
			try {
				// Step 5: new accelerations
				a1x = getAcceleration(body2_half.mass, body2_half.xPos, body1_half.xPos);
				a1y = getAcceleration(body2_half.mass, body2_half.yPos, body1_half.yPos);
				a2x = getAcceleration(body1_half.mass, body1_half.xPos, body2_half.xPos);
				a2y = getAcceleration(body1_half.mass, body1_half.yPos, body2_half.yPos);
			}
			catch (DivisionByZeroException ex) {
				throw ex;
			}
			// Step 6: new velocities
			body1.xVel = getVelocity(body1.xVel, a1x);
			body1.yVel = getVelocity(body1.yVel, a1y);
			body2.xVel = getVelocity(body2.xVel, a2x);
			body2.yVel = getVelocity(body2.yVel, a2y);

			// Step 7: new positions
			body1.xPos = getPosition(body1.xPos, body1.xVel);
			body1.yPos = getPosition(body1.yPos, body1.yVel);
			body2.xPos = getPosition(body2.xPos, body2.xVel);
			body2.yPos = getPosition(body2.yPos, body2.yVel);

			// Step 8: new half-step
			body1_half.xPos = getPosition(body1.xPos, body1.xVel);
			body1_half.yPos = getPosition(body1.yPos, body1.yVel);
			body2_half.xPos = getPosition(body2.xPos, body2.xVel);
			body2_half.yPos = getPosition(body2.yPos, body2.yVel);
		}
		setBarycenterPosition();
		area1 = getArea(body1, body1before);
		area2 = getArea(body2, body2before);

		this.setChanged();
		this.notifyObservers();
	}

	private double getAcceleration(double mass, double pos1, double pos2) {
		final double alpha = getAlpha(body1.xPos, body1.yPos, body2.xPos, body2.yPos);
		final double acc = GRAV_CONST * mass * (pos1 - pos2) / alpha;
		if (isInfiniteOrNaN(acc)) {
			throw new DivisionByZeroException("alpha == 0.0");
		}
		return acc;
	}

	private double getAlpha(double x1, double y1, double x2, double y2) {
		final double deltaX, deltaY, base;
		deltaX = x2 - x1;
		deltaY = y2 - y1;
		base = deltaX * deltaX + deltaY * deltaY;
		return Math.sqrt(base * base * base);
	}

	// only when alpha is exactly 0.0 => source of error
	private boolean isInfiniteOrNaN(double d) {
		return Double.isInfinite(d) || Double.isNaN(d);
	}

	private double getInitialPosition(double pos, double vel, double acc) // eq25
	{
		return pos + deltaT * vel / 2.0 + deltaT * deltaT * acc / 4.0;
	}

	private double getVelocity(double vel, double acc) // eq23
	{
		return vel + deltaT * acc;
	}

	private double getPosition(double pos, double vel) // eq22, eq24
	{
		return pos + deltaT * vel / 2.0;
	}


	private double getArea(Body b, Body before) {
		final double x1 = barycenter.xPos,
				y1 = barycenter.yPos,
				x2 = before.xPos,
				y2 = before.yPos,
				x3 = b.xPos,
				y3 = b.yPos;
		return Math.abs((x1 - x3) * (y2 - y1) - (x1 - x2) * (y3 - y1)) / 2.0;
	}
}
