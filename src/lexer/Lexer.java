package lexer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Lexer {

    static String filename;
    static ArrayList<String> keywords = new ArrayList<>();
    public static ArrayList<Integer> tokensInLine = new ArrayList <>();
    public boolean success;

    public Lexer()
    {
        initKeywords();
        success = false;
    }

    public boolean setFilename(String file)
    {
        filename = file;
        //check for valid file using the extension .cc of the filename
        if (!filename.endsWith(".cc")) {
            System.out.println("Incorrect file! The file extension should be (.cc)");
            return false;
        }
        return true;
    }

    //function to initialize all keywords of our language
    static void initKeywords() {
        keywords.add("int");
        keywords.add("char");
        keywords.add("if");
        keywords.add("elif");
        keywords.add("else");
        keywords.add("while");
        keywords.add("input");
        keywords.add("print");
        keywords.add("println");
    }

    //function to check whether the given string is a keyword or not
    static boolean checkKeyword(String s) {
        for (String i : keywords) {
            if (i.equalsIgnoreCase(s))
                return true;
        }
        return false;
    }

    public void startLexicalAnalysis() throws IOException {
        int line = 1;           //keeps track of which line is currently being read
        int state = 0;          //keeps track of current state during DFA transitions

        //creating readers and writers for filing
        File myFile = new File(filename);
        FileWriter myWriter = new FileWriter("tokens.txt");
        FileWriter myWriter2 = new FileWriter("readable_tokens.txt");
        Scanner myReader = new Scanner(myFile);

        StringBuilder commentValue = new StringBuilder();   //stores comment if encountered

        //scan until EOF
        while (myReader.hasNextLine()) {
            int inLineTokens = 0;
            String data = myReader.nextLine();          //read a line from code file
            String removedWhiteSpace = data.trim();     //removing leading and trailing whitespaces, tabs and newlines
            myWriter2.write(line + "\t" + removedWhiteSpace + "\n");
            System.out.println(line + "\t" + removedWhiteSpace);

            char[] ch = removedWhiteSpace.toCharArray();        //converting the read data into characters in array form
            int integerValue = 0;                               //stores integer if encountered
            StringBuilder wordValue = new StringBuilder();      //stores a word if encountered
            StringBuilder literalValue = new StringBuilder();   //stores literal if encountered
            StringBuilder stringValue = new StringBuilder();    //stores string if encountered

            for (int i = 0; i < ch.length; i++) {
                //System.out.println(ch[i]);
                String token;   //to store formatted (token,lexeme) pair for printing

                //Mapping of DFA transitions based on state and labels (currently read character)
                switch (state) {
                    case 0:
                        if (ch[i] == '<') {
                            state = 1;
                            break;
                        } else if (ch[i] == '>') {
                            state = 4;
                            break;
                        } else if (ch[i] == '~') {
                            state = 7;
                            break;
                        } else if (ch[i] == '=') {
                            state = 9;
                            break;
                        } else if (ch[i] == ':') {
                            state = 12;
                            i--;
                            break;
                        } else if (ch[i] == ';') {
                            state = 13;
                            i--;
                            break;
                        } else if (ch[i] == ',') {
                            state = 14;
                            i--;
                            break;
                        } else if (ch[i] == '(') {
                            state = 15;
                            i--;
                            break;
                        } else if (ch[i] == ')') {
                            state = 16;
                            i--;
                            break;
                        } else if (ch[i] == '[') {
                            state = 17;
                            i--;
                            break;
                        } else if (ch[i] == ']') {
                            state = 18;
                            i--;
                            break;
                        } else if (ch[i] == '{') {
                            state = 19;
                            i--;
                            break;
                        } else if (ch[i] == '}') {
                            state = 20;
                            i--;
                            break;
                        } else if (Character.isDigit(ch[i])) {
                            state = 21;
                            integerValue += Character.getNumericValue(ch[i]);
                            break;
                        } else if (ch[i] == '+') {
                            state = 23;
                            i--;
                            break;
                        } else if (ch[i] == '*') {
                            state = 24;
                            i--;
                            break;
                        }
                        else if (ch[i] == '-') {
                            state = 26;
                            break;
                        } else if (Character.isLetter(ch[i])) {
                            state = 29;
                            wordValue.append(ch[i]);
                            break;
                        } else if (ch[i] == '\'') {
                            state = 31;
                            literalValue.append(ch[i]);
                            break;
                        } else if (ch[i] == '\"') {
                            state = 34;
                            stringValue.append(ch[i]);
                            break;
                        } else if (ch[i] == '/') {
                            state = 36;
                            commentValue.append(ch[i]);
                            break;
                        } else if (ch[i] == ' ') {
                            break;
                        } else {
                            //error statement
                            System.out.println("Unknown symbol " + ch[i] + " found at line " + line);
                            System.out.println(line + "\t" + removedWhiteSpace);
                            System.out.println("Check your code file and try again!");
                            myWriter.close();
                            myWriter2.close();
                            myReader.close();
                            return;     //terminate program
                        }
                    case 1:
                        if (ch[i] == '=') {
                            state = 2;
                            i--;
                            break;
                        } else {
                            state = 3;
                            i--;
                            break;
                        }
                    case 2:
                        token = "(REL_OP,LE)";
                        myWriter.write(token + "\n");
                        myWriter2.write("\t" + token + "\n");
                        System.out.println("\t" + token);
                        state = 0; //resetting the DFA
                        inLineTokens++;
                        break;
                    case 3:
                        token = "(REL_OP,LT)";
                        myWriter.write(token + "\n");
                        myWriter2.write("\t" + token + "\n");
                        System.out.println("\t" + token);
                        state = 0; //resetting the DFA
                        i--;
                        inLineTokens++;
                        break;
                    case 4:
                        if (ch[i] == '=') {
                            state = 5;
                            i--;
                            break;
                        } else {
                            state = 6;
                            i--;
                            break;
                        }
                    case 5:
                        token = "(REL_OP,GE)";
                        myWriter.write(token + "\n");
                        myWriter2.write("\t" + token + "\n");
                        System.out.println("\t" + token);
                        state = 0; //resetting the DFA
                        inLineTokens++;
                        break;
                    case 6:
                        token = "(REL_OP,GT)";
                        myWriter.write(token + "\n");
                        myWriter2.write("\t" + token + "\n");
                        System.out.println("\t" + token);
                        state = 0; //resetting the DFA
                        i--;
                        inLineTokens++;
                        break;
                    case 7:
                        if (ch[i] == '=') {
                            state = 8;
                            i--;
                            break;
                        } else {
                            //error statement
                            System.out.println("The symbol '~' is not defined in this language as standalone at line " + line);
                            System.out.println(line + "\t" + removedWhiteSpace);
                            System.out.println("Check your code file and try again!");
                            myWriter.close();
                            myWriter2.close();
                            myReader.close();
                            return;     //terminate program
                        }
                    case 8:
                        token = "(REL_OP,NE)";
                        myWriter.write(token + "\n");
                        myWriter2.write("\t" + token + "\n");
                        System.out.println("\t" + token);
                        state = 0; //resetting the DFA
                        inLineTokens++;
                        break;
                    case 9:
                        if (ch[i] == '=') {
                            state = 10;
                            i--;
                            break;
                        } else {
                            state = 11;
                            i--;
                            break;
                        }
                    case 10:
                        token = "(REL_OP,EQ)";
                        myWriter.write(token + "\n");
                        myWriter2.write("\t" + token + "\n");
                        System.out.println("\t" + token);
                        state = 0; //resetting the DFA
                        inLineTokens++;
                        break;
                    case 11:
                        token = "('=',^)";
                        myWriter.write(token + "\n");
                        myWriter2.write("\t" + token + "\n");
                        System.out.println("\t" + token);
                        state = 0; //resetting the DFA
                        inLineTokens++;
                        i--;
                        break;
                    case 12:
                        token = "(':',^)";
                        myWriter.write(token + "\n");
                        myWriter2.write("\t" + token + "\n");
                        System.out.println("\t" + token);
                        state = 0; //resetting the DFA
                        inLineTokens++;
                        break;
                    case 13:
                        token = "(';',^)";
                        myWriter.write(token + "\n");
                        myWriter2.write("\t" + token + "\n");
                        System.out.println("\t" + token);
                        state = 0; //resetting the DFA
                        inLineTokens++;
                        break;
                    case 14:
                        token = "(',',^)";
                        myWriter.write(token + "\n");
                        myWriter2.write("\t" + token + "\n");
                        System.out.println("\t" + token);
                        state = 0; //resetting the DFA
                        inLineTokens++;
                        break;
                    case 15:
                        token = "('(',^)";
                        myWriter.write(token + "\n");
                        myWriter2.write("\t" + token + "\n");
                        System.out.println("\t" + token);
                        state = 0; //resetting the DFA
                        inLineTokens++;
                        break;
                    case 16:
                        token = "(')',^)";
                        myWriter.write(token + "\n");
                        myWriter2.write("\t" + token + "\n");
                        System.out.println("\t" + token);
                        state = 0; //resetting the DFA
                        inLineTokens++;
                        break;
                    case 17:
                        token = "('[',^)";
                        myWriter.write(token + "\n");
                        myWriter2.write("\t" + token + "\n");
                        System.out.println("\t" + token);
                        state = 0; //resetting the DFA
                        inLineTokens++;
                        break;
                    case 18:
                        token = "(']',^)";
                        myWriter.write(token + "\n");
                        myWriter2.write("\t" + token + "\n");
                        System.out.println("\t" + token);
                        state = 0; //resetting the DFA
                        inLineTokens++;
                        break;
                    case 19:
                        token = "('{',^)";
                        myWriter.write(token + "\n");
                        myWriter2.write("\t" + token + "\n");
                        System.out.println("\t" + token);
                        state = 0; //resetting the DFA
                        inLineTokens++;
                        break;
                    case 20:
                        token = "('}',^)";
                        myWriter.write(token + "\n");
                        myWriter2.write("\t" + token + "\n");
                        System.out.println("\t" + token);
                        state = 0; //resetting the DFA
                        inLineTokens++;
                        break;
                    case 21:
                        if (Character.isDigit(ch[i])) {
                            //forming the appropriate integer
                            integerValue *= 10;
                            integerValue += Character.getNumericValue(ch[i]);
                            break;
                        } else {
                            state = 22;
                            i--;
                            break;
                        }
                    case 22:
                        token = "(NUM," + integerValue + ")";
                        myWriter.write(token + "\n");
                        myWriter2.write("\t" + token + "\n");
                        System.out.println("\t" + token);
                        state = 0; //resetting the DFA
                        i--;
                        integerValue = 0;
                        inLineTokens++;
                        break;
                    case 23:
                        token = "('+',^)";
                        myWriter.write(token + "\n");
                        myWriter2.write("\t" + token + "\n");
                        System.out.println("\t" + token);
                        state = 0; //resetting the DFA
                        inLineTokens++;
                        break;
                    case 24:
                        token = "('*',^)";
                        myWriter.write(token + "\n");
                        myWriter2.write("\t" + token + "\n");
                        System.out.println("\t" + token);
                        state = 0; //resetting the DFA
                        inLineTokens++;
                        break;
                    case 25:
                        token = "('/',^)";
                        myWriter.write(token + "\n");
                        myWriter2.write("\t" + token + "\n");
                        System.out.println("\t" + token);
                        state = 0; //resetting the DFA
                        i--;
                        inLineTokens++;
                        break;
                    case 26:
                        if (ch[i] == '>') {
                            state = 27;
                            i--;
                            break;
                        } else {
                            state = 28;
                            i--;
                            break;
                        }
                    case 27:
                        token = "(INPUT_OP,^)";
                        myWriter.write(token + "\n");
                        myWriter2.write("\t" + token + "\n");
                        System.out.println("\t" + token);
                        state = 0; //resetting the DFA
                        inLineTokens++;
                        break;
                    case 28:
                        token = "('-',^)";
                        myWriter.write(token + "\n");
                        myWriter2.write("\t" + token + "\n");
                        System.out.println("\t" + token);
                        state = 0; //resetting the DFA
                        i--;
                        inLineTokens++;
                        break;
                    case 29:
                        if (Character.isLetter(ch[i]) || Character.isDigit(ch[i]) || ch[i] == '_') {
                            wordValue.append(ch[i]);
                            break;
                        } else {
                            state = 30;
                            i--;
                            break;
                        }
                    case 30:
                        if (checkKeyword(wordValue.toString())) {
                            token = "(" + wordValue.toString().toUpperCase() + ",^)";
                            myWriter.write(token + "\n");
                            myWriter2.write("\t" + token + "\n");
                            System.out.println("\t" + token);
                            state = 0; //resetting the DFA
                            i--;
                            wordValue.setLength(0); //resetting the string builder
                            inLineTokens++;
                            break;
                        } else {
                            token = "(ID," + wordValue.toString() + ")";
                            myWriter.write(token + "\n");
                            myWriter2.write("\t" + token + "\n");
                            System.out.println("\t" + token);
                            state = 0; //resetting the DFA
                            i--;
                            wordValue.setLength(0); //resetting the string builder
                            inLineTokens++;
                            break;
                        }
                    case 31:
                        if (Character.isLetter(ch[i])) {
                            state = 32;
                            literalValue.append(ch[i]); //forming the literal
                            break;
                        } else {
                            //error statement
                            System.out.println("Invalid symbol following ' for literal at line " + line);
                            System.out.println(line + "\t" + removedWhiteSpace);
                            System.out.println("Check your code file and try again!");
                            myWriter.close();
                            myWriter2.close();
                            myReader.close();
                            return; //terminate program
                        }
                    case 32:
                        if (ch[i] == '\'') {
                            state = 33;
                            literalValue.append(ch[i]); //forming the literal
                            i--;
                            break;
                        } else {
                            //error statement
                            System.out.println("Literal not enclosed properly at line " + line);
                            System.out.println(line + "\t" + removedWhiteSpace);
                            System.out.println("Check your code file and try again!");
                            myWriter.close();
                            myWriter2.close();
                            myReader.close();
                            return; //terminate program
                        }
                    case 33:
                        token = "(LIT," + literalValue.toString() + ")";
                        myWriter.write(token + "\n");
                        myWriter2.write("\t" + token + "\n");
                        System.out.println("\t" + token);
                        state = 0; //resetting the DFA
                        literalValue.setLength(0); //resetting the string builder
                        inLineTokens++;
                        break;
                    case 34:
                        if (ch[i] == '\"') {
                            state = 35;
                            stringValue.append(ch[i]);
                            i--;
                            break;
                        } else if (i == ch.length - 1) {
                            //error statement
                            System.out.println("String not enclosed properly at line " + line);
                            System.out.println(line + "\t" + removedWhiteSpace);
                            System.out.println("Check your code file and try again!");
                            myWriter.close();
                            myWriter2.close();
                            myReader.close();
                            return; //terminate program
                        } else {
                            stringValue.append(ch[i]);  //forming the string
                            break;
                        }
                    case 35:
                        token = "(STR," + stringValue.toString() + ")";
                        myWriter.write(token + "\n");
                        myWriter2.write("\t" + token + "\n");
                        System.out.println("\t" + token);
                        state = 0; //resetting the DFA
                        stringValue.setLength(0); //resetting the string builder
                        inLineTokens++;
                        break;
                    case 36:
                        if (ch[i] == '/') {
                            state = 37;
                            commentValue.append(ch[i]); //forming the comment
                            break;
                        } else if (ch[i] == '*') {
                            state = 39;
                            commentValue.append(ch[i]); //forming the comment
                            break;
                        } else {
                            state = 25;
                            commentValue.setLength(0); //resetting the string builder
                            i--;
                            break;
                        }
                    case 37:
                        commentValue.append(ch[i]); //forming the comment
                        if (i == ch.length - 1) {
                            state = 38;
                            i--;
                        }
                        break;
                    case 38:
                        token = "(S_COMMENT," + commentValue.toString() + ")";
                        myWriter.write(token + "\n");
                        myWriter2.write("\t" + token + "\n");
                        System.out.println("\t" + token);
                        state = 0; //resetting the DFA
                        commentValue.setLength(0); //resetting the string builder
                        inLineTokens++;
                        break;
                    case 39:
                        commentValue.append(ch[i]); //forming the comment
                        if (ch[i] == '*') {
                            state = 40;
                        } else if (i == ch.length - 1 && myReader.hasNextLine()) {
                            commentValue.append(' '); //forming the comment by replacing newline with space
                        } else if (i == ch.length - 1 && !myReader.hasNextLine()) {
                            //error statement
                            System.out.println("End of file reached and Multi line comment not enclosed properly at line " + line);
                            System.out.println(line + "\t" + removedWhiteSpace);
                            System.out.println("Check your code file and try again!");
                            myWriter.close();
                            myWriter2.close();
                            myReader.close();
                            return; //terminate program
                        }
                        break;
                    case 40:
                        if (ch[i] == '/') {
                            commentValue.append(ch[i]); //forming the comment
                            state = 41;
                            i--;
                            break;
                        } else {
                            //error statement
                            System.out.println("Multi line comment not enclosed properly at line " + line);
                            System.out.println(line + "\t" + removedWhiteSpace);
                            System.out.println("Check your code file and try again!");
                            myWriter.close();
                            myWriter2.close();
                            myReader.close();
                            return; //terminate program
                        }
                    case 41:
                        token = "(M_COMMENT," + commentValue.toString() + ")";
                        myWriter.write(token + "\n");
                        myWriter2.write("\t" + token + "\n");
                        System.out.println("\t" + token);
                        state = 0; //resetting the DFA
                        commentValue.setLength(0); //resetting the string builder
                        inLineTokens++;
                        break;
                }
            }
            line++; //increase the line number on newline encounter
            tokensInLine.add(inLineTokens);
        }

        System.out.println("\nLexer execution successful! Check the generated tokens in tokens.txt");     //success message(no error scenario)
        success = true;
        
        //for (int t:tokensInLine) { System.out.println(t); }

        //close all file readers and writers
        myWriter.close();
        myWriter2.close();
        myReader.close();
    }
}