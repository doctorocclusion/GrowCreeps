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
	
	public AICreep(CreepSpec spec, BasicNetwork network)
	{
		super(spec);
		
		this.ai = (BasicNetwork) network.clone();
		this.ai.clearContext();
		
		this.input = new BasicMLData(11);
	}
	
	public double norm(double in)
	{
		double out = 1 / (1 + Math.exp(-in));
		return out * 2 - 1;
	}
	
	@Override
	public void tick(double rate, EnumTickPass pass)
	{
		super.tick(rate, pass);
		if (pass == EnumTickPass.COMPUTE)
		{
			this.input.clear();
			
			for (int i = 0; i < 4; i++)
			{
				this.input.add(i, 1 - this.hits[i] - 0.5);
			}
			for (int i = 0; i < 4; i++)
			{
				this.input.add(i + 4, 1 - this.rayHit[i] - 0.5);
			}
			this.input.add(8, (this.info.food / this.spec.maxFood) - 0.5);
			this.input.add(9, (this.info.health / this.spec.maxHealth) - 0.5);
			
			this.output = this.ai.compute(this.input);
		}
		else if (pass == EnumTickPass.LAST)
		{
			double drot = this.norm(this.output.getData(0));
			if (Math.abs(drot) > 0.2)
			{
				drot -= Math.signum(drot) * 0.2;
				drot /= 0.8;
				this.rot += drot * this.spec.rotSpeed * rate;
			}
			this.velx += this.cos * this.norm(this.output.getData(1)) * this.spec.accel * rate;
			this.vely += this.sin * this.norm(this.output.getData(1)) * this.spec.accel * rate;
			this.velx += -this.sin * this.norm(this.output.getData(2)) * this.spec.accelSide * rate;
			this.vely += this.cos * this.norm(this.output.getData(2)) * this.spec.accelSide * rate;
			this.myColor = this.spec.baseColor;
			this.myColor |= ((int) (this.norm(this.output.getData(3)) * 128 + 64) & 0xFF) << 16;
			this.myColor |= ((int) (this.norm(this.output.getData(4)) * 128 + 64) & 0xFF) << 8;
		}
	}
}
