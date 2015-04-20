package net.eekysam.creeps.grow.sim;

public class Collision
{
	public final WorldObject a;
	public final WorldObject b;
	public final double distsqr;
	public final double velDot;
	
	public Collision(WorldObject a, WorldObject b, double distsqr, double dot)
	{
		this.a = a;
		this.b = b;
		this.distsqr = distsqr;
		this.velDot = dot;
	}
	
	@Override
	public int hashCode()
	{
		return this.a.hashCode() ^ this.b.hashCode();
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o instanceof Collision)
		{
			Collision col = (Collision) o;
			return (this.a.equals(col.a) && this.b.equals(col.b)) || (this.a.equals(col.b) && this.b.equals(col.a));
		}
		return false;
	}
}
