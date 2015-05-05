package net.eekysam.creeps.grow.sim;

import java.util.Random;
import java.util.UUID;
import java.util.function.Function;

import org.lwjgl.opengl.GL11;

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
	
	public double speed;
	
	public WorldObject(double radius)
	{
		this.id = UUID.randomUUID();
		this.radius = radius;
	}
	
	public void spawn(World world)
	{
		if (this.theWorld == null)
		{
			this.theWorld = world;
			this.speed = this.theWorld.speed;
			this.theWorld.addSpawn(this);
		}
	}
	
	public void tick(EnumTickPass pass)
	{
		if (pass == EnumTickPass.MOVE)
		{
			double nx = this.x += this.velx * this.speed;
			double ny = this.y += this.vely * this.speed;
			
			double dist = ny * ny + nx * nx;
			
			if (dist > (this.theWorld.radius - 0.02) * (this.theWorld.radius - 0.02))
			{
				if (World.solidWalls)
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
					
					nx = ux * (this.theWorld.radius - 0.01);
					ny = uy * (this.theWorld.radius - 0.01);
				}
				else
				{
					nx = nx * -0.96;
					ny = ny * -0.96;
				}
			}
			
			this.x = nx;
			this.y = ny;
		}
	}
	
	public void wallCollision(double dx, double dy)
	{
		
	}
	
	public void collision(WorldObject other, double distsqr, double velx, double vely)
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
	
	public void kill()
	{
		this.remove = true;
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
		int c = this.getColor();
		
		double r = ((c >> 16) & 0xFF) / 255.0;
		double g = ((c >> 8) & 0xFF) / 255.0;
		double b = ((c >> 0) & 0xFF) / 255.0;
		
		GL11.glColor3d(r, g, b);
		
		World.renderCircle(this.x, this.y, this.radius, 12, 0, 2 * Math.PI);
	}
	
	public void randomLoc(Random rand, Function<Double, Double> radSmudge)
	{
		double theta = rand.nextDouble() * 2 * Math.PI;
		double rad = rand.nextDouble();
		rad = radSmudge.apply(rad) * this.theWorld.radius;
		this.x = Math.sin(theta) * rad;
		this.y = Math.cos(theta) * rad;
	}
	
	public void randomLoc(Random rand)
	{
		this.randomLoc(rand, new Function<Double, Double>()
		{
			@Override
			public Double apply(Double rad)
			{
				return rad;
			}
		});
	}
}
