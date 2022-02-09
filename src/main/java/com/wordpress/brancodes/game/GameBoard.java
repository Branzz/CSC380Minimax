package com.wordpress.brancodes.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static com.wordpress.brancodes.game.Tile.*;

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
	private final Tile[][] captured;
	private final byte[][] tileValues;
	private int score; // p2 - p1
	private boolean throwDraw = false;

	public GameBoard(final int length) {
		this(length, length);
	}

	public GameBoard(final int height, final int width) {
		this(width, height, 0, -1, 0,
			 new byte[height][width + 1],
			 new byte[height + 1][width],
			 new Tile[height][width],
			 new byte[height][width]);
		for (int i = 0; i < height; i++)
			for (int j = 0; j < width; j++)
				captured[i][j] = EMPTY;
		for (int i = 0; i < height; i++)
			for (int j = 0; j < width; j++)
				tileValues[i][j] = (byte) (RAND.nextInt(5) + 1);
	}

	public GameBoard(final int width, final int height,
					 final int moveAmount, final int move,
					 final int score,
					 final byte[][] verticals, final byte[][] horizontals,
					 final Tile[][] captured, final byte[][] tileValues) {
		expanded = false;
		this.width = width;
		this.height = height;
		this.moveAmount = moveAmount;
		this.move = move;
		this.score = score;
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
				if (horizontals[i][j] == 0) {
					if (option == pos) {
						horizontals[i][j] = (byte) 1;
						move(player);
					return;
					}
					option++;
				}
			}
			if (i < height) {
				for (int j = 0; j < width + 1; j++) {
					if (verticals[i][j] == 0) {
						if (option == pos) {
							verticals[i][j] = (byte) 1;
							move(player);
							return;
						}
						option++;
					}
				}
			}
		}
	}

	private void move(int player, boolean isVertical, int i, int j) {
		(isVertical ? verticals : horizontals)[i][j] = (byte) player;
		move(player);
	}

	public void move(final Move m) {
		move(m.player(), m.isVertical(), m.i(), m.j());
	}

	void moveRandom(int player) {
		while (true) {
			final boolean isVertical = RAND.nextBoolean();
			final int i = RAND.nextInt(isVertical ? height : height + 1);
			final int j = RAND.nextInt(isVertical ? width + 1 : width);
			if ((isVertical ? verticals : horizontals)[i][j] == 0) {
				(isVertical ? verticals : horizontals)[i][j] = (byte) 1;
				move(player);
				return;
			}
		}
	}

	private void move(int player) {
		refreshTileCaptures(player);
		unexpand();
		incrementMoveAmount();
	}

	private void unexpand() {
		expanded = false;
		moves = null;
	}

	public List<Move> expand(int player) {
		if (expanded)
			return moves;
		moves = new ArrayList<>();
		int moveNum = 0;
		for (int i = 0; i < height + 1; i++) {
			for (int j = 0; j < width; j++) {
				if (horizontals[i][j] == 0) {
					GameBoard next = clone();
					next.horizontals[i][j] = (byte) player;
					next.refreshTileCaptures(player);
					next.incrementMoveAmount();
					next.unexpand();
					next.setMove(moveNum++);
					moves.add(new Move(player, false, i, j, next));
				}
			}
			if (i < height) {
				for (int j = 0; j < width + 1; j++) {
					if (verticals[i][j] == 0) { // extracted method is tempting here, but it quickly leads to bad design
						GameBoard next = clone();
						next.verticals[i][j] = (byte) player;
						next.refreshTileCaptures(player);
						next.incrementMoveAmount();
						next.unexpand();
						next.setMove(moveNum++);
						moves.add(new Move(player, true, i, j, next));
					}
				}
			}
		}
		expanded = true;
		return moves;
	}

	private void setMove(final int move) {
		this.move = move;
	}

	/**
	 * check to find any new enclosed areas and give those tiles to the last player who moved
	 * to be called after every move (otherwise it may give area to the wrong person)
	 * @param lastPlayer player who will be given the tiles
	 */
	private void refreshTileCaptures(int lastPlayer) {
		for (int i = 0; i < height; i++)
			for (int j = 0; j < width; j++) {
				if (searchCaptured(i, j)) {
					int pointsGained = 0;
					for (int i0 = 0; i0 < height; i0++)
						for (int j0 = 0; j0 < width; j0++)
							if (captured[i0][j0] == SEARCHED) {
								pointsGained += tileValues[i0][j0];
								captured[i0][j0] = Tile.get(lastPlayer);
								if (horizontals[i][j] == 0) // above
									horizontals[i][j] = 3;
								if (verticals[i][j + 1] == 0) // right
									verticals[i][j + 1] = 3;
								if (horizontals[i + 1][j] == 0) // below
									horizontals[i + 1][j] = 3;
								if (verticals[i][j] == 0) // left
									verticals[i][j] = 3;
							}
					score += lastPlayer == 2 ? pointsGained : -pointsGained;
				} else {
					for (int i0 = 0; i0 < height; i0++)
						for (int j0 = 0; j0 < width; j0++)
							if (captured[i0][j0] == SEARCHED)
								captured[i0][j0] = UNENCLOSED;
				}
			}
		for (int i = 0; i < height; i++) // reset
			for (int j = 0; j < width; j++)
				if (captured[i][j] == SEARCHED || captured[i][j] == UNENCLOSED)
					captured[i][j] = EMPTY;
	}

	/**
	 * input: coordinates of an empty and unsearched tile in captured
	 */
	private boolean searchCaptured(final int i, final int j) {
		if (i < 0 || i >= height || j < 0 || j >= width)
			return false;
		if (captured[i][j] == EMPTY) { // i and j guaranteed to be in bounds
			captured[i][j] = SEARCHED; // indicate that it is searched to avoid repeat checks
			if (horizontals[i][j] == 0 && !searchCaptured(i - 1, j)) // wall above
				return false;
			if (verticals[i][j + 1] == 0 && !searchCaptured(i, j + 1)) // wall right
				return false;
			if (horizontals[i + 1][j] == 0 && !searchCaptured(i + 1, j)) // wall below
				return false;
			if (verticals[i][j] == 0 && !searchCaptured(i, j - 1)) // wall left
				return false;
			return true;
		}
		return captured[i][j] == SEARCHED; // just checked a searched tile, so it could still be enclosed
	}

	private int getPlayer1Score() {
		return getPlayerScore(1);
	}

	private int getPlayer2Score() {
		return getPlayerScore(2);
	}

	int getPlayerScore(int player) {
		final Tile tile = get(player);
		int score = 0;
		for (int i = 0; i < height; i++)
			for (int j = 0; j < width; j++) {
				if (captured[i][j] == tile)
					score += tileValues[i][j];
			}
		return score;
	}

	/**
	 * @return player 2's score - player 1's score
	 */
	int getScore() {
		return score;
		// int score = 0;
		// for (int i = 0; i < height; i++)
		// 	for (int j = 0; j < width; j++)
		// 		if (captured[i][j] == FILL2)
		// 			score += tileValues[i][j];
		// 		else if (captured[i][j] == FILL1)
		// 			score -= tileValues[i][j];
		// return score;
	}

	/**
	 * @return if board has been finished and can no longer be played on
	 */
	public boolean complete() {
		for (int i = 0; i < height; i++)
			for (int j = 0; j < width; j++)
				if (captured[i][j] == EMPTY)
					return false;
		return true;
	}

	/**
	 * @return the level of this board to be stored into respective category on minimax's storage table
	 */
	public int getMoveAmount() {
		return moveAmount;
	}

	void incrementMoveAmount() {
		moveAmount++;
	}

	/**
	 * @return message at end of game showing if it's a tie or the winner and the scores
	 */
	public String gameOverToString() {
		final int player1Score = getPlayer1Score();
		final int player2Score = getPlayer2Score();
		boolean p1Win = player1Score > player2Score;
		return (player1Score == player2Score) ? "Tie " : ("Player " + (p1Win ? '1' : '2') + " wins! ")
														 + (p1Win ? player1Score : player2Score) + " to " + (p1Win ? player2Score : player1Score);
	}

	/**
	 * @return message showing only the current score
	 */
	public String getScoreString() {
		final int player1Score = getPlayer1Score();
		final int player2Score = getPlayer2Score();
		boolean p1Win = player1Score > player2Score;
		return (p1Win ? player1Score : player2Score) + " to " + (p1Win ? player2Score : player1Score);
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
				sB.append('*').append(horizontals[i][j] == 0 ? ' ' : horizontals[i][j] == 3 ? '|' : '-');
			}
			sB.append("*\n");
			if (i < height) {
				for (int j = 0; j < width + 1; j++) {
					sB.append(verticals[i][j] == 0 ? ' ' : verticals[i][j] == 3 ? '-' : '|');
					if (j < width)
						sB.append(captured[i][j].toChar());
				}
			}
			sB.append('\n');
		}
		return sB.toString();
	}

	/**
	 * @return string of the board with the tile's values
	 */
	public String toTileValueString() {
		StringBuilder sB = new StringBuilder();
		for (int i = 0; i < height + 1; i++) {
			for (int j = 0; j < width; j++) {
				sB.append('*').append(horizontals[i][j] == 0 || horizontals[i][j] == 3 ? ' ' : '-');
			}
			sB.append("*\n");
			if (i < height) {
				for (int j = 0; j < width + 1; j++) {
					sB.append(verticals[i][j] == 0 || verticals[i][j] == 3 ? ' ' : '|');
					if (j < width)
						sB.append(tileToChar(tileValues[i][j]));
				}
			}
			sB.append('\n');
		}
		return sB.toString();
	}

	/**
	 * @return user interface string for console players with each letter representing a possible move
	 */
	public String toPlayableString() {
		StringBuilder sB = new StringBuilder();
		char option = 'a';
		for (int i = 0; i < height + 1; i++) {
			for (int j = 0; j < width; j++) {
				sB.append('*').append(horizontals[i][j] == 0 ? option++ : horizontals[i][j] == 3 ? ' ' : '-');
				if (option == 'z' + 1)
					option = 'A';
			}
			sB.append("*\n");
			if (i < height) {
				for (int j = 0; j < width + 1; j++) {
					sB.append(verticals[i][j] == 0 ? option++ : verticals[i][j] == 3 ? ' ' : '|');
					if (option == 'z' + 1)
						option = 'A';
					if (j < width)
						sB.append(' ');
				}
			}
			sB.append('\n');
		}
		return sB.toString();
	}

	@Override
	public GameBoard clone() {
		// try {
		// 	return (GameBoard) super.clone();
		// } catch (CloneNotSupportedException e) {
		// 	e.printStackTrace();
		// }
		return new GameBoard(width, height, moveAmount, move, score, clone(verticals), clone(horizontals), clone(captured), clone(tileValues));
	}

	private byte[][] clone(final byte[][] bytes) {
		final byte[][] clone = new byte[bytes.length][bytes[0].length];
		for (int i = 0; i < bytes.length; i++)
			System.arraycopy(bytes[i], 0, clone[i], 0, bytes[i].length);
		return clone;
	}

	private Tile[][] clone(final Tile[][] tiles) {
		final Tile[][] clone = new Tile[tiles.length][tiles[0].length];
		for (int i = 0; i < tiles.length; i++)
			System.arraycopy(tiles[i], 0, clone[i], 0, tiles[i].length);
		return clone;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		final GameBoard gameBoard = (GameBoard) o;

		if (!Arrays.deepEquals(verticals, gameBoard.verticals))
			return false;
		if (!Arrays.deepEquals(horizontals, gameBoard.horizontals))
			return false;
		return Arrays.deepEquals(captured, gameBoard.captured);
	}

	/**
	 * @return completely unique hashcode if the board is small enough for fast repeat checking, otherwise a default implementation
	 */
	@Override
	public int hashCode() {
		int hash = 0;
		int shift = 0;
		// moves you can make + who captured which must be in range for efficient bitwise hashcode (up to 2x3)
		if (getHashCodeBitSize() > 32)
			return defaultHashCode();
		for (int i = 0; i < height; i++)
			for (int j = 0; j < width + 1; j++)
				hash |= (verticals[i][j] & 1) << shift++;
		for (int i = 0; i < height + 1; i++)
			for (int j = 0; j < width; j++)
				hash |= (horizontals[i][j] & 1) << shift++;
		for (int i = 0; i < height; i++)
			for (int j = 0; j < width; j++)
				hash |= (captured[i][j] == FILL1 ? 1 : captured[i][j] == FILL2 ? 2 : 0) << (shift += 2);
		return hash;
	}

	private int defaultHashCode() {
		int result = Arrays.deepHashCode(verticals);
		result = 31 * result + Arrays.deepHashCode(horizontals);
		result = 31 * result + Arrays.deepHashCode(captured);
		return result;
	}

	/**
	 * @return the most amount of moves you could play on this board size
	 */
	public int getMaxDepth() {
		return height * (width + 1) + (height + 1) + width;
	}

	/**
	 * @return amount of bits of unique data for efficient hashcode
	 */
	int getHashCodeBitSize() {
		return getMaxDepth() + 2 * height * width;
	}

	void forfeit() {
		throwDraw = true;
	}

	public boolean didForfeit() {
		return throwDraw;
	}

}
