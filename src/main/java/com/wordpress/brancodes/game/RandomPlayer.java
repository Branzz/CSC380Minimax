package com.wordpress.brancodes.game;

public class RandomPlayer extends Player {

	public RandomPlayer(final int playerID) {
		super(playerID);
	}

	@Override
	public void move(final GameBoard gameBoard) {
		gameBoard.moveRandom(getPlayerID());
	}

}
