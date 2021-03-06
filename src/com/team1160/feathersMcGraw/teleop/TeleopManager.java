package com.team1160.feathersMcGraw.teleop;

import com.sun.squawk.util.MathUtils;
import com.team1160.feathersMcGraw.api.Constants;
import com.team1160.feathersMcGraw.commands.PulleyCommand;
import com.team1160.feathersMcGraw.commands.RobotCommand;
import com.team1160.feathersMcGraw.input.InputState;

public class TeleopManager {
private static TeleopManager _INSTANCE;
	
	private RobotCommand currentCommand;
	private InputState inputState;
	
    /*
     * These are all the variables needed
     * to do the math to compute auto climb
     * I am trying to come up with a way to clean them up
     */
	
	protected double k1;
	protected double k2;
	protected double k3;
	protected double k4;
	protected double k5;
	protected double a;
	protected double b;
	protected double c;
	protected double x2;
	protected double m2;
	protected double beta;
                  
	protected double j1;
	protected double j2;
	protected double j3;
	protected double j4;
	protected double j5;
	protected double j6;
	protected double j7;
	
	protected double lastRightServo;
	protected double lastLeftServo;
	protected double lastMidServo;
	
	protected boolean rightSwap;
	protected boolean leftSwap;
	protected boolean midSwap;
	
	public static TeleopManager getInstance(){
		if(_INSTANCE == null){
			_INSTANCE =  new TeleopManager();
		}
		return _INSTANCE;
	}
	
	private TeleopManager() {
		currentCommand = new RobotCommand();
		inputState = new InputState();
		k2 = 30.75;
		k3 = 12.1;
	}
	
	public RobotCommand getCommand(InputState inputState){
		this.inputState = inputState;
		forgeArmCommand();
		forgeDriveCommand();
		forgePulleySystemCommand();
		return currentCommand;
	}
    	
	private void forgeDriveCommand(){
		if(inputState.toggleBoard.driveToggle[0]){
			currentCommand.drivetrainCommand.left = inputState.driveStick.x + inputState.driveStick.y;
			currentCommand.drivetrainCommand.right = inputState.driveStick.x - inputState.driveStick.y;
		}else{
			currentCommand.drivetrainCommand.left = 0;
			currentCommand.drivetrainCommand.right = 0;
		}
	}
	              
	private double getTapeAngle(boolean floor, boolean middle, double frameAngle, double T){ 
		double rFrameAngle = Math.toRadians(frameAngle);
		if(floor){
			if(middle){
				return 0;   //TODO might need to put not 0
			}else if(T < 11.1){    // The break free condition
				return 90-frameAngle;  
			}else{                //If you havent broken free
				if(frameAngle <= 20){
					return Math.toDegrees(MathUtils.asin((k2-12.3*Math.sin(rFrameAngle))/T));  // Rear wheel touching
				}else{
					return Math.toDegrees(MathUtils.asin((k2-18.9*Math.sin(rFrameAngle))/T));  // rear bumper
				}	
			}
		}else{   // Between rungs... not floor
			
			// Breaking free 
			if(T<16.3){    //TODO add angle condition once found
				return 90-frameAngle;
			}
			if(middle){
				k1 = 9.375;
			}
			else{
				k1 = 1.125;                    		  
			}
			if(frameAngle < 92 && frameAngle > 88){
				m2 = MathUtils.acos((k3+k1)/T);
			}else if(frameAngle < 2 && frameAngle > -2 ){
				if(T<=Math.sqrt(MathUtils.pow(k2-k1, 2) + MathUtils.pow(k3+k1, 2))){
					m2 = MathUtils.asin((k2-k1)/T);
				}else{
					m2 = MathUtils.acos((k3+k1)/T);
				}
			}else{    //Ugly quad stuff no need to do unless not 90 or 0 (frame angle)
				k5 = 1/Math.tan(rFrameAngle);
				a = (1+MathUtils.pow(k5, 2));
				k4 = k1/Math.sin(rFrameAngle);
				b = -2*(k2 + k4*k5 + k3*k5);
				c = MathUtils.pow(k2, 2)+MathUtils.pow(k3, 2)+MathUtils.pow(k4, 2)+2*(k3*k4) - MathUtils.pow(T,2);
				x2 = (-b-Math.sqrt(((b*b)-(4*a*c))))/(2*a);
			}
			
			return Math.toDegrees(m2)-frameAngle;
		}
                      
	}
	              
//	private double getServoAngle(double tapeAngle, double T, boolean middle){
//		double rTapeAngle = Math.toRadians(tapeAngle);
//		if(middle){
//			return 40-Math.toDegrees(MathUtils.atan((4.5-T*Math.sin(rTapeAngle)/T*Math.cos(rTapeAngle))+1.52*T));
//		}else{
//			return 59-Math.toDegrees(MathUtils.atan((4.6-T*Math.sin(rTapeAngle)/(3+T*Math.cos(rTapeAngle)))+1.94*T));
//		}
//		
//	}
	
	private double getServoAngle(double tapeAngle, int side){
		/*
		 * 1 = right
		 * 2 = middle
		 * 3 = left
		 */
		tapeAngle = Math.toRadians(tapeAngle);
		if(side == 1){
			j2 = Math.sin(tapeAngle)*inputState.sensorState.tapeLengthRight-4.5;
			j3 = Math.cos(tapeAngle)*inputState.sensorState.tapeLengthRight+2.5;
			j4 = Math.sqrt((j2*j2)+(j3*j3));
			j5 = 134-2.046*j4;
			j6 = Math.toDegrees(MathUtils.atan(j2/j3));
			j7 = j5+j6;
			return  1.296-.0037*j7;
		}else if(side == 2){
			j2 = Math.sin(tapeAngle)*inputState.sensorState.tapeLengthTop-4.5;
			j3 = Math.cos(tapeAngle)*inputState.sensorState.tapeLengthTop;
			j4 = Math.sqrt((j2*j2)+(j3*j3));
			j5 = 140-1.88*j4;
			j6 = Math.toDegrees(MathUtils.atan(j2/j3));
			j7 = j5+j6;
			return  -.0033*j7+.79;
		}else if(side == 3){
			System.out.println(Math.toDegrees(tapeAngle));
			j2 = Math.sin(tapeAngle)*inputState.sensorState.tapeLengthLeft-4.5;
			j3 = Math.cos(tapeAngle)*inputState.sensorState.tapeLengthLeft+2.5;
			j4 = Math.sqrt((j2*j2)+(j3*j3));
			j5 = 134-2.046*j4;
			j6 = Math.toDegrees(MathUtils.atan(j2/j3));
			j7 = j5+j6;
		return -.0248+.0033*j7;
		}
		
		return 1337;  //bs return... this shouldnt get hit ever... just to make my ide shut up about return problems -.-
	} 
	
	private void forgePulleySystemCommand(){
		currentCommand.pulleySystemCommand.left = forgePulleyCommand(inputState.toggleBoard.leftPulleyToggle[0], inputState.toggleBoard.leftLockToggle[0], inputState.leftArmStick.y, inputState.toggleBoard.leftAutoClimbToggle[0], 3, inputState.sensorState.tapeLengthLeft,currentCommand.pulleySystemCommand.left,1);
		currentCommand.pulleySystemCommand.right = forgePulleyCommand(inputState.toggleBoard.rightPulleyToggle[0], inputState.toggleBoard.rightLockToggle[0], inputState.rightArmStick.y, inputState.toggleBoard.rightAutoClimbToggle[0], 1, inputState.sensorState.tapeLengthRight,currentCommand.pulleySystemCommand.right, -1);
		if(inputState.toggleBoard.pulleyToggle[0]){
			currentCommand.pulleySystemCommand.top = forgePulleyCommand(inputState.toggleBoard.topPulleyToggle[0], false, inputState.driveStick.y, inputState.toggleBoard.topAutoClimbToggle[0],2, inputState.sensorState.tapeLengthTop,currentCommand.pulleySystemCommand.top,1);
		}

		currentCommand.pulleySystemCommand.top.angle = Math.max(Math.min(currentCommand.pulleySystemCommand.top.angle, Constants.P_TOP_MAX), Constants.P_TOP_MIN);
		currentCommand.pulleySystemCommand.left.angle = Math.max(Math.min(currentCommand.pulleySystemCommand.left.angle, Constants.P_LEFT_MAX), Constants.P_LEFT_MIN);
		currentCommand.pulleySystemCommand.right.angle = Math.max(Math.min(currentCommand.pulleySystemCommand.right.angle, Constants.P_RIGHT_MAX), Constants.P_RIGHT_MIN);
	}
	
	private PulleyCommand forgePulleyCommand(boolean pulleyMode, boolean lock, double y, boolean auto, int side, double T, PulleyCommand command, int mult){
		/*
		 * mult is inteaded to be used as a -1 -> 1 to invert controls because well you know....
		 * 
		 */
		if(!auto){
			y = y*mult;
			if(!pulleyMode){
				command.angle = (y+1)/2;
				command.velocity = 0;
			}else{
				command.velocity = -y;
			}
			
		}else{
			command.angle = getServoAngle(getTapeAngle(inputState.toggleBoard.floorToggle[0], side==2, inputState.sensorState.robotAngle,T), side);
			command.velocity = -y*mult;
		}
		command.locked = lock;
		return command;
	}
        
	private void forgeArmCommand(){
		if(inputState.toggleBoard.hutchArmToggle[0]){
			currentCommand.armCommand.velocity = inputState.driveStick.y;
		}else{
			currentCommand.armCommand.velocity = 0;
		}
		
		if(inputState.toggleBoard.gripToggle[0]){
			currentCommand.armCommand.grip = Constants.G_SERVO_GRIP;
		}else{
			currentCommand.armCommand.grip = Constants.G_SERVO_OPEN;
		}
	}
	
        
	public String toString(){
		String output = "";
		output+= currentCommand;
		return output;
	}
	
}