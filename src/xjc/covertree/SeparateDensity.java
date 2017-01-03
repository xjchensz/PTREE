package xjc.covertree;

public class SeparateDensity implements Comparable<SeparateDensity> {
	public int id;
	private double density;
	private double seprateDistance;
	public double seprateDensity;

	public SeparateDensity(int id) {
		this.id = id;
	}

	public int getID() {
		return id;
	}

	public double getLevelDensity() {
		return density;
	}

	public void setLevelDensity(double density) {
		this.density = density;
	}

	public double getSeprateDistance() {
		return seprateDistance;
	}

	public void setSeprateDistance(double seprateDistance) {
		this.seprateDistance = seprateDistance;
		seprateDensity = seprateDistance * density;
	}

	public double getSeprateDensity() {
		return seprateDensity;
	}

	@Override
	public int compareTo(SeparateDensity o) {
		double diff = seprateDensity - o.seprateDensity;
		if (diff == 0) {
			return 0;
		} else {
			return diff > 0 ? 1 : -1;
		}
	}

}
