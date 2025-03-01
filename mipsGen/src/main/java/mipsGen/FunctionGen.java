package mipsGen;

import java.util.ArrayList;

import cs132.util.ProblemException;
import cs132.vapor.ast.VInstr.Visitor;
import cs132.vapor.ast.VMemRef.Global;
import cs132.vapor.ast.VMemRef.Stack;
import cs132.vapor.ast.*;
import cs132.vapor.ast.VAddr.Label;

public class FunctionGen extends Visitor<ProblemException> {

    private VFunction function;
    private int labelIndex = 0;

    private StringBuilder body = new StringBuilder();

    public ArrayList<String> staticStrs;

    private int framesize;
    public FunctionGen(VFunction function, ArrayList<String> staticStrs) {
        this.staticStrs = staticStrs;
        this.function = function;
        this.framesize = 4*(2 + function.stack.out + function.stack.local);

    }

    public String getFunction() throws ProblemException {
        // Function prologue
        body.append(prologue()+"\n");
        // Function body
        for (VInstr instr : function.body) {
            visit(instr);
        }

        // Function epilogue
        body.append(epilogue());
        
        return body.toString();
    }

    private String prologue() {
        String prologue = ""
            +function.ident+":\n"
            +"  sw $fp -8($sp)\n"
            +"  move $fp $sp\n"
            +"  subu $sp $sp "+framesize+"\n"
            +"  sw $ra -4($fp)";
        return prologue;
    }

    private String epilogue() {
        String epilogue = ""
            +"  lw $ra -4($fp)\n"
            +"  lw $fp -8($fp)\n"
            +"  addu $sp $sp "+framesize+"\n"
            +"  jr $ra\n";
        return epilogue;
    }

    private boolean filterNums(String s) {
        boolean num = false;
        try {
            Integer.parseInt(s);
            num = true;
        } catch (NumberFormatException e) {}
        return num;
    }

    public void visit(VInstr n) throws ProblemException {
        Integer lineNum = n.sourcePos.line;
        while ((labelIndex < function.labels.length)
                &&(lineNum > function.labels[labelIndex].sourcePos.line)){
            body.append(function.labels[labelIndex].ident+":\n");
            labelIndex++;
        }
        n.accept(this);
    }

    // VAddr<VFunction> addr -- Address of function being called
    // VOperand[] args -- List of arguments to pass to function. cannot be registers
    // VVarRef.Local dest -- Variable used to store return value of function. Optional and 
    // may be null, in which case the return value is ignored.
    public void visit(VCall n) throws ProblemException {
        if (n.addr instanceof Label) {
            body.append(""
                    +"  jal "+n.addr.toString().substring(1)+"\n");
        }
        else {
            body.append(""
                    +"  jalr "+n.addr.toString()+"\n");
        }
    }

    // Op op -- operation being performed
    // VOperand[] args -- arguments to the operation
    // VVarRef dest -- variable/register in which to store the result of the operation
    // this is optional and may be null
    public void visit(VBuiltIn n) throws ProblemException {
        String s = getOp(n);
        body.append(s+"\n");
    }

    // VMemRef dest -- memory location being written to
    // VOperand source -- value being written
    public void visit(VMemWrite n) throws ProblemException {
        String source = n.source.toString();
        if (n.dest instanceof Stack) {
            // writing to stack
            Stack ref = (Stack) n.dest;
            if (ref.region.name().equals("Local")) {
                int index = -4*(2+(ref.index+1)); // 2 for ra & fp, then bottom address of local stack index
                if (filterNums(source)) {
                    body.append(""
                            +"  li $t9 "+source+"\n"
                            +"  sw $t9 "+index+"($fp)\n");
                } else if (source.startsWith(":")) {
                    body.append(""
                            +"  la $t9 "+source.substring(1)+"\n"
                            +"  sw $t9 "+index+"($fp)\n");
                } else {
                    body.append(""
                            +"  sw "+source+" "+index+"($fp)\n");
                }
            }
            if (ref.region.name().equals("Out")) {
                int index = 4*ref.index;
                if (filterNums(source)) {
                    body.append(""
                            +"  li $t9 "+source+"\n"
                            +"  sw $t9 "+index+"($sp)\n");
                } else if (source.startsWith(":")) {
                        body.append(""
                                +"  la $t9 "+source.substring(1)+"\n"
                                +"  sw $t9 "+index+"($sp)\n");
                } else {
                        body.append(""
                                +"  sw "+n.source.toString()+" "+index+"($sp)\n");
                }
                
            }
        } else {
            // global/register
            Global ref = (Global) n.dest;
            int offset = ref.byteOffset;
            if (source.startsWith(":")) {
                source = source.substring(1);
                body.append(""
                        +"  la $t9 "+source+"\n"
                        +"  sw $t9 "+offset+"("+ref.base+")\n");
            } else if (filterNums(source)) {
                body.append(""
                        +"  li $t9 "+source+"\n"
                        +"  sw $t9 "+offset+"("+ref.base+")\n");
            } else {
                body.append(""
                        +"  sw "+source+" "+offset+"("+ref.base+")\n");
            }
        }
    }

    // VVarRef dest -- variable/register to store the value into
    // VMemRef source -- memory location being read
    public void visit(VMemRead n) throws ProblemException {
        if (n.source instanceof Stack) {
            Stack ref = (Stack) n.source;
            if (ref.region.name().equals("Local")) {
                int index = -4*(2+(ref.index+1));
                body.append(""
                        +"  lw "+n.dest.toString()+" "+index+"($fp)\n");
            }
            if (ref.region.name().equals("In")) {
                int index = 4*ref.index;
                body.append(""
                        +"  lw "+n.dest.toString()+" "+index+"($fp)\n");
            }
        } else {
            Global ref = (Global) n.source;
            int offset = ref.byteOffset;
            body.append(""
                    +"  lw "+n.dest.toString()+" "+offset+"("+ref.base+")\n");
        }
    }

    // boolean positive -- for 'if' branches, positive is true. 
    // for 'if0' branches, positive is false
    // VOperand value -- value being branched on. For 'if' branches, the branch
    // will be taken if value is non-zero. For 'if0' branches, the branch will be taken if value
    // is zero
    // VLabelRef<VCodeLabel> target -- code label that will be executed next if branch is taken
    public void visit(VBranch n) throws ProblemException {
        String value = n.value.toString();
        if (filterNums(value)) {
            body.append(""
                    +"  li $t9 "+value+"\n");
            value = "$t9";
        }
        if (n.positive) {
            body.append(""
                    +"  bnez "+value+" "+n.target.ident+"\n");
        } else {
            body.append(""
                    +"  beqz "+value+" "+n.target.ident+"\n");
        }
    }

    // VAddr<VCodeLabel> target -- target of the jump. can be direct code label reference
    // or a variable/register
    public void visit(VGoto n) throws ProblemException {
        body.append(""
                +"  j "+n.target.toString().substring(1)+"\n");
    }


    // Only used for assignments of simple operands to registers and local variables.
    // other instr's that use "=" are VMemRead, VMemWrite, VBuiltIn, VCall
    //
    // VVarRef dest -- location being stored to
    // VOperand source -- value being stored
    public void visit(VAssign n) throws ProblemException {
        String source = n.source.toString();
        if (source.startsWith(":")) {
            source = source.substring(1);
            body.append("  la "+n.dest.toString()+" "+source+"\n");
        } else if (filterNums(source)) {
            body.append("  li "+n.dest.toString()+" "+source+"\n");
        } else {
            body.append("  move "+n.dest.toString()+" "+source+"\n");
        }
    }


    // VOperand value -- value being returned. Optional and may be null
    public void visit(VReturn n) throws ProblemException {
    }

    public String getOp(VBuiltIn n) {
        String opStr = n.op.name;
        switch(opStr) {
            case "HeapAlloc":
            case "HeapAllocZ":
                return heapAlloc(n);
            case "PrintIntS":
                return print(n);
            case "Error":
                return error(n);
            case "Lt":
            case "LtS":
                return LtS(n);
            case "Add":
                return add(n);
            case "Sub":
                return sub(n);
            case "MulS":
                return mul(n);
        }
        return "";
    }

    private String heapAlloc(VBuiltIn n) {
        if (filterNums(n.args[0].toString())) {
            return ""
                +"  li $a0 "+n.args[0].toString()+"\n"
                +"  jal _heapAlloc\n"
                +"  move "+n.dest.toString()+" $v0";
        }

        return ""
            +"  move $a0 "+n.args[0].toString()+"\n"
            +"  jal _heapAlloc\n"
            +"  move "+n.dest.toString()+" $v0";
    } 

    private String error(VBuiltIn n) {
        String errMsg = n.args[0].toString()+"\n";
        if (!staticStrs.contains(errMsg)){
            staticStrs.add(errMsg);
        }
        int msgIndex = staticStrs.indexOf(errMsg);
        return ""
            +"  la $a0 _str"+msgIndex+"\n"
            +"  j _error";
    }

    private String print(VBuiltIn n) {
        if (filterNums(n.args[0].toString())) {
            return ""
                +"  li $a0 "+n.args[0].toString()+"\n"
                +"  jal _print";
        }
        return ""
            +"  move $a0 "+n.args[0].toString()+"\n"
            +"  jal _print";
    }

    private String LtS(VBuiltIn n) {
        String arg0 = n.args[0].toString();
        String arg1 = n.args[1].toString();
        if (filterNums(arg0)) {
            if (filterNums(arg1)) {
                return ""
                    +"  li $t9 "+arg0+"\n"
                    +"  slti "+n.dest.toString()+" $t9 "+n.args[1].toString();
            }
            return ""
                +"  li $t9 "+arg0+"\n"
                +"  slt "+n.dest.toString()+" $t9 "+n.args[1].toString();
        }
        
        if (filterNums(arg1)) {
            return ""
                +"  slti "+n.dest.toString()+" "+n.args[0].toString()+" "+n.args[1].toString();
        }
        return ""
            +"  slt "+n.dest.toString()+" "+n.args[0].toString()+" "+n.args[1].toString();

    }

    private String add(VBuiltIn n) {
        String arg0 = n.args[0].toString();
        if (filterNums(arg0)) {
            return ""
                +"  li $t9 "+arg0+"\n"
                +"  add "+n.dest.toString()+" $t9 "+n.args[1].toString(); 
        }
        return ""
            +"  add "+n.dest.toString()+" "+n.args[0].toString()+" "+n.args[1].toString();
    }

    private String sub(VBuiltIn n) {
        String arg0 = n.args[0].toString();
        if (filterNums(arg0)) {
            return ""
                +"  li $t9 "+arg0+"\n"
                +"  sub "+n.dest.toString()+" $t9 "+n.args[1].toString(); 
        }
        return ""
            +"  sub "+n.dest.toString()+" "+n.args[0].toString()+" "+n.args[1].toString();
    }
    
    private String mul(VBuiltIn n) {
        String arg0 = n.args[0].toString();
        if (filterNums(arg0)) {
            return ""
                +"  li $t9 "+arg0+"\n"
                +"  mul "+n.dest.toString()+" $t9 "+n.args[1].toString(); 
        }
        return ""
            +"  mul "+n.dest.toString()+" "+n.args[0].toString()+" "+n.args[1].toString();
    }

}
