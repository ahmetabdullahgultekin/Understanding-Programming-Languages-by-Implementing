/*
150121025 Ahmet Abdullah Gültekin
150120004 Melik Özdemir
150120035 Ömer Deligöz
*/

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    //Global variables for checking the "isContain?" cases
    static String specialChars = " ()[]{}";
    static String hexDigits = "0123456789abcdefABCDEF";
    static String decDigits = "0123456789";
    static String binDigits = "01";
    static String signs = "+-.";
    static String marks = "!*/:<=>?";

    static ArrayList<String> tokens = new ArrayList<>();

    //Check contains
    public static boolean isSpecialChars(char ch) {
        return specialChars.contains(ch + "");
    }

    //Check contains
    public static boolean isHexDigit(char ch) {
        return hexDigits.contains(ch + "");
    }

    //Check contains
    public static boolean isBinDigit(char ch) {
        return binDigits.contains(ch + "");
    }

    //Check contains
    public static boolean isDecDigit(char ch) {
        return decDigits.contains(ch + "");
    }

    //Check contains
    public static boolean isSign(char ch) {
        return signs.contains(ch + "");
    }

    public static boolean isMark(char ch) {
        return marks.contains(ch + "");
    }

    public static boolean isStart(String str) {
        return str.equals("+.") || str.equals("-.");
    }

    //Main function of process
    public static void lexer(File file) throws IOException {
        Scanner input = new Scanner(file);
        String line;
        int row = 0;
        int col;

        //Check whether input is exist or not,
        //Then read input line by line
        while (input.hasNext()) {
            row++;
            line = input.nextLine();
            String str = "";

            //Get the chars one by one from line
            for (int i = 0; i < line.length(); i++) {
                col = i + 1;
                char ch = line.charAt(i);

                String subS = "";

                //Check for characters
                if (ch == '\'') {
                    subS += ch;
                    if (i + 2 < line.length() && (line.charAt(i + 2) == '\'' || line.charAt(i + 3) == '\'')) {
                        subS += line.charAt(i + 1);
                        subS += line.charAt(i + 2);
                    } else {
                        tokens.add("LEXICAL ERROR [" + row
                                + ":" + col + "]: Invalid token '" + subS + "'");
                        return;
                    }
                    if (i + 3 < line.length() && line.charAt(i + 3) == '\'') {
                        subS += line.charAt(i + 3);
                    }

                    if (subS.length() == 3 || subS.length() == 4) {
                        i += subS.length();
                    }

                    if (subS.charAt(1) == '\'') {
                        tokens.add("LEXICAL ERROR [" + row
                                + ":" + col + "]: Invalid token '" + subS + "'");
                        return;
                    } else if (subS.charAt(1) == '\\') {
                        if (subS.length() == 3) {
                            tokens.add("LEXICAL ERROR [" + row
                                    + ":" + col + "]: Invalid token '" + subS + "'");
                            return;
                        } else if (subS.charAt(3) != '\'') {
                            tokens.add("LEXICAL ERROR [" + row
                                    + ":" + col + "]: Invalid token '" + subS + "'");
                            return;
                        } else if (subS.charAt(2) != '\'' && subS.charAt(2) != '\\') {
                            tokens.add("LEXICAL ERROR [" + row
                                    + ":" + col + "]: Invalid token '" + subS + "'");
                            return;
                        } else {
                            tokens.add("CHAR " + row + ":" + col);
                            continue;
                        }
                    } else {
                        tokens.add("CHAR " + row + ":" + col);
                        continue;
                    }
                }

                //Check for String
                if (ch == '"') {
                    int startCol = i + 1;
                    int startRow = row;
                    char newChar = line.charAt(++i);
                    str += ch;

                    if (newChar == '"') {
                        tokens.add("LEXICAL ERROR [" + startRow + ":" + startCol + "]: Invalid token '\"\"'");
                        return;
                    }
                    while (newChar != '"') {
                        str += newChar;
                        char nextCh = line.charAt(i + 1);
                        if ((newChar == '\\' && ((nextCh == '\\') || nextCh == '\"'))) {
                            str += nextCh;
                            newChar = line.charAt(i + 2);
                            i += 2;
                            continue;
                        }

                        if (i == line.length() - 1) {
                            if (!input.hasNext()) {
                                tokens.add("LEXICAL ERROR [" + startRow + ":" + startCol + "]: Invalid token '" + str + "'");
                                return;
                            }
                            str += newChar;
                            line = input.nextLine();
                            row++;
                            col = 1;
                            i = 0;
                            continue;
                        }
                        col++;
                        newChar = line.charAt(++i);
                    }

                    if (!isSpecialChars(line.charAt(i + 1))) {
                        tokens.add("LEXICAL ERROR [" + startRow
                                + ":" + startCol + "]: Invalid token '" + str + "'");
                        return;
                    } else {
                        tokens.add("STRING " + startRow + ":" + startCol);
                        str = "";
                    }
                    continue;
                }

                //Check comment
                if (ch == '~')
                    break;

                //Check whitespaces and parentheses
                if (isSpecialChars(ch)) {
                    switch (ch) {
                        case '(' -> tokens.add("LEFTPAR " + row + ":" + col);
                        case ')' -> tokens.add("RIGHTPAR " + row + ":" + col);
                        case '[' -> tokens.add("LEFTSQUAREB " + row + ":" + col);
                        case ']' -> tokens.add("RIGHTSQUAREB " + row + ":" + col);
                        case '{' -> tokens.add("LEFTCURLYB " + row + ":" + col);
                        case '}' -> tokens.add("RIGHTCURLYB " + row + ":" + col);
                    }
                } else {
                    str += ch;
                    char nextCh;

                    if (!(i == line.length() - 1)) {
                        nextCh = line.charAt(i + 1);
                    } else {
                        nextCh = ' ';
                    }

                    //Check next char to determine string is ended
                    if (isSpecialChars(nextCh)) {
                        int startCol = col - str.length() + 1;
                        //Check if token is keyword
                        if (str.equals("define") || str.equals("let")
                                || str.equals("cond") || str.equals("if")
                                || str.equals("begin")) {
                            tokens.add(str.toUpperCase() + " " + row + ":" + startCol);
                        }
                        //Check if token is boolean
                        else if (str.equals("true") || str.equals("false")) {
                            tokens.add("BOOLEAN " + row + ":" + startCol);
                        } else {
                            if (checkIdentifier(str)) {
                                tokens.add("IDENTIFIER " + row + ":" + startCol);
                            } else if (checkNumber(str, row, startCol)) {
                                tokens.add("NUMBER " + row + ":" + startCol);
                            }
                            //Token is invalid
                            else {
                                tokens.add("LEXICAL ERROR [" + row + ":" + startCol + "]: Invalid token '" + str + "'");
                                return;
                            }
                        }
                        str = "";
                    }
                }
            }
        }
        //Close scanner
        input.close();
    }

    public static boolean checkIdentifier(String token) {
        if (token.length() == 0)
            return false;
        if (isSign(token.charAt(0)) && token.length() == 1)
            return true;

        //Check if first char is valid
        if (!((isMark(token.charAt(0)) || Character.isLetter(token.charAt(0))))) {
            return false;
        }

        if (token.length() == 1) {
            return true;
        } else {
            token = token.substring(1);
            for (int i = 0; i < token.length(); i++) {
                char ch = token.charAt(i);

                if (!(Character.isLetterOrDigit(ch) || isSign(ch))) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean checkNumber(String token, int startRow, int startCol) {
        int cntX = 0, cntB = 0, cntSign = 0, cntPoint = 0, cntNum = 0, cntHexs = 0;
        for (int i = 0; i < token.length(); i++) {

            switch (token.charAt(i)) {
                case 'x' -> cntX++;
                case 'b' -> cntB++;
                case '+', '-' -> cntSign++;
                case '.' -> cntPoint++;
                case 'a', 'A', 'B', 'c', 'C', 'd', 'D', 'e', 'E', 'f', 'F' -> cntHexs++;
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> cntNum++;
                default -> {
                    return false;
                }
            }
        }
        //If the token contains more than 1 point or 2 sign character, give error
        if (cntSign > 2 || cntPoint > 1) {
            return false;
        }

        //check if the token is decimal,hexadecimal,binary or floating point numbers
        return isDecimal(token) || isHexadecimal(token) || isBinary(token) || isFloating(token);
    }

    //check if the given token is decimal number
    public static boolean isDecimal(String token) {
        if ((isSign(token.charAt(0)) && token.length() > 1) || isDecDigit(token.charAt(0))) {
            for (int i = 1; i < token.length(); i++)
                if (!isDecDigit(token.charAt(i)))
                    return false;
        } else return false;
        return true;
    }

    //check if the given token is hexadecimal number
    public static boolean isHexadecimal(String token) {
        if (token.charAt(0) == '0' && token.charAt(1) == 'x') {
            for (int i = 2; i < token.length(); i++)
                if (!isHexDigit(token.charAt(i)))
                    return false;
        } else return false;
        return true;
    }

    //check if the given token is binary number
    public static boolean isBinary(String token) {
        if (token.charAt(0) == '0' && token.charAt(1) == 'b') {
            for (int i = 2; i < token.length(); i++)
                if (!isBinDigit(token.charAt(i)))
                    return false;
        } else return false;
        return true;
    }

    //check if the given token is floating-point number
    public static boolean isFloating(String token) {
        int indexE = -1;
        int dotIndex = token.indexOf('.');

        //return false if the dot is at the end of the token
        if (dotIndex == token.length() - 1) {
            return false;
        }


        if (token.indexOf('e') > 0) {
            indexE = token.indexOf('e');
        } else if (token.indexOf('E') > 0) {
            indexE = token.indexOf('E');
        } else if (((!isSign(token.charAt(0)) && !isDecDigit(token.charAt(0))))
                || ((isSign(token.charAt(dotIndex + 1)) || !isDecimal(token.substring(dotIndex + 1))))) {
            return false;
        } else return true;

        //return false if the (e/E) is at the end of the token
        if (indexE == token.length() - 1)
            return false;

            //check after E
        else if (!isDecimal(token.substring(indexE + 1)))
            return false;


        if (dotIndex > 0) {
            if (((isSign(token.charAt(dotIndex + 1)))
                    || !isDecimal(token.substring(dotIndex + 1, indexE)))
                    || ((!isStart(token.substring(0, 2)))
                    && !isDecimal(token.substring(0, dotIndex)))) {
                return false;
            }
        }

        //starts with '.'
        else if ((dotIndex == 0) && ((isSign(token.charAt(1))) || !isDecimal(token.substring(1, indexE)))) {
            return false;
        }
        //without .
        else if (!isDecimal(token.substring(0, indexE)) || !isDecimal(token.substring(indexE + 1))) {
            return false;
        }
        return true;
    }


    //Print result
    public static void printTokens() throws IOException {
        FileWriter output = new FileWriter("output.txt");
        BufferedWriter bufferedWriter = new BufferedWriter(output);
        for (String token : tokens) {
            System.out.println(token);
            bufferedWriter.write(token);
            bufferedWriter.newLine();
        }
        bufferedWriter.close();
    }

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        System.out.print("Please enter the name of the input file: ");
        String inputFileStr = input.next();

        try {
            File inputFile = new File(inputFileStr);
            lexer(inputFile);
            printTokens();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
