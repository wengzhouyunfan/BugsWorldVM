package programGenerator;
import java.util.Iterator;

import components.map.Map;
import components.program.Program;
import components.program.Program1;
import components.sequence.Sequence;
import components.sequence.Sequence1L;
import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;
import components.statement.Statement;
import components.statement.StatementKernel.Condition;

/**
 * Layered implementation of secondary method {@code generatedCode} for
 * {@code Program}.
 *
 *
 */
public final class ProgramGenerator extends Program1 {

	/*
	 * Private members --------------------------------------------------------
	 */

	/**
	 * Checks that value, an object of type V, is not aliased with any value stored
	 * in a pair of map. The package-wide precondition, known as the "repeated
	 * argument rule", that no argument may be aliased with any other, does not
	 * apply to this method; hence, if one argument here is aliased with the other,
	 * this method is obliged to terminate normally, returning false. This behavior
	 * allows this method to be used to check for violations of the repeated
	 * argument rule.
	 *
	 * @param <K>   the type of the key in the map
	 * @param <V>   the type of the value in the map
	 * @param value the object of type V to be checked for aliasing in the map
	 * @param map   the map in which aliasing is to be checked
	 * @return whether value is aliased in map
	 * @ensures notAliased = [value is not aliased in map]
	 */
	private static <K, V> boolean notAliased(V value, Map<K, V> map) {
		assert value != null : "Violation of: value is not null";
		assert map != null : "Violation of: map is not null";

		boolean notAliased = true;
		Iterator<Map.Pair<K, V>> it = map.iterator();
		while (notAliased && it.hasNext()) {
			notAliased = value != it.next().value();
		}
		return notAliased;
	}

	/**
	 * Constructs into the given {@code Program} the program read from the given
	 * input file.
	 *
	 * @param fileName the name of the file containing the program
	 * @param p        the constructed program
	 * @replaces p
	 * @requires [fileName is the name of a file containing a valid BL program]
	 * @ensures p = [program from file fileName]
	 */
	private static void loadProgram(String fileName, Program p) {
		SimpleReader in = new SimpleReader1L(fileName);
		p.parse(in);
		in.close();
	}

	/**
	 * Saves the given compiled program {@code cp} to the given output file.
	 *
	 * @param fileName the name of the file containing the program
	 * @param cp       the compiled program
	 * @requires
	 * 
	 *           <pre>
	 * [fileName is the name of a file to be used to save the compiled program]
	 *           </pre>
	 * 
	 * @ensures [cp is saved to to file fileName prefixed by the length of cp]
	 */
	private static void saveCompiledProgram(String fileName, Sequence<Integer> cp) {
		SimpleWriter out = new SimpleWriter1L(fileName);
		out.println(cp.length());
		for (Integer i : cp) {
			out.println(i);
		}
		out.close();
	}

	/**
	 * Converts {@code Condition} into corresponding conditional jump instruction
	 * byte code.
	 *
	 * @param c the {@code Condition} to be converted
	 * @return the conditional jump instruction byte code corresponding to {@code c}
	 * @ensures
	 * 
	 *          <pre>
	 * conditionalJump =
	 *  [conditional jump instruction byte code corresponding to c]
	 *          </pre>
	 */
	private static Instruction conditionalJump(Condition c) {
		assert c != null : "Violation of: c is not null";
		Instruction result;
		switch (c) {
		case NEXT_IS_EMPTY: {
			result = Instruction.JUMP_IF_NOT_NEXT_IS_EMPTY;
			break;
		}
		case NEXT_IS_NOT_EMPTY: {
			result = Instruction.JUMP_IF_NOT_NEXT_IS_NOT_EMPTY;
			break;
		}
		case NEXT_IS_ENEMY: {
			result = Instruction.JUMP_IF_NOT_NEXT_IS_ENEMY;
			break;
		}
		case NEXT_IS_NOT_ENEMY: {
			result = Instruction.JUMP_IF_NOT_NEXT_IS_NOT_ENEMY;
			break;
		}
		case NEXT_IS_FRIEND: {
			result = Instruction.JUMP_IF_NOT_NEXT_IS_FRIEND;
			break;
		}
		case NEXT_IS_NOT_FRIEND: {
			result = Instruction.JUMP_IF_NOT_NEXT_IS_NOT_FRIEND;
			break;
		}
		case NEXT_IS_WALL: {
			result = Instruction.JUMP_IF_NOT_NEXT_IS_WALL;
			break;
		}
		case NEXT_IS_NOT_WALL: {
			result = Instruction.JUMP_IF_NOT_NEXT_IS_NOT_WALL;
			break;
		}
		case RANDOM: {
			result = Instruction.JUMP_IF_NOT_RANDOM;
			break;
		}
		default: { // case TRUE
			result = Instruction.JUMP_IF_NOT_TRUE;
			break;
		}
		}
		return result;
	}

	/**
	 * Generates the sequence of virtual machine instructions ("byte codes")
	 * corresponding to {@code s} and appends it at the end of {@code cp}.
	 *
	 * @param s       the {@code Statement} for which to generate code
	 * @param context the {@code Context} in which to find user defined instructions
	 * @param cp      the {@code Sequence} containing the generated code
	 * @updates cp
	 * @ensures
	 * 
	 *          <pre>
	 * if [all instructions called in s are either primitive or
	 *     defined in context]  and
	 *    [context does not include any calling cycles, i.e., recursion] then
	 *  cp = #cp * s[the sequence of virtual machine "byte codes" corresponding to s]
	 * else
	 *  [reports an appropriate error message to the console and terminates client]
	 *          </pre>
	 */
	private static void generateCodeForStatement(Statement s, Map<String, Statement> context, Sequence<Integer> cp) {
		assert s != null : "Violation of: s is not null";
		assert context != null : "Violation of: context is not null";
		assert cp != null : "Violation of: cp is not null";
		assert notAliased(s, context) : "Violation of: s is not " + "aliased in context (the repeated argument rule)";

		final int dummy = 0;

		switch (s.kind()) {
		case BLOCK: {
			Statement tmp = s.newInstance();
			for (int i = 0; i < s.lengthOfBlock(); i++) {
				tmp = s.removeFromBlock(i);
				generateCodeForStatement(tmp, context, cp);
				s.addToBlock(i, tmp);
			}
			break;
		}
		case IF: {
			Statement tmp = s.newInstance();
			Condition c = s.disassembleIf(tmp);
			cp.add(cp.length(), conditionalJump(c).byteCode());
			int jump = cp.length();
			cp.add(cp.length(), dummy);
			generateCodeForStatement(tmp, context, cp);
			cp.replaceEntry(jump, cp.length());
			s.assembleIf(c, tmp);
			break;
		}
		case IF_ELSE: {
			Statement tmp1 = s.newInstance();
			Statement tmp2 = s.newInstance();
			Condition c = s.disassembleIfElse(tmp1, tmp2);
			cp.add(cp.length(), conditionalJump(c).byteCode());
			int condJump = cp.length();
			cp.add(cp.length(), dummy);
			generateCodeForStatement(tmp1, context, cp);
			cp.add(cp.length(), Instruction.valueOf("JUMP").byteCode());
			int jump = cp.length();
			cp.add(cp.length(), dummy);
			cp.replaceEntry(condJump, cp.length());
			generateCodeForStatement(tmp2, context, cp);
			cp.replaceEntry(jump, cp.length());
			s.assembleIfElse(c, tmp1, tmp2);
			break;
		}
		case WHILE: {

			Statement tmp = s.newInstance();
			Condition c = s.disassembleWhile(tmp);
			int test = cp.length();
			cp.add(cp.length(), conditionalJump(c).byteCode());
			int jump = cp.length();
			cp.add(cp.length(), dummy);
			generateCodeForStatement(tmp, context, cp);
			cp.add(cp.length(), Instruction.valueOf("JUMP").byteCode());
			cp.add(cp.length(), test);
			s.assembleWhile(c, tmp);
			cp.replaceEntry(jump, cp.length());
			break;
		}
		case CALL: {

			String label = s.disassembleCall();
			if (context.hasKey(label)) {
				generateCodeForStatement(context.value(label), context.newInstance(), cp);
			} else {
				label = label.toUpperCase();
				cp.add(cp.length(), Instruction.valueOf(label).byteCode());
				label = label.toLowerCase();
			}
			s.assembleCall(label);

			break;
		}
		default: {
			// this will never happen...
			break;
		}
		}
	}

	/*
	 * Constructors -----------------------------------------------------------
	 */

	/**
	 * Default constructor.
	 */
	public ProgramGenerator() {
		super();
	}

	/*
	 * Public methods ---------------------------------------------------------
	 */

	@Override
	public Sequence<Integer> generatedCode() {
		Sequence<Integer> cp = new Sequence1L<Integer>();
		
		return cp;
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
		out.print("Enter valid BL program file name: ");
		String fileName = in.nextLine();
		/*
		 * Generate expected output in file "data/expected-output.txt"
		 */
		out.println("*** Generating expected output ***");
		Program p1 = new Program1();
		loadProgram(fileName, p1);
		Sequence<Integer> cp1 = p1.generatedCode();
		saveCompiledProgram("data/expected-output.txt", cp1);
		/*
		 * Disassemble generated code
		 */
		out.println("*** Expected disassembled output ***");
		Program1.disassembleProgram(out, cp1);
		/*
		 * Generate actual output in file "data/actual-output.txt"
		 */
		out.println("*** Generating actual output ***");
		Program p2 = new ProgramGenerator();
		loadProgram(fileName, p2);
		Sequence<Integer> cp2 = p2.generatedCode();
		saveCompiledProgram("data/actual-output.txt", cp2);
		/*
		 * Disassemble generated code
		 */
		out.println("*** Actual disassembled output ***");
		Program1.disassembleProgram(out, cp2);
		/*
		 * Check that generatedCode restored the value of the program
		 */
		out.println();
		if (p2.equals(p1)) {
			out.println("Program value restored correctly.");
		} else {
			out.println("Error: program value was not restored.");
		}

		in.close();
		out.close();
	}

}
