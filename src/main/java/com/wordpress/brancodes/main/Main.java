package com.wordpress.brancodes.main;

import com.wordpress.brancodes.game.*;

public class Main {

	public static void main(String[] args) {
		run(3, 5);
		// final Scanner in = new Scanner(System.in);
		// System.out.println("Board size?");
		// int boardSize = in.nextInt();
		// System.out.println("Ply depth?");
		// int layers = in.nextInt();
		// run(boardSize, layers);
	}

	public static void run(int boardSize, int layers) {
		run(new ConsolePlayer(1), new MinimaxPlayer(2, layers), new GameBoard(boardSize));
	}

	// TODO scale to any amount of players?
	public static void run(Player player1, Player player2, GameBoard gameBoard) {
		System.out.print(gameBoard.toTileValueString());
		while (true) {
			if (playerMove(gameBoard, player1, 1))
				break;
			if (playerMove(gameBoard, player2, 2))
				break;
		}
	}

	/**
	 * @return if game ended
	 */
	private static boolean playerMove(GameBoard gameBoard, Player player, int playerNum) {
		System.out.printf("Player %d's move:\n", playerNum);
		player.move(gameBoard);
		if (gameBoard.didForfeit()) {
			System.out.printf("Player %d forfeited!\n" + gameBoard + gameBoard.getScoreString(), playerNum);
			return true;
		}
		if (gameBoard.complete()) {
			System.out.println("Game complete\n" + gameBoard + gameBoard.gameOverToString());
			return true;
		}
		System.out.print(gameBoard);
		return false;
	}

}
