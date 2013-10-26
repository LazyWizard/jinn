package org.lazywizard.jinn.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.HullModEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import java.util.HashMap;
import java.util.Map;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector2f;

// Can't access non-jarred classes in jars, so can't extend BaseHullMod
public class JinnStats implements HullModEffect
{
    private static Map<ShipAPI, Float> getVictims(ShipAPI jinn)
    {
        // Find all non-allied ships whose collision radius overlaps with the jinn
        float jinnRadius = jinn.getCollisionRadius(), a, dx, dy;
        Map<ShipAPI, Float> victims = new HashMap<ShipAPI, Float>();
        for (ShipAPI tmp : Global.getCombatEngine().getShips())
        {
            // Don't eat allied ships
            if (tmp.getOwner() == jinn.getOwner())
            {
                continue;
            }

            // Test that collision radii are overlapping
            a = jinnRadius + tmp.getCollisionRadius();
            dx = jinn.getLocation().x - tmp.getLocation().x;
            dy = jinn.getLocation().y - tmp.getLocation().y;
            if (a * a > (dx * dx + dy * dy))
            {
                // TODO: adjust strength based on distance/other factors
                victims.put(tmp, 1f);
            }
        }

        return victims;
    }

    public static void main(String[] args)
    {
        Vector2f loc1 = new Vector2f(0f, 0f), loc2 = new Vector2f(0f, 50f);
        float rad1 = 2f, rad2 = 30f;
        System.out.println(MathUtils.getDistance(loc1, loc2));
        System.out.println(MathUtils.getDistance(loc1, loc2) - (rad1 + rad2));
        float overlap = MathUtils.getDistance(loc1, loc2) - (rad1 + rad2);
        overlap *= -1;

        System.out.println("Overlapping by " + overlap);
        System.out.println(MathUtils.getDistance(loc1, loc2) / (rad1 + rad2));
    }

    private static void checkConsume(ShipAPI ship, float amount)
    {
        ShipAPI victim;
        float strength;
        for (Map.Entry<ShipAPI, Float> entry : getVictims(ship).entrySet())
        {
            victim = entry.getKey();
            strength = entry.getValue();
            System.out.println(ship.getVariant().getDisplayName()
                    + " would be consuming " + victim.getVariant().getDisplayName()
                    + " at strength " + strength + " right now");
        }
    }

    private static void checkDamage(ShipAPI ship)
    {
        // TODO: take flux damage from projectiles that pass through Jinn
        //ship.getFluxTracker().increaseFlux(50f, true);

        if (ship.getCollisionClass() == CollisionClass.NONE
                && ship.getFluxTracker().getFluxLevel() >= .99f)
        {
            ship.setCollisionClass(CollisionClass.SHIP);
            ship.getFluxTracker().forceOverload(86400f);
        }
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount)
    {
        if (ship.isHulk())
        {
            return;
        }

        if (ship.equals(Global.getCombatEngine().getPlayerShip()))
        {
            if (Mouse.isButtonDown(0))
            {
                ship.getFluxTracker().decreaseFlux(50f);
            }
            else if (Mouse.isButtonDown(1))
            {
                ship.getFluxTracker().increaseFlux(50f, true);
            }
        }

        float bonusMod = 1f - ship.getFluxTracker().getFluxLevel();
        System.out.println("Bonus for " + ship.getVariant().getDisplayName()
                + " is " + (int) (bonusMod * 100) + "%");
        ship.getMutableStats().getAcceleration().modifyMult("jinn_fluxbonus", bonusMod);
        ship.getMutableStats().getDeceleration().modifyMult("jinn_fluxbonus", bonusMod);
        ship.getMutableStats().getMaxSpeed().modifyMult("jinn_fluxbonus", bonusMod);
        ship.getMutableStats().getTurnAcceleration().modifyMult("jinn_fluxbonus", bonusMod);
        ship.getMutableStats().getMaxTurnRate().modifyMult("jinn_fluxbonus", bonusMod);

        checkConsume(ship, amount);
        checkDamage(ship);
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize,
            MutableShipStatsAPI stats, String id)
    {
        stats.getFluxDissipation().modifyMult(id, 0f);
        stats.getZeroFluxMinimumFluxLevel().modifyFlat(id, 1f);
        stats.getZeroFluxSpeedBoost().modifyMult(id, 0f);
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id)
    {
        ship.setCollisionClass(CollisionClass.NONE);
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship)
    {
        return false;
    }

    //<editor-fold defaultstate="collapsed" desc="Not implemented">
    @Override
    public void advanceInCampaign(FleetMemberAPI member, float amount)
    {
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize)
    {
        return null;
    }
    //</editor-fold>
}
