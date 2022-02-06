package com.wordpress.brancodes.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class GameBoard implements Cloneable {

	private static final Random RAND = new Random();

	private boolean expanded;
	private List<Move> moves;
	private int moveAmount;
	private final int width;
	private final int height;
	private int move; // move to get here; semi-hashcode for MinimaxSearcher
	private final byte[][] verticals;
	private final byte[][] horizontals;
	private final byte[][] captured;
	private final byte[][] tileValues;

	public GameBoard(final int length) {
		this(length, length);
	}

	public GameBoard(final int height, final int width) {
		this(width, height, 0, -1, new byte[height][width + 1],
			 new byte[height + 1][width],
			 new byte[height][width],
			 new byte[height][width]);
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				tileValues[i][j] = (byte) (RAND.nextInt(4) + 1);
			}
		}
	}

	public GameBoard(final int width, final int height,
					 final int moveAmount, final int move,
					 final byte[][] verticals, final byte[][] horizontals,
					 final byte[][] captured, final byte[][] tileValues) {
		expanded = false;
		this.move = move;
		this.width = width;
		this.height = height;
		this.moveAmount = moveAmount;
		this.verticals = verticals;
		this.horizontals = horizontals;
		this.captured = captured;
		this.tileValues = tileValues;
	}

	/**
	 * @param pos position of next edge to play from top left to bottom right excluding filled edges
	 */
	public void move(int player, int pos) { // could implement a Map<Integer, Point> for O(1), but the board is too small to care
		int option = 0;
		for (int i = 0; i < height + 1; i++) {
			for (int j = 0; j < width; j++) {
				if (horizontals[i][j] == 0)
					option++;
				if (option == pos)
					horizontals[i][j] = (byte) player;
			}
			if (i < height - 1) {
				for (int j = 0; j < width; j++) {
					if (verticals[i][j] == 0)
						option++;
					if (option == pos)
						verticals[i][j] = (byte) player;
				}
			}
		}
		refreshTileCaptures(player);
	}

	private void move(int player, boolean isVertical, int i, int j) {
		(isVertical ? verticals : horizontals)[i][j] = (byte) player;
		refreshTileCaptures(player);
	}

	public List<Move> expand(int player) {
		if (expanded)
			return moves;
		moves = new ArrayList<>();
		AtomicInteger moveNum = new AtomicInteger(0);
		for (int i = 0; i < height + 1; i++) {
			expand0(player, i, false, moveNum);
			if (i < height - 1) {
				expand0(player, i, true, moveNum);
			}
		}
		expanded = true;
		return moves;
	}

	private void expand0(int player, int i, boolean isVertical, AtomicInteger moveNum) { // TODO antipattern?
		for (int j = 0; j < width; j++) {
			if (directionals(isVertical)[i][j] == 0) {
				GameBoard next = clone();
				(isVertical ? next.verticals : next.horizontals)[i][j] = (byte) player;
				refreshTileCaptures(player);
				next.incrementMoveAmount();
				next.setMove(moveNum.getAndIncrement());
				moves.add(new Move(player, isVertical, i, j, next));
			}
		}
	}

	private void setMove(final int move) {
		this.move = move;
	}

	private byte[][] directionals(boolean isVertical) {
		return isVertical ? verticals : horizontals;
	}

	/**
	 * to be called after every move (otherwise it may give area to the wrong person)
	 * temporary tile key:  0: unsearched tile
	 *                     -1: just searched; unknown if enclosed area
	 *                     -2: searched and did not find enclosed area
	 * @param lastPlayer player who will be given the tiles
	 */
	private void refreshTileCaptures(int lastPlayer) {
		for (int i = 0; i < height; i++)
			for (int j = 0; j < width; j++) {
				if (searchCaptured(i, j)) {
					updateJustSearched(lastPlayer);
				} else {
					updateJustSearched(-2);
				}
			}
		for (int i = 0; i < height; i++)
			for (int j = 0; j < width; j++)
				if (captured[i][j] < 0)
					captured[i][j] = 0; // fix temp negatives back to 0
	}

	private void updateJustSearched(int to) {
		for (int i = 0; i < height; i++)
			for (int j = 0; j < width; j++)
				if (captured[i][j] == -1)
					captured[i][j] = (byte) to;
	}

	/**
	 * input: coordinates of an empty and unsearched tile in captured
	 */
	private boolean searchCaptured(final int i, final int j) {
		if (captured[i][j] == 0) { // i and j guaranteed to be in bounds
			captured[i][j] = -1; // indicate that it is searched to avoid repeat checks
			if (horizontals[i][j] == 0) { // wall above
				if (i == 0) // edge wall
					return false;
				else if (!searchCaptured(i - 1, j))
					return false;
			}
			if (horizontals[i + 1][j] == 0) { // wall below
				if (i == height - 1) // edge wall
					return false;
				else if (!searchCaptured(i + 1, j))
					return false;
			}
			if (verticals[i][j] == 0) { // wall left
				if (j == 0) // edge wall
					return false;
				else if (!searchCaptured(i, j - 1))
					return false;
			}
			if (verticals[i][j + 1] == 0) { // wall left
				if (j == width - 1) // edge wall
					return false;
				else if (!searchCaptured(i, j + 1))
					return false;
			}
			return true;
		}
		return false;
	}

	private int getPlayer1Score() {
		return getPlayerScore(1);
	}

	private int getPlayer2Score() {
		return getPlayerScore(2);
	}

	private int getPlayerScore(int player) {
		int score = 0;
		for (int i = 0; i < height; i++)
			for (int j = 0; j < width; j++)
				if (captured[i][j] == player)
					score += tileValues[i][j];
		return score;
	}

	public boolean complete() {
		for (int i = 0; i < height; i++)
			for (int j = 0; j < width; j++)
				if (captured[i][j] == 0)
					return false;
		return true;
	}

	public int getMoveAmount() {
		return moveAmount;
	}

	void incrementMoveAmount() {
		moveAmount++;
	}

	public String gameOverToString() {
		final int player1Score = getPlayer1Score();
		final int player2Score = getPlayer2Score();
		return (player1Score == player2Score) ? "Tie " : ("Player " + (player1Score > player2Score ? '1' : '2') + " wins ")
					+ player1Score + " - " + player2Score;
	}

	private char tileToChar(byte val) {
		return val == 0 ? ' ' : (char) (val + '0');
		// return val == 0 ? ' ' : '-';
	}

	@Override
	public String toString() {
		StringBuilder sB = new StringBuilder();
		for (int i = 0; i < height + 1; i++) {
			for (int j = 0; j < width; j++) {
				sB.append('*').append(tileToChar(horizontals[i][j]));
			}
			sB.append("*\n");
			if (i < height - 1) {
				for (int j = 0; j < width; j++) {
					sB.append(tileToChar(verticals[i][j]));
					if (j < width - 1)
						sB.append(tileToChar(tileValues[i][j]));
				}
			}
			sB.append('\n');
		}
		return sB.toString();
	}

	public String toPlayableString() {
		StringBuilder sB = new StringBuilder();
		char option = 'A';
		for (int i = 0; i < height + 1; i++) {
			for (int j = 0; j < width; j++) {
				sB.append('*').append(horizontals[i][j] == 0 ? option++ : horizontals[i][j]);
			}
			sB.append("*\n");
			if (i < height - 1) {
				for (int j = 0; j < width; j++) {
					sB.append(verticals[i][j] == 0 ? option++ : verticals[i][j]);
					if (j < width - 1)
						sB.append(tileToChar(tileValues[i][j]));
				}
			}
			sB.append('\n');
		}
		return sB.toString();
	}

	@Override
	public GameBoard clone() {
		try {
			return (GameBoard) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return new GameBoard(width, height, moveAmount, move, verticals.clone(), horizontals.clone(), captured.clone(), tileValues.clone());
	}

}
