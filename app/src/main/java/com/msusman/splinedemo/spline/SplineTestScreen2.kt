package com.msusman.splinedemo.spline

import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.msusman.splinedemo.App
import com.msusman.splinedemo.R
import com.spr.jetpack_loading.components.indicators.gridIndicator.GridPulsatingDot

@Composable
fun SplineTestScreen2() {
    val splineView = remember { App.instance.splineEngine.splineView }
    val isLoaded = App.instance.splineEngine.engineInitialized.collectAsState()

    LaunchedEffect(Unit) {
        App.instance.splineEngine.initialize(R.raw.original_seed)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.app_background),
            contentScale = ContentScale.FillBounds,
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(400.dp),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    modifier = Modifier
                        .size(400.dp),
                    factory = { ctx ->
                        splineView
                    }
                )
                androidx.compose.animation.AnimatedVisibility(
                    visible = !isLoaded.value,
                    exit = fadeOut()
                ) {
                    GridPulsatingDot()
                }
            }

            Button(onClick = {
                App.instance.splineEngine.reInit(R.raw.blue_orb)
            }) {
                Text("Next")
            }

        }
    }
}
