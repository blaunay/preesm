package org.ietr.preesm.experiment.model.transformation.properties;

import java.util.Arrays;
import java.util.List;

import com.singularsys.jep.Jep;
import com.singularsys.jep.JepException;
import com.singularsys.jep.ParseException;
import com.singularsys.jep.parser.Node;
import com.singularsys.jep.walkers.TreeAnalyzer;

public class Test {
	
	public static void main(String[] args) {
		
	}

	
	/**
     * Returns true if n1 depends on n2. That is if one of the variables on the
     * rhs of n1 is the lhs variable of n2.
     */
    boolean dependsOn(Node n1,Node n2) throws JepException {
            TreeAnalyzer ta = new TreeAnalyzer(n1.jjtGetChild(1));
            List<String> n1rhs = Arrays.asList(ta.getVariableNames());
            String n2lhs = n2.jjtGetChild(0).getName();
            return n1rhs.contains(n2lhs);
    }

    
    /**
     * Tests use of TreeAnalyzer to work out the order of expressions.
     * @throws ParseException
     * @throws Exception
     */
    public void testExpressionOrdering() throws ParseException, Exception {
            Jep jep = new Jep();
    
            // setup equations to be parsed
            String[] eqns = new String[]{
                    "Var4 = Var1 + Var3",
                    "Var5 = Var4 + 1",
                    "Var3 = 3 * Var2"
                    };
            
            // parse each expression
            Node[] nodes = new Node[eqns.length];
            for(int i=0;i<eqns.length;++i) {
                    nodes[i]=jep.parse(eqns[i]);
            }
            
            // build an array of dependancies
            boolean[][] deps = new boolean[eqns.length][eqns.length];
            for(int i=0;i<eqns.length;++i) {
                    for(int j=0;j<eqns.length;++j) {
                            if(i==j) continue;
                            if(dependsOn(nodes[i],nodes[j])) {
                                    System.out.printf("\"%s\" depends on \"%s\"%n",
                                                    jep.toString(nodes[i]),jep.toString(nodes[j]));
                                    deps[i][j]=true;
                            }
                            else {
                                    System.out.printf("\"%s\" does not depends on \"%s\"%n",
                                                jep.toString(nodes[i]),jep.toString(nodes[j]));
                                    deps[i][j]=false;
                            }
                    }
            }
            System.out.println(Arrays.deepToString(deps));
            

            int[] order = new int[eqns.length];
            Arrays.fill(order, -1);
            boolean[] done = new boolean[eqns.length];
            Arrays.fill(done, false);
            
            for(int loop=0;loop<eqns.length;++loop) {
                    
                // find an expression A for which (B dep A) is false for all other B
                    // exclude those which have already been done
                    for(int i=0;i<eqns.length;++i) {
                            if(done[i]) continue;
                            
                            boolean OK=true; // does anything depend on this
                            for(int j=0;j<eqns.length;++j) {
                                if(done[j]) continue;
                                    if(i==j) continue;
                                    // something depends on this
                                    if(deps[j][i]) OK=false;
                            }
                            if(OK) {
                                    order[loop] = i;
                                    done[i] = true;
                                    break;
                            }
                    }
                    if(order[loop]==-1) {
                            System.out.println("nothing found");
                    }
            }
            System.out.println(Arrays.toString(order));
            System.out.println(Arrays.toString(done));
            
            jep.addVariable("Var1",1.0);
            jep.addVariable("Var2",2.0);
            for(int i=eqns.length-1;i>=0;--i) {
                    jep.println(nodes[order[i]]);
                    jep.evaluate(nodes[order[i]]);
            }
    }
	
}
