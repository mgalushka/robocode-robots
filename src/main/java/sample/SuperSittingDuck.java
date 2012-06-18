package wiki.SuperSampleBot;
import robocode.*;
import java.awt.Color;

/**
 * SuperSittingDuck - a SuperSampleBot by CrazyBassoonist
 */
public class SuperSittingDuck extends AdvancedRobot{
    int enemyHits;
    boolean	goCryInCorner;
    /**
     * run: SuperSittingDuck's default behavior
     */
    public void run() {
        /**
         *set the colors
         */
        setBodyColor(Color.yellow);
        setGunColor(Color.yellow);
        do{
            turnRadarRightRadians(Double.POSITIVE_INFINITY);
        }while(true);
    }
    public void onScannedRobot(ScannedRobotEvent e){
        double absBearing=e.getBearingRadians()+getHeadingRadians();
        double robotForce=5*(e.getDistance()-100);
        /*
          *If we get hit too much, go cry in the corner.
          */
        if(goCryInCorner){
            turnRight(90-getHeading());
            ahead((getBattleFieldWidth()-getX())-20);
            turnRight(0-getHeading());
            ahead((getBattleFieldHeight()-getY())-20);
        }
        /*
          *Otherwise go towards the other robot and don't fire at him
          */
        else{
            setAhead(robotForce);
            setTurnRightRadians(robocode.util.Utils.normalRelativeAngle(absBearing-getHeadingRadians()));
        }
        setTurnRadarRightRadians(robocode.util.Utils.normalRelativeAngle(absBearing-getRadarHeadingRadians()));
    }
    public void onHitByBullet(HitByBulletEvent e){
        /*
          *Find out how much the enemy has hit us...
          */
        enemyHits++;
        if(enemyHits==4){
            System.out.println("Oh, the shame of losing!");
            goCryInCorner=true;
        }
    }

}