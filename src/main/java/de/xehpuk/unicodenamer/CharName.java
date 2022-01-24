package de.xehpuk.unicodenamer;

import java.nio.charset.StandardCharsets;

public final class CharName implements Comparable<CharName> {
	private static final CharName[] cache = new CharName[128];
	
	static {
		for (int i = 0, m = cache.length; i < m; i++) {
			cache[i] = new CharName(i);
		}
	}
	
	public static CharName valueOf(final int codePoint) {
		if (codePoint >= 0 && codePoint < cache.length) {
			return cache[codePoint];
		}
		if (Character.isValidCodePoint(codePoint)) {
			return new CharName(codePoint);
		}
		throw new IllegalArgumentException(String.format("Invalid code point: %d", codePoint));
	}
	
	private final int codePoint;
	
	private CharName(final int codePoint) {
		this.codePoint = codePoint;
	}
	
	public int getCodePoint() {
		return codePoint;
	}
	
	public String getCodePointChars() {
		return String.valueOf(Character.toChars(codePoint));
	}
	
	public byte[] getUtf8Bytes() {
		return getCodePointChars().getBytes(StandardCharsets.UTF_8);
	}
	
	public byte[] getUtf16Bytes() {
		return getCodePointChars().getBytes(StandardCharsets.UTF_16BE);
	}
	
	public String getCodePointString() {
		return String.format(String.format("U+%%0%dX", Utils.MAX_LENGTH), codePoint);
	}
	
	public String getCodePointShortString() {
		return "U+" + Integer.toHexString(codePoint).toUpperCase();
	}
	
	public Character.UnicodeScript getUnicodeScript() {
		return Character.UnicodeScript.of(codePoint);
	}
	
	public Character.UnicodeBlock getUnicodeBlock() {
		return Character.UnicodeBlock.of(codePoint);
	}
	
	public String getName() {
		return Character.getName(codePoint);
	}

	@Override
	public int hashCode() {
		return codePoint;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final CharName other = (CharName) obj;
		return codePoint == other.codePoint;
	}

	@Override
	public int compareTo(final CharName o) {
		return Integer.compare(codePoint, o.codePoint);
	}
}