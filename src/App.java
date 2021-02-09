import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Stack;

public class App {
    public static HashMap<String,  HashMap<String,  String>> lookupTable = new HashMap<String,  HashMap<String,  String>>();
    public static Stack<Character> grammarState = new Stack<Character>();
    public static final Set<Character> VALUES = Set.of(
        'a',  'b',  'c',  'd',  'e',  'f',  'g',  'h',  'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
    );
    public static final Set<Character> SYMBOLS = Set.of('?', '+', '*');
    public static final Set<Character> SPECIAL = Set.of('U', 'E');

    public static String FINAL_STRING = "";
    public static int ctr = 0;

    public static void populateLookupTable(){
        HashMap<String,String> S = new HashMap<>();
        HashMap<String,String> B = new HashMap<>();
        HashMap<String,String> C = new HashMap<>();
        HashMap<String,String> D = new HashMap<>();
        HashMap<String,String> A = new HashMap<>();
        HashMap<String,String> F = new HashMap<>();
        HashMap<String,String> G = new HashMap<>();
    
        S.put("(", "A$");
        S.put("x", "A$");
    
        A.put("(", "(F)C");
        A.put("x", "B");
    
        B.put("x", "xC");
    
        C.put("$", "ε");
        C.put("(", "A");
        C.put("x", "A");
        C.put(")", "ε");
        C.put("y", "yG");
        C.put("U", "D");
    
        D.put("U", "UF");
    
        F.put("(", "A");
        F.put("x", "A");
        F.put("E", "E");
    
        G.put("$", "ε");
        G.put("(", "A");
        G.put(")", "ε");
        G.put("x", "A");
        G.put("U", "D");
    
        lookupTable.put("S", S);
        lookupTable.put("A", A);
        lookupTable.put("B", B);
        lookupTable.put("C", C);
        lookupTable.put("D", D);
        lookupTable.put("F", F);
        lookupTable.put("G", G);
    }

    public static void main(String[] args) throws Exception {
        populateLookupTable();
        checkInput();
    }

    public static void checkInput() throws Exception{
        File output = new File("src/output.txt");
        FileWriter fw = new FileWriter(output);

        try{
            File input = new File("src/input.txt");
            FileReader fr = new FileReader(input);
            BufferedReader reader = new BufferedReader(fr);
            String strings;

            while ((strings = reader.readLine()) != null){
                FINAL_STRING += strings;
                String tokens = strings.replace(" ", "");

                tokens += '$';
                tokens = lexicalize(tokens);
                if(tokens.equals("Invalid")){
                    FINAL_STRING += " REJECT";
                }else{
                    checkToken(tokens);
                }
                FINAL_STRING += '\n';
            }

            reader.close();
        }catch (FileNotFoundException e) {
            System.out.println("File Not Found");
            e.printStackTrace();
        }

        fw.write(FINAL_STRING);
        fw.close();
    }

    public static String lexicalize(String input){
        String result = "";
        char[] inputChars = input.toCharArray();
        char state = 'A';
        int ctr = 0;

        while(ctr < inputChars.length){
            switch(state){
                case 'A':   if(VALUES.contains(inputChars[ctr])){ // if was used to check whether the token is in the set of [a-z] and [0-9]
                                state = 'B';
                            }else if(SYMBOLS.contains(inputChars[ctr])){// check if the char is in the symbol set
                                state = 'C';
                            }else if(inputChars[ctr] == 'U'){
                                state = 'D';
                            }else if(inputChars[ctr] == 'E'){
                                state = 'E';
                            }else if(inputChars[ctr] == '('){
                                state = 'F';
                            }else if(inputChars[ctr] == ')'){
                                state = 'G';
                            }else if(inputChars[ctr] == '$'){
                                state = 'H';
                            }else{
                                state = 'I';
                            }
                            break;

                case 'B':   if(VALUES.contains(inputChars[ctr])){
                                state = 'B';
                                ctr++;   
                            }else{
                                state = 'A';
                                result += 'x';
                            }
                            break;

                case 'C':   result += 'y'; 
                            state = 'A';
                            ctr++;
                            break;

                case 'D':   result += 'U';
                            state = 'A';
                            ctr++;
                            break;

                case 'E':   result += 'E';
                            state = 'A';
                            ctr++;
                            break;
                

                case 'F':   result += '(';
                            state = 'A';
                            ctr++;
                            break;

                case 'G':   result += ')';
                            state = 'A';
                            ctr++;
                            break;

                case 'H':   result += '$';
                            state = 'A';
                            ctr++;
                            break;
                
                case 'I':   return "Invalid";

                default:    return "Invalid";
                            
            }
        }

        return result;
    }

    public static void checkToken(String token){
        ArrayList<Character> inputTemp = new ArrayList<Character>();
        
        for(int x = 0; x < token.length(); x++){
            if(x == 0){
                if(token.charAt(0) == 'E'){
                    inputTemp.add('x');
                }else{
                    inputTemp.add(token.charAt(x));
                }
            }else{
                inputTemp.add(token.charAt(x));
            }
        }
        grammarState.push('S'); //initialize grammar state


        while(grammarState.peek() != '$' || inputTemp.get(ctr) != '$'){
            // System.out.println("Test");

            HashMap<String, String> currGrammarValue = new HashMap<String, String>();
            String currInputToken = Character.toString(inputTemp.get(ctr));
            currGrammarValue = lookupTable.get(grammarState.peek().toString());

            if(Character.isUpperCase(grammarState.peek()) && !SPECIAL.contains(grammarState.peek())){ //check if grammar top is a non terminal
                if(currGrammarValue.containsKey(currInputToken)){ //check if there is a transition input from the current grammar state
                    String temp = Character.toString(grammarState.pop());
                        
                    char[] grammarTemp = lookupTable.get(temp).get(currInputToken).toCharArray();
    
                    for(int x = grammarTemp.length - 1; x >= 0; x--){
                        if(grammarTemp[x] != 'ε')
                            grammarState.push(grammarTemp[x]);
                    }
    
                }else{
                    FINAL_STRING += " REJECT";
                    //System.out.println("REJECT");
                    break;
                }
            }else{
                if(inputTemp.get(ctr) == grammarState.peek()){ //check if input token is equal to grammar peek
                    if(inputTemp.get(ctr) == '$' && grammarState.peek() == '$')
                        break;

                    grammarState.pop();
                    ctr++;
                }else{
                    FINAL_STRING += " REJECT";
                    //System.out.println("REJECT");
                    break;
                }
            }
        }
        // System.out.println(grammarState.peek());
        // System.out.println(inputTemp.get(ctr));

        if(grammarState.peek() == '$' && inputTemp.get(ctr) == '$'){
            FINAL_STRING += " ACCEPT";
            //System.out.println("ACCEPT");
        }
        grammarState.clear();
        ctr = 0;
        inputTemp = new ArrayList<Character>();
    }
    


}
