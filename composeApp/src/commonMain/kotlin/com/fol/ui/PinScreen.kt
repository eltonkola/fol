package com.fol.ui

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Password
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fol.com.fol.crypto.CryptoManager
import com.fol.com.fol.ui.elements.KeyboardUi
import fol.composeapp.generated.resources.Res
import fol.composeapp.generated.resources.compose_multiplatform
import fol.composeapp.generated.resources.landing_background
import fol.composeapp.generated.resources.logo
import fol.composeapp.generated.resources.logo_ere
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import kotlin.random.Random


@Composable
fun PinScreen(
    viewModel: SplashViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Enter Pin") },
//                actions = {
//
//                    val coroutineScope = rememberCoroutineScope()
//
//                    IconButton(onClick = {
//                        coroutineScope.launch {
//                            CryptoManager.testValidity()
//                        }
//                    }) {
//                        Icon(Icons.Default.Key, contentDescription = "Settings")
//                    }
//
//
//                }
//            )
//        },

        content = { padding ->

            Box(
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {

                //AnimatedGradientBackground()
//                CreativeAnimatedBackground()
                AnimatedGradientWithParticlesAndNeurons()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.4f),
                                    MaterialTheme.colorScheme.background,
                                    MaterialTheme.colorScheme.background
                                )
                            )
                        )
                )


                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {


                    Text(
                        text = "FOL",
                        fontSize = 90.sp
                    )
                    Image(
                        modifier = Modifier.size(280.dp, 200.dp),
                        painter = painterResource(Res.drawable.logo_ere),
                        contentDescription = "logo",
                        contentScale = ContentScale.Fit
                    )

                    Text(
                        text = "Fortress Of Letters",
                        fontSize = 30.sp
                    )



                    Spacer(modifier = Modifier.weight(1f))


                    OutlinedTextField(
                        value = uiState.pin,
                        onValueChange = {},
                        readOnly = true,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "To use the app, enter the secret code.",
                            )
                        },
                        shape = MaterialTheme.shapes.medium,
                        leadingIcon = {
                            Icon(
                                modifier = Modifier.size(16.dp),
                                imageVector = Icons.Default.Password, contentDescription = null
                            )
                        },
                        trailingIcon = {
                            if (uiState.pin.isNotBlank()) {
                                IconButton(onClick = {
                                    viewModel.updatePin("")
                                }) {
                                    Icon(
                                        modifier = Modifier.size(16.dp),
                                        imageVector = Icons.Default.Clear, contentDescription = null
                                    )
                                }
                            }
                        },

                        supportingText = {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = "Pin needs to be 6 chars: ${uiState.pin.length}/6",
                                textAlign = TextAlign.End
                            )

                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    KeyboardUi(
                        query = uiState.pin,
                        onQueryChange = { viewModel.updatePin(it) },
                        onDone = {
                            if (uiState.pin.length <= 6) {
                                viewModel.authenticateUser()
                            }
                        }
                    )
                }
            }
        }
    )

}

@Composable
fun AnimatedGradientBackground() {
    val colors = listOf(
        MaterialTheme.colorScheme.background,
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer,
        MaterialTheme.colorScheme.surfaceContainer,
    )

    val infiniteTransition = rememberInfiniteTransition()

    val animatedColor1 = infiniteTransition.animateColor(
        initialValue = colors[0],
        targetValue = colors[1],
        animationSpec = infiniteRepeatable(
            animation = tween(20000),
            repeatMode = RepeatMode.Reverse
        )
    )

    val animatedColor2 = infiniteTransition.animateColor(
        initialValue = colors[2],
        targetValue = colors[3],
        animationSpec = infiniteRepeatable(
            animation = tween(20000),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        animatedColor1.value,
                        animatedColor2.value
                    )
                )
            )
    )
}


@Composable
fun CreativeAnimatedBackground() {
    val colors = listOf(
        MaterialTheme.colorScheme.background,
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer,
        MaterialTheme.colorScheme.surfaceContainer,
    )
    val infiniteTransition = rememberInfiniteTransition()

    val animatedColor1 = infiniteTransition.animateColor(
        initialValue = colors[0],
        targetValue = colors[1],
        animationSpec = infiniteRepeatable(
            animation = tween(5000),
            repeatMode = RepeatMode.Reverse
        )
    )

    val animatedColor2 = infiniteTransition.animateColor(
        initialValue = colors[2],
        targetValue = colors[3],
        animationSpec = infiniteRepeatable(
            animation = tween(5000),
            repeatMode = RepeatMode.Reverse
        )
    )

    var particles by remember { mutableStateOf(List(50) { Particle() }) }
    var neurons by remember { mutableStateOf(List(10) { Neuron() }) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(16) // Roughly 60 FPS
            particles = particles.map { it.update() }
            neurons = neurons.map { it.update() }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        animatedColor1.value,
                        animatedColor2.value
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawParticles(particles)
            drawNeurons(neurons)
        }
    }
}

private fun DrawScope.drawParticles(particles: List<Particle>) {
    particles.forEach { particle ->
        drawCircle(
            color = Color.White.copy(alpha = particle.alpha),
            radius = particle.size,
            center = Offset(particle.x, particle.y)
        )
    }
}

private fun DrawScope.drawNeurons(neurons: List<Neuron>) {
    neurons.forEach { neuron ->
        drawCircle(
            color = Color.White.copy(alpha = 0.5f),
            radius = 5f,
            center = Offset(neuron.x, neuron.y)
        )
        neurons.forEach { otherNeuron ->
            val distance = calculateDistance(neuron, otherNeuron)
            if (distance < 200) {
                drawLine(
                    color = Color.White.copy(alpha = (1 - distance / 200) * 0.3f),
                    start = Offset(neuron.x, neuron.y),
                    end = Offset(otherNeuron.x, otherNeuron.y),
                    strokeWidth = 1f
                )
            }
        }
    }
}

private fun calculateDistance(a: Neuron, b: Neuron): Float {
    return kotlin.math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y))
}

class Particle(
    var x: Float = Random.nextFloat() * 1000,
    var y: Float = Random.nextFloat() * 2000,
    var speed: Float = Random.nextFloat() * 2 + 1,
    var size: Float = Random.nextFloat() * 5 + 2,
    var alpha: Float = Random.nextFloat() * 0.5f + 0.1f
) {
    fun update(): Particle {
        y -= speed
        if (y < 0) {
            y = 2000f
            x = Random.nextFloat() * 1000
        }
        return this
    }
}

class Neuron(
    var x: Float = Random.nextFloat() * 1000,
    var y: Float = Random.nextFloat() * 2000,
    var speedX: Float = Random.nextFloat() * 2 - 1,
    var speedY: Float = Random.nextFloat() * 2 - 1
) {
    fun update(): Neuron {
        x += speedX
        y += speedY
        if (x < 0 || x > 1000) speedX *= -1
        if (y < 0 || y > 2000) speedY *= -1
        return this
    }
}



@Composable
fun AnimatedGradientWithParticles() {
    val colors = listOf(
        MaterialTheme.colorScheme.background,
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer,
        MaterialTheme.colorScheme.surfaceContainer,
    )

    val infiniteTransition = rememberInfiniteTransition()

    val animatedColor1 = infiniteTransition.animateColor(
        initialValue = colors[0],
        targetValue = colors[1],
        animationSpec = infiniteRepeatable(
            animation = tween(10000),
            repeatMode = RepeatMode.Reverse
        )
    )

    val animatedColor2 = infiniteTransition.animateColor(
        initialValue = colors[2],
        targetValue = colors[3],
        animationSpec = infiniteRepeatable(
            animation = tween(10000),
            repeatMode = RepeatMode.Reverse
        )
    )

    var particles by remember { mutableStateOf(List(100) { Particle() }) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(16) // Roughly 60 FPS
            particles = particles.map { it.update() }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw the animated gradient background
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        animatedColor1.value,
                        animatedColor2.value
                    )
                )
            )

            // Draw the particles
            drawParticles(particles)
        }
    }
}

@Composable
fun AnimatedGradientWithParticlesAndNeurons() {
    val colors = listOf(
        MaterialTheme.colorScheme.background,
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer,
        MaterialTheme.colorScheme.surfaceContainer,
    )

    val infiniteTransition = rememberInfiniteTransition()

    val animatedColor1 = infiniteTransition.animateColor(
        initialValue = colors[0],
        targetValue = colors[1],
        animationSpec = infiniteRepeatable(
            animation = tween(10000),
            repeatMode = RepeatMode.Reverse
        )
    )

    val animatedColor2 = infiniteTransition.animateColor(
        initialValue = colors[2],
        targetValue = colors[3],
        animationSpec = infiniteRepeatable(
            animation = tween(10000),
            repeatMode = RepeatMode.Reverse
        )
    )

    var particles by remember { mutableStateOf(List(100) { Particle() }) }
    var neurons by remember { mutableStateOf(List(15) { Neuron() }) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(16) // Roughly 60 FPS
            particles = particles.map { it.update() }
            neurons = neurons.map { it.update() }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw the animated gradient background
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        animatedColor1.value,
                        animatedColor2.value
                    )
                )
            )

            // Draw the particles
            drawParticles(particles)

            // Draw the neurons
            drawNeurons(neurons)
        }
    }
}
