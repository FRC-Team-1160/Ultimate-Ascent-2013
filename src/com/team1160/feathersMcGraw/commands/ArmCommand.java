package com.team1160.feathersMcGraw.commands;


public class ArmCommand {
	public double velocity;      // Holds the data for direction
	public double grip;        // Holds data for servo pos on the gripper
	
	public String toString(){                         // Print function helps a lot with debugging
		String output = "";                           // Initialize string
		output += "Arm Command \n";                   // Lable for legibility
		output += "Velocity: " + velocity + "\n";     // The velocity
		output += "Grip: " + grip + "\n";    		  // The grip
		return output;                                // Return the string
	}
	
	
}
