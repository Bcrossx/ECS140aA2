// Parsing shell partially completed

// Note that EBNF rules are provided in comments
// Just add new methods below rules without them

import java.util.*;

public class Parser extends Object{

   private Chario chario;
   private Scanner scanner;
   private Token token;

   private Set<Integer> addingOperator,
                        multiplyingOperator,
                        relationalOperator,
                        basicDeclarationHandles,
                        statementHandles;

   public Parser(Chario c, Scanner s){
      chario = c;
      scanner = s;
      initHandles();
      token = scanner.nextToken();
   }

   public void reset(){
      scanner.reset();
      token = scanner.nextToken();
   }

   private void initHandles(){
      addingOperator = new HashSet<Integer>();
      addingOperator.add(Token.PLUS);
      addingOperator.add(Token.MINUS);
      multiplyingOperator = new HashSet<Integer>();
      multiplyingOperator.add(Token.MUL);
      multiplyingOperator.add(Token.DIV);
      multiplyingOperator.add(Token.MOD);
      relationalOperator = new HashSet<Integer>();
      relationalOperator.add(Token.EQ);
      relationalOperator.add(Token.NE);
      relationalOperator.add(Token.LE);
      relationalOperator.add(Token.GE);
      relationalOperator.add(Token.LT);
      relationalOperator.add(Token.GT);
      basicDeclarationHandles = new HashSet<Integer>();
      basicDeclarationHandles.add(Token.TYPE);
      basicDeclarationHandles.add(Token.ID);
      basicDeclarationHandles.add(Token.PROC);
      statementHandles = new HashSet<Integer>();
      statementHandles.add(Token.EXIT);
      statementHandles.add(Token.ID);
      statementHandles.add(Token.IF);
      statementHandles.add(Token.LOOP);
      statementHandles.add(Token.NULL);
      statementHandles.add(Token.WHILE);
   }

   private void accept(int expected, String errorMessage){
      if (token.code != expected)
         fatalError(errorMessage);
      token = scanner.nextToken();
   }

   private void fatalError(String errorMessage){
      chario.putError(errorMessage);
      throw new RuntimeException("Fatal error");
   }

   public void parse(){
      subprogramBody();
      accept(Token.EOF, "extra symbols after logical end of program");
   }

   /*
   subprogramBody =
         subprogramSpecification "is"
         declarativePart
         "begin" sequenceOfStatements
         "end" [ <procedure>identifier ] ";"
   */
    private void subprogramBody(){ //x (//x means dont touch)
      subprogramSpecification();
      accept(Token.IS, "'is' expected");
      declarativePart();
      accept(Token.BEGIN, "'begin' expected");
      sequenceOfStatements();
      accept(Token.END, "'end' expected");
      if (token.code == Token.ID)
         token = scanner.nextToken();
      accept(Token.SEMI, "semicolon expected1");
   }

   /*
   subprogramSpecification = "procedure" identifier [ formalPart ]
   */
   private void subprogramSpecification(){ //x
     accept(Token.PROC, "'procedure' expected");
     accept(Token.ID,"identifier expected1");
     if (token.code == Token.L_PAR)
        //token = scanner.nextToken();
        formalPart();
   }

   /*
   formalPart = "(" parameterSpecification { ";" parameterSpecification } ")"
   */
   private void formalPart(){ //*
     accept(Token.L_PAR, "'(' expected1");
     parameterSpecification();
     while(token.code == Token.SEMI){
       token = scanner.nextToken();
       parameterSpecification();
     }
     accept(Token.R_PAR, "')' expected1");
   }

   /*
   parameterSpecification = identifierList ":" mode <type>identifier
   */
   private void parameterSpecification(){ //**
     identifierList();
     accept(Token.COLON, "':' expected1");
     mode();
     accept(Token.ID,"identifier expected2");
   }

   //mode = [ "in" ] | "in" "out" | "out"
   private void mode(){ //*
         if (token.code == Token.IN){
            accept(Token.IN, "'in' expected");
          }
         if (token.code == Token.OUT){
            accept(Token.OUT, "'out' expected");
          }
   }

   /*
   declarativePart = { basicDeclaration }
   */
   private void declarativePart(){ //x
      while (basicDeclarationHandles.contains(token.code))
         basicDeclaration();
   }

   /*
   basicDeclaration = objectDeclaration | numberDeclaration
                    | typeDeclaration | subprogramBody
   */
   private void basicDeclaration(){//x
      switch (token.code){
         case Token.ID:
            numberOrObjectDeclaration();
            break;
         case Token.TYPE:
            typeDeclaration();
            break;
         case Token.PROC:
            subprogramBody();
            break;
         default: fatalError("error in declaration part");
      }
   }

   /*
   objectDeclaration =
         identifierList ":" typeDefinition ";"

   numberDeclaration =
         identifierList ":" "constant" ":=" <static>expression ";"
   */
   private void numberOrObjectDeclaration(){ //x
      identifierList();
      accept(Token.COLON, "':' expected2");
      if (token.code == Token.CONST){
         token = scanner.nextToken();
         accept(Token.GETS, "':=' expected");
         expression();
      }
      else
         typeDefinition();
      System.out.print(token);
      accept(Token.SEMI, "semicolon expected2");
   }

   /*
   typeDeclaration = "type" identifier "is" typeDefinition ";"
   */
   private void typeDeclaration(){ //*
     accept(Token.TYPE, "'type' expected");
     accept(Token.ID, "identifier expected3");
     accept(Token.IS, "'is' expected");
     typeDefinition();
     accept(Token.SEMI, "';' expected1");
   }

   /*
   typeDefinition = enumerationTypeDefinition | arrayTypeDefinition
                  | range | <type>identifier
   */
   private void typeDefinition(){ //***
     switch (token.code){
        case Token.L_PAR:
          enumerationTypeDefinition();
          break;
        case Token.ARRAY:
          arrayTypeDefinition();
          break;
        case Token.RANGE:
          range();
          break;
        case Token.ID:
          accept(Token.ID, "identifier expected4");
          break;
        default: fatalError("error in declaration part");
   }
 }
   //identifierList = identifier {"," identifier}
   private void identifierList(){ //***
     accept(Token.ID,"identifier expected9");
     while (token.code == Token.COMMA){
       accept(Token.ID,"identifier expected01");
     }
   }

   /*
   enumerationTypeDefinition = "(" identifierList ")"
   */
   private void enumerationTypeDefinition(){ //***
     accept(Token.L_PAR, "'(' expected2");
     identifierList();
     token = scanner.nextToken();
     accept(Token.R_PAR,"')' expected2");
   }

   /*
   arrayTypeDefinition = "array" "(" index { "," index } ")" "of" <type>identifier
   */
   private void arrayTypeDefinition(){ //*
     accept(Token.ARRAY, "'array' expected");
     accept(Token.L_PAR, "'(' expected3");
     index();
     token = scanner.nextToken();
     while(token.code == Token.COMMA){
       accept(Token.COMMA, "',' expected");
       index();
       token = scanner.nextToken();
     }
     accept(Token.R_PAR, "')' expected3");
     accept(Token.OF,"'of' expected");
     accept(Token.ID,"identifier expected5");
   }

   /*
   index = range | <type>identifier
   */
   private void index(){ //*
    if(token.code == Token.RANGE)
      range();
    if(token.code == Token.ID)
      accept(Token.ID, "identifier expected02");
   }

   /*
   range = "range " simpleExpression ".." simpleExpression
   */
   private void range(){ //*
     accept(Token.RANGE, "'range' expected");
     simpleExpression();
     token = scanner.nextToken();
     accept(Token.THRU, "'..' expected");
     simpleExpression();
   }

   /*
   identifierList = identifier { "," identifer }
   */
   private void identiferList(){ //*
     accept(Token.ID, "identifier expected6");
     while(token.code == Token.COMMA){
       accept(Token.ID, "identifier expected7");
     }
   }

   /*
   sequenceOfStatements = statement { statement }
   */
   private void sequenceOfStatements(){ //x
      statement();
      while (statementHandles.contains(token.code))
         statement();
   }

   /*
   statement = simpleStatement | compoundStatement

   simpleStatement = nullStatement | assignmentStatement
                   | procedureCallStatement | exitStatement

   compoundStatement = ifStatement | loopStatement
   */
   private void statement(){ //x
      switch (token.code){
         case Token.ID:
            assignmentOrCallStatement();
            break;
         case Token.EXIT:
            exitStatement();
            break;
         case Token.IF:
            ifStatement();
            break;
         case Token.NULL:
            nullStatement();
            break;
         case Token.WHILE:
         case Token.LOOP:
            loopStatement();
            break;
         default: fatalError("error in statement");
      }
   }

   /*
   nullStatement = "null" ";"
   */
   private void nullStatement(){ //x
     accept(Token.NULL, "'null' expected");
     accept(Token.SEMI, "';' expected2");
   }

   /*
   loopStatement =
         [ iterationScheme ] "loop" sequenceOfStatements "end" "loop" ";"

   iterationScheme = "while" condition
   */
   private void loopStatement(){ //**
     while(token.code == Token.LOOP){
       accept(Token.LOOP, "'loop' expected");
       sequenceOfStatements();
       token = scanner.nextToken();
       accept(Token.END, "'end' expected");
       accept(Token.LOOP, "'loop' expected");
     }
    accept(Token.WHILE, "'while' expected");
    condition();
   }

   /*
   ifStatement =
         "if" condition "then" sequenceOfStatements
         { "elsif" condition "then" sequenceOfStatements }
         [ "else" sequenceOfStatements ]
         "end" "if" ";"
   */
   private void ifStatement(){ //**
     accept(Token.IF, "'if' expected");
     condition();
     token = scanner.nextToken();
     accept(Token.THEN, "'then' expected");
     sequenceOfStatements();
     while(token.code == Token.ELSIF){
       accept(Token.ELSIF,"'elsif' expected");
       condition();
       token = scanner.nextToken();
       while(token.code == Token.THEN){
         accept(Token.THEN, "'then' expected");
       }
       sequenceOfStatements();
     }
     if(token.code == Token.ELSE){
       accept(Token.ELSE,"'else' expected");
       sequenceOfStatements();
     }
     accept(Token.END, "'end' expected");
     accept(Token.IF, "'if' expected");
     accept(Token.SEMI, "';' expected3");
   }

   /*
   exitStatement = "exit" [ "when" condition ] ";"
   */
   private void exitStatement(){ //**
     accept(Token.EXIT, "'exit' expected");
     if(token.code == Token.WHEN){
       accept(Token.WHEN, "'when' expected");
       condition();
     }
     accept(Token.SEMI, "';' expected4");
   }

   /*
   assignmentStatement = <variable>name ":=" expression ";"

   procedureCallStatement = <procedure>name [ actualParameterPart ] ";"
   */
   private void assignmentOrCallStatement(){ //x
      name();
      if (token.code == Token.GETS){
         token = scanner.nextToken();
         expression();
      }
      accept(Token.SEMI, "semicolon expected3");
   }

   /*
   condition = <boolean>expression
   */
   private void condition(){ //x
      expression();
   }

   /*
   expression = relation { "and" relation } | { "or" relation }
   */
   private void expression(){ //x
      relation();
      token = scanner.nextToken();
      if (token.code == Token.AND)
         while (token.code == Token.AND){
            token = scanner.nextToken();
            relation();
         }
      else if (token.code == Token.OR)
         while (token.code == Token.OR){
            token = scanner.nextToken();
            relation();
         }
   }

   /*
   relation = simpleExpression [ relationalOperator simpleExpression ]
   */
   private void relation(){ //**
     simpleExpression();
     if(relationalOperator.contains(token.code)){
       token = scanner.nextToken();
       simpleExpression();
     }
   }

   /*
  simpleExpression =
         [ unaryAddingOperator ] term { binaryAddingOperator term }
   */
   private void simpleExpression(){ //x
      if (addingOperator.contains(token.code))
         token = scanner.nextToken();
      term();
      while (addingOperator.contains(token.code)){
         token = scanner.nextToken();
         term();
      }
   }

   /*
   term = factor { multiplyingOperator factor }
   */
   private void term(){ //*
     factor();
     while(multiplyingOperator.contains(token.code)){
       token = scanner.nextToken();
       factor();
     }
   }

   /*
   factor = primary [ "**" primary ] | "not" primary
   */
   private void factor(){ //**
    primary();
    if(token.code == Token.EXPO)
      primary();
    if(token.code == Token.NOT)
      primary();
   }

   /*
   primary = numericLiteral | name | "(" expression ")"
   */
   void primary(){ //x
      switch (token.code){
         case Token.INT:
         case Token.CHAR:
            token = scanner.nextToken();
            break;
         case Token.ID:
            name();
            break;
         case Token.L_PAR:
            token = scanner.nextToken();
            expression();
            accept(Token.R_PAR, "')' expected4");
            break;
         default: fatalError("error in primary");
      }
   }

   /*
   name = identifier [ indexedComponent ]
   */
   private void name(){ //x
    accept(Token.ID, "identifier expected8");
    if (token.code == Token.L_PAR)
      token = scanner.nextToken();
      indexedComponent();
   }

   /*
   indexedComponent = "(" expression  { "," expression } ")"
   */
   private void indexedComponent(){ //*
     accept(Token.L_PAR, "'(' expected4");
     expression();
     while(token.code == Token.COMMA){
       token = scanner.nextToken();
       expression();
     }
     accept(Token.R_PAR, "')' expected5");
   }

}
