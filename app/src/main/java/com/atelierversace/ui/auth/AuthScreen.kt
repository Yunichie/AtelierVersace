package com.atelierversace.ui.auth

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atelierversace.ui.components.*
import com.atelierversace.ui.theme.*

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthSuccess: () -> Unit
) {
    val authState by viewModel.authState.collectAsState()
    var isLogin by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showResetPassword by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var displayNameError by remember { mutableStateOf<String?>(null) }

    var showResetSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            onAuthSuccess()
        }
    }

    fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "Email is required"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email format"
            else -> null
        }
    }

    fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Password is required"
            password.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }
    }

    fun validateDisplayName(name: String): String? {
        return when {
            name.isBlank() -> "Display name is required"
            name.length < 2 -> "Display name must be at least 2 characters"
            else -> null
        }
    }

    LaunchedEffect(isLogin) {
        emailError = null
        passwordError = null
        displayNameError = null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(SoftPeriwinkle, Color(0xFFF8F7FF), Color(0xFFF5F5F5))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                IceBlue.copy(alpha = 0.3f),
                                IceBlue.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Spa,
                    contentDescription = null,
                    modifier = Modifier.size(50.dp),
                    tint = IceBlue
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Atelier Versace",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp
                ),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your personal fragrance stylist",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(48.dp))

            AnimatedContent(
                targetState = isLogin,
                transitionSpec = {
                    fadeIn() + slideInVertically() togetherWith fadeOut() + slideOutVertically()
                },
                label = "auth_mode"
            ) { loginMode ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (loginMode) "Welcome Back" else "Create Account",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextPrimary
                    )

                    if (!loginMode) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = Color.White.copy(alpha = 0.3f),
                                borderColor = if (displayNameError != null) {
                                    Cornflower.copy(alpha = 0.6f)
                                } else {
                                    Color.White.copy(alpha = 0.5f)
                                }
                            ) {
                                TextField(
                                    value = displayName,
                                    onValueChange = {
                                        displayName = it
                                        displayNameError = null
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = {
                                        Text("Display Name", color = TextSecondary.copy(alpha = 0.7f))
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = IceBlue
                                        )
                                    },
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        cursorColor = IceBlue
                                    ),
                                    singleLine = true,
                                    isError = displayNameError != null
                                )
                            }

                            displayNameError?.let { error ->
                                Text(
                                    text = error,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Cornflower,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = Color.White.copy(alpha = 0.3f),
                            borderColor = if (emailError != null) {
                                Cornflower.copy(alpha = 0.6f)
                            } else {
                                Color.White.copy(alpha = 0.5f)
                            }
                        ) {
                            TextField(
                                value = email,
                                onValueChange = {
                                    email = it
                                    emailError = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text("Email", color = TextSecondary.copy(alpha = 0.7f))
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null,
                                        tint = IceBlue
                                    )
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = IceBlue
                                ),
                                singleLine = true,
                                isError = emailError != null
                            )
                        }

                        emailError?.let { error ->
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodySmall,
                                color = Cornflower,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = Color.White.copy(alpha = 0.3f),
                            borderColor = if (passwordError != null) {
                                Cornflower.copy(alpha = 0.6f)
                            } else {
                                Color.White.copy(alpha = 0.5f)
                            }
                        ) {
                            TextField(
                                value = password,
                                onValueChange = {
                                    password = it
                                    passwordError = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text("Password", color = TextSecondary.copy(alpha = 0.7f))
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = IceBlue
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { showPassword = !showPassword }) {
                                        Icon(
                                            imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = null,
                                            tint = TextSecondary
                                        )
                                    }
                                },
                                visualTransformation = if (showPassword) {
                                    VisualTransformation.None
                                } else {
                                    PasswordVisualTransformation()
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = IceBlue
                                ),
                                singleLine = true,
                                isError = passwordError != null
                            )
                        }

                        passwordError?.let { error ->
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodySmall,
                                color = Cornflower,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }

                    if (loginMode) {
                        TextButton(
                            onClick = { showResetPassword = true },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(
                                "Forgot password?",
                                color = IceBlue,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    GlassButton(
                        onClick = {
                            if (loginMode) {
                                emailError = validateEmail(email)
                                passwordError = validatePassword(password)

                                if (emailError == null && passwordError == null) {
                                    viewModel.signIn(email, password)
                                }
                            } else {
                                displayNameError = validateDisplayName(displayName)
                                emailError = validateEmail(email)
                                passwordError = validatePassword(password)

                                if (displayNameError == null && emailError == null && passwordError == null) {
                                    viewModel.signUp(email, password, displayName)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (authState is AuthState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (loginMode) Icons.Default.Login else Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (loginMode) "Sign In" else "Create Account",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (authState is AuthState.Error) {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = Cornflower.copy(alpha = 0.1f),
                            borderColor = Cornflower.copy(alpha = 0.3f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    tint = Cornflower,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = (authState as AuthState.Error).message,
                                    color = Cornflower,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (loginMode) "Don't have an account?" else "Already have an account?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        TextButton(onClick = {
                            isLogin = !isLogin
                            email = ""
                            password = ""
                            displayName = ""
                        }) {
                            Text(
                                text = if (loginMode) "Sign Up" else "Sign In",
                                color = IceBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }

    if (showResetPassword) {
        var resetEmail by remember { mutableStateOf(email) }
        var resetEmailError by remember { mutableStateOf<String?>(null) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            GlassCard(
                modifier = Modifier.padding(32.dp),
                backgroundColor = Color.White.copy(alpha = 0.95f),
                borderColor = Color.White.copy(alpha = 0.6f),
                cornerRadius = 24.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Reset Password",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )

                    Text(
                        "Enter your email to receive a password reset link",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = Color.White.copy(alpha = 0.3f),
                            borderColor = if (resetEmailError != null) {
                                Cornflower.copy(alpha = 0.6f)
                            } else {
                                Color.White.copy(alpha = 0.5f)
                            }
                        ) {
                            TextField(
                                value = resetEmail,
                                onValueChange = {
                                    resetEmail = it
                                    resetEmailError = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Email", color = TextSecondary.copy(alpha = 0.7f)) },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = IceBlue
                                ),
                                singleLine = true,
                                isError = resetEmailError != null
                            )
                        }

                        resetEmailError?.let { error ->
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodySmall,
                                color = Cornflower,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedGlassButton(
                            onClick = {
                                showResetPassword = false
                                resetEmailError = null
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = TextSecondary)
                        }

                        GlassButton(
                            onClick = {
                                resetEmailError = validateEmail(resetEmail)
                                if (resetEmailError == null) {
                                    viewModel.resetPassword(resetEmail)
                                    showResetPassword = false
                                    showResetSuccess = true
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Send Link", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    if (showResetSuccess) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(3000)
            showResetSuccess = false
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            GlassCard(
                modifier = Modifier.padding(horizontal = 32.dp),
                backgroundColor = IceBlue.copy(alpha = 0.9f),
                borderColor = Color.White.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Text(
                        "Password reset link sent! Check your email.",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}