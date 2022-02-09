package com.wordpress.brancodes.game;

import java.util.*;

public class MinimaxPlayer extends Player {

	private final int layers; // how many plies deep it will search
	boolean storeStates; // should we use the storage table? hashcodes may exceed int range and cause mass collision otherwise
	private final Map<Integer, HashMap<GameBoard, Integer>> stateStorageTable; // <layer number, <board, score>>
	// TODO could implement it with a trie?

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
		final Move bestMove = gameBoard.expand(getPlayerID())
								.stream()
								.max(Comparator.comparing(move -> getScore(move.gameBoard(), 1, false)))
								.orElseThrow(); // (guaranteed not to be empty because it checks if the game is complete before)
		if (getScore(bestMove.gameBoard(), 1, false) < 0)
			gameBoard.forfeit();
		else
			gameBoard.move(bestMove);
	}

	/**
	 * @param depth the current depth we've searched to check if we've reached the set limit
	 * @param max to max or min the children depending on whose turn it is
	 */
	private int getScore(GameBoard gameBoard, int depth, boolean max) {
		if (storeStates)
			return getScoreTable(gameBoard, depth, max);
		else
			return getScoreSearch(gameBoard, depth, max);
	}

	/**
	 * @return score of a game based on depth searching the board's children
	 */
	private int getScoreSearch(final GameBoard gameBoard, final int depth, boolean max) {
		if (depth == layers)
			return getScoreCompleted(gameBoard);
		final int nextDepth = depth + 1;
		return gameBoard.expand(max ? getPlayerID() : getOtherPlayerID()).stream()
						.map(move -> getScore(move.gameBoard(), nextDepth, !max))
						.max(max ? Integer::compare : (x, y) -> Integer.compare(y, x)) // y and x flipped for minimize
						.orElse(getScoreCompleted(gameBoard)); // no children; fully played out
	}

	/**
	 * @return score of a game by depth search but with reusing already checked boards with a memoization table
	 */
	private int getScoreTable(GameBoard gameBoard, int depth, boolean max) {
		final HashMap<GameBoard, Integer> levelTable =
				stateStorageTable.computeIfAbsent(gameBoard.getMoveAmount(), k -> new HashMap<>());
		Integer score = levelTable.get(gameBoard);
		if (score == null) {
			score = getScoreSearch(gameBoard, depth, max);
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
