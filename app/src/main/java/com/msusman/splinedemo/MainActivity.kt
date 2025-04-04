package com.msusman.splinedemo

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ConfigurationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.msusman.splinedemo.ui.screen.SplineTestScreen
import com.msusman.splinedemo.ui.theme.SplineDemoTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SplineDemoTheme {
                SplineTestScreen()
            }
        }
        checkDeviceCapabilities()
    }

    private fun checkDeviceCapabilities() {
        val supportedAbis = Build.SUPPORTED_ABIS.joinToString()
        val sdkVersion = Build.VERSION.SDK_INT
        val release = Build.VERSION.RELEASE

        val isVulkanSupported =
            packageManager.hasSystemFeature(PackageManager.FEATURE_VULKAN_HARDWARE_LEVEL)
        val vulkanDepqLevel =
            packageManager.hasSystemFeature(PackageManager.FEATURE_VULKAN_DEQP_LEVEL)
        val vulkanHardwareCompute =
            packageManager.hasSystemFeature(PackageManager.FEATURE_VULKAN_HARDWARE_COMPUTE)
        val vulkanHardvareversion =
            packageManager.hasSystemFeature(PackageManager.FEATURE_VULKAN_HARDWARE_VERSION)
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val configurationInfo: ConfigurationInfo = activityManager.deviceConfigurationInfo
        val glEsVersion = configurationInfo.glEsVersion
        Log.d(
            "checkDeviceCapabilities",
            "Supported ABIs: $supportedAbis, SDK Version: $sdkVersion, Release: $release, GL ES Version: $glEsVersion, Vulkan Supported: $isVulkanSupported, Vulkan DEQP Level: $vulkanDepqLevel, Vulkan Hardware Compute: $vulkanHardwareCompute, Vulkan Hardware Version: $vulkanHardvareversion"
        )

    }


}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SplineDemoTheme {
        Greeting("Android")
    }
}