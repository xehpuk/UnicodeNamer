package de.xehpuk.unicodenamer;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import javax.imageio.ImageIO;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

/**
 * @author xehpuk <xehpuk@netbeans.org>
 */
public class Utils {
	public static final int MAX_CODE_POINT = getMaxCodePoint();
	public static final int MAX_LENGTH = getMaxLength();
	private static final String PACKAGE_PATH = getPackagePath();
	private static final int COMPILER_VERSION = 7;
	
	private static int getMaxCodePoint() {
		try {
			final String specVersion = ManagementFactory.getRuntimeMXBean().getSpecVersion();
			final int version = Integer.parseInt(specVersion.substring(specVersion.indexOf('.')));
			return (version <= COMPILER_VERSION) ? Character.MAX_CODE_POINT : Character.class.getDeclaredField("MAX_CODE_POINT").getInt(null);
		} catch (final NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | StringIndexOutOfBoundsException e) {
			// ignore and fall back to version <= COMPILER_VERSION
			return Character.MAX_CODE_POINT;
		}
	}
	
	private static int getMaxLength() {
		return Integer.toHexString(MAX_CODE_POINT).length();
	}
	
	private static BufferedImage load(final String name) {
		try {
			final Class<Utils> clazz = Utils.class;
			return ImageIO.read(clazz.getResource(PACKAGE_PATH + name));
		} catch (final IOException ioe) {
			return null;
		}
	}

	public static BufferedImage loadIcon(final String name) {
		return load("icons/" + name);
	}

	private static String getPackagePath() {
		return '/' + Utils.class.getPackage().getName().replace('.', '/') + '/';
	}
	
	public static String encodeHtml(final String html) {
		final StringBuilder sb = new StringBuilder(html.length() << 1);
		for (int i = 0, m = html.length(); i < m; i++) {
			final char c = html.charAt(i);
			switch (c) {
				case '"':
					sb.append("&quot;");
					break;
				case '&':
					sb.append("&amp;");
					break;
				case '<':
					sb.append("&lt;");
					break;
				case '>':
					sb.append("&gt;");
					break;
				default:
					sb.append(c);
			}
		}
		return sb.toString();
	}
	
	public static void installFont(final String name) {
		final Font newFont = new FontUIResource(name, Font.PLAIN, 0);
		final Enumeration keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			final Object key = keys.nextElement();
			final Font oldFont = UIManager.getFont(key);
			if (oldFont != null) {
				UIManager.put(key, newFont.deriveFont(oldFont.getStyle(), oldFont.getSize2D()));
			}
		}
	}
	
	public static String hex(final byte[] bytes) {
		final int length = bytes.length;
		if (length == 0) {
			return "";
		}
		final StringBuilder sb = new StringBuilder(length * 3);
		for (final byte b : bytes) {
			sb.append(String.format("%02X ", b));
		}
		return sb.deleteCharAt(sb.length() - 1).toString();
	}
	
	public static String urlEncode(final byte[] bytes) {
		final int length = bytes.length;
		if (length == 0) {
			return "";
		}
		final StringBuilder sb = new StringBuilder(length * 3);
		for (final byte b : bytes) {
			sb.append(String.format("%%%02X", b));
		}
		return sb.toString();
	}

	private Utils() {
		// utility class
	}
}