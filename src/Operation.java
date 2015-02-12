import java.util.ArrayList;

/**
 * @author Austin Bruch
 * CNT4007C Spring 2015
 * Programming Assignment 1
 * 
 * Operation class
 */

public class Operation {

	/**
	 * Instance variables that represent which operator is to be used, and the value of the four possible arguments
	 */
	private String operator;

	private ArrayList<Integer> integers;

	/**
	 * Default Constructor
	 * No instance variables are set
	 */
	public Operation() {
		this.operator = null;
		this.integers = new ArrayList<Integer>();
	}

	/**
	 * Constructor
	 * @param operator - String representing the operation to be performed
	 * @param integers - ArrayList<Integer> that contains all of the Integers to be used for the operation
	 */
	public Operation(String operator, ArrayList<Integer> integers) {
		this.operator = operator;
		this.integers = integers;
	}


	/**
	 * Getters and Setters for all instance variables
	 */
	public String getOperator() {
		return operator;
	}

	public void setOperator(String operation) {
		this.operator = operation;
	}

	public ArrayList<Integer> getIntegers() {
		return integers;
	}

	public void setIntegers(ArrayList<Integer> integers) {
		this.integers = integers;
	}

	/**
	 * Performs the operation specified by the operator and the Integer arguments
	 * @return integer result of the specified operation
	 */
	public Integer doOperation() {
		Integer toReturn = 0; // Integer container for the result to return

		if(operator.equals("add")) { // If the operator is addition
			for(Integer i : this.integers) {
				if(i != null) {
					toReturn += i; // Simply add each Integer to the others
				}
			}
		} else if(operator.equals("subtract")) { // If the operator is subtraction
			toReturn = this.integers.get(0); // Start with the first Integer
			for(int i = 1; i < this.integers.size(); i++) {
				if(this.integers.get(i) != null) {
					toReturn -= this.integers.get(i); // Subtract each subsequent Integer from the previous total
				}
			}
		} else if(operator.equals("multiply")) { // If the operator is multiplication
			toReturn = 1; // Start with 1 since 1 * X = X
			for(Integer i : this.integers) { 
				if(i != null) {
					toReturn *= i; // Simply multiply each Integer together
				}
			}
		}

		return toReturn;
	}

}