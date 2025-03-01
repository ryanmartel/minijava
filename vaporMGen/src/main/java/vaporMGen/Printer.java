package vaporMGen;

import cs132.util.ProblemException;
import cs132.vapor.ast.*;
import cs132.vapor.ast.VInstr.VisitorPR;
import cs132.vapor.ast.VMemRef.Global;

public class Printer extends VisitorPR<Integer, Integer, ProblemException>{
    private VFunction[] functions;
    private boolean debug;

    public Printer(VFunction[] functions) {
        this.functions = functions;
        debug = true;
    }

    public void printInstructions() throws ProblemException {
        for (VFunction function : functions) {
            System.out.println("FUNC: "+function.index+"-------------------------"+function.ident);
            for (VInstr  intr: function.body) {
                visit(0, intr);
            }
        }
    }

    public void printVars() {
        for (VFunction function : functions) {
            System.out.println(function.ident);
            System.out.println("FUNC: "+function.index);
            for (String var : function.vars) {
                System.out.println(var);
            }
        }
    }

    public Integer visit(Integer p, VInstr n) throws ProblemException {
        if (debug) {
            System.out.println("Visiting VInstr");
        }
        Integer _ret = null;
        n.accept(p, this);
        if (debug) {
            System.out.println("Exiting VInstr");
        }
        return _ret;
    }

    // VAddr<VFunction> addr -- Address of function being called
    // VOperand[] args -- List of arguments to pass to function. cannot be registers
    // VVarRef.Local dest -- Variable used to store return value of function. Optional and 
    // may be null, in which case the return value is ignored.
    public Integer visit(Integer p, VCall n) throws ProblemException {
        if (debug) {
            System.out.println("Visiting VCall");
        }
        Integer _ret = null;
        System.out.println("\t Function Call: Addr => "+n.addr.toString());
        for (VOperand arg : n.args) {
            System.out.println("\t arg: "+arg.toString());
        }
        if (n.dest != null) {
            System.out.println("\t Dest: "+n.dest.toString());
        }
        if (debug) {
            System.out.println("Exiting VCall");
        }
        return _ret;
    }

    // Op op -- operation being performed
    // VOperand[] args -- arguments to the operation
    // VVarRef dest -- variable/register in which to store the result of the operation
    // this is optional and may be null
    public Integer visit(Integer p, VBuiltIn n) throws ProblemException {
        if (debug) {
            System.out.println("Visiting VBuiltIn");
            System.out.println("\tOperation called: "+n.op.name);
            for (VOperand arg : n.args) {
                System.out.println("\t\t"+arg.toString());
            }
            if (n.dest != null) {
                System.out.println("\tSoring to: "+n.dest.toString());
            }
        }
        Integer _ret = null;
        
        if (debug) {
            System.out.println("Exiting VBuiltIn");
        }
        return _ret;
    }

    // VMemRef dest -- memory location being written to
    // VOperand source -- value being written
    public Integer visit(Integer p, VMemWrite n) throws ProblemException {
        if (debug) {
            System.out.println("Visiting VMemWrite");
        }
        Integer _ret = null;
        System.out.println("\t MemWrite, dest: "+n.dest.getClass().getTypeName());
        Global ref = (Global) n.dest;
        System.out.println("\t MemWrite dest name: "+ref.base.toString());
        System.out.println("\t MemWrite, source: "+n.source.toString());
        if (debug) {
            System.out.println("Exiting VMemWrite");
        }
        return _ret;
    }

    // VVarRef dest -- variable/register to store the value into
    // VMemRef source -- memory location being read
    public Integer visit(Integer p, VMemRead n) throws ProblemException {
        if (debug) {
            System.out.println("Visiting VMemRead");
        }
        Integer _ret = null;
        System.out.println("\t MemRead, dest: "+n.dest.toString());
        System.out.println("\t MemRead, source: "+n.source.getClass().getTypeName());
        if (debug) {
            System.out.println("Exiting VMemRead");
        }
        return _ret;
    }

    // boolean positive -- for 'if' branches, positive is true. 
    // for 'if0' branches, positive is false
    // VOperand value -- value being branched on. For 'if' branches, the branch
    // will be taken if value is non-zero. For 'if0' branches, the branch will be taken if value
    // is zero
    // VLabelRef<VCodeLabel> target -- code label that will be executed next if branch is taken
    public Integer visit(Integer p, VBranch n) throws ProblemException {
        if (debug) {
            System.out.println("Visiting VBranch");
        }
        Integer _ret = null;
        System.out.println("\tPositive Status: "+n.positive);
        System.out.println("\tOperand: "+n.value.toString());
        System.out.println("\tTarget: "+n.target.toString());
        System.out.println("\tTarget ident: "+n.target.ident);
        if (debug) {
            System.out.println("Exiting VBranch");
        }
        return _ret;
    }

    // VAddr<VCodeLabel> target -- target of the jump. can be direct code label reference
    // or a variable/register
    public Integer visit(Integer p, VGoto n) throws ProblemException {
        if (debug) {
            System.out.println("Visiting VGoto");
        }
        Integer _ret = null;
        System.out.println("\t GOTO: "+n.target.toString());
        if (debug) {
            System.out.println("Exiting VGoto");
        }
        return _ret;
    }


    // Only used for assignments of simple operands to registers and local variables.
    // other instr's that use "=" are VMemRead, VMemWrite, VBuiltIn, VCall
    //
    // VVarRef dest -- location being stored to
    // VOperand source -- value being stored
    public Integer visit(Integer p, VAssign n) throws ProblemException {
        if (debug) {
            System.out.println("Visiting VAssign");
        }
        Integer _ret = null;
        System.out.println("\t Storage loaction: "+n.dest.toString());
        System.out.println("\t Value being stored: "+n.source.toString());
        if (debug) {
            System.out.println("Exiting VAssign");
        }
        return _ret;
    }


    // VOperand value -- value being returned. Optional and may be null
    public Integer visit(Integer p, VReturn n) throws ProblemException {
        if (debug) {
            System.out.println("Visiting VReturn");
        }
        Integer _ret = null;
        if (n.value != null) {
            System.out.println("\t Returning val: "+n.value.toString());
        }
        if (debug) {
            System.out.println("Exiting VReturn");
        }
        return _ret;
    }
}
