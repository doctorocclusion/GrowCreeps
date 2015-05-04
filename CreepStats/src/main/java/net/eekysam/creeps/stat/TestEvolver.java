package net.eekysam.creeps.stat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.commons.math3.genetics.Chromosome;
import org.apache.commons.math3.genetics.CrossoverPolicy;
import org.apache.commons.math3.genetics.FixedGenerationCount;
import org.apache.commons.math3.genetics.MutationPolicy;
import org.apache.commons.math3.genetics.SelectionPolicy;
import org.apache.commons.math3.genetics.StoppingCondition;
import org.apache.commons.math3.genetics.TournamentSelection;
import org.apache.commons.math3.genetics.UniformCrossover;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.pattern.ElmanPattern;

import com.google.common.collect.Lists;

import net.eekysam.creeps.evol.CreepChrom;
import net.eekysam.creeps.evol.CreepEvolver;
import net.eekysam.creeps.evol.CreepMutation;
import net.eekysam.creeps.grow.CreepSpec;
import net.eekysam.creeps.grow.sim.AICreep;
import net.eekysam.creeps.grow.sim.Creep;
import net.eekysam.creeps.grow.sim.CreepInfo;
import net.eekysam.creeps.grow.sim.FoodObject;
import net.eekysam.creeps.grow.sim.SpikeObject;
import net.eekysam.creeps.grow.sim.World;

public class TestEvolver extends CreepEvolver
{
	public boolean lockRender = false;
	public int tournamentSize = 4;
	public double crossoverRatio = 0.6;
	public int simSize = 6;
	public int populationSize = 24;
	public int generations = 1000;
	public Predicate<Integer> doRender = new Predicate<Integer>()
	{
		List<Integer> at = Lists.newArrayList(0, 1, 10, 20, 50, 100, 150, 200, 300, 500, 750, 1000);
		
		@Override
		public boolean test(Integer gen)
		{
			return this.at.contains(gen);
		}
	};
	
	public Supplier<CreepChrom> getSupplier()
	{
		CreepSpec spec = new CreepSpec();
		ElmanPattern elman = new ElmanPattern();
		elman.setInputNeurons(10);
		elman.setOutputNeurons(5);
		elman.addHiddenLayer(15);
		elman.setActivationFunction(new ActivationSigmoid());
		BasicNetwork net = (BasicNetwork) elman.generate();
		return new Supplier<CreepChrom>()
		{
			@Override
			public CreepChrom get()
			{
				net.reset();
				return CreepChrom.make(spec, net);
			}
		};
	}
	
	@Override
	public List<Chromosome> getInitial()
	{
		Supplier<CreepChrom> sup = this.getSupplier();
		ArrayList<Chromosome> chroms = new ArrayList<>(this.populationSize);
		for (int i = 0; i < this.populationSize; i++)
		{
			chroms.add(sup.get());
		}
		return chroms;
	}
	
	@Override
	public CrossoverPolicy crossover()
	{
		return new UniformCrossover<Double>(this.crossoverRatio);
	}
	
	@Override
	public MutationPolicy mutation()
	{
		return new CreepMutation(5, 8)
		{
			@Override
			public Double apply(Double past, Random rand)
			{
				double mult = rand.nextDouble() * 2 - 1;
				mult *= 1.8;
				return past * mult + (rand.nextDouble() * 2 - 1) * 0.5;
			}
		};
	}
	
	@Override
	public SelectionPolicy selection()
	{
		return new TournamentSelection(this.tournamentSize);
	}
	
	@Override
	public StoppingCondition condition()
	{
		return new FixedGenerationCount(this.generations);
	}
	
	@Override
	public void simulate(int generation, List<CreepChrom> chroms)
	{
		//Make sure that the number of chroms is a multiple of the simSize
		int num = chroms.size();
		Random rand = new Random();
		int rem = num % this.simSize;
		for (int i = 0; i < rem; i++)
		{
			chroms.add(chroms.get(rand.nextInt(num)));
		}
		
		//Shuffle the chroms
		Collections.shuffle(chroms);
		
		Iterator<CreepChrom> it = chroms.iterator();
		int k = 0;
		while (it.hasNext())
		{
			//Make arrays of creeps for sim
			Creep[] creeps = new Creep[this.simSize];
			CreepChrom[] runpop = new CreepChrom[creeps.length];
			int i = 0;
			while (i < creeps.length)
			{
				//Will exist because of the stuff we did at the start
				CreepChrom chrom = it.next();
				runpop[i] = chrom;
				runpop[i].fitness = 0;
				i++;
			}
			int n = 4;
			boolean render = this.doRender.test(generation) || this.lockRender;
			for (int j = 0; j < n; j++)
			{
				for (i = 0; i < creeps.length; i++)
				{
					CreepChrom chrom = runpop[i];
					creeps[i] = new AICreep(chrom.spec, chrom.brain);
				}
				this.runWorld(creeps, render && j == 0 && k == 0, generation);
				for (i = 0; i < creeps.length; i++)
				{
					CreepInfo info = creeps[i].info;
					runpop[i].fitness += (info.adjustedAge + info.food * 12 + info.health) / n;
				}
			}
			k++;
		}
	}
	
	public void runWorld(Creep[] creeps, boolean render, int generation)
	{
		this.startWorld(render, generation);
		double rad = 150;
		double speed = 4;
		Random rand = new Random();
		World world = new World(rad, speed, render);
		
		for (Creep creep : creeps)
		{
			creep.spawn(world);
			creep.randomLoc(rand);
		}
		
		for (int i = 0; i < rand.nextInt(4) + 7; i++)
		{
			FoodObject food = new FoodObject(Math.max(5 + rand.nextGaussian() * 1.2, 1));
			food.spawn(world);
			food.randomLoc(rand);
		}
		
		for (int i = 0; i < rand.nextInt(3) + 5; i++)
		{
			SpikeObject spike = new SpikeObject(Math.max(5 + rand.nextGaussian() * 1.2, 1));
			spike.spawn(world);
			spike.randomLoc(rand, new Function<Double, Double>()
			{
				@Override
				public Double apply(Double r)
				{
					return 1 - (r * r);
				}
			});
		}
		
		double time = 0;
		while (world.countCreeps() > 0 && time < 20000)
		{
			if (render)
			{
				this.preRenderTick(time, world);
				world.tick();
				this.postRenderTick();
			}
			else
			{
				world.tick();
			}
			time += speed;
		}
	}
	
	public void startWorld(boolean render, int generation)
	{
	}
	
	public void preRenderTick(double time, World world)
	{
	}
	
	public void postRenderTick()
	{
	}
}
