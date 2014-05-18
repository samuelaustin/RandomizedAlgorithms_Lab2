package user;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;


public final class Tree {
	private Node root;
	private List<Node> nodes;
	private final Lock lock = new ReentrantLock();

	public Tree(List<Move> move, MachineState state)
	{
		root = new Node(move,state);
		nodes = new ArrayList<Node>();
		nodes.add(root);
	}

	public int getTreeSize()
	{
		return nodes.size();
	}

	public void lock()
	{
		lock.lock();
	}

	public void unlock()
	{
		lock.unlock();
	}

	public Node getRoot(){return root;}
	public void setRoot(Node node)
	{
		Node parent = node.getParent();
		node.parent = null;
		root = node;

		if(parent != null)
		{
			parent.children.remove(node);
		}
		nodes = new Vector<Node>();
		setTreeNodesRecursive(root);
	}

	private void setTreeNodesRecursive(Node n)
	{
		nodes.add(n);
		for(Node c : n.children)
			setTreeNodesRecursive(c);
	}

	public void removeRecursive(Node node)
	{
		for(int i = 0; i < node.children.size(); i++)
		{
			removeRecursive(node.getChild(i));
		}
		nodes.remove(node);
	}

	public synchronized Node addNode(Node parent, List<Move> move, MachineState state)
	{
		if(nodes.contains(parent))
		{
			Node child = new Node(move,state);
			if(!parent.containsChild(child))
			{
				nodes.add(child);
				return parent.addNode(child);
			}
		}
		return null;
	}

	public boolean containsNode(Node n)
	{
		for(int i = 0; i < nodes.size(); i++)
		{
			if(nodes.get(i).equals(n))
			{
				return true;
			}
		}
		return false;
	}

	public synchronized List<Node> getNodes()
	{
		return nodes;
	}

	@Override
	public String toString()
	{
		return root.toString();
	}

	public double getExplorationUCT()
	{
		double UCT = 0;
		for(Node n:nodes)
			UCT+=n.getExplorationUCT();

		return UCT;
	}

	public double getSelectionUCT()
	{
		double UCT = 0;
		for(Node n:nodes)
			UCT+=n.getSelectionUCT();
		return UCT;
	}

	public static class Node
	{
		private Node parent;
		private List<Node> children;

		private List<Move> move;
		private MachineState state;
		private int score;
		private int visits;

		public Node(List<Move> move, MachineState state)
		{
			this.move = move;
			this.state = state;
			score = 0;
			visits = 0;
			children = new ArrayList<Node>();
			parent = null;
		}

		public synchronized void addScore(int s)
		{
			score+=s;
			visits++;
			if(parent!=null)
				parent.addScore(s);
		}

		public List<Move> getMove() {
			return move;
		}

		public MachineState getState() {
			return state;
		}

		public int getScore()
		{
			return score;
		}

		public int getNumberVisits()
		{
			return visits;
		}

		public float getRatio()
		{
			if(visits == 0)
				return Float.MAX_VALUE;
			else
				return ((float)score)/((float)visits);
		}

		public double getExplorationUCT()
		{
			if(getNumberVisits()>0)
			{
				double parentScore = 0;
				if(parent!=null)
					parentScore = parent.getNumberVisits();

				return getRatio()/100 + 2*2.0/Math.sqrt(2.0)*Math.sqrt(2*Math.log(parentScore+1)/getNumberVisits());
			}
			else
				return 0;
		}

		public double getSelectionUCT()
		{
			if(getNumberVisits() > 0)
			{
				double parentScore = 0;
				if(parent!=null)
					parentScore = parent.getNumberVisits();

				return getRatio()/100+2*2.0/Math.sqrt(2.0)*Math.sqrt(2*Math.log(parentScore+1)/getNumberVisits());
			}
			else
				return Double.MAX_VALUE;
		}

		public Node getParent(){return parent;}

		public Node getChild(int index){return children.get(index);}

		public int getNumberOfChildren(){return children.size();}

		public synchronized List<Node> getChildren() { return children; }

		private synchronized Node addNode(List<Move> move, MachineState state)
		{
			Node node = new Node(move,state);
			node.parent = this;
			children.add(node);
			return node;
		}
		public synchronized Node addNode(Node node)
		{
			node.parent = this;
			children.add(node);
			return node;
		}

		public boolean equals(Node node)
		{
			return this.getState().equals(node.getState()) && this.getMove().equals(node.getMove());
		}

		public boolean containsChild(Node child)
		{
			for(int i = 0; i < children.size();i++)
			{
				if(children.get(i).equals(child))
					return true;
			}
			return false;
		}

		public Node getChild(List<Move> m, MachineState s)
		{
			for(int i = 0; i < children.size(); i++)
			{
				if(children.get(i).getMove().equals(m) && children.get(i).getState().equals(s))
					return children.get(i);
			}
			return null;
		}

		@Override
		public String toString()
		{
			String ans = "Node(" + getRatio() + ")[ ";

			for(int i=0;i<getNumberOfChildren();i++)
				ans = ans + getChild(i).toString() + " ";
			ans += "]";
			return ans;
		}
	}

	public static void main(String[] args)
	{
		Tree tree = new Tree(null,null);
		tree.getRoot().addScore(0);
		Node n = new Node(null,null);
		tree.getRoot().addNode(n);
		n.addScore(1);
		System.out.println(tree);
	}
}
