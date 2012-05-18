package org.jf.dexlib.Interface;

import java.io.FileNotFoundException;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.List;

import org.jf.dexlib.ClassDataItem.EncodedMethod;
import org.jf.dexlib.CodeItem;
import org.jf.dexlib.MethodIdItem;
import org.jf.dexlib.Code.Analysis.AnalyzedInstruction;
import org.jf.dexlib.Code.Analysis.InlineMethodResolver;
import org.jf.dexlib.Code.Analysis.MethodAnalyzer;
import org.jf.dexlib.Code.Analysis.graphs.CDG;
import org.jf.dexlib.Code.Analysis.graphs.CFG;
import org.jf.dexlib.Code.Analysis.graphs.DominanceFrontiers;
import org.jf.dexlib.Code.Analysis.graphs.Dominators;
import org.jf.dexlib.Code.Analysis.graphs.Dominators.DomTree;
import org.jf.dexlib.Code.Analysis.graphs.GraphDumper;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.EdgeReversedGraph;

/**
 * 
 * @author Juergen Graf <juergen.graf@gmail.com>
 *
 */
public class DexMethod {
	
	private final List<AnalyzedInstruction> instructions;
	private final EncodedMethod encMethod;
	private SoftReference<CFG> cfg;
	private SoftReference<CFG> cfgWithExc;

	public static DexMethod build(final EncodedMethod encMethod) {
		return build(encMethod, false, null);
	}
	
	public static DexMethod build(final EncodedMethod encMethod, final boolean deodex, final InlineMethodResolver inlineResolver) {
		final MethodAnalyzer analyzer = new MethodAnalyzer(encMethod, deodex, inlineResolver);
        analyzer.analyze();
        final List<AnalyzedInstruction> instrList = analyzer.getInstructions();
        
        return new DexMethod(instrList, encMethod);
	}
	
	public DexMethod(final List<AnalyzedInstruction> instructions, final EncodedMethod encMethod) {
		this.instructions = Collections.unmodifiableList(instructions);
		this.encMethod = encMethod;
	}

	public MethodIdItem getId() {
		return encMethod.method;
	}
	
	public CodeItem getCode() {
		return encMethod.codeItem;
	}
	
	public List<AnalyzedInstruction> getInstructions() {
		return instructions;
	}
	
	public CFG getControlFlowGraph(final boolean includeExceptions) {
		if (includeExceptions) {
			if (cfgWithExc == null || cfgWithExc.get() == null) {
				final CFG tmp = CFG.build(instructions, getId().getMethodString(), true);
				cfgWithExc = new SoftReference<CFG>(tmp);
			}
			
			return cfgWithExc.get();
		} else {
			if (cfg == null || cfg.get() == null) {
				final CFG tmp = CFG.build(instructions, getId().getMethodString(), false);
				cfg = new SoftReference<CFG>(tmp);
			}
			
			return cfg.get();
		}
	}

	public CDG getControlDependenceGraph(final boolean includeExceptions) {
		return CDG.build(getControlFlowGraph(includeExceptions));
	}
	
	public Dominators<CFG.Node, CFG.Edge> getDominators(final boolean includeExceptions) {
		final CFG cfg = getControlFlowGraph(includeExceptions);
		final DirectedGraph<CFG.Node, CFG.Edge> reversedCfg = new EdgeReversedGraph<CFG.Node, CFG.Edge>(cfg);
		
		return Dominators.compute(reversedCfg, cfg.getExit());
	}
	
	public DomTree<CFG.Node> getDominationTree(final boolean includeExceptions) {
		return getDominators(includeExceptions).getDominationTree();
	}

	public DominanceFrontiers<CFG.Node, CFG.Edge> getDominanceFrontiers(final boolean includeExceptions) {
		final CFG cfg = getControlFlowGraph(includeExceptions);
		final DirectedGraph<CFG.Node, CFG.Edge> reversedCfg = new EdgeReversedGraph<CFG.Node, CFG.Edge>(cfg);

		return DominanceFrontiers.compute(reversedCfg, cfg.getExit());
	}
	
	public void dumpGraphs(final GraphDumper gDump) throws FileNotFoundException {
        gDump.dump(instructions, encMethod.method.getVirtualMethodString());
	}
	
	public String toString() {
		return "DexMethod(" + encMethod.method.getMethodString() + "): " + instructions.size() + " instructions";
	}
}
