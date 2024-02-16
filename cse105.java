import java.util.*;
import java.io.*;

/*
 * RESOURCES USED: 
 */

public class cse105 {

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
            return this.name.equals(obj);
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
     * REPRESENTATION OF AN NFA
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

        void addTransition(State from, char input, State to) {
            alphabet.add(input);
            transitions.computeIfAbsent(from, k -> new HashMap<>());
            transitions.get(from).computeIfAbsent(input, k -> new HashSet<>());
            transitions.get(from).get(input).add(to);
        }
    }

    /*
     * REPRESENTATION OF A DFA (MACRO-STATES CONSTRUCTION)
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

        void addTransition(Set<State> from, char input, Set<State> to) {
            alphabet.add(input);
            transitions.computeIfAbsent(from, k -> new HashMap<>());
            transitions.get(from).computeIfAbsent(input, k -> new HashSet<>());
            transitions.get(from).get(input).add(to);
        }
    }


    /*
     * TODO: All transitions including to empty set and macro states, add accept state 
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

        //

        
        dfa.addTransition(emptySet, 'a', emptySet);
        dfa.addTransition(emptySet, 'b', emptySet);

        for (Set<State> dfaState: dfa.transitions.keySet()) {
            dfa.states.add(dfaState);
        }

        return dfa;
    }

    /*
     * assume that the file containing NFA is formatted correctly
     * first line is set of states
     * second line is initial state
     * third state is set of accept states
     * rest of lines are transitions in NFA
     * Seperated by spaces
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

        try {
            File f = new File(filename);
            Scanner sc = new Scanner(f);

            HashMap<String, State> states = new HashMap<>();

            //parse through first line to add states
            String[] statesToAdd = sc.nextLine().split(" ");
            for (String state: statesToAdd) {
                states.put(state, new State(state));
                NFA.states.add(states.get(state));
            }

            //get initial state from second line
            //State initial = states.get(sc.nextLine());
            NFA.initialState = new State(sc.nextLine());

            //parse through third line to add accept states
            String[] acceptStatesToAdd = sc.nextLine().split(" ");
            for (String state: acceptStatesToAdd) {
                NFA.acceptStates.add(states.get(state));
            }

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
