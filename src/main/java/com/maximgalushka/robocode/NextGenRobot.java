package com.maximgalushka.robocode;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import robocode.TurnCompleteCondition;

import java.awt.*;

/**
 * <p></p>
 *
 * @author Maxim Galushka
 * @since 15.06.12
 */
public class NextGenRobot extends AdvancedRobot {

    @Override
    public void run() {

        // Set colors
        setBodyColor(new Color(200, 18, 29));
        setGunColor(new Color(24, 44, 150));
        setRadarColor(new Color(255, 253, 199));
        setBulletColor(new Color(255, 40, 239));
        setScanColor(new Color(71, 223, 255));

        while(true){
            setAhead(100);
            setTurnRadarRight(360);
            waitFor(new TurnCompleteCondition(this));

            setBack(100);
            //setTurnRadarRight(360);
            waitFor(new TurnCompleteCondition(this));
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {

        double d = getHeading() - getGunHeading() + e.getBearing();
        System.out.printf(String.format("Enemy Bearing: [%f], My Heading: [%f], " +
                "My Gun Heading: [%f], projected turn: [%f]\n",
                e.getBearing(), getHeading(), getGunHeading(), d));

        //e.get
        if (d < 180){
            setTurnGunRight(d);
        }
        else{
            setTurnGunLeft(360 - d);
        }
        if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 10){
            setFire(Math.min(400 / e.getDistance(), 3));
        }
        waitFor(new TurnCompleteCondition(this));

    }
}
