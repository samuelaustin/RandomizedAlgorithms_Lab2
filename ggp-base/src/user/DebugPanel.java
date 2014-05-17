package user;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLabel;

import org.apache.commons.collections15.Factory;

import user.Tree.Node;
import edu.uci.ics.jung.algorithms.layout.RadialTreeLayout;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.samples.SimpleGraphDraw;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;


public class DebugPanel extends JFrame {

	JLabel moveCountLabel;
	int moveCount = 0;
	VisualizationViewer<String,Integer> vv;
	SimpleGraphDraw sgv;
	Forest<String,Integer> graph;

	TreeLayout<String,Integer> treeLayout;

    RadialTreeLayout<String,Integer> radialLayout;

	Factory<Integer> edgeFactory = new Factory<Integer>() {
		int i=0;
		@Override
		public Integer create() {
			return i++;
		}};

	public DebugPanel()
	{
		//super(new GridBagLayout());

		//moveCountLabel = new JLabel("Move Count: " + moveCount);
		//this.add(moveCountLabel);

		graph = new DelegateForest<String,Integer>();

		treeLayout = new TreeLayout<String,Integer>(graph);
        radialLayout = new RadialTreeLayout<String,Integer>(graph);
        radialLayout.setSize(new Dimension(600,600));

		// sets the initial size of the space
		// The BasicVisualizationServer<V,E> is parameterized by the edge types
        vv =  new VisualizationViewer<String,Integer>(treeLayout, new Dimension(600,600));
        final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
        this.getContentPane().add(panel);
		this.setVisible(true);
	}

	public void incrementMoveCount()
	{
		//moveCount++;
		//moveCountLabel.setText("Move Count: " + (moveCount-1)/2);
	}

	public void visualizeTree(Node root)
	{
		graph = new DelegateForest<String,Integer>();
		createTreeRecursive(root);
	}

	public void createTreeRecursive(Node root)
	{
		graph.addVertex(root.getState().toString());
		for(Node n : root.getChildren())
		{
			createTreeRecursive(n);
			graph.addEdge(edgeFactory.create(),root.getState().toString(),n.getState().toString());
		}
	}
}
