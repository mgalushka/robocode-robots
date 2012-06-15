package com.maximgalushka.robocode;

import robocode.Robot;

/**
 * <p></p>
 *
 * @author Maxim Galushka
 * @since 15.06.12
 */
public class StaticBot extends Robot {

    @Override
    public void run() {
        while (true) {
            turnRadarRight(360);
        }
    }
}
