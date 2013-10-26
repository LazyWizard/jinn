package org.lazywizard.jinn;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.CampaignPlugin.PickPriority;
import com.fs.starfarer.api.combat.ShipAIPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import org.lazywizard.jinn.ai.JinnAI;

public class JinnModPlugin extends BaseModPlugin
{
    @Override
    public PluginPick<ShipAIPlugin> pickShipAI(FleetMemberAPI member, ShipAPI ship)
    {
        if (ship.getHullSpec().getHullId().startsWith("jinn_"))
        {
            return new PluginPick<ShipAIPlugin>(new JinnAI(ship),
                    PickPriority.MOD_SPECIFIC);
        }

        return null;
    }
}
