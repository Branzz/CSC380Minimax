package com.wordpress.brancodes.game;

import java.util.List;

public class MinimaxSearcher extends Player {

	private final int layers;

	public MinimaxSearcher(final int playerID, final int layers) {
		super(playerID);
		this.layers = layers;
	}

	private void search(GameBoard gameBoard) {

	}

	@Override
	public void move(final GameBoard gameBoard) {
		final List<Move> moves = gameBoard.expand(getPlayerID());
	}

}
