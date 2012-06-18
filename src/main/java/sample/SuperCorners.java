package sample;

import robocode.*;
import java.awt.*;
import java.awt.geom.Point2D;

import robocode.util.*;

/*
* SuperCorners-A SuperSampleBot by CrazyBassoonist. This robot tries to trap it's opponents in the corners,
*  making it easier to hit them. This robot is an example of provocative movement.
*/
public class SuperCorners extends AdvancedRobot {

    //The amount of velocities we will store at one time.
    static int depth=200;

    //Whether or not we changed our gun's depth.
    static boolean depthChange=true;

    //The list of data we store our enemy's movements in.
    static double enemyVelocities[][];

    //Our enemy's current velocity.
    static int currentEnemyVelocity;

    //The velocity we are currently keeping track of.
    static int aimingEnemyVelocity;

    //The averaged velocity.
    double velocityToAimAt;


    //Other
    boolean fired;
    double oldTime;
    int count;
    int averageCount;


    //The middle of our field, we'll divide it into four quadrants.
    static double fieldXMid;
    static double fieldYMid;

    //Enemy's X and Y.
    static double EX;
    static double EY;

    //Our bulletpower and turn direction.
    static double maxBP;
    static double turn;

    //Our direction and distance to stay away from the enemy.
    int dir=1;
    double dist=1000;

    //The X and Y of the corner we are trying to push them into.
    double cornerX,cornerY;

    //The amount to modify our angle by.
    static double firingAngleMod=1.0;

    //The closest distance we want to be from our enemy.
    static double closestDist=100;

    //The old heading of the enemy, used to find turn rate.
    double oldEnemyHeading;

    static boolean firstTime=true;

    public void run(){

        // Set colors
        setBodyColor(Color.red);
        setGunColor(Color.black);
        setRadarColor(Color.yellow);
        setBulletColor(Color.green);
        setScanColor(Color.green);

        //To help out our targeting, we initialize the whole movement array to the current aim when we change depths.
        //This helps just a tiny bit in the early rounds.
        if(firstTime){
            velocityToAimAt=4;
            currentEnemyVelocity=1;
        }
        firstTime=false;

        if(depthChange){
            enemyVelocities=new double[depth][4];
            double assumedVelocity=velocityToAimAt;
            count=0;
            while(count<4){
                if(count==0||count==2){
                    if(currentEnemyVelocity==0||currentEnemyVelocity==2){
                        assumedVelocity=velocityToAimAt;
                    }
                    else{
                        assumedVelocity=-velocityToAimAt;
                    }
                }
                else{
                    if(currentEnemyVelocity==1||currentEnemyVelocity==3){
                        assumedVelocity=velocityToAimAt;
                    }
                    else{
                        assumedVelocity=-velocityToAimAt;
                    }

                }
                averageCount=0;
                while(averageCount<depth){
                    enemyVelocities[averageCount][count]=assumedVelocity;
                    averageCount++;
                }
                count++;
            }
            count=0;
        }
        depthChange=false;


        //Find the middle of the field.
        fieldYMid=getBattleFieldHeight()/2;
        fieldXMid=getBattleFieldWidth()/2;
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        do{
            turnRadarRightRadians(Double.POSITIVE_INFINITY);
        }while(true);
    }
    public void onScannedRobot(ScannedRobotEvent e){

        //Our maximum bulletPower is 3, unless we are further than 200 distance units away from them, in which case it is 2.4.
        //We'll also find our bulletSpeed here.
        maxBP=3;
        if(e.getDistance()>200){
            maxBP=2.4;
        }
        double bulletPower=Math.min(maxBP,Math.min(getEnergy()/10,e.getEnergy()/4));
        double bulletSpeed=20-3*bulletPower;

        //Find our enemy's absolute bearing and makes a thing used for painting.
        double absBearing=e.getBearingRadians()+getHeadingRadians();
        Graphics2D g=getGraphics();

        //This finds our enemies X and Y (note:  it can be used to find the X and Y of anything by switching the distance and bearing).
        EX=getX()+e.getDistance()*Math.sin(absBearing);
        EY=getY()+e.getDistance()*Math.cos(absBearing);



        //Find out which velocity segment our enemy is at right now. Note that I use two segments for when their velocity is zero,
        //because it is often important to consider where they are coming from.
        if(e.getVelocity()<-2){
            currentEnemyVelocity=0;
        }
        else if(e.getVelocity()>2){
            currentEnemyVelocity=1;
        }
        else if(e.getVelocity()<=2&&e.getVelocity()>=-2){
            if(currentEnemyVelocity==0){
                currentEnemyVelocity=2;
            }
            else if(currentEnemyVelocity==1){
                currentEnemyVelocity=3;
            }
        }

        //We update our aiming segment each time enough time has passed for a bullet to hit them and we have fired;
        if(getTime()-oldTime>e.getDistance()/12.8&&fired==true){
            aimingEnemyVelocity=currentEnemyVelocity;
        }
        else{
            fired=false;
        }

        //Record a new enemy velocity and raise the count.
        enemyVelocities[count][aimingEnemyVelocity]=e.getVelocity();
        count++;
        if(count==depth){
            count=0;
        }


        //Calculate our average velocity for our the list of velocities at our current segment.
        averageCount=0;
        velocityToAimAt=0;
        while(averageCount<depth){
            velocityToAimAt+=enemyVelocities[averageCount][currentEnemyVelocity];
            averageCount++;
        }
        velocityToAimAt/=depth;




        //find our enemy's heading and heading change.
        double enemyHeading = e.getHeadingRadians();
        double enemyHeadingChange = enemyHeading - oldEnemyHeading;
        oldEnemyHeading = enemyHeading;

        /*This method of targeting is know as circular targeting; you assume your enemy will keep moving with the same speed and turn rate that he is using at
          *fire time. This particular version of it will not aim outside the walls, important for when your enemy is trapped in a corner.
          *The base code comes from the wiki, however, it has been improved by keeping track of more than just the current state of the enemy robot.
          */
        double deltaTime = 0;
        double predictedX = EX, predictedY = EY;
        while((++deltaTime) * bulletSpeed <  Point2D.Double.distance(getX(), getY(), predictedX, predictedY)){

            //Add the movement we think our enemy will make to our enemy's current X and Y(note that it is modified by the average of our enemy's velocities
            //at the current segment, instead of their current velocity.
            predictedX += Math.sin(enemyHeading) * velocityToAimAt;
            predictedY += Math.cos(enemyHeading) * velocityToAimAt;


            //Find our enemy's heading changes.
            enemyHeading += enemyHeadingChange;

            //Paint the path we think our enemy will take(turn on paint in the robot console to see).
            g.setColor(Color.red);
            g.fillOval((int)predictedX-2, (int)predictedY-2, 4, 4);

            //If our predicted coordinates are outside the walls, put them 18 distance units away from the walls as we know
            //that that is the closest they can get to the wall (bots are non-rotating 36*36 squares).
            predictedX=Math.max(predictedX,18);
            predictedY=Math.max(predictedY,18);
            predictedX=Math.min(predictedX,getBattleFieldWidth()-18);
            predictedY=Math.min(predictedY,getBattleFieldHeight()-18);

        }
        //Find the bearing of our predicted coordinates from us.
        double aim = Utils.normalAbsoluteAngle(Math.atan2(  predictedX - getX(), predictedY - getY()));

        //Aim and fire.
        setTurnGunRightRadians(Utils.normalRelativeAngle(aim - getGunHeadingRadians()));
        setFire(bulletPower);

        //Find which corner we want to trap them in, based on their distance from each corner.
        if(EX>fieldXMid){
            cornerX=getBattleFieldWidth();
        }
        else{
            cornerX=0;
        }
        if(EY>fieldYMid){
            cornerY=getBattleFieldHeight();
        }
        else{
            cornerY=0;
        }

        //Yay, math!!

        //This finds the enemy's distance from the current corner. Note that you can (and should, it is much easier)
        //use the Point2D.Double.distance(X1, Y1, X2, Y2) method instead. I do it this way just for the heck of it.
        double enemyDistanceFromCorner=Math.sqrt(Math.pow((EX-cornerX),2)+Math.pow((EY-cornerY),2));

        //This finds the enemy's bearing from the current corner,
        double enemyBearingFromCorner=Utils.normalAbsoluteAngle(Math.atan2(EX-cornerX,EY-cornerY));

        /*This assigns coordinates for where we want to move. Note that this is the same formula used to find the enemies X and Y,
          *except that the variables are replaced. This is equivalent to graphing a line from the corner through the enemy and picking a
          *spot that is "dist"(the distance we want to be from the enemy) down the line.
          */
        double targetX=cornerX+(enemyDistanceFromCorner+dist)*Math.sin(enemyBearingFromCorner);
        double targetY=cornerY+(enemyDistanceFromCorner+dist)*Math.cos(enemyBearingFromCorner);


        //We don't want to aim at a point outside the battlefield, do we?
        targetX=Math.max(18, targetX);
        targetX=Math.min(getBattleFieldWidth()-20, targetX);
        targetY=Math.max(18, targetY);
        targetY=Math.min(getBattleFieldHeight()-20, targetY);


        //This part is for determining which direction to go in order to best get to the point.
        //There are much easier ways to do this, but this works and it is an interesting example.

        //What we are doing here is making two points, one directly in front of us and one behind.....
        double cushion1X=getX()+100*Math.sin(getHeadingRadians());
        double cushion1Y=getY()+100*Math.cos(getHeadingRadians());
        double cushion2X=getX()-100*Math.sin(getHeadingRadians());
        double cushion2Y=getY()-100*Math.cos(getHeadingRadians());

        //...Then finding the distance from those points to the place we want to go.
        double cushion1Dist=Math.sqrt(Math.pow((cushion1X-targetX),2)+Math.pow((cushion1Y-targetY),2));
        double cushion2Dist=Math.sqrt(Math.pow((cushion2X-targetX),2)+Math.pow((cushion2Y-targetY),2));

        //Here we assign our direction based on which one is closer.
        if(cushion1Dist>cushion2Dist){
            dir=-1;
        }
        else{
            dir=1;
        }

        //Graphics that paint a target at the location we are trying to get to. To see them, press "paint" in the robot console.
        g.setColor(Color.red);
        g.fillOval((int)targetX-5, (int)targetY-5, 10, 10);
        g.setColor(Color.white);
        g.fillOval((int)targetX-4, (int)targetY-4, 8, 8);
        g.setColor(Color.red);
        g.fillOval((int)targetX-3, (int)targetY-3, 6, 6);


        //if we reach our target, make the distance closer
        if(getX()>targetX-18&&getX()<targetX+18&&getY()>targetY-18&&getY()<targetY+18){
            dist-=50;
        }

        //It would be counterproductive to move away from the enemy, so we make the maximum distance equivalent to our distance from the enemy robot.
        dist=Math.min(dist,e.getDistance());

        //If they aren't moving away, then we don't want to hit them. If we did that, we'd be a Rambot *shudder*
        //Instead, we'll stay at close quarters, which will help our targeting a lot, as linear targeting can actually be decent at close range.
        dist=Math.max(dist, closestDist);

        //This finds the bearing of the point from us. Really this does the same thing as the turn,
        //but it helps us figure out where to go when we change directions.
        double inverseTurn=Utils.normalAbsoluteAngle(Math.atan2(targetX-getX(),targetY-getY()));

        //If we are going backwards, we want to reflect our target points over our robot for the purposes of turning.
        if(dir==-1){
            targetX=getX()-100*Math.sin(inverseTurn);
            targetY=getY()-100*Math.cos(inverseTurn);
        }

        //This finds the  bearing of our target coordinates from us.
        turn=Utils.normalAbsoluteAngle(Math.atan2(targetX-getX(),targetY-getY()));

        //.... If you've read this whole thing and you don't know what this does, you've just wasted 20 minutes of your life.
        setAhead(100*dir);
        setTurnRightRadians(robocode.util.Utils.normalRelativeAngle(turn-getHeadingRadians()));
        setTurnRadarRightRadians(robocode.util.Utils.normalRelativeAngle(absBearing-getRadarHeadingRadians())*2);

    }
    public void onDeath(DeathEvent e){

        /*There are vast differences in movement among robots, and what works for one may not work for another. Changing the closest
           *distance we will go to our enemy robot greatly helps our robot to adapt to different enemies. The idea behind changing it when the robot dies is that
           *if the distance works, the robot will be less likely to die and it will continue to use that movement. Note that it is not effective to have
           *multiple things set this way, because that dramatically decreases your chances of finding the optimal values.
          */
        closestDist=Math.max(50, 600*Math.random());
        if(Math.random()>.5){
            closestDist=Math.max(50,150*Math.random());
        }

        //Same basic idea for this, which will change the depth of our gun when we die.
        depth=(int)(400*Math.random());
        depthChange=true;
    }
}