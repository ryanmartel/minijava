package vaporGen;

import java.util.ArrayList;
import java.util.HashMap;

import visitor.*;
import syntaxtree.*;

import symbolTable.*;

public class VaporVis extends GJDepthFirst<VaporAST, SymbolTable> {

    // Final Vapor Representation 
    private VaporAST vaporRep = VaporAST.newVaporStatement("");

    // Current class/method being used
    private ClassSymbol currClass;
    private MethodSymbol currMethod;

    // Current vapor function being created
    private VaporAST currFunc = null;

    // Counters for generating unique statements and variables
    private TempCounters counter = new TempCounters();

    // If array allocated function needs to be generated
    private boolean arrayAllocated = false;

    // (Java Identifier -> tVar) Reset each method
    private HashMap<String, String> tVars = new HashMap<>();
    // (tVar -> ClassSymbol) Resest each method
    private HashMap<String, ClassSymbol> tVarClassMap = new HashMap<>();
    // (this+_ -> ClassSymbol) For class fields, Reset each class
    private HashMap<String, ClassSymbol> thisClassMap = new HashMap<>();
    // Track current Identifier name for Assignment
    private String currIdentifier = "";

    // For accumulating parameters/arguments for function calls
    private ArrayList<String> callArgs = new ArrayList<>();
    private ArrayList<String> callParams = new ArrayList<>();

    // Reset temp variable information for each method end
    private void resetTemps() {
        counter.resetTVarCounter();
        tVars.clear();
        tVarClassMap.clear();
    }

    private boolean filterNums(String s) {
        boolean num = false;
        try {
            Integer.parseInt(s);
            num = true;
        } catch (NumberFormatException e) {}
        return num;
    }

    /**
     * f0 -> MainClass()
     * f1 -> ( TypeDeclaration() )*
     * f2 -> <EOF>
     */
    public VaporAST visit(Goal n, SymbolTable argu) {
        vaporRep.addBlock(n.f0.accept(this, argu));
        n.f1.accept(this, argu);

        // Array allocation function
        if (arrayAllocated) {
            VaporAST arrayFunc = VaporAST.newVaporStatement("func AllocArray(size)");
            arrayFunc.addChild("bytes = MulS(size 4)");
            arrayFunc.addChild("bytes = Add(bytes 4)");
            arrayFunc.addChild("v = HeapAllocZ(bytes)");
            arrayFunc.addChild("[v] = size");
            arrayFunc.addChild("ret v");

            vaporRep.addBlock(arrayFunc);
        }
        return vaporRep;
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
    public VaporAST visit(MainClass n, SymbolTable argu) {

        VaporAST vapor = VaporAST.newVaporStatement("func Main()");
        currFunc = vapor;
        Symbol sym = argu.get(n.f1.accept(this, argu).toString());
        currClass = (ClassSymbol) sym;
        n.f15.accept(this, argu);

        vapor.addChild("ret");


        resetTemps();
        thisClassMap.clear();
        return vapor;
    }

    /**
     * f0 -> ClassDeclaration()
     *       | ClassExtendsDeclaration()
     */
    public VaporAST visit(TypeDeclaration n, SymbolTable argu) {
        VaporAST block = n.f0.accept(this, argu);
        vaporRep.addBlock(block);
        return null;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */
    public VaporAST visit(ClassDeclaration n, SymbolTable argu) {
        currClass = (ClassSymbol) argu.get(n.f1.accept(this, argu).toString());
        VaporAST vapor = VaporAST.newVaporStatement("const functable_"+currClass.getType());
        for (String method : currClass.methodList) {
            String[] mParts = method.split("\\.");
            vapor.addChild(":"+mParts[0]+"_"+mParts[1]);
        }
        n.f4.accept(this, argu);
        thisClassMap.clear();
        return vapor;
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
    public VaporAST visit(ClassExtendsDeclaration n, SymbolTable argu) {
        currClass = (ClassSymbol) argu.get(n.f1.accept(this, argu).toString());
        VaporAST vapor = VaporAST.newVaporStatement("const functable_"+currClass.getType());
        for (String method : currClass.methodList) {
            String[] mParts = method.split("\\.");
            vapor.addChild(":"+mParts[0]+"_"+mParts[1]);
        }
        n.f6.accept(this, argu);
        thisClassMap.clear();
        return vapor;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    public VaporAST visit(VarDeclaration n, SymbolTable argu) {
        VaporAST vapor=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        return vapor;
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
    public VaporAST visit(MethodDeclaration n, SymbolTable argu) {
        VaporAST f2 = n.f2.accept(this, argu);
        n.f4.accept(this, argu);
        StringBuilder params = new StringBuilder();
        for (String param : callParams) {
            params.append(" "+param);
        }
        VaporAST funcBlock = VaporAST.newVaporStatement("func "+currClass.getType()+"_"+n.f2.f0.toString()+"(this"+params+")");
        callParams.clear();
        currFunc = funcBlock;
        currMethod = (MethodSymbol) argu.getSymbol(f2.toString()+"_M", currClass.getType(), "");
        n.f8.accept(this, argu);
        VaporAST ret = n.f10.accept(this, argu);
        if (ret != null) {
            funcBlock.addChild("ret "+ret);
        } else {
            funcBlock.addChild("ret");
        }
        vaporRep.addBlock(funcBlock);
        resetTemps();
        return null;
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> ( FormalParameterRest() )*
     */
    public VaporAST visit(FormalParameterList n, SymbolTable argu) {
        VaporAST vapor=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        return vapor;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    public VaporAST visit(FormalParameter n, SymbolTable argu) {
        callParams.add(n.f1.accept(this, argu).toString());
        return null;
    }

    /**
     * f0 -> ","
     * f1 -> FormalParameter()
     */
    public VaporAST visit(FormalParameterRest n, SymbolTable argu) {
        n.f1.accept(this, argu);
        return null;
    }

    /**
     * f0 -> ArrayType()
     *       | BooleanType()
     *       | IntegerType()
     *       | Identifier()
     */
    public VaporAST visit(Type n, SymbolTable argu) {
        VaporAST vapor=null;
        n.f0.accept(this, argu);
        return vapor;
    }

    /**
     * f0 -> "int"
     * f1 -> "["
     * f2 -> "]"
     */
    public VaporAST visit(ArrayType n, SymbolTable argu) {
        VaporAST vapor=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        return vapor;
    }

    /**
     * f0 -> "boolean"
     */
    public VaporAST visit(BooleanType n, SymbolTable argu) {
        VaporAST vapor=null;
        n.f0.accept(this, argu);
        return vapor;
    }

    /**
     * f0 -> "int"
     */
    public VaporAST visit(IntegerType n, SymbolTable argu) {
        VaporAST vapor=null;
        n.f0.accept(this, argu);
        return vapor;
    }

    /**
     * f0 -> Block()
     *       | AssignmentStatement()
     *       | ArrayAssignmentStatement()
     *       | IfStatement()
     *       | WhileStatement()
     *       | PrintStatement()
     */
    public VaporAST visit(Statement n, SymbolTable argu) {
        n.f0.accept(this, argu);
        return null;
    }

    /**
     * f0 -> "{"
     * f1 -> ( Statement() )*
     * f2 -> "}"
     */
    public VaporAST visit(Block n, SymbolTable argu) {
        VaporAST vapor=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        return vapor;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     */
    public VaporAST visit(AssignmentStatement n, SymbolTable argu) {
        currIdentifier = n.f0.accept(this, argu).toString();
        VaporAST assignment = VaporAST.newVaporStatement(currIdentifier+" = "+n.f2.accept(this, argu));
        currIdentifier = "";
        currFunc.addChild(assignment);
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
    public VaporAST visit(ArrayAssignmentStatement n, SymbolTable argu) {
        VaporAST f0 = n.f0.accept(this, argu);
        VaporAST f2 = n.f2.accept(this, argu);

        String aPtr = counter.getTemp();
        currFunc.addChild(aPtr+" = "+f0);

        // Null check
        int nCount = counter.getNull();
        VaporAST nullCheck = VaporAST.newVaporStatement("if "+aPtr+" goto :null"+nCount);
        nullCheck.addChild("Error(\"null pointer\")");
        currFunc.addChild(nullCheck);
        currFunc.addChild("null"+nCount+":");

        String itemPtr = counter.getTemp();
        currFunc.addChild(itemPtr+" = ["+aPtr+"]");
        currFunc.addChild(itemPtr+" = Lt("+f2+" "+itemPtr+")");

        // Bounds check
        int bCount = counter.getBounds();
        VaporAST boundsCheck = VaporAST.newVaporStatement("if "+itemPtr+" goto :bounds"+bCount);
        boundsCheck.addChild("Error(\"array index out of bounds\")");
        currFunc.addChild(boundsCheck);
        currFunc.addChild("bounds"+bCount+":");

        currFunc.addChild(itemPtr+" = MulS("+f2+" 4)");
        currFunc.addChild(itemPtr+" = Add("+itemPtr+" "+aPtr+")");
        VaporAST f5 = n.f5.accept(this, argu);
        currFunc.addChild("["+itemPtr+"+4] = "+f5);
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
    public VaporAST visit(IfStatement n, SymbolTable argu) {
        int ifNum = counter.getIf();
        VaporAST ifStart = VaporAST.newVaporStatement("if0 "+n.f2.accept(this, argu)+" goto :if"+ifNum+"_else");
        currFunc.addChild(ifStart);

        n.f4.accept(this, argu);

        currFunc.addChild("goto :if"+ifNum+"_end");

        VaporAST ifElse = VaporAST.newVaporStatement("if"+ifNum+"_else:");
        currFunc.addChild(ifElse);
        n.f6.accept(this, argu);

        VaporAST ifEnd = VaporAST.newVaporStatement("if"+ifNum+"_end:");
        currFunc.addChild(ifEnd);
        return null;
    }

    /**
     * f0 -> "while"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     */
    public VaporAST visit(WhileStatement n, SymbolTable argu) {
        int whileNum = counter.getWhile();
        // WHILE TOP
        currFunc.addChild("while"+whileNum+"_top:");
        VaporAST top = n.f2.accept(this, argu);
        currFunc.addChild("if0 "+top+ " goto :while"+whileNum+"_end");
        // WHILE BODY
        n.f4.accept(this, argu);
        currFunc.addChild("goto :while"+whileNum+"_top");
        // WHILE END
        currFunc.addChild("while"+whileNum+"_end:");
        return null;
    }

    /**
     * f0 -> "System.out.println"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> ";"
     */
    public VaporAST visit(PrintStatement n, SymbolTable argu) {
        VaporAST vapor = VaporAST.newVaporStatement("PrintIntS("+n.f2.accept(this, argu)+")");
        currFunc.addChild(vapor);
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
    public VaporAST visit(Expression n, SymbolTable argu) {
        VaporAST exp = n.f0.accept(this, argu);
        if (exp.isPartial()) {
            return exp;
        }
        currFunc.addChild(exp);
        return VaporAST.newVaporPartial(exp.toString());
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "&&"
     * f2 -> PrimaryExpression()
     */
    public VaporAST visit(AndExpression n, SymbolTable argu) {
        int aCount = counter.getAnd();
        VaporAST f0 = n.f0.accept(this, argu);
        currFunc.addChild("if0 "+f0+" goto :ss"+aCount+"_else");
        VaporAST f2 = n.f2.accept(this, argu);
        currFunc.addChild("goto :ss"+aCount+"_end");
        currFunc.addChild("ss"+aCount+"_else:");
        if (!filterNums(f2.toString())) {
            currFunc.addChild(f2+" = 0");
            currFunc.addChild("ss"+aCount+"_end:");
            return f2;
        } else {
            currFunc.addChild("ss"+aCount+"_end:");
            if (Integer.parseInt(f2.toString()) == 0) {
                return VaporAST.newVaporPartial("0");
            } else {
                return VaporAST.newVaporPartial("1");
            }
        }


    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */
    public VaporAST visit(CompareExpression n, SymbolTable argu) {
        VaporAST f0 = n.f0.accept(this, argu);
        VaporAST f2 = n.f2.accept(this, argu);
        String var = counter.getTemp();
        currFunc.addChild(var+" = LtS("+f0 + " "+f2+")");
        return VaporAST.newVaporPartial(var);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    public VaporAST visit(PlusExpression n, SymbolTable argu) {
        VaporAST f0 = n.f0.accept(this, argu);
        VaporAST f2 = n.f2.accept(this, argu);
        String var = counter.getTemp();
        currFunc.addChild(var+" = Add("+f0 + " "+f2+")");
        return VaporAST.newVaporPartial(var);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
    public VaporAST visit(MinusExpression n, SymbolTable argu) {
        VaporAST f0 = n.f0.accept(this, argu);
        VaporAST f2 = n.f2.accept(this, argu);
        String var = counter.getTemp();
        currFunc.addChild(var+" = Sub("+f0+" "+f2+")");
        return VaporAST.newVaporPartial(var);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
    public VaporAST visit(TimesExpression n, SymbolTable argu) {
        VaporAST f0 = n.f0.accept(this, argu);
        VaporAST f2 = n.f2.accept(this, argu);
        String var = counter.getTemp();
        currFunc.addChild(var+" = MulS("+f0+" "+f2+")");
        return VaporAST.newVaporPartial(var);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */
    public VaporAST visit(ArrayLookup n, SymbolTable argu) {
        VaporAST f0 = n.f0.accept(this, argu);
        VaporAST f2 = n.f2.accept(this, argu);
        int nCount = counter.getNull();
        VaporAST nullCheck = VaporAST.newVaporStatement("if "+f0+" goto :null"+nCount);
        nullCheck.addChild("Error(\"null pointer\")");
        currFunc.addChild(nullCheck);
        currFunc.addChild("null"+nCount+":");
        String aPtr = counter.getTemp();
        currFunc.addChild(aPtr+" = ["+f0+"]");

        // Check entry in array bounds
        currFunc.addChild(aPtr+" = LtS("+f2+" "+aPtr+")");
        int bCount = counter.getBounds();
        VaporAST boundsCheck = VaporAST.newVaporStatement("if "+aPtr+" goto :bounds"+bCount);
        boundsCheck.addChild("Error(\"array index out of bounds\")");
        currFunc.addChild(boundsCheck);
        currFunc.addChild("bounds"+bCount+":");

        currFunc.addChild(aPtr+" = MulS("+f2+" 4)");
        currFunc.addChild(aPtr+" = Add("+aPtr+" "+f0+")");

        String itemPtr = counter.getTemp();
        currFunc.addChild(itemPtr+" = ["+aPtr+"+4]");
        return VaporAST.newVaporPartial(itemPtr);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    public VaporAST visit(ArrayLength n, SymbolTable argu) {
        VaporAST f0 = n.f0.accept(this, argu);

        int nCount = counter.getNull();
        VaporAST nullCheck = VaporAST.newVaporStatement("if "+f0+" goto :null"+nCount);
        nullCheck.addChild("Error(\"null pointer\")");
        currFunc.addChild(nullCheck);
        currFunc.addChild("null"+nCount+":");
        String aPtr = counter.getTemp();
        currFunc.addChild(aPtr+" = ["+f0+"]");
        return VaporAST.newVaporPartial(aPtr);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )?
     * f5 -> ")"
     */
    public VaporAST visit(MessageSend n, SymbolTable argu) {
        VaporAST f0 = n.f0.accept(this, argu);
        // null check
        if (!f0.wasChecked()){
            int nCount = counter.getNull();
            VaporAST nullCheck = VaporAST.newVaporStatement("if "+f0+" goto :null"+nCount);
            nullCheck.addChild("Error(\"null pointer\")");
            currFunc.addChild(nullCheck);
            currFunc.addChild("null"+nCount+":");
        }

        String callerIdentifier = f0.toString();
        ClassSymbol callingClass;

        if (callerIdentifier.equals("this")) {
            callingClass = currClass;
        } else {
            callingClass = tVarClassMap.get(callerIdentifier);
            if (callingClass == null) {
                callingClass = thisClassMap.get(callerIdentifier);
            }
        } 
        // Parameter check
        if (callingClass == null) {
            Symbol sym = argu.getSymbol(callerIdentifier, currClass, currMethod);
            callingClass = (ClassSymbol) argu.get(sym.getType());
        }
        // function table
        String tFTable = counter.getTemp();
        currFunc.addChild(tFTable+" = ["+callerIdentifier+"]");
        // method pointer
        int methodIndex = callingClass.methodIndex(n.f2.accept(this, argu).toString());
        int methodOffset = (methodIndex*4);
        currFunc.addChild(tFTable+" = ["+tFTable+"+"+methodOffset+"]");

        String tCall = counter.getTemp();
        // gather arguments
        n.f4.accept(this, argu);
        StringBuilder argsRestBuilder = new StringBuilder();
        for (String arg : callArgs) {
            argsRestBuilder.append(" "+arg);
        }
        // reset arg collector
        callArgs.clear();
        // generate call
        currFunc.addChild(tCall+" = call "+tFTable+"("+callerIdentifier+argsRestBuilder.toString()+")");

        MethodSymbol calledM = (MethodSymbol) argu.getSymbol(n.f2.accept(this, argu).toString()+"_M", callingClass.getType(), "");
        String retType = calledM.getReturnType();
        if (argu.get(retType) instanceof ClassSymbol) {
            tVarClassMap.put(tCall, (ClassSymbol) argu.get(retType));
        }
        return VaporAST.newVaporPartial(tCall);
    }

    /**
     * f0 -> Expression()
     * f1 -> ( ExpressionRest() )*
     */
    public VaporAST visit(ExpressionList n, SymbolTable argu) {
        callArgs.add(n.f0.accept(this, argu).toString());
        n.f1.accept(this, argu);
        return null;
    }

    /**
     * f0 -> ","
     * f1 -> Expression()
     */
    public VaporAST visit(ExpressionRest n, SymbolTable argu) {
        callArgs.add(n.f1.accept(this, argu).toString());
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
    public VaporAST visit(PrimaryExpression n, SymbolTable argu) {
        VaporAST pEx = n.f0.accept(this, argu);
        if (pEx.toString().startsWith("[this+")) {
            String fieldVar = counter.getTemp();
            tVarClassMap.put(fieldVar, thisClassMap.get(pEx.toString()));
            currFunc.addChild(fieldVar+" = "+pEx);
            return VaporAST.newVaporPartial(fieldVar);
        }
        return pEx;
    }

    /**
     * f0 -> <INTEGER_LITERAL>
     */
    public VaporAST visit(IntegerLiteral n, SymbolTable argu) {
        return VaporAST.newVaporPartial(n.f0.toString());
    }

    /**
     * f0 -> "true"
     */
    public VaporAST visit(TrueLiteral n, SymbolTable argu) {
        return VaporAST.newVaporPartial("1");
    }

    /**
     * f0 -> "false"
     */
    public VaporAST visit(FalseLiteral n, SymbolTable argu) {
        return VaporAST.newVaporPartial("0");
    }

    /**
     * f0 -> <IDENTIFIER>
     */
    public VaporAST visit(Identifier n, SymbolTable argu) {
        String f0 = n.f0.toString();

        if (tVars.containsKey(f0)) {
            return VaporAST.newVaporPartial(tVars.get(f0));
        }
        if (currMethod != null && currMethod.localVars.contains(f0)) {
            VaporAST vapor = VaporAST.newVaporPartial(f0);
            return vapor;
        }
        if (currClass != null && currClass.hasField(f0)) {
            int fieldOffset = (currClass.fieldIndex(f0)*4)+4;
            String field = "[this+"+fieldOffset+"]";
            Symbol sym = argu.getSymbol(f0, currClass.getType(), currMethod.getType());
            if (argu.get(sym.getType()) instanceof ClassSymbol) {
                thisClassMap.put(field, (ClassSymbol) argu.get(sym.getType()));
            }
            return VaporAST.newVaporPartial("[this+"+fieldOffset+"]");
        }
        return VaporAST.newVaporPartial(f0);
    }

    /**
     * f0 -> "this"
     */
    public VaporAST visit(ThisExpression n, SymbolTable argu) {
        if (currIdentifier != "") {
            tVarClassMap.put(currIdentifier, currClass);
        }
        return VaporAST.newVaporPartial("this");
    }

    /**
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
    public VaporAST visit(ArrayAllocationExpression n, SymbolTable argu) {
        String var = counter.getTemp();
        currFunc.addChild(var+" = call :AllocArray("+n.f3.accept(this, argu)+")");
        arrayAllocated = true;
        return VaporAST.newVaporPartial(var);
    }

    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    public VaporAST visit(AllocationExpression n, SymbolTable argu) {
        ClassSymbol allocClass = (ClassSymbol) argu.get(n.f1.accept(this, argu).toString());
        int allocSize = (allocClass.fieldList.size()*4)+4;

        String var = counter.getTemp();
        tVarClassMap.put(var, allocClass);
        if (currIdentifier != "") {
            tVars.put(currIdentifier, var);
        }

        int nullError = counter.getNull();

        currFunc.addChild(var+" = HeapAllocZ("+allocSize+")");
        currFunc.addChild("["+var+"] = :functable_"+allocClass.getType());
        VaporAST errIF = VaporAST.newVaporStatement("if "+var+" goto :null"+nullError);
        errIF.addChild("Error(\"null pointer\")");
        currFunc.addChild(errIF);
        currFunc.addChild("null"+nullError+":");
        VaporAST ret = VaporAST.newVaporPartial(var);
        ret.setChecked();
        return ret;
    }

    /**
     * f0 -> "!"
     * f1 -> Expression()
     */
    public VaporAST visit(NotExpression n, SymbolTable argu) {
        VaporAST ex = n.f1.accept(this, argu);
        String var = counter.getTemp();
        currFunc.addChild(var+" = Sub(1 "+ex+")");
        return VaporAST.newVaporPartial(var);
    }

    /**
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     */
    public VaporAST visit(BracketExpression n, SymbolTable argu) {
        return n.f1.accept(this, argu);
    }


}
