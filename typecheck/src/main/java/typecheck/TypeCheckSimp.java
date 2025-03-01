package typecheck;

import visitor.*;

import java.util.ArrayList;

import syntaxtree.*;

import symbolTable.*;

public class TypeCheckSimp extends GJDepthFirst<Symbol, SymbolTable>{

    String currClass;
    String currMethod;

    public ArrayList<TypeError> errors;

    private MethodSymbol methodSymbolTarget;
    private int pCounter;
    private int methodLine;

    public TypeCheckSimp() {
        this.currMethod = "";
        this.methodSymbolTarget = null;
        this.errors = new ArrayList<>();
    }

   /**
    * f0 -> MainClass()
    * f1 -> ( TypeDeclaration() )*
    * f2 -> <EOF>
    */
   public Symbol visit(Goal n, SymbolTable argu) {
      Symbol _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "{"
    * f3 -> "public"
    * f4 -> "static"
    * f5 -> "void"
    * f6 -> "main"
    * f7 -> "("
    * f8 -> "String"
    * f9 -> "["
    * f10 -> "]"
    * f11 -> Identifier()
    * f12 -> ")"
    * f13 -> "{"
    * f14 -> ( VarDeclaration() )*
    * f15 -> ( Statement() )*
    * f16 -> "}"
    * f17 -> "}"
    */
   public Symbol visit(MainClass n, SymbolTable argu) {
      Symbol _ret=null;

      // BEGIN MAIN CLASS SCOPE
      currClass = n.f1.f0.toString();

      n.f11.accept(this, argu);
      n.f14.accept(this, argu);
      n.f15.accept(this, argu);

      // END MAIN CLASS SCOPE
      currClass = "";

      return _ret;

   }

   /**
    * f0 -> ClassDeclaration()
    *       | ClassExtendsDeclaration()
    */
   public Symbol visit(TypeDeclaration n, SymbolTable argu) {
      Symbol _ret=null;
      n.f0.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "{"
    * f3 -> ( VarDeclaration() )*
    * f4 -> ( MethodDeclaration() )*
    * f5 -> "}"
    */
   public Symbol visit(ClassDeclaration n, SymbolTable argu) {
      Symbol _ret=null;


      // BEGIN CLASS SCOPE
      currClass = n.f1.f0.toString();

      n.f1.accept(this, argu);
      n.f3.accept(this, argu);
      n.f4.accept(this, argu);

      // END CLASS SCOPE
      currClass = "";
      return _ret;
   }

   /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "extends"
    * f3 -> Identifier()
    * f4 -> "{"
    * f5 -> ( VarDeclaration() )*
    * f6 -> ( MethodDeclaration() )*
    * f7 -> "}"
    */
   public Symbol visit(ClassExtendsDeclaration n, SymbolTable argu) {
      Symbol _ret=null;

      // BEGIN CLASS SCOPE
      currClass = n.f1.f0.toString();

      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      n.f3.accept(this, argu);
      n.f4.accept(this, argu);
      n.f5.accept(this, argu);
      n.f6.accept(this, argu);
      n.f7.accept(this, argu);

      // END CLASS SCOPE
      currClass = "";
      return _ret;
   }

   /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
   public Symbol visit(VarDeclaration n, SymbolTable argu) {
      Symbol _ret=null;
      if (n.f0.accept(this, argu) instanceof MethodSymbol) {
          errors.add(new TypeError(n.f2.beginLine, "can not declare variable of method type"));
      }
      n.f1.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "public"
    * f1 -> Type()
    * f2 -> Identifier()
    * f3 -> "("
    * f4 -> ( FormalParameterList() )?
    * f5 -> ")"
    * f6 -> "{"
    * f7 -> ( VarDeclaration() )*
    * f8 -> ( Statement() )*
    * f9 -> "return"
    * f10 -> Expression()
    * f11 -> ";"
    * f12 -> "}"
    */
   public Symbol visit(MethodDeclaration n, SymbolTable argu) {
      Symbol _ret=null;

      Symbol retType = n.f1.accept(this, argu);
      if (retType instanceof MethodSymbol) {
          errors.add(new TypeError(n.f0.beginLine, "Can not return a method"));
      }

      methodLine = n.f0.beginLine;

      // BEGIN METHOD SCOPE
      currMethod = n.f2.f0.toString();

      n.f2.accept(this, argu);
      n.f4.accept(this, argu);
      n.f7.accept(this, argu);
      n.f8.accept(this, argu);
      Symbol returnSym = n.f10.accept(this, argu);
      if (argu.classes.contains(returnSym.getType())) {

          ClassSymbol typeClass = (ClassSymbol) argu.getClass(returnSym.getType());
          ArrayList<String> validClasses = argu.superClassList(typeClass);
          if (!validClasses.contains(retType.getType())) {
              errors.add(new TypeError(n.f0.beginLine, String.format("%s or subClass Expected", retType.getType())));
          }
      } else if (!returnSym.getType().equals(retType.getType())) {
          errors.add(new TypeError(n.f0.beginLine, "Mismatched return type."));
      }

      // END METHOD SCOPE
      currMethod = "";

      return _ret;
   }

   /**
    * f0 -> FormalParameter()
    * f1 -> ( FormalParameterRest() )*
    */
   public Symbol visit(FormalParameterList n, SymbolTable argu) {
      Symbol _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> Type()
    * f1 -> Identifier()
    */
   public Symbol visit(FormalParameter n, SymbolTable argu) {
      Symbol _ret=null;
      n.f0.accept(this, argu);
      if (n.f0.accept(this, argu) instanceof MethodSymbol) {
          errors.add(new TypeError(methodLine, "Can not use a method as a parameter"));
      }
      n.f1.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> ","
    * f1 -> FormalParameter()
    */
   public Symbol visit(FormalParameterRest n, SymbolTable argu) {
      Symbol _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> ArrayType()
    *       | BooleanType()
    *       | IntegerType()
    *       | Identifier()
    */
   public Symbol visit(Type n, SymbolTable argu) {
      return n.f0.accept(this, argu);
   }

   /**
    * f0 -> "int"
    * f1 -> "["
    * f2 -> "]"
    */
   public Symbol visit(ArrayType n, SymbolTable argu) {
       return Symbol.newSimpleSymbol("int[]");
   }

   /**
    * f0 -> "boolean"
    */
   public Symbol visit(BooleanType n, SymbolTable argu) {
       return Symbol.newSimpleSymbol("boolean");
   }

   /**
    * f0 -> "int"
    */
   public Symbol visit(IntegerType n, SymbolTable argu) {
       return Symbol.newSimpleSymbol("int");
   }

   /**
    * f0 -> Block()
    *       | AssignmentStatement()
    *       | ArrayAssignmentStatement()
    *       | IfStatement()
    *       | WhileStatement()
    *       | PrintStatement()
    */
   public Symbol visit(Statement n, SymbolTable argu) {
      Symbol _ret=null;
      n.f0.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "{"
    * f1 -> ( Statement() )*
    * f2 -> "}"
    */
   public Symbol visit(Block n, SymbolTable argu) {
      Symbol _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> Identifier()
    * f1 -> "="
    * f2 -> Expression()
    * f3 -> ";"
    */
   public Symbol visit(AssignmentStatement n, SymbolTable argu) {

      Symbol idSym = n.f0.accept(this, argu);
      Symbol typeSym = n.f2.accept(this, argu);

      if (idSym == null) {
          errors.add(new TypeError(n.f1.beginLine, String.format("Identifier not defined", idSym)));
          return null;
      }
      if (typeSym == null) {
          errors.add(new TypeError(n.f1.beginLine, String.format("Expression not defined", typeSym)));
          return null;
      }

      // Handle inherited types
      if (argu.classes.contains(typeSym.getType())) {
          ClassSymbol typeClass = (ClassSymbol) argu.getClass(typeSym.getType());
          ArrayList<String> validClasses = argu.superClassList(typeClass);
          if (!validClasses.contains(idSym.getType())) {
              errors.add(new TypeError(n.f1.beginLine, String.format("%s or subClass Expected", idSym.getType())));
          }
      } else {
          if (!idSym.getType().equals(typeSym.getType())) {
              errors.add(new TypeError(n.f1.beginLine, String.format("%s Expected, symbol %s", idSym.getType(), typeSym.getType())));
          }
      }
      return null;
   }

   /**
    * f0 -> Identifier()
    * f1 -> "["
    * f2 -> Expression()
    * f3 -> "]"
    * f4 -> "="
    * f5 -> Expression()
    * f6 -> ";"
    */
   public Symbol visit(ArrayAssignmentStatement n, SymbolTable argu) {
       if (n.f0.accept(this, argu) == null) {
           errors.add(new TypeError(n.f1.beginLine, "Identifier not defined"));
       }
       if (!n.f0.accept(this, argu).getType().equals("int[]")) {
           errors.add(new TypeError(n.f1.beginLine, "int[] Expected"));
       }
       if (!n.f2.accept(this,argu).getType().equals("int")) {
           errors.add(new TypeError(n.f1.beginLine, "Int Expected"));
       }
       if (!n.f5.accept(this,argu).getType().equals("int")) {
           errors.add(new TypeError(n.f1.beginLine, "Int Expected"));
       }
       return null;
   }

   /**
    * f0 -> "if"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    * f5 -> "else"
    * f6 -> Statement()
    */
   public Symbol visit(IfStatement n, SymbolTable argu) {
       if (!n.f2.accept(this,argu).getType().equals("boolean")) {
           errors.add(new TypeError(n.f0.beginLine, "Boolean Expected"));
       }
       n.f4.accept(this, argu);
       n.f6.accept(this, argu);
       return null;
   }

   /**
    * f0 -> "while"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    */
   public Symbol visit(WhileStatement n, SymbolTable argu) {
       if (!n.f2.accept(this,argu).getType().equals("boolean")) {
           errors.add(new TypeError(n.f0.beginLine, "Boolean Expected"));
       }
       return null;
   }

   /**
    * f0 -> "System.out.println"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> ";"
    */
   public Symbol visit(PrintStatement n, SymbolTable argu) {
       if (n.f2.accept(this, argu) == null) {
           errors.add(new TypeError(n.f0.beginLine, "Int Expected"));
           return null;
       }
       if (!n.f2.accept(this,argu).getType().equals("int")) {
           errors.add(new TypeError(n.f0.beginLine, "Int Expected"));
       }
      return null;
   }

   /**
    * f0 -> AndExpression()
    *       | CompareExpression()
    *       | PlusExpression()
    *       | MinusExpression()
    *       | TimesExpression()
    *       | ArrayLookup()
    *       | ArrayLength()
    *       | MessageSend()
    *       | PrimaryExpression()
    */
   public Symbol visit(Expression n, SymbolTable argu) {
       return n.f0.accept(this, argu);
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "&&"
    * f2 -> PrimaryExpression()
    */
   public Symbol visit(AndExpression n, SymbolTable argu) {
      if (!n.f0.accept(this, argu).getType().equals("boolean")) {
          errors.add(new TypeError(n.f1.beginLine, "Boolean Expected"));
      }
      if (!n.f2.accept(this, argu).getType().equals("boolean")) {
          errors.add(new TypeError(n.f1.beginLine, "Boolean Expected"));
      }
      return Symbol.newSimpleSymbol("boolean");
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "<"
    * f2 -> PrimaryExpression()
    */
   public Symbol visit(CompareExpression n, SymbolTable argu) {
      if (!n.f0.accept(this, argu).getType().equals("int")) {
          errors.add(new TypeError(n.f1.beginLine, "Int Expected"));
      }
      if (!n.f2.accept(this, argu).getType().equals("int")) {
          errors.add(new TypeError(n.f1.beginLine, "Int Expected"));
      }
      return Symbol.newSimpleSymbol("boolean");
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "+"
    * f2 -> PrimaryExpression()
    */
   public Symbol visit(PlusExpression n, SymbolTable argu) {
      if (!n.f0.accept(this, argu).getType().equals("int")) {
          errors.add(new TypeError(n.f1.beginLine, "Int Expected"));
      }
      if (!n.f2.accept(this, argu).getType().equals("int")) {
          errors.add(new TypeError(n.f1.beginLine, "Int Expected"));
      }
      return Symbol.newSimpleSymbol("int");
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "-"
    * f2 -> PrimaryExpression()
    */
   public Symbol visit(MinusExpression n, SymbolTable argu) {
      if (!n.f0.accept(this, argu).getType().equals("int")) {
          errors.add(new TypeError(n.f1.beginLine, "Int Expected"));
      }
      if (!n.f2.accept(this, argu).getType().equals("int")) {
          errors.add(new TypeError(n.f1.beginLine, "Int Expected"));
      }
      return Symbol.newSimpleSymbol("int");
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "*"
    * f2 -> PrimaryExpression()
    */
   public Symbol visit(TimesExpression n, SymbolTable argu) {
      if (!n.f0.accept(this, argu).getType().equals("int")) {
          errors.add(new TypeError(n.f1.beginLine, "Int Expected"));
      }
      if (!n.f2.accept(this, argu).getType().equals("int")) {
          errors.add(new TypeError(n.f1.beginLine, "Int Expected"));
      }
      return Symbol.newSimpleSymbol("int");
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "["
    * f2 -> PrimaryExpression()
    * f3 -> "]"
    */
   public Symbol visit(ArrayLookup n, SymbolTable argu) {
      if (!n.f0.accept(this, argu).getType().equals("int[]")) {
          errors.add(new TypeError(n.f1.beginLine, "Int[] Expected"));
      }
      if (!n.f2.accept(this, argu).getType().equals("int")) {
          errors.add(new TypeError(n.f1.beginLine, "Int Expected"));
      }
      return Symbol.newSimpleSymbol("int");
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> "length"
    */
   public Symbol visit(ArrayLength n, SymbolTable argu) {
      if (!n.f0.accept(this, argu).getType().equals("int[]")) {
          errors.add(new TypeError(n.f1.beginLine, "Int[] Expected"));
      }
      return Symbol.newSimpleSymbol("int");
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> Identifier()
    * f3 -> "("
    * f4 -> ( ExpressionList() )?
    * f5 -> ")"
    */
   public Symbol visit(MessageSend n, SymbolTable argu) {
      // Check that PrimaryExpression is a Identifier with
      // a ClassSymbol type.
      if ( n.f0.accept(this, argu) == null || !(argu.get((n.f0.accept(this, argu).getType())) instanceof ClassSymbol)) {
          errors.add(new TypeError(n.f1.beginLine, "Method call must be on a class obj"));
          return null;
      }
      // What class does the method belong to?
      ClassSymbol callingClass = (ClassSymbol) (argu.get(n.f0.accept(this, argu).getType()));

      String tmp = currClass;
      // Enter calling class's scope
      currClass = callingClass.getType();

      if (n.f2.accept(this, argu) == null) {
          errors.add(new TypeError(n.f1.beginLine, "Identifier not defined"));
          return null;
      }

      if (!callingClass.hasMethod(n.f2.accept(this, argu).getType())) {
          errors.add(new TypeError(n.f1.beginLine, "Calling class does not contain a method with this name"));
          return null;
      }

      MethodSymbol method = (MethodSymbol) n.f2.accept(this, argu);
      // Restore class scope
      currClass = tmp;

      // Store target for ExpressionList evaluation
      MethodSymbol tmpSym = methodSymbolTarget;
      methodSymbolTarget = method;
      pCounter = 0;
      methodLine = n.f1.beginLine;
      // ExpressionList must match the parameters of the MethodSymbol (or properly subclass)
      n.f4.accept(this, argu);
      // If all matches without error, method will return it's declared return type
      if (pCounter != methodSymbolTarget.parameters.size()) {
          errors.add(new TypeError(n.f1.beginLine, "Improper number of arguments given"));
      }
      methodSymbolTarget = tmpSym;
      return Symbol.newSimpleDymSymbol(method.getReturnType());
   }

   /**
    * f0 -> Expression()
    * f1 -> ( ExpressionRest() )*
    */
   public Symbol visit(ExpressionList n, SymbolTable argu) {

      String exprType = n.f0.accept(this, argu).getType();
      Symbol param = methodSymbolTarget.parameters.get(pCounter);
      // Is the param a class obj?
      if (argu.get(param.getType()) instanceof ClassSymbol) {
          // Is the method expecting a class obj?
          if (!(argu.get(exprType) instanceof ClassSymbol)) {
              errors.add(new TypeError(methodLine, String.format("%s Expected",param.getType())));
              return null;
          }
          // What are all of the extended classes of the parameter obj?
          ClassSymbol arg = (ClassSymbol)argu.get(exprType);
          ArrayList<String> extClasses = argu.superClassList(arg);
          // Does the list of extended classes contain the expected type?
          if (!extClasses.contains(param.getType())) {
              errors.add(new TypeError(methodLine, String.format("%s or subClass Expected", param.getType())));
          }
          // not class obj, easy compare
      } else {
          if (!exprType.equals(param.getType())) {
              errors.add(new TypeError(methodLine, String.format("%s Expected", param.getType())));
              return null;
          }
      }
      pCounter++;
      n.f1.accept(this, argu);
      return null;
   }

   /**
    * f0 -> ","
    * f1 -> Expression()
    */
   public Symbol visit(ExpressionRest n, SymbolTable argu) {
      String exprType = n.f1.accept(this, argu).getType();
      Symbol param = methodSymbolTarget.parameters.get(pCounter);
      // Is the param a class obj?
      if (argu.get(param.getType()) instanceof ClassSymbol) {
          // Is the method expecting a class obj?
          if (!argu.classes.contains(exprType)) {
              errors.add(new TypeError(methodLine, String.format("%s Expected",param.getType())));
              return null;
          }
          // What are all of the extended classes of the parameter obj?
          ClassSymbol arg = (ClassSymbol)argu.get(exprType);
          ArrayList<String> extClasses = argu.superClassList(arg);
          // Does the list of extended classes contain the expected type?
          if (!extClasses.contains(param.getType())) {
              errors.add(new TypeError(methodLine, String.format("%s or subClass Expected", param.getType())));
          }
          // not class obj, easy compare
      } else {
          if (!exprType.equals(param.getType())) {
              errors.add(new TypeError(methodLine, String.format("%s Expected", param.getType())));
              return null;
          }
      }
      pCounter++;
      n.f1.accept(this, argu);
      return null;
   }


   /**
    * f0 -> IntegerLiteral()
    *       | TrueLiteral()
    *       | FalseLiteral()
    *       | Identifier()
    *       | ThisExpression()
    *       | ArrayAllocationExpression()
    *       | AllocationExpression()
    *       | NotExpression()
    *       | BracketExpression()
    */
   public Symbol visit(PrimaryExpression n, SymbolTable argu) {
      return n.f0.accept(this, argu);
   }

   /**
    * f0 -> <INTEGER_LITERAL>
    */
   public Symbol visit(IntegerLiteral n, SymbolTable argu) {
      return Symbol.newSimpleSymbol("int");
   }

   /**
    * f0 -> "true"
    */
   public Symbol visit(TrueLiteral n, SymbolTable argu) {
       return Symbol.newSimpleSymbol("boolean");
   }

   /**
    * f0 -> "false"
    */
   public Symbol visit(FalseLiteral n, SymbolTable argu) {
       return Symbol.newSimpleSymbol("boolean");
   }

   /**
    * f0 -> <IDENTIFIER>
    */
   public Symbol visit(Identifier n, SymbolTable argu) {
       Symbol sym = argu.getSymbol(n.f0.toString(), currClass, currMethod);
       if (sym == null) {
           Symbol mSym = argu.getSymbol(n.f0.toString()+"_M", currClass, currMethod);
           if (mSym == null) {
               errors.add(new TypeError(n.f0.beginLine, "Identifier not found"));
               return Symbol.newSimpleSymbol("void");
           }
           return mSym;
       }
       // Returns Classes, methods, or fieldsNames.
       // Distinction between these must be done in traversals above this.
       return argu.getSymbol(n.f0.toString(), currClass, currMethod);
   }

   /**
    * f0 -> "this"
    */
   public Symbol visit(ThisExpression n, SymbolTable argu) {
       return argu.get(currClass);
   }

   /**
    * f0 -> "new"
    * f1 -> "int"
    * f2 -> "["
    * f3 -> Expression()
    * f4 -> "]"
    */
   // must use an int for allocation size.
   public Symbol visit(ArrayAllocationExpression n, SymbolTable argu) {
       Symbol sym = n.f3.accept(this, argu);
       if (sym == null) {
           errors.add(new TypeError(n.f2.beginLine, "Null reference"));
           return Symbol.newSimpleSymbol("int[]");
       }
       if (sym.isDynamic()) {
           errors.add(new TypeError(n.f2.beginLine, "Must be set through static reference"));
       }
       if (!sym.getType().equals("int")) {
           errors.add(new TypeError(n.f2.beginLine, "Int expected"));
       }
       return Symbol.newSimpleSymbol("int[]");
   }

   /**
    * f0 -> "new"
    * f1 -> Identifier()
    * f2 -> "("
    * f3 -> ")"
    */
   // Identifier created must have the matching identifier type.
   // Can only instantiate class objects using AllocationExpression.
   public Symbol visit(AllocationExpression n, SymbolTable argu) {
       if (n.f1.accept(this, argu) == null) {
           errors.add(new TypeError(n.f0.beginLine, "Identifier not defined"));
       }
       Symbol idSym = n.f1.accept(this, argu);
       if (idSym instanceof ClassSymbol) {
           return idSym;
       }
       errors.add(new TypeError(n.f0.beginLine, "Can not instantiate using new"));
       return idSym;
   }

   /**
    * f0 -> "!"
    * f1 -> Expression()
    */
   // BOOLEAN ONLY
   public Symbol visit(NotExpression n, SymbolTable argu) {
       if (!n.f1.accept(this, argu).getType().equals("boolean")) {
           errors.add(new TypeError(n.f0.beginLine, "Boolean expected"));
       }
       return n.f1.accept(this, argu);
   }

   /**
    * f0 -> "("
    * f1 -> Expression()
    * f2 -> ")"
    */
   public Symbol visit(BracketExpression n, SymbolTable argu) {
      return n.f1.accept(this, argu);
   }
}
