package net.eekysam.creeps.grow.sim;

public class SpikeObject extends BrownianObject
{
	public SpikeObject(double radius)
	{
		super(radius * 1.2);
	}
	
	@Override
	public int getColor()
	{
		return 0xD00000;
	}
	
	@Override
	public double getHardness()
	{
		return -1;
	}
	
	@Override
	public void collision(WorldObject other, double distsqr, double velx, double vely)
	{
		if (other instanceof Creep)
		{
			double dmg = 1.5;
			Creep creep = (Creep) other;
			creep.damage(EnumDmgType.SPIKE, dmg * this.world().speed);
		}
	}
}
