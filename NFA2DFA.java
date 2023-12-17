import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;


public class NFA2DFA {

    //NFA->DFA FUNCTIONS:
    //function that evaluates the transition function and returns states that can be reached via. Acts as a helper for recursiveStateFunction()
    public static Set<Integer> determineConnectedStates(ArrayList<HashMap<String,Set<Integer>>> transition, Set<Integer> states)
    {
        if(states==null) //if a null set, return the null
        {
            return states;
        }
        Set<Integer> retStates = new HashSet<>(states);
        Set<Integer> visitedStates = new HashSet<>();


        for(int state : states) //for each state in the set we're evaluating, call the recursive stateFunction
        {
            recursiveStateFunction(state,retStates,visitedStates,transition);
        }
        return retStates;
    }

    //A function that returns true if the set of NFA states exist in the DFA
    public static int containsDFAState(ArrayList<HashMap<String,Set<Integer>>> DFAStateTable, Set<Integer> states)
    {
        for(int i=0;i<DFAStateTable.size();i++)
            if(states.equals(DFAStateTable.get(i).get("value")))
                return i;
        return -1;
    }

    //Function that acts as recursion for determineConnectedStates
    private static void recursiveStateFunction(int state, Set<Integer> closedStates, Set<Integer> visitedStates, ArrayList<HashMap<String,Set<Integer>>> transition)
    {
        visitedStates.add(state); //adding state to set if not already there

        HashMap<String,Set<Integer>> curStateTransitions = transition.get(state); //grabbing current state

        if(curStateTransitions!=null)
        {
            Set<Integer> lambdaTransition = curStateTransitions.get("lambda"); //grabbing set of states from lambda transition
            for(int evaluateState : lambdaTransition) //for each state in the lambda transtion
            {
                if(!closedStates.contains(evaluateState)) //if we dont have the state in our current state set, add them and check its states recursively
                {
                    closedStates.add(evaluateState);
                    recursiveStateFunction(evaluateState, closedStates, visitedStates, transition);
                }
            }
        }
    }

    //DFA->Minimized DFA Functions:

    public static Map<String, Object> minimizeDFA(int[][] transitions, int[] acceptingStates) {
        int numStates = transitions.length;
        int numSymbols = transitions[0].length;
    
        // Step 1: Initialize the equivalent array to identify equivalent states
        boolean[][] equivalent = new boolean[numStates][numStates];
    
        // Step 2: Mark pairs of states as equivalent based on accepting and non-accepting states
        for (int i = 0; i < numStates; i++) {
            for (int j = i + 1; j < numStates; j++) {
                if ((Arrays.binarySearch(acceptingStates, i) < 0 && Arrays.binarySearch(acceptingStates, j) >= 0)
                        || (Arrays.binarySearch(acceptingStates, i) >= 0 && Arrays.binarySearch(acceptingStates, j) < 0)) {
                    equivalent[i][j] = true;
                    equivalent[j][i] = true; // Ensure symmetry
                }
            }
        }
    
        // Step 3: Refine the equivalent array until no further changes occur
        boolean changed;
        do {
            changed = false;
            for (int i = 0; i < numStates; i++) {
                for (int j = i + 1; j < numStates; j++) {
                    if (!equivalent[i][j]) {
                        for (int k = 0; k < numSymbols; k++) {
                            int nextState1 = transitions[i][k];
                            int nextState2 = transitions[j][k];
                            if (nextState1 != nextState2 && equivalent[nextState1][nextState2]) {
                                equivalent[i][j] = true;
                                equivalent[j][i] = true; // Ensure symmetry
                                changed = true;
                                break;
                            }
                        }
                    }
                }
            }
        } while (changed);
    
        // Step 4: Create a mapping between original states and minimized states
        Map<Integer, Integer> stateMapping = new HashMap<>();
        int newState = 0;
        for (int i = 0; i < numStates; i++) {
            if (!stateMapping.containsKey(i)) {
                stateMapping.put(i, newState++);
                for (int j = i + 1; j < numStates; j++) {
                    if (!equivalent[i][j] && !stateMapping.containsKey(j)) {
                        stateMapping.put(j, stateMapping.get(i));
                    }
                }
            }
        }

        // Step 5: Create the minimized transitions array
        int[][] minimizedTransitions = new int[newState][numSymbols];
        Set<Integer> minimalAcceptingStates = new HashSet<>();

        for (int i = 0; i < newState; i++) {
            int originalState = -1;
            for (Map.Entry<Integer, Integer> entry : stateMapping.entrySet()) {
                if (entry.getValue() == i) {
                    originalState = entry.getKey();
                    break;
                }
            }
            if (originalState != -1) {
                for (int j = 0; j < numSymbols; j++) {
                    minimizedTransitions[i][j] = stateMapping.get(transitions[originalState][j]);
                }
                if (Arrays.binarySearch(acceptingStates, originalState) >= 0) {
                    minimalAcceptingStates.add(i);
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("newState", newState);
        result.put("stateMapping", stateMapping);
        result.put("minimizedTransitions", minimizedTransitions);
        result.put("minimalAcceptingStates", minimalAcceptingStates);
        return result;
    }

    public static void printMinimizedDFA(int[][] minimizedTransitions, int[] acceptingStates, int newState, Map<Integer, Integer> stateMapping) {
        int numSymbols = minimizedTransitions[0].length;
    
        System.out.print("Sigma:");
        for (int i = 0; i < numSymbols; i++) {
            System.out.print(String.format("%7s", (char) ('a' + i)));
        }
        System.out.println("\n------------------------------");
    
        Set<Integer> minimalAcceptingStates = new HashSet<>();
        for (int i = 0; i < newState; i++) {
            boolean isAccepting = false;
            for (int originalState : acceptingStates) {
                if (stateMapping.get(originalState) == i) {
                    isAccepting = true;
                    break;
                }
            }
            if (isAccepting) {
                minimalAcceptingStates.add(i);
            }
        }
    
        for (int i = 0; i < newState; i++) {
            System.out.print(String.format("%5d:", i));
            for (int j = 0; j < numSymbols; j++) {
                System.out.print(String.format("%7d", minimizedTransitions[i][j]));
            }
            System.out.println();
        }
        System.out.println("------------------------------");
        System.out.println("0: Initial State");
    
        boolean isFirstAcceptingState = true;
        for (int state : minimalAcceptingStates) {
            if (!isFirstAcceptingState) {
                System.out.print(",");
            } else {
                isFirstAcceptingState = false;
            }
            System.out.print(String.format("%d", state));
        }
    
        System.out.println(": Accepting State(s)");
    }


    public static void main(String[] args) {
       
        if(args.length != 1){
                System.out.println("Incorrect usage. Enter: NFA2DFA [Filename.nfa]");
                return;
            }

        try {
            String fileName = args[0];
            File inputFile = new File(fileName);

            Scanner scanner =  new Scanner(inputFile);

            String totalNFAStatesLine = scanner.nextLine(); //reading in the Q-value, then skipping to next line
            int totalNFAStates = Integer.parseInt(totalNFAStatesLine.replaceAll("\\D","")); //parses the line and removes non-numeral values

            String [] inputSymbolsLine = scanner.nextLine().split(" "); //reads in alphabet line and splits into array of Strings
            String [] inputSymbols = new String[inputSymbolsLine.length];
            for(int i=0;i<inputSymbols.length-1;i++)
            {
                inputSymbols[i] = inputSymbolsLine[i+1];
            }

            inputSymbols[inputSymbols.length-1] = "lambda"; //including lambda

            int inputSize = inputSymbols.length-1; //Sigma letters on file (not including lambda)

            scanner.nextLine();

            ArrayList<HashMap<String,Set<Integer>>> tranFunction = new ArrayList<>(); //transition function that will hold table for transitions & states
           
            //Setting up the transition function for NFA
            for(int i=0;i<totalNFAStates;i++)
            {
                HashMap<String,Set<Integer>> curState = new HashMap<>(); //current NFA state that will be read in
                String tranString = scanner.nextLine();
                String [] seperateValues = tranString.split(" "); //transitions split into {} by spaces
                for(int j=1; j<inputSize+2;j++)
                {
                    if(seperateValues[j].matches(".\\D.*")) //if the value does not have a digit, indicating {}
                    {

                        curState.put(inputSymbols[j-1],null); //set will be null if no values
                    }
                    else if(seperateValues[j].contains(",")) //if we have multiple values, indicated by ","
                    {
                        String [] seperateStates = seperateValues[j].split(",");
                        Set<Integer> individualStates = new HashSet<>(); //designated set for hashmap
                        for(int k=0;k<seperateStates.length;k++)
                        {
                            individualStates.add(Integer.parseInt(seperateStates[k].replaceAll("\\D",""))); //placing in int array for assigned input sigma
                        }
                        curState.put(inputSymbols[j-1],individualStates);
                    }
                    else //if its just a single value in curly braces. Ex: {1}
                    {

                        Set<Integer> inputState = new HashSet<>();
                        inputState.add(Integer.parseInt(seperateValues[j].replaceAll("\\D",""))); //remove all nondigit characters
                        curState.put(inputSymbols[j-1],inputState);
                    }

                }
                tranFunction.add(curState); //adding read-in state to transition function (line)
            }

        scanner.nextLine(); //skipping line

        String initStateLine = scanner.nextLine(); //reading in the initial value, then skipping to next line
        int initState = Integer.parseInt(initStateLine.replaceAll("\\D",""));

        String acceptStateString = scanner.nextLine(); //reading accepting state line
        String[] splitAcceptString = acceptStateString.split(" "); //splitting accept state line in 3 parts (final index has all numbers)
        acceptStateString = splitAcceptString[2];

        String[] acceptStateCommaSplit = acceptStateString.split(",");
        Set<Integer> acceptStates = new HashSet<>();

        for(int i=0;i<acceptStateCommaSplit.length;i++)
            acceptStates.add(Integer.parseInt(acceptStateCommaSplit[i]));

        ArrayList<HashMap<String,Set<Integer>>> DFAStates = new ArrayList<>(); //A list of all DFAstates (ArrayList), sigma input character (String), and the corresponding array of NFAstates (int[])
        int currentDFAStateVal = 0; //counter that keeps track of current DFA state, starting at 0
        int totalDFAStatesVal = 1; //counter that keeps track of total DFA states, starting at 1

        //Setting up the intital DFA state & values
        HashMap<String,Set<Integer>> q0 = new HashMap<>();
        Set<Integer> stateZeroSet = new HashSet<>();
        stateZeroSet.add(initState);
        Set<Integer> q0States = determineConnectedStates(tranFunction,stateZeroSet);
        q0.put("value",q0States); //"Values" are the nfa states associated with current dfa state (intital here)
        DFAStates.add(q0);

        while(currentDFAStateVal<totalDFAStatesVal)
        {
            //evaluate tranFunction for # of NFAstates for curState
            Set<Integer> curDFAState = DFAStates.get(currentDFAStateVal).get("value");
            for(int i=0;i<inputSize;i++)
            {
                Set<Integer> transitionStates = new HashSet<>();
                String curInput = inputSymbols[i];
                    for(int state:curDFAState) //for each state we're acessing
                        if(determineConnectedStates(tranFunction, tranFunction.get(state).get(inputSymbols[i]))!=null) //
                            transitionStates.addAll(determineConnectedStates(tranFunction, tranFunction.get(state).get(inputSymbols[i])));
                if(containsDFAState(DFAStates, transitionStates)==-1) //if the NFA values are not in DFA as a state, then add it and increase total states
                {  
                    HashMap<String,Set<Integer>> newState = new HashMap<>();
                    newState.put("value",transitionStates);
                    DFAStates.add(newState);
                    totalDFAStatesVal++;
                }

                DFAStates.get(currentDFAStateVal).put(curInput,transitionStates);
            }
            currentDFAStateVal++;
            
        }

        int[][] DFATransitions = new int[DFAStates.size()][inputSize];

        Set<Integer> DFAAcceptingStates = new HashSet<>();

        //assess the NFA->DFA Table (DFAStates)

        System.out.println("NFA "+ fileName + " to DFA " + fileName.substring(0,1) + ".dfa");
       
        System.out.print("Sigma:   ");
        for(int i=0;i<inputSize;i++)
            System.out.print(inputSymbols[i]+ "\t");

        System.out.println("\n----------------------------------");

        for(int i=0;i<DFAStates.size();i++)
        {
            for(int state : DFAStates.get(i).get("value"))
                    if(acceptStates.contains(state))
                        DFAAcceptingStates.add(i);

            System.out.print(String.format("%5d:", i));
            System.out.print("   ");
            for(int j=0;j<inputSize;j++)
            {
                DFATransitions[i][j] = containsDFAState(DFAStates, DFAStates.get(i).get(inputSymbols[j]));
                System.out.print(DFATransitions[i][j]+ "\t");
            }
            System.out.println("");
        }

        System.out.println("----------------------------------");

        int DFAInitialState = 0;
        System.out.println(DFAInitialState + ":  Initial State");

        String acceptingStateString = "";
        int[] acceptingStates = new int[DFAAcceptingStates.size()];
        int k=0;
        for(int state: DFAAcceptingStates)
        {
            acceptingStates[k] = state;
            k++;
            acceptingStateString = acceptingStateString + state + ",";
        }

        Arrays.sort(acceptingStates);
       
        System.out.print(acceptingStateString.substring(0,acceptingStateString.length()-1) + ":");
        System.out.println("  Accepting State(s)");

        //Execute MinimizeDFA Process:

        // Minimize the DFA
        Map<String, Object> result = minimizeDFA(DFATransitions, acceptingStates);
        int newState = (int) result.get("newState");
        @SuppressWarnings("unchecked")
        Map<Integer, Integer> stateMapping = (Map<Integer, Integer>) result.get("stateMapping");
        int[][] minimizedTransitions = (int[][]) result.get("minimizedTransitions");

        // Print the minimized DFA
        System.out.println("Minimized DFA from " + fileName +" \n");
        printMinimizedDFA(minimizedTransitions, acceptingStates, newState, stateMapping);

    } catch (FileNotFoundException e) {
        System.out.println("An error has occurred with input/output");
    }
}

}

        