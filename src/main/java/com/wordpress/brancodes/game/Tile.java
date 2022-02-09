package com.wordpress.brancodes.game;

public enum Tile {

	EMPTY(' '), FILL1('1'), FILL2('2'), SEARCHED('x'), UNENCLOSED('o');

	private final char toChar;

	Tile(final char toChar) {
		this.toChar = toChar;
	}

	/**
	 * convert player ID into Fill Tile
	 */
	public static Tile get(final int lastPlayer) {
		return lastPlayer == 1 ? FILL1 : lastPlayer == 2 ? FILL2 : null;
	}

	public Character toChar() {
		return toChar;
	}

}
