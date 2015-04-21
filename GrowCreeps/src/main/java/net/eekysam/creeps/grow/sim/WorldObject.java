package net.eekysam.creeps.grow.sim;

import java.util.UUID;

public abstract class WorldObject
{
	private World theWorld;
	
	private boolean remove = false;
	
	public double x;
	public double y;
	
	public double radius;
	
	public double velx;
	public double vely;
	
	public final UUID id;
	
	public WorldObject()
	{
		this.id = UUID.randomUUID();
	}
	
	public void spawn(World world)
	{
		if (this.theWorld == null)
		{
			this.theWorld = world;
			this.theWorld.addSpawn(this);
		}
	}
	
	public void tick(double rate, EnumTickPass pass)
	{
		if (pass == EnumTickPass.MOVE)
		{
			double nx = this.x += this.velx * rate;
			double ny = this.y += this.vely * rate;
			
			double dist = ny * ny + nx * nx;
			
			if (dist > this.theWorld.radius * this.theWorld.radius)
			{
				dist = Math.sqrt(dist);
				double ux = nx / dist;
				double uy = ny / dist;
				this.wallCollision(ux, uy);
				
				double dot = ux * this.velx + uy * this.vely;
				
				if (dot > 0)
				{
					double perpdot = ux * this.vely - uy * this.velx;
					
					this.velx = -uy * perpdot;
					this.vely = ux * perpdot;
				}
				
				nx = ux * this.theWorld.radius;
				ny = uy * this.theWorld.radius;
			}
			
			this.x = nx;
			this.y = ny;
		}
	}
	
	public void wallCollision(double dx, double dy)
	{
		
	}
	
	public void collision(WorldObject other, double distsqr, double dot)
	{
		
	}
	
	public World world()
	{
		return this.theWorld;
	}
	
	public boolean isRemoved()
	{
		return this.remove;
	}
	
	public abstract int getColor();
	
	public abstract double getHardness();
	
	@Override
	public int hashCode()
	{
		return this.id.hashCode();
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o instanceof WorldObject)
		{
			return this.id.equals(((WorldObject) o).id);
		}
		return false;
	}
	
	public void render()
	{
		
	}
}
