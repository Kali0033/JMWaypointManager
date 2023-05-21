package org.grimurrp.jmwaypointmanager

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPluginMessage
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.nio.charset.StandardCharsets

class JMWaypointManager : JavaPlugin() {
    override fun onEnable() {
    }

    public fun createWaypoint(player: Player, name: String, loc: Location, type: String, red: Int, green: Int, blue: Int) {
        println("Create ${name + '_' + loc.x + ',' + loc.y+ ',' + loc.z} for player ${player.name}")

        val obj = JsonObject()
        obj.addProperty("id", name + '_' + loc.x + ',' + loc.y+ ',' + loc.z)
        obj.addProperty("name", name)
        obj.addProperty("icon", "waypoint-normal.png")
        obj.addProperty("enable", true)
        obj.addProperty("type", type)
        obj.addProperty("origin", "external")
        obj.addProperty("x", loc.x)
        obj.addProperty("y", loc.y)
        obj.addProperty("z", loc.z)
        obj.addProperty("r", red)
        obj.addProperty("g", green)
        obj.addProperty("b", blue)
        obj.addProperty("persistent", false)
        val dimensions = JsonArray(1)
        dimensions.add("minecraft:" + player.getWorld().getName())
        obj.add("dimensions", dimensions)

        val bytes = obj.toString().toByteArray(StandardCharsets.UTF_8)

        val wrapper = WrapperPlayServerPluginMessage("journeymap:waypoint", bytes)
        PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, wrapper)
    }

    public fun deleteWaypoint(player: Player, name: String?) {
        println("Delete $name for player ${player.name}")

        val obj = JsonObject()
        obj.addProperty("name", name)
        obj.addProperty("origin", "external")
        val bytes = obj.toString().toByteArray(StandardCharsets.UTF_8)

        val wrapper = WrapperPlayServerPluginMessage("journeymap:waypoint", bytes)
        PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, wrapper)
    }
}