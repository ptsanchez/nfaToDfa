import java.util.*;
import java.io.*;

/*
 * RESOURCES USED: ChatGPT for initial idea for program. Mainly used to give me an idea for how to represent an NFA within the program. 
 * Code generated includes the State class, Transition class, NFA class and parts of the main method for testing. 
 * I was able to build off what was generated from this to create.
 * Exact prompts are listed in the PDF writeup for this project submission.
 * 
 * Due to the fact that I chose Java as my language choice and it has been a while since I wrote code in Java, I did reference my 
 * previous work in programming assignments from CSE 12. I mainly used it to remember how to read a file to parse its contents, as well as general style.
 */
public class nfaToDfa {

    static class State {
        String name;
        State(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }

        @Override
        public boolean equals(Object obj) {
            return this.toString().equals(obj.toString());
        }
    }

    static class Transition {
        String from;
        char input;
        String to;

        Transition(String from, char input, String to) {
            this.from = from;
            this.input = input;
            this.to = to;
        }

        @Override 
        public int hashCode() {
            return Objects.hash(from, input, to);
        }
    }

    
    /**
     * Representation of an NFA
     */
    static class NFA {
        Set<State> states;
        Set<Character> alphabet;
        Map<State, Map<Character, Set<State>>> transitions;
        State initialState;
        Set<State> acceptStates;

        NFA() {
            this.states = new HashSet<>();
            this.alphabet = new HashSet<>();
            this.transitions = new HashMap<>();
            this.initialState = null;
            this.acceptStates = new HashSet<>(); 
        }

        /**
         * Adds a transition to the NFA.transitions map
         * @param from the state of the machine before reading input
         * @param input the input that transitions states
         * @param to the state of the machine after reading input
         */
        void addTransition(State from, char input, State to) {
            alphabet.add(input);
            transitions.computeIfAbsent(from, k -> new HashMap<>());
            transitions.get(from).computeIfAbsent(input, k -> new HashSet<>());
            transitions.get(from).get(input).add(to);
        }
    }

    /*
     * Representation of a DFA (Macro-States Construction)
     * See README file for clarification on terminology and specification
     */
    static class DFA {
        Set<Set<State>> states;
        Set<Character> alphabet;
        Map<Set<State>, Map<Character, Set<Set<State>>>> transitions;
        State initialState;
        Set<Set<State>> acceptStates;

        DFA() {
            this.states = new HashSet<>();
            this.alphabet = new HashSet<>();
            this.transitions = new HashMap<>();
            this.initialState = null;
            this.acceptStates = new HashSet<>();
        }

        /**
         * Adds a transition to the DFA.transitions map.
         * @param from the state of the DFA before reading input
         * @param input the input that transitions the state
         * @param to the state of the DFA after reading input
         */
        void addTransition(Set<State> from, char input, Set<State> to) {
            alphabet.add(input);
            transitions.computeIfAbsent(from, k -> new HashMap<>());
            transitions.get(from).computeIfAbsent(input, k -> new HashSet<>());
            transitions.get(from).get(input).add(to);
        }
    }
 
    /**
     * Converts a valid NFA to a DFA.
     * @param nfa the NFA to be converted to a DFA
     * @return the converted DFA
     */
    static DFA convertToDFA(NFA nfa) {
        DFA dfa = new DFA();
        dfa.initialState = nfa.initialState;
        
        //Convert NFA transitions to DFA transitions
        for (Map.Entry<State, Map<Character, Set<State>>> entry: nfa.transitions.entrySet()) {
            State state = entry.getKey();
            Map<Character, Set<State>> transition = entry.getValue();
            Set<State> states = new HashSet<>();
            states.add(state);
            for (Character chara: transition.keySet()) {
                dfa.addTransition(states, chara, transition.get(chara));
            }
        }
        

        /*
         * Create an new state "EMPTY" which is a representation of the empty set.
         * This empty state acts as a "trap state" where transitions not listed in the NFA (strings not accepted) transition to the empty set.
         * The empty set will never be an accept state.
         */
        State emptyState = new State("EMPTY");
        Set<State> emptySet = new HashSet<>();
        emptySet.add(emptyState);
        for (Set<State> dfaState: dfa.transitions.keySet()) {
            Map<Character, Set<Set<State>>> charMap = dfa.transitions.get(dfaState);
            if (!charMap.containsKey('a')) {
                dfa.addTransition(dfaState, 'a', emptySet);
            }
            if (!charMap.containsKey('b')) {
                dfa.addTransition(dfaState, 'b', emptySet);
            }
        }

        /*
        / Create additional transitions for the new macro-states contructed within the DFA.
        */
        for (Set<State> dfaState: dfa.transitions.keySet()) {
            Map<Character, Set<Set<State>>> map = dfa.transitions.get(dfaState);  
            for (char charKey: map.keySet()) {
                Set<Set<State>> toState = map.get(charKey);
                for (Set<State> setState: toState) {
                    if (toState.toString().length() > 6) {
                        for (State state: setState) {
                            if (nfa.transitions.containsKey(state)) {
                                Map<Character, Set<State>> addTransitions = nfa.transitions.get(state);
                                for (Character chara: addTransitions.keySet()) {
                                    Set<State> otherState = addTransitions.get(chara);
                                    dfa.addTransition(setState, chara, otherState);
                                }
                            }
                        }
                    }
                }
            }
        }
        
        /*
         * Any transition to the empty set means that string is not recognized by the machine, so add transitions
         * to make the empty set a trap state.
         */
        dfa.addTransition(emptySet, 'a', emptySet);
        dfa.addTransition(emptySet, 'b', emptySet);

        /*
         * If any set of states contains an accept state from the NFA, make that state an accept state for the DFA.
         */
        for (Set<State> dfaState: dfa.transitions.keySet()) {
            for (State nfaAccept: nfa.acceptStates) {
                for (State state: dfaState) {
                    if (state.toString().equals(nfaAccept.toString())) {
                        dfa.acceptStates.add(dfaState);
                    }
                }
                
            }
        }

        /*
         * Add the final list of states to the DFA.
         */
        for (Set<State> dfaState: dfa.transitions.keySet()) {
            dfa.states.add(dfaState);
        }

        return dfa;
    }

    /*
     * Input file must be fomatted correctly:
     * First line is set of states.
     * Second line is initial state.
     * Third state is set of accept states.
     * Rest of lines are transitions in NFA. These are parameters for the addTransition method for the NFA class. 
     * Please see example.txt for an example format.
     */
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            throw new FileNotFoundException("Filename not given");
        }
        else if (args.length > 1) {
            throw new Exception("Too many arguments. Only provide filname");
        }

        NFA NFA = new NFA();
        String filename = args[0];

        /*
         * Try to parse through given file from command line and build an NFA.
         */
        try {
            File f = new File(filename);
            Scanner sc = new Scanner(f);

            HashMap<String, State> states = new HashMap<>();

            /*
            * Iterate through first line to add states to NFA.
            */
            String[] statesToAdd = sc.nextLine().split(" ");
            for (String state: statesToAdd) {
                states.put(state, new State(state));
                NFA.states.add(states.get(state));
            }

            /*
             * Set initial state from second line.
             */
            NFA.initialState = new State(sc.nextLine());

            /*
             * Iterate through third line to add accept states to NFA.
             */
            String[] acceptStatesToAdd = sc.nextLine().split(" ");
            for (String state: acceptStatesToAdd) {
                NFA.acceptStates.add(states.get(state));
            }

            /*
             * Iterate through the rest of lines, adding transitions to the NFA.
             */
            while (sc.hasNextLine()) {
                String[] line = sc.nextLine().split(" ");

                NFA.addTransition(states.get(line[0]), line[1].charAt(0), states.get(line[2]));
            }
            sc.close();
        } catch (FileNotFoundException e) {
            System.out.println(e);
        } 

        System.out.println("NFA CONTENTS:\n");
        System.out.println("Set of states: " + NFA.states);
        System.out.println("Alphabet: " + NFA.alphabet);
        System.out.println("Transitions: " + NFA.transitions);
        System.out.println("Initial State: " + NFA.initialState);
        System.out.println("Set of Accepting States: " + NFA.acceptStates);
        
        DFA DFA = convertToDFA(NFA);

        System.out.println("\n-----\n");

        System.out.println("DFA CONTENTS:\n");
        System.out.println("Set of states: " + DFA.states);
        System.out.println("Alphabet: " + DFA.alphabet);
        System.out.println("Transitions: " + DFA.transitions);
        System.out.println("Initial State: " + DFA.initialState);
        System.out.println("Set of Accepting States: " + DFA.acceptStates);
    }
}
