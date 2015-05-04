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
			double speed = this.world().speed;
			Creep creep = (Creep) other;
			creep.damage(EnumDmgType.SPIKE, 3 * speed);
		}
	}
}
