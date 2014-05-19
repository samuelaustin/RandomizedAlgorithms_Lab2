package user;

import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import user.Tree.Node;

public class TreeExpander extends Thread{

	private StateMachine machine;
	private Tree tree;
	private Role role;
	private long timeout;
	private boolean root_expander = false;

	public void setRootExpander()
	{
		root_expander = true;
	}

	public TreeExpander(StateMachine m, Tree t, Role r, long to)
	{
		machine = m;
		tree = t;
		role = r;
		timeout = to;
	}

	@Override
	public void run() {
		long runtime = 0;
		while(runtime+1<timeout-System.currentTimeMillis())
		{
			runtime=expandTree(selectNode());
		}
	}

	private Node selectNode()
	{
		try
		{
			if(root_expander)
				return tree.getRoot();

			double rand = ThreadLocalRandom.current().nextDouble()*tree.getUCT();
			double sumUCT = 0;
			Node selected = null;
			for(Node n:tree.getNodes())
			{
				double nextUCT = n.getUCT();
				if(rand >= sumUCT && rand <= (sumUCT+nextUCT))
				{
					selected = n;
					break;
				}
				else
					sumUCT+=nextUCT;
			}
			return selected;
		}
		catch(ConcurrentModificationException e){return selectNode();}
	}

	private long expandTree(Node node)
	{
		try
		{
			if(!machine.isTerminal(node.getState()))
			{
				long start = System.currentTimeMillis();
				List<Move> move = machine.getRandomJointMove(node.getState());
				MachineState childState = machine.getNextState(node.getState(),move);

				Node child = node.getChild(move,childState);
				if(child != null)
				{
					monte_carlo_search(child,3);
				}
				else
				{
					if(childState != null)
					{
						child = tree.addNode(node, move, childState);
						if(child != null)
							monte_carlo_search(child,3);
					}
				}
				long stop = System.currentTimeMillis();
				return stop-start;
			}
			else
				return 0;
		}
		catch(Exception e) {e.printStackTrace();return -1;}
	}

	private void monte_carlo_search(Tree.Node current, int itterations)
	{
		try
		{
			for(int i=0;i<itterations;i++)
				probe(current.getState(), current);
		}
		catch(Exception e){e.printStackTrace();}
	}

	private void probe(MachineState state, Tree.Node node) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException
	{
		try
		{
			boolean check = true;
			while(check)
			{
				if(machine.isTerminal(state))
				{
					node.addScore( machine.getGoal(state, role));
					return;
				}

				state = machine.getRandomNextState(state);
				check = false;
				for(Node c : node.getChildren())
					if(c.getState().equals(state))
					{
						check = true;
						node = c;
					}

			}
			while(!machine.isTerminal(state))
				state = machine.getRandomNextState(state);

			node.addScore( machine.getGoal(state, role) );
		}
		catch(ConcurrentModificationException e)
		{probe(state,node);}
	}
}
