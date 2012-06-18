package sample;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;

import robocode.*;
import robocode.util.*;
/*
 * SuperMercutio-A SuperSampleBot by CrazyBassoonist. This robot is an improved version of my 
 * micro melee robot Mercutio, which is personally one of my favorite robots because of its movement.
 * SuperMercutio is meant to demonstrate how to effectively use simple waves in both movement and targeting.
 * 
 * --Note--
 * This is a slightly more advanced SuperSample than any of the other ones, 
 * so if you have not yet mastered the basics you may wish to look at some of the other robots.
 * If you do not know what a wave is, there are some very informative articles on the robowiki.
 * That said, I will move onto a description of the robot.
 * 
 * --Movement--
 * Details:
 * When the enemy fires, SuperMercutio creates a wave and records two firing angles the enemy could have used:
 * a head-on targeting angle and a linear targeting angle. It then uses that information to create imaginary
 * bullets in the air, and uses a form of anti-gravity movement to avoid them.
 * 
 * Effectiveness:
 * This movement is very good at dodging simple targeting, as the waves make it very precise. It is a form
 * of wavesurfing in the strictest sense because it makes use of waves, but it is not what most people 
 * would think of as wavesurfing because it is not a learning movement and it does not make use of guessfactors.
 * Because it is not an adaptive movement and it is not random movement, advanced guns can hone in on its 
 * movement better than they can on most bots. However, the inherent complexity of its movement can make it a
 * hard target.
 * 
 * --Gun--
 * Details:
 * Upon firing, this robot logs a wave and records the lateral velocity of the opponent. When the wave passes
 * the opponent, it records the angle the other robot moved in and records this in an array segmented by the
 * lateral velocity of the opponent when the shot was fired. To aim its gun, it merely uses the most recent angle
 * the opponent ended up at at the opponent's current lateral velocity.
 * 
 * Effectiveness:
 * This gun adapts reasonably quickly to adaptive movements. However, because it doesn't save any more data than
 * the most recent angle for each lateral velocity segment, it does not capable of building accuracy over time
 * like guns such as patternmatching and guessfactor guns.
 */

public class SuperMercutio extends AdvancedRobot {
    final static double FIRE_POWER=2;
    final static double FIRE_SPEED=20-FIRE_POWER*3;
    final static double BULLET_DAMAGE=10;
    /*
      * change these statistics to see different graphics.
      */
    final static boolean PAINT_MOVEMENT=true;
    final static boolean PAINT_GUN=false;

    static double enemyEnergy;

    /*
      * An ArrayList can hold a list of objects.
      * We'll be using the first one to hold all the waves that we wish to keep track of for movement, and the
      * second for the targeting waves.
      */
    ArrayList<SuperMercutio.MovementWave> moveWaves=new ArrayList<SuperMercutio.MovementWave>();
    ArrayList<SuperMercutio.GunWave> gunWaves=new ArrayList<SuperMercutio.GunWave>();

    /*
      * This Array will hold the most recent movement angle for every lateral velocity segment;
      */
    static double gunAngles[]=new double[16];
    public void run(){
        enemyEnergy=100;

        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        setColors(Color.white,Color.gray,Color.red);

        //This is the best possible radar lock
        while(true){
            if(getRadarTurnRemainingRadians()==0){
                setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
            }
            //This method paints the waves.
            paint();
            execute();
        }
    }
    /*
      * In this robot, as in many others, onScannedRobot is used as the main place to put actions done every tick.
      */
    public void onScannedRobot(ScannedRobotEvent e){
        double absBearing=e.getBearingRadians()+getHeadingRadians();

        /*
           * ==================Movement Section============================
           * This makes us put a log into our log when we notice the enemy firing.
           * To see the actual logging of the wave, look at the logWave method.
           */
        double energyChange=(enemyEnergy-(enemyEnergy=e.getEnergy()));
        MovementWave w;
        if(energyChange<=3&&energyChange>=0.1){
            logMovementWave(e,energyChange);
        }
        /*
           * After we are done checking to see if we need to log any waves, we'll decide where to move.
           * To see this process take a peek at the chooseDirection method.
           */
        chooseDirection(project(new Point2D.Double(getX(),getY()),e.getDistance(),absBearing));

        /*
           * logs a gun wave when we fire;
           */
        if(getGunHeat()==0){
            logFiringWave(e);
        }
        /*
           * This method checks our waves to see if they have reached the enemy yet.
           */
        checkFiringWaves(project(new Point2D.Double(getX(),getY()),e.getDistance(),absBearing));

        /*
           * Aiming our gun and firing
           */
        setTurnGunRightRadians(Utils.normalRelativeAngle(absBearing-getGunHeadingRadians())
                +gunAngles[8+(int)(e.getVelocity()*Math.sin(e.getHeadingRadians()-absBearing))]);
        setFire(FIRE_POWER);

        setTurnRadarRightRadians(Utils.normalRelativeAngle(absBearing-getRadarHeadingRadians())*2);
    }
    /*
      * This helps us keep from being confused about the enemy's energy after hitting them with a bullet.
      */
    public void onBulletHit(BulletHitEvent e){
        enemyEnergy-=BULLET_DAMAGE;
    }
    /*
      * This method receives a ScannedRobotEvent and uses that information to create a new wave and place it in
      * our log. Basically we're going to take all the information we'll need to know later to figure out where
      * to move to and store it in one object so we can use it easily later.
      */
    public void logMovementWave(ScannedRobotEvent e,double energyChange){
        double absBearing=e.getBearingRadians()+getHeadingRadians();
        MovementWave w=new MovementWave();
        //This is the spot that the enemy was in when they fired.
        w.origin=project(new Point2D.Double(getX(),getY()),e.getDistance(),absBearing);
        //20-3*bulletPower is the formula to find a bullet's speed.
        w.speed=20-3*energyChange;
        //The time at which the bullet was fired.
        w.startTime=getTime();
        //The absolute bearing from the enemy to us can be found by adding Pi to our absolute bearing.
        w.angle=Utils.normalRelativeAngle(absBearing+Math.PI);
        /*
           * Our lateral velocity, used to calculate where a bullet fired with linear targeting would be.
           * Note that the speed has already been factored into the calculation.
           */
        w.latVel=(getVelocity()*Math.sin(getHeadingRadians()-w.angle))/w.speed;
        //This actually adds the wave to the list.
        moveWaves.add(w);
    }
    /*
      * This method looks at all the directions we could go, then rates them based on how close they will take us
      * to simulated bullets fired with both linear and head-on targeting generated by the waves we have logged.
      * It is the core of our movement.
      */
    public void chooseDirection(Point2D.Double enemyLocation){
        MovementWave w;
        //This for loop rates each angle individually
        double bestRating=Double.POSITIVE_INFINITY;
        for(double moveAngle=0;moveAngle<Math.PI*2;moveAngle+=Math.PI/16D){
            double rating=0;

            //Movepoint is position we would be at if we were to move one robot-length in the given direction.
            Point2D.Double movePoint=project(new Point2D.Double(getX(),getY()),36,moveAngle);

            /*
                * This loop will iterate through each wave and add a risk for the simulated bullets on each one
                * to the total risk for this angle.
                */
            for(int i=0;i<moveWaves.size();i++){
                w=moveWaves.get(i);

                //This part will remove waves that have passed our robot, so we no longer keep taking into account old ones
                if(new Point2D.Double(getX(),getY()).distance(w.origin)<(getTime()-w.startTime)*w.speed+w.speed){
                    moveWaves.remove(w);
                }
                else{
                    /*
                          * This adds two risks for each wave: one based on the distance from where a head-on targeting
                          * bullet would be, and one for where a linear targeting bullet would be.
                          */
                    rating+=1D/Math.pow(movePoint.distance(project(w.origin,movePoint.distance(w.origin),w.angle)),2);
                    rating+=1D/Math.pow(movePoint.distance(project(w.origin,movePoint.distance(w.origin),w.angle+w.latVel)),2);
                }
            }
            //This adds a risk associated with being to close to the other robot if there are no waves.
            if(moveWaves.size()==0){
                rating=1D/Math.pow(movePoint.distance(enemyLocation),2);
            }
            //This part tells us to go in the direction if it is better than the previous best option and is reachable.
            if(rating<bestRating&&new Rectangle2D.Double(50,50,getBattleFieldWidth()-100,getBattleFieldHeight()-100).contains(movePoint)){
                bestRating=rating;
                /*
                     * These next three lines are a very codesize-efficient way to
                     * choose the best direction for moving to a point.
                     */
                int pointDir;
                setAhead(1000*(pointDir=(Math.abs(moveAngle-getHeadingRadians())<Math.PI/2?1:-1)));
                setTurnRightRadians(Utils.normalRelativeAngle(moveAngle+(pointDir==-1?Math.PI:0)-getHeadingRadians()));
            }
        }
    }

    /*
      * This method will log a firing wave.
      */
    public void logFiringWave(ScannedRobotEvent e){
        GunWave w=new GunWave();
        w.absBearing=e.getBearingRadians()+getHeadingRadians();
        w.speed=FIRE_SPEED;
        w.origin=new Point2D.Double(getX(),getY());
        w.velSeg=(int)(e.getVelocity()*Math.sin(e.getHeadingRadians()-w.absBearing));
        w.startTime=getTime();
        gunWaves.add(w);
    }
    /*
      * This method checks firing waves to see if they have passed the enemy yet.
      */
    public void checkFiringWaves(Point2D.Double ePos){
        GunWave w;
        for(int i=0;i<gunWaves.size();i++){
            w=gunWaves.get(i);
            if((getTime()-w.startTime)*w.speed>=w.origin.distance(ePos)){
                gunAngles[w.velSeg+8]=Utils.normalRelativeAngle(Utils.normalAbsoluteAngle(Math.atan2(ePos.x-w.origin.x, ePos.y-w.origin.y))-w.absBearing);
                gunWaves.remove(w);
            }
        }
    }
    /*
      * This extremely useful method lets us project one point from another given a specific angle and distance.
      */
    public Point2D.Double project(Point2D.Double origin,double dist,double angle){
        return new Point2D.Double(origin.x+dist*Math.sin(angle),origin.y+dist*Math.cos(angle));
    }
    /*
      * This is where we will paint our waves;
      */
    public void paint(){
        Graphics g=getGraphics();
        double radius;

        /*
           * Paints the waves and the imaginary bullets from the movement.
           */
        if(PAINT_MOVEMENT){
            for(int i=0;i<moveWaves.size();i++){
                MovementWave w=moveWaves.get(i);
                g.setColor(Color.blue);
                radius=(getTime()-w.startTime)*w.speed+w.speed;
                g.drawOval((int)(w.origin.x-radius),(int)(w.origin.y-radius),(int)radius*2,(int)radius*2);
                Point2D.Double hotBullet=project(w.origin,radius,w.angle);
                Point2D.Double latBullet=project(w.origin,radius,w.angle+w.latVel);
                g.setColor(Color.red);
                g.fillOval((int)hotBullet.x-3,(int)hotBullet.y-3,6,6);
                g.fillOval((int)latBullet.x-3,(int)latBullet.y-3,6,6);
            }
        }
        /*
           * Just paints the waves for the targeting.
           */
        if(PAINT_GUN){
            for(int i=0;i<gunWaves.size();i++){
                GunWave w=gunWaves.get(i);
                g.setColor(Color.blue);
                radius=(getTime()-w.startTime)*w.speed;
                g.drawOval((int)(w.origin.x-radius),(int)(w.origin.y-radius),(int)radius*2,(int)radius*2);
            }
        }


    }
    /*
      * This class is the data we will need to use our movement waves.
      */
    public static class MovementWave{
        Point2D.Double origin;
        double startTime;
        double speed;
        double angle;
        double latVel;
    }
    /*
      * This class is the data we will need to use for our targeting waves.
      */
    public class GunWave{
        double speed;
        Point2D.Double origin;
        int velSeg;
        double absBearing;
        double startTime;
    }

}