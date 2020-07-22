import java.io.Serializable
import java.lang.Integer.max
import java.lang.Integer.min
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToLong

class HyperLogLog : Serializable {
    companion object {
        const val MIN_P_PARAM = 4
        const val MAX_P_PARAM = 18
    }

    private val p: Int
    private val m: Int
    private val kComplement: Int
    private val alphaM2: Double
    private val registerM: ByteArray
    private val biasK: Int

    constructor(_p: Int) {
        p = _p
        if (p < MIN_P_PARAM || p > MAX_P_PARAM) {
            throw IllegalArgumentException("'p' must be at least $MIN_P_PARAM and at most $MAX_P_PARAM (was: $p)")
        }

        kComplement = 64 - p
        m = 1 shl p
        alphaM2 = HLLUtils.getAlphaM2(m)
        registerM = ByteArray(m)
        biasK = 6
    }

    fun add(hash: Long) {
        val index = hash.toInt() ushr kComplement
        var rank: Int = min(kComplement, countConsecutiveZeros(hash)) + 1
        var maxRank =  max(registerM[index].toInt() , rank)
        registerM[index] = maxRank.toByte()
    }

    private fun countConsecutiveZeros(v: Long) : Int {
        var result: Int = 0
        var k: Long = v shl m
        while (result < kComplement) {
            k = (if (k > -1) {
                k shl 1
                result++
            } else {
                break
            }).toLong()
        }

        return result
    }

    fun estimateCount(): Long {
        var c = .0
        var estimate: Double
        var v: Long = 0
        for (i in 0 until m) {
            if (registerM[i].toInt() != 0) {
                c += 1.0 / 2.0.pow(registerM[i].toDouble())
            } else {
                c += 1.0
                v++
            }
        }
        estimate = alphaM2 / c
        estimate = if (estimate <= 5 * m) estimate - estimateBias(estimate) else estimate
        val h: Long
        h = if (v != 0L) {
            linearCounting(m.toLong(), v)
        } else {
            estimate.roundToLong()
        }
        return if (h <= HLLUtils.getThreshold(p)) {
            h
        } else {
            estimate.roundToLong()
        }
    }

    private fun estimateBias(e: Double): Double {
        val rawEstimateData = HLLUtils.getRawEstimateData(p)
        val biasData = HLLUtils.getBiasData(p)
        val weights = DoubleArray(biasK)
        var index: Int = biasData.size - biasK
        for (i in rawEstimateData.indices) {
            val w = 1.0 / abs(rawEstimateData[i] - e)
            val j = i % weights.size
            if (java.lang.Double.isInfinite(w)) {
                return biasData[i]
            } else if (weights[j] >= w) {
                index = i - biasK
                break
            }
            weights[j] = w
        }
        var weightSum = 0.0
        var biasSum = 0.0
        var i = 0
        var j = index
        while (i < biasK) {
            val w = weights[i]
            val b = biasData[j]
            biasSum += w * b
            weightSum += w
            ++i
            ++j
        }
        return biasSum / weightSum
    }

    private fun linearCounting(m: Long, v: Long): Long {
        return (m * ln(m.toDouble() / v)).toLong()
    }

    fun union(other: HyperLogLog) {
        check(other.m == m) { "The number of counters must be the same." }
        if (other === this) {
            return
        }
        for (i in 0 until m) {
            registerM[i] = if (registerM[i] >= other.registerM[i]) registerM[i] else other.registerM[i]
        }
    }
}