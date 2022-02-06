package com.wordpress.brancodes.main;

import com.wordpress.brancodes.game.ConsolePlayer;
import com.wordpress.brancodes.game.GameBoard;
import com.wordpress.brancodes.game.MinimaxSearcher;
import com.wordpress.brancodes.game.Player;

public class Main {

	public static void main(String[] args) {
		run(4, 5);
		// final Scanner in = new Scanner(System.in);
		// System.out.println("Board size?");
		// int boardSize = in.nextInt();
		// System.out.println("Ply depth?");
		// int layers = in.nextInt();
		// run(boardSize, layers);
		// in.close();
	}

	static void run(int boardSize, int layers) {
		run(new ConsolePlayer(1), new MinimaxSearcher(2, layers), new GameBoard(boardSize));
	}

	static void run(Player player1, Player player2, GameBoard gameBoard) { // TODO scale to any amount of players?
		while (!gameBoard.complete()) {
			player1.move(gameBoard);
			System.out.println(gameBoard);
			if (gameBoard.complete()) {
				break;
			}
			player2.move(gameBoard);
			// System.out.println(gameBoard);
		}
		System.out.println("Game complete: " + gameBoard.gameOverToString());
	}

}
