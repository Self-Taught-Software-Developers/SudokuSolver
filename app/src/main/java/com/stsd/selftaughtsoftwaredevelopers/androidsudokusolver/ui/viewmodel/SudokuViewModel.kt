package com.stsd.selftaughtsoftwaredevelopers.androidsudokusolver.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stsd.selftaughtsoftwaredevelopers.androidsudokusolver.framework.manager.SudokuSolverWorker
import com.stsd.selftaughtsoftwaredevelopers.androidsudokusolver.ui.model.BoardState
import com.stsd.selftaughtsoftwaredevelopers.androidsudokusolver.ui.model.TileState.Companion.EMPTY_TILE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SudokuViewModel @Inject constructor(
    private val worker: SudokuSolverWorker
) : ViewModel() {

    private val _sudokuBoardStateAlt = MutableStateFlow(BoardState())
    val sudokuBoardStateAlt : StateFlow<BoardState> = _sudokuBoardStateAlt.asStateFlow()

    private val _selectedPosition = MutableStateFlow(Pair(0, 0))
    val selectedPosition : StateFlow<Pair<Int, Int>> = _selectedPosition.asStateFlow()

    fun updateSelectedPosition(
        newPosition: Pair<Int, Int>
    ) = _selectedPosition.update { newPosition }

    /**
     * Call this function when a numbered button is clicked.
     */
    fun enterNewValue(newValue: String) = viewModelScope.launch {

        _sudokuBoardStateAlt.update { state ->

            val position = selectedPosition.value

            return@update BoardState(
                board = state.board.copyOf().apply {
                    this[position.first][position.second].text = newValue
                }
            )

        }

    }

    fun unDoRecentChange() = viewModelScope.launch {

        _sudokuBoardStateAlt.update { state ->

            val position = selectedPosition.value

            return@update BoardState(
                board = state.board.copyOf().apply {
                    this[position.first][position.second].text = EMPTY_TILE
                }
            )

        }

    }

    fun clearBoard() = _sudokuBoardStateAlt.update { BoardState() }

    fun solveBoard() = viewModelScope.launch(Dispatchers.Default) {

        _sudokuBoardStateAlt.update { board ->

            val solved = CompletableDeferred<BoardState>().apply {
                worker.solveBoard(board.convertFromUiBoard())
                    .also { deferred ->
                        complete(deferred)
                    }
            }.await()

            return@update solved

        }

    }

}