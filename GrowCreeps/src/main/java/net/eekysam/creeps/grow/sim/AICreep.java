package net.eekysam.creeps.grow.sim;

import net.eekysam.creeps.grow.CreepSpec;

import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.networks.BasicNetwork;

public class AICreep extends Creep
{
	public final BasicNetwork ai;
	
	public final CreepInfo info;
	public final CreepSpec spec;
	
	public MLData output;
	public MLData input;
	
	public AICreep(CreepSpec spec, CreepInfo info)
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
			this.input.add(8, 1.0);
			
			this.output = this.ai.compute(this.input);
		}
		else if (pass == EnumTickPass.APPLY)
		{
			this.rot += (this.output.getData(0) - 0.5) * rotSpeed * rate;
			this.velx += this.cos * (this.output.getData(1) - 0.5) * acc * rate;
			this.vely += this.sin * (this.output.getData(1) - 0.5) * acc * rate;
			this.velx += this.sin * (this.output.getData(2) - 0.5) * sideAcc * rate;
			this.vely += this.cos * (this.output.getData(2) - 0.5) * sideAcc * rate;
			this.myColor = defColor;
			this.myColor |= ((int) (this.output.getData(3) * 255) & 0xFF) << 0;
			this.myColor |= ((int) (this.output.getData(4) * 255) & 0xFF) << 16;
		}
	}
}
