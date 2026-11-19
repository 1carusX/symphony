package io.github.zyrouge.symphony.desktop.spike

import java.io.File
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine

fun main(args: Array<String>) {
    val dir = File(args[0])
    val files = dir.listFiles()!!.filter { it.isFile }.sortedBy { it.name }
    for (file in files) {
        println(probe(file))
    }
}

private fun probe(file: File): String {
    try {
        val fileFormat = AudioSystem.getAudioFileFormat(file)
        val stream = AudioSystem.getAudioInputStream(file)
        val base = stream.format
        val durationFromProps: Long? = fileFormat.properties()["duration"] as? Long
        val durationFallback: Long? =
            if (stream.frameLength > 0 && base.frameRate > 0f) {
                (stream.frameLength * 1_000_000L / base.frameRate).toLong()
            } else {
                null
            }
        val pcm = AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            base.sampleRate,
            16,
            base.channels,
            base.channels * 2,
            base.sampleRate,
            false,
        )
        val converted = AudioSystem.getAudioInputStream(pcm, stream)
        val buffer = ByteArray(8192)
        var decoded = 0L
        while (true) {
            val read = converted.read(buffer)
            if (read < 0) break
            decoded += read.toLong()
        }
        var lineOk = false
        try {
            val info = DataLine.Info(SourceDataLine::class.java, pcm)
            val line = AudioSystem.getLine(info) as SourceDataLine
            line.open(pcm)
            line.close()
            lineOk = true
        } catch (error: Exception) {
        }
        converted.close()
        stream.close()
        val durationUs: Long? = durationFromProps ?: durationFallback
        val typeName = fileFormat.type.toString()
        val message = "OK file=" + file.name + " type=" + typeName +
            " durationUs=" + durationUs + " decodedBytes=" + decoded + " line=" + lineOk
        return message
    } catch (error: Exception) {
        return "FAIL file=${file.name} error=${error.javaClass.simpleName}: ${error.message}"
    }
}
