/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cs4j.core.algorithms;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;

import org.cs4j.core.SearchDomain;
import org.cs4j.core.SearchDomain.Operator;
import org.cs4j.core.SearchDomain.State;
import org.cs4j.core.SearchResult;

/**
 * The search result class.
 *
 * @author Matthew Hatem
 */
public class SearchResultImpl implements SearchResult {

    long expanded;
    long generated;
    long duplicates;
    public long reopened;
    private long startWallTimeMillis;
    private long startCpuTimeMillis;
    private long stopWallTimeMillis;
    private long stopCpuTimeMillis;
    private List<Iteration> iterations = new ArrayList<>();
    private List<Solution> solutions = new ArrayList<>();

    @Override
    public double getExpanded() {
        return this.expanded;
    }

    @Override
    public double getGenerated () {
        return this.generated;
    }

    @Override
    public double getReopened () {
        return this.reopened;
    }

    @Override
    public List<Solution> getSolutions() {
        return solutions;
    }

    @Override
    public long getWallTimeMillis() {
        return stopWallTimeMillis - startWallTimeMillis;
    }

    @Override
    public long getCpuTimeMillis() {
        return (long)((stopCpuTimeMillis - startCpuTimeMillis) * 0.000001);
    }

    public void addSolution(Solution solution) {
        solutions.add(solution);
    }

    public void addIteration(int i, double bound, long expanded, long generated) {
        iterations.add(new Iteration(bound, expanded, generated));
    }

    public void setExpanded(long expanded) {
        this.expanded = expanded;
    }

    public void setGenerated(long generated) {
        this.generated = generated;
    }

    public void startTimer() {
        this.startWallTimeMillis = System.currentTimeMillis();
        this.startCpuTimeMillis = getCpuTime();
    }

    public void stopTimer() {
        this.stopWallTimeMillis = System.currentTimeMillis();
        this.stopCpuTimeMillis = getCpuTime();
    }

    public long getCpuTime() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        return bean.isCurrentThreadCpuTimeSupported() ?
                bean.getCurrentThreadCpuTime() : -1L;
    }
  
  /*
   * Returns the machine Id
   */
  /*private String getMachineId() {
    String uname = "unknown";
    try {
      String switches[] = new String[] {"n", "s", "r", "m"};
      String tokens[] = new String[4];
      for (int i=0; i<switches.length; i++) {
        Process p = Runtime.getRuntime().exec("uname -"+switches[i]);
        p.waitFor();
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(p.getInputStream()));
        tokens[i] = reader.readLine();
      }
      uname = tokens[0]+"-"+tokens[1]+"-"+tokens[2]+"-"+tokens[3];
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    
    return uname;
  }*/

    /*
     * The iteration class.
     */
    private static class Iteration implements SearchResult.Iteration {
        private double b;
        private long e, g;
        public Iteration(double b, long e, long g) {
            this.b = b; this.e = e; this.g = g;
        }
        @Override
        public double getBound() {
            return b;
        }
        @Override
        public long getExpanded() {
            return e;
        }
        @Override
        public long getGenerated() {
            return g;
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Nodes Generated: ");sb.append(generated);sb.append("\n");
        sb.append("Nodes Expanded: ");sb.append(expanded);sb.append("\n");
        sb.append("Total Wall Time: "+this.getWallTimeMillis());sb.append("\n");
        sb.append("Total CPU Time: "+this.getCpuTimeMillis());sb.append("\n");
        sb.append(solutions.get(0));
        return sb.toString();
    }

    static class SolutionImpl implements Solution {
        private SearchDomain domain;
        private double cost;
        private List<Operator> operators = new ArrayList<>();
        private List<State> states = new ArrayList<>();

        /**
         * A default constructor of the class
         */
        public SolutionImpl() {
            this.domain = null;
        }

        /**
         * A constructor of the class
         *
         * @param domain The domain on which the search was ran
         */
        public SolutionImpl(SearchDomain domain) {
            this.domain = domain;
        }


        @Override
        public List<Operator> getOperators() {
            return operators;
        }

        @Override
        public List<State> getStates() {
            return states;
        }

        @Override
        public double getCost() {
            return cost;
        }

        @Override
        public int getLength() {
            return operators.size();
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("Cost: ");
            sb.append(getCost());
            sb.append("\n");
            sb.append("Length: ");
            sb.append(getLength());
            sb.append("\n");
            return sb.toString();
        }

        public String dumpSolution() {
            if (this.domain != null) {
                // First, let's try dump the states of the solution, using a specific function that may be provided
                // by the domain
                String domainStatesCollectionDump = this.domain.dumpStatesCollection(
                        this.states.toArray(new State[this.states.size()]));
                if (domainStatesCollectionDump != null) {
                    return domainStatesCollectionDump;
                }
            }
            // Otherwise, let's dump state by state
            StringBuffer sb = new StringBuffer();
            for (State state: this.states) {
                sb.append(state.dumpState());
            }
            return sb.toString();
        }

        void addOperator(Operator operator) {
            this.operators.add(operator);
        }

        void addOperators(List<Operator> operators) {
            for (Operator o : operators) {
                this.operators.add(o);
            }
        }

        void addState(State state) {
            this.states.add(state);
        }

        void addStates(List<State> states) {
            for (State o : states) {
                this.states.add(o);
            }
        }

        void setCost(double cost) {
            this.cost = cost;
        }

    }

}
