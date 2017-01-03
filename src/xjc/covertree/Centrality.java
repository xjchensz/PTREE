package xjc.covertree;

public class Centrality implements Comparable<Centrality> {
	public int id;
	private double knnradius;
	private double cdist;
	public double centrality;

	public Centrality(int id) {
		this.id = id;
	}

	public int getID() {
		return id;
	}

	public double getKnnradius() {
		return knnradius;
	}

	public void setKnnradius(double knnradius) {
		this.knnradius = knnradius;
	}

	public double getCdist() {
		return cdist;
	}

	public void setCdist(double cdist) {
		this.cdist = cdist;
		centrality = cdist / knnradius;
	}

	public double getCentrality() {
		return centrality;
	}

	@Override
	public int compareTo(Centrality o) {
		return Double.compare(centrality, o.centrality);
	}

}
