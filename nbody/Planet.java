public class Planet {
	double xxPos;
	double yyPos;
	double xxVel;
	double yyVel;
	double mass;
	String imgFileName;
	private static final double g = 6.67e-11;

	public Planet(double xP, double yP, double xV, double yV, double m, String img) {
		xxPos = xP;
		yyPos = yP;
		xxVel = xV;
		yyVel = yV;
		mass = m;
		imgFileName = img;
	}
	public Planet(Planet p) {
		xxPos = p.xxPos;
		yyPos = p.yyPos;
		xxVel = p.xxVel;
		yyVel = p.yyVel;
		mass = p.mass;
		imgFileName = p.imgFileName;
	}

	public double calcDistance(Planet p) {
		double xdist = 0;
		double ydist = 0;
		xdist = p.xxPos - this.xxPos;
		ydist = p.yyPos - this.yyPos;
		return Math.sqrt(xdist*xdist + ydist*ydist);
	}
	public double calcForceExertedBy(Planet p) {
		return (g * this.mass * p.mass) / ((this.calcDistance(p)) * (this.calcDistance(p)));
	}

	public double calcForceExertedByX(Planet p) {
	    double dx = p.xxPos - this.xxPos;
	    return (calcForceExertedBy(p) * dx) / calcDistance(p);
    }

    public double calcForceExertedByY (Planet p) {
		double dy = p.yyPos - this.yyPos;
		return (calcForceExertedBy(p) * dy) / calcDistance(p);
	}

	public double calcNetForceExertedByX(Planet[] AllPlanets) {
		double totalxforce = 0;
		for (Planet p : AllPlanets) {
			if (p.equals(this)) {
				continue;
			}
			totalxforce = totalxforce + calcForceExertedByX(p);

		}
		return totalxforce;
	}

	public double calcNetForceExertedByY(Planet[] AllPlanets) {
		double totalyforce = 0;
		for (Planet p : AllPlanets) {
			if (p.equals(this)) {
				continue;
			}
			totalyforce = totalyforce + calcForceExertedByY(p);

		}
		return totalyforce;
	}

	public void update(double dt, double fX, double fY) {
		double aNetX = fX / this.mass;
		double aNetY = fY / this.mass;
		this.xxVel = this.xxVel + (dt * aNetX);
		this.yyVel = this.yyVel + (dt * aNetY);
		this.xxPos = this.xxPos + (dt * xxVel);
		this.yyPos = this.yyPos + (dt * yyVel);
	}

	public void draw() {
		StdDraw.picture(this.xxPos, this.yyPos, "images/" + this.imgFileName);
	}
}
