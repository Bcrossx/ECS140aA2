
import java.io.*;
import java.util.*;

public class TerminalApp{

   // Data model
   private Chario chario;
   private Scanner scanner;
   private Parser parser;
   private String args[];


   public TerminalApp(String args[]){
      String filename = args[0];
      FileInputStream stream;
      try{
         stream = new FileInputStream(filename);
     }catch(IOException e){
         System.out.println("Error opening file.");
         return;
      }
      chario = new Chario(stream);
      //testChario();
      scanner = new Scanner(chario);
      //testScanner();
      parser = new Parser(chario, scanner, args);
      testParser();
   }

   private void testChario(){
      char ch = chario.getChar();
      while (ch != Chario.EF)
         ch = chario.getChar();
      chario.reportErrors();
   }

   private void testScanner(){
      Token token = scanner.nextToken();
      while (token.code != Token.EOF){
         chario.println(token.toString());
         token = scanner.nextToken();
      }
      chario.reportErrors();
   }

   private void testParser(){
      try{
         parser.parse();
      }
      catch(Exception e){}
      chario.reportErrors();
   }

   public static void main(String args[]){
     new TerminalApp(args);
   }
}
