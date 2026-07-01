package com.example.utils

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.math.sin

object SoundSynthesizer {
    var isSoundEnabled: Boolean = true
    var isMusicEnabled: Boolean = true

    fun playTone(frequency: Double, durationMs: Int, type: ToneType = ToneType.SINE) {
        if (!isSoundEnabled) return
        
        GlobalScope.launch(Dispatchers.Default) {
            try {
                val sampleRate = 44100
                val numSamples = (durationMs * sampleRate / 1000)
                val sample = DoubleArray(numSamples)
                val generatedSnd = ByteArray(2 * numSamples)

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    when (type) {
                        ToneType.SINE -> {
                            sample[i] = sin(2 * Math.PI * frequency * t)
                        }
                        ToneType.TRIANGLE -> {
                            val period = sampleRate / frequency
                            val phase = (i % period) / period
                            sample[i] = if (phase < 0.5) {
                                4.0 * phase - 1.0
                            } else {
                                3.0 - 4.0 * phase
                            }
                        }
                        ToneType.SWEEP -> {
                            val freqInst = frequency - (frequency / 2.0) * (i.toDouble() / numSamples)
                            sample[i] = sin(2 * Math.PI * freqInst * t)
                        }
                        ToneType.NOISE -> {
                            sample[i] = Math.random() * 2.0 - 1.0
                        }
                    }
                }

                var idx = 0
                for (dVal in sample) {
                    val valShort = (dVal * 32767).toInt().toShort()
                    generatedSnd[idx++] = (valShort.toInt() and 0x00ff).toByte()
                    generatedSnd[idx++] = ((valShort.toInt() and 0xff00) ushr 8).toByte()
                }

                val audioTrack = AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    generatedSnd.size,
                    AudioTrack.MODE_STATIC
                )
                audioTrack.write(generatedSnd, 0, generatedSnd.size)
                audioTrack.play()
                
                kotlinx.coroutines.delay(durationMs.toLong() + 30)
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playClick() = playTone(750.0, 50, ToneType.SINE)
    fun playCorrect() = playTone(1100.0, 150, ToneType.SINE)
    fun playWrong() = playTone(220.0, 250, ToneType.SWEEP)
    fun playVictory() {
        playTone(523.25, 100, ToneType.SINE) // C5
        GlobalScope.launch {
            kotlinx.coroutines.delay(120)
            playTone(659.25, 100, ToneType.SINE) // E5
            kotlinx.coroutines.delay(120)
            playTone(783.99, 100, ToneType.SINE) // G5
            kotlinx.coroutines.delay(120)
            playTone(1046.50, 220, ToneType.SINE) // C6
        }
    }
    fun playBonus() {
        playTone(880.0, 80, ToneType.SINE)
        GlobalScope.launch {
            kotlinx.coroutines.delay(80)
            playTone(1320.0, 180, ToneType.SINE)
        }
    }

    enum class ToneType {
        SINE, TRIANGLE, SWEEP, NOISE
    }
}
