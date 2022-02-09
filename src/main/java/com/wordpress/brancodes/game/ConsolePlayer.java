package com.wordpress.brancodes.game;

import java.util.Scanner;

public class ConsolePlayer extends Player {

	final static Scanner in = new Scanner(System.in);

	public ConsolePlayer(final int playerID) {
		super(playerID);
	}

	@Override
	public void move(final GameBoard gameBoard) {
		System.out.print(gameBoard.toPlayableString());
		final String inputString = in.next();
		if (inputString.equalsIgnoreCase("forfeit")) {
			gameBoard.forfeit();
			return;
		}
		final char input = inputString.charAt(0);
		gameBoard.move(getPlayerID(), input - (input > 'z' ? 'A' : 'a'));
	}

}
