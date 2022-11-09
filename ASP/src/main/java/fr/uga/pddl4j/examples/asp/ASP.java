/*
 * Copyright (c) 2021 by Damien Pellier <Damien.Pellier@imag.fr>.
 *
 * This file is part of PDDL4J library.
 *
 * PDDL4J is free software: you can redistribute it and/or modify * it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * PDDL4J is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License * along with PDDL4J.  If not,
 * see <http://www.gnu.org/licenses/>
 */

package fr.uga.pddl4j.examples.asp;

import fr.uga.pddl4j.heuristics.state.StateHeuristic;
import fr.uga.pddl4j.parser.DefaultParsedProblem;
import fr.uga.pddl4j.plan.Plan;
import fr.uga.pddl4j.plan.SequentialPlan;
import fr.uga.pddl4j.planners.AbstractPlanner;
import fr.uga.pddl4j.planners.Planner;
import fr.uga.pddl4j.planners.PlannerConfiguration;
import fr.uga.pddl4j.planners.SearchStrategy;
import fr.uga.pddl4j.planners.statespace.HSP;
import fr.uga.pddl4j.planners.statespace.search.StateSpaceSearch;
import fr.uga.pddl4j.problem.*;
import fr.uga.pddl4j.problem.operator.Action;
import fr.uga.pddl4j.problem.operator.Condition;
import fr.uga.pddl4j.problem.operator.ConditionalEffect;
import fr.uga.pddl4j.util.BitSet;
import fr.uga.pddl4j.util.BitVector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sat4j.core.VecInt;
import org.sat4j.maxsat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.Reader;
import java.util.*;

/**
 * The class is an example. It shows how to create a simple A* search planner able to
 * solve an ADL problem by choosing the heuristic to used and its weight.
 *
 * @author D. Pellier
 * @version 4.0 - 30.11.2021
 */
@CommandLine.Command(name = "ASP",
    version = "ASP 1.0",
    description = "Solves a specified planning problem using A* search strategy.",
    sortOptions = false,
    mixinStandardHelpOptions = true,
    headerHeading = "Usage:%n",
    synopsisHeading = "%n",
    descriptionHeading = "%nDescription:%n%n",
    parameterListHeading = "%nParameters:%n",
    optionListHeading = "%nOptions:%n")
public class ASP extends AbstractPlanner {

    /**
     * The class logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(ASP.class.getName());



    @Override
    public Problem instantiate(DefaultParsedProblem problem) {
        final Problem pb = new DefaultProblem(problem);
        pb.instantiate();
        return pb;
    }

    /**
     * Search a solution plan to a specified domain and problem using A*.
     *
     * @param problem the problem to solve.
     * @return the plan found or null if no plan was found.
     */
    @Override
    public Plan solve(final Problem problem) {

        TreeMap<Integer, Object> dico = new TreeMap<>();
        int index = 1;


        for (Fluent f : problem.getFluents()) {
            dico.put(index, f);
            index++;
        }
        for (Action a : problem.getActions()) {
            dico.put(index,a);
            index++;
        }

        final int MAXVAR = 1000000;
        final int NBCLAUSES = 500000;

        List<BitVector> listeConcat = new ArrayList<>();

            for (Action a : problem.getActions()){
                listeConcat.add(a.getPrecondition().getPositiveFluents());
                listeConcat.add(a.getPrecondition().getNegativeFluents());
                List<ConditionalEffect> effets = a.getConditionalEffects();

                for (int i = 0; i < effets.size(); i++) {
                    BitVector bv = effets.get(i).getEffect().getPositiveFluents();
                }

                for (ConditionalEffect c : ) {
                    listeConcat.add(c.getEffect().getPositiveFluents());
                    c.getEffect().getPositiveFluents()

                }
        }

        ISolver solver = SolverFactory.newDefault();

        // prepare the solver to accept MAXVAR variables. MANDATORY for MAXSAT solving
        solver.newVar(MAXVAR);
        solver.setExpectedNumberOfClauses(NBCLAUSES);
        // Feed the solver using Dimacs format, using arrays of int
        // (best option to avoid dependencies on SAT4J IVecInt)
        for (int i=0;i<NBCLAUSES;i++) {
            int [] clause = null;// get the clause from somewhere
                // the clause should not contain a 0, only integer (positive or negative)
                // with absolute values less or equal to MAXVAR
                // e.g. int [] clause = {1, -3, 7}; is fine
                // while int [] clause = {1, -3, 7, 0}; is not fine
            try {
                solver.addClause(new VecInt(clause)); // adapt Array to IVecInt
            } catch (ContradictionException e) {
                throw new RuntimeException(e);
            }
        }

        // we are done. Working now on the IProblem interface
        IProblem iproblem = solver;
        try {
            if (iproblem.isSatisfiable()) {
                //todo
            } else {
                //todo
            }
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }

        //QUAND IL Y A UN "ET" ON FAIT UN \n, QUAND IL Y A UN "OU" ON MET UN ESPACE ET IL FAUT FINIR CHAQUE LIGNE PAR UN 0
        //c c'est pour mettre un commentaire
        //p c'est pour définir un problème, on met donc p <nbVariables> <nbLignes> au début de notre problème
        //une ligne = une close


        /*
        objects A ?-block/B ?-block/table ?-X
            predicates on(x,x)/clear(x)

            1 -> on(A,B)
            2 -> on(B,A)
            3 -> on(A,table)
            4 -> on(B,table)
            5 -> clear(A)
            6 -> clear(B)
            7 -> clear(table) ? x
         */

        List<Fluent> fluents = problem.getFluents();
        System.out.println("Fluents :" + fluents.toString() + "\n" + "longueur" + fluents.size());
        List<Action> actions = problem.getActions();
        System.out.println("Fluents :" + actions.get(1) + "\n" + "longueur" + actions.size());



        /*
         * GESTION DE L'ETAT INITIAL
         * */
        InitialState etatInitial = problem.getInitialState();
        BitVector posFInit = etatInitial.getPositiveFluents();
        BitVector negFInit = etatInitial.getNegativeFluents();

        System.out.println("Fluents positifs de l'initiation"+posFInit.toString()+"\n");
        System.out.println("Fluents negatifs de l'initiation"+negFInit.toString()+"\n");

        /*
         * GESTION DU BUT
         * */
        Condition but = problem.getGoal();
        BitVector posFIBut = but.getPositiveFluents();
        BitVector negFBut = but.getNegativeFluents();



        System.out.println("Fluents positifs du but"+posFIBut.toString()+"\n");
        System.out.println("Fluents negatifs du but"+negFBut.toString()+"\n");

        System.out.println("Test affiche actions"+problem.getActions()+"\n");


        /*if(etatInitial.equals(but)){
            return plan;
        }*/




        // Return the plan found or null if the search fails.
        return null;
    }

    /**
     * The main method of the <code>ASP</code> planner.
     *
     * @param args the arguments of the command line.
     */
    public static void main(String[] args) {
        try {
            final ASP planner = new ASP();
            CommandLine cmd = new CommandLine(planner);
            cmd.execute("domain.pddl", "p001.pddl");
        } catch (IllegalArgumentException e) {
            LOGGER.fatal(e.getMessage());
        }


        /**
         * SAT4J*

         ISolver solver = SolverFactory.newDefault();
         solver.setTimeout(3600); // 1 hour timeout
         Reader reader = new DimacsReader(solver);
         PrintWriter out = new PrintWriter(System.out,true);
         // CNF filename is given on the command line
         try {
         IProblem problem = reader.parseInstance(args[0]);
         if (problem.isSatisfiable()) {
         System.out.println("Satisfiable !");
         reader.decode(problem.model(),out);
         } else {
         System.out.println("Unsatisfiable !");
         }
         } catch (Exception e) {
         System.out.println("Problème au niveau du solver SAT (pour plus de détails retourner voir tous les catch)");
         }*/
    }
}
