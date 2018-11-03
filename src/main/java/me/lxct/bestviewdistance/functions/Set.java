package me.lxct.bestviewdistance.functions;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import static me.lxct.bestviewdistance.functions.Variable.afkList;
import static me.lxct.bestviewdistance.functions.Variable.playerViewDistance;

public class Set extends org.bukkit.plugin.java.JavaPlugin {

    // MAKE SURE CALCULATED VIEW DISTANCE ISN'T OVER LIMITS
    private static int setViewDistanceLimit(int viewDistance){
        if (viewDistance > Variable.max) {viewDistance = Variable.max;}
        else if (viewDistance < Variable.min) {viewDistance = Variable.min;}
        return viewDistance;
    }


    // A FUNCTION FOR CLIENT SIDE SETTING. DON'T GIVE MORE VIEW DISTANCE THAN REQUIRED.
    private static int setClientSettingLimit(Player player, int viewDistance) {

        //noinspection deprecation
        int clientSideViewDistance = player.getClientViewDistance(); // Get Client Side View Distance
        if (viewDistance > clientSideViewDistance) { // If given view distance is more than client side view distance
            if (viewDistance < Variable.min) {
                viewDistance = Variable.min;
            } else {
                viewDistance = clientSideViewDistance;
            }
        }
        return viewDistance;
    }

    // MAKE SURE SUPPORTED VIEW DISTANCE ISN'T OVER LIMITS
    private static void setSupportedViewDistanceLimit(String player) {
        int supportedViewDistance = playerViewDistance.get(player); // The ping supported view distance
        if (supportedViewDistance > Variable.max) { // If playerViewDistance is over maximum view distance
            playerViewDistance.put(player, Variable.max); // Set it to max
        }
        else if (supportedViewDistance < Variable.min) { // Same with min
            playerViewDistance.put(player, Variable.min);
        }
    }

    // MAKE SURE REDUCTION INDICE ISN'T OVER LIMITS
    public static void setServerLimits() {
        if (Variable.reductionIndice > Variable.maxindice) { // Make sure the reduction indice don't escape limits
            Variable.reductionIndice = Variable.maxindice;
        } else if (Variable.reductionIndice < 0) {
            Variable.reductionIndice = 0.0;
        }
    }

    // THE MAIN FUNCTION ! CALCULATE BEST PLAYER VIEW DISTANCE WITH REDUCTION INDICE
    public static void setPlayersBestViewDistance(double ReductionIndice) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if(afkList.contains(player.getName())){ // IF player is afk
                player.setViewDistance(Variable.min);
            }
            else {
                int supportedViewDistance = playerViewDistance.get(player.getName()); // View distance supported by player
                int ping = player.spigot().getPing(); // Ping of player

                if (ping < Variable.aping && ping > 1) {supportedViewDistance = supportedViewDistance + 1;} // Low ping = More View Distance
                else if (ping >= Variable.rping) {supportedViewDistance = supportedViewDistance- 1;} // Big ping = Less View Distance

                playerViewDistance.put(player.getName(), supportedViewDistance); // Store in var

                setSupportedViewDistanceLimit(player.getName()); // Make sure supported view distance doesn't get over limits
                int viewDistance = Math.round((int) (supportedViewDistance * (1 - ReductionIndice))); // Apply percentage
                // About the line under this comment. We set player view distance only if view distance doesn't get over limits
                // And respect player settings
                player.setViewDistance(setClientSettingLimit(player,setViewDistanceLimit(viewDistance)));
            }
        }
    }
}