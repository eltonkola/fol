package com.fol.com.fol.db

fun ByteArray.sha256(): ByteArray {
    val K = longArrayOf(
        0x428a2f98L, 0x71374491L, 0xb5c0fbcfL, 0xe9b5dba5L, 0x3956c25bL, 0x59f111f1L, 0x923f82a4L, 0xab1c5ed5L,
        0xd807aa98L, 0x12835b01L, 0x243185beL, 0x550c7dc3L, 0x72be5d74L, 0x80deb1feL, 0x9bdc06a7L, 0xc19bf174L,
        0xe49b69c1L, 0xefbe4786L, 0x0fc19dc6L, 0x240ca1ccL, 0x2de92c6fL, 0x4a7484aaL, 0x5cb0a9dcL, 0x76f988daL,
        0x983e5152L, 0xa831c66dL, 0xb00327c8L, 0xbf597fc7L, 0xc6e00bf3L, 0xd5a79147L, 0x06ca6351L, 0x14292967L,
        0x27b70a85L, 0x2e1b2138L, 0x4d2c6dfcL, 0x53380d13L, 0x650a7354L, 0x766a0abbL, 0x81c2c92eL, 0x92722c85L,
        0xa2bfe8a1L, 0xa81a664bL, 0xc24b8b70L, 0xc76c51a3L, 0xd192e819L, 0xd6990624L, 0xf40e3585L, 0x106aa070L,
        0x19a4c116L, 0x1e376c08L, 0x2748774cL, 0x34b0bcb5L, 0x391c0cb3L, 0x4ed8aa4aL, 0x5b9cca4fL, 0x682e6ff3L,
        0x748f82eeL, 0x78a5636fL, 0x84c87814L, 0x8cc70208L, 0x90befffaL, 0xa4506cebL, 0xbef9a3f7L, 0xc67178f2L
    )

    var h0 = 0x6a09e667L
    var h1 = 0xbb67ae85L
    var h2 = 0x3c6ef372L
    var h3 = 0xa54ff53aL
    var h4 = 0x510e527fL
    var h5 = 0x9b05688cL
    var h6 = 0x1f83d9abL
    var h7 = 0x5be0cd19L

    val paddedData = padMessage(this)
    val chunks = paddedData.chunked(64)

    for (chunk in chunks) {
        val w = LongArray(64)
        for (i in 0 until 16) {
            w[i] = (chunk[i * 4].toLong() and 0xff shl 24) or
                    (chunk[i * 4 + 1].toLong() and 0xff shl 16) or
                    (chunk[i * 4 + 2].toLong() and 0xff shl 8) or
                    (chunk[i * 4 + 3].toLong() and 0xff)
        }

        for (i in 16 until 64) {
            val s0 = (w[i - 15].rotateRight(7)) xor (w[i - 15].rotateRight(18)) xor (w[i - 15] ushr 3)
            val s1 = (w[i - 2].rotateRight(17)) xor (w[i - 2].rotateRight(19)) xor (w[i - 2] ushr 10)
            w[i] = (w[i - 16] + s0 + w[i - 7] + s1) and 0xFFFFFFFFL
        }

        var a = h0
        var b = h1
        var c = h2
        var d = h3
        var e = h4
        var f = h5
        var g = h6
        var h = h7

        for (i in 0 until 64) {
            val s1 = (e.rotateRight(6)) xor (e.rotateRight(11)) xor (e.rotateRight(25))
            val ch = (e and f) xor ((e.inv()) and g)
            val temp1 = (h + s1 + ch + K[i] + w[i]) and 0xFFFFFFFFL
            val s0 = (a.rotateRight(2)) xor (a.rotateRight(13)) xor (a.rotateRight(22))
            val maj = (a and b) xor (a and c) xor (b and c)
            val temp2 = (s0 + maj) and 0xFFFFFFFFL

            h = g
            g = f
            f = e
            e = (d + temp1) and 0xFFFFFFFFL
            d = c
            c = b
            b = a
            a = (temp1 + temp2) and 0xFFFFFFFFL
        }

        h0 = (h0 + a) and 0xFFFFFFFFL
        h1 = (h1 + b) and 0xFFFFFFFFL
        h2 = (h2 + c) and 0xFFFFFFFFL
        h3 = (h3 + d) and 0xFFFFFFFFL
        h4 = (h4 + e) and 0xFFFFFFFFL
        h5 = (h5 + f) and 0xFFFFFFFFL
        h6 = (h6 + g) and 0xFFFFFFFFL
        h7 = (h7 + h) and 0xFFFFFFFFL
    }

    return byteArrayOf(
        (h0 shr 24).toByte(), (h0 shr 16).toByte(), (h0 shr 8).toByte(), h0.toByte(),
        (h1 shr 24).toByte(), (h1 shr 16).toByte(), (h1 shr 8).toByte(), h1.toByte(),
        (h2 shr 24).toByte(), (h2 shr 16).toByte(), (h2 shr 8).toByte(), h2.toByte(),
        (h3 shr 24).toByte(), (h3 shr 16).toByte(), (h3 shr 8).toByte(), h3.toByte(),
        (h4 shr 24).toByte(), (h4 shr 16).toByte(), (h4 shr 8).toByte(), h4.toByte(),
        (h5 shr 24).toByte(), (h5 shr 16).toByte(), (h5 shr 8).toByte(), h5.toByte(),
        (h6 shr 24).toByte(), (h6 shr 16).toByte(), (h6 shr 8).toByte(), h6.toByte(),
        (h7 shr 24).toByte(), (h7 shr 16).toByte(), (h7 shr 8).toByte(), h7.toByte()
    )
}

fun ByteArray.chunked(size: Int): List<ByteArray> {
    return List(this.size / size + if (this.size % size == 0) 0 else 1) { index ->
        this.slice(index * size until minOf((index + 1) * size, this.size)).toByteArray()
    }
}

private fun padMessage(data: ByteArray): ByteArray {
    val originalLength = data.size
    val paddingLength = (56 - (originalLength + 1) % 64 + 64) % 64
    val paddedLength = originalLength + 1 + paddingLength + 8

    return ByteArray(paddedLength).apply {
        data.copyInto(this)
        this[originalLength] = 0x80.toByte()
        val bitLength = (originalLength * 8).toLong()
        for (i in 0 until 8) {
            this[paddedLength - 8 + i] = (bitLength shr ((7 - i) * 8)).toByte()
        }
    }
}

private fun Long.rotateRight(n: Int): Long = (this ushr n) or (this shl (32 - n))


fun String.encodeToUtf8ByteArray(): ByteArray {
    return this.encodeToByteArray()
}
