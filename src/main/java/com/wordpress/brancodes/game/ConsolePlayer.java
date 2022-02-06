package com.wordpress.brancodes.game;

import java.util.Scanner;

public class ConsolePlayer extends Player {

	public ConsolePlayer(final int playerID) {
		super(playerID);
	}

	@Override
	public void move(final GameBoard gameBoard) {
		System.out.println(gameBoard.toPlayableString());
		final Scanner in = new Scanner(System.in);
		char input = in.next().charAt(0);
		gameBoard.move(getPlayerID(), input - 'A');
		in.close();
	}

}
