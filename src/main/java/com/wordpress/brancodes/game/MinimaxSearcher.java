package com.wordpress.brancodes.game;

import java.util.*;

public class MinimaxSearcher extends Player {

	private final int layers;
//	private int currentMoveAmount; // plus index is move amount of tracker
	// boards with that move amount; removes past move amounts and keeps them for future searches
	// <layer num, <move num, board>>
	private final Map<Integer, Map<Integer, GameBoard>> gameBoardTracker;

	public MinimaxSearcher(final int playerID, final int layers) {
		super(playerID);
		this.layers = layers;
		gameBoardTracker = new HashMap<>(layers);
	}

	private void search(GameBoard gameBoard) {

	}

	@Override
	public void move(final GameBoard gameBoard) {
		refreshTracker(gameBoard.getMoveAmount());
		final List<Move> moves = gameBoard.expand(getPlayerID());
	}

	private void refreshTracker(final int moveAmount) {
		gameBoardTracker.remove(moveAmount - 1); // move was made; forget last layer's search levels
		// completely clear the map, except keep the expansion of the current gameboard

	}

}
