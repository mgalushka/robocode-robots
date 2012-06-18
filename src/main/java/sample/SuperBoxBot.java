package sample;

import java.awt.Color;
import robocode.AdvancedRobot;
import robocode.HitByBulletEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

/*
* SuperBoxBot - a SuperSampleBot by Exauge
*
* This robot demonstrates basic melee strategy such as
*  -Picking targets
*  -Melee movement strategy
*
*  It also demonstrates some of the Java API operators including
*  remainder and ternary.
*
*  If you need help with any of these operators look here:
*  http://download.oracle.com/javase/tutorial/java/nutsandbolts/opsummary.html
*  A bit of googling always helps too.
*
* Movement:
* This robot goes to the nearest corner, and then moves in a box.
* There is a 15% chance it will change direction when the enemy fires.
*
* Targeting:
* At first this robot will target the first robot it sees, but it switches
* to any robot that hits it. It uses Head-on targeting which is popular
* among lightweight melee bots.
*/
public class SuperBoxBot extends AdvancedRobot{

    private boolean moved = false; // if we need to move or turn
    private boolean inCorner = false; // if we are in a corner
    private String targ; // what robot to target
    private byte spins = 0; // spin counter
    private byte dir = 1; // direction to move
    private short prevE; // previous energy of robot we're targeting

    @Override
    public void run(){
        setColors(Color.PINK, Color.BLACK, Color.CYAN); // set the colors
        setAdjustGunForRobotTurn(true); // when the robot turns, adjust gun in opposite dir
        setAdjustRadarForGunTurn(true); // when the gun turns, adjust radar in opposite dir
        while(true){ // for radar lock (aka "Narrow Lock")
            turnRadarLeftRadians(1); // continually turn the radar left
        }
    }

    @Override
    public void onHitByBullet(HitByBulletEvent e){ // if hit buy a bullet
        targ = e.getName(); // target the one who hit us!
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e){
        if(targ == null || spins > 6){ // if we don't have a target
            targ = e.getName(); // choose the first robot scanned
        }

        if(getDistanceRemaining() == 0 && getTurnRemaining() == 0){ // not moving or turning
            if(inCorner){
                if(moved){ // if last movement cycle we were moving,
                    setTurnLeft(90); // turn this cycle
                    moved = false; // and move next cycle
                }
                else{ // else if last cycle we were turning
                    setAhead(160 * dir); // move this cycle
                    moved = true; // and turn next cycle
                }
            }
            else{
                // if we aren't going N/S go north or south
                if((getHeading() % 90) != 0){
                    setTurnLeft((getY() > (getBattleFieldHeight() / 2)) ? getHeading()
                            : getHeading() - 180);
                }
                // if we aren't at the top or bottom, go to whichever is closer
                else if(getY() > 30 && getY() < getBattleFieldHeight() - 30){
                    setAhead(getHeading() > 90 ? getY() - 20 : getBattleFieldHeight() - getY()
                            - 20);
                }
                // if we aren't facing toward East/West, face toward it
                else if(getHeading() != 90 && getHeading() != 270){
                    if(getX() < 350){
                        setTurnLeft(getY() > 300 ? 90 : -90);
                    }
                    else{
                        setTurnLeft(getY() > 300? -90 : 90);
                    }
                }
                // if we aren't at the left or right, go to whichever is closer
                else if(getX() > 30 && getX() < getBattleFieldWidth() - 30){
                    setAhead(getHeading() < 180 ? getX() - 20 : getBattleFieldWidth() - getX()
                            - 20);
                }
                // we are in the corner; turn and start moving
                else if(getHeading() == 270){
                    setTurnLeft(getY() > 200 ? 90 : 180);
                    inCorner = true;
                }
                // we are in the corner; turn and start moving
                else if(getHeading() == 90){
                    setTurnLeft(getY() > 200 ? 180 : 90);
                    inCorner = true;
                }
            }
        }
        if(e.getName().equals(targ)){ // if the robot scanned is our target
            spins = 0; // reset radar spin counter

            // if the enemy fires, with a 15% chance,
            if((prevE < (prevE = (short)e.getEnergy())) && Math.random() > .85){
                dir *= -1; // change direction
            }

            setTurnGunRightRadians(Utils.normalRelativeAngle((getHeadingRadians() + e
                    .getBearingRadians()) - getGunHeadingRadians())); // move gun toward them

            if(e.getDistance() < 200){ // the the enemy is further than 200px
                setFire(3); // fire full power
            }
            else{
                setFire(2.4); // else fire 2.4
            }

            double radarTurn = getHeadingRadians() + e.getBearingRadians()
                    - getRadarHeadingRadians();
            setTurnRadarRightRadians(2 * Utils.normalRelativeAngle(radarTurn)); // lock radar
        }
        else if(targ != null){ // else
            spins++; // add one to spin count
        }
    }

}