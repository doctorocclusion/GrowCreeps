package net.eekysam.creeps.stat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;

public class StatMain
{
	public static void main(String[] args) throws IOException
	{
		Stat stat = Stat.valueOf(args[0]);
		File file = new File(String.format("%s_%04X.%s.json", args[1], (int) (Math.random() * 0x10000), stat.filename));
		file.createNewFile();
		int runs = Integer.parseInt(args[2]);
		int threads = Integer.parseInt(args[3]);
		int lastSize = 256;
		
		Gson gson = new Gson();
		JsonWriter out = new JsonWriter(new BufferedWriter(new FileWriter(file)));
		out.setIndent("\t");
		
		out.beginObject();
		
		out.name("data").beginArray();
		Object[] vars = stat.variables();
		for (int i = 0; i < vars.length; i++)
		{
			Object var = vars[i];
			System.out.printf("Testing %s with %s (%d/%d)%n", stat.name(), String.valueOf(var), i + 1, vars.length);
			
			out.beginObject();
			
			out.name("value");
			gson.toJson(var, var.getClass(), out);
			
			EvolverStats stats = new EvolverStats(stat.runner(var, lastSize));
			
			out.name("solutions").beginArray();
			System.out.printf("Finding %d solutions...%n", runs);
			stats.run(out, threads, runs);
			out.endArray();
			
			out.endObject();
		}
		out.endArray();
		
		out.endObject();
		out.close();
	}
}
