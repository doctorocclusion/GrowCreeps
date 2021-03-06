package net.eekysam.creeps.stat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.commons.math3.genetics.Chromosome;
import org.apache.commons.math3.genetics.CrossoverPolicy;
import org.apache.commons.math3.genetics.MutationPolicy;
import org.apache.commons.math3.genetics.SelectionPolicy;
import org.apache.commons.math3.genetics.TournamentSelection;
import org.apache.commons.math3.genetics.UniformCrossover;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.stat.descriptive.UnivariateStatistic;
import org.apache.commons.math3.stat.descriptive.rank.Median;
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
import net.eekysam.creeps.grow.sim.FoodObject;
import net.eekysam.creeps.grow.sim.SpikeObject;
import net.eekysam.creeps.grow.sim.World;
import net.eekysam.creeps.grow.sim.WorldObject;

public class Evolver extends CreepEvolver
{
	public class WorldRun
	{
		public final int generation;
		public final int set;
		public final int run;
		
		public WorldRun(int generation, int set, int run)
		{
			this.generation = generation;
			this.set = set;
			this.run = run;
		}
	}
	
	public int tournamentSize = 4;
	public double crossoverRatio = 0.98;
	public int simSize = 1;
	public int populationSize = 24;
	
	public UnivariateStatistic center = new Median();
	
	public Predicate<WorldRun> doRender = new Predicate<WorldRun>()
	{
		@Override
		public boolean test(WorldRun run)
		{
			return false;
		}
	};
	
	public ArrayList<DescriptiveStatistics> generationStats;
	public SummaryStatistics allStats;
	public SummaryStatistics meanStats;
	public SummaryStatistics maxStats;
	public SimpleRegression allRegression;
	public SimpleRegression meanRegression;
	public SimpleRegression maxRegression;
	
	public Supplier<CreepChrom> getSupplier()
	{
		CreepSpec spec = new CreepSpec();
		/*
		 * JordanPattern patt = new JordanPattern();
		 * patt.setInputNeurons(10);
		 * patt.setOutputNeurons(3);
		 * patt.addHiddenLayer(20);
		 * patt.setActivationFunction(new ActivationSigmoid());
		 * BasicNetwork net = (BasicNetwork) patt.generate();
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
		network.addLayer(new BasicLayer(null, true, 8));
		network.addLayer(layer1 = new BasicLayer(active, true, 12));
		network.addLayer(layer2 = new BasicLayer(active, true, 8));
		network.addLayer(new BasicLayer(active, false, 4));
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
		return new CreepMutation(1, 3)
		{
			@Override
			public Double apply(Double past, Random rand)
			{
				double mult = rand.nextGaussian() * 0.15;
				mult += 1;
				if (rand.nextFloat() < 0.05)
				{
					mult *= -1;
				}
				return past * mult + rand.nextGaussian() * 0.15;
			}
		};
	}
	
	@Override
	public SelectionPolicy selection()
	{
		return new TournamentSelection(this.tournamentSize);
	}
	
	@Override
	public void startSimulation()
	{
		this.generationStats = new ArrayList<>();
		this.allStats = new SummaryStatistics();
		this.meanStats = new SummaryStatistics();
		this.maxStats = new SummaryStatistics();
		this.allRegression = new SimpleRegression();
		this.meanRegression = new SimpleRegression();
		this.maxRegression = new SimpleRegression();
	}
	
	@Override
	public void simulate(int generation, List<CreepChrom> chroms)
	{
		this.simulate(generation, chroms, 16);
	}
	
	public void simulate(int generation, List<CreepChrom> chroms, int n)
	{
		DescriptiveStatistics stats = new DescriptiveStatistics();
		this.generationStats.add(stats);
		
		double[][] fitness = this.simulateWorld(chroms, generation, n);
		
		for (int i = 0; i < chroms.size(); i++)
		{
			CreepChrom chrom = chroms.get(i);
			chrom.fitness = this.center.evaluate(fitness[i]);
			stats.addValue(chrom.fitness);
			this.allStats.addValue(chrom.fitness);
			this.allRegression.addData(generation, chrom.fitness);
		}
		
		this.meanRegression.addData(generation, stats.getMean());
		this.maxRegression.addData(generation, stats.getMax());
		this.meanStats.addValue(stats.getMean());
		this.maxStats.addValue(stats.getMax());
		
		this.endGeneration(generation, stats);
	}
	
	public double[][] simulateWorld(List<CreepChrom> chroms, int n)
	{
		return this.simulateWorld(chroms, -1, n);
	}
	
	public double[][] simulateWorld(List<CreepChrom> chroms, int generation, int n)
	{
		Random rand = new Random(347527825);
		
		//Random rand = new Random(347527825 + (generation / 10) % 5);
		//rand.setSeed(rand.nextLong());
		
		//Random rand = new Random(347527829);
		//rand.setSeed(rand.nextLong());
		
		//Random rand = new Random();
		long seed = rand.nextLong();
		
		//Make sure that the number of chroms is a multiple of the simSize
		int num = chroms.size();
		int rem = num % this.simSize;
		for (int i = 0; i < rem; i++)
		{
			chroms.add(chroms.get(rand.nextInt(num)));
		}
		
		double[][] fitness = new double[chroms.size()][n];
		
		Iterator<CreepChrom> it = chroms.iterator();
		int k = 0;
		while (it.hasNext())
		{
			Random rand1 = new Random(seed);
			
			//Make arrays of creeps for sim
			Creep[] creeps = new Creep[this.simSize];
			CreepChrom[] runpop = new CreepChrom[creeps.length];
			for (int i = 0; i < creeps.length; i++)
			{
				//Will exist because of the stuff we did at the start
				runpop[i] = it.next();
			}
			for (int j = 0; j < n; j++)
			{
				WorldRun run = null;
				if (generation != -1)
				{
					run = new WorldRun(generation, k, j);
				}
				for (int i = 0; i < creeps.length; i++)
				{
					CreepChrom chrom = runpop[i];
					creeps[i] = new AICreep(chrom.spec, chrom.brain);
				}
				this.runWorld(creeps, run, new Random(rand1.nextLong()));
				for (int i = 0; i < creeps.length; i++)
				{
					fitness[i + k * creeps.length][j] = creeps[i].info.fitness();
				}
			}
			k++;
		}
		
		return fitness;
	}
	
	public double[] simulateWorld(CreepChrom creep, int generation, int n)
	{
		return this.simulateWorld(Lists.newArrayList(creep), generation, n)[0];
	}
	
	public double[] simulateWorld(CreepChrom creep, int n)
	{
		return this.simulateWorld(creep, -1, n);
	}
	
	public void runWorld(Creep[] creeps, WorldRun run, Random rand)
	{
		double rad = 100;
		double speed = 4;
		World world = new World(rad, speed, false, rand)
		{
			@Override
			public void spawnNew()
			{
				double total = 0;
				for (WorldObject obj : this.getObjects())
				{
					if (obj instanceof FoodObject)
					{
						total += obj.radius * 1.5;
					}
				}
				if (this.rand.nextDouble() * (60 + total * 1.0) < 1 * this.speed && total < this.countCreeps() * 40)
				{
					FoodObject food = Evolver.this.genFood(this.rand);
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
		
		for (int i = 0; i < 3; i++)
		{
			FoodObject food = this.genFood(rand);
			food.spawn(world);
			food.randomLoc(rand, new Function<Double, Double>()
			{
				@Override
				public Double apply(Double r)
				{
					return r * r;
				}
			});
		}
		
		for (int i = 0; i < 4; i++)
		{
			SpikeObject spike = new SpikeObject(Math.max(5 + rand.nextGaussian() * 1.0, 1));
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
		while (world.countCreeps() > 0 && time < 6000)
		{
			if (run != null)
			{
				world.doRender = this.doRender.test(run);
			}
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
	
	public void endGeneration(int generation, DescriptiveStatistics stats)
	{
	}
	
	public void preRenderTick(double time, World world)
	{
	}
	
	public void postRenderTick()
	{
	}
	
	public FoodObject genFood(Random rand)
	{
		return new FoodObject(Math.max(5 + rand.nextGaussian() * 1.0, 1));
	}
}
