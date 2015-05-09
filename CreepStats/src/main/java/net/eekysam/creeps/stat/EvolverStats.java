package net.eekysam.creeps.stat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Supplier;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.google.gson.stream.JsonWriter;

import net.eekysam.creeps.stat.CreepSolver.EvolveResult;

public class EvolverStats
{
	public class StatRunner implements Runnable
	{
		public EvolveResult result = null;
		public final int number;
		
		public StatRunner(int number)
		{
			this.number = number;
		}
		
		@Override
		public void run()
		{
			this.result = EvolverStats.this.evolver.get();
		}
	}
	
	private ArrayList<StatRunner> runs = new ArrayList<>();
	private int done = 0;
	private int started = 0;
	private int count = 0;
	private int threads = 0;
	
	public final Supplier<EvolveResult> evolver;
	
	public EvolverStats(Supplier<EvolveResult> evolver)
	{
		this.evolver = evolver;
	}
	
	public void run(JsonWriter out, int threads, int count)
	{
		synchronized (this.runs)
		{
			this.done = 0;
			this.started = 0;
			this.count = count;
			this.threads = threads;
			this.runs.clear();
			while (this.done < count)
			{
				this.processThreads(out);
				if (!this.runs.isEmpty())
				{
					try
					{
						Thread.sleep(1000);
					}
					catch (InterruptedException e)
					{
					}
				}
			}
		}
	}
	
	private void processThreads(JsonWriter out)
	{
		Iterator<StatRunner> runs = this.runs.iterator();
		while (runs.hasNext())
		{
			StatRunner run = runs.next();
			if (run.result != null)
			{
				runs.remove();
				try
				{
					this.addResult(run, out);
				}
				catch (IOException e)
				{
					e.printStackTrace();
					break;
				}
			}
		}
		this.addThreads(Math.min(this.count - this.started, this.threads - this.runs.size()));
	}
	
	private void addResult(StatRunner run, JsonWriter out) throws IOException
	{
		System.out.printf("\t#%d DONE%n", run.number);
		this.done++;
		EvolveResult res = run.result;
		
		DescriptiveStatistics stats = new DescriptiveStatistics(res.finalFitness);
		
		out.beginObject();
		out.name("mean").value(stats.getMean());
		out.name("sd").value(stats.getStandardDeviation());
		out.name("min").value(stats.getMin());
		out.name("q1").value(stats.getPercentile(25));
		out.name("median").value(stats.getPercentile(50));
		out.name("q3").value(stats.getPercentile(75));
		out.name("max").value(stats.getMax());
		
		out.name("log");
		out.beginArray();
		for (DescriptiveStatistics gen : res.generationStats)
		{
			out.beginObject();
			out.name("min").value(gen.getMin());
			out.name("mean").value(gen.getMean());
			out.name("median").value(gen.getPercentile(50));
			out.name("max").value(gen.getMax());
			out.endObject();
		}
		out.endArray();
		
		out.endObject();
		out.flush();
	}
	
	private void addThreads(int num)
	{
		for (int i = 0; i < num; i++)
		{
			StatRunner run = new StatRunner(this.started);
			this.started++;
			this.runs.add(run);
			new Thread(run).start();
			System.out.printf("\t#%d STARTED%n", run.number);
		}
	}
}
