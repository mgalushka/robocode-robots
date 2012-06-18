package com.maximgalushka.robocode;

import robocode.Robot;
import robocode.ScannedRobotEvent;

/**
 * <p></p>
 *
 * @author Maxim Galushka
 * @since 15.06.12
 */
public class EnemyBot extends Robot {

    private volatile double bearing;
    private volatile double distance;
    private volatile double energy;
    private volatile double heading;
    private volatile String name = "";
    private volatile double velocity;

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBearing() {
        return bearing;
    }

    public void setBearing(double bearing) {
        this.bearing = bearing;
    }

    public double getEnergy() {
        return energy;
    }

    public void setEnergy(double energy) {
        this.energy = energy;
    }

    public double getHeading() {
        return heading;
    }

    public void setHeading(double heading) {
        this.heading = heading;
    }

    public double getVelocity() {
        return velocity;
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    public void reset() {
        bearing = 0.0;
        distance = 0.0;
        energy = 0.0;
        heading = 0.0;
        name = "";
        velocity = 0.0;

        System.out.printf("Reset tracked bot!\n");
    }


    public boolean none() {
        System.out.printf("None tracked!\n");
        return "".equals(name);
    }

    public void update(ScannedRobotEvent e) {
        bearing = e.getBearing();
        distance = e.getDistance();
        energy = e.getEnergy();
        heading = e.getHeading();
        name = e.getName();
        velocity = e.getVelocity();
    }

    @Override
    public String toString() {
        return "EnemyBot{" +
                "bearing=" + bearing +
                ", distance=" + distance +
                ", energy=" + energy +
                ", heading=" + heading +
                ", name='" + name + '\'' +
                ", velocity=" + velocity +
                '}';
    }
}
