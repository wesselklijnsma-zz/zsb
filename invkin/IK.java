/*
 * Names       : Jouke van der Maas & Wessel Klijnsma
 * Student IDs : 10186883 and 10172432
 * Description : Inverse kinematics for controlling a robot arm.
 * Date        : June 2012
 * Comments    : The structure of this file was provided by Nikos Massios
 *   and Matthijs Spaan.
 */

import java.lang.*;
import java.util.*;

/**
 * Converts a list of positions to a list of joint angles of the robot arm.
 */
public class IK
{
	
	// Class containing the Denavit-Hartenberg representation of the robot arm
	private static RobotJoints robotJoints;
	
	/**
	 * Calculate roll, pitch, and yaw for the gripper of the robot arm plus the
	 * value of the gripper itself.
	 */
	private static void handJointCalculation(GripperPosition pos, JointValues j)
	{
		j.roll = pos.theta;
		j.pitch = -90.0;
		j.yaw = 0.0;
		j.grip = pos.grip;
	}
	
	/**
	 * Calculate the wrist coordinates from the hand coordinates. If the robot's
	 * last link has some length and the tip of the robot grabs the piece. At
	 * what height is the start of the robot's last link?
	 */
	private static Point wristCoordinatesCalculation(GripperPosition pos)
	{
		
		Point c = new Point(pos.coords.x, pos.coords.y, pos.coords.z + 189);
		return (c);
	}
	
	/**
	 * Calculate the arm joints from the (x,y,z) coordinates of the wrist (the
	 * last link).
	 */
	private static void armJointCalculation(Point wristCoords, JointValues j)
	{
		j.zed = wristCoords.z + 175;
		
		double x = wristCoords.y; // - 67.5;
		double y = wristCoords.x;
		double c = (Math.pow(x, 2) + Math.pow(y, 2) - Math.pow(253.5, 2) - Math
				.pow(253.5, 2)) / (2 * Math.pow(253.5, 2));
		double s = Math.sqrt(1 - Math.pow(c, 2));
		
		j.shoulder = Math.toDegrees(Math.atan2(y, x)
				- Math.atan2(253.5 * s, 253.5 + 253.5 * c));
		j.elbow = Math.toDegrees(Math.atan2(s, c));
	}
	
	/**
	 * Calculate the appropriate values for all joints for position pos.
	 */
	private static JointValues jointCalculation(GripperPosition pos)
	{
		JointValues j = new JointValues();
		Point wristCoords;
		
		handJointCalculation(pos, j);
		wristCoords = wristCoordinatesCalculation(pos);
		armJointCalculation(wristCoords, j);
		
		return (j);
	}
	
	private static void inverseKinematics(Vector<GripperPosition> p,
			Vector<JointValues> j)
	{
		
		// initialize the Denavit-Hartenberg representation
		robotJoints = new RobotJoints();
		
		for (int i = 0; i < p.size(); i++)
		{
			GripperPosition pos = (GripperPosition) p.elementAt(i);
			/* correct for errors in the arm */
			// if left on the board then assume left-hand configuration
			// if right on the board then assume right-hand configuration
			if (pos.coords.x < 0)
				RobotJoints.correctCartesian(pos, 0);
			else
				RobotJoints.correctCartesian(pos, 1);
			j.addElement(jointCalculation(pos));
		}
	}
	
	public static void main(String[] args)
	{
		Vector<GripperPosition> p = new Vector<GripperPosition>();
		Vector<JointValues> j = new Vector<JointValues>();
		
		System.out.println("**** THIS IS THE STUDENT IK MODULE IN JAVA\n");
		
		// read the gripper positions as produced by PP.java
		GripperPosition.read(p);
		
		inverseKinematics(p, j);
		
		for (int i = 0; i < j.size(); i++)
			System.out.println((JointValues) j.get(i));
		
		JointValues.write(j);
	}
}
