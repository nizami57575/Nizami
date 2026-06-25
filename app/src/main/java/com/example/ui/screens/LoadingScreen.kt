package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.SearchState
import com.example.ui.SimaViewModel

@Composable
fun LoadingScreen(
    viewModel: SimaViewModel,
    imageUri: String,
    onNavigateToDetails: (Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    val searchState by viewModel.searchState.collectAsState()

    // Trigger the search exactly once when the screen appears
    LaunchedEffect(imageUri) {
        viewModel.startFaceSearch(imageUri)
    }

    // Handle navigation on Success
    LaunchedEffect(searchState) {
        if (searchState is SearchState.Success) {
            onNavigateToDetails((searchState as SearchState.Success).scanId)
            viewModel.resetSearchState()
        }
    }

    when (val state = searchState) {
        is SearchState.Scanning -> {
            ScanningUI(
                imageUri = imageUri,
                progress = state.progress,
                currentStep = state.currentStep
            )
        }
        is SearchState.Error -> {
            ErrorUI(
                errorMessage = state.message,
                onBack = {
                    viewModel.resetSearchState()
                    onNavigateBack()
                }
            )
        }
        else -> {
            // Idle or transitioning to success
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F1115)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF818CF8))
            }
        }
    }
}

@Composable
fun ScanningUI(
    imageUri: String,
    progress: Int,
    currentStep: String
) {
    // Holographic animated scan line offsets
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val scanProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1115)) // Cinematic deep slate background
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Holographic Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                "SİMA SİSTEMİ SKAN EDİR",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFA5B4FC),
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Biometrik Analiz və Doğrulama",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Animated Photo Scan Frame
        BoxWithConstraints(
            modifier = Modifier
                .size(260.dp)
                .clip(RoundedCornerShape(28.dp))
                .border(2.dp, Color(0xFF818CF8).copy(alpha = 0.5f), RoundedCornerShape(28.dp))
                .background(Color(0xFF020617)),
            contentAlignment = Alignment.TopCenter
        ) {
            val h = maxHeight

            // User Photo
            AsyncImage(
                model = imageUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Scanning laser line
            val yOffset = h * scanProgress
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .offset(y = yOffset)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFF818CF8),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Glowing aura overlay behind laser
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .offset(y = yOffset - 20.dp)
                    .blur(12.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFF818CF8).copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // HUD Grid Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, Color(0xFF818CF8).copy(alpha = 0.1f), RoundedCornerShape(28.dp))
            )
        }

        // Terminal Progress Console log block as Glass Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.04f))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "SİMULYASİYA EDİLƏN DOĞRULAMA",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )
                Text(
                    "$progress%",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFA5B4FC)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape),
                color = Color(0xFF818CF8),
                trackColor = Color(0xFF818CF8).copy(alpha = 0.1f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Current Terminal Step Row
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 1.5.dp,
                    color = Color(0xFF818CF8)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = currentStep,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

@Composable
fun ErrorUI(
    errorMessage: String,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1115)) // Deep slate background
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.BugReport,
                contentDescription = null,
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Axtarış Baş Tutmadı",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            errorMessage,
            fontSize = 14.sp,
            color = Color(0xFF94A3B8),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Geri Qayıt", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
