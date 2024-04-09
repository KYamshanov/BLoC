package ru.kyamshanov.bloc

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*

fun main() {
    val someRootViewModel = SomeRootViewModel()
    runBlocking {
        launch {
            someRootViewModel.someComponent.commands.collect {
                when (it) {
                    is SomeCommand.Print -> println(it.text)
                }
            }
        }
        launch {
            someRootViewModel.someComponent.event.tryEmit(SightEvent.OnClickDone)
            if(someRootViewModel.someComponent.event.tryEmit(SightEvent.OnClickDone).not()){
                println("error")
            }
            delay(1500L)
            someRootViewModel.someComponent.event.tryEmit(SightEvent.OnClickDone)
        }
        delay(1200L)
        someRootViewModel.onCleared()
    }
    println("Hello world")
}

/**
 * about my BLoC architect
 */

interface EventCollector<T> : FlowCollector<T> {

    fun tryEmit(value: T): Boolean
}

fun <T> MutableSharedFlow<T>.asEventCollector(): EventCollector<T> = object : EventCollector<T> {
    override suspend fun emit(value: T) = this@asEventCollector.emit(value)
    override fun tryEmit(value: T): Boolean = this@asEventCollector.tryEmit(value)
}

interface Component<State, Command, Event> {

    val state: StateFlow<State>

    val commands: Flow<Command>

    val event: EventCollector<Event>
}

class SomeRootViewModel {

    private val viewModelScope = CoroutineScope(SupervisorJob())

    fun onCleared() {
        println("End")
        viewModelScope.cancel()
    }

    val someComponent: Component<Unit, SomeCommand, SightEvent> = SomeComponent(
        CoroutineScope(SupervisorJob() + viewModelScope.coroutineContext)
    )


}

class SomeComponent(
    private val coroutineScope: CoroutineScope
) : Component<Unit, SomeCommand, SightEvent> {

    private val _state = MutableStateFlow(Unit)
    private val _commands =
        MutableSharedFlow<SomeCommand>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val _event = MutableSharedFlow<SightEvent>(extraBufferCapacity = 1)


    override val state: StateFlow<Unit> = _state.asStateFlow()
    override val commands: Flow<SomeCommand> = _commands.asSharedFlow()
    override val event: EventCollector<SightEvent> = _event.asEventCollector()


    init {
        coroutineScope.launch {
            _event.collect { handleEvent(it) }
        }
        coroutineScope.coroutineContext.job.invokeOnCompletion {
            println("End")
        }
    }

    private suspend fun handleEvent(event: SightEvent): Unit = when (event) {
        SightEvent.OnClickDone -> {
            println("Event")
            delay(1000L)
            _commands.emit(SomeCommand.Print("TEXT"))
        }
    }
}

sealed interface SomeCommand {


    data class Print(
        val text: String
    ) : SomeCommand
}

sealed interface SightEvent {


    data object OnClickDone : SightEvent
}