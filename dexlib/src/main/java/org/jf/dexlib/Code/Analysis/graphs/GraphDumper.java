package org.jf.dexlib.Code.Analysis.graphs;

import java.io.FileNotFoundException;
import java.util.List;

import org.jf.dexlib.Code.Analysis.AnalyzedInstruction;

public class GraphDumper {

    private final String prefix;
    private final boolean dumpCFG;
    private final boolean dumpDOM;
    private final boolean dumpCDG;
    private final boolean includeExc;
    
    public GraphDumper(String prefix, boolean dumpCFG, boolean dumpDOM, boolean dumpCDG,
            boolean includeExc) {
        this.prefix = prefix;
        this.dumpCFG = dumpCFG;
        this.dumpDOM = dumpDOM;
        this.dumpCDG = dumpCDG;
        this.includeExc = includeExc;
    }
    
    public void dump(final List<AnalyzedInstruction> instructions, final String name) throws FileNotFoundException {
        if (!dumpCFG && !dumpDOM && !dumpCDG) {
            return;
        }
        
        final String fileName = prefix + WriteGraphToDot.sanitizeFileName(name);
        final CFG cfg = CFG.build(instructions, name, includeExc);
        
        if (dumpCFG) {
            WriteGraphToDot.write(cfg, fileName + ".cfg.dot");
        }
        
        if (dumpDOM) {
            throw new UnsupportedOperationException("No dominator computation implemented by now.");
        }
        
        if (dumpCDG) {
            throw new UnsupportedOperationException("No control dependency computation implemented by now.");
        }
    }
    
}