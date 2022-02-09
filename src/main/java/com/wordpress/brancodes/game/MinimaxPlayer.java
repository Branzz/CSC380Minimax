package com.wordpress.brancodes.game;

import java.util.*;

public class MinimaxPlayer extends Player {

	private final int layers; // how many plies deep it will search
	boolean storeStates; // should we use the storage table? hashcodes may exceed int range and cause mass collision otherwise
	private final Map<Integer, HashMap<GameBoard, Integer>> stateStorageTable; // <layer number, <board, score>>

	public MinimaxPlayer(final int playerID, final int layers) {
		super(playerID);
		this.layers = layers;
		stateStorageTable = new HashMap<>();
	}

	@Override
	public void move(final GameBoard gameBoard) {
		storeStates = gameBoard.getHashCodeBitSize() <= 32;
		if (storeStates)
			stateStorageTable.clear();
		GameBoard best = null;
		int bestScore = Integer.MIN_VALUE;
		for (GameBoard move : gameBoard.expand(getPlayerID())) {
			int score = getScore(move, 1, false, bestScore, Integer.MAX_VALUE);
			if (score > bestScore) {
				bestScore = score;
				best = move;
			}
		}
		// (guaranteed not to be empty because it checks if the game is complete before)
		if (best.getScore() < 0)
			gameBoard.forfeit();
		else
			gameBoard.move(best);
	}

	/**
	 * @param depth the current depth we've searched to check if we've reached the set limit
	 * @param max to max or min the children depending on whose turn it is
	 */
	private int getScore(GameBoard gameBoard, int depth, boolean max, int alpha, int beta) {
		if (storeStates)
			return getScoreTable(gameBoard, depth, max, alpha, beta);
		else
			return getScoreSearch(gameBoard, depth, max, alpha, beta);
	}

	/**
	 * @return score of a game based on depth searching the board's children
	 */
	private int getScoreSearch(final GameBoard gameBoard, final int depth, boolean max, int alpha, int beta) {
		if (depth == layers)
			return getScoreCompleted(gameBoard);
		final int nextDepth = depth + 1;
		int best = max ? Integer.MIN_VALUE : Integer.MAX_VALUE;
		final Iterator<GameBoard> expandIter = gameBoard.expandIter(max ? getPlayerID() : getOtherPlayerID());
		while (expandIter.hasNext()) {
			int score = getScore(expandIter.next(), nextDepth, !max, alpha, beta);
			if (max) {
				best = Math.max(best, score);
				alpha = Math.max(alpha, score);
			} else {
				best = Math.min(best, score);
				beta = Math.min(beta, score);
			}
			if (beta <= alpha)
				break;
		}
		if (best == (max ? Integer.MIN_VALUE : Integer.MAX_VALUE))
			return getScoreCompleted(gameBoard);
		return best; // no children; fully played out
	}

	/**
	 * @return score of a game by depth search but with reusing already checked boards with a memoization table
	 */
	private int getScoreTable(GameBoard gameBoard, int depth, boolean max, int alpha, int beta) {
		final HashMap<GameBoard, Integer> levelTable =
				stateStorageTable.computeIfAbsent(gameBoard.getMoveAmount(), k -> new HashMap<>());
		Integer score = levelTable.get(gameBoard);
		if (score == null) {
			score = getScoreSearch(gameBoard, depth, max, alpha, beta);
			levelTable.put(gameBoard, score);
		}
		return score;
	}

	/**
	 * @return get the score of a complete game, the difference of the points earned by the players
	 */
	private int getScoreCompleted(GameBoard gameBoard) {
		// return gameBoard.getPlayerScore(getPlayerID()) - gameBoard.getPlayerScore(getOtherPlayerID());
		return gameBoard.getScore();
	}

}
