/**
 * @author Austin Bruch
 * CNT4007C Spring 2015
 * Programming Assignment 1
 * 
 * ErrorChecker class
 */

import java.util.ArrayList;
import java.util.StringTokenizer;

public class ErrorChecker {

	private static ArrayList<String> operators = null; // Static ArrayList of Strings that contain all valid operators

	/**
	 * Default Constructor
	 */
	public ErrorChecker() {
		if (operators == null) { // Make sure the operators list is populated
			operators = new ArrayList<String>();
			operators.add("add");
			operators.add("subtract");
			operators.add("multiply");
		}
	}

	/**
	 * Checks the given input String for errors, returns the error code
	 * @param input - String to be checked for errors
	 * @return errorCode - Integer representing the error code for the most significant error found
	 */
	public int checkForErrors(String input) {
		
		/**
		 * -1: incorrect operation command.
		 * -2: number of inputs is less than two.
		 * -3: number of inputs is more than four.
		 * -4: one or more of the inputs contain(s) non-number(s).
		 * -5: exit.
		 * 
		 * logic flow for error checking
		 * 
		 * check to see if the input is empty
		 * 		if it's empty, return error code -1
		 * 
		 * then check to see if the command is malformed
		 * 		if so, return error code -1
		 * 
		 * then check to see if the command has less than 2 inputs
		 * 		if so, return the error code -2
		 * 
		 * then check to see if the command has more than 4 inputs
		 * 		if so, return the error code -3
		 * 
		 * then check to see if the command uses non-number inputs
		 * 		if so, return the error code -4
		 * 
		 */

		int errorCode = 0;

		// Use nested if statements to handle precedence of errors
		if(checkEmptyInput(input)) {
			// The input doesn't have any parsable strings
			errorCode = -1;
		} else {
			if(checkInvalidOperation(input)) {
				// The operation is invalid
				errorCode = -1;
			} else {
				if(checkNotEnoughArguments(input)) {
					// There aren't enough arguments
					errorCode = -2;
				} else {
					if(checkTooManyArguments(input)) {
						// There are too many arguments
						errorCode = -3;
					} else {
						if(checkNumberFormat(input)) {
							// The input has at least one argument that is not an Integer 
							errorCode = -4;
						}
					}
				}
			}
		}

		return errorCode;

	}

	/**
	 * Returns true if the input String doesn't have any parsable tokens, false otherwise
	 * @param input String input to check
	 * @return whether or not the input string is empty
	 */
	private boolean checkEmptyInput(String input) {
		boolean toReturn = false;

		StringTokenizer tokenizer = new StringTokenizer(input, " ");

		if(tokenizer.countTokens() == 0) { // If there aren't any tokens available from the Tokenizer
			toReturn = true;
		}

		return toReturn;
	}

	/**
	 * Returns true if the operation in the input String is invalid, false otherwise
	 * @param input String input to check
	 * @return whether or not the input string contains a valid operation
	 */
	private boolean checkInvalidOperation(String input) {
		boolean toReturn = false;

		StringTokenizer tokenizer = new StringTokenizer(input, " ");
		String operator = tokenizer.nextToken();

		if(operator.equals("bye") || operator.equals("terminate")) {
			// The first string in the command input was either bye or terminate
			if(tokenizer.hasMoreTokens()) {
				// There should not be any additional strings in the command (no invalid arguments passed to either bye or terminate)
				toReturn = true;
			}
		} else if(!operators.contains(operator)) {
			// The list of accepted operators doesn't contain the operator specified by this command
			toReturn = true;
		}

		return toReturn;
	}

	/**
	 * Returns true if the input String doesn't have enough arguments, false otherwise
	 * @param input String input to check
	 * @return whether or not the input string has sufficient arguments
	 */
	private boolean checkNotEnoughArguments(String input) {
		boolean toReturn = false;

		StringTokenizer tokenizer = new StringTokenizer(input, " ");
		String operator = tokenizer.nextToken();

		if(operator.equals("bye") || operator.equals("terminate")) {
			// Don't check this for bye or terminate, as it's been handled in the checkInvalidOperation method
		} else if(tokenizer.countTokens() < 2) {
			// Since we've already called nextToken to get the operator, if it can't be called 2 more times, there's not enough arguments in the string
			toReturn = true;
		}

		return toReturn;
	}

	/**
	 * Returns true if the input String has too many arguments, false otherwise
	 * @param input String input to check
	 * @return whether or not the input string has too many arguments
	 */
	private boolean checkTooManyArguments(String input) {
		boolean toReturn = false;

		StringTokenizer tokenizer = new StringTokenizer(input, " ");
		tokenizer.nextToken();

		if(tokenizer.countTokens() > 4) {
			// Since we've already called nextToken to get the operator, if it can be called more than 4 more times, there's too many arguments in the string
			toReturn = true;
		}

		return toReturn;
	}

	/**
	 * Returns true if any of the arguments in the input String can't be parsed to Integers, false otherwise
	 * @param input String input to check
	 * @return whether or not the input string has invalid arguments (non-integers)
	 */
	private boolean checkNumberFormat(String input) {
		boolean toReturn = false;

		StringTokenizer tokenizer = new StringTokenizer(input, " ");
		tokenizer.nextToken();

		String temp = null;
		while(tokenizer.hasMoreTokens()) {
			temp = tokenizer.nextToken();
			// All of the remaining Strings need to be Integers
			try {
				Integer.parseInt(temp);
			} catch (NumberFormatException nfe) {
				// If parsing any String results in a NumberFormatException, we know the String isn't an Integer, and we can return true
				toReturn = true;
				break; // Only need to find the first one, don't need to see if there are more
			}
		}

		return toReturn;
	}

}