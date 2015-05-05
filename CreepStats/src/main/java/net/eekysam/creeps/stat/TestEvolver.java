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
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.encog.engine.network.activation.ActivationFunction;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
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
import net.eekysam.creeps.grow.sim.WorldObject;

public class TestEvolver extends CreepEvolver
{
	public boolean lockRender = false;
	public int tournamentSize = 6;
	public double crossoverRatio = 0.8;
	public int simSize = 8;
	public int populationSize = 24;
	public int generations = 200;
	public Predicate<Integer> doRender = new Predicate<Integer>()
	{
		List<Integer> at = Lists.newArrayList(0, 1, 10, 20, 50, 100, 150, 200);
		
		@Override
		public boolean test(Integer gen)
		{
			return this.at.contains(gen);
		}
	};
	
	public ArrayList<SummaryStatistics> runStats;
	public SummaryStatistics allStats;
	public SimpleRegression meanRegression;
	
	public Supplier<CreepChrom> getSupplier()
	{
		CreepSpec spec = new CreepSpec();
		/*
		JordanPattern patt = new JordanPattern();
		patt.setInputNeurons(10);
		patt.setOutputNeurons(3);
		patt.addHiddenLayer(20);
		patt.setActivationFunction(new ActivationSigmoid());
		BasicNetwork net = (BasicNetwork) patt.generate();
		*/
		BasicNetwork net = this.makeNetwork();
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
	
	public BasicNetwork makeNetwork()
	{
		ActivationFunction active = new ActivationSigmoid();
		BasicLayer layer1;
		BasicLayer layer2;
		BasicNetwork network = new BasicNetwork();
		network.addLayer(new BasicLayer(null, true, 10));
		network.addLayer(layer1 = new BasicLayer(active, true, 12));
		network.addLayer(layer2 = new BasicLayer(active, true, 8));
		network.addLayer(new BasicLayer(active, false, 3));
		layer1.setContextFedBy(layer2);
		network.getStructure().finalizeStructure();
		network.reset();
		return network;
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
		return new CreepMutation(3, 8)
		{
			@Override
			public Double apply(Double past, Random rand)
			{
				double mult = rand.nextDouble() * 2 - 1;
				mult *= 0.2;
				mult += 1;
				if (rand.nextFloat() < 0.2)
				{
					mult *= -1;
				}
				return past * mult + (rand.nextDouble() * 2 - 1) * 0.2;
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
	public void startSimulation()
	{
		this.runStats = new ArrayList<>();
		this.allStats = new SummaryStatistics();
		this.meanRegression = new SimpleRegression();
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
		
		//Summary Stats
		SummaryStatistics stats = new SummaryStatistics();
		this.runStats.add(stats);
		
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
			int n = 6;
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
					runpop[i].fitness += (info.fitness()) / n;
					stats.addValue(info.fitness());
				}
			}
			k++;
		}
		
		this.allStats.addValue(stats.getMean());
		this.meanRegression.addData(generation, stats.getMean());
	}
	
	public void runWorld(Creep[] creeps, boolean render, int generation)
	{
		this.startWorld(render, generation);
		double rad = 150;
		double speed = 4;
		Random rand = new Random();
		World world = new World(rad, speed, render)
		{
			@Override
			public void spawnNew()
			{
				double total = 0;
				for (WorldObject obj : this.getObjects())
				{
					if (obj instanceof FoodObject)
					{
						total += obj.radius;
					}
				}
				if (this.rand.nextDouble() * (85 / this.speed + total * 0.4) < 1 && total < this.countCreeps() * 120)
				{
					FoodObject food = new FoodObject(Math.max(4 + this.rand.nextGaussian() * 1.2, 1));
					food.spawn(this);
					food.randomLoc(this.rand);
				}
			}
		};
		
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
		
		for (int i = 0; i < rand.nextInt(4) + 7; i++)
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
			if (world.doRender)
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
