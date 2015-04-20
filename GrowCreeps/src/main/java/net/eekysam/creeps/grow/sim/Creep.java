package net.eekysam.creeps.grow.sim;

import java.util.Arrays;

import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.networks.BasicNetwork;

import net.eekysam.creeps.grow.CreepSpec;

public class Creep extends WorldObject
{
	public static final int defColor = 0x007000;
	
	public final CreepInfo info;
	public final CreepSpec spec;
	
	public final BasicNetwork ai;
	
	public double[] hits;
	
	public double rot;
	public double sin;
	public double cos;
	
	public double[] rayHit;
	
	public int myColor = defColor;
	
	public MLData output;
	public MLData input;
	
	public Creep(CreepSpec spec, CreepInfo info)
	{
		this.spec = spec;
		this.info = info;
		
		this.ai = (BasicNetwork) this.spec.network.clone();
		this.ai.clearContext();
		
		this.input = new BasicMLData(9);
	}
	
	@Override
	public void tick(double rate, EnumTickPass pass)
	{
		super.tick(rate, pass);
		if (pass == EnumTickPass.START)
		{
			this.hits = new double[4];
			Arrays.fill(this.hits, this.world().ambiHardness);
			
			this.sin = Math.sin(this.rot);
			this.cos = Math.cos(this.rot);
			
			this.rayHit = new double[4];
			this.rayHit[0] = Double.POSITIVE_INFINITY;
		}
		else if (pass == EnumTickPass.COMPUTE)
		{
			double toxwall;
			double toywall;
			
			if (this.cos < 0)
			{
				toxwall = this.x;
			}
			else
			{
				toxwall = this.world().width - this.x;
			}
			
			if (this.sin < 0)
			{
				toywall = this.y;
			}
			else
			{
				toywall = this.world().height - this.y;
			}
			
			double ltx = toxwall / this.cos;
			double lty = toywall / this.sin;
			
			if (lty < ltx || Double.isNaN(ltx))
			{
				
				this.addRayHit(toywall, this.world().wallColor);
			}
			else
			{
				this.addRayHit(toxwall, this.world().wallColor);
			}
			
			for (WorldObject obj : this.world().getObjects())
			{
				if (obj != this)
				{
					this.addRayHit(this.intersection(obj.x, obj.y, obj.radius), obj.getColor());
				}
			}
			
			this.input.clear();
			
			for (int i = 0; i < 4; i++)
			{
				this.input.add(i, this.hits[i]);
			}
			for (int i = 0; i < 4; i++)
			{
				this.input.add(i + 4, this.rayHit[i]);
			}
			this.input.add(8, 1.0);
			
			this.output = this.ai.compute(this.input);
		}
		else if (pass == EnumTickPass.APPLY)
		{
			this.rot += (this.output.getData(0) - 0.5) * 0.01;
			this.velx += this.cos * (this.output.getData(1) - 0.5) * 0.1;
			this.vely += this.sin * (this.output.getData(1) - 0.5) * 0.1;
			this.velx += this.sin * (this.output.getData(2) - 0.5) * 0.05;
			this.vely += this.cos * (this.output.getData(2) - 0.5) * 0.05;
			this.myColor = defColor;
			this.myColor |= ((int) (this.output.getData(3) * 255) & 0xFF) << 0;
			this.myColor |= ((int) (this.output.getData(4) * 255) & 0xFF) << 16;
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
		if (dx * this.cos - dy * this.sin > 0)
		{
			side += 2;
		}
		if (dx * this.sin + dy * this.cos > 0)
		{
			side += 1;
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
		if (dist < this.rayHit[0])
		{
			this.rayHit[0] = dist;
			this.rayHit[1] = ((color >> 0) & 0xFF) / 255.0;
			this.rayHit[2] = ((color >> 8) & 0xFF) / 255.0;
			this.rayHit[3] = ((color >> 16) & 0xFF) / 255.0;
		}
	}
	
	public double intersection(double h, double k, double r)
	{
		double tox = this.x - h;
		double toy = this.y - k;
		
		double c = tox * tox + toy * toy - r * r;
		
		if (c < 0)
		{
			return 0;
		}
		
		double b = 2 * this.cos * tox + 2 * this.sin * toy;
		
		double discr = b * b + 4 * c;
		
		if (discr < 0)
		{
			return Double.POSITIVE_INFINITY;
		}
		
		return 2 * c / (Math.sqrt(discr) - b);
	}
}
