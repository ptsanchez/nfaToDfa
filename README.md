# NFA to DFA

This is a program built to convert a Nondeterministic Finite Automaton (NFA) to a Deterministic Finite Automaton (DFA). The conversion algorithm used is a "Macro-States" conversion that can be found in _Introduction to the Theory of Computation_ by Michael Sipser. All terminology is consistent and aligned with this textbook. 

## How the input works

This file is assumed to have been properly structured as such: The first line contains the list of space-separated states in the NFA. To build the NFA found in example.png, the first line of the input file would be "q0 q1 q2". The second line contains __only__ the initial state. The third line contains __only__ the accept state(s). The rest of the lines of the text file are for the transitions, where the first part is the beginning state, the second part is the input that the machine reads, and the third part of the line is the state to transition to. For example, the way to format the transition from $q_0$ to $q_1$ would be: "q0 a q1". By continuing this process, you can see a valid input file example.txt, which is a representation for example.png.
