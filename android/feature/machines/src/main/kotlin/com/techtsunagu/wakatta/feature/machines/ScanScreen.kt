package com.techtsunagu.wakatta.feature.machines

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.techtsunagu.wakatta.core.design.component.ErrorState
import com.techtsunagu.wakatta.core.design.component.LoadingState

@Composable
fun ScanScreen(
    onBackClick: () -> Unit = {},
    onScanSuccess: (String) -> Unit = {},
    viewModel: ScanViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ScanEffect.NavigateToPanel -> onScanSuccess(effect.machineId)
            }
        }
    }

    ScanContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScanContent(
    state: ScanUiState,
    onEvent: (ScanEvent) -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF0F172A),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Camera Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.machines_scan_close),
                        tint = Color.White
                    )
                }

                Text(
                    text = stringResource(R.string.machines_scan_camera_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                IconButton(onClick = { onEvent(ScanEvent.ToggleTorch) }) {
                    Icon(
                        imageVector = if (state.isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = stringResource(R.string.machines_scan_torch),
                        tint = if (state.isTorchOn) Color(0xFFFBBF24) else Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                state.isLoading -> {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        LoadingState(message = stringResource(R.string.machines_loading))
                    }
                }
                state.error != null -> {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        ErrorState(
                            message = stringResource(state.error.scanMessageRes()),
                            onRetry = { onEvent(ScanEvent.ClearError) },
                            retryLabel = stringResource(R.string.machines_retry)
                        )
                    }
                }
                else -> {
                    // Camera Viewfinder Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        ViewfinderFrame(isTorchOn = state.isTorchOn)
                    }

                    Text(
                        text = stringResource(R.string.machines_scan_point_camera),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Manual Code Input
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.machines_scan_manual_input),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFFE2E8F0)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = state.manualCodeInput,
                                    onValueChange = { onEvent(ScanEvent.ManualCodeChanged(it)) },
                                    placeholder = {
                                        Text(
                                            text = stringResource(R.string.machines_scan_code_placeholder),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { onEvent(ScanEvent.SubmitManualCode) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.height(56.dp)
                                ) {
                                    Text(text = stringResource(R.string.machines_scan_submit))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quick Chips for Demo Simulation
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 24.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.machines_scan_simulate_tip),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            state.availableMachines.forEach { machine ->
                                Surface(
                                    onClick = { onEvent(ScanEvent.SimulateScan(machine.id)) },
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color(0xFF334155),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.QrCode,
                                            contentDescription = null,
                                            tint = Color(0xFF38BDF8),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = machine.name,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
