import lexer.Lexer;
import parser.Parser;


import java.io.IOException;

public class Source {

    public static void main(String[] args) throws IOException {

        //Lexical Analysis
        Lexer lexer = new Lexer();
        if(!lexer.setFilename(args[0]))
        {
            return;
        }
        lexer.startLexicalAnalysis();
        if(!lexer.success)
        {
            System.exit(1);     //not to proceed further if some error was encountered previously
        }

        //Parsing
        Parser parser = new Parser();
        parser.setFilename(args[0]);
        parser.startParsing();
    }
}