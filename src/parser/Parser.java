package parser;

import lexer.Lexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Parser {

    static int line = 0;                //used to track line number when parsing
    static int tokenCounter = 0;        //used to update line
    static int parsetreeDepth = 0;      //used to track depth when printing parse tree
    static String filename;             //name/path of code file given by user

    public void setFilename(String file)
    {
        filename = file;
    }

    static void writeToSymbolTable(String s) throws IOException
    {
        FileWriter writer = new FileWriter("parser-symboltable.txt",true);
        writer.append(s+'\n');
        writer.close();
    }

    static String[] extractToken(Scanner reader) throws FileNotFoundException
    {
        String[] token = new String[2];
        String data = "";

        if (reader.hasNext()) {
            data = reader.nextLine(); //reading (token,lexeme) from file
        } else {
            SyntaxError();
        }

        //skipping code lines with no token
        while(Lexer.tokensInLine.get(line) == 0)
        {
            line++;
        }
        tokenCounter++;

        String tempToken = data.substring(1, data.length() - 1); //extracting inner part
        if (tempToken.startsWith("','")) {
            token[0] = "','";
            token[1] = "^";
        } else {
            String[] tokenList = tempToken.split(",");
            token[0] = tokenList[0];
            token[1] = tokenList[1];
        }
        //System.out.println(token[0]+"   "+token[1]);

        //updating tokenCounter and line accordingly
        if(tokenCounter > Lexer.tokensInLine.get(line))
        {
            tokenCounter = 1;
            line++;
            while(Lexer.tokensInLine.get(line) == 0)
            {
                line++;
            }
        }

        return  token;
    }

    static void SyntaxError() throws FileNotFoundException
    {
        File tempF = new File(filename);
        Scanner tempR = new Scanner(tempF);

        String tempStr="";
        for(int i=0;i<=line;i++)
        {
            tempStr = tempR.nextLine();
        }
        tempStr=tempStr.trim();
        System.out.println("Syntax error at line "+(line+1));
        System.out.println((line+1)+"\t"+tempStr);
        System.out.println("Review your code and try again");
        tempR.close();
        System.exit(0);
    }

    static void outputParser(FileWriter writer, String str) throws IOException
    {
        String outputDepthPattern = "";
        for(int i=0; i<parsetreeDepth;i++)
        {
            outputDepthPattern+="==>";
        }
        writer.write(outputDepthPattern+str+'\n');
    }

    static void Start(String[] token, FileWriter writer, Scanner reader) throws IOException
    {
        parsetreeDepth++;
        String attachStr;

        if(token[0].equals("IF") || token[0].equals("WHILE") || token[0].equals("PRINT") || token[0].equals("PRINTLN") || token[0].equals("INT") || token[0].equals("CHAR") || token[0].equals("ID") || token[0].equals("INPUT") || token[0].equals("S_COMMENT") || token[0].equals("M_COMMENT"))
        {
            attachStr = " Statements";
            outputParser(writer, attachStr);
            Statements(token, writer, reader);
        }
        else
        {
            SyntaxError();
        }

        parsetreeDepth--;
    }

    static void Statements(String[] token, FileWriter writer, Scanner reader) throws IOException
    {
        parsetreeDepth++;

        String attachStr = " Statement";
        outputParser(writer, attachStr);
        Statement(token, writer, reader);

        attachStr = " Statements";
        outputParser(writer, attachStr);

        if(reader.hasNext(Pattern.quote("(IF,^)")) || reader.hasNext(Pattern.quote("(WHILE,^)")) || reader.hasNext(Pattern.quote("(PRINT,^)")) || reader.hasNext(Pattern.quote("(PRINTLN,^)")) || reader.hasNext(Pattern.quote("(INPUT,^)")) || reader.hasNext(Pattern.quote("(INT,^)")) || reader.hasNext(Pattern.quote("(CHAR,^)")) || reader.hasNext("\\(ID.*") || reader.hasNext("\\(S_COMMENT.*") || reader.hasNext("\\(M_COMMENT.*"))
        {
            token = extractToken(reader);
            Statements(token, writer, reader);
        }
        else
        {
            parsetreeDepth++;
            attachStr = " ^";
            outputParser(writer, attachStr);
            parsetreeDepth--;
        }

        parsetreeDepth--;
    }

    static void Statement(String[] token, FileWriter writer, Scanner reader) throws IOException
    {
        parsetreeDepth++;
        String attachStr;

        //IF or WHILE case
        if(token[0].equals("IF") || token[0].equals("WHILE"))
        {
            boolean ifFlag = false;
            if(token[0].equals("IF"))
            {
                ifFlag = true;
            }
            attachStr = " "+token[0];
            outputParser(writer, attachStr);

            token = extractToken(reader);
            if(token[0].contains("'('"))
            {
                attachStr = " (";
                outputParser(writer, attachStr);
                token = extractToken(reader);
            }

            attachStr = " Condition";
            outputParser(writer, attachStr);
            Condition(token, writer, reader);

            token = extractToken(reader);
            if(token[0].contains("')'"))
            {
                attachStr = " )";
                outputParser(writer, attachStr);
                token = extractToken(reader);
            }

            if(token[0].contains("':'"))
            {
                attachStr = " :";
                outputParser(writer, attachStr);
            }
            else
            {
                SyntaxError();
            }

            token = extractToken(reader);
            if(token[0].contains("'{'"))
            {
                attachStr = " {";
                outputParser(writer, attachStr);
                writeToSymbolTable("SCOPE START");
            }
            else
            {
                SyntaxError();
            }

            token = extractToken(reader);
            attachStr = " Statements";
            outputParser(writer, attachStr);
            Statements(token, writer, reader);

            token = extractToken(reader);
            if(token[0].contains("'}'"))
            {
                attachStr = " }";
                outputParser(writer, attachStr);
                writeToSymbolTable("SCOPE END");
            }
            else
            {
                SyntaxError();
            }

            if(ifFlag)
            {
                if(reader.hasNext(Pattern.quote("(ELIF,^)")) || reader.hasNext(Pattern.quote("(ELSE,^)")))
                {
                    token = extractToken(reader);
                }
                attachStr = " ElifOrElse";
                outputParser(writer, attachStr);
                ElifOrElse(token, writer, reader);
            }
        }
        //PRINT and PRINTLN case
        else if(token[0].equals("PRINT") || token[0].equals("PRINTLN"))
        {
            boolean lnFlag = false;
            if(token[0].equals("PRINTLN"))
            {
                lnFlag = true;
            }
            attachStr = " "+token[0];
            outputParser(writer, attachStr);

            token = extractToken(reader);
            if(token[0].contains("'('"))
            {
                attachStr = " (";
                outputParser(writer, attachStr);
            }
            else
            {
                SyntaxError();
            }

            attachStr = " OutputOptions";
            outputParser(writer, attachStr);
            String option = OutputOptions(token, writer, reader);

            if(option.length() > 0)
            {
                token = extractToken(reader);
            }
            if(token[0].contains("')'") || option.length() == 0)
            {
                attachStr = " )";
                outputParser(writer, attachStr);
            }
            else
            {
                SyntaxError();
            }

            token = extractToken(reader);
            if(token[0].contains("';'"))
            {
                attachStr = " ;";
                outputParser(writer, attachStr);
            }
            else
            {
                SyntaxError();
            }
        }
        //INPUT case
        else if(token[0].equals("INPUT"))
        {
            attachStr = " "+token[0];
            outputParser(writer, attachStr);
            token = extractToken(reader);
            if(token[0].equals("INPUT_OP"))
            {
                attachStr = " INPUT_OP";
                outputParser(writer, attachStr);
                INPUT_OP(token, writer, reader);
            }
            else
            {
                SyntaxError();
            }

            token = extractToken(reader);
            if(token[0].equals("ID"))
            {
                attachStr = " ID("+token[1]+")";
                outputParser(writer, attachStr);
            }
            else
            {
                SyntaxError();
            }

            token = extractToken(reader);
            attachStr = " inputDelimiter";
            outputParser(writer, attachStr);
            inputDelimiter(token, writer, reader);
        }
        //Assignment and IncOp/DecOp case
        else if(token[0].equals("ID"))
        {
            if(reader.hasNext(Pattern.quote("('+',^)")))
            {
                attachStr = " IncOp";
                outputParser(writer, attachStr);
                IncOp(token, writer, reader);
            }
            else if(reader.hasNext(Pattern.quote("('-',^)")))
            {
                attachStr = " DecOp";
                outputParser(writer, attachStr);
                DecOp(token, writer, reader);
            }
            else if(reader.hasNext(Pattern.quote("('=',^)")))
            {
                attachStr = " AssignmentStatement";
                outputParser(writer, attachStr);
                AssignmentStatement(token, writer, reader);
            }
            else
            {
                SyntaxError();
            }
        }
        //Variable case
        else if(token[0].equals("INT") || token[0].equals("CHAR"))
        {
            attachStr = " Variable";
            outputParser(writer, attachStr);
            Variable(token, writer, reader);
        }
        //Comments case
        else if(token[0].equals("S_COMMENT") || token[0].equals("M_COMMENT"))
        {
            attachStr = " "+token[0];
            outputParser(writer, attachStr);
        }
        else
        {
            SyntaxError();
        }

        parsetreeDepth--;
    }

    static void ElifOrElse(String[] token, FileWriter writer, Scanner reader) throws IOException
    {
        parsetreeDepth++;
        String attachStr;

        if(token[0].equals("ELIF"))
        {
            attachStr = " elif";
            outputParser(writer, attachStr);

            token = extractToken(reader);
            if(token[0].contains("'('"))
            {
                attachStr = " (";
                outputParser(writer, attachStr);
                token = extractToken(reader);
            }

            attachStr = " Condition";
            outputParser(writer, attachStr);
            Condition(token, writer, reader);

            token = extractToken(reader);
            if(token[0].contains("')'"))
            {
                attachStr = " )";
                outputParser(writer, attachStr);
                token = extractToken(reader);
            }
            if(token[0].contains("':'"))
            {
                attachStr = " :";
                outputParser(writer, attachStr);
            }
            else
            {
                SyntaxError();
            }

            token = extractToken(reader);
            if(token[0].contains("'{'"))
            {
                attachStr = " {";
                outputParser(writer, attachStr);
                writeToSymbolTable("SCOPE START");
            }
            else
            {
                SyntaxError();
            }

            token = extractToken(reader);
            attachStr = " Statements";
            outputParser(writer, attachStr);
            Statements(token, writer, reader);

            token = extractToken(reader);
            if(token[0].contains("'}'"))
            {
                attachStr = " }";
                outputParser(writer, attachStr);
                writeToSymbolTable("SCOPE END");
            }
            else
            {
                SyntaxError();
            }

            if(reader.hasNext(Pattern.quote("(ELIF,^)")) || reader.hasNext(Pattern.quote("(ELSE,^)")))
            {
                token = extractToken(reader);
            }
            attachStr = " ElifOrElse";
            outputParser(writer, attachStr);
            ElifOrElse(token, writer, reader);
        }
        else
        {
            attachStr = " Else";
            outputParser(writer, attachStr);
            Else(token, writer, reader);
        }

        parsetreeDepth--;
    }

    static void Else(String[] token, FileWriter writer, Scanner reader) throws IOException
    {
        parsetreeDepth++;
        String attachStr;

        if(token[0].equals("ELSE"))
        {
            attachStr = " else";
            outputParser(writer, attachStr);

            token = extractToken(reader);
            if(token[0].contains("'{'"))
            {
                attachStr = " {";
                outputParser(writer, attachStr);
                writeToSymbolTable("SCOPE START");
            }
            else
            {
                SyntaxError();
            }

            token = extractToken(reader);
            attachStr = " Statements";
            outputParser(writer, attachStr);
            Statements(token, writer, reader);

            token = extractToken(reader);
            if(token[0].contains("'}'"))
            {
                attachStr = " }";
                outputParser(writer, attachStr);
                writeToSymbolTable("SCOPE END");
            }
            else
            {
                SyntaxError();
            }
        }
        else
        {
            attachStr = " ^";
            outputParser(writer, attachStr);
        }

        parsetreeDepth--;
    }

    static String Expression(String[] token, FileWriter writer, Scanner reader) throws IOException
    {
        parsetreeDepth++;
        String getValue;

        String attachStr = " Term";
        outputParser(writer, attachStr);

        String temp = Term(token, writer, reader);

        attachStr = " R";
        outputParser(writer, attachStr);

        if(reader.hasNext(Pattern.quote("('+',^)")) || reader.hasNext(Pattern.quote("('-',^)")))
        {
            token = extractToken(reader);
            getValue = R(token, writer, reader);
        }
        else
        {
            parsetreeDepth++;
            attachStr = " ^";
            outputParser(writer, attachStr);
            parsetreeDepth--;
            getValue = temp;
        }

        parsetreeDepth--;
        return getValue;
    }

    static String Term(String[] token, FileWriter writer, Scanner reader) throws IOException
    {
        parsetreeDepth++;
        String getValue = "";

        String attachStr = " Factor";
        outputParser(writer, attachStr);

        String temp = Factor(token, writer, reader);

        attachStr = " R_";
        outputParser(writer, attachStr);

        if(reader.hasNext(Pattern.quote("('*',^)")) || reader.hasNext(Pattern.quote("('/',^)")))
        {
            token = extractToken(reader);
            getValue = R_(token, writer, reader);
        }
        else
        {
            parsetreeDepth++;
            attachStr = " ^";
            outputParser(writer, attachStr);
            parsetreeDepth--;
            getValue = temp;
        }

        parsetreeDepth--;
        return getValue;
    }

    static String R(String[] token, FileWriter writer, Scanner reader) throws IOException
    {
        parsetreeDepth++;
        String getValue = ""; //<--check again
        String attachStr;

        if(token[0].contains("'+'"))
        {
            attachStr = " +";
            outputParser(writer, attachStr);
        }
        else if(token[0].contains("'-'"))
        {
            attachStr = " -";
            outputParser(writer, attachStr);
        }
        else
        {
            SyntaxError();
        }

        attachStr = " Term";
        outputParser(writer, attachStr);
        token = extractToken(reader);

        String temp = Term(token, writer, reader);

        attachStr = " R";
        outputParser(writer, attachStr);

        if(reader.hasNext(Pattern.quote("('+',^)")) || reader.hasNext(Pattern.quote("('-',^)")))
        {
            token = extractToken(reader);
            getValue = R(token, writer, reader);
        }
        else
        {
            parsetreeDepth++;
            attachStr = " ^";
            outputParser(writer, attachStr);
            parsetreeDepth--;
        }

        parsetreeDepth--;
        return getValue;
    }

    static String Factor(String[] token, FileWriter writer, Scanner reader) throws IOException
    {
        parsetreeDepth++;
        String attachStr;
        String getValue = "";

        if(token[0].equals("ID"))
        {
            attachStr = " ID("+token[1]+")";
            outputParser(writer, attachStr);
            getValue = token[1];
        }
        else if(token[0].equals("NUM"))
        {
            attachStr = " NUM("+token[1]+")";
            outputParser(writer, attachStr);
            getValue = token[1];
        }
        else if(token[0].equals("'('"))
        {
            attachStr = " (";
            outputParser(writer, attachStr);

            token = extractToken(reader);
            attachStr = " Expression";
            outputParser(writer, attachStr);
            getValue = Expression(token, writer, reader);

            token = extractToken(reader);
            if(token[0].equals("')'"))
            {
                attachStr = " )";
                outputParser(writer, attachStr);
            }
            else
            {
                SyntaxError();
            }
        }
        else
        {
            SyntaxError();
        }

        parsetreeDepth--;
        return getValue;
    }

    static String R_(String[] token, FileWriter writer, Scanner reader) throws IOException
    {
        parsetreeDepth++;
        String getValue = ""; //<--check again
        String attachStr;

        if(token[0].contains("'*'"))
        {
            attachStr = " *";
            outputParser(writer, attachStr);
        }
        else if(token[0].contains("'/'"))
        {
            attachStr = " /";
            outputParser(writer, attachStr);
        }
        else
        {
            SyntaxError();
        }

        attachStr = " Factor";
        outputParser(writer, attachStr);
        token = extractToken(reader);

        String temp = Factor(token, writer, reader);

        attachStr = " R_";
        outputParser(writer, attachStr);

        if(reader.hasNext(Pattern.quote("('*',^)")) || reader.hasNext(Pattern.quote("('/',^)")))
        {
            token = extractToken(reader);
            getValue = R(token, writer, reader);
        }
        else
        {
            parsetreeDepth++;
            attachStr = " ^";
            outputParser(writer, attachStr);
            parsetreeDepth--;
        }

        parsetreeDepth--;
        return getValue;
    }

    static String OutputOptions(String[] token, FileWriter writer, Scanner reader) throws IOException
    {
        parsetreeDepth++;
        String attachStr;
        String getValue = "";

        token = extractToken(reader);

        if(token[0].equals("STR"))
        {
            attachStr = " STR("+token[1]+")";
            outputParser(writer, attachStr);
            getValue = token[1];
        }
        else if(token[0].equals("LIT"))
        {
            attachStr = " LIT("+token[1]+")";
            outputParser(writer, attachStr);
            getValue = token[1];
        }
        else if(token[0].equals("ID"))
        {
            if (reader.hasNext(Pattern.quote("('+',^)")) || reader.hasNext(Pattern.quote("('-',^)")) || reader.hasNext(Pattern.quote("('*',^)")) || reader.hasNext(Pattern.quote("('/',^)")))
            {
                attachStr = " Expression";
                outputParser(writer, attachStr);
                getValue = Expression(token, writer, reader);

            }
            else
            {
                attachStr = " ID(" + token[1] + ")";
                outputParser(writer, attachStr);
                //getValue = token[1].substring(1, token[1].length() - 1);
                getValue = token[1];
            }
        }
        else if(token[0].equals("NUM"))
        {
            if (reader.hasNext(Pattern.quote("('+',^)")) || reader.hasNext(Pattern.quote("('-',^)")) || reader.hasNext(Pattern.quote("('*',^)")) || reader.hasNext(Pattern.quote("('/',^)")))
            {
                attachStr = " Expression";
                outputParser(writer, attachStr);
                getValue = Expression(token, writer, reader);

            }
            else
            {
                attachStr = " NUM(" + token[1] + ")";
                outputParser(writer, attachStr);
                getValue = token[1];
            }
        }
        else
        {
            attachStr = " ^";
            outputParser(writer, attachStr);
        }

        parsetreeDepth--;
        return getValue;
    }

    static void relOp(String[] token, FileWriter writer, Scanner reader) throws IOException
    {
        parsetreeDepth++;
        String attachStr = "relOp("+token[1]+")";
        outputParser(writer, attachStr);
        parsetreeDepth--;
    }

    static String Condition(String[] token, FileWriter writer, Scanner reader) throws IOException
    {
        parsetreeDepth++;
        String attachStr;
        String getValue = "";
        String getString;

        if(token[0].equals("ID") || token[0].equals("NUM"))
        {
            attachStr = " Expression";
            outputParser(writer, attachStr);
            getValue = Expression(token, writer, reader);
        }
        else
        {
            SyntaxError();
        }

        getString = getValue;
        token = extractToken(reader);
        if(token[0].equals("REL_OP"))
        {
            attachStr = " relOp";
            outputParser(writer, attachStr);
            relOp(token, writer, reader);
            getString = getString+" "+token[1];

            token = extractToken(reader);
            if(token[0].equals("ID") || token[0].equals("NUM"))
            {
                attachStr = " Expression";
                outputParser(writer, attachStr);
                getValue = Expression(token, writer, reader);
                getString = getString+" "+getValue;
            }
            else
            {
                SyntaxError();
            }
        }
        else
        {
            SyntaxError();
        }

        parsetreeDepth--;
        return getString;
    }

    static void INPUT_OP(String[] token, FileWriter writer, Scanner reader) throws IOException
    {
        parsetreeDepth++;
        String attachStr = " ->";
        outputParser(writer, attachStr);
        parsetreeDepth--;
    }

    static void inputDelimiter(String[] token, FileWriter writer, Scanner reader) throws IOException
    {
        parsetreeDepth++;
        String attachStr;

        if(token[0].equals("';'"))
        {
            attachStr = " ;";
            outputParser(writer, attachStr);
        }
        else if(token[0].equals("','"))
        {
            attachStr = " ,";
            outputParser(writer, attachStr);

            token = extractToken(reader);
            attachStr = " nextInput";
            outputParser(writer, attachStr);
            nextInput(token, writer, reader);
        }
        else
        {
            SyntaxError();
        }

        parsetreeDepth--;
    }

    static void nextInput(String[] token, FileWriter writer, Scanner reader) throws IOException
    {
        parsetreeDepth++;
        String attachStr;

        if(token[0].equals("ID"))
        {
            attachStr = " ID("+token[1]+")";
            outputParser(writer, attachStr);
        }
        else
        {
            SyntaxError();
        }

        token = extractToken(reader);
        attachStr = " inputDelimiter";
        outputParser(writer, attachStr);
        inputDelimiter(token, writer, reader);

        parsetreeDepth--;
    }

    static void IncOp(String[] token, FileWriter writer, Scanner reader) throws IOException
    {
        parsetreeDepth++;
        String attachStr;

        if(token[0].equals("ID"))
        {
            attachStr = " ID("+token[1]+")";
            outputParser(writer, attachStr);

            token = extractToken(reader);
            if(token[0].equals("'+'"))
            {
                attachStr = " +";
                outputParser(writer, attachStr);

                token = extractToken(reader);
                if(token[0].equals("'+'")) {
                    attachStr = " +";
                    outputParser(writer, attachStr);
                }
                else
                {
                    SyntaxError();
                }

                token = extractToken(reader);
                if(token[0].equals("';'"))
                {
                    attachStr = " ;";
                    outputParser(writer, attachStr);
                }
                else
                {
                    SyntaxError();
                }
            }
            else
            {
                SyntaxError();
            }
        }

        parsetreeDepth--;
    }

    static void DecOp(String[] token, FileWriter writer, Scanner reader) throws IOException
    {
        parsetreeDepth++;
        String attachStr;

        if(token[0].equals("ID"))
        {
            attachStr = " ID("+token[1]+")";
            outputParser(writer, attachStr);

            token = extractToken(reader);
            if(token[0].equals("'-'"))
            {
                attachStr = " -";
                outputParser(writer, attachStr);

                token = extractToken(reader);
                if(token[0].equals("'-'")) {
                    attachStr = " -";
                    outputParser(writer, attachStr);
                }
                else
                {
                    SyntaxError();
                }

                token = extractToken(reader);
                if(token[0].equals("';'"))
                {
                    attachStr = " ;";
                    outputParser(writer, attachStr);
                }
                else
                {
                    SyntaxError();
                }
            }
            else
            {
                SyntaxError();
            }
        }

        parsetreeDepth--;
    }

    static void AssignmentStatement(String[] token, FileWriter writer, Scanner reader) throws IOException
    {
        parsetreeDepth++;
        String attachStr;

        if(token[0].equals("ID"))
        {
            attachStr = " ID("+token[1]+")";
            outputParser(writer, attachStr);

            token = extractToken(reader);
            if(token[0].equals("'='"))
            {
                attachStr = " =";
                outputParser(writer, attachStr);

                token = extractToken(reader);
                attachStr = " Value";
                outputParser(writer, attachStr);
                Value(token, writer, reader);

                token = extractToken(reader);
                if(token[0].equals("';'"))
                {
                    attachStr = " ;";
                    outputParser(writer, attachStr);
                }
                else
                {
                    SyntaxError();
                }
            }
            else
            {
                SyntaxError();
            }
        }

        parsetreeDepth--;
    }

    static String Value(String[] token, FileWriter writer, Scanner reader) throws IOException
    {
        parsetreeDepth++;
        String attachStr;
        String getValue = "";

        if(reader.hasNext(Pattern.quote("('+',^)")) || reader.hasNext(Pattern.quote("('-',^)")) || reader.hasNext(Pattern.quote("('*',^)")) || reader.hasNext(Pattern.quote("('/',^)")))
        {
            attachStr = " Expression";
            outputParser(writer, attachStr);
            getValue = Expression(token, writer, reader);
        }
        else if(token[0].equals("ID"))
        {
            attachStr = " ID(" + token[1] + ")";
            outputParser(writer, attachStr);
            //getValue = token[1].substring(1, token[1].length() - 1);
            getValue = token[1];
        }
        else if(token[0].equals("NUM"))
        {
            attachStr = " NUM(" + token[1] + ")";
            outputParser(writer, attachStr);
            getValue = token[1];
        }
        else if(token[0].equals("LIT"))
        {
            attachStr = " LIT(" + token[1] + ")";
            outputParser(writer, attachStr);
            getValue = token[1];
        }
        else
        {
            SyntaxError();
        }

        parsetreeDepth--;
        return getValue;
    }

    static void DT(String[] token, FileWriter writer, Scanner reader) throws IOException
    {
        parsetreeDepth++;
        String attachStr;

        if(token[0].equals("INT"))
        {
            attachStr = " int";
            outputParser(writer, attachStr);
        }
        else if(token[0].equals("CHAR"))
        {
            attachStr = " char";
            outputParser(writer, attachStr);
        }
        else
        {
            SyntaxError();
        }

        parsetreeDepth--;
    }

    static void optionAssign(String[] token, FileWriter writer, Scanner reader) throws IOException
    {
        parsetreeDepth++;
        String attachStr;

        if(token[0].equals("ID"))
        {
            token = extractToken(reader);
            if(token[0].equals("'='"))
            {
                attachStr = " =";
                outputParser(writer, attachStr);

                token = extractToken(reader);
                attachStr = " Value";
                outputParser(writer, attachStr);
                Value(token, writer, reader);
            }
            else
            {
                SyntaxError();
            }
        }

        parsetreeDepth--;
    }

    static void Variable(String[] token, FileWriter writer, Scanner reader) throws IOException
    {
        parsetreeDepth++;
        String attachStr = " DT";
        outputParser(writer, attachStr);
        DT(token, writer, reader);
        String type = token[0];

        token = extractToken(reader);
        if(token[0].equals("':'"))
        {
            attachStr = " :";
            outputParser(writer, attachStr);
        }
        else
        {
            SyntaxError();
        }

        token = extractToken(reader);
        if(token[0].equals("ID"))
        {
            attachStr = " ID("+token[1]+")";
            outputParser(writer, attachStr);

            writeToSymbolTable(token[1]+"\t"+type);
        }
        else
        {
            SyntaxError();
        }

        if(reader.hasNext(Pattern.quote("('=',^)")))
        {
            optionAssign(token, writer, reader);
        }

        token = extractToken(reader);
        attachStr = " VariableDelimiter";
        outputParser(writer, attachStr);
        VariableDelimiter(token, writer, reader, type);

        parsetreeDepth--;
    }

    static void VariableDelimiter(String[] token, FileWriter writer, Scanner reader, String type) throws IOException
    {
        parsetreeDepth++;
        String attachStr;

        if(token[0].equals("';'"))
        {
            attachStr = " ;";
            outputParser(writer, attachStr);
        }
        else if(token[0].equals("','"))
        {
            attachStr = " ,";
            outputParser(writer, attachStr);
            token = extractToken(reader);
            attachStr = " nextVariable";
            outputParser(writer, attachStr);
            nextVariable(token, writer, reader, type);
        }
        else
        {
            SyntaxError();
        }

        parsetreeDepth--;
    }

    static void nextVariable(String[] token, FileWriter writer, Scanner reader, String type) throws IOException
    {
        parsetreeDepth++;
        String attachStr;

        if(token[0].equals("ID"))
        {
            attachStr = " ID(" + token[1] + ")";
            outputParser(writer, attachStr);

            writeToSymbolTable(token[1]+"\t"+type);

            if(reader.hasNext(Pattern.quote("('=',^)")))
            {
                optionAssign(token, writer, reader);
            }

            token = extractToken(reader);
            attachStr = " VariableDelimiter";
            outputParser(writer, attachStr);
            VariableDelimiter(token, writer, reader, type);
        }
        else
        {
            SyntaxError();
        }

        parsetreeDepth--;
    }

    public void startParsing() throws IOException
    {
        File tokenFile = new File("tokens.txt"); //file for input from lexer

        //creating files, writers and readers
        FileWriter parserWriter = new FileWriter("parsetree.txt");
        FileWriter parserSTWriter = new FileWriter("parser-symboltable.txt");
        Scanner tokenReader = new Scanner(tokenFile);

        String[] token; //Token variable

        token = extractToken(tokenReader);

        parserWriter.write("Start\n");
        writeToSymbolTable("SCOPE START");

        Start(token,parserWriter,tokenReader);

        writeToSymbolTable("SCOPE END");

        //closing readers and writers
        parserWriter.close();
        parserSTWriter.close();
        tokenReader.close();

        System.out.println("\nParser execution successful! Check parsetree.txt and parser-symboltable.txt files.");
    }
}