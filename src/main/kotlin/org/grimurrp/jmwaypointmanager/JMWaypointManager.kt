package org.grimurrp.jmwaypointmanager

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.netty.buffer.ByteBufHelper
import com.github.retrooper.packetevents.netty.buffer.UnpooledByteBufAllocationHelper
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPluginMessage
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin


class JMWaypointManager : JavaPlugin() {
    override fun onLoad() {
        plugin = this
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this))
        PacketEvents.getAPI().load()
    }
    override fun onEnable() {
        PacketEvents.getAPI().init();
    }

    override fun onDisable() {
        PacketEvents.getAPI().terminate()
    }

    /**
     * This is a data object representing a waypoint.
     * Note that ID will always be "id = "name + "_" + x + "," + y + "," + z", this format is required.
     *
     * @property name The name of the waypoint, also used for ID.
     * @property loc Location containing the X, Y, Z coordinates of waypoint
     * @property type Indicates type of waypoint, can either be "normal" or "death"
     * @property red RGB Red value
     * @property green RGB Green value
     * @property blue RGB Blue value
     * @property icon Icon for the waypoint, currently unused as only valid option is "journeymap:ui/img/waypoint-icon.png"
     * @property origin Currently supported origins include "external", a base type, and "external-force" which is a waypoint that a user cannot disable
     * @property announce Informs the user of waypoint creation
     * @property persistent If true waypoints will save to disk, persevering beyond current user session to the server
     * @property dimensions String array containing dimensions in which to create the waypoint, multiple supported
     * Example dimensions input: "minecraft:overworld"
     */
    data class Waypoint(
        val name: String,
        val loc: Location,
        val type: String, // can be either "normal" or "death"
        val red: Int,
        val green: Int,
        val blue: Int,
        val icon: String, // not currently used, reserved for future JM update
        val persistent: Boolean,
        val origin: String,
        val announce: Boolean,
        val dimensions: Array<String>
    )

    companion object {
        var plugin: Plugin? = null
        val CHANNEL = "journeymap:waypoint"

        fun writeStringToBuffer(dataOut: Any, string: String) {
            val bytes = string.toByteArray(Charsets.UTF_8)
            ByteBufHelper.writeInt(dataOut, bytes.size)
            ByteBufHelper.writeBytes(dataOut, bytes)
        }

        /**
         * Sends a waypoint to a list of players.
         *
         * @property players List of bukkit players.
         * @property waypoint Waypoint object to send
         */
        @JvmStatic
        fun createWaypoint(players: List<Player>, waypoint: Waypoint) {
            // create packet
            val obj = JsonObject()
            obj.addProperty("id", waypoint.name + '_' + waypoint.loc.x + ',' + waypoint.loc.y+ ',' + waypoint.loc.z)
            obj.addProperty("name", waypoint.name)
            obj.addProperty("icon", "journeymap:ui/img/waypoint-icon.png")
            obj.addProperty("enable", true) // must always be true
            obj.addProperty("type", waypoint.type)
            obj.addProperty("origin", waypoint.origin)
            obj.addProperty("x", waypoint.loc.x)
            obj.addProperty("y", waypoint.loc.y)
            obj.addProperty("z", waypoint.loc.z)
            obj.addProperty("r", waypoint.red)
            obj.addProperty("g", waypoint.green)
            obj.addProperty("b", waypoint.blue)
            obj.addProperty("persistent", waypoint.persistent)

            // process dimensions
            val dimensions = Gson().toJsonTree(waypoint.dimensions).asJsonArray.toString()
            obj.addProperty("dimensions", dimensions)

            // Debug
            println(obj.toString())

            val buffer = UnpooledByteBufAllocationHelper.buffer()
            ByteBufHelper.writeByte(buffer, 0) // Extra byte for Forge
            writeStringToBuffer(buffer, obj.toString()) // Payload
            writeStringToBuffer(buffer, "create") // Action
            ByteBufHelper.writeBoolean(buffer, waypoint.announce) // Announce

            for (player in players) {
                if (player.isOnline) {
                    println("Create ${waypoint.name + '_' + waypoint.loc.x + ',' + waypoint.loc.y+ ',' + waypoint.loc.z} for player ${player.name}")
                    //player.sendPacket(ClientboundCustomPayloadPacket(ResourceLocation(CHANNEL), out))

                    val wrapper = WrapperPlayServerPluginMessage(CHANNEL, ByteBufHelper.array(buffer))
                    PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, wrapper)

                    println("Waypoint packet successfully sent to ${player.name}")
                }
            }
        }

        /**
         * Deletes a waypoint for a list of players.
         *
         * @property players List of bukkit players.
         * @property name Name of the waypoint to delete
         * @property announce Whether to announce waypoint deletion to player
         * @property origin Currently unused, defaults internally to "external"
         */
        @JvmStatic
        fun deleteWaypoint(players: List<Player>, name: String, announce: Boolean, origin: String) {
            // create packet
            val obj = JsonObject()
            obj.addProperty("name", name)
            obj.addProperty("origin", origin)

            // Debug
            println(obj.toString())

            val buffer = UnpooledByteBufAllocationHelper.buffer()
            ByteBufHelper.writeByte(buffer, 0) // Extra byte for Forge
            writeStringToBuffer(buffer, obj.toString()) // Payload
            writeStringToBuffer(buffer, "delete") // Action
            ByteBufHelper.writeBoolean(buffer, announce) // Announce

            // send packet to all players in list
            for (player in players) {
                if (player.isOnline) {
                    println("Delete $name for player ${player.name}")

                    val wrapper = WrapperPlayServerPluginMessage(CHANNEL, ByteBufHelper.array(buffer))
                    PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, wrapper)

                    println("Waypoint deletion packet successfully sent to ${player.name}")
                }
            }
        }

        /**
         * Sends a waypoint to one player.
         * Note that if you do intend to send out a waypoint to multiple to use the appropriate method
         * as it avoids creating a packet multiple times
         *
         * @property player Bukkit player object.
         * @property waypoint Waypoint object to send
         */
        fun createWaypoint(player: Player, waypoint: Waypoint) {
            val playerList = listOf(player)
            createWaypoint(playerList, waypoint)
        }

        /**
         * Deletes a waypoint for one player.
         * Note that if you do intend to delete a waypoint for multiple to use the appropriate method
         * as it avoids creating a packet multiple times
         *
         * @property player Bukkit player object.
         * @property name Name of the waypoint to delete
         * @property announce Whether to announce waypoint deletion to player
         * @property origin Currently unused, defaults internally to "external"
         */
        fun deleteWaypoint(player: Player, name: String, announce: Boolean, origin: String) {
            val playerList = listOf(player)
            deleteWaypoint(playerList, name, announce, origin)
        }

        /**
         * Wrapper for getting Bukkit player connection and sending packet.
         */
        //internal fun <T: PacketListener> Player.sendPacket(p: Packet<T>) {
        //    return (this as CraftPlayer).handle.connection.send(p)
        //}
    }

}