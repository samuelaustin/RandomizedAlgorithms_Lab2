package user;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ggp.base.apps.player.detail.DetailPanel;
import org.ggp.base.apps.player.detail.SimpleDetailPanel;
import org.ggp.base.player.gamer.exception.GamePreviewException;
import org.ggp.base.player.gamer.statemachine.StateMachineGamer;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.cache.CachedStateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;

import user.Tree.Node;

public final class MCTS extends StateMachineGamer implements Runnable
{
	private Tree tree;
	private int roleNumber = 0;
	private DebugWindow win;
	private boolean multiThreaded = true;
	private boolean visualize = false;

	@Override
	public void run() {
		expandTree(selectNode());
	}


	private void monte_carlo_search(Tree.Node current, int itterations)
	{
		try
		{
			for(int i=0;i<itterations;i++)
				probe(current.getState(), current);
		}
		catch(Exception e){System.out.println("EXCEOTUSDIJAHFJKLHALGKLSDJAFY");e.printStackTrace();}
	}

	private Node selectBestChild()
	{
		Node n = null;
		double max = -1;
		for(int i=0;i<tree.getRoot().getNumberOfChildren();i++)
			if(tree.getRoot().getChild(i).getRatio()>max)
			{
				max = tree.getRoot().getChild(i).getRatio();
				n = tree.getRoot().getChild(i);
			}
		return n;
	}

	private Node selectNode()
	{
		Random r = new Random();
		double rand = r.nextDouble()*tree.getExplorationUCT();
		double sumUCT = 0;
		Node selected = null;
		for(Node n:tree.getNodes())
		{
			double nextUCT = n.getExplorationUCT();
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

	private long expandTree(Node node)
	{
		try
		{
			if(!getStateMachine().isTerminal(node.getState()))
			{
				long start = System.currentTimeMillis();
				List<Move> move = getStateMachine().getRandomJointMove(node.getState());
				MachineState childState = getStateMachine().getNextState(node.getState(),move);

				Node child = node.getChild(move,childState);
				if(child != null)
				{
					monte_carlo_search(child,3);
				}
				else
				{
					child = tree.addNode(node, move, childState);
					monte_carlo_search(child,3);
				}
				long stop = System.currentTimeMillis();
				return stop-start;
			}
			else
				return 0;
		}
		catch(Exception e) {System.out.println("Could not perform monte_carlo_search");e.printStackTrace();return -1;}
	}


	@Override
	public Move stateMachineSelectMove(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException
	{
		if(!tree.getRoot().getState().equals(getCurrentState()))
			for(int i = 0; i < tree.getRoot().getNumberOfChildren(); i++)
				if(getCurrentState().equals(tree.getRoot().getChild(i).getState()))
				{
					tree.setRoot(tree.getRoot().getChild(i));
					break;
				}

		if(tree.getRoot().getState().toString().compareTo(getCurrentState().toString())!=0)
		{
			System.out.println("CREATE NEW TREE");
			tree = new Tree(null, getCurrentState());
		}



		if(multiThreaded)
		{
			List<TreeExpander> threads = new ArrayList<TreeExpander>();
			int numThreads = 8;
			long runtime = 0;
			try {
				long start = System.currentTimeMillis();

				for(int i = 0 ; i < numThreads; i++)
					threads.add(new TreeExpander(getStateMachine(), tree, getRole(), timeout));

				threads.get(0).setRootExpander();

				for(int i = 0; i < numThreads; i++)
					threads.get(i).start();
				for(int i = 0; i < numThreads; i++)
					threads.get(i).join();

				long stop = System.currentTimeMillis();
				runtime += stop-start;
			} catch (InterruptedException e) {e.printStackTrace();}
		}
		else
		{
			long runtime = 0;
			while(runtime+3<timeout-System.currentTimeMillis())
			{
				Node selected = selectNode();
				runtime = expandTree(selected);
			}

		}

		List<Move> moves = getStateMachine().getLegalMoves(getCurrentState(), getRole());
		if(visualize && moves.size() == 1 && moves.get(0).toString().equals("noop"))
		{
			win.visualizeTree(tree);
			return moves.get(0);
		}


		Node n = selectBestChild();
		tree.setRoot(n);
		System.out.println("TREE SIZE: "+tree.getTreeSize());
		if(visualize)
			win.visualizeTree(tree);
		return n.getMove().get(roleNumber);
	}

	@Override
	public void stateMachineMetaGame(long timeout)
	{
		System.out.println("@META STATE!!");

		//Initialize the monte carlo search tree
		tree = new Tree(null, getCurrentState());
		if(visualize)
			win = new DebugWindow(tree);
		List<Role> roles = getStateMachine().getRoles();
		for(int i = 0; i < roles.size();i++)
		{
			if(roles.get(i).equals(getRole()))
			{
				roleNumber = i;
			}
		}
	}

	private void probe(MachineState state, Tree.Node node) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException
	{
		boolean childHasState = true;
		while(!getStateMachine().isTerminal(state) || childHasState)
		{
			childHasState = false;
			state = getStateMachine().getRandomNextState(state);
			for(Node c : node.getChildren())
				if(c.getState().equals(state))
				{
					node = c;
					childHasState = true;
					break;
				}
		}

		while(!getStateMachine().isTerminal(state))
			state = getStateMachine().getRandomNextState(state);

		node.addScore( getStateMachine().getGoal(state, getRole()) );
	}

	private int probe2(MachineState state) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException
	{
		final int[] depth = new int[1];
		return getStateMachine().getGoal(getStateMachine().performDepthCharge(state, depth), getRole());
	}

	@Override
	public String getName(){return "Monte Carlo Tree Search Player";}

	@Override
	public StateMachine getInitialStateMachine()
	{
		return new CachedStateMachine(new ProverStateMachine());
	}

	@Override
	public DetailPanel getDetailPanel()
	{
		System.out.println("@GETDETAILPANEL STATE!!");
		return new SimpleDetailPanel();
	}

	@Override
	public void stateMachineStop()
	{}

	@Override
	public void stateMachineAbort()
	{}

	@Override
	public void preview(Game g, long timeout) throws GamePreviewException
	{}

}