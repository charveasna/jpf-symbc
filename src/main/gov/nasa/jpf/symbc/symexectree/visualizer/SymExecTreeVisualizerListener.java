/**
 * 
 */
package gov.nasa.jpf.symbc.symexectree.visualizer;

import java.util.LinkedList;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.symbc.symexectree.ASymbolicExecutionTreeListener;
import gov.nasa.jpf.symbc.symexectree.DefaultNodeFactory;
import gov.nasa.jpf.symbc.symexectree.NodeFactory;
import gov.nasa.jpf.symbc.symexectree.SymbolicExecutionTree;

/**
 * @author Kasper S. Luckow <luckow@cs.aau.dk>
 *
 */
public class SymExecTreeVisualizerListener extends ASymbolicExecutionTreeListener {

	private String dotFileOutputPath;
	private SETPrettyPrinter dotPrinter;
	/**
	 * Convert DOT file:
	 * symbolic.visualiser.outputformat = [png|eps|dot|pdf|ps|svg]		(default: dot)
	 */
	public SymExecTreeVisualizerListener(Config conf, JPF jpf	) {
		super(conf, jpf);
		this.dotFileOutputPath = conf.getString("symbolic.visualizer.basepath");
		if(this.dotFileOutputPath == null)
			throw new SymExecTreeVisualierException("symbolic.visualizer.basepath has not been set");
		String outputFormat = conf.getString("symbolic.visualizer.outputformat", "dot");
		PRETTYPRINTER_FORMAT format = PRETTYPRINTER_FORMAT.valueOf(outputFormat.toUpperCase());
		this.dotPrinter = new SETPrettyPrinter(format);
	}

	@Override
	protected NodeFactory getNodeFactory() {
		return new DefaultNodeFactory();
	}

	@Override
	protected void doneConstructingSymbExecTree(
			LinkedList<SymbolicExecutionTree> trees) {
		this.dotPrinter.prettyPrintSymTrees(trees, this.dotFileOutputPath);
	}
}