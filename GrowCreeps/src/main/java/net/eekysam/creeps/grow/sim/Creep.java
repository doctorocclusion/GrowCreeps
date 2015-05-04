package net.eekysam.creeps.grow.sim;

import java.util.Arrays;
import java.util.Random;

import net.eekysam.creeps.grow.CreepSpec;

import org.lwjgl.opengl.GL11;

public class Creep extends WorldObject
{
	public final CreepInfo info;
	public final CreepSpec spec;
	
	public double[] hits;
	
	public double rot;
	public double sin;
	public double cos;
	
	public double[] rayHit;
	
	public int myColor;
	
	public Creep(CreepSpec spec)
	{
		super(spec.radius);
		this.spec = spec;
		this.info = new CreepInfo();
		this.info.health = this.spec.maxHealth;
		this.info.food = this.spec.startingFood;
		this.myColor = this.spec.baseColor;
	}
	
	@Override
	public void tick(double speed, EnumTickPass pass)
	{
		super.tick(speed, pass);
		if (pass == EnumTickPass.START)
		{
			this.hits = new double[4];
			Arrays.fill(this.hits, this.world().ambiHardness);
			
			this.sin = Math.sin(this.rot);
			this.cos = Math.cos(this.rot);
			
			this.rayHit = new double[4];
			this.rayHit[0] = Double.POSITIVE_INFINITY;
		}
		if (pass == EnumTickPass.MOVE)
		{
			double f = 1 - this.spec.fric;
			double mvel = this.spec.maxVel;
			if (speed != 1.0)
			{
				f = Math.exp(Math.log(f) * speed);
			}
			this.velx *= f;
			this.vely *= f;
			double vel = this.velx * this.velx + this.vely * this.vely;
			if (vel > mvel * mvel)
			{
				double scale = mvel / Math.sqrt(vel);
				this.velx *= scale;
				this.vely *= scale;
			}
		}
		else if (pass == EnumTickPass.COMPUTE)
		{
			double wallDist = this.intersection(0, 0, this.world().radius, 1);
			if (!Double.isFinite(wallDist))
			{
				wallDist = 0;
			}
			this.addRayHit(wallDist, this.world().wallColor);
			
			for (WorldObject obj : this.world().getObjects())
			{
				if (obj != this)
				{
					this.addRayHit(this.intersection(obj.x, obj.y, obj.radius, -1), obj.getColor());
				}
			}
		}
		else if (pass == EnumTickPass.LAST)
		{
			this.info.age++;
			this.info.adjustedAge += speed;
			
			if (this.info.food <= 0)
			{
				this.info.food = 0;
				this.damage(EnumDmgType.STARVE, 2 * speed);
			}
			else
			{
				double velDec = (this.velx * this.velx + this.vely * this.vely) / (this.spec.maxVel * this.spec.maxVel);
				velDec -= 0.5;
				if (this.cos * this.velx + this.sin * this.vely < 0)
				{
					velDec *= 1.5;
				}
				velDec *= 0.5;
				velDec += 1;
				this.info.food -= 0.1 * velDec * speed;
				this.info.foodLost += 0.1 * velDec * speed;
			}
			
			if (this.info.health <= 0)
			{
				this.kill();
				Random rand = new Random();
				FoodObject meat = new FoodObject(1.5 * this.info.food / this.spec.maxFood + Math.max(rand.nextGaussian() * 1 + 2.5, 0));
				meat.x = this.x;
				meat.y = this.y;
				meat.spawn(this.world());
			}
		}
	}
	
	@Override
	public void collision(WorldObject other, double distsqr, double dot)
	{
		super.collision(other, distsqr, dot);
		this.addHit(other.x - this.x, other.y - this.y, other.getHardness());
	}
	
	@Override
	public void wallCollision(double dx, double dy)
	{
		this.addHit(dx, dy, 0.5);
	}
	
	public void addHit(double dx, double dy, double hardness)
	{
		int side = 0;
		if (dx * this.sin - dy * this.cos > 0)
		{
			side += 1;
		}
		if (dx * this.cos + dy * this.sin < 0)
		{
			side += 2;
		}
		
		if (this.hits[side] < hardness)
		{
			this.hits[side] = hardness;
		}
	}
	
	@Override
	public int getColor()
	{
		return this.myColor;
	}
	
	@Override
	public double getHardness()
	{
		return 0.7;
	}
	
	public void addRayHit(double dist, int color)
	{
		if (dist < this.rayHit[0] && dist > 0)
		{
			this.rayHit[0] = dist;
			this.rayHit[1] = ((color >> 16) & 0xFF) / 255.0;
			this.rayHit[2] = ((color >> 8) & 0xFF) / 255.0;
			this.rayHit[3] = ((color >> 0) & 0xFF) / 255.0;
		}
	}
	
	public double intersection(double h, double k, double r, int dir)
	{
		double tox = this.x - h;
		double toy = this.y - k;
		
		double c = tox * tox + toy * toy - r * r;
		
		double b = 2 * this.cos * tox + 2 * this.sin * toy;
		
		double discr = b * b - 4 * c;
		
		if (discr < 0)
		{
			return Double.NaN;
		}
		
		return 2 * c / (-b - dir * Math.sqrt(discr));
	}
	
	public void damage(EnumDmgType type, double dmg)
	{
		this.info.health = Math.max(0, this.info.health - dmg);
		this.info.dmgTaken.put(type, this.info.dmgTaken.get(type) + dmg);
	}
	
	@Override
	public void render()
	{
		int c = this.getColor();
		
		double r = ((c >> 16) & 0xFF) / 255.0;
		double g = ((c >> 8) & 0xFF) / 255.0;
		double b = ((c >> 0) & 0xFF) / 255.0;
		
		GL11.glColor3d(r, g, b);
		World.renderCircle(this.x, this.y, this.radius, 16, 0, 2 * Math.PI);
		
		double v = 1 - this.hits[0];
		GL11.glColor3d(v, v, v);
		World.renderCircle(this.x, this.y, this.radius, 2, World.rad(30) + this.rot, World.rad(60) + this.rot);
		
		v = 1 - this.hits[1];
		GL11.glColor3d(v, v, v);
		World.renderCircle(this.x, this.y, this.radius, 2, World.rad(-60) + this.rot, World.rad(-30) + this.rot);
		
		v = 1 - this.hits[2];
		GL11.glColor3d(v, v, v);
		World.renderCircle(this.x, this.y, this.radius, 2, World.rad(110) + this.rot, World.rad(140) + this.rot);
		
		v = 1 - this.hits[3];
		GL11.glColor3d(v, v, v);
		World.renderCircle(this.x, this.y, this.radius, 2, World.rad(-140) + this.rot, World.rad(-110) + this.rot);
		
		GL11.glColor3d(this.rayHit[1], this.rayHit[2], this.rayHit[3]);
		World.renderCircle(this.x, this.y, this.radius * 1.2, 2, World.rad(-15) + this.rot, World.rad(15) + this.rot);
		
		/*
		double rad = this.rayHit[0];
		GL11.glColor3d(1, 1, 1);
		World.renderCircle(this.x, this.y, rad, 5, (this.radius * World.rad(-15)) / rad + this.rot, (this.radius * World.rad(15)) / rad + this.rot);
		*/
		
		GL11.glColor3d(0, 0, 0);
		World.renderCircle(this.x, this.y, this.radius * 0.8, 12, 0, 2 * Math.PI);
		GL11.glColor3d(1, 1, 1);
		World.renderCircle(this.x, this.y, this.radius * 0.6, 10, this.rot, (this.info.food / this.spec.maxFood) * 2 * Math.PI + this.rot);
		GL11.glColor3d(0, 0, 0);
		World.renderCircle(this.x, this.y, this.radius * 0.4, 10, 0, 2 * Math.PI);
		GL11.glColor3d(1, 1, 1);
		World.renderCircle(this.x, this.y, this.radius * 0.3, 10, this.rot, (this.info.health / this.spec.maxHealth) * 2 * Math.PI + this.rot);
	}
}
