package me.emiliomini.dutyschedule.ui.base

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.emiliomini.dutyschedule.ui.theme.currentTextFieldPlaceholderColor
import me.emiliomini.dutyschedule.ui.theme.currentTextFieldLeadingIconColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    leadingIcon: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    labelFontWeight: FontWeight? = null // Reinstated
) {
    Box(
        modifier = modifier // Reinstated usage of modifier parameter
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled, // Reinstated
            readOnly = readOnly, // Reinstated
            textStyle = textStyle, // Reinstated
            label = {
                Text(
                    text = label,
                    fontWeight = labelFontWeight // Reinstated
                )
            },
            leadingIcon = leadingIcon,
            keyboardOptions = keyboardOptions, // Reinstated
            visualTransformation = visualTransformation, // Reinstated
            singleLine = singleLine, // Reinstated
            maxLines = maxLines, // Reinstated
            minLines = minLines, // Reinstated
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,
                unfocusedLabelColor = currentTextFieldPlaceholderColor,
                focusedLeadingIconColor = currentTextFieldLeadingIconColor,
                unfocusedLeadingIconColor = currentTextFieldLeadingIconColor,
                disabledLeadingIconColor = currentTextFieldLeadingIconColor.copy(alpha = 0.5f)
            )
        )
    }
}

@Preview(showBackground = false)
@Composable
fun AppTextFieldPreview() {
    AppTextField(
        value = "Sample Text",
        onValueChange = {},
        label = "Sample Label"
    )
}
