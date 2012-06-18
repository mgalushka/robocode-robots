package sample;

import java.awt.Color;
import java.awt.geom.Point2D;

import robocode.*;
import robocode.util.Utils;
/**
 * SuperSpinBot - a Super Sample Robot by CrazyBassoonist based on the robot RamFire by Mathew Nelson and maintained by Flemming N. Larsen.
 *
 * This robot tries to ram it's opponents.
 *
 */
public class SuperRamFire extends AdvancedRobot {

    //These are constants. One advantage of these is that the logic in them (such as 20-3*BULLET_POWER)
    //does not use codespace, making them cheaper than putting the logic in the actual code.

    final static double BULLET_POWER=3;//Our bulletpower.
    final static double BULLET_DAMAGE=BULLET_POWER*4;//Formula for bullet damage.
    final static double BULLET_SPEED=20-3*BULLET_POWER;//Formula for bullet speed.

    //Variables
    static double dir=1;
    static double oldEnemyHeading;
    static double enemyEnergy;


    public void run(){

        //RamFire Colors
        setBodyColor(Color.lightGray);
        setGunColor(Color.gray);
        setRadarColor(Color.darkGray);

        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
    }
    public void onScannedRobot(ScannedRobotEvent e){
        double absBearing=e.getBearingRadians()+getHeadingRadians();

        //This makes the amount we want to turn be perpendicular to the enemy.
        double turn=absBearing+Math.PI/2;

        //This formula is used because the 1/e.getDistance() means that as we get closer to the enemy, we will turn to them more sharply.
        //We want to do this because it reduces our chances of being defeated before we reach the enemy robot.
        turn-=Math.max(0.5,(1/e.getDistance())*100)*dir;

        setTurnRightRadians(Utils.normalRelativeAngle(turn-getHeadingRadians()));

        //This block of code detects when an opponents energy drops.
        if(enemyEnergy>(enemyEnergy=e.getEnergy())){

            //We use 300/e.getDistance() to decide if we want to change directions.
            //This means that we will be less likely to reverse right as we are about to ram the enemy robot.
            if(Math.random()>200/e.getDistance()){
                dir=-dir;
            }
        }

        //This line makes us slow down when we need to turn sharply.
        setMaxVelocity(400/getTurnRemaining());

        setAhead(100*dir);

        //Finding the heading and heading change.
        double enemyHeading = e.getHeadingRadians();
        double enemyHeadingChange = enemyHeading - oldEnemyHeading;
        oldEnemyHeading = enemyHeading;

        /*This method of targeting is know as circular targeting; you assume your enemy will
           *keep moving with the same speed and turn rate that he is using at fire time.The
           *base code comes from the wiki.
          */
        double deltaTime = 0;
        double predictedX = getX()+e.getDistance()*Math.sin(absBearing);
        double predictedY = getY()+e.getDistance()*Math.cos(absBearing);
        while((++deltaTime) * BULLET_SPEED <  Point2D.Double.distance(getX(), getY(), predictedX, predictedY)){

            //Add the movement we think our enemy will make to our enemy's current X and Y
            predictedX += Math.sin(enemyHeading) * e.getVelocity();
            predictedY += Math.cos(enemyHeading) * e.getVelocity();


            //Find our enemy's heading changes.
            enemyHeading += enemyHeadingChange;

            //If our predicted coordinates are outside the walls, put them 18 distance units away from the walls as we know
            //that that is the closest they can get to the wall (Bots are non-rotating 36*36 squares).
            predictedX=Math.max(Math.min(predictedX,getBattleFieldWidth()-18),18);
            predictedY=Math.max(Math.min(predictedY,getBattleFieldHeight()-18),18);

        }
        //Find the bearing of our predicted coordinates from us.
        double aim = Utils.normalAbsoluteAngle(Math.atan2(  predictedX - getX(), predictedY - getY()));

        //Aim and fire.
        setTurnGunRightRadians(Utils.normalRelativeAngle(aim - getGunHeadingRadians()));
        setFire(BULLET_POWER);






        setTurnRadarRightRadians(Utils.normalRelativeAngle(absBearing-getRadarHeadingRadians())*2);
    }
    public void onBulletHit(BulletHitEvent e){
        enemyEnergy-=BULLET_DAMAGE;
    }
    public void onHitWall(HitWallEvent e){
        dir=-dir;
    }
}