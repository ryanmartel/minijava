package mipsGen;

import cs132.vapor.ast.VInstr.Visitor;
import cs132.vapor.ast.VMemRef.Global;
import cs132.vapor.ast.VMemRef.Stack;
import cs132.vapor.ast.VOperand.Static;
import cs132.util.ProblemException;
import cs132.vapor.ast.*;
import cs132.vapor.ast.VAddr.Label;
import cs132.vapor.ast.VAddr.Var;

public class Printer extends Visitor<ProblemException> {

    private VFunction[] functions;
    private VDataSegment[] dataSegments;
    private int spaces = 0;
    public Printer(VFunction[] functions, VDataSegment[] dataSegments) {
        this.functions = functions;
        this.dataSegments = dataSegments;
    }

    public void printInstructions() throws ProblemException {
        for (VDataSegment dataSegment : dataSegments) {
            System.out.println("DATA SEG ------------ Start");
            spaces += 2;
            System.out.println(s()+"seg name: "+dataSegment.ident);
            System.out.println(s()+"index: "+dataSegment.index);
            System.out.println(s()+"mutable: "+dataSegment.mutable);
            for (Static value : dataSegment.values) {
                System.out.println(s()+"value: "+value.toString());
            }
            spaces -= 2;
            System.out.println("DATA SEG ------------ End");
        }
        for (VFunction function : functions) {
            System.out.println("FUNC "+function.ident+" ----------------- Start");
            System.out.println("LABELS -------- Start");
            for (int i = 0; i < function.labels.length; i++) {
                System.out.println("LABEL: "+function.labels[i].ident+" Line: "+function.labels[i].sourcePos.line);
            }
            System.out.println("LABELS -------- End");
            for (VInstr instr : function.body) {
                visit(instr);
            }
            System.out.println("FUNC "+function.ident+" ----------------- End");
        }
    }
    
    public String s() {
        StringBuilder sb = new StringBuilder();
        sb.append("");
        for (int i = 0; i < spaces; i++) {
            sb.append("\t");
        }
        return sb.toString();
    }

    public void visit(VInstr n) throws ProblemException {
        System.out.println("Visiting VInstr");
        spaces += 2;
        
        n.accept(this);
        spaces -= 2;
        System.out.println("Exiting VInstr");
        
    }

    // Only used for assignments of simple operands to registers and local variables.
    // other instr's that use "=" are VMemRead, VMemWrite, VBuiltIn, VCall
    //
    // VVarRef dest -- location being stored to
    // VOperand source -- value being stored
    public void visit(VAssign n) throws ProblemException {
        System.out.println(s()+"Visiting VAssign");
        spaces += 2;
        
        System.out.println(s()+"Source: "+n.source.toString());
        System.out.println(s()+"Des: "+n.dest.toString());
        spaces -= 2;
        System.out.println(s()+"Exiting VAssign");
        
    }

    // VAddr<VFunction> addr -- Address of function being called
    // VOperand[] args -- List of arguments to pass to function. cannot be registers
    // VVarRef.Local dest -- Variable used to store return value of function. Optional and 
    // may be null, in which case the return value is ignored.
    public void visit(VCall n) throws ProblemException {
        System.out.println(s()+"Visiting VCall");
        spaces += 2;
        
        System.out.println(s()+"addr: "+n.addr.toString());
        if (n.addr instanceof Label) {
            System.out.println(s()+"type: label");
        }
        if (n.addr instanceof Var) {
            System.out.println(s()+"type: var");
        }

        // for VaporM- These will be empty as they can not contain registers!
        for (VOperand arg : n.args) {
            System.out.println(s()+"arg: "+arg.toString());
        }
        if (n.dest != null) {
            System.out.println(s()+"dest: "+n.dest.toString());
        }
        spaces -= 2;
        System.out.println(s()+"Exiting VCall");
        
    }

    // Op op -- operation being performed
    // VOperand[] args -- arguments to the operation
    // VVarRef dest -- variable/register in which to store the result of the operation
    // this is optional and may be null
    public void visit(VBuiltIn n) throws ProblemException {
        System.out.println(s()+"Visiting VBuiltIn");
        spaces += 2;
        
        System.out.println(s()+"OP: "+n.op.name);
        for (VOperand arg : n.args) {
            System.out.println(s()+"arg: "+arg.toString());
        }
        if (n.dest != null) {
            System.out.println(s()+"dest: "+n.dest.toString());
        }
        spaces -= 2;
        System.out.println(s()+"Exiting VBuiltIn");
        
    }

    // VMemRef dest -- memory location being written to
    // VOperand source -- value being written
    public void visit(VMemWrite n) throws ProblemException {
        System.out.println(s()+"Visiting VMemWrite");
        spaces += 2;
        
        if (n.dest instanceof Global) {
            Global ref = (Global) n.dest;
            System.out.println(s()+"dest(global): "+ref.base);
        }
        if (n.dest instanceof Stack) {
            Stack ref = (Stack) n.dest;
            System.out.println(s()+"dest(stack): "+ref.region.name()+" index: "+ref.index);
        }
        System.out.println(s()+"source: "+n.source.toString());
        spaces -= 2;
        System.out.println(s()+"Exiting VMemWrite");
        
    }


    // VVarRef dest -- variable/register to store the value into
    // VMemRef source -- memory location being read
    public void visit(VMemRead n) throws ProblemException {
        System.out.println(s()+"Visiting VMemRead");
        spaces += 2;
        
        System.out.println(s()+"dest: "+n.dest.toString());
        if (n.source instanceof Global) {
            Global ref = (Global) n.source;
            System.out.println(s()+"source(global): "+ref.base);
            System.out.println(s()+"offset: "+ref.byteOffset);
        }
        if (n.source instanceof Stack) {
            Stack ref = (Stack) n.source;
            System.out.println(s()+"source(stack): "+ref.region.name()+" index: "+ref.index);
        }
        spaces -= 2;
        System.out.println(s()+"Exiting VMemRead");
        
    }


    // boolean positive -- for 'if' branches, positive is true. 
    // for 'if0' branches, positive is false
    // VOperand value -- value being branched on. For 'if' branches, the branch
    // will be taken if value is non-zero. For 'if0' branches, the branch will be taken if value
    // is zero
    // VLabelRef<VCodeLabel> target -- code label that will be executed next if branch is taken
    public void visit(VBranch n) throws ProblemException {
        System.out.println(s()+"Visiting VBranch");
        spaces += 2;
        
        System.out.println(s()+"Positive: "+n.positive);
        System.out.println(s()+"value: "+n.value.toString());
        System.out.println(s()+"target: "+n.target.toString());
        spaces -= 2;
        System.out.println(s()+"Exiting VBranch");
        
    }


    // VAddr<VCodeLabel> target -- target of the jump. can be direct code label reference
    // or a variable/register
    public void visit(VGoto n) throws ProblemException {
        System.out.println(s()+"Visiting VGoto");
        spaces += 2;
        
        System.out.println(s()+"target: "+n.target.toString());
        spaces -= 2;
        System.out.println(s()+"Exiting VGoto");
        
    }


    // VOperand value -- value being returned. Optional and may be null
    public void visit(VReturn n) throws ProblemException {
        System.out.println(s()+"Visiting VReturn");
        spaces += 2;
        if (n.value != null) {
            System.out.println(s()+"value: "+n.value.toString());
        }
        spaces -= 2;
        System.out.println(s()+"Exiting VReturn");
        
    }

}
