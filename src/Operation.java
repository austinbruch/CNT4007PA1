/**
 * @author Austin Bruch
 * CNT4007C Spring 2015
 * Programming Assignment 1
 * 
 * Operation class
 */

import java.util.ArrayList;

public class Operation {

	/**
	 * Instance variables that represent which operator is to be used, and the value of the four possible arguments
	 */
	private String operator;

	private boolean disconnectClient;
	private boolean terminateServer;

	private ArrayList<Integer> integers;

	/**
	 * Default Constructor
	 */
	public Operation() {
		this.operator = null;
		this.integers = null;
		this.disconnectClient = false;
		this.terminateServer = false;
	}

	/**
	 * Constructor
	 * @param operator - String representing the operation to be performed
	 * @param integers - ArrayList<Integer> that contains all of the Integers to be used for the operation
	 */
	public Operation(String operator, ArrayList<Integer> integers) {
		this.operator = operator;
		this.integers = integers;
		this.disconnectClient = false;
		this.terminateServer = false;
	}

	/**
	 * Used as flags for the ClientConnection to see if the operation was "bye" or "terminate"
	 * @return
	 */
	public boolean disconnectClient() {
		return this.disconnectClient;
	}

	public boolean terminateServer() {
		return this.terminateServer;
	}

	/**
	 * Getters and Setters for all instance variables
	 */
	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
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

		if(operator.equals("bye")) { // If the operator is bye
			toReturn = -5; // Return -5 as the result for the bye operation
			this.disconnectClient = true; // Need to disconnect this client
		} else if(operator.equals("terminate")) { // If the operator is terminate
			toReturn = -5; // Return -5 as the result for the terminate operation
			this.disconnectClient = true; // Need to disconnect this client 
			this.terminateServer = true; // Need to terminate the server
		} else if(operator.equals("add")) { // If the operator is addition
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