package net.eekysam.creeps.grow.sim;

public class SpikeObject extends BrownianObject
{
	public SpikeObject(double radius)
	{
		super(radius);
	}
	
	@Override
	public int getColor()
	{
		return 0xD00000;
	}
	
	@Override
	public double getHardness()
	{
		return 0.9;
	}
}
