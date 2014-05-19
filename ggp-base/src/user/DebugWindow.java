package user;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;

import javax.swing.JFrame;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;

import user.Tree.Node;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;

public class DebugWindow extends JFrame{

	private int X = 0;
	private int Y = 0;

	VisualizationViewer<Tree.Node,Integer> vv;
	Forest<Tree.Node,Integer> graph;

	TreeLayout<Tree.Node,Integer> treeLayout;

	Factory<Integer> edgeFactory = new Factory<Integer>() {
		int i=0;
		@Override
		public Integer create() {
			return i++;
		}};

	public DebugWindow(Tree tree) {
		super();

		setSize(1024,512);
		setTitle("MCTS Debug");

		graph = new DelegateForest<Tree.Node,Integer>();
		graph.addVertex(new Node(null,null));
		treeLayout = new TreeLayout<Tree.Node,Integer>(graph, 40,40);

        vv =  new VisualizationViewer<Tree.Node,Integer>(treeLayout, new Dimension(1024,512));

        vv.getRenderContext().setVertexLabelTransformer(new Transformer<Node,String>(){
        	@Override
			public String transform(Node n){return ""+(n.getScore()/100)+"/"+n.getNumberVisits();}
        });

        vv.getRenderContext().setVertexFillPaintTransformer(new Transformer<Node, Paint>(){
        	@Override
            public Paint transform(Tree.Node n) {
                if(n==null)
                	return Color.DARK_GRAY;
                if(n.getMove()==null || n.getMove().get(0).toString().equals("noop"))
                	return Color.RED;
                else
                	return Color.BLACK;
            }
        });

        this.getContentPane().add(vv);
        vv.setGraphMouse(new DefaultModalGraphMouse<>());
		this.setVisible(true);
		this.pack();
		setVisible(true);
	}

	public void visualizeTree(Tree tree)
	{
		tree.lock();

		graph = new DelegateForest<Tree.Node,Integer>();
		for(Node n : tree.getNodes())
			graph.addVertex(n);
		for(Node n : tree.getNodes())
			if(n.getParent()!=null)
				graph.addEdge(edgeFactory.create(),n.getParent(),n);

		TreeLayout<Tree.Node,Integer> layout = new TreeLayout<Tree.Node,Integer>(graph);
		vv.setGraphLayout(layout);
		vv.repaint();
		this.pack();
		tree.unlock();
	}
}
