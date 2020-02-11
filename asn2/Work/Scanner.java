import java.awt.*;
import java.util.*;

  /* Everything we code goes into the jar file at the
   top level*/
  //.ada, .java, .class
  /* **Jar file must be executable** */
  //submit Parser.java to canvas seperately

/* Parser rules*/
//  = means "is defined as"
//  " " enclose literal items; accept()
//  [ ] enclose items which may be omitted; check if []
//  { } enclose items which may appear zero or more times; while {}
//  | indicates a choice; or
//  ( ) are used for grouping required choices;
//  < > enclose semantic qualifications;
//  identifier = Token.ID;

/* Scope analysis rules*/
// 1. All identifiers must be declared before they are used or referenced.
// 2. There may be at most one declaration of a given identifier in a single block.
// 3. In TinyAda, a new block begins with the formal parameter specifications of a
// procedure and extends to the reserved word end belonging to that procedure.
// 4. The visibility of a declared identifier extends into nested blocks, unless that
// identifier is redeclared in a nested block.
// 5. Identifiers are not case sensitive.

/* Role analysis */
//setRole in SymbolEntry when identifer declared
//Ex. typeDeclaration: setRole to establish role as
//TYPE, we also make a SymbolEntry

//acceptRole does the role checking. Either for role or
//set of roles.
//Ex. index: findId to get the SymbolEntry. use acceptRole
//to verify entry is a type.
//Ex. parameterSpecification and enumerationTypeDefinition
// use identifierList

//SymbolEntry has "next" field to link to for next identifier
//in the list. append() used to append each remaining identifier
//to the list. setRole then sets same role in the entire list.
//Ex. enumerationTypeDefinition and identifierList

//identifierList returns SymbolEntry, which is the head of a
//list of SymbolEntry objects. enumerationTypeDefinition receives
//the object and sets role using setRole()



public class Scanner extends Object{

   private Token token;
   private StringBuffer tokenBuffer;
   private Map<String, Token> keywords;
   private Map<String, Token> singleOps;
   private Map<String, Token> doubleOps;
   private Chario chario;
   private char ch;

   private static final int MAX_KEY_SPELLING = 9;

   public Scanner(Chario c){
      chario = c;
      initKeywords();
      initSingleOps();
      initDoubleOps();
      ch = chario.getChar();
   }

   public void reset(){
      chario.reset();
      ch = chario.getChar();
   }

   private void initKeywords(){
      keywords = new HashMap<String, Token>();
      keywords.put("AND", new Token(Token.AND));
      keywords.put("ARRAY", new Token(Token.ARRAY));
      keywords.put("BEGIN", new Token(Token.BEGIN));
      keywords.put("CONSTANT", new Token(Token.CONST));
      keywords.put("ELSE", new Token(Token.ELSE));
      keywords.put("ELSIF", new Token(Token.ELSIF));
      keywords.put("END", new Token(Token.END));
      keywords.put("EXIT", new Token(Token.EXIT));
      keywords.put("IF", new Token(Token.IF));
      keywords.put("IN", new Token(Token.IN));
      keywords.put("IS", new Token(Token.IS));
      keywords.put("LOOP", new Token(Token.LOOP));
      keywords.put("MOD", new Token(Token.MOD));
      keywords.put("NOT", new Token(Token.NOT));
      keywords.put("NULL", new Token(Token.NULL));
      keywords.put("OF", new Token(Token.OF));
      keywords.put("OR", new Token(Token.OR));
      keywords.put("OUT", new Token(Token.OUT));
      keywords.put("PROCEDURE", new Token(Token.PROC));
      keywords.put("RANGE", new Token(Token.RANGE));
      keywords.put("THEN", new Token(Token.THEN));
      keywords.put("TYPE", new Token(Token.TYPE));
      keywords.put("WHEN", new Token(Token.WHEN));
      keywords.put("WHILE", new Token(Token.WHILE));
   }

   private void initSingleOps(){
      singleOps = new HashMap<String, Token>();
      singleOps.put(":", new Token(Token.COLON));
      singleOps.put(",", new Token(Token.COMMA));
      singleOps.put("=", new Token(Token.EQ));
      singleOps.put(">", new Token(Token.GT));
      singleOps.put("<", new Token(Token.LT));
      singleOps.put("(", new Token(Token.L_PAR));
      singleOps.put("-", new Token(Token.MINUS));
      singleOps.put("*", new Token(Token.MUL));
      singleOps.put("/", new Token(Token.DIV));
      singleOps.put("+", new Token(Token.PLUS));
      singleOps.put(")", new Token(Token.R_PAR));
      singleOps.put(";", new Token(Token.SEMI));
   }

   private void initDoubleOps(){
      doubleOps = new HashMap<String, Token>();
      doubleOps.put("**", new Token(Token.EXPO));
      doubleOps.put(">=", new Token(Token.GE));
      doubleOps.put(":=", new Token(Token.GETS));
      doubleOps.put("<=", new Token(Token.LE));
      doubleOps.put("/=", new Token(Token.NE));
      doubleOps.put("..", new Token(Token.THRU));
   }

   private Token findToken(Map<String, Token> table, String target){
      Token t = table.get(target);
      if (t == null)
         return new Token(Token.ERROR);
      else
         return t;
   }

   private void skipBlanks(){
      while (ch == ' ' || ch == Chario.EL || ch == Chario.TAB)
         ch = chario.getChar();
   }

   private void getIdentifierOrKeyword(){
      int i = 0;
      int barCount = 0;
      StringBuffer id = new StringBuffer(MAX_KEY_SPELLING);
      tokenBuffer = new StringBuffer();
      token = new Token(Token.ID);
      if (ch == '_')
         chario.putError("illegal leading '_'");
      do{
         ch = Character.toUpperCase(ch);
         i++;
         tokenBuffer.append(ch);
         if (i <= MAX_KEY_SPELLING)
            id.append(ch);
         if (ch == '_'){
            ch = chario.getChar();
            if (ch == '_')
               barCount++;
            if (! Character.isLetterOrDigit(ch) && ch != '_')
               chario.putError("letter or digit expected after '_'");
         }
         else
            ch = chario.getChar();
      }while (Character.isLetterOrDigit(ch) || ch == '_');
      if (barCount > 0)
         chario.putError("letter or digit expected after '_'");
      if (i <= MAX_KEY_SPELLING){
         token = findToken(keywords, id.toString());
         if (token.code == Token.ERROR)
            token.code = Token.ID;
      }
      if (token.code == Token.ID)
         token.string = tokenBuffer.toString();
   }

   private void getInteger(){

      int base = 16;

      token = new Token(Token.INT);
      getBasedInteger(10);
      if (ch == '#'){
         base = token.integer;
         if (base < 2 || base > 16){
            chario.putError("base must be between 2 and 16");
            base = 16;
         }
         ch = chario.getChar();
         if (! Character.isLetterOrDigit(ch))
            chario.putError("letter or digit expected after '#'");
         getBasedInteger(base);
         if (ch == '#')
            ch = chario.getChar();
         else
            chario.putError("'#' expected");
      }
   }

   private void getBasedInteger(int base){
      int barCount = 0;
      token.integer = 0;
      while (Character.isLetterOrDigit(ch) || ch == '_')
         if (ch == '_'){
            ch = chario.getChar();
            if (ch == '_')
               barCount++;
            if (! Character.isLetterOrDigit(ch) && ch != '_')
               chario.putError("letter or digit expected after '_'");
         }
         else{
            token.integer = base * token.integer + charToInt(ch, base);
            ch = chario.getChar();
         }
      if (barCount > 0)
         chario.putError("letter or digit expected after '_'");
   }

   private int charToInt(char ch, int base){
      int digit = Character.digit(ch, base);
      if (digit == -1){
         chario.putError("digit not in range of base");
         digit = 0;
      }
      return digit;
   }

   private void getCharacter(){
      token = new Token(Token.CHAR);
      ch = chario.getChar();
      if (ch == Chario.EL){
         chario.putError("''' expected");
         tokenBuffer.append(' ');
         ch = chario.getChar();
      }
      else{
         token.string = "" + ch;
         ch = chario.getChar();
         if (ch == '\'')
            ch = chario.getChar();
         else
            chario.putError("''' expected");
      }
   }

   private void getDoubleOp(){
      tokenBuffer = new StringBuffer(2);
      tokenBuffer.append(ch);
      ch = chario.getChar();
      tokenBuffer.append(ch);
      token = findToken(doubleOps, tokenBuffer.toString());
      if (token.code != Token.ERROR)
         ch = chario.getChar();
   }

   private void getSingleOp(){
      token = findToken(singleOps, "" + tokenBuffer.charAt(0));
   }

   public Token nextToken(){
      do{
         skipBlanks();
         if (Character.isLetter(ch) || ch == '_')
            getIdentifierOrKeyword();
         else if (Character.isDigit(ch))
            getInteger();
         else if (ch == '\'')
            getCharacter();
         else if (ch == Chario.EF)
            token = new Token(Token.EOF);
         else{
            getDoubleOp();
            if (token.code == Token.ERROR){
               getSingleOp();
               if (token.code == Token.ERROR)
                  chario.putError("unrecognized symbol");
            }
         }
      }while (token.code == Token.ERROR);
      return token;
   }

}
