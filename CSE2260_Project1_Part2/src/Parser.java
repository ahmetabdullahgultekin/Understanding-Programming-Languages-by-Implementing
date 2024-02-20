//Melik Özdemir 150120004
//Ömer Deligöz 150120035
//Ahmet Abdullah Gültekin 150121025

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Parser {
    private static final List<String> tokens = new ArrayList<>();
    private static final List<Integer[]> indexes = new ArrayList<>();
    private static final List<String> output = new ArrayList<>();
    private static int currentTokenIndex;
    private static final String blank = " ";

    public static void parse() {
        parseProgram();
    }

    private static void parseProgram() {
        addMethod("<Program>");
        checkError();
        if (match("LEFTPAR")) {
            currentTokenIndex--;
            parseTopLevelForm();
            parseProgram();
        } else {
            addMethod("__");
        }
    }

    private static void parseTopLevelForm() {
        addMethod("<TopLevelForm>");
        checkError();
        expect("LEFTPAR");
        parseSecondLevelForm();
        expect("RIGHTPAR");
    }

    private static void parseSecondLevelForm() {
        addMethod("<SecondLevelForm>");
        checkError();
        if (match("LEFTPAR")) {
            currentTokenIndex--;
            expect("LEFTPAR");
            parseFunCall();
            expect("RIGHTPAR");
        } else {
            parseDefinition();
        }
    }

    private static void parseDefinition() {
        addMethod("<Definition>");
        checkError();
        expect("DEFINE");
        parseDefinitionRight();
    }


    private static void parseDefinitionRight() {
        addMethod("<DefinitionRight>");
        checkError();
        if (match("LEFTPAR")) {
            currentTokenIndex--;
            expect("LEFTPAR");
            expect("IDENTIFIER");
            parseArgList();
            expect("RIGHTPAR");
            parseStatements();
        } else {
            expect("IDENTIFIER");
            parseExpression();
        }
    }

    private static void parseArgList() {
        addMethod("<ArgList>");
        checkError();
        if (match("IDENTIFIER")) {
            currentTokenIndex--;
            expect("IDENTIFIER");
            parseArgList();
        } else {
            addMethod("__");
        }
    }

    private static void parseStatements() {
        addMethod("<Statements>");
        checkError();
        if (match("IDENTIFIER") || match("NUMBER") || match("CHAR") ||
                match("BOOLEAN") || match("STRING") || match("LEFTPAR")) {
            currentTokenIndex--;
            parseExpression();
        } else if (match("DEFINE")) {
            currentTokenIndex--;
            parseDefinition();
            parseStatements();
        }
    }

    private static void parseExpressions() {
        addMethod("<Expressions>");
        checkError();
        if (match("IDENTIFIER") || match("NUMBER") || match("CHAR") ||
                match("BOOLEAN") || match("STRING") || match("LEFTPAR")) {
            currentTokenIndex--;
            parseExpression();
            parseExpressions();
        } else {
            addMethod("__");
        }
    }

    private static void parseExpression() {
        addMethod("<Expression>");
        checkError();
        if (match("IDENTIFIER") || match("NUMBER") || match("CHAR") ||
                match("BOOLEAN") || match("STRING")) {
            currentTokenIndex--;
            expect(tokens.get(currentTokenIndex));
        } else if (match("LEFTPAR")) {
            currentTokenIndex--;
            expect("LEFTPAR");
            parseExpr();
            expect("RIGHTPAR");
        }
    }

    private static void parseExpr() {
        addMethod("<Expr>");
        checkError();
        if (match("LET")) {
            currentTokenIndex--;
            parseLetExpression();
        } else if (match("COND")) {
            currentTokenIndex--;
            parseCondExpression();
        } else if (match("IF")) {
            currentTokenIndex--;
            parseIfExpression();
        } else if (match("BEGIN")) {
            currentTokenIndex--;
            parseBeginExpression();
        } else {
            parseFunCall();
        }
    }

    private static void parseFunCall() {
        addMethod("<FunCall>");
        checkError();
        expect("IDENTIFIER");
        parseExpressions();
    }

    private static void parseLetExpression() {
        addMethod("<LetExpression>");
        checkError();
        expect("LET");
        parseLetExpr();
    }

    private static void parseLetExpr() {
        addMethod("<LetExpr>");
        checkError();
        if (match("LEFTPAR")) {
            currentTokenIndex--;
            expect("LEFTPAR");
            parseVarDefs();
            expect("RIGHTPAR");
            parseStatements();
        } else if (match("IDENTIFIER")) {
            currentTokenIndex--;
            expect("IDENTIFIER");
            expect("LEFTPAR");
            parseVarDefs();
            expect("RIGHTPAR");
            parseStatements();
        }
    }

    private static void parseVarDefs() {
        addMethod("<VarDefs>");
        checkError();
        expect("LEFTPAR");
        expect("IDENTIFIER");
        parseExpression();
        expect("RIGHTPAR");
        parseVarDef();
    }

    private static void parseVarDef() {
        addMethod("<VarDef>");
        checkError();
        if (match("LEFTPAR")) {
            currentTokenIndex--;
            parseVarDefs();
        } else {
            addMethod("__");
        }
    }

    private static void parseCondExpression() {
        addMethod("<CondExpression>");
        checkError();
        expect("COND");
        parseCondBranches();
    }

    private static void parseCondBranches() {
        addMethod("<CondBranches>");
        checkError();
        expect("LEFTPAR");
        parseExpression();
        parseStatements();
        expect("RIGHTPAR");
        parseCondBranch();
    }

    private static void parseCondBranch() {
        addMethod("<CondBranch>");
        checkError();
        if (match("LEFTPAR")) {
            currentTokenIndex--;
            expect("LEFTPAR");
            parseExpression();
            parseStatements();
            expect("RIGHTPAR");
        } else {
            addMethod("__");
        }
    }

    private static void parseIfExpression() {
        addMethod("<IfExpression>");
        checkError();
        expect("IF");
        parseExpression();
        parseExpression();
        parseEndExpression();
    }

    private static void parseEndExpression() {
        addMethod("<EndExpression>");
        checkError();
        if (match("IDENTIFIER") || match("NUMBER") || match("CHAR") ||
                match("BOOLEAN") || match("STRING") || match("LEFTPAR")) {
            currentTokenIndex--;
            parseExpression();
        } else {
            addMethod("__");
        }
    }

    private static void parseBeginExpression() {
        addMethod("<BeginExpression>");
        checkError();
        expect("BEGIN");
        parseStatements();
    }

    private static boolean match(String token) {
        if (currentTokenIndex < tokens.size() && tokens.get(currentTokenIndex).equals(token)) {
            currentTokenIndex++;
            return true;
        }
        return false;
    }

    private static void expect(String token) {
        if (!match(token)) {
            switch (token) {
                case "LEFTPAR" -> token = "(";
                case "RIGHTPAR" -> token = ")";
            }
            output.add("SYNTAX ERROR [" + indexes.get(currentTokenIndex)[0] + ":" + indexes.get(currentTokenIndex)[1] + "]: '" + token + "' is expected");
            throw new RuntimeException();
        }
        addToken(token, currentTokenIndex - 1);
    }

    private static void checkError() {
        if (currentTokenIndex < tokens.size()) {
            String token = tokens.get(currentTokenIndex);
            switch (token) {
                case "LEFTSQUAREB" -> token = "[";
                case "RIGHTSQUAREB" -> token = "]";
                case "LEFTCURLYB" -> token = "{";
                case "RIGHTCURLYB" -> token = "}";
            }
            if (token.equals("[") || token.equals("]") || token.equals("}") || token.equals("{")) {
                throw new RuntimeException("SYNTAX ERROR [" + indexes.get(currentTokenIndex)[0] + ":" + indexes.get(currentTokenIndex)[1] + "]: INVALID TOKEN '" + token + "'");
            }
        }
    }


    private static void printLexicalError(String[] tokens) throws IOException {
        FileWriter output = new FileWriter("output.txt");
        BufferedWriter bufferedWriter = new BufferedWriter(output);

        for (String token : tokens) {
            System.out.println(token);
            bufferedWriter.write(token);
            bufferedWriter.newLine();
        }
        bufferedWriter.close();
    }


    private static void printResults() throws IOException {
        FileWriter outputFile = new FileWriter("output.txt");
        BufferedWriter bufferedWriter = new BufferedWriter(outputFile);

        for (String token : output) {
            System.out.println(token);
            bufferedWriter.write(token);
            bufferedWriter.newLine();
        }
        bufferedWriter.close();
    }

    private static void addMethod(String s) {
        if (s.equals("__"))
            output.add(blank.repeat(Thread.currentThread().getStackTrace().length - 4) + s);
        else
            output.add(blank.repeat(Thread.currentThread().getStackTrace().length - 5) + s);
    }

    private static void addToken(String token, int index) {
        output.add(blank.repeat(Thread.currentThread().getStackTrace().length - 5) + token + " (" + Lexer.tokenNames.get(index) + ")");
    }

    private static void preprocessTokens(String fileName) throws IOException {
        String str = "";
        try {
            str = new String(Files.readAllBytes(Paths.get(fileName)));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        String[] strArr = str.split("\r\n");
        if (strArr[strArr.length - 1].startsWith("LEXICAL ERROR")) { //if there is an incorrect token give error and exit the program
            printLexicalError(strArr);
            System.exit(-1);
        }

        ArrayList<String[]> strList = new ArrayList<>();

        for (String s : strArr) {  // split (LEFTPAR) (1:1)
            strList.add(s.split(" "));
        }
        String[] parts;
        int row, col;
        for (String[] s : strList) { // split (1):(1)
            tokens.add(s[0]);
            parts = s[1].split(":");
            row = Integer.parseInt(parts[0]);
            col = Integer.parseInt(parts[1]);
            indexes.add(new Integer[]{row, col});
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner input = new Scanner(System.in);
        System.out.print("Please enter the name of the input file: ");
        String inputFileStr = input.next();

        try {
            Lexer.main(inputFileStr);
            String lexerOutputFile = "lexeroutput.txt";
            preprocessTokens(lexerOutputFile);
            parse();
            printResults();
        } catch (Exception e) {
            printResults();
        }
    }
}