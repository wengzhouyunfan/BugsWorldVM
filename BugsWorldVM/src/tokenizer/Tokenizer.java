package tokenizer;
import static components.utilities.Tokenizer.isCondition;
import static components.utilities.Tokenizer.isIdentifier;
import static components.utilities.Tokenizer.isKeyword;

import components.queue.Queue;
import components.queue.Queue1L;
import components.set.Set;
import components.set.Set1L;
import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;

/**
 * {@code Tokenizer} utility class with methods to tokenize an input stream and
 * to perform various checks on tokens.
 */
public final class Tokenizer {

	/*
	 * Private members --------------------------------------------------------
	 */

	/**
	 * Definition of whitespace separators.
	 */
	private static final String SEPARATORS = " \t\n\r";

	/**
	 * Private constructor so this utility class cannot be instantiated.
	 */
	private Tokenizer() {
	}

	/**
	 * Returns the token "kind" corresponding to the given {@code token}.
	 *
	 * @param token the given token
	 * @return the "kind" of the given token
	 * @ensures tokenKind = ["kind" of the given token]
	 */
	private static String tokenKind(String token) {
		assert token != null : "Violation of: token is not null";
		String kind = "ERROR     ";
		if (isKeyword(token)) {
			kind = "KEYWORD   ";
		} else if (isCondition(token)) {
			kind = "CONDITION ";
		} else if (isIdentifier(token)) {
			kind = "IDENTIFIER";
		}
		return kind;
	}

	/**
	 * Returns the first "word" (maximal length string of characters not in
	 * {@code SEPARATORS}) or "separator string" (maximal length string of
	 * characters in {@code SEPARATORS}) in the given {@code text} starting at the
	 * given {@code position}.
	 *
	 * @param text     the {@code String} from which to get the word or separator
	 *                 string
	 * @param position the starting index
	 * @return the first word or separator string found in {@code text} starting at
	 *         index {@code position}
	 * @requires 0 <= position < |text|
	 * @ensures
	 * 
	 *          <pre>
	 * nextWordOrSeparator =
	 *   text[position, position + |nextWordOrSeparator|)  and
	 * if entries(text[position, position + 1)) intersection entries(SEPARATORS) = {}
	 * then
	 *   entries(nextWordOrSeparator) intersection entries(SEPARATORS) = {}  and
	 *   (position + |nextWordOrSeparator| = |text|  or
	 *    entries(text[position, position + |nextWordOrSeparator| + 1))
	 *      intersection entries(SEPARATORS) /= {})
	 * else
	 *   entries(nextWordOrSeparator) is subset of entries(SEPARATORS)  and
	 *   (position + |nextWordOrSeparator| = |text|  or
	 *    entries(text[position, position + |nextWordOrSeparator| + 1))
	 *      is not subset of entries(SEPARATORS))
	 *          </pre>
	 */
	private static String nextWordOrSeparator(String text, int position) {
		assert text != null : "Violation of: text is not null";
		assert 0 <= position : "Violation of: 0 <= position";
		assert position < text.length() : "Violation of: position < |text|";
		String result = "";
		Set<Character> separators = new Set1L<Character>();
		for (int i = 0; i < SEPARATORS.length(); i++) {
			char c = SEPARATORS.charAt(i);
			if (!separators.contains(c)) {
				separators.add(c);
			}
		}
		if (separators.contains(text.charAt(position))) {
			int i = 0;
			while (((position + i) < text.length()) && (separators.contains(text.charAt(position + i)))) {
				i++;
			}
			result = text.substring(position, position + i);
		} else {
			int i = 0;
			while (((position + i) < text.length()) && (!separators.contains(text.charAt(position + i)))) {
				i++;
			}
			result = text.substring(position, position + i);
		}
		return result;
	}

	/*
	 * Public members ---------------------------------------------------------
	 */

	/**
	 * Token to mark the end of the input. This token cannot come from the input
	 * stream because it contains whitespace.
	 */
	public static final String END_OF_INPUT = "### END OF INPUT ###";

	/**
	 * Tokenizes the entire input getting rid of all whitespace separators and
	 * returning the non-separator tokens in a {@code Queue<String>}.
	 *
	 * @param in the input stream
	 * @return the queue of tokens
	 * @requires in.is_open
	 * @ensures
	 * 
	 *          <pre>
	 * tokens =
	 *   [the non-whitespace tokens in #in.content] * <END_OF_INPUT>  and
	 * in.content = <>
	 *          </pre>
	 */
	public static Queue<String> tokens(SimpleReader in) {
		assert in != null : "Violation of: in is not null";
		assert in.isOpen() : "Violation of: in.is_open";
		Set<Character> separators = new Set1L<Character>();
		for (int i = 0; i < SEPARATORS.length(); i++) {
			char c = SEPARATORS.charAt(i);
			if (!separators.contains(c)) {
				separators.add(c);
			}
		}
		Queue<String> q = new Queue1L<String>();
		while (!in.atEOS()) {
			int pos = 0;
			String tmp = in.nextLine();
			while (pos < tmp.length()) {
				String token = nextWordOrSeparator(tmp, pos);
				if (!separators.contains(tmp.charAt(pos))) {
					q.enqueue(token);
				}
				pos += token.length();
			}
		}
		q.enqueue(END_OF_INPUT);
		return q;
	}

	/*
	 * Main test method -------------------------------------------------------
	 */

	/**
	 * Main method.
	 *
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		SimpleReader in = new SimpleReader1L();
		SimpleWriter out = new SimpleWriter1L();
		/*
		 * Get input file name
		 */
		out.print("Enter input file name: ");
		String fileName = in.nextLine();
		/*
		 * Tokenize input with Tokenizer implementation from library.
		 */
		SimpleReader file = new SimpleReader1L(fileName);
		Queue<String> q1 = components.utilities.Tokenizer.tokens(file);
		file.close();
		/*
		 * Tokenize input with Tokenizer implementation under test.
		 */
		file = new SimpleReader1L(fileName);
		Queue<String> q2 = Tokenizer.tokens(file);
		file.close();
		/*
		 * Check that the two Queues are equal.
		 */
		out.println();
		if (q2.equals(q1)) {
			out.println("Input appears to have been tokenized correctly.");
		} else {
			out.println("Error: input tokens are not correct.");
		}
		out.println();
		/*
		 * Generate expected output in file "data/expected-output.txt"
		 */
		out.println("*** Generating expected output ***");
		SimpleWriter tOut = new SimpleWriter1L("data/expected-output.txt");
		for (String token : q1) {
			tOut.println(tokenKind(token) + ": <" + token + ">");
		}
		tOut.close();
		/*
		 * Generate actual output in file "data/actual-output.txt"
		 */
		out.println("*** Generating actual output ***");
		tOut = new SimpleWriter1L("data/actual-output.txt");
		for (String token : q2) {
			tOut.println(tokenKind(token) + ": <" + token + ">");
		}
		tOut.close();

		in.close();
		out.close();
	}

}
