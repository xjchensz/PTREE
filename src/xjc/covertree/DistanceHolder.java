package xjc.covertree;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import common.data.distance.InstanceDistanceMeasure;
import common.data.instance.numeric.INumericInstance;

public class DistanceHolder implements IDistanceHolder{

	private static final long serialVersionUID = -33125012558577264L;
	double[][] m_Distance;
	int size;
	
	public DistanceHolder(double[][] distance){
		m_Distance=distance;
		size=distance.length;
	}
	
	@Override
	public void write(DataOutput out) throws IOException {
		out.writeUTF(getClass().getName());
		out.write(size);
		for(int i=0;i<size;i++){
			for(int j=0;j<size;j++)
			out.writeDouble(distance(i, j));
		}
		
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		try {
			Class<InstanceDistanceMeasure> claszz = (Class<InstanceDistanceMeasure>) Class.forName(in.readUTF());
			size=in.readInt();
			m_Distance=new double[size][size];
			for(int i=0;i<size;i++){
				for(int j=0;j<size;j++)
					m_Distance[i][j]=in.readDouble();
			}
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
	}

	@Override
	public double[][] distances() throws Exception {
		// TODO Auto-generated method stub
		return m_Distance;
	}

	@Override
	public double[] distances(int ins1) throws Exception {
		// TODO Auto-generated method stub
		return m_Distance[ins1];
	}

	@Override
	public double distance(int ins1, int ins2) {
		// TODO Auto-generated method stub
		return m_Distance[ins1][ins2];
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return size;
	}

	@Override
	public IDistanceHolder clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return null;
	}

}
