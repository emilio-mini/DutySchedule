package me.emiliomini.dutyschedule.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import kotlinx.coroutines.delay
import kotlin.random.Random

enum class Symbols {
    LETTERS,
    NUMBER,
    SYMBOLS
}

@Composable
fun ObfuscatedText(
    length: Int,
    vararg symbols: Symbols,
    modifier: Modifier = Modifier,
    intervalMs: Long = 100L,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
) {
    var sequence by remember { mutableStateOf(getSymbolSequence(symbols.toSet())) }
    var displayText by remember { mutableStateOf("") }

    LaunchedEffect(symbols) {
        sequence = getSymbolSequence(symbols.toSet())
    }

    LaunchedEffect(sequence, length, intervalMs) {
        while (true) {
            delay(intervalMs)
            displayText = randomOfSequence(sequence, length)
        }
    }

    Text(displayText, modifier, color = color, fontSize = fontSize, fontWeight = fontWeight)
}

private fun getSymbolSequence(symbols: Set<Symbols>): List<Char> {
    val numbers = List(10) { ('0'.code + it).toChar() }
    val special = (
            (0x21..0x2F) +          // ! " # $ % & ' ( ) * + , - . /
                    (0x3A..0x40) +  // : ; < = > ? @
                    (0x5B..0x60) +  // [ \ ] ^ _ `
                    (0x7B..0x7E)    // { | } ~
            ).map { it.toChar() }
    val letters = (('A'..'Z') + ('a'..'z'))

    val result = mutableListOf<Char>()
    if (Symbols.LETTERS in symbols) result.addAll(letters)
    if (Symbols.NUMBER in symbols) result.addAll(numbers)
    if (Symbols.SYMBOLS in symbols) result.addAll(special)

    return result
}

private fun randomOfSequence(sequence: List<Char>, length: Int): String {
    if (length <= 0) {
        return ""
    }

    val sb = StringBuilder(length)
    repeat(length) {
        sb.append(sequence[Random.nextInt(0, sequence.size)])
    }
    return sb.toString()
}