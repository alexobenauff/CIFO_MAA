package project;

import gd.gui.GeneticDrawingApp;

public class Main {

	public static final int NUMBER_OF_TRIANGLES = 100;
	public static final int NUMBER_OF_RUNS = 3;
	public static final int NUMBER_OF_GENERATIONS = 2000;
	public static final int POPULATION_SIZE = 25;
	public static final String Selection = "Tournament";
	public static double MUTATION_PROBABILIY =1;
	public static final double MUTATION_AMOUNT =0.005;
	public static final double CROSSOVER_PROBABILIY =1;
	public static final int TOURNAMENT_SIZE =25;
	public static final String Initialization = "initializeFillingBorders";
	public static final String initializeColor = "Midpoint";
	public static final boolean initalizeConstantAlpha = true;
	public static final int Alpha = 60;	
	
	public static final String Crossover = "multiPointCrossover";
	public static final String Mutation ="applyMutationTargetPictureColor";
	public static final String MutationRate = "default";
	
	//best methods approaches should be used separately
	///a.  Choose best crossover or mutation operator (BestXO and/or BestMut)
	public static final boolean BestXO = false; 
	public static final boolean BestMutation = true;
	
	//b. Check if Parents, xo offsprings have better fitness (CheckBestParentsXO and CheckFitnessXOMut)
	public static final boolean CheckBestParentsXO = false; 
	public static final boolean CheckFitnessXOMut = false; 
	
	//c.  Apply several crossover and mutation operators (CheckBest2XO, CheckBest2Mut)
	public static final boolean CheckBest2XO = false;
	public static final boolean CheckBest2Mut = false;
	public static final int NumDoWhile2XO = 3; //0 for one - max can be xo applied
	public static final int NumDoWhile2Mut = 3; //0 for one - max mutation can be applied

	

	public static boolean KEEP_WINDOWS_OPEN = false;

	public static Solution[] bestSolutions = new Solution[NUMBER_OF_RUNS];
	public static double[] bestFitness = new double[NUMBER_OF_RUNS];
	public static int currentRun = 0;

	public static void main(String[] args) {
		run();
	}
	
	public static void addBestSolution(Solution bestSolution) {
		bestSolutions[currentRun] = bestSolution;
		bestFitness[currentRun] = bestSolution.getFitness();
		System.out.printf("Got %.2f as a result for run %d\n", bestFitness[currentRun], currentRun + 1);
		System.out.print("All runs:");
		for (int i = 0; i <= currentRun; i++) {
			System.out.printf("\t%.2f", bestFitness[i]);
		}
		System.out.println();
		currentRun++;
		if (KEEP_WINDOWS_OPEN == false) {
			Problem.view.getFittestDrawingView().dispose();
			Problem.view.getFrame().dispose();
		}
		if (currentRun < NUMBER_OF_RUNS) {
			run();
		} else {
			presentResults();
		}
	}

	public static void presentResults() {
		double mean = Statistics.mean(bestFitness);
		double stdDev = Statistics.standardDeviation(bestFitness);
		double best = Statistics.min(bestFitness);
		double worst = Statistics.max(bestFitness);
		System.out.printf("\n\t\tMean +- std dev\t\tBest\t\tWorst\n\n");
		System.out.printf("Results\t\t%.2f +- %.2f\t%.2f\t%.2f\n", mean, stdDev, best, worst);
		
		CreateExcel.writeExcel();
	}

	public static void run() {
		GeneticDrawingApp.main(null);
	}
}






