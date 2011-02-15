package gov.nasa.jpf.symbc.string.translate;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;


public class Z3Interface {
	
	Process process;
	OutputStream stdin;
	InputStream stdout;
	BufferedReader brCleanUp;
	boolean sat;
	
	Map<String, String> answers;
	
	public Z3Interface () throws IOException{
		process = Runtime.getRuntime().exec("./lib/z3 -smt2 -in -m");
		stdin = process.getOutputStream();
		stdout = process.getInputStream();
		brCleanUp = new BufferedReader (new InputStreamReader (stdout));
	}
	
	public void sendMessage (String msg) throws IOException{
		sat = false;
		stdin.write((msg + "\n(exit)").getBytes());
		stdin.flush();
		answers = new HashMap<String, String>();
		String line = brCleanUp.readLine();
		//System.out.println("[Stdout] " + line);
		while (line != null) {
			if (line.equals ("sat")) {
				sat = true;
			}
			if (line.contains("ERROR")) {
				String oldline = line;
				line = brCleanUp.readLine();
				System.out.println(msg);
				throw new RuntimeException("Z3 encountered an error in its input: " + oldline + "\n" + line);
			}
			else if (line.startsWith("((\"model\" \"") && sat) {
				String temp = line.substring(11);
				process (temp);
				line = brCleanUp.readLine();
				while (line != null) {
					process (line);
					line = brCleanUp.readLine();
				}
				break;
			}
			line = brCleanUp.readLine();
			//System.out.println("[Stdout] " + line);
		}
	}
	
	public Map<String, String> getAns () {
		if (sat == true)
			return answers;
		else
			return null;
	}
	
	private void process (String line) {
		String words[] = line.split(" -> ");
		String varName = words[0];
		StringBuilder sb = new StringBuilder();
		for (int i = 2; i < words[1].length(); i++) {
			char c = words[1].charAt(i);
			if (Character.isDigit(c)) {
				sb.append (c);
			}
			else {
				break;
			}
		}
		BigInteger bi = new BigInteger(sb.toString());
		sb = new StringBuilder();
		for (int i = 1; i < 8 - bi.bitLength() % 8; i++) {
			sb.append ("0");
		}
		for (int i = bi.bitLength(); i >= 0; i--) {
			if (bi.testBit(i))
				sb.append("1");
			else
				sb.append("0");
		}
		answers.put(varName, sb.toString());
	}
	
	public static void main (String [] args) throws IOException{
		Z3Interface z3 = new Z3Interface();
		
		z3.sendMessage("(declare-fun a () (_ BitVec 32))\n(declare-fun b () (_ BitVec 32))\n(assert (= ((_ extract 7 0) a) (_ bv255 8)))\n(assert (= ((_ extract 15 8) a) (_ bv255 8)))\n(assert (= ((_ extract 15 8) b) ((_ extract 7 0) a)))\n(check-sat)\n(get-info model)\n");
		System.out.println("Answers: " + z3.getAns());
		
		z3.sendMessage("(declare-fun a () (_ BitVec 32))\n(declare-fun b () (_ BitVec 32))\n(assert (= ((_ extract 7 0) a) (_ bv255 8)))\n(assert (= ((_ extract 15 8) a) (_ bv255 8)))\n(assert (= ((_ extract 15 8) b) ((_ extract 15 8) a)))\n(check-sat)\n(get-info model)\n");
		System.out.println("Answers: " + z3.getAns());
	}
}