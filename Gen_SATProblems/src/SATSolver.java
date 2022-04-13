import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.*;
import org.sat4j.reader.*;
import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.io.IOException;

public class SATSolver {

	public static void main(String[] args) {
		ISolver solver = SolverFactory.newDefault();
		solver.setTimeout(3600);
		Reader reader = new DimacsReader(solver);
		PrintWriter out = new PrintWriter(System.out,true);
		// CNF filename is given on the command line
		try {
			if (args.length>0) {
				IProblem problem = reader.parseInstance(args[0]);
				if (problem.isSatisfiable()) {
					System.out.println("Satisfiable");
					reader.decode(problem.model(),out);
					System.out.println("Constraints = " + problem.nConstraints());
					System.out.println("Variables = " + problem.nVars());
					
				} else {
					System.out.println("Unsatisfiable");
				}
			} else {
				System.out.println("Missing Command Line Arguments");
			}
		} catch (FileNotFoundException e) {
			System.out.println("File Not Found!");
		} catch (ParseFormatException e) {
			System.out.println(e.toString());
		} catch (IOException e) {
			System.out.println("IOException error");
		} catch (ContradictionException e) {
			System.out.println("Unsatisfiable (trivial)");
		} catch (TimeoutException e) {
			System.out.println("Timeout!");
		}
	}
}
