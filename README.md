# Understanding Programming Languages by Implementing — Mini Compiler in Java

A two‑part project that walks through the classic compiler pipeline: **lexical analysis** and **recursive‑descent parsing**. The toy language resembles a subset of Scheme/Lisp with nested lists, identifiers, integers (decimal & hex) and basic arithmetic operators.

> Course: **CSE 2260 – Principles of Programming Languages**  
> Authors: Ahmet Abdullah Gültekin · Melik Özdemir · Ömer Deliğöz

---

## 📂 Repository Layout

```
Understanding-Programming-Languages-by-Implementing/
├── CSE2260_project1_part1/
│   ├── src/Main.java            # Stand‑alone Lexer
│   ├── src/input*.txt           # Test programs (1‑3)
│   └── output.txt               # Tokens for all inputs
└── CSE2260_Project1_Part2/
    ├── src/Lexer.java           # Same lexer as Part 1 (refactored)
    ├── src/Parser.java          # Recursive‑descent parser → AST pretty print
    ├── input1.txt input2.txt    # Source samples
    ├── lexeroutput.txt          # Raw tokens
    ├── output.txt               # Parse tree / Errors
    └── Report.docx              # Design write‑up & screenshots
```

---

## ✨ Language Spec (subset)
```
<program>   ::= <list>
<list>      ::= '(' { <atom> | <list> } ')'
<atom>      ::= <identifier> | <integer> | <hex-int>
<identifier>::= letter { letter | digit }
<integer>   ::= digit { digit }
<hex-int>   ::= '0x' hexDigit { hexDigit }
```
Whitespace is insignificant; `;` starts a comment to end‑of‑line.

---

## 🛠 Build & Run
### Compile Both Parts
```bash
# Part 1 – Lexer only
javac CSE2260_project1_part1/src/Main.java -d build1
java -cp build1 Main CSE2260_project1_part1/src/input1.txt

# Part 2 – Lexer + Parser
javac CSE2260_Project1_Part2/src/*.java -d build2
java -cp build2 Parser CSE2260_Project1_Part2/input1.txt
```
The parser writes results to `lexeroutput.txt` and `output.txt` in its folder.

---

## 🧠 Implementation Highlights
* **Lexer**
  * Single pass using a `Scanner` – recognises parentheses, brackets, braces as separate tokens.
  * Hex detection via regex `^0[xX][0-9A-Fa-f]+$`.
  * Outputs `<type,value,row,column>` quadruples.
* **Parser**
  * Hand‑written recursive descent matching the grammar above.
  * Builds an ArrayList‑based AST; pretty‑prints with indentation.
  * Detects mismatched parentheses, invalid atoms, unexpected EOF.

---

## 📈 Sample Output (Parser)
```
LIST
  ATOM:foo
  LIST
    INT:42
    HEX:0x2A
  ATOM:bar
END
```
Error example:
```
ERROR line 4 col 7: expected ')' before ']'
```

---

## 🔬 Possible Extensions
1. **Evaluation** – walk the AST to interpret (+, -, *, /) expressions.
2. **Symbol table** for `define` / `let` bindings.
3. **Pretty‑print** back to source with canonical spacing.
4. Port lexer to **Flex/JFlex** and compare performance.


