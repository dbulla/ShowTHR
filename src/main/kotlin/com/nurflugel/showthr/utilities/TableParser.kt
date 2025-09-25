package com.nurflugel.showthr.utilities

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import kotlin.system.exitProcess

/** This class will parse the json file output from the Sisyphus "status.json" file and create a list of all the tracks/authors and print it out */
object TableParser {
    @JvmStatic
    fun main(args: Array<String>) {
        val gson = Gson()
        val mapAdapter = gson.getAdapter(object : TypeToken<Array<Map<String, Any?>>>() {})
        val file: File = File("/Users/douglasbullard/Sisyphus/Backups/sisbot-server/sisbot/content/status_copy.json")
        val fileLines = BufferedReader(InputStreamReader(FileInputStream(file))).use { reader ->
            val lineSequence = reader.lineSequence().toMutableList()
            if (lineSequence.isEmpty()) exitProcess(0)
            lineSequence
        }
        val fileAsString = fileLines.joinToString("\n")

        val model: Array<Map<String, Any?>> = mapAdapter.fromJson(fileAsString)
        if (model.isEmpty()) exitProcess(0)

        val tracks = mutableListOf<Track>()
        val playlists = mutableListOf<Playlist>()
        val trackMap = mutableMapOf<String, Track>()

        for (nodeMap in model) {
            if (nodeMap.containsKey("t")) {
                when (val type = nodeMap["t"].toString()) {
                    "track"    -> tracks.add(parseTrack(nodeMap, trackMap))
                    "playlist" -> playlists.add(parsePlaylist(nodeMap))
                    else       -> println("Unknown type: $type")
                }
            }
        }

        outputPlaylists(playlists, trackMap)
        outputTracksNotInPlaylist(playlists, trackMap)
    }

    private fun outputTracksNotInPlaylist(
        playlists: MutableList<Playlist>,
        trackMap: MutableMap<String, Track>,
    ) {
        val playlistTrackIds = playlists.filterNot { it.name == "All Tracks" }
            .flatMap {
                it.tracks.values
                    .map { track -> track.uuid }
            }

        val allTracksIds = trackMap.keys
        val tracksNotInAPlaylist = allTracksIds.minus(playlistTrackIds.toSet())
        println("Tracks not in a playlist:")
        tracksNotInAPlaylist.forEach { it ->
            val track = trackMap[it]!!
            println("    ${track.uuid}    ${track.name}    ${track.authorName}")
        }
    }

    private fun outputPlaylists(
        playlists: MutableList<Playlist>,
        trackMap: MutableMap<String, Track>,
    ) {
        println("Playlists:")
        playlists.forEach {
            println("    ${it.name}")
            for (i in 0..<it.tracks.size) {
                val track = it.tracks[i]!!
                val authorName = trackMap[track.uuid]?.authorName
                if (authorName != null && authorName != "false") println("        ${track.name} by $authorName   ${track.uuid}")
                else println("        ${track.name}   ${track.uuid}")
            }
        }
    }

    private fun parsePlaylist(nodeMap: Map<String, Any?>): Playlist {
        val uuid = nodeMap["i"].toString()
        val name = nodeMap["n"].toString()
        val authorName = nodeMap["cbn"].toString()
        val playlist = Playlist(uuid, name, authorName)

        @Suppress("UNCHECKED_CAST")
        val playlistTrackData: ArrayList<Map<String, Any?>> = nodeMap["tr"] as ArrayList<Map<String, Any?>>
        playlistTrackData.map {
            if (it.containsKey("_index")) {
                val aaa = it["_index"]
                val bbb = aaa.toString().toDouble()
                val trackIndex = bbb.toInt()
                val playlistTrack = PlaylistTrack(it["id"].toString(), it["name"].toString())
                playlist.tracks[trackIndex] = playlistTrack
            }
        }
        return playlist
    }

    private fun parseTrack(nodeMap: Map<String, Any?>, trackMap: MutableMap<String, Track>): Track {
        val uuid = nodeMap["i"].toString()
        val name = nodeMap["n"].toString()
        val authorName = nodeMap["cbn"].toString()
        val thumb = nodeMap["thumb"].toString()
        val largePhoto = nodeMap["large_photo"].toString()
        val trackId = nodeMap["track_id"].toString()
        val track = Track(uuid, name, authorName, trackId)
        trackMap[uuid] = track
        return track
    }
}

data class Track(val uuid: String, val name: String, val authorName: String, val trackId: String) {}
data class PlaylistTrack(val uuid: String, val name: String) {}
data class Playlist(val uuid: String, val name: String, val authorName: String) {
    val tracks: MutableMap<Int, PlaylistTrack> = mutableMapOf()
}