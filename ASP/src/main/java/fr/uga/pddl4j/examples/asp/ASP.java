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

        ISolver solver = SolverFactory.newDefault();


        for (Fluent f : problem.getFluents()) {
            dico.put(index, f);
            index++;
        }
        for (Action a : problem.getActions()) {
            dico.put(index,a);
            index++;
        }

        try {
            for (Action a : problem.getActions()) {

                List<Integer> clause = new ArrayList<>();

                for (int val : a.getPrecondition().getPositiveFluents().stream().toArray()) {     //Préconditions positives
                    if (val != 0)
                        clause.add(val);
                }
                for (int val : a.getPrecondition().getNegativeFluents().stream().toArray()) {     //Préconditions négatives
                    if (val != 0)
                        clause.add(val);
                }
                List<ConditionalEffect> effets = a.getConditionalEffects();

                for (int i = 0; i < effets.size(); i++) {                                        //Fluents positifs et négatifs des effets des actions
                    int[] posFluents = effets.get(i).getEffect().getPositiveFluents().stream().toArray();
                    int[] negFluents = effets.get(i).getEffect().getNegativeFluents().stream().toArray();
                    int[] posFluentsInv = new int[posFluents.length];
                    int[] negFluentsInv = new int[negFluents.length];

                    for (int j = 0; j < posFluents.length; j++) {
                        if (posFluentsInv[j] != 0) {
                            posFluentsInv[j] = (posFluents[j] * -1);
                            clause.add(posFluentsInv[j]);
                        }
                    }
                    for (int j = 0; j < negFluents.length; j++) {
                        if (negFluentsInv[j] != 0) {
                            negFluentsInv[j] = (negFluentsInv[j] * -1);
                            clause.add(negFluentsInv[j]);
                        }
                    }
                }
                int[] clause_int = clause.stream().mapToInt(i -> i).toArray();
                solver.addClause(new VecInt(clause_int)); // adapt Array to IVecInt
            }
        } catch (ContradictionException e) {
            throw new RuntimeException(e);
        }

        //IProblem iproblem = solver;
        try {
            if (solver.isSatisfiable()) {
                System.out.println("le problème est satisfiable");
                for(int num : solver.model()){
                    //on récupère les fluents et les actions dans notre dictionnaire
                    Object result = dico.get(num);
                    if (result instanceof Action) {
                        Action a = (Action)result;
                        System.out.println(a.getName());
                    }
                    if (result instanceof Fluent) {
                        Fluent f = (Fluent)result;
                        System.out.println(f.toString());
                    }
                    System.out.println(num);
                }
            } else {
                System.out.println("");

            }
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }

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
    }
}
