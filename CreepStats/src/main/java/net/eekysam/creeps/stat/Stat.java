package net.eekysam.creeps.stat;

import java.util.Random;
import java.util.function.Supplier;

import org.apache.commons.math3.genetics.MutationPolicy;

import net.eekysam.creeps.evol.CreepMutation;
import net.eekysam.creeps.stat.CreepSolver.EvolveResult;

public enum Stat
{
	CONTROL("0")
	{
		@Override
		public Object[] variables()
		{
			return new Object[] { "control" };
		}
		
		@Override
		public Supplier<EvolveResult> runner(Object var, int lastSample)
		{
			return new Supplier<EvolveResult>()
			{
				CreepSolver run = new CreepSolver();
				
				@Override
				public EvolveResult get()
				{
					return this.run.run(crossRate, mutRate, elitRate, gens, lastSample);
				}
			};
		}
	},
	MUT_RATE("mutrate")
	{
		@Override
		public Object[] variables()
		{
			return new Object[] { 0.05, 0.1, 0.3, 0.5, 0.7, 0.9, 0.95 };
		}
		
		@Override
		public Supplier<EvolveResult> runner(Object var, int lastSample)
		{
			return new Supplier<EvolveResult>()
			{
				CreepSolver run = new CreepSolver();
				
				@Override
				public EvolveResult get()
				{
					return this.run.run(crossRate, (Double) var, elitRate, gens, lastSample);
				}
			};
		}
	},
	MUT_STR("mutstr")
	{
		@Override
		public Object[] variables()
		{
			return new Object[] { 0.05, 0.1, 0.15, 0.2, 0.3, 0.5 };
		}
		
		@Override
		public Supplier<EvolveResult> runner(Object var, int lastSample)
		{
			return new Supplier<EvolveResult>()
			{
				CreepSolver run = new CreepSolver()
				{
					@Override
					public Evolver makeEvolver()
					{
						return new Evolver()
						{
							@Override
							public MutationPolicy mutation()
							{
								return new CreepMutation(1, 3)
								{
									@Override
									public Double apply(Double past, Random rand)
									{
										double mult = rand.nextGaussian() * (Double) var;
										mult += 1;
										if (rand.nextFloat() < 0.05)
										{
											mult *= -1;
										}
										return past * mult + rand.nextGaussian() * (Double) var;
									}
								};
							}
						};
					}
				};
				
				@Override
				public EvolveResult get()
				{
					return this.run.run(crossRate, mutRate, elitRate, gens, lastSample);
				}
			};
		}
	};
	
	public final static double crossRate = 0.1;
	public final static double mutRate = 0.5;
	public final static double elitRate = 18.0 / 24;
	public final static int gens = 100;
	
	public final String filename;
	
	Stat(String filename)
	{
		this.filename = filename;
	}
	
	public abstract Object[] variables();
	
	public abstract Supplier<EvolveResult> runner(Object var, int lastSample);
}
