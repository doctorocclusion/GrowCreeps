package net.eekysam.creeps.grow.sim;

import net.eekysam.creeps.grow.CreepSpec;

import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.networks.BasicNetwork;

public class AICreep extends Creep
{
	public final BasicNetwork ai;
	
	public MLData output;
	public MLData input;
	
	public double lastPress1;
	
	public AICreep(CreepSpec spec, BasicNetwork network)
	{
		super(spec);
		
		this.ai = (BasicNetwork) network.clone();
		this.ai.clearContext();
		
		this.input = new BasicMLData(11);
	}
	
	public double norm(double in)
	{
		double out = in;//1 / (1 + Math.exp(-in));
		return out * 2 - 1;
	}
	
	@Override
	public void tick(EnumTickPass pass)
	{
		super.tick(pass);
		if (pass == EnumTickPass.COMPUTE)
		{
			this.input.clear();
			
			for (int i = 0; i < 4; i++)
			{
				this.input.add(i, this.hits[i]);
			}
			for (int i = 0; i < 4; i++)
			{
				this.input.add(i + 4, this.rayHit[i]);
			}
			//this.input.add(8, this.info.food / this.spec.maxFood);
			//this.input.add(9, this.info.health / this.spec.maxHealth);
			
			this.output = this.ai.compute(this.input);
		}
		else if (pass == EnumTickPass.LAST)
		{
			double drot = this.norm(this.output.getData(0));
			if (Math.abs(drot) > 0.2)
			{
				drot -= Math.signum(drot) * 0.2;
				drot /= 0.8;
				this.rot += drot * this.spec.rotSpeed * this.speed;
			}
			this.velx += this.cos * this.output.getData(1) * this.spec.accel * this.speed;
			this.vely += this.sin * this.output.getData(1) * this.spec.accel * this.speed;
			this.velx += -this.sin * this.norm(this.output.getData(2)) * this.spec.accelSide * this.speed;
			this.vely += this.cos * this.norm(this.output.getData(2)) * this.spec.accelSide * this.speed;
			
			double press1 = this.norm(this.output.getData(3));
			this.action1 += (press1 - this.lastPress1) * 5;
			if (this.action1 > 1)
			{
				this.action1 = 1;
			}
			if (this.action1 < 0)
			{
				this.action1 = 0;
			}
			this.action1 *= 0.95;
			this.lastPress1 = press1;
			
			/*
			double press2 = this.norm(this.output.getData(4)) * 5;
			this.action2 += (press2 - this.lastPress1);
			if (this.action1 > 1)
			{
				this.action1 = 1;
			}
			if (this.action1 < -1)
			{
				this.action1 = -1;
			}
			this.action1 *= 0.95;
			this.lastPress1 = press2;
			*/
			
			this.myColor = this.spec.baseColor;
			//this.myColor |= ((int) (this.norm(this.output.getData(3)) * 128 + 64) & 0xFF) << 16;
			//this.myColor |= ((int) (this.norm(this.output.getData(4)) * 128 + 64) & 0xFF) << 8;
		}
	}
}
