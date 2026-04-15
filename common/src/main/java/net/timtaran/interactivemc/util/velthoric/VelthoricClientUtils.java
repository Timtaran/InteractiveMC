/*
 * This file is part of InteractiveMC.
 * Licensed under LGPL 3.0.
 */
package net.timtaran.interactivemc.util.velthoric;

import com.github.stephengold.joltjni.RVec3;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.xmx.velthoric.core.body.client.VxClientBodyDataContainer;
import net.xmx.velthoric.core.body.client.VxClientBodyManager;

import java.util.List;

public class VelthoricClientUtils {

    /**
     * Returns indices of all bodies whose last known position
     * lies within the specified radius from the given center point.
     *
     * <p>The distance check is performed using squared distance
     * to avoid an expensive square root operation.</p>
     *
     * @param center the center point of the search sphere (not null)
     * @param radius the search radius (must be >= 0)
     * @return a list of indices of bodies located strictly inside the radius
     */
    public static List<Integer> bodiesAround(RVec3 center, double radius) {
        return bodiesAround(center.xx(), center.yy(), center.zz(), radius);
    }

    /**
     * Returns indices of all bodies whose last known position
     * lies within the specified radius from the given center point.
     *
     * <p>The distance check is performed using squared distance
     * to avoid an expensive square root operation.</p>
     *
     * @param x      the x-coordinate of the search point
     * @param y      the y-coordinate of the search point
     * @param z      the z-coordinate of the search point
     * @param radius the search radius (must be >= 0)
     * @return a list of indices of bodies located strictly inside the radius
     */
    public static List<Integer> bodiesAround(double x, double y, double z, double radius) {
        double squaredRadius = radius * radius;

        List<Integer> result = new IntArrayList();
        VxClientBodyDataContainer dataStore = VxClientBodyManager.getInstance().getStore().clientCurrent();

        for (int i = 0; i < dataStore.lastKnownPosition.length; i++) {
            RVec3 bodyPosition = dataStore.lastKnownPosition[i];

            double xdif, ydif, zdif;

            xdif = (bodyPosition.xx() - x);
            ydif = (bodyPosition.yy() - y);
            zdif = (bodyPosition.zz() - z);

            if ((xdif * xdif + ydif * ydif + zdif * zdif) < squaredRadius)
                result.add(i);
        }

        return result;
    }
}
