package org.grimurrp.jmwaypointmanager

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPluginMessage
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.nio.charset.StandardCharsets


class JMWaypointManager : JavaPlugin() {
    override fun onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this))
        PacketEvents.getAPI().load()
    }
    override fun onEnable() {
        PacketEvents.getAPI().init();
    }

    override fun onDisable() {
        PacketEvents.getAPI().terminate()
    }

    data class Waypoint(
        val name: String,
        val loc: Location,
        val type: String,
        val red: Int,
        val green: Int,
        val blue: Int
    )

    companion object {
        @JvmStatic
        fun createWaypoint(player: Player, waypoint: Waypoint) {
            println("Create ${waypoint.name + '_' + waypoint.loc.x + ',' + waypoint.loc.y+ ',' + waypoint.loc.z} for player ${player.name}")

            val obj = JsonObject()
            obj.addProperty("id", waypoint.name + '_' + waypoint.loc.x + ',' + waypoint.loc.y+ ',' + waypoint.loc.z)
            obj.addProperty("name", waypoint.name)
            obj.addProperty("icon", "waypoint-normal.png")
            obj.addProperty("enable", true)
            obj.addProperty("type", waypoint.type)
            obj.addProperty("origin", "external")
            obj.addProperty("x", waypoint.loc.x)
            obj.addProperty("y", waypoint.loc.y)
            obj.addProperty("z", waypoint.loc.z)
            obj.addProperty("r", waypoint.red)
            obj.addProperty("g", waypoint.green)
            obj.addProperty("b", waypoint.blue)
            obj.addProperty("persistent", false)
            val dimensions = JsonArray(1)
            dimensions.add("minecraft:" + player.getWorld().getName())
            obj.add("dimensions", dimensions)

            val bytes = obj.toString().toByteArray(StandardCharsets.UTF_8)

            val wrapper = WrapperPlayServerPluginMessage("journeymap:waypoint", bytes)
            PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, wrapper)
        }
        @JvmStatic
        fun deleteWaypoint(player: Player, name: String?) {
            println("Delete $name for player ${player.name}")

            val obj = JsonObject()
            obj.addProperty("name", name)
            obj.addProperty("origin", "external")
            val bytes = obj.toString().toByteArray(StandardCharsets.UTF_8)

            val wrapper = WrapperPlayServerPluginMessage("journeymap:waypoint", bytes)
            PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, wrapper)
        }
    }
}