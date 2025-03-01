package vaporMGen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cs132.util.ProblemException;
import cs132.vapor.ast.VInstr.Visitor;
import cs132.vapor.ast.VMemRef.Global;
import vaporMGen.LinearScanAlloc.SpillPoints;
import cs132.vapor.ast.*;

public class ImplAllocations extends Visitor<ProblemException> {

    private NodeList cfg;

    // Register mapping varName -> register assigned.
    private HashMap<String, String> registerMap;
    // Contains variables that spill varName -> location where spill occurs
    private HashMap<SpillPoints, String> spillMap;
    // List of variables declared but never read. These do not need to be assigned to registers
    // or output in vaporm codeGen
    private ArrayList<String> unusedVars;

    // Number of callee-reserved registers for this function. Max of 8
    private int sMax;
    private VFunction function;

    // Output
    public String bodyStr;

    public ImplAllocations(NodeList cfg, int sMax, VFunction function, LinearScanAlloc linearScanAlloc) throws ProblemException{
        this.cfg = cfg;
        this.sMax = sMax;
        this.local = sMax;
        this.registerMap = linearScanAlloc.registerMap;
        this.spillMap = linearScanAlloc.spillMap;
        this.unusedVars = linearScanAlloc.unusedVars;
        this.function = function;
        fillParameters();
        for (VInstr instr: function.body) {
            visit(instr);
        }
        composeFunction();
    }

    // Fill the $a registers from the function definition. 
    // If there are more than 4 input parameters then the 
    // 'in' stack array is used
    private void fillParameters() {
        boolean set = false;
        StringBuilder params = new StringBuilder();
        for (int i = 0; i < function.params.length; i++) {
            String ident;
            ident = resolveVariable(function.params[i].ident);
            if (!ident.equals("")) {
                if (i < 4) {
                    params.append("  "+resolveVariable(function.params[i].ident)+" = $a"+i);
                } else {
                    in++;
                    params.append("  "+resolveVariable(function.params[i].ident)+" = in["+(i-4)+"]");
                }
                if (i != function.params.length-1) {
                    params.append("\n");
                }
                set = true;
            }
        }
        // IF there are any parameters, add this block to output
        if (set) {
            body.append(params.toString()+"\n");
        }
    }

    StringBuilder body = new StringBuilder();

    // Meta Trackers
    private Integer lineNum = 0;
    private int in = 0;
    private int out = 0;
    private int local = 0;


    // Get Assigned register names for the variable 
    // that need them. Literals are passed as is
    private String resolveVariable(String var) {
        // IS THIS JUST AN Integer LITERAL?
        try {
            Integer.parseInt(var);
            return var;
        } catch(NumberFormatException e){}
        // Not a literal
        // does it get spilled?
        String spill = spillRegMap.get(var);
        if (spill != null) {
            return spill;
        }
        // Is it assigned a register?
        String reg = registerMap.get(var);
        if (reg != null) {
            return "$"+reg;
        }
        // Is it just unused
        if (unusedVars.contains(var)) {
            return "";
        }
        // Must be literal string
        return var;
    }

    // Build the final representation of the vapor-m function
    private void composeFunction() {
        StringBuilder func = new StringBuilder();
        func.append("func "+function.ident+" [in "+in+", out "+out+", local "+local+"]\n");
        // Backup callee saved registers
        for (int i = 0; i < sMax; i++) {
            func.append("  local["+i+"] = $s"+i+"\n");
        }

        func.append(body.toString());

        // restore callee saved registers
        for (int i = 0; i < sMax; i++) {
            func.append("  $s"+i+" = local["+i+"]\n");
        }
        func.append("  ret");
        bodyStr = func.toString();

    }

    // Current number of values occupying spill registers
    private int currSpilled = 0;
    // Total number of variables spilled at once
    private int spilledVars = 0;
    // Registers made available to handle spills/loads
    private String[] spillRegs = {"$t8", "$v1", "$v0"};
    private ArrayList<String> spilledRead = new ArrayList<>();
    private ArrayList<String> spilledDef = new ArrayList<>();
    // varName -> spill register
    private HashMap<String, String> spillRegMap = new HashMap<>();
    // varName -> local[x]
    private HashMap<String, String> spillStackMap = new HashMap<>();
    // insert labels in appropriate places
    private int labelIndex = 0;
    public void visit(VInstr n) throws ProblemException {
        
        lineNum = n.sourcePos.line;
        if (labelIndex < function.labels.length) {
            while ((labelIndex < function.labels.length)&&(lineNum > function.labels[labelIndex].sourcePos.line)) {
                body.append(function.labels[labelIndex].ident+":\n");
                labelIndex++;
            }
        }
        // Check for spilled variable. if read spilled variable,
        // pull from stack to temp register, update mapping for spilled variable to 
        // register.
        for (Map.Entry<SpillPoints, String> entry : spillMap.entrySet()) {
            if (entry.getKey().spillPoint == lineNum) {
                // variable spills here
                String localStr = "local["+(sMax+spilledVars)+"]";
                spillStackMap.put(entry.getValue(), localStr);
                spilledVars++;
                local++;
                String assignedVar = registerMap.get(entry.getValue());
                if (assignedVar != null) {
                    body.append(localStr+" = "+resolveVariable(entry.getValue())+"\n");
                }
            }
        }
        Node node = cfg.get(lineNum);
         // check reads at this line
        for (String s : node.read) {
            if (spillStackMap.containsKey(s)) {
                spilledRead.add(s);
            }
        }
        // check defs at this line
        for (String s : node.def) {
            if (spillStackMap.containsKey(s)) {
                spilledDef.add(s);
            }
        }
        for (String spill : spilledRead) {
            // get local[x] assignment of variable
            String stackVar = spillStackMap.get(spill);
            spillRegMap.put(spill, spillRegs[currSpilled]);
            currSpilled++;
            body.append("  "+spillRegMap.get(spill)+" = "+stackVar+"\n");
        }
        for (String spill : spilledDef) {
            // get local[x] assignment of variable
            if (!spilledRead.contains(spill )) {
                spillRegMap.put(spill, spillRegs[currSpilled]);
                currSpilled++;
            }
        }
        n.accept(this);
        // After line where spilled variable was used,
        // restore spilled variable back to stack adn remove mapping to register
        for (String spill : spilledDef) {
            String stackVar = spillStackMap.get(spill);
            body.append("  "+stackVar+" = "+spillRegMap.get(spill)+"\n");
            spillRegMap.remove(spill);
            currSpilled--;
        }
        for (String spill : spilledRead) {
            spillRegMap.remove(spill);
            currSpilled--;
        }
        spilledRead.clear();
        spilledDef.clear();
        
    }

    // VAddr<VFunction> addr -- Address of function being called
    // VOperand[] args -- List of arguments to pass to function. cannot be registers
    // VVarRef.Local dest -- Variable used to store return value of function. Optional and 
    // may be null, in which case the return value is ignored.
    public void visit(VCall n) throws ProblemException {
        
        int localOut = 0;
        StringBuilder call = new StringBuilder();
        // Collect arguments
        String[] args = new String[n.args.length];
        for (int i = 0; i < n.args.length; i++) {
            args[i] = n.args[i].toString();
        }
        // Setup arguments
        for (int i = 0; i < args.length; i++) {
            String arg;
            if (i < 4) {
                // fits in '$a' registers
                arg = "  $a"+i+" = "+resolveVariable(args[i])+"\n";
            } else {
                // throw on stack
                localOut++;
                arg = "  out["+(i-4)+"] = "+resolveVariable(args[i])+"\n";
            }
            call.append(arg);
        }
        call.append("  call "+resolveVariable(n.addr.toString())+"\n");
        // Is there a storing variable, is it used?
        if ((n.dest != null)&&(!(resolveVariable(n.dest.toString()).equals("")))) {
            call.append("  "+resolveVariable(n.dest.toString())+" = $v0"+"\n");
        }
        if (localOut > out) {
            out = localOut;
        }
        body.append(call.toString());
        
    }

    // Op op -- operation being performed
    // VOperand[] args -- arguments to the operation
    // VVarRef dest -- variable/register in which to store the result of the operation
    // this is optional and may be null
    public void visit(VBuiltIn n) throws ProblemException {
        
        StringBuilder builtInStr = new StringBuilder();
        String[] argCollector = new String[n.args.length];
        for (int i = 0; i < n.args.length; i++) {
            argCollector[i] = resolveVariable(n.args[i].toString());
        }
        // Is there a destination. Is the destination an unused variable?
        if ((n.dest != null)&&(!(resolveVariable(n.dest.toString()).equals("")))) {
            String dest = resolveVariable(n.dest.toString());
            String s = ("  "+dest+" = "+n.op.name+"(");
            builtInStr.append(s);
        } else {
            builtInStr.append("  "+n.op.name+"(");
        }
        for (int i = 0; i < argCollector.length; i++) {
            builtInStr.append(argCollector[i]);
            if (i != argCollector.length-1) {
                builtInStr.append(" ");
            }
        }
        builtInStr.append(")");
        body.append(builtInStr.toString()+"\n");
        
    }

    // VMemRef dest -- memory location being written to
    // VOperand source -- value being written
    public void visit(VMemWrite n) throws ProblemException {
        
        Global ref = (Global) n.dest;
        String dest = resolveVariable(ref.base.toString());
        if (!dest.equals("")){
            String offset = "";
            if (ref.byteOffset > 0) {
                offset = "+"+ref.byteOffset;
            }
            String s = ("  ["+dest+offset+"] = "+
                    resolveVariable(n.source.toString()));
            body.append(s+"\n");
        }
        
    }

    // VVarRef dest -- variable/register to store the value into
    // VMemRef source -- memory location being read
    public void visit(VMemRead n) throws ProblemException {
        
        String dest = resolveVariable(n.dest.toString());
        if (!dest.equals("")) {
            Global ref = (Global) n.source;
            String offset = "";
            if (ref.byteOffset > 0) {
                offset = "+"+ref.byteOffset;
            }
            String s = ("  "+dest+" = ["+resolveVariable(ref.base.toString())+offset+"]");
            body.append(s+"\n");
        }
        
    }

    // boolean positive -- for 'if' branches, positive is true. 
    // for 'if0' branches, positive is false
    // VOperand value -- value being branched on. For 'if' branches, the branch
    // will be taken if value is non-zero. For 'if0' branches, the branch will be taken if value
    // is zero
    // VLabelRef<VCodeLabel> target -- code label that will be executed next if branch is taken
    public void visit(VBranch n) throws ProblemException {
        
        StringBuilder branch = new StringBuilder();
        if (n.positive) {
            branch.append("  if ");
        } else {
            branch.append("  if0 ");
        } 
        branch.append(resolveVariable(n.value.toString())+" goto "+n.target.toString());
        String s = branch.toString();
        body.append(s+"\n");
        
    }

    // VAddr<VCodeLabel> target -- target of the jump. can be direct code label reference
    // or a variable/register
    public void visit(VGoto n) throws ProblemException {
        
        String s = "  goto "+n.target.toString();
        // System.out.println(s);
        body.append(s+"\n");
        
    }


    // Only used for assignments of simple operands to registers and local variables.
    // other instr's that use "=" are VMemRead, VMemWrite, VBuiltIn, VCall
    //
    // VVarRef dest -- location being stored to
    // VOperand source -- value being stored
    public void visit(VAssign n) throws ProblemException {
        
        String dest = resolveVariable(n.dest.toString());
        if (!dest.equals("")) {
            String s = ("  "+dest+" = "+resolveVariable(n.source.toString()));
            body.append(s+"\n");
        }
        

    }


    // VOperand value -- value being returned. Optional and may be null
    public void visit(VReturn n) throws ProblemException {
        
        if (n.value != null) {
            String s = "  $v0 = "+resolveVariable(n.value.toString())+"\n";
            body.append(s);
        }
        
    }
}
