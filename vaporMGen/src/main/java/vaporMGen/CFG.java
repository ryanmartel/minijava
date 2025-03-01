package vaporMGen;

import java.util.HashMap;

import cs132.util.ProblemException;
import cs132.vapor.ast.*;
import cs132.vapor.ast.VInstr.Visitor;
import cs132.vapor.ast.VMemRef.Global;

public class CFG extends Visitor<ProblemException> {


    public NodeList nodeList = new NodeList();

    public Node prevNode;
    public Node currNode;

    private VFunction function;
    private HashMap<String, Integer> labelToLine = new HashMap<>();
    private HashMap<Integer, String> lineToLabel = new HashMap<>();

    public CFG(VFunction function) {
        this.function = function;
        for (VCodeLabel label : function.labels) {
            labelToLine.put(label.ident, label.sourcePos.line);
            lineToLabel.put(label.sourcePos.line, label.ident);
        }
    }

    public NodeList getCFG() throws ProblemException {
        for (VInstr instr : function.body) {
            visit(instr);
        }
        return nodeList;
    }

    // True on Integer Literal, otherwise false
    private boolean filterNums(String s) {
        boolean num = false;
        try {
            Integer.parseInt(s);
            num = true;
        } catch (NumberFormatException e) {}
        return num;
    }

    public void visit(VInstr n) throws ProblemException {
        n.accept(this);
    }

    // VAddr<VFunction> addr -- Address of function being called
    // VOperand[] args -- List of arguments to pass to function. cannot be registers
    // VVarRef.Local dest -- Variable used to store return value of function. Optional and 
    // may be null, in which case the return value is ignored.
    public void visit(VCall n) throws ProblemException {
        prevNode = currNode;
        currNode = new Node(n);
        currNode.isCall = true;
        //def
        if (n.dest != null) {
            currNode.addDef(n.dest.toString());
        }
        // read
        currNode.addRead(n.addr.toString());
        for (VOperand arg : n.args) {
            currNode.addRead(arg.toString());
        }
        nodeList.addNode(currNode);
        if (prevNode != null) {
            nodeList.addEdge(prevNode.line, currNode.line);
        }
        
        
    }

    // Op op -- operation being performed
    // VOperand[] args -- arguments to the operation
    // VVarRef dest -- variable/register in which to store the result of the operation
    // this is optional and may be null
    public void visit(VBuiltIn n) throws ProblemException {
        
        // create nodes
        prevNode = currNode;
        currNode = new Node(n);
        //def
        if (n.dest != null) {
            // stores the result of the operation
            currNode.addDef(n.dest.toString());
        }
        //read
        if (!(n.op.name.equals("Error"))) {
            for (VOperand arg : n.args) {
                currNode.addRead(arg.toString());
            }
        }
        // Add to CFG
        nodeList.addNode(currNode);
        if (prevNode != null) {
            nodeList.addEdge(prevNode.line, currNode.line);
        }
        
    }

    // VMemRef dest -- memory location being written to
    // VOperand source -- value being written
    public void visit(VMemWrite n) throws ProblemException {
        
        // create nodes
        prevNode = currNode;
        currNode = new Node(n);
        //read
        if (n.dest instanceof Global) {
            Global ref = (Global) n.dest; 
            currNode.addRead(ref.base.toString());
        }
        // Yea, source needs to be added to read if it is not a function
        currNode.addRead(n.source.toString());
        nodeList.addNode(currNode);
        if (prevNode != null) {
            nodeList.addEdge(prevNode.line, currNode.line);
        }
        
    }

    // VVarRef dest -- variable/register to store the value into
    // VMemRef source -- memory location being read
    public void visit(VMemRead n) throws ProblemException {
        
        // create nodes
        prevNode = currNode;
        currNode = new Node(n);
        // def
        currNode.addDef(n.dest.toString());
        // read
        if (n.source instanceof Global) {
            Global ref = (Global) n.source;
            currNode.addRead(ref.base.toString());
        }
        nodeList.addNode(currNode);
        if (prevNode != null) {
            nodeList.addEdge(prevNode.line, currNode.line);
        }
        
    }

    // boolean positive -- for 'if' branches, positive is true. 
    // for 'if0' branches, positive is false
    // VOperand value -- value being branched on. For 'if' branches, the branch
    // will be taken if value is non-zero. For 'if0' branches, the branch will be taken if value
    // is zero
    // VLabelRef<VCodeLabel> target -- code label that will be executed next if branch is taken
    public void visit(VBranch n) throws ProblemException {
        
        // create nodes
        prevNode = currNode;
        currNode = new Node(n);
        // read
        currNode.addRead(n.value.toString());

        nodeList.addNode(currNode);
        if (prevNode != null) {
            nodeList.addEdge(prevNode.line, currNode.line);
        }
        String label = n.target.ident;
        Integer labelLine = labelToLine.get(label);
        Integer nextInst = labelLine+1;
        while(lineToLabel.containsKey(nextInst)) {
            nextInst++;
        }
        nodeList.addEdge(currNode.line, nextInst);
        
    }

    // VAddr<VCodeLabel> target -- target of the jump. can be direct code label reference
    // or a variable/register
    public void visit(VGoto n) throws ProblemException {
        
        prevNode = currNode;
        currNode = new Node(n);

        nodeList.addNode(currNode);
        if (prevNode != null) {
            nodeList.addEdge(prevNode.line, currNode.line);
        }
        // get label name without :
        String label = n.target.toString().substring(1);
        // next instruction is immediately following label
        Integer labelLine = labelToLine.get(label);
        Integer nextInst = labelLine+1;
        while(lineToLabel.containsKey(nextInst)) {
            nextInst++;
        }
        nodeList.addEdge(currNode.line, nextInst);
        
    }


    // Only used for assignments of simple operands to registers and local variables.
    // other instr's that use "=" are VMemRead, VMemWrite, VBuiltIn, VCall
    //
    // VVarRef dest -- location being stored to
    // VOperand source -- value being stored
    public void visit(VAssign n) throws ProblemException {
        
        prevNode = currNode;
        currNode = new Node(n);
        // def
        currNode.addDef(n.dest.toString());
        // read
        // Assignment could be a number or a variable
        if (!filterNums(n.source.toString())) {
            currNode.addRead(n.source.toString());
        }
        nodeList.addNode(currNode);
        if (prevNode != null) {
            nodeList.addEdge(prevNode.line, currNode.line);
        }
        
    }


    // VOperand value -- value being returned. Optional and may be null
    public void visit(VReturn n) throws ProblemException {
        
        prevNode = currNode;
        currNode = new Node(n);
        // read
        if (n.value != null) {
            currNode.addRead(n.value.toString());
        }
        nodeList.addNode(currNode);
        if (prevNode != null) {
            nodeList.addEdge(prevNode.line, currNode.line);
        }
        
    }

}
