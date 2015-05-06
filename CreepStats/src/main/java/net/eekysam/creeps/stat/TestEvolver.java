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
import org.apache.commons.math3.genetics.MutationPolicy;
import org.apache.commons.math3.genetics.SelectionPolicy;
import org.apache.commons.math3.genetics.TournamentSelection;
import org.apache.commons.math3.genetics.UniformCrossover;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.stat.descriptive.UnivariateStatistic;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.encog.engine.network.activation.ActivationFunction;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;

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

public class TestEvolver extends CreepEvolver
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
	
	public int tournamentSize = 5;
	public double crossoverRatio = 0.8;
	public int simSize = 8;
	public int populationSize = 24;
	
	public UnivariateStatistic center = new Mean();
	
	public Predicate<WorldRun> doRender = new Predicate<WorldRun>()
	{
		@Override
		public boolean test(WorldRun run)
		{
			return false;
		}
	};
	
	public ArrayList<SummaryStatistics> generationStats;
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
		return new CreepMutation(1, 8)
		{
			@Override
			public Double apply(Double past, Random rand)
			{
				double mult = rand.nextDouble() * 2 - 1;
				mult *= 0.4;
				mult += 1;
				if (rand.nextFloat() < 0.2)
				{
					mult *= -1;
				}
				return past * mult + (rand.nextDouble() * 2 - 1) * 0.35;
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
		int n = 16;
		
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
		this.generationStats.add(stats);
		
		double[][] fitness = new double[chroms.size()][n];
		
		Iterator<CreepChrom> it = chroms.iterator();
		int k = 0;
		while (it.hasNext())
		{
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
				WorldRun run = new WorldRun(generation, k, j);
				for (int i = 0; i < creeps.length; i++)
				{
					CreepChrom chrom = runpop[i];
					creeps[i] = new AICreep(chrom.spec, chrom.brain);
				}
				this.runWorld(creeps, run);
				for (int i = 0; i < creeps.length; i++)
				{
					fitness[i + k * creeps.length][j] = creeps[i].info.fitness();
				}
			}
			k++;
		}
		
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
	
	public void runWorld(Creep[] creeps, WorldRun run)
	{
		double rad = 150;
		double speed = 4;
		Random rand = new Random();
		World world = new World(rad, speed, false)
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
		
		for (int i = 0; i < 8; i++)
		{
			FoodObject food = new FoodObject(Math.max(5 + rand.nextGaussian() * 1.2, 1));
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
		
		for (int i = 0; i < 10; i++)
		{
			SpikeObject spike = new SpikeObject(Math.max(6 + rand.nextGaussian() * 1.3, 1));
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
			world.doRender = this.doRender.test(run);
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
	
	public void endGeneration(int generation, SummaryStatistics stats)
	{
	}
	
	public void preRenderTick(double time, World world)
	{
	}
	
	public void postRenderTick()
	{
	}
}
