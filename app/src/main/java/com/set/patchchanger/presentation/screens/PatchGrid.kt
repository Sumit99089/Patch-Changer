package com.set.patchchanger.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.zIndex
import com.set.patchchanger.domain.model.PatchData
import com.set.patchchanger.domain.model.PatchSlot
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.core.graphics.toColorInt

/**
 * Internal state holder for drag-and-drop operations within the grid.
 */
private data class DragState(
    val isDragging: Boolean = false,
    val draggedSlot: PatchSlot? = null,
    val dragOffset: Offset = Offset.Zero,
    val dropTargetSlot: PatchSlot? = null
)

@Composable
fun PatchGrid(
    patchData: PatchData,
    currentBankIndex: Int,
    currentPageIndex: Int,
    isEditMode: Boolean,
    onSlotClick: (PatchSlot) -> Unit,
    onSlotEdit: (PatchSlot) -> Unit,
    onSlotSwap: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val page = patchData.banks.getOrNull(currentBankIndex)?.pages?.getOrNull(currentPageIndex)
    val slots = page?.slots

    var dragState by remember { mutableStateOf(DragState()) }
    val slotBounds = remember { mutableMapOf<Int, Rect>() }
    val density = LocalDensity.current

    // Store the grid's own offset relative to the root
    var gridOffset by remember { mutableStateOf(Offset.Zero) }

    if (slots != null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                // Get the grid's position relative to the root
                .onGloballyPositioned {
                    gridOffset = it.localToRoot(Offset.Zero)
                }
        ) {
            // The 4x4 Grid
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                repeat(4) { rowIndex ->
                    Row(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        repeat(4) { colIndex ->
                            val slotIndex = rowIndex * 4 + colIndex
                            if (slotIndex < slots.size) {
                                val slot = slots[slotIndex]
                                PatchSlotItem(
                                    slot = slot,
                                    isEditMode = isEditMode,
                                    isBeingDragged = dragState.draggedSlot?.id == slot.id,
                                    isDropTarget = dragState.dropTargetSlot?.id == slot.id,
                                    onGloballyPositioned = { bounds -> slotBounds[slot.id] = bounds },
                                    onDragStart = {
                                        dragState = dragState.copy(isDragging = true, draggedSlot = slot, dragOffset = Offset.Zero)
                                    },
                                    onDragEnd = {
                                        dragState.dropTargetSlot?.let { target ->
                                            dragState.draggedSlot?.let { source ->
                                                if (source.id != target.id) onSlotSwap(source.id, target.id)
                                            }
                                        }
                                        dragState = DragState()
                                    },
                                    onDrag = { offsetChange ->
                                        if (dragState.isDragging) {
                                            dragState = dragState.copy(dragOffset = dragState.dragOffset + offsetChange)
                                            val dragCenter = slotBounds[dragState.draggedSlot?.id]?.center?.plus(dragState.dragOffset)
                                            if (dragCenter != null) {
                                                val targetEntry = slotBounds.entries.find { (id, bounds) ->
                                                    id != dragState.draggedSlot?.id && bounds.contains(dragCenter)
                                                }
                                                dragState = dragState.copy(
                                                    dropTargetSlot = targetEntry?.let { entry -> slots.find { s -> s.id == entry.key } }
                                                )
                                            }
                                        }
                                    },
                                    onClick = { if (isEditMode) onSlotEdit(slot) else onSlotClick(slot) }
                                )
                            } else {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Drag Ghost Overlay
            if (dragState.isDragging && dragState.draggedSlot != null) {
                val slot = dragState.draggedSlot!!
                val bounds = slotBounds[slot.id]
                if (bounds != null) {
                    val widthInDp = with(density) { bounds.width.toDp() }
                    val heightInDp = with(density) { bounds.height.toDp() }

                    Box(
                        modifier = Modifier
                            .zIndex(10f)
                            .offset {
                                // FIX:
                                // bounds.topLeft is the slot's absolute screen position
                                // gridOffset is the grid's absolute screen position
                                // (bounds.topLeft - gridOffset) is the slot's position *relative to the grid*
                                // Then we add the dragOffset
                                val relativeTopLeft = bounds.topLeft - gridOffset
                                IntOffset(
                                    (relativeTopLeft.x + dragState.dragOffset.x).roundToInt(),
                                    (relativeTopLeft.y + dragState.dragOffset.y).roundToInt()
                                )
                            }
                            .width(widthInDp)
                            .height(heightInDp)
                            .graphicsLayer(alpha = 0.8f, scaleX = 1.05f, scaleY = 1.05f)
                    ) {
                        PatchSlotCard(
                            slot = slot,
                            isEditMode = true,
                            isBeingDragged = false,
                            isDropTarget = false,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.PatchSlotItem(
    slot: PatchSlot,
    isEditMode: Boolean,
    isBeingDragged: Boolean,
    isDropTarget: Boolean,
    onGloballyPositioned: (Rect) -> Unit,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    onDrag: (Offset) -> Unit,
    onClick: () -> Unit
) {
    val scope = rememberCoroutineScope()

    PatchSlotCard(
        slot = slot,
        isEditMode = isEditMode,
        isBeingDragged = isBeingDragged,
        isDropTarget = isDropTarget,
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .onGloballyPositioned { layoutCoordinates ->
                onGloballyPositioned(Rect(layoutCoordinates.localToRoot(Offset.Zero), layoutCoordinates.size.toSize()))
            }
            // --- FIX ---
            // We key pointerInput not just on `isEditMode`, but also on the `slot` itself.
            // When the slot data changes (like after a swap), this key will be different,
            // forcing the pointerInput block to re-run and capture the new `onDragStart`
            // lambda, which holds a reference to the new (e.g., yellow) slot.
            .pointerInput(isEditMode, slot) {
                if (isEditMode) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = { scope.launch { onDragStart() } },
                        onDragEnd = { scope.launch { onDragEnd() } },
                        onDragCancel = { scope.launch { onDragEnd() } },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            scope.launch { onDrag(dragAmount) }
                        }
                    )
                }
            }
            .clickable { onClick() }
    )
}

@Composable
fun PatchSlotCard(
    slot: PatchSlot,
    isEditMode: Boolean,
    isBeingDragged: Boolean,
    isDropTarget: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor = try {
        Color(slot.color.toColorInt())
    } catch (e: Exception) {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val borderColor = when {
        isDropTarget -> Color.Yellow
        slot.selected && !isEditMode -> Color(0xFFFFA726)
        isEditMode -> Color.White.copy(alpha = 0.3f)
        else -> Color.Transparent
    }
    val borderWidth = when {
        isDropTarget -> 3.dp
        slot.selected || isEditMode -> 2.dp
        else -> 0.dp
    }

    Card(
        modifier = modifier.graphicsLayer { alpha = if (isBeingDragged) 0f else 1f },
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(borderWidth, borderColor),
        shape = RoundedCornerShape(6.dp)
    ) {
        Box(
            Modifier.fillMaxSize().padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = slot.getDisplayName(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                modifier = Modifier.padding(2.dp)
            )
        }
    }
}