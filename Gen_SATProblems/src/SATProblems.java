import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import java.lang.Object;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

public class SATProblems {

	static int RandomLiteral(int NVAR){
		int randomNum = 0;
		while (randomNum == 0) {
			randomNum = ThreadLocalRandom.current().nextInt(-NVAR, NVAR);
		}
		return randomNum;
	}
	
	static int[] GenerateClause (int KSAT, int NVAR, ArrayList<String> clauses) {
		int len_sat = ThreadLocalRandom.current().nextInt(1, KSAT + 1);
		int[] clause = new int[len_sat];
		boolean is_exists = true;
		
		while (is_exists) {
			for (int j=0; j < len_sat ; j++) {
				int flag = 1;
				while (flag == 1) {
					flag = 0;
					int lit = RandomLiteral(NVAR);
					for (int k=0; k < len_sat; k++) {
						if (Math.abs(clause[k]) == Math.abs(lit)) {
							flag = 1;
						}
					}
					if (flag==0) {
						clause[j] = lit;
					}
				}
			}
			is_exists = clauses.contains(Arrays.toString(clause)) ? true : false;
		}
		return clause;
	}
	
	
	static int[] NegateHypothesis (int[] hypo){
		int hypo_len = hypo.length;
		int[] result = new int[hypo_len];
		int negs = 0;
		
		for (int i = 0; i < hypo_len ; i++) 
		{
			  negs = -hypo[i];
			  result[i] = negs;
		}
		return result;
	}
	
	
	static Exception handleException(Exception e) {
        System.err.println("Handling Exception: " + e);
        return new Exception(e);
    }
	
	static ISolver AddClausestoSolver (ArrayList<ArrayList<Integer>> clauses) throws Exception {
		ISolver result = SolverFactory.newDefault();
		for (ArrayList<Integer> clause : clauses) {
			Integer[] literal = new Integer[clause.size()];
			literal = clause.toArray(literal);
			int[] add_lit = new int[literal.length];
			for (int i = 0 ; i < literal.length; i++) {
				add_lit[i] = literal[i].intValue();
			}
			
			try {
				result.addClause(new VecInt(add_lit));
			} catch (ContradictionException e) {
				throw handleException(e);
			}
		}
		return result;
	}
	
	static boolean Is_Satisfiable (ISolver solver) {
		boolean result = false;
		IProblem problem = solver;
		try {
			if (problem.isSatisfiable()) {
				result = true;
			} else {
				result = false;
			}
		} catch (TimeoutException e) {
			result = false;
			e.printStackTrace();
		}
		return result;
	}
	
	static boolean Is_InPrime(int[] hypo, int[] primes) {
		boolean result = false;
		if (hypo.length == 1) {
			int lit = hypo[0];
			for (int el : primes) {
				if (el == lit);
					result = true;
			}
		}
		return result;
	}
	
	static boolean Is_InPrime (int lit, int[] primes) {
		boolean result = false;
		for (int el : primes) {
			if (el == lit);
				result = true;
		}
		return result;
	}
	
	public static void main(String[] args) throws ContradictionException {
		final int NVAR = 5; //number of literal variation, max 8
		final int KSAT = 3; //2-SAT or 3-SAT problems
		final int MAXCLAUSE = 3; //maximum number of clause is 8 in each set

		int numset = 10000; //data size
		int numHypos = 20; //Hypothesis size per set
		
		int id_set = 1;
		int idx = 1;
		boolean is_sat = false;
		String file_name = KSAT + "sat_" + NVAR + "vars_" + numset + ".csv";
		String file_clauses = KSAT + "sat_" + NVAR + "vars_" + numset + "_clauses.csv";
		
		try {
			PrintWriter writer = new PrintWriter(file_name, "UTF-8"); 
			PrintWriter writer_clauses = new PrintWriter(file_clauses, "UTF-8"); 
			writer.println("id;id_set;clauses;entailments;is_entail;is_contradict;k_sat;n_var;num_clauses");
			writer_clauses.println("id_set;clauses;k_sat;n_var;num_clauses");
			
			while (id_set <= numset) {
				is_sat = false;
				ArrayList<String> clauses = new ArrayList<String>(); //store the main clauses
				int numClause = ThreadLocalRandom.current().nextInt(2, MAXCLAUSE + 1);
				int numFact = 0;
				ISolver solver = SolverFactory.newDefault();
				ISolver temp_solver = SolverFactory.newDefault();
				System.out.println("Set: " + id_set + " Number Clauses: " + numClause + " Number Facts: " + numFact);
				
				solver.setExpectedNumberOfClauses(numClause+numFact); //the size of solver
				
				//Feed the solver using Dimacs format, using arrays of int
				// (best option to avoid dependencies on SAT4J IVecInt)
				
				//Generate clauses for premises and feed them to the solver using Dimacs format
				for (int i=0 ; i < numClause ; i++) {
					int[] clause = new int[KSAT];
					boolean is_valid = false;
					
					while (!is_valid) {
						is_valid = true;
						clause = GenerateClause(KSAT, NVAR, clauses);
						clauses.add(Arrays.toString(clause));
						try {
							solver.addClause(new VecInt(clause)); // adapt Array to IVecInt
						} catch (ContradictionException e){
							is_valid = false;
							clauses.remove(clauses.size()-1);
						}
					}
					
				}
				
				
				System.out.println("Default Premises:");
				System.out.println(clauses);
				
				if(Is_Satisfiable(solver) == true) {
					System.out.println("Satisfiable");
					is_sat = true;
				} else {
					System.out.println("Unsatisfiable");
					is_sat = false;
				}
				
				//if problem is satisfiable
				if (is_sat) {
				//Get an ArrayList of PrimeImplicant (minimal literal to satisfy the clauses)
				IProblem problem = solver;
				int[] primes  = problem.primeImplicant();
				
				System.out.println("Prime Implicants: ");
				for (int pi = 0 ; pi < primes.length; pi++) {
					System.out.println(primes[pi]);
				}
				
				System.out.println("");
				
				if (primes.length == 0) {
					id_set = id_set - 1;
				} 
				else {
					//We are done generating the clauses and prime implicants. Working now on generating random hypothesis and their validation.
					String premise_clause = id_set + ";" + clauses + ";" + KSAT + ";" + NVAR + ";" + numClause;
					writer_clauses.println(premise_clause);
					
					ArrayList<String> temp_hypos = new ArrayList<String>();
					
					for (int nh = 1; nh <= numHypos ; nh++) 
					{
						int[] hypo = GenerateClause(KSAT, NVAR, temp_hypos);
						int[] neg_hypo = new int[hypo.length];
						boolean is_entailed = false;
						boolean is_solved = false;
						boolean is_contradict = false;
						temp_solver = solver; //Copy main solver to temp_solver for satisfiability checking
						
						String premise = "";
						
						while (temp_hypos.contains(Arrays.toString(hypo)) || Is_InPrime(hypo, primes)) {
							hypo = GenerateClause(KSAT,NVAR, temp_hypos);
						}
						temp_hypos.add(Arrays.toString(hypo));
						
						for (int literal : hypo) {
							if (Arrays.stream(primes).anyMatch(i -> i == literal))
							{
								is_entailed = true;
								is_solved = true;
								break;
							}
						}
						
						if (!is_solved) {
							neg_hypo = NegateHypothesis(hypo);
							// Add literal to temp_solver
							for (int neg : neg_hypo) {
								try {
									int[] new_lit = new int[1];
									new_lit[0] = neg;
									temp_solver.addClause(new VecInt(new_lit));
									is_solved = false;
								} catch (ContradictionException e){
									is_contradict = true;
									is_entailed = false;
									is_solved = true;
									break;
								}
							}
						}
						
						if (!is_solved) {
							if (Is_Satisfiable(temp_solver)) {
								is_entailed = false;
								is_solved = true;
							} else {
								//System.out.println("cek lagi");
								is_entailed = true;
								is_solved = true;
							}
						}
						
						//Print the hypos
						if (is_solved && is_entailed) {
							System.out.println(nh + " " + Arrays.toString(hypo)+ " E");
							premise = premise + idx + ";" + id_set + ";" + clauses + ";" + Arrays.toString(hypo) + ";" + "1" + ";" + "0" + ";" + KSAT + ";" + NVAR + ";" + numClause ;
						} else if (is_solved && !is_entailed && is_contradict) {
							System.out.println(nh + " " + Arrays.toString(hypo)+ " C");
							premise = premise + idx + ";" + id_set + ";" + clauses + ";" + Arrays.toString(hypo) + ";" + "0" + ";" + "1" + ";" + KSAT + ";" + NVAR + ";" + numClause ;
						} else if (is_solved && !is_entailed) {
							System.out.println(nh + " " + Arrays.toString(hypo)+ " N");
							premise = premise + idx + ";" + id_set + ";" + clauses + ";" + Arrays.toString(hypo) + ";" + "0" + ";" + "0" + ";" + KSAT + ";" + NVAR + ";" + numClause ;
						}
						writer.println(premise);
						idx += 1;
					}
				}
				
				System.out.println("");
				id_set += 1;
				}
				//if problem is satisfiable
				
			} //end of loop id_set
			
		    writer.close();
		    writer_clauses.close();
		    System.out.println("Successfully wrote to the file.");
		} catch(IOException e) {
			System.out.println("An error occurred.");
		    e.printStackTrace();
		}
	}
	
}
