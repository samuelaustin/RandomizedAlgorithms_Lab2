package user;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;

import javax.swing.JFrame;
import javax.swing.JLabel;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;

import user.Tree.Node;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.samples.SimpleGraphDraw;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;

public class DebugWindow extends JFrame{

	private int X = 0;
	private int Y = 0;

	JLabel moveCountLabel;
	int moveCount = 0;
	VisualizationViewer<Tree.Node,Integer> vv;
	SimpleGraphDraw sgv;
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

		//super(new GridBagLayout());

		//moveCountLabel = new JLabel("Move Count: " + moveCount);
		//this.add(moveCountLabel);

		graph = new DelegateForest<Tree.Node,Integer>();
		graph.addVertex(new Node(null,null));
		treeLayout = new TreeLayout<Tree.Node,Integer>(graph, 40,40);

		// sets the initial size of the space
		// The BasicVisualizationServer<V,E> is parameterized by the edge types

        vv =  new VisualizationViewer<Tree.Node,Integer>(treeLayout, new Dimension(1024,512));

        //vv.getRenderContext().setVertexFillPaintTransformer(new VertexPaintTransformer(vv.getPickedVertexState()));

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


        final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
        this.getContentPane().add(vv);
        vv.setGraphMouse(new DefaultModalGraphMouse<>());
		this.setVisible(true);
		this.pack();
		setVisible(true);
	}

	public void visualizeTree(Tree tree)
	{
		tree.lock();
	//	DelegateForest<Node, Integer> empty = new DelegateForest<Tree.Node,Integer>();
		graph = new DelegateForest<Tree.Node,Integer>();
		for(Node n : tree.getNodes())
			graph.addVertex(n);
		for(Node n : tree.getNodes())
			if(n.getParent()!=null)
				graph.addEdge(edgeFactory.create(),n.getParent(),n);


		BorderLayout m = new BorderLayout();

		TreeLayout<Tree.Node,Integer> layout = new TreeLayout<Tree.Node,Integer>(graph);

		//LayoutManager m = new LayoutManager();
		vv.setGraphLayout(layout);
		//treeLayout.setGraph(graph);
		vv.repaint();
		this.pack();
		tree.unlock();
	}
/*
	private class VertexPaintTransformer implements
			Transformer<Tree.Node,Paint>
	{

        private final PickedInfo<Tree.Node> pi;

        VertexPaintTransformer ( PickedInfo<Tree.Node> pi ) {
            super();
            if (pi == null)
                throw new IllegalArgumentException("PickedInfo instance must be non-null");
            this.pi = pi;
        }

        @Override
        public Paint transform(Tree.Node n) {
            Color p = Color.YELLOW;
            //Edit here to set the colours as reqired by your solution
            if(n.getMove().get(0).toString().equals("noop"))
            	p = Color.RED;
            else
            	p = Color.BLACK;

            return p;
        }
    }*/
}
