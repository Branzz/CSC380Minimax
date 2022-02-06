package com.wordpress.brancodes.game;

public abstract class Player {

	private final int playerID;

	public Player(final int playerID) {
		this.playerID = playerID;
	}

	public abstract void move(final GameBoard gameBoard);

	protected final int getPlayerID() {
		return playerID;
	}

}
