package com.MORTlib.Test.Swerve;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;

import com.MORTlib.Test.Hardware.ctre.CTREUtility.Falcon500;
import com.MORTlib.Test.Hardware.ctre.CTREUtility.Krakenx60;
import com.MORTlib.Test.Hardware.rev.RevUtility.NEO;
import com.MORTlib.Test.Hardware.rev.RevUtility.NEO550;
import com.MORTlib.Test.Hardware.Motor;
import com.MORTlib.Test.Hardware.PIDMotor;
import com.MORTlib.Test.Hardware.MotorIntf;
import com.MORTlib.Test.Hardware.MotorTypeEnum;
import com.MORTlib.Test.Hardware.Encoder;
import com.MORTlib.Test.Hardware.EncoderIntf;
import com.MORTlib.Test.Hardware.EncoderTypeEnum;
import static com.MORTlib.Test.Swerve.Constants.*;

public class SwerveModule {

    public int driveMotorID;
    public int steerMotorID;
    public int encoderID;

    public MotorTypeEnum driveMotorType;
    public MotorTypeEnum steerMotorType;
    public EncoderTypeEnum encoderType;
    public ModuleTypeEnum moduleType;

    public MotorIntf driveMotor;
    public MotorIntf steerMotor;
    public EncoderIntf encoder;

    public double maxSpeed;
    public double maxVoltage;
    public double rotationToMeters;

    public double offset;

    public SwerveModuleState state;

    public SwerveModule(MotorTypeEnum driveMotorType, int driveMotorID, 
            MotorTypeEnum steerMotorType, int steerMotorID, 
            EncoderTypeEnum encoderType, int encoderID,
            ModuleTypeEnum moduleType
        ) {
        this.driveMotorID = driveMotorID;
        this.driveMotorID = driveMotorID;
        this.encoderID = encoderID;

        this.driveMotorType = driveMotorType;
        this.steerMotorType = steerMotorType;
        this.encoderType = encoderType;
        this.moduleType = moduleType;

        driveMotor = new Motor(driveMotorType, driveMotorID);
        steerMotor = new PIDMotor(steerMotorType, steerMotorID);
        encoder = new Encoder(encoderType, encoderID);

        maxSpeed = Math.PI / 60;
        maxVoltage = 12;
        offset = 0;
        rotationToMeters = Math.PI;

        switch(driveMotorType) {
            case NEO:
                maxSpeed = maxSpeed * NEO.MAX_RPM;
                break;
            case NEO550:
                maxSpeed = maxSpeed * NEO550.MAX_RPM;
                break;
            case FALCON:
                maxSpeed = maxSpeed * Falcon500.MAX_RPM;
                break;
            case KRAKEN:
                maxSpeed = maxSpeed * Krakenx60.MAX_RPM;
                break;
        }

        switch (steerMotorType) {
            case NEO:
                steerMotor.setPIDValues(
                    NEO.SWERVE_STEER_KP, 
                    NEO.SWERVE_STEER_KI, 
                    NEO.SWERVE_STEER_KD
                );
                break;
            case NEO550:
                steerMotor.setPIDValues(
                    NEO550.SWERVE_STEER_KP, 
                    NEO550.SWERVE_STEER_KI, 
                    NEO550.SWERVE_STEER_KD
                );
                break;
            case FALCON:
                steerMotor.setPIDValues(
                    Falcon500.SWERVE_STEER_KP, 
                    Falcon500.SWERVE_STEER_KI, 
                    Falcon500.SWERVE_STEER_KD
                );
                break;
            case KRAKEN:
                steerMotor.setPIDValues(
                    Krakenx60.SWERVE_STEER_KP, 
                    Krakenx60.SWERVE_STEER_KI, 
                    Krakenx60.SWERVE_STEER_KD
                );
                break;
        }

        switch (moduleType) {
            case MK4i:
                maxSpeed = maxSpeed * MK4i.WHEEL_DIAMETER * MK4i.DRIVE_REDUCTION;
                rotationToMeters = rotationToMeters * MK4i.WHEEL_DIAMETER * MK4i.DRIVE_REDUCTION;
        }
    }

    public void setCurrentLimits(double limit) {
        driveMotor.setCurrentLimit(limit);
        steerMotor.setCurrentLimit(limit);
    }

    public void setPosition(Rotation2d setpoint) {
        setPositionD(setpoint.getDegrees());
    }

    public void setPositionD(double setpoint) {
        steerMotor.setPositionD((encoder.getPositionD() - offset), setpoint);
    }

    public void setDrivePercent(double percent) {
        driveMotor.setPercent(percent);
    }

    public void setDriveVoltage(double voltage) {
        driveMotor.setVoltage(voltage);
    }

    public void setDriveSpeedMeters(double speedMeters) {
        driveMotor.setVoltage((speedMeters / maxSpeed) * maxVoltage);
    }

    public void setModuleState(SwerveModuleState state) {
        this.state = state;

        SwerveModuleState.optimize(state, getEncoderPosition());
        setDriveSpeedMeters(state.speedMetersPerSecond);
        setPosition(state.angle);
    }

    public void setMaxVoltage(double maxVoltage) {
        this.maxVoltage = maxVoltage;
    }

    public void setOffset(double offset) {
        this.offset = offset;
    }



    public Rotation2d getEncoderPosition() {
        return Rotation2d.fromDegrees(getEncoderPositionD());
    }

    public double getEncoderPositionD() {
        return encoder.getPositionD() - offset;
    }

    public double getDrivePosition() {
        return driveMotor.getPosition();
    }

    public double getDriveVelocityD() {
        return driveMotor.getVelocityD();
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public double getMaxVoltage() {
        return maxVoltage;
    }

    public SwerveModuleState getModuleState() {
        return state;
    }

    public SwerveModulePosition getPosition() {
        return new SwerveModulePosition(
            (driveMotor.getPosition() * rotationToMeters), 
            Rotation2d.fromDegrees(getEncoderPositionD())
        );
    }



    public MotorIntf getDriveMotor() {
        return driveMotor;
    }
    
    public MotorIntf getSteerMotor() {
        return steerMotor;
    }

    public EncoderIntf getEncoder() {
        return encoder;
    }



    public MotorTypeEnum getDriveMotorType() {
        return driveMotorType;
    }
    
    public MotorTypeEnum getSteerMotorType() {
        return steerMotorType;
    }

    public EncoderTypeEnum getEncoderType() {
        return encoderType;
    }

    public ModuleTypeEnum getModuleType() {
        return moduleType;
    }

}