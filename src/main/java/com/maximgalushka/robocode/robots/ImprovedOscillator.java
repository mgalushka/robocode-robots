package com.maximgalushka.robocode.robots;

import com.maximgalushka.robocode.EnemyBot;
import robocode.*;

public class ImprovedOscillator extends AdvancedRobot {

	private EnemyBot enemy = new EnemyBot();
	private byte radarDirection = 1;

	public void run() {
		setAdjustRadarForGunTurn(true);
		enemy.reset();
		while (true) {
			doScanner();
			doMovement();
			doGun();
			execute(); // you must call this!!!
		}
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		if (
				// we have no enemy, or...
				enemy.none() ||
				// the one we just spotted is significantly closer, or...
				e.getDistance() < enemy.getDistance() - 70 ||
				// we found the one we've been tracking
				e.getName().equals(enemy.getName())
		   ) {
			// track him
			enemy.update(e);
		}
	}

	public void onRobotDeath(RobotDeathEvent e) {
		// check if the enemy we were tracking died
		if (e.getName().equals(enemy.getName())) {
			enemy.reset();
			stop();
		}
	}   

	public void onHitByBullet(HitByBulletEvent e) {
		setBack(100);
	}

	void doScanner() {
		if (enemy.none()) {
			// look around
			setTurnRadarRight(36000);
		} else {
			// keep him inside a cone
			double turn = getHeading() - getRadarHeading() + enemy.getBearing();
			turn += 30 * radarDirection;
			setTurnRadarRight(turn);
			radarDirection *= -1;
		}
	}

	void doMovement() {
		setTurnRight(enemy.getBearing());
		// move a little closer
		if (enemy.getDistance() > 200)
			setAhead(enemy.getDistance() / 2);
		// but not too close
		if (enemy.getDistance() < 100)
			setBack(enemy.getDistance());
	}

	void doGun() {

		// don't fire if there's no enemy
		if (enemy.none()) return;

		// convenience variable
		double max = Math.max(getBattleFieldHeight(), getBattleFieldWidth());

		// only shoot if we're (close to) pointing at our enemy
		if (Math.abs(getTurnRemaining()) < 10) {
			if (enemy.getDistance() < max / 3) {
				// fire hard when close
				setFire(3);
			} else {
				// otherwise, just plink him
				setFire(1);
			}
		}
	}

}
