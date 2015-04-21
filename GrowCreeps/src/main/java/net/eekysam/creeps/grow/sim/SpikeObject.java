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
	
	@Override
	public void collision(WorldObject other, double distsqr, double dot)
	{
		if (other instanceof Creep)
		{
			Creep creep = (Creep) other;
			creep.health -= 3.0;
		}
	}
}
