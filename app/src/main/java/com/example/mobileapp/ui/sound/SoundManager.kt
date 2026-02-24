package com.example.mobileapp.ui.sound

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.*
import kotlin.math.*

object SoundManager {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var musicJob: Job? = null
    private var musicTrack: AudioTrack? = null
    private var isMusicPlaying = false

    private val sampleRate = 44100

    // ── Helpers ─────────────────────────────────────────────────────────────

    private fun noteFreq(note: String): Double {
        return when (note) {
            "C4"  -> 261.63; "D4"  -> 293.66; "E4"  -> 329.63
            "F4"  -> 349.23; "G4"  -> 392.00; "A4"  -> 440.00
            "B4"  -> 493.88; "C5"  -> 523.25; "D5"  -> 587.33
            "E5"  -> 659.25; "F5"  -> 698.46; "G5"  -> 783.99
            "A5"  -> 880.00; "REST"-> 0.0
            else  -> 0.0
        }
    }

    private fun buildAudioTrack(bufferSize: Int): AudioTrack {
        return AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize * 4)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
    }

    /** Generates samples for a single note with fade in/out envelope */
    private fun generateNote(
        freq: Double,
        durationSamples: Int,
        amplitude: Float = 0.4f,
        waveform: String = "sine"
    ): FloatArray {
        val samples = FloatArray(durationSamples)
        val fadeLen = (durationSamples * 0.08).toInt().coerceAtLeast(100)
        for (i in 0 until durationSamples) {
            val t = i.toDouble() / sampleRate
            val raw = when (waveform) {
                "square"   -> if (sin(2.0 * PI * freq * t) >= 0) 1.0 else -1.0
                "triangle" -> 2.0 / PI * asin(sin(2.0 * PI * freq * t))
                else       -> sin(2.0 * PI * freq * t) // sine
            }
            // Envelope
            val env = when {
                freq == 0.0        -> 0f
                i < fadeLen        -> i.toFloat() / fadeLen
                i > durationSamples - fadeLen -> (durationSamples - i).toFloat() / fadeLen
                else               -> 1f
            }
            samples[i] = (raw * amplitude * env).toFloat()
        }
        return samples
    }

    // ── Shutter snap sound ───────────────────────────────────────────────────

    /**
     * Plays a satisfying camera shutter snap:
     *  - A short descending tone sweep (mechanical feel)
     *  - Followed by a soft "confirmation" chime
     */
    fun playShutter() {
        scope.launch {
            try {
                val snapDuration = (sampleRate * 0.06).toInt()   // 60ms sweep
                val chimeDuration = (sampleRate * 0.18).toInt()  // 180ms chime
                val totalSamples = snapDuration + chimeDuration

                val buffer = FloatArray(totalSamples)

                // Descending frequency sweep (800 → 200 Hz) for the "click"
                for (i in 0 until snapDuration) {
                    val t = i.toDouble() / sampleRate
                    val freq = 800.0 - (600.0 * i / snapDuration)
                    val env = 1f - (i.toFloat() / snapDuration)
                    buffer[i] = (sin(2.0 * PI * freq * t) * 0.6 * env).toFloat()
                }

                // Soft chime at E5 for the confirmation feel
                val chime = generateNote(noteFreq("E5"), chimeDuration, amplitude = 0.25f)
                for (i in 0 until chimeDuration) {
                    buffer[snapDuration + i] += chime[i]
                }

                val track = buildAudioTrack(totalSamples)
                track.write(buffer, 0, totalSamples, AudioTrack.WRITE_BLOCKING)
                track.play()
                delay(400)
                track.stop()
                track.release()
            } catch (_: Exception) {}
        }
    }

    // ── Background music ─────────────────────────────────────────────────────

    /**
     * Cheerful looping safari melody.
     * Pattern: upbeat 8-bar phrase in C major, played on a soft triangle wave.
     * Each "beat" = 1 quarter note at ~120 BPM → 0.5s per beat.
     */
    private val melody = listOf(
        // Bar 1 — ascending opening
        "C4" to 0.25, "E4" to 0.25, "G4" to 0.25, "C5" to 0.5,
        // Bar 2
        "B4" to 0.25, "G4" to 0.25, "E4" to 0.25, "D4" to 0.5,
        // Bar 3 — bouncy
        "E4" to 0.25, "F4" to 0.25, "G4" to 0.5,  "E4" to 0.25,
        // Bar 4
        "D4" to 0.25, "C4" to 0.5,  "REST" to 0.5,
        // Bar 5 — energetic climb
        "G4" to 0.25, "A4" to 0.25, "B4" to 0.25, "C5" to 0.5,
        // Bar 6
        "D5" to 0.25, "C5" to 0.25, "B4" to 0.25, "A4" to 0.5,
        // Bar 7 — settle
        "G4" to 0.5,  "E4" to 0.25, "F4" to 0.25, "G4" to 0.25,
        // Bar 8 — resolve
        "E4" to 0.25, "D4" to 0.25, "C4" to 1.0,  "REST" to 0.5
    )

    private fun buildMelodyBuffer(): FloatArray {
        val allSamples = mutableListOf<Float>()
        for ((note, duration) in melody) {
            val numSamples = (sampleRate * duration).toInt()
            val wave = generateNote(noteFreq(note), numSamples, amplitude = 0.22f, waveform = "triangle")
            wave.forEach { allSamples.add(it) }
        }
        return allSamples.toFloatArray()
    }

    fun startMusic() {
        if (isMusicPlaying) return
        isMusicPlaying = true
        musicJob = scope.launch {
            try {
                val loopBuffer = buildMelodyBuffer()
                while (isActive && isMusicPlaying) {
                    val track = AudioTrack.Builder()
                        .setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_GAME)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                        )
                        .setAudioFormat(
                            AudioFormat.Builder()
                                .setSampleRate(sampleRate)
                                .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build()
                        )
                        .setBufferSizeInBytes(loopBuffer.size * 4)
                        .setTransferMode(AudioTrack.MODE_STATIC)
                        .build()

                    musicTrack = track
                    track.setVolume(musicVolume)
                    track.write(loopBuffer, 0, loopBuffer.size, AudioTrack.WRITE_BLOCKING)
                    track.play()

                    // Wait for the loop to finish before replaying
                    val loopMs = (loopBuffer.size.toLong() * 1000L) / sampleRate
                    delay(loopMs)

                    track.stop()
                    track.release()
                    musicTrack = null
                }
            } catch (_: Exception) {
                isMusicPlaying = false
            }
        }
    }

    private var musicVolume = 1f

    private suspend fun duckMusic(targetVol: Float, steps: Int = 20, stepDelayMs: Long = 15) {
        val start = musicVolume
        for (i in 1..steps) {
            musicVolume = start + (targetVol - start) * (i.toFloat() / steps)
            musicTrack?.setVolume(musicVolume)
            delay(stepDelayMs)
        }
        musicVolume = targetVol
    }

    /**
     * Plays a unique characteristic sound for each animal type.
     * Ducks background music to 15% while the sound plays, then fades back up.
     */
    fun playAnimalSound(animalName: String) {
        scope.launch {
            try {
                val name = animalName.lowercase()
                val buffer = when {
                    // ── Dogs / Foxes ── playful bouncy arpeggio
                    name.contains("dog") || name.contains("fox") ->
                        buildSequence(listOf("C4" to 0.1, "E4" to 0.1, "G4" to 0.1, "C5" to 0.2), "square", 0.3f)

                    // ── Cats ── smooth descending sigh
                    name.contains("cat") ->
                        buildSequence(listOf("G5" to 0.15, "E5" to 0.15, "C5" to 0.3), "sine", 0.28f)

                    // ── Small fluffy (hamster, bunny, hedgehog, squirrel, mouse) ── tiny rapid chirps
                    name.contains("hamster") || name.contains("bunny") || name.contains("rabbit") ||
                            name.contains("hedgehog") || name.contains("squirrel") || name.contains("mouse") ->
                        buildSequence(listOf("A5" to 0.05, "REST" to 0.04, "A5" to 0.05, "REST" to 0.04, "C5" to 0.12), "sine", 0.25f)

                    // ── Bears / Pandas / Koalas ── deep slow growl sweep
                    name.contains("bear") || name.contains("panda") || name.contains("koala") ||
                            name.contains("bison") || name.contains("hippo") ->
                        buildSweep(120.0, 60.0, 0.5, 0.5f, "triangle")

                    // ── Big cats (lion, tiger, cheetah) ── dramatic low roar
                    name.contains("lion") || name.contains("tiger") || name.contains("cheetah") ||
                            name.contains("wolf") ->
                        buildSweep(180.0, 80.0, 0.6, 0.55f, "square")

                    // ── Birds (duck, chicken, owl, penguin, parrot, eagle, flamingo, swan, peacock, rooster, turkey) ── fast high trill
                    name.contains("duck") || name.contains("chicken") || name.contains("owl") ||
                            name.contains("penguin") || name.contains("parrot") || name.contains("eagle") ||
                            name.contains("flamingo") || name.contains("swan") || name.contains("peacock") ||
                            name.contains("rooster") || name.contains("turkey") || name.contains("dodo") ->
                        buildTrill(880.0, 1100.0, 6, 0.08, 0.22f)

                    // ── Ocean / Water creatures (fish, dolphin, whale, shark, octopus, crab, lobster, shrimp, seal, squid) ── bubbly wobble
                    name.contains("fish") || name.contains("dolphin") || name.contains("whale") ||
                            name.contains("shark") || name.contains("octopus") || name.contains("crab") ||
                            name.contains("lobster") || name.contains("shrimp") || name.contains("seal") ||
                            name.contains("squid") || name.contains("puffer") ->
                        buildWobble(300.0, 500.0, 0.5, 0.28f)

                    // ── Reptiles / Snakes / Lizards / Croc / Turtle ── eerie low pulse
                    name.contains("snake") || name.contains("lizard") || name.contains("croc") ||
                            name.contains("turtle") || name.contains("scorpion") ->
                        buildPulse(110.0, 3, 0.18, 0.4f)

                    // ── Insects / Bugs (bee, ant, cricket, mosquito, beetle, butterfly, caterpillar, ladybug, snail) ── buzzy square wave
                    name.contains("bee") || name.contains("ant") || name.contains("cricket") ||
                            name.contains("mosquito") || name.contains("beetle") || name.contains("butterfly") ||
                            name.contains("caterpillar") || name.contains("ladybug") || name.contains("snail") ->
                        buildSequence(listOf("E5" to 0.06, "REST" to 0.03, "E5" to 0.06, "REST" to 0.03, "G5" to 0.1), "square", 0.2f)

                    // ── Large safari (elephant, rhino, giraffe, zebra, kangaroo, llama, sloth, otter) ── low majestic tone
                    name.contains("elephant") || name.contains("rhino") || name.contains("giraffe") ||
                            name.contains("zebra") || name.contains("kangaroo") || name.contains("llama") ||
                            name.contains("sloth") || name.contains("otter") ->
                        buildSequence(listOf("C4" to 0.3, "G4" to 0.3, "E4" to 0.4), "triangle", 0.35f)

                    // ── Dinosaurs / Dragons ── thunderous descending roar
                    name.contains("rex") || name.contains("bronto") || name.contains("dragon") ||
                            name.contains("saber") || name.contains("dino") ->
                        buildSweep(250.0, 50.0, 0.8, 0.6f, "square")

                    // ── Legendary (unicorn, fire spirit, storm, moon, ocean god, comet, rainbow, king, shroom) ── magical ascending fanfare
                    name.contains("unicorn") || name.contains("spirit") || name.contains("storm") ||
                            name.contains("moon") || name.contains("god") || name.contains("comet") ||
                            name.contains("rainbow") || name.contains("king") || name.contains("shroom") ||
                            name.contains("microbe") || name.contains("polar") || name.contains("coral") ->
                        buildSequence(listOf("C4" to 0.1, "E4" to 0.1, "G4" to 0.1, "C5" to 0.1, "E5" to 0.15, "G5" to 0.3), "sine", 0.35f)

                    // ── Fallback ── simple pleasant chime
                    else ->
                        buildSequence(listOf("C5" to 0.15, "E5" to 0.15, "G5" to 0.25), "sine", 0.28f)
                }

                // Duck background music down before playing
                duckMusic(0.15f)

                val track = buildAudioTrack(buffer.size)
                track.write(buffer, 0, buffer.size, AudioTrack.WRITE_BLOCKING)
                track.play()
                val playMs = (buffer.size.toLong() * 1000L) / sampleRate + 100
                delay(playMs)
                track.stop()
                track.release()

                // Fade music back up after sound finishes
                duckMusic(1f)
            } catch (_: Exception) {}
        }
    }

    // ── Sound shape builders ─────────────────────────────────────────────────

    private fun buildSequence(notes: List<Pair<String, Double>>, waveform: String, amp: Float): FloatArray {
        val all = mutableListOf<Float>()
        for ((note, dur) in notes) {
            val n = (sampleRate * dur).toInt()
            generateNote(noteFreq(note), n, amp, waveform).forEach { all.add(it) }
        }
        return all.toFloatArray()
    }

    private fun buildSweep(startHz: Double, endHz: Double, durationSec: Double, amp: Float, waveform: String): FloatArray {
        val n = (sampleRate * durationSec).toInt()
        val samples = FloatArray(n)
        val fadeLen = (n * 0.1).toInt()
        var phase = 0.0
        for (i in 0 until n) {
            val freq = startHz + (endHz - startHz) * (i.toDouble() / n)
            phase += 2.0 * PI * freq / sampleRate
            val raw = when (waveform) {
                "square"   -> if (sin(phase) >= 0) 1.0 else -1.0
                "triangle" -> 2.0 / PI * asin(sin(phase))
                else       -> sin(phase)
            }
            val env = when {
                i < fadeLen      -> i.toFloat() / fadeLen
                i > n - fadeLen  -> (n - i).toFloat() / fadeLen
                else             -> 1f
            }
            samples[i] = (raw * amp * env).toFloat()
        }
        return samples
    }

    private fun buildTrill(freqA: Double, freqB: Double, reps: Int, noteDur: Double, amp: Float): FloatArray {
        val all = mutableListOf<Float>()
        repeat(reps) { i ->
            val freq = if (i % 2 == 0) freqA else freqB
            val n = (sampleRate * noteDur).toInt()
            generateNote(freq, n, amp, "sine").forEach { all.add(it) }
        }
        return all.toFloatArray()
    }

    private fun buildWobble(freqLow: Double, freqHigh: Double, durationSec: Double, amp: Float): FloatArray {
        val n = (sampleRate * durationSec).toInt()
        val samples = FloatArray(n)
        val fadeLen = (n * 0.1).toInt()
        var phase = 0.0
        for (i in 0 until n) {
            val lfo = 0.5 + 0.5 * sin(2.0 * PI * 6.0 * i / sampleRate)
            val freq = freqLow + (freqHigh - freqLow) * lfo
            phase += 2.0 * PI * freq / sampleRate
            val env = when {
                i < fadeLen     -> i.toFloat() / fadeLen
                i > n - fadeLen -> (n - i).toFloat() / fadeLen
                else            -> 1f
            }
            samples[i] = (sin(phase) * amp * env).toFloat()
        }
        return samples
    }

    private fun buildPulse(freq: Double, pulses: Int, pulseDur: Double, amp: Float): FloatArray {
        val all = mutableListOf<Float>()
        repeat(pulses) {
            val n = (sampleRate * pulseDur).toInt()
            generateNote(freq, n, amp, "triangle").forEach { all.add(it) }
            val gap = (sampleRate * 0.08).toInt()
            repeat(gap) { all.add(0f) }
        }
        return all.toFloatArray()
    }

    fun stopMusic() {
        isMusicPlaying = false
        musicJob?.cancel()
        musicJob = null
        try {
            musicTrack?.stop()
            musicTrack?.release()
            musicTrack = null
        } catch (_: Exception) {}
    }
}