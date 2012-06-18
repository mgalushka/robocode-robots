package com.maximgalushka.robocode.robots;

import robocode.*;

public class NarrowBeam extends AdvancedRobot {

	public void run() {
		setAdjustRadarForGunTurn(true);
		setTurnRadarRight(1000); // initial scan
		execute();
		while (true) {

			// if we stopped moving the radar, move it a tiny little bit
			// so we keep generating scan events
			if (getRadarTurnRemaining() == 0)
				setTurnRadarRight(1);

			execute();
		}
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		//out.println("scanned: " + e.getName() + " at " + getTime());

		// turn toward the robot we scanned
		setTurnRight(e.getBearing());

		// if we've turned toward our enemy...
		if (Math.abs(getTurnRemaining()) < 10) {
			// move a little closer
			if (e.getDistance() > 200) {
				setAhead(e.getDistance() / 2);
			}
			// but not too close
			if (e.getDistance() < 100) {
				setBack(e.getDistance() * 2);
			}
			setFire(3.0);
		}

		// lock our radar onto our target
		setTurnRadarRight(getHeading() - getRadarHeading() + e.getBearing());
	}
	
	// if the robot we were shooting at died, scan around again
	public void onRobotDeath(RobotDeathEvent e) { setTurnRadarRight(1000); }
}
