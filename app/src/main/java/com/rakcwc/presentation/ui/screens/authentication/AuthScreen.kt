package com.rakcwc.presentation.ui.screens.authentication

import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.composables.icons.lucide.*
import com.rakcwc.data.remote.resources.Resource
import com.rakcwc.presentation.ui.theme.AccentColor

@Composable
fun AuthScreen(
    authVm: AuthViewModel = hiltViewModel(),
    navController: NavController,
    onForgotPasswordClick: () -> Unit = {},
    onGoogleSignInClick: () -> Unit = {},
    onAppleSignInClick: () -> Unit = {},
    onSignUpClick: () -> Unit = {}
) {
    val state = authVm.state.value
    val authState by authVm.authState.collectAsState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(authState) {
        if (authState is Resource.Success && authState.data != null) {
            // Navigate back to previous screen or settings after successful login
            navController.popBackStack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = Color.White
            )
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {

        Spacer(modifier = Modifier.height(24.dp))

        // Title
        Text(
            text = "Login",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Please login to your account",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        // General Error Message
        if (state.generalError != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Lucide.CircleAlert,
                    contentDescription = "Error",
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = state.generalError,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFD32F2F)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Email Field
        Text(
            text = "E-mail Address",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = state.email,
            onValueChange = remember { { authVm.onEmailChange(it) } },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = "m.info@example.com") },
            leadingIcon = {
                Icon(
                    imageVector = Lucide.Mail,
                    contentDescription = "Email",
                    tint = if (state.emailError != null) Color(0xFFD32F2F) else Color.Gray
                )
            },
            textStyle = TextStyle(
                color = Color.Black,
                fontWeight = FontWeight.Medium,
            ),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = if (state.emailError != null) Color(0xFFD32F2F) else Color.LightGray.copy(alpha = 0.8f),
                focusedBorderColor = if (state.emailError != null) Color(0xFFD32F2F) else Color.Gray,
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                errorBorderColor = Color(0xFFD32F2F),
                errorContainerColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                }
            ),
            isError = state.emailError != null,
            singleLine = true,
            supportingText = if (state.emailError != null) {
                {
                    Text(
                        text = state.emailError,
                        color = Color(0xFFD32F2F),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else null
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Field
        Text(
            text = "Password",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = state.password,
            onValueChange = remember { { authVm.onPasswordChange(it) } },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = "********") },
            leadingIcon = {
                Icon(
                    imageVector = Lucide.Lock,
                    contentDescription = "Password",
                    tint = if (state.passwordError != null) Color(0xFFD32F2F) else Color.Gray
                )
            },
            trailingIcon = {
                IconButton(onClick = { authVm.onPasswordVisibilityChange(!state.isPasswordVisible) }) {
                    Icon(
                        imageVector = if (state.isPasswordVisible) Lucide.Eye else Lucide.EyeOff,
                        contentDescription = if (state.isPasswordVisible) "Hide password" else "Show password",
                        tint = Color.Gray
                    )
                }
            },
            visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            textStyle = TextStyle(
                color = Color.Black,
                fontWeight = FontWeight.Medium,
            ),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = if (state.passwordError != null) Color(0xFFD32F2F) else Color.LightGray.copy(
                    alpha = 0.8f
                ),
                focusedBorderColor = if (state.passwordError != null) Color(0xFFD32F2F) else Color.Gray,
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                errorBorderColor = Color(0xFFD32F2F),
                errorContainerColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    if (state.email.isNotEmpty() && state.password.isNotEmpty()) {
                        authVm.signIn(state.email, state.password)
                    }
                }
            ),
            isError = state.passwordError != null,
            singleLine = true,
            supportingText = if (state.passwordError != null) {
                {
                    Text(
                        text = state.passwordError,
                        color = Color(0xFFD32F2F),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else null
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Remember me and Forgot password
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = false,
                    onCheckedChange = { Log.d("checkbox", "onCheckedChange: $it") },
                    colors = CheckboxDefaults.colors(
                        uncheckedColor = Color.Gray,
                        checkedColor = Color.Black
                    )
                )
                Text(
                    text = "Remember me",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            TextButton(onClick = onForgotPasswordClick) {
                Text(
                    text = "Forgot password?",
                    color = Color.Black,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Login Button
        Button(
            onClick = {
                focusManager.clearFocus()
                authVm.signIn(state.email, state.password)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentColor,
                disabledContainerColor = AccentColor.copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = !state.isLoading
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Login",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Or divider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(Color.LightGray)
            )
            Text(
                text = "Or",
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(Color.LightGray)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Google Sign In
        Button(
            onClick = onGoogleSignInClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = !state.isLoading
        ) {
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Continue with Google",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Apple Sign In
        Button(
            onClick = onAppleSignInClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = !state.isLoading
        ) {
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Continue with Apple",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Sign up text
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.Gray)) {
                    append("Don't have account? ")
                }
                withStyle(style = SpanStyle(color = Color.Black, fontWeight = FontWeight.Bold)) {
                    append("Sign up")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !state.isLoading) { onSignUpClick() },
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}