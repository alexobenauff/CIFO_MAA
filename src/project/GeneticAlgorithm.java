package project;

import java.util.Random;
import java.util.Arrays;

public class GeneticAlgorithm extends SearchMethod {
	protected Problem instance;
	protected Solution[] population;
	protected Solution currentBest;
	protected int populationSize, numberOfGenerations, currentGeneration, tournamentSize;
	protected double mutationProbability, crossoverProbability;
	protected boolean printFlag;
	protected Random r;

	public GeneticAlgorithm() {
		instance = new Problem(Main.NUMBER_OF_TRIANGLES);
		populationSize = Main.POPULATION_SIZE;
		numberOfGenerations = Main.NUMBER_OF_GENERATIONS;
		mutationProbability = Main.MUTATION_PROBABILIY;
		crossoverProbability = Main.CROSSOVER_PROBABILIY;
		tournamentSize = Main.TOURNAMENT_SIZE;
		printFlag = false;
		currentGeneration = 0;
		r = new Random();
	}

	public void run() {
		initialize();
		search();
		Main.addBestSolution(currentBest);
	}

	public void initialize() {
		population = new Solution[populationSize];
		for (int i = 0; i < population.length; i++) {
			population[i] = new Solution(instance);
			population[i].evaluate();
		}
		updateCurrentBest();
		updateInfo();
		currentGeneration++;
	}

	public void updateCurrentBest() {
		currentBest = getBest(population);
	}

	public void search() {
		int[] ColorArray = TargetPictureColors();
		Solution firstParent = null, secondParent = null, XOoffspring = null, MutOffspring = null,
				XOoffspringStart = null;
		double FitnessXO = 0, FitnessMut = 0, P1Fitness = 0, P2Fitness = 0, MutOsFitness = 0,
				XOoffspringStartFitness = 0;
		boolean CheckBest2Mut = Main.CheckBest2Mut, CheckBest2XO = Main.CheckBest2XO;
		String selectionmethod = Main.Selection;
		String[] XOmethods = { "singlePointCrossover", "multiPointCrossover", "UniformCrossover", "CycleCrossover",
				"TriangleCrossover" };
		String[] Mmethods = { "applyMutation", "applySwapMutation", "applyScrambleMutation",
				"applyMutationTargetPictureColor", "applyInversionMutation", "applyMutationVertex" };
		int[] parents = new int[2];
		int p1 = 0;

		while (currentGeneration <= numberOfGenerations) {

			Solution[] offsprings = new Solution[populationSize]; // P'

			for (int k = 0; k < population.length; k++) {

				/// different mutation rate methods (random, default, decayed)
				switch (Main.MutationRate) {

				case "random":
					Main.MUTATION_PROBABILIY = r.nextDouble();
					break;
				case "decayed":
					double numerator = (numberOfGenerations - currentGeneration);
					double exponent = (numerator / (numberOfGenerations));
					Main.MUTATION_PROBABILIY = Math.pow((1 - mutationProbability), exponent);
					break;
				case "default":
					mutationProbability = Main.MUTATION_PROBABILIY;
					break;
				}

				/// Select 1st parent
				switch (selectionmethod) {
				case "Tournament":
					p1 = tournamentSelection();
					break;
				case "Roulette":
					p1 = rouletteWheelSelection();
					break;
				case "Ranking":
					p1 = rankingSelection();
					break;
				}

				/// Crossover
				if (r.nextDouble() <= crossoverProbability) {

					// choose 2nd parent add their index (population) to int[] array called parents
					if (selectionmethod == "Tournament") {
						parents[0] = p1;
						parents[1] = tournamentSelection();
					}

					if (selectionmethod == "Roulette") {
						parents[0] = p1;
						parents[1] = rouletteWheelSelection();
					}

					if (selectionmethod == "Ranking") {
						parents[0] = p1;
						parents[1] = rankingSelection();
					}

					/// if BestXO set to true, then...
					if (Main.BestXO) {
						// creates a double[] array to store fitness for different xo operators
						double[] XOfitness = new double[4];
						// create a empty Offspring(clone)
						Solution[] offspringClone = new Solution[XOfitness.length];

						// loop through different XO operators and create new Offspring store into
						// offspringClone + evaluate fitness and store XOfitness array
						for (int i = 0; i <= XOfitness.length; i++) {
							switch (i) {
							case 0:
								offspringClone[i] = singlePointCrossover(parents);
								XOfitness[i] = offspringClone[i].evaluateOffspring();
								break;
							case 1:
								offspringClone[i] = multiPointCrossover(parents);
								XOfitness[i] = offspringClone[i].evaluateOffspring();
								break;
							case 2:
								offspringClone[i] = UniformCrossover(parents);
								XOfitness[i] = offspringClone[i].evaluateOffspring();
								break;
							case 3:
								offspringClone[i] = CycleCrossover(parents);
								XOfitness[i] = offspringClone[i].evaluateOffspring();
								break;
							// case 4: offspringClone[i] = TriangleCrossover(parents); //excluded here,
							// performance
							// XOfitness[i] = offspringClone[i].evaluateOffspring();break; //excluded here,
							// performance
							}
						}

						// after running different crossover operators, find lowest fitness
						double min = XOfitness[0];
						int idx = 0;
						for (int x = 0; x < XOfitness.length; x++) {
							if (XOfitness[x] < min) {
								min = XOfitness[x];
								idx = x;
							}
						}

						// new offspring equals the offspring with lowest fitness created by
						// corresponding XO operator
						offsprings[k] = offspringClone[idx];
					} else {

						/// if BestXO false, then ....
						String crossover = Main.Crossover;

						FitnessXO = 0;

						// offsprings[k] = population[p1];
						int countxo = 0;

						/// only if CheckBest2XO is true the do while will run more than once, false XO
						/// only once
						do {

							switch (crossover) {

							case "singlePointCrossover":
								offsprings[k] = singlePointCrossover(parents);
								break;
							case "multiPointCrossover":
								offsprings[k] = multiPointCrossover(parents);
								break;
							case "UniformCrossover":
								offsprings[k] = UniformCrossover(parents);
								break;
							case "CycleCrossover":
								offsprings[k] = CycleCrossover(parents);
								break;
							case "TriangleCrossover":
								offsprings[k] = TriangleCrossover(parents);
								break;
							case "PartiallyMatchedCrossover":
								offsprings[k] = PartiallyMatchedCrossover(parents);
								break;
							}

							/// CheckBest2XO method part
							XOoffspring = offsprings[k];
							if (CheckBest2XO) {
								FitnessXO = XOoffspring.evaluateOffspring();

								if (countxo == 0) {
									XOoffspringStart = offsprings[k];
									XOoffspringStartFitness = XOoffspringStart.evaluateOffspring();
								}

								// if this CheckBest2XO false than crossover will only run once
								if (CheckBest2XO && countxo == Main.NumDoWhile2XO) {

									if ((XOoffspringStartFitness < FitnessXO)) {
										offsprings[k] = XOoffspringStart;
									} else {
										offsprings[k] = XOoffspring;
									}
								}
								countxo++;
								crossover = XOmethods[r.nextInt(4)];
							}
							// if this CheckBest2XO false than crossover will only run once, else it run
							// until termination condition met or xo was applied n times to same offspring
						} while (CheckBest2XO
								&& (FitnessXO >= (XOoffspringStartFitness) && (countxo <= Main.NumDoWhile2XO)));
					}

					// CheckBestParentsXO method, if true checks if FitnessXO is bigger than parent1
					// or parent2;takes the one with best fitness
					if (Main.CheckBestParentsXO) {

						// firstParent and secondParent
						firstParent = population[parents[0]];
						secondParent = population[parents[1]];

						// evaluate fitness of parents
						P1Fitness = firstParent.evaluateOffspring();
						P2Fitness = secondParent.evaluateOffspring();
						FitnessXO = offsprings[k].evaluateOffspring();

						if (FitnessXO > P1Fitness || FitnessXO > P2Fitness) {
							if (P1Fitness < P2Fitness) {
								offsprings[k] = firstParent;
							} else {
								offsprings[k] = secondParent;
							}
						}
					}
					XOoffspring = offsprings[k];
				} else {
					offsprings[k] = population[p1];
				}

				/// Mutation
				if (r.nextDouble() <= mutationProbability) {

					String mutation;

					/// BestMutation method
					if (Main.BestMutation) {

						double[] mfitness = new double[11];
						Solution[] offspringClone = new Solution[mfitness.length];

						for (int i = 0; i <= mfitness.length; i++) {
							Solution offspringclone = offsprings[k];
							switch (i) {
							case 0:
								offspringClone[i] = offspringclone.applyMutation();
								mfitness[i] = offspringClone[i].evaluateOffspring();
								break;
							case 1:
								offspringClone[i] = offspringclone.applyMutationTargetPictureColor(ColorArray);
								mfitness[i] = offspringClone[i].evaluateOffspring();
								break;
							case 2:
								offspringClone[i] = offspringclone.applyScrambleMutation();
								mfitness[i] = offspringClone[i].evaluateOffspring();
								break;
							case 3:
								offspringClone[i] = offspringclone.applySwapMutation();
								mfitness[i] = offspringClone[i].evaluateOffspring();
								break;
							case 4:
								offspringClone[i] = offspringclone.applyInversionMutation();
								mfitness[i] = offspringClone[i].evaluateOffspring();
								break;
							case 5:
								offspringClone[i] = offspringclone.applyMutationVertex();
								mfitness[i] = offspringClone[i].evaluateOffspring();
								break;
							case 6:
								offspringClone[i] = offspringclone.applyMidpointColor();
								mfitness[i] = offspringClone[i].evaluateOffspring();
								break;
							case 7:
								offspringClone[i] = offspringclone.applyRandomTriangleLineColor();
								mfitness[i] = offspringClone[i].evaluateOffspring();
								break;
							case 8:
								offspringClone[i] = offspringclone.applyMutationTriangleColorTargetPicture();
								mfitness[i] = offspringClone[i].evaluateOffspring();
								break;
							case 9:
								offspringClone[i] = offspringclone.applyMutationConstantAlpha();
								mfitness[i] = offspringClone[i].evaluateOffspring();
								break;
							case 10:
								offspringClone[i] = offspringclone.applyColorTriangleAmount();
								mfitness[i] = offspringClone[i].evaluateOffspring();
								break;
							}
						}

						/// get offspring where mutation achieved lowest fitness
						double min = mfitness[0];
						int idx = 0;
						for (int x = 0; x < mfitness.length; x++) {
							if (mfitness[x] < min) {
								min = mfitness[x];
								idx = x;
							}
						}
						// replace offsprings[k] with the offspring with the lowest fitness
						offsprings[k] = offspringClone[idx];

						/// if BestMutation method set to false, proceed as normal
					} else {

						mutation = Main.Mutation;

						int count = 0;
						Solution MutOffspringStart = null;
						do {

							switch (mutation) {

							case "applyMutation":
								offsprings[k] = offsprings[k].applyMutation();
								break;
							case "applyMutationConstantAlpha":
								offsprings[k] = offsprings[k].applyMutationConstantAlpha();
								break;
							case "applyMutationVertex":
								offsprings[k] = offsprings[k].applyMutationVertex();
								break;
							case "applyMutationWithAmount":
								offsprings[k] = offsprings[k].applyMutationWithAmount(ColorArray);
								break;
							case "applyMutationTargetPictureColor":
								offsprings[k] = offsprings[k].applyMutationTargetPictureColor(ColorArray);
								break;
							case "applyMutationTriangleColorTargetPicture":
								offsprings[k] = offsprings[k].applyMutationTriangleColorTargetPicture();
								break;
							case "applyRandomTriangleLineColor":
								offsprings[k] = offsprings[k].applyRandomTriangleLineColor(); // only changes color not vertices															
								offsprings[k] = offsprings[k].applyMutationVertex();// changes vertices only
								break; 
							case "applyMidpointColor":
								offsprings[k] = offsprings[k].applyMidpointColor();
								break;
							case "applyColorTriangleAll":
								offsprings[k] = offsprings[k].applyColorTriangleAll();
								break;
							case "applyColorTriangleAmount":
								offsprings[k] = offsprings[k].applyColorTriangleAmount();
								break; 
							case "applySwapMutation":
								offsprings[k] = offsprings[k].applySwapMutation();
								break;
							case "applyInversionMutation":
								offsprings[k] = offsprings[k].applyInversionMutation();
								break;
							case "applyScrambleMutation":
								offsprings[k] = offsprings[k].applyScrambleMutation();
								break;
							}

							// if CheckBest2Mut false then mutation will only run once
							if (CheckBest2Mut) {
								MutOffspring = offsprings[k];
								FitnessMut = MutOffspring.evaluateOffspring();

								if (count == 0) {
									MutOffspringStart = offsprings[k];
									MutOsFitness = MutOffspringStart.evaluateOffspring();
								}
								if (count == Main.NumDoWhile2XO) {

									if (MutOsFitness < FitnessMut) {
										offsprings[k] = MutOffspringStart;
									} else {
										offsprings[k] = MutOffspring;
									}
								}
								count++;
								mutation = Mmethods[r.nextInt(6)];
							}

						} while (CheckBest2Mut && (FitnessMut >= MutOsFitness && count <= Main.NumDoWhile2XO));
					}
				}

				// Check XO fitness > Mut fitness ? Mutoffspring : XOoffspring
				if (Main.CheckFitnessXOMut) {
					FitnessMut = offsprings[k].evaluateOffspring();
					FitnessXO = XOoffspring.evaluateOffspring();
					if (FitnessXO < FitnessMut) {
						offsprings[k] = XOoffspring;
					}
				}

				offsprings[k].evaluate();
			}

			population = replacement(offsprings);
			updateCurrentBest();
			updateInfo();
			currentGeneration++;
		}
	}

	// --------------- Variation
	// --- Single Point Crossover
	public Solution singlePointCrossover(int[] parents) {
		Solution firstParent = population[parents[0]];
		Solution secondParent = population[parents[1]];
		Solution offspring = firstParent.copy();
		int crossoverPoint = r.nextInt(instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE);
		for (int i = crossoverPoint; i < instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE; i++) {
			offspring.setValue(i, secondParent.getValue(i));
		}
		return offspring;
	}

	// --------------- Replacement: P=P'
	// --- Elitism: one elit
	public Solution[] replacement2(Solution[] offspring) {
		Solution bestParent = getBest(population);
		Solution bestOffspring = getBest(offspring);
		if (bestOffspring.getFitness() <= bestParent.getFitness()) {
			return offspring;
		} else {
			// no improvement, apply shared fitness
			Solution[] newPopulation = new Solution[population.length];
			Solution[] pop = population;
			Solution[] offs = offspring;
			double[] popshared = new double[population.length];
			double[] offsshared = new double[population.length];

			newPopulation[0] = bestParent;

			// order population
			for (int i = 0; i < population.length; i++) {
				pop[i] = Solution.ordersolution(pop[i]);
				pop[i] = Solution.orderwholesolution(pop[i]);
				offs[i] = Solution.ordersolution(offs[i]);
				offs[i] = Solution.orderwholesolution(offs[i]);
			}

			// calculate shared fitnesses
			popshared = Solution.getsharedfitnesses(pop);
			offsshared = Solution.getsharedfitnesses(offs);

			// order offspring
			for (int i = 1; i < population.length; i++) {
				if (popshared[i] < offsshared[i]) {
					newPopulation[i] = pop[i];
				} else {
					newPopulation[i] = offs[i];
				}
			}
			return newPopulation;
		}
	}

	// --------------- Auxiliary methods
	// get best solution
	public Solution getBest(Solution[] solutions) {
		Solution best = solutions[0];
		for (int i = 1; i < solutions.length; i++) {
			if (solutions[i].getFitness() < best.getFitness()) {
				best = solutions[i];
			}
		}
		return best;
	}

	// get best with shared fitness
	public Solution getBestShared(Solution[] solutions) {
		Solution best = solutions[0];

		// shared fitness
		double[] sharedfitnesses = Solution.getsharedfitnesses(solutions);
		double bestshared = sharedfitnesses[0];

		for (int i = 1; i < solutions.length; i++) {
			if (sharedfitnesses[i] < bestshared) {
				bestshared = sharedfitnesses[i];
				best = solutions[i];
			}
		}
		return best;
	}

	// --------------- Replacement: P=P'
	// --- Elitism: one elit
	public Solution[] replacement(Solution[] offspring) {
		Solution bestParent = getBest(population);
		Solution bestOffspring = getBest(offspring);
		if (bestOffspring.getFitness() <= bestParent.getFitness()) {
			return offspring;
		} else {
			Solution[] newPopulation = new Solution[population.length];
			newPopulation[0] = bestParent;
			int worstOffspringIndex = getWorstIndex(offspring);
			for (int i = 0; i < newPopulation.length; i++) {
				if (i < worstOffspringIndex) {
					newPopulation[i + 1] = offspring[i];
				} else if (i > worstOffspringIndex) {
					newPopulation[i] = offspring[i];
				}
			}
			return newPopulation;
		}
	}

	// get worst
	public int getWorstIndex(Solution[] solutions) {
		Solution worst = solutions[0];
		int index = 0;
		for (int i = 1; i < solutions.length; i++) {
			if (solutions[i].getFitness() > worst.getFitness()) {
				worst = solutions[i];
				index = i;
			}
		}
		return index;
	}

	// update output
	public void updateInfo() {
		currentBest.draw();
		series.add(currentGeneration, currentBest.getFitness());
		if (printFlag) {
			System.out.printf("Generation: %d\tFitness: %.1f\n", currentGeneration, currentBest.getFitness());
		}
		CreateExcel.storedata(Main.currentRun + 1, currentGeneration, currentBest.getFitness());

	}

	///// store all Colors from targetPicture into an array
	public int[] TargetPictureColors() {
		int[] cArray = new int[instance.getImageWidth() * instance.getImageHeight()];
		int[] targetPixels = instance.getTargetPixels();
		for (int p = 0; p < cArray.length; p++) {
			// int[] HSB = GetColorOfMidpoint(p);
			cArray[p] = targetPixels[p];
		}
		return cArray;
	}

	///////// SELECTION
	/////// --- Roulette Wheel
	protected int rouletteWheelSelection() {
		double p = r.nextDouble(), sum = 0, totalFitness = totalFitness(), totalmFitness = totalmFitness();
		int i;
		for (i = 0; i < population.length; i++) {
			sum += ((1 - (population[i].getFitness() / totalFitness)) / totalmFitness);
			if ((sum) >= p)
				break;
		}
		return i;
	}

	/// Total Fitness - Roulette Wheel
	protected double totalFitness() {
		double total = 0;
		for (int i = 0; i < population.length; i++) {
			total += population[i].getFitness();
		}
		return total;

	}

	/// Total modified Fitness - Roulette Wheel
	protected double totalmFitness() {
		double total = 0, totalFitness = totalFitness();
		for (int i = 0; i < population.length; i++) {
			total += (1 - (population[i].getFitness() / totalFitness));
		}
		return total;

	}

	////// Ranking Selection
	protected int rankingSelection() {
		double p = r.nextDouble(), sum = 0, totalFitness = totalFitness(), totalmFitness = totalmFitness();
		int i;

		// create arrays to store ranked Fitness, initial Fitness, ordered Fitness
		// (indices)
		double[] rFitness = new double[population.length];
		double[] iFitness = new double[population.length];
		double[] ordFitness = new double[population.length];

		// add fitness to rFitness and iFitness

		for (i = 0; i < population.length; i++) {
			rFitness[i] = population[i].getFitness();
			iFitness[i] = population[i].getFitness();
		}
		// sort rFitness ascending order
		Arrays.sort(rFitness);

		// add indiviuals index of population to ordFitness,corresponding indices added
		for (i = 0; i < population.length; i++) {
			for (int j = 0; j < population.length; j++) {
				if (rFitness[i] == iFitness[j]) {
					ordFitness[j] = j;
				}
			}
		}

		// ranked fitness assign 1 to 25 asc that selection is propotional to ranking
		for (i = 0; i < population.length; i++) {
			rFitness[0] = i + 1;
		}
		// use roulette wheel methodology to choose indiviual with lowest fitness
		for (i = 0; i < population.length; i++) {
			sum += ((1 - (population[i].getFitness() / totalFitness)) / totalmFitness);
			if ((sum) >= p)
				break;
		}
		return i;
	}

	/////// Tournament Selection
	protected int tournamentSelection() {
		int parentIndex = r.nextInt(populationSize);
		for (int i = 0; i < tournamentSize; i++) {
			int temp = r.nextInt(populationSize);
			if (population[temp].getFitness() < population[parentIndex].getFitness()) {
				parentIndex = temp;
			}
		}
		return parentIndex;
	}

	///////////// CROSSOVER
	///////////// OPERATORS/////////////////////////////////////////////////////////////////////////////////////////
	/////// Multipoint crossover - 2 crossover points)
	public Solution multiPointCrossover(int[] parents) {
		Solution firstParent = population[parents[0]];
		Solution secondParent = population[parents[1]];
		Solution offspring = firstParent.copy();
		int crossoverPoint1 = r.nextInt(instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE);
		int crossoverPoint2 = r.nextInt(instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE);
		int minCrossover = 0;
		int maxCrossover = 0;

		while (crossoverPoint1 == crossoverPoint2) {
			crossoverPoint2 = r.nextInt(instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE);
		}

		if (crossoverPoint1 > crossoverPoint2) {
			minCrossover = crossoverPoint2;
			maxCrossover = crossoverPoint1;
		} else {
			minCrossover = crossoverPoint1;
			maxCrossover = crossoverPoint2;
		}
		for (int i = minCrossover; i < maxCrossover; i++)
			offspring.setValue(i, secondParent.getValue(i));

		return offspring;
	}

	/////// Uniform Crossover
	public Solution UniformCrossover(int[] parents) {
		Solution firstParent = population[parents[0]];
		Solution secondParent = population[parents[1]];
		Solution offspring = firstParent.copy();

		for (int i = 1; i < instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE; i++)
			if (r.nextDouble() < 0.5)
				offspring.setValue(i, secondParent.getValue(i));

		return offspring;
	}

	public int[] OrderArray(int[] oArray) {
		for (int i = 0; i < instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE; i++) {
			oArray[i] = i;
		}
		return oArray;
	}

	////// Triangle crossover
	public Solution TriangleCrossover(int[] parents) {

		// Solution firstParent = population[parents[0]];
		double[] fitnesses1 = new double[Main.NUMBER_OF_TRIANGLES];
		double[] fitnesses2 = new double[Main.NUMBER_OF_TRIANGLES];
		Solution offspring = population[parents[0]].copy();
		Solution firstParent = population[parents[0]];
		Solution secondParent = population[parents[1]];

		// order both solutions
		firstParent = Solution.ordersolution(firstParent);
		firstParent = Solution.orderwholesolution(firstParent);

		// firstParent.evaluate2();

		// based on the fitnesses per triangle
		fitnesses1 = Solution.evaluate2(firstParent);
		// based on the avg fitness per index per triangle
		// fitnesses1 = population[parents[0]].getFitnessPerIndexOfTriangles();

		// Solution secondParent = population[parents[1]];
		secondParent = Solution.ordersolution(secondParent);
		secondParent = Solution.orderwholesolution(secondParent);

		// secondParent.evaluate2();

		// based on the fitnesses per triangle
		fitnesses2 = Solution.evaluate2(secondParent);// getFitnessOfTriangles();
		// based on the avg fitness per index per triangle
		// fitnesses2 = population[parents[1]].getFitnessPerIndexOfTriangles();

		for (int i = 0; i < Main.NUMBER_OF_TRIANGLES; i++) {
			if (fitnesses1[i] < fitnesses2[i]) { // <
				for (int j = 0; j < 10; j++) {
					offspring.setValue(i * Solution.VALUES_PER_TRIANGLE + j,
							firstParent.getValue(i * Solution.VALUES_PER_TRIANGLE + j));
				}
			} else {
				for (int j = 0; j < 10; j++) {
					offspring.setValue(i * Solution.VALUES_PER_TRIANGLE + j,
							secondParent.getValue(i * Solution.VALUES_PER_TRIANGLE + j));
				}
			}
		}
		// offspring.evaluate();
		if (offspring.getFitness() < firstParent.getFitness()) {
			return offspring;
		} else {
			if (secondParent.getFitness() < firstParent.getFitness()) {
				return secondParent;
			} else {
				return firstParent;
			}

		}

	}

	////// Cycle crossover
	public Solution CycleCrossover(int[] parents) {
		// double InversionRate = Main.InversionRate;
		Solution firstParent = population[parents[0]];
		Solution secondParent = population[parents[1]];

		Solution offspring = firstParent.copy();
		// Solution offspring2 = secondParent.copy();

		// #1 Create Array to keep initial Order for both parents

		int[] OrderArrayOffspring = new int[instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE];

		for (int i = 0; i < instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE; i++) {
			OrderArrayOffspring[i] = i;
		}
		// #1 create clone of OrderArrayOffsprint for OrderArrayOffsprint2
		int[] OrderArrayOffspring2 = OrderArrayOffspring.clone();

		// #2 for loop through index to invert them randomly

		for (int i = 0; i < instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE; i++) {
			// check for each i, to be inverted

			int i2 = r.nextInt(instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE);

			int tmp = OrderArrayOffspring[i];
			OrderArrayOffspring[i] = OrderArrayOffspring[i2];
			OrderArrayOffspring[i2] = tmp;
		}

		for (int i = 0; i < instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE; i++) {
			// check for each i, to be inverted
			int i2 = r.nextInt(instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE);
			int tmp = OrderArrayOffspring2[i];
			OrderArrayOffspring2[i] = OrderArrayOffspring2[i2];
			OrderArrayOffspring2[i2] = tmp;
		}

		// #3 array with -1 to distinct between the inversion offsprings
		int[] OffspringArray = new int[instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE];
		for (int i = 0; i < OffspringArray.length; i++) {
			OffspringArray[i] = -1;
		}

		// #4 Offspring 1st index equal to OrderArrayOffspring
		OffspringArray[0] = OrderArrayOffspring[0];
		// #5 store value 1st index as int (Idx)
		int Idx = OrderArrayOffspring2[0];
		boolean check = false;

		while (check == false) {
			// #6 check if Idx is already in OffspringArray
			for (int i = 0; i < OffspringArray.length; i++) {

				if (OffspringArray[i] == Idx) {
					check = true;
				}
			}
			// #7 if Idx not in OffspringArray search for Idx in OrderArrayOffspring
			for (int j = 0; j < OrderArrayOffspring.length; j++) {
				// #8 check each value in OrderArrayOffspring, and find where it matches Idx
				if (OrderArrayOffspring[j] == Idx) {
					// IdxValue is "Idx" or OrderArrayOffspring[j]
					// offspringarray set IdxValue
					OffspringArray[j] = Idx;
					// new Idxvalue
					Idx = OrderArrayOffspring2[j];
					break;
				}
			}
		}
		// #9 set offspring values from 1st and 2nd parent by checking OffspringArray ,
		// if index value != -1 1st parent else 2nd parent
		for (int x = 0; x < instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE; x++)
			if (OffspringArray[x] != -1) {
				offspring.setValue(OffspringArray[x], firstParent.getValue(OffspringArray[x]));
			} else {
				offspring.setValue(OrderArrayOffspring2[x], secondParent.getValue(OrderArrayOffspring2[x]));
			}
		return offspring;

	}

	////// PMX crossover
	public Solution PartiallyMatchedCrossover(int[] parents) {
		Solution firstParent = population[parents[0]];
		Solution secondParent = population[parents[1]];

		Solution offspring = firstParent.copy();

		int[] OrderArrayOffspring = new int[instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE];

		for (int i = 0; i < instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE; i++) {
			OrderArrayOffspring[i] = i;
		}
		// #1 create clone of OrderArrayOffsprint for OrderArrayOffsprint2
		int[] OrderArrayOffspring2 = OrderArrayOffspring.clone();

		int[] Placestochange = new int[instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE];

		// #2 for loop through index to invert them randomly

		for (int i = 0; i < instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE; i++) {
			// check for each i, to be inverted

			int i2 = r.nextInt(instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE);

			int tmp = OrderArrayOffspring[i];
			OrderArrayOffspring[i] = OrderArrayOffspring[i2];
			OrderArrayOffspring[i2] = tmp;

			tmp = firstParent.getValue(i);
			firstParent.setValue(i, firstParent.getValue(i2));
			firstParent.setValue(i2, tmp);

		}

		for (int i = 0; i < instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE; i++) {
			// check for each i, to be inverted
			int i2 = r.nextInt(instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE);

			int tmp = OrderArrayOffspring2[i];
			OrderArrayOffspring2[i] = OrderArrayOffspring2[i2];
			OrderArrayOffspring2[i2] = tmp;

			tmp = secondParent.getValue(i);
			secondParent.setValue(i, secondParent.getValue(i2));
			secondParent.setValue(i2, tmp);
		}

		// create 2 random numbers
		int i2 = r.nextInt(instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE);
		int i3 = r.nextInt(instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE);

		int temp = 0;

		if (i2 == i3) {
			i2 = r.nextInt(instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE);
			i3 = r.nextInt(instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE);
		}
		if (i3 < i2) {
			temp = i3;
			i3 = i2;
			i2 = temp;
		}
		// from the first number to the second number and switch between
		for (int i = i2; i < i3; i++) {

			// swap the values
			temp = firstParent.getValue(i);
			firstParent.setValue(i, secondParent.getValue(i));
			secondParent.setValue(i, temp);

			// switch between the two order arrays
			temp = OrderArrayOffspring[i];
			OrderArrayOffspring[i] = OrderArrayOffspring2[i];
			OrderArrayOffspring2[i] = temp;
		}

		// check which places we should change based on second parent
		for (int i = 0; i < instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE; i++) {
			for (int j = 0; j < instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE; j++) {
				if (OrderArrayOffspring2[i] == OrderArrayOffspring2[j]) {
					if (i != j) {
						if (i < i2 || i > (i3 - 1)) {
							Placestochange[i] = -1;
						}
					}
				}
			}
		}

		int placetochange = 0;

		// change the places that must change
		for (int i = 0; i < instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE; i++) {
			if (Placestochange[i] == -1) {
				// find the value
				for (int j = 0; j < instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE; j++) {
					// find the first duplicate
					int newvalue = -1;
					if (OrderArrayOffspring2[i] == OrderArrayOffspring2[j]) {
						if (i != j) {
							newvalue = OrderArrayOffspring[j];
							placetochange = j;
						}
					}
					if (newvalue != -1) {
						// do until newvalue is not in array
						while (IsInArray(OrderArrayOffspring2, newvalue) == true) {
							for (int k = 0; k < instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE; k++) {
								if (OrderArrayOffspring2[k] == newvalue) {
									newvalue = OrderArrayOffspring[k];
									placetochange = k;
								}
							}
						}
					}

				}
				// swap the values
				temp = firstParent.getValue(placetochange);
				firstParent.setValue(placetochange, secondParent.getValue(i));
				secondParent.setValue(i, temp);

				// swap the places
				temp = OrderArrayOffspring[placetochange];
				OrderArrayOffspring[placetochange] = OrderArrayOffspring2[i];
				OrderArrayOffspring2[i] = temp;
			}

		}

		int place = 0;
		// create the two offsprings again based on the order of the firstarrays
		for (int i = 0; i < instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE; i++) {
			place = OrderArrayOffspring[i];

			for (int j = 0; j < instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE; j++) {
				if (j == place) {
					offspring.setValue(i, firstParent.getValue(j));
				}
			}

		}
		return offspring;
	}

	///// IsinArray
	public Boolean IsInArray(int[] Array, int value) {
		boolean isinarray = false;
		for (int i = 0; i < Array.length; i++) {
			if (Array[i] == value) {
				isinarray = true;
			}
		}

		return isinarray;
	}

	/// NOT USED --- NOT USED
	protected int tournamentSelectionNOTUSED() {

		// shared fitness
		double[] sharedfitnesses = Solution.getsharedfitnesses(population);

		int parentIndex = r.nextInt(populationSize);
		for (int i = 0; i < tournamentSize; i++) {
			int temp = r.nextInt(populationSize);
			if (sharedfitnesses[temp] < sharedfitnesses[parentIndex]) {
				parentIndex = temp;
			}
		}
		return parentIndex;
	}
}
