package com.wordpress.brancodes.main;

import com.wordpress.brancodes.game.ConsolePlayer;
import com.wordpress.brancodes.game.GameBoard;
import com.wordpress.brancodes.game.MinimaxPlayer;
import com.wordpress.brancodes.game.Player;

public class Main {

	// ideas: scale to any amount of players?, implement minmax memoization table it with a trie?

	public static void main(String[] args) {
		run(3, 9);
		// final Scanner in = new Scanner(System.in);
		// System.out.println("Board size?");
		// int boardSize = in.nextInt();
		// System.out.println("Ply depth?");
		// int layers = in.nextInt();
		// run(boardSize, layers);
	}

	/**
	 * limit for quick response:
	 * boardSize:    2, 3, 4, 5
	 * max layers: inf, 9, 7, 6
	 */
	public static void run(int boardSize, int layers) {
		run(new ConsolePlayer(1), new MinimaxPlayer(2, layers), new GameBoard(boardSize));
	}

	public static void run(Player player1, Player player2, GameBoard gameBoard) {
		System.out.print(gameBoard.toTileValueString());
		while (true) {
			if (playerMove(gameBoard, player1, 1))
				break;
			if (playerMove(gameBoard, player2, 2))
				break;
		}
		System.out.println(gameBoard + gameBoard.gameOverToString());
	}

	/**
	 * apply a player's move to a board
	 * @return if game ended (no places to move or a player forfeited)
	 */
	private static boolean playerMove(GameBoard gameBoard, Player player, int playerNum) {
		System.out.printf("Player %d's move:\n", playerNum);
		player.move(gameBoard);
		if (gameBoard.didForfeit()) {
			System.out.printf("Player %d forfeited!\n", playerNum);
			return true;
		}
		if (gameBoard.complete()) {
			System.out.println("Game complete\n");
			return true;
		}
		System.out.print(gameBoard);
		return false;
	}

}
