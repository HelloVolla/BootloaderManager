package com.volla.bootmanager

import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope

class MainActivityState {
	var name by mutableStateOf("Android")
	var navController: NavHostController? = null
	@OptIn(ExperimentalMaterial3Api::class)
	var drawerState: DrawerState? = null
	var scope: CoroutineScope? = null
}