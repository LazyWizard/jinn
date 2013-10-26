package org.lazywizard.jinn.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAIPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;

public class JinnAI implements ShipAIPlugin
{
    private final ShipAPI ship;
    private ShipAPI target;

    public JinnAI(ShipAPI ship)
    {
        this.ship = ship;
    }

    public ShipAPI calcBestTarget()
    {
        ShipAPI bestTarget = null;
        float bestFlux = 0f;
        // TODO: give targets weight based on distance
        for (ShipAPI tmp : AIUtils.getEnemiesOnMap(ship))
        {
            if (tmp.getFluxTracker().getMaxFlux() > bestFlux)
            {
                bestTarget = tmp;
                bestFlux = tmp.getFluxTracker().getMaxFlux();
            }
        }

        System.out.println("Best target is "
                + (bestTarget != null ? bestTarget.getVariant().getDisplayName()
                : " null") + " at " + bestFlux + " total flux");
        return bestTarget;
    }

    public boolean hasTarget()
    {
        // Target is set, is not a hulk, and is still in play
        return (target != null && !target.isHulk()
                && Global.getCombatEngine().isEntityInPlay(target));
    }

    public void findTarget()
    {
        target = calcBestTarget();
        ship.setShipTarget(target);
    }

    @Override
    public void advance(float amount)
    {
        if (!hasTarget())
        {
            findTarget();
        }

        if (target != null)
        {
            // If we're practically in range, try to match speed/facing of target
            if (MathUtils.getDistanceSquared(ship.getLocation(),
                    target.getLocation()) > 62500f)
            {
            }
            // Head directly towards new target when not consuming
            else
            {
                float intendedFacing = MathUtils.getAngle(ship.getLocation(),
                        target.getLocation());
                float rotation = MathUtils.getShortestRotation(
                        ship.getFacing(), intendedFacing);
                // Don't bother if we're pretty much spot on (avoids jittering)
                if (Math.abs(rotation) > 0.5f)
                {
                    ship.giveCommand((rotation > 0f
                            ? ShipCommand.TURN_LEFT : ShipCommand.TURN_RIGHT),
                            null, 0);
                }

                // If we are facing towards our target, accelerate
                if (Math.abs(rotation) < 90)
                {
                    ship.giveCommand(ShipCommand.ACCELERATE, null, 0);
                }
                // Otherwise, sit and wait until facing towards them
                else
                {
                    ship.giveCommand(ShipCommand.DECELERATE, null, 0);
                }
            }
        }
        // No target found on the map, stick where you are
        // TODO: continue at current heading to map edge and escape instead
        else
        {
            ship.giveCommand(ShipCommand.DECELERATE, null, 0);
        }
    }

    @Override
    public void forceCircumstanceEvaluation()
    {
        findTarget();
    }

    @Override
    public boolean needsRefit()
    {
        return false;
    }

    @Override
    public void setDoNotFireDelay(float amount)
    {
    }
}
