package gephi.data.network;

public class Benchmarking {

	private int numSubject;
	private int numExperiment;
	private long[] results;
	private long[] params;

	private long startTime;
	
	public Benchmarking(int numSubject, int numExperiment)
	{
		this.numSubject = numSubject;
		this.numExperiment = numExperiment;
		results = new long[numSubject];
		params = new long[numSubject];
	}
	
	private int exp=0;
	public void startExperiment()
	{
		System.out.println("--start:"+exp);
	}
	
	public void stopExperiment()
	{
		System.out.println("--stop:"+exp);
		exp++;
		if(exp==numExperiment)
			showResults();
	}
	
	public void startSubject(int num)
	{
		startTime = System.nanoTime();
	}
	
	public void startSubject(int num, long param)
	{
		params[num] = param;
		startSubject(num);
	}
	
	public void stopSubject(int num)
	{
		long time = System.nanoTime()-startTime;
		results[num]+= time;
		System.out.println("Subject "+num+" : "+time);
	}
	
	public void stopSubject(int num, int param)
	{
		long time = System.nanoTime()-startTime;
		results[num]= time;
	}
	
	public void showResults()
	{
		int subject=0;
		for(long r : results)
		{
			long avg = r/numExperiment;
			System.out.println("******* Final Subject "+subject+" : "+avg);
			subject++;
		}
	}
	
	public void showTable()
	{
		int subject=0;
		for(long r : results)
		{
			System.out.println(params[subject]+"\t"+r);
			subject++;
		}
	}
}
