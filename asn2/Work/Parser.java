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

import java.util.*;

public class Parser extends Object{

   private Chario chario;
   private Scanner scanner;
   private Token token;
   private SymbolTable table;
   private String args[];

   private Set<Integer> addingOperator,
                        multiplyingOperator,
                        relationalOperator,
                        basicDeclarationHandles,
                        statementHandles;

   public Parser(Chario c, Scanner s, String args[]){
      this.args = args;
      chario = c;
      scanner = s;
      initHandles();
      initTable();
      token = scanner.nextToken();
   }

   public void reset(){
      scanner.reset();
      initTable();
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

   private void acceptRole(SymbolEntry s, int expected, String errorMessage){
      if (s.role != SymbolEntry.NONE && s.role != expected)
         chario.putError(errorMessage);
   }

   private void acceptRole(SymbolEntry s, Set<Integer> expected, String errorMessage){
      if (s.role != SymbolEntry.NONE && ! (expected.contains(s.role)))
         chario.putError(errorMessage);
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

   //Scope analysis routines
   private void initTable(){
      table = new SymbolTable(chario, args);
      table.enterScope();
      SymbolEntry entry = table.enterSymbol("BOOLEAN");
      entry.setRole(SymbolEntry.TYPE);
      entry = table.enterSymbol("CHAR");
      entry.setRole(SymbolEntry.TYPE);
      entry = table.enterSymbol("INTEGER");
      entry.setRole(SymbolEntry.TYPE);
      entry = table.enterSymbol("TRUE");
      entry.setRole(SymbolEntry.CONST);
      entry = table.enterSymbol("FALSE");
      entry.setRole(SymbolEntry.CONST);
   }

   private SymbolEntry enterId(){
      SymbolEntry entry = null;
      if (token.code == Token.ID)
         entry = table.enterSymbol(token.string);
      else
         fatalError("identifier expected");
      token = scanner.nextToken();
      return entry;
   }

   private SymbolEntry findId(){
      SymbolEntry entry = null;
      if (token.code == Token.ID)
         entry = table.findSymbol(token.string);
      else
         fatalError("identifier expected");
      token = scanner.nextToken();
      return entry;
   }

   public void parse(){
      subprogramBody();
      accept(Token.EOF, "extra symbols after logical end of program");
      table.exitScope();
   }

   /*
   subprogramBody =
         subprogramSpecification "is"
         declarativePart
         "begin" sequenceOfStatements
         "end" [ <procedure>identifier ] ";"
   */
    private void subprogramBody(){ //x (//x means given method)
      subprogramSpecification();
      accept(Token.IS, "'is' expected");
      declarativePart();
      accept(Token.BEGIN, "'begin' expected");
      sequenceOfStatements();
      accept(Token.END, "'end' expected");
      table.exitScope();
      if (token.code == Token.ID){
         SymbolEntry entry = findId();
         acceptRole(entry, SymbolEntry.PROC, "procedure expected");
      }
      accept(Token.SEMI, "semicolon expected");
   }

   /*
   subprogramSpecification = "procedure" identifier [ formalPart ]
   */
   //We need "procedure" and an identifier after it
   //then we check for "(" and if so call formalPart();
   private void subprogramSpecification(){
     accept(Token.PROC, "'procedure' expected");
     SymbolEntry entry = enterId();
     entry.setRole(SymbolEntry.PROC);
     table.enterScope();
     if (token.code == Token.L_PAR)
        formalPart();
   }

   /*
   formalPart = "(" parameterSpecification { ";" parameterSpecification } ")"
   */
   //Check for "(" and then call parameterSpecification(),
   //while we see ";" scan the next token and call parameterSpecification()
   //again. Closing with a ")".
   private void formalPart(){
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
   //Call identifierList() followed by a check for ":" and a call
   //for mode() which is defined below. Ends with an identifier.
   private void parameterSpecification(){
     SymbolEntry list = identifierList();
     list.setRole(SymbolEntry.PARAM);
     accept(Token.COLON, "':' expected1");
     mode();
     SymbolEntry entry = findId();
     acceptRole(entry, SymbolEntry.TYPE, "type name expected1");
   }

   /* mode = [ "in" ] | "in" "out" | "out" */
   //Can be either "in" or "out" or both
   //so just check either case individually.
   private void mode(){
         if (token.code == Token.IN){
            token = scanner.nextToken();
          }
         if (token.code == Token.OUT){
            token = scanner.nextToken();
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
   private void basicDeclaration(){ //x
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
     SymbolEntry list = identifierList();
     accept(Token.COLON, "':' expected");
     if (token.code == Token.CONST){
        list.setRole(SymbolEntry.CONST);
        token = scanner.nextToken();
        accept(Token.GETS, "':=' expected");
        expression();
     }
     else{
        list.setRole(SymbolEntry.VAR);
        typeDefinition();
     }
     accept(Token.SEMI, "semicolon expected");
   }

   /*
   typeDeclaration = "type" identifier "is" typeDefinition ";"
   */
   //Straight forward, check type, identifier, and "is"; then
   //call typeDefinition() and end with a semicolon
   private void typeDeclaration(){
     accept(Token.TYPE, "'type' expected");
     SymbolEntry entry = enterId();
     entry.setRole(Token.TYPE);
     accept(Token.IS, "'is' expected");
     typeDefinition();
     accept(Token.SEMI, "';' expected1");
   }

   /*
   typeDefinition = enumerationTypeDefinition | arrayTypeDefinition
                  | range | <type>identifier
   */
   //I use a switch statement here with the cases being: checking
   //for "(" token, if so call enumerationTypeDefinition(); if token
   //== array, call arrayTypeDefinition(); if token == range, call range();
   //if token == identifier we just accept.
   private void typeDefinition(){
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
          SymbolEntry entry = findId();
          entry.setRole(SymbolEntry.TYPE);
          break;
        default: fatalError("error in definition part");
   }
 }

   /*
   enumerationTypeDefinition = "(" identifierList ")"
   */
   //check for "(" first then call identifierList() and end with
   // a ")".
   private void enumerationTypeDefinition(){
      accept(Token.L_PAR, "'(' expected");
      SymbolEntry list = identifierList();
      list.setRole(SymbolEntry.CONST);
      accept(Token.R_PAR, "')' expected");
   }

   /*
   arrayTypeDefinition = "array" "(" index { "," index } ")" "of" <type>identifier
   */
   //Check for "array" then "(" and call index(). While we see a comma
   //we accept and call index(). End with ")" and "of" and an identifier.
   private void arrayTypeDefinition(){
     accept(Token.ARRAY, "'array' expected");
     accept(Token.L_PAR, "'(' expected3");
     index();
     while(token.code == Token.COMMA){
       accept(Token.COMMA, "',' expected");
       index();
     }
     accept(Token.R_PAR, "')' expected3");
     accept(Token.OF,"'of' expected");
     SymbolEntry entry = findId();
     acceptRole(entry, SymbolEntry.TYPE, "type name expected");
   }

   /*
   index = range | <type>identifier
   */
   // If token == range, call range; or If token ==
   // identifier then we accept
   private void index(){
     if (token.code == Token.RANGE)
        range();
     else if (token.code == Token.ID){
        SymbolEntry entry = findId();
        //acceptRole(entry, SymbolEntry.TYPE, "type name expected2");
        entry.setRole(SymbolEntry.TYPE);
     }
     else
        fatalError("error in index type");
  }

   /*
   range = "range " simpleExpression ".." simpleExpression
   */
   //accept range, call simpleExpression(), accept "..",
   // and call simpleExpression() again.
   private void range(){
     accept(Token.RANGE, "'range' expected");
     simpleExpression();
     accept(Token.THRU, "'..' expected");
     simpleExpression();
   }

   /*
   identifierList = identifier { "," identifer }
   */
   //accept an identifier, then while we see a comma
   //scan next token and accept if it is an identifier
   private SymbolEntry identifierList(){
      SymbolEntry list = enterId();
      while (token.code == Token.COMMA){
         token = scanner.nextToken();
         list.append(enterId());
      }
      return list;
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
   // accepts "null" token followed by a ";" token.
   private void nullStatement(){
     accept(Token.NULL, "'null' expected");
     accept(Token.SEMI, "';' expected2");
   }

   /*
   loopStatement =
         [ iterationScheme ] "loop" sequenceOfStatements "end" "loop" ";"

   iterationScheme = "while" condition
   */
   //check for "while" or "loop" token. If "loop" get the next
   // token then call sequenceOfStatements(), accept "end" "loop"
   // and ";" tokens. If "while" get the next token and call condition()
   private void loopStatement(){
     if(token.code == Token.LOOP){
       token = scanner.nextToken();
       sequenceOfStatements();
       accept(Token.END,"'end' expected");
       accept(Token.LOOP,"'loop' expected");
       accept(Token.SEMI,"';' expected");
     }
     if(token.code == Token.WHILE){
       token = scanner.nextToken();
       condition();
     }
   }

   /*
   ifStatement =
         "if" condition "then" sequenceOfStatements
         { "elsif" condition "then" sequenceOfStatements }
         [ "else" sequenceOfStatements ]
         "end" "if" ";"
   */
   // accepts "if" token, call condition(), accept "then" token
   // and call sequenceOfStatements(). While theres a "elsif" token
   // get next token, call condition() and accept "then", then call
   // sequenceOfStatements(). If the token after the while finishes
   // is "else" get the next token and call sequenceOfStatements().
   // Then accept "end", "if", and ";" tokens.
   private void ifStatement(){
     accept(Token.IF, "'if' expected");
     condition();
     accept(Token.THEN, "'then' expected");
     sequenceOfStatements();
     while(token.code == Token.ELSIF){
       token = scanner.nextToken();
       condition();
       accept(Token.THEN,"'then' expected");
       sequenceOfStatements();
     }
     if(token.code == Token.ELSE){
       token = scanner.nextToken();
       sequenceOfStatements();
     }
     accept(Token.END, "'end' expected3");
     accept(Token.IF, "'if' expected");
     accept(Token.SEMI, "';' expected3");
   }

   /*
   exitStatement = "exit" [ "when" condition ] ";"
   */
   // accepts "exit" token and if the next token is "when" get the
   // token after and call condition() on it.
   // close by accepting a ";" token
   private void exitStatement(){
     accept(Token.EXIT, "'exit' expected");
     if(token.code == Token.WHEN){
       token = scanner.nextToken();
       condition();
     }
     accept(Token.SEMI, "';' expected4");
   }

   /*
   assignmentStatement = <variable>name ":=" expression ";"

   procedureCallStatement = <procedure>name ";"
   */
   private void assignmentOrCallStatement(){ //x
      SymbolEntry entry = name();
      Set<Integer> set = new HashSet<Integer>();
    if(token.code == Token.GETS){
        token = scanner.nextToken();
        set.add(SymbolEntry.VAR);
        set.add(SymbolEntry.PARAM);
        acceptRole(entry, set, "variable and parameter name expected");
        expression();
    } else {
      acceptRole(entry, SymbolEntry.PROC, "procedure name expected");
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
   // calls simpleExpression(), then if the next token is a relationalOperator
   // scan in the next token and call simpleExpression() again.
   private void relation(){
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
   // calls factor() and while a multiplyingOperator token is seen
   // scan in next token and call factor() again.
   private void term(){
     factor();
     while(multiplyingOperator.contains(token.code)){
       token = scanner.nextToken();
       factor();
     }
   }

   /*
   factor = primary [ "**" primary ] | "not" primary
   */
   // calls primary() and if the next token is "**" or "not" call
   // primary() again.
   private void factor(){
    primary();
    if(token.code == Token.EXPO || token.code == Token.NOT)
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
            SymbolEntry entry = name();
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
   // accept an identifier and if the next token is "("
   // call indexedComponent()
   private SymbolEntry name(){
    SymbolEntry entry = findId();
    if (token.code == Token.L_PAR)
      indexedComponent();
    return entry;
   }

   /*
   indexedComponent = "(" expression  { "," expression } ")"
   */
   // accepts a "(" token then calls expression(). While a "," token
   // is seen we scan the next token and call expression(). End with
   // a ")" token.
   private void indexedComponent(){
     accept(Token.L_PAR, "'(' expected4");
     expression();
     while(token.code == Token.COMMA){
       token = scanner.nextToken();
       expression();
     }
     accept(Token.R_PAR, "')' expected5");
   }

}
