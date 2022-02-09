package com.wordpress.brancodes.game;

public abstract class Player {

	private final int playerID; // the order of the player; first or second

	public Player(final int playerID) {
		this.playerID = playerID;
	}

	/**
	 * how the player makes a move on a given game board
	 */
	public abstract void move(final GameBoard gameBoard);

	protected final int getPlayerID() {
		return playerID;
	}

	/**
	 * {@code getPlayerID() == 1 ? 2 : 1}
	 * @return the other potential player's playerID
	 */
	protected final int getOtherPlayerID() {
		return playerID ^ 3;
	}

}
