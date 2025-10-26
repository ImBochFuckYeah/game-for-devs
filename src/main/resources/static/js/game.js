/**
 * Game For Devs - JavaScript Principal
 * Maneja toda la lógica del juego "Codifica con Guali"
 */

class GameManager {
    constructor() {
        this.currentTrack = null;
        this.gameSession = null;
        this.robot = {
            x: 0,
            y: 0,
            direction: 'EAST', // NORTH, SOUTH, EAST, WEST
            element: null
        };
        this.moves = [];
        this.isExecuting = false;
        this.isInsideLoop = false;
        this.executionStartTime = null;
        this.visitedCells = new Set();
        this.attemptsCount = 0; // Contador de intentos de ejecución
        
        // Estado de ejecución para continuidad
        this.lastExecutionState = {
            x: null,
            y: null,
            direction: null,
            wasSuccessful: false,
            executedMovesCount: 0  // Cantidad de movimientos ya ejecutados
        };
        
        this.initializeEventListeners();
    }
    
    /**
     * Inicializa los event listeners
     */
    initializeEventListeners() {
        // Botones de control
        document.getElementById('btnForward')?.addEventListener('click', () => this.addMove('FORWARD'));
        document.getElementById('btnLeft')?.addEventListener('click', () => this.addMove('LEFT'));
        document.getElementById('btnRight')?.addEventListener('click', () => this.addMove('RIGHT'));
        document.getElementById('btnLoop')?.addEventListener('click', () => this.toggleLoop());
        
        // Botones de acción
        document.getElementById('btnExecute')?.addEventListener('click', () => this.executeSequence());
        document.getElementById('btnReset')?.addEventListener('click', () => this.resetGame());
        document.getElementById('btnClearMoves')?.addEventListener('click', () => this.clearMoves());
        
        // Event listeners para cerrar modales
        document.addEventListener('click', (e) => {
            if (e.target.classList.contains('game-modal')) {
                this.closeModals();
            }
        });
    }
    
    /**
     * Inicializa el juego
     */
    async initializeGame(excludeCurrentTrack = false) {
        this.showLoadingModal();
        try {
            // Verificar si hay una pista específica cargada desde el servidor
            if (window.gameConfig && window.gameConfig.track) {
                await this.loadSpecificTrack(window.gameConfig.track);
            } else {
                await this.loadRandomTrack(excludeCurrentTrack);
            }
            this.createGameGrid();
            this.resetRobotPosition();
            this.updateUI();
        } catch (error) {
            console.error('Error al inicializar el juego:', error);
            this.showErrorModal('Error al cargar el juego. Por favor, recarga la página.');
        } finally {
            this.closeModals();
        }
    }
    
    /**
     * Carga una pista aleatoria
     */
    async loadRandomTrack(excludeCurrentTrack = false) {
        console.log('🎲 Solicitando pista aleatoria...');
        try {
            let url = '/api/game/track/random';
            
            // Si se debe excluir la pista actual y existe una pista cargada
            if (excludeCurrentTrack && this.currentTrack && this.currentTrack.id) {
                url += `?excludeId=${this.currentTrack.id}`;
                console.log('🚫 Excluyendo pista actual ID:', this.currentTrack.id);
            }
            
            const response = await fetch(url);
            console.log('📡 Respuesta del servidor:', response.status, response.statusText);
            
            if (!response.ok) {
                const errorText = await response.text();
                console.error('❌ Error del servidor:', errorText);
                throw new Error(`No se pudo cargar la pista: ${response.status} ${response.statusText}`);
            }
            
            const previousTrackId = this.currentTrack ? this.currentTrack.id : null;
            this.currentTrack = await response.json();
            
            // Mostrar información sobre la diferencia de pistas
            if (excludeCurrentTrack && previousTrackId) {
                if (this.currentTrack.isDifferent === false) {
                    console.log('⚠️ Se cargó la misma pista (puede ser la única disponible)');
                } else if (this.currentTrack.isDifferent === true) {
                    console.log('🔄 ¡Nueva pista diferente cargada exitosamente!');
                } else if (this.currentTrack.id === previousTrackId) {
                    console.log('⚠️ Se cargó la misma pista (servidor no retornó información de diferencia)');
                } else {
                    console.log('🔄 Nueva pista diferente cargada!');
                }
            }
            
            console.log('📋 Pista cargada:', this.currentTrack.name, 'ID:', this.currentTrack.id);
            
            // Actualizar información de la pista en la UI
            document.getElementById('trackName').textContent = this.currentTrack.name;
            document.getElementById('trackDifficulty').textContent = this.currentTrack.difficultyLevel;
            document.getElementById('trackDescription').textContent = this.currentTrack.description || 'Sin descripción';
            
            // Generar estrellas de dificultad
            const starsElement = document.getElementById('difficultyStars');
            starsElement.innerHTML = '';
            for (let i = 0; i < this.currentTrack.difficultyLevel; i++) {
                starsElement.innerHTML += '<i class="fas fa-star text-warning"></i>';
            }
            
            console.log('🎯 UI actualizada con información de la pista');
            
            // Iniciar sesión de juego
            await this.startGameSession();
            
        } catch (error) {
            console.error('💥 Error al cargar pista aleatoria:', error);
            throw error;
        }
    }
    
    /**
     * Carga una pista específica pasada desde el servidor
     */
    async loadSpecificTrack(track) {
        try {
            this.currentTrack = track;
            
            // Actualizar información de la pista en la UI
            document.getElementById('trackName').textContent = this.currentTrack.name;
            document.getElementById('trackDifficulty').textContent = this.currentTrack.difficultyLevel;
            document.getElementById('trackDescription').textContent = this.currentTrack.description || 'Sin descripción';
            
            // Generar estrellas de dificultad
            const starsElement = document.getElementById('difficultyStars');
            starsElement.innerHTML = '';
            for (let i = 0; i < this.currentTrack.difficultyLevel; i++) {
                starsElement.innerHTML += '<i class="fas fa-star text-warning"></i>';
            }
            
            // Iniciar sesión de juego
            await this.startGameSession();
            
        } catch (error) {
            console.error('Error al cargar pista específica:', error);
            throw error;
        }
    }
    
    /**
     * Inicia una nueva sesión de juego
     */
    async startGameSession() {
        try {
            const response = await fetch('/api/game/session/start', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    trackId: this.currentTrack.id
                })
            });
            
            if (response.ok) {
                const data = await response.json();
                this.gameSession = {
                    sessionId: data.sessionId,
                    trackId: data.trackId
                };
            }
        } catch (error) {
            console.error('Error al iniciar sesión de juego:', error);
        }
    }
    
    /**
     * Crea la grilla del juego
     */
    createGameGrid() {
        const gridContainer = document.getElementById('gameGrid');
        gridContainer.innerHTML = '';
        
        const gridConfig = JSON.parse(this.currentTrack.gridConfig);
        
        // Crear celdas del grid (4 filas x 5 columnas)
        for (let row = 0; row < 4; row++) {
            for (let col = 0; col < 5; col++) {
                const cell = document.createElement('div');
                cell.className = 'grid-cell';
                cell.dataset.x = col;
                cell.dataset.y = row;
                
                // Verificar si es una celda de camino
                if (gridConfig[row] && gridConfig[row][col] === 1) {
                    cell.classList.add('path');
                }
                
                gridContainer.appendChild(cell);
            }
        }
        
        // El robot se creará cuando se actualice su posición
        this.robot.element = null;
    }
    
    /**
     * Crea el elemento visual del robot
     */
    createRobotElement() {
        if (this.robot.element) {
            this.robot.element.remove();
        }
        
        this.robot.element = document.createElement('div');
        this.robot.element.className = 'robot-element';
        this.robot.element.innerHTML = '🤖'; // Flecha que indica la dirección
        this.robot.element.style.cssText = `
            font-size: 1.8rem;
            color: #e74c3c;
            font-weight: bold;
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            z-index: 10;
            transition: all 0.5s ease;
            pointer-events: none;
            text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
        `;
    }
    
    /**
     * Resetea la posición del robot
     */
    resetRobotPosition() {
        this.robot.x = this.currentTrack.startX;
        this.robot.y = this.currentTrack.startY;
        this.robot.direction = this.currentTrack.startDirection;
        this.visitedCells.clear();
        this.visitedCells.add(`${this.robot.x},${this.robot.y}`);
        
        this.updateRobotPosition();
        this.clearErrorCells();
    }
    
    /**
     * Actualiza la posición visual del robot
     */
    updateRobotPosition() {
        const targetCell = document.querySelector(`[data-x="${this.robot.x}"][data-y="${this.robot.y}"]`);
        if (targetCell) {
            // Remover robot de celdas anteriores
            document.querySelectorAll('.grid-cell').forEach(cell => {
                cell.classList.remove('robot');
                // Remover elemento del robot si existe en la celda
                const existingRobot = cell.querySelector('.robot-element');
                if (existingRobot) {
                    existingRobot.remove();
                }
            });
            
            // Agregar robot a la nueva celda
            targetCell.classList.add('robot');
            
            // Crear nuevo elemento del robot si no existe
            if (!this.robot.element) {
                this.createRobotElement();
            }
            
            // Actualizar rotación del robot según la dirección
            let rotation = 0;
            switch (this.robot.direction) {
                case 'NORTH': rotation = 0; break;
                case 'EAST': rotation = 90; break;
                case 'SOUTH': rotation = 180; break;
                case 'WEST': rotation = 270; break;
                default:
                    console.warn('Dirección desconocida:', this.robot.direction);
                    rotation = 0;
            }
            
            console.log(`Robot en (${this.robot.x}, ${this.robot.y}) mirando ${this.robot.direction} (${rotation}°)`);
            this.robot.element.style.transform = `translate(-50%, -50%) rotate(${rotation}deg)`;
            
            // Posicionar el elemento del robot dentro de la celda objetivo
            targetCell.appendChild(this.robot.element);
        }
    }
    
    /**
     * Agrega un movimiento a la lista
     */
    addMove(moveType) {
        if (this.isExecuting) return;
        
        const move = {
            type: moveType,
            id: Date.now() + Math.random()
        };
        
        if (moveType === 'LOOP_START') {
            this.isInsideLoop = true;
        } else if (moveType === 'LOOP_END') {
            this.isInsideLoop = false;
        }
        
        this.moves.push(move);
        this.updateMovesDisplay();
        this.updateUI();
    }
    
    /**
     * Alterna el estado del bucle
     */
    toggleLoop() {
        if (this.isInsideLoop) {
            this.addMove('LOOP_END');
        } else {
            this.addMove('LOOP_START');
        }
    }
    
    /**
     * Actualiza la visualización de movimientos
     */
    updateMovesDisplay() {
        const movesList = document.getElementById('movesList');
        movesList.innerHTML = '';
        
        if (this.moves.length === 0) {
            movesList.innerHTML = `
                <li class="text-muted text-center py-3">
                    <i class="fas fa-info-circle me-2"></i>
                    Selecciona movimientos usando los botones de control
                </li>
            `;
            return;
        }
        
        let indentLevel = 0;
        this.moves.forEach((move, index) => {
            const li = document.createElement('li');
            li.className = 'move-item';
            
            // Marcar movimientos ya ejecutados
            if (this.lastExecutionState.wasSuccessful && index < this.lastExecutionState.executedMovesCount) {
                li.classList.add('executed');
            }
            
            if (move.type === 'LOOP_START') {
                li.classList.add('loop-start');
                li.innerHTML = `
                    <div class="move-icon" style="background: #95a5a6;">
                        <i class="fas fa-sync"></i>
                    </div>
                    <span>Inicia Bucle</span>
                `;
                indentLevel++;
            } else if (move.type === 'LOOP_END') {
                indentLevel--;
                li.classList.add('loop-end');
                li.innerHTML = `
                    <div class="move-icon" style="background: #95a5a6;">
                        <i class="fas fa-sync"></i>
                    </div>
                    <span>Finaliza Bucle</span>
                `;
            } else {
                if (indentLevel > 0) {
                    li.classList.add('loop-content');
                }
                
                let icon, color, text;
                switch (move.type) {
                    case 'FORWARD':
                        icon = 'fas fa-arrow-up';
                        color = '#3498db';
                        text = 'Adelante';
                        break;
                    case 'LEFT':
                        icon = 'fas fa-undo';
                        color = '#f1c40f';
                        text = 'Izquierda';
                        break;
                    case 'RIGHT':
                        icon = 'fas fa-redo';
                        color = '#e67e22';
                        text = 'Derecha';
                        break;
                }
                
                li.innerHTML = `
                    <div class="move-icon" style="background: ${color};">
                        <i class="${icon}"></i>
                    </div>
                    <span>${text}</span>
                `;
            }
            
            movesList.appendChild(li);
        });
    }
    
    /**
     * Ejecuta la secuencia de movimientos
     */
    async executeSequence() {
        if (this.isExecuting || this.moves.length === 0) return;
        
        this.isExecuting = true;
        this.executionStartTime = Date.now();
        this.attemptsCount++; // Incrementar contador de intentos
        
        // Si no hay ejecución previa exitosa, resetear posición
        if (!this.lastExecutionState.wasSuccessful) {
            this.resetRobotPosition();
        } else {
            console.log('Continuando desde la posición anterior');
        }
        
        // Deshabilitar botones durante la ejecución
        this.toggleButtons(false);
        
        try {
            await this.processMovesSequence(this.moves);
            
            // Guardar estado actual como exitoso
            this.lastExecutionState = {
                x: this.robot.x,
                y: this.robot.y,
                direction: this.robot.direction,
                wasSuccessful: true,
                executedMovesCount: this.moves.length  // Todos los movimientos actuales se ejecutaron
            };
            
            await this.checkWinCondition();
        } catch (error) {
            console.error('Error durante la ejecución:', error);
            this.showErrorModal(error.message);
            
            // Marcar como no exitoso si hubo error
            this.lastExecutionState.wasSuccessful = false;
            
            // Usar coordenadas de error si están disponibles, sino usar posición actual del robot
            const errorX = error.errorX !== undefined ? error.errorX : this.robot.x;
            const errorY = error.errorY !== undefined ? error.errorY : this.robot.y;
            
            // Actualizar sesión de juego con el error
            await this.updateGameSessionWithError(error.message, errorX, errorY);
        } finally {
            this.isExecuting = false;
            this.toggleButtons(true);
        }
    }
    
    /**
     * Procesa una secuencia de movimientos
     */
    async processMovesSequence(moves) {
        // Determinar desde dónde empezar a ejecutar
        const startIndex = this.lastExecutionState.wasSuccessful ? this.lastExecutionState.executedMovesCount : 0;
        
        for (let i = startIndex; i < moves.length; i++) {
            const move = moves[i];
            
            // Destacar movimiento actual
            this.highlightCurrentMove(i);
            
            if (move.type === 'LOOP_START') {
                // Encontrar el LOOP_END correspondiente
                let loopEnd = this.findLoopEnd(moves, i);
                if (loopEnd !== -1) {
                    // Ejecutar bucle dos veces mostrando cada movimiento interno
                    for (let loopIteration = 0; loopIteration < 2; loopIteration++) {
                        console.log(`Ejecutando bucle - iteración ${loopIteration + 1}/2`);
                        // Ejecutar cada movimiento del bucle con highlighting individual
                        for (let j = i + 1; j < loopEnd; j++) {
                            const loopMove = moves[j];
                            // Destacar movimiento dentro del bucle
                            this.highlightCurrentMove(j);
                            await this.executeMove(loopMove.type);
                            await this.delay(500);
                        }
                    }
                    // Saltar al final del bucle
                    i = loopEnd;
                }
            } else if (move.type === 'LOOP_END') {
                // No hacer nada, se maneja en LOOP_START
            } else {
                await this.executeMove(move.type);
            }
            
            await this.delay(500); // Pausa entre movimientos
        }
    }
    
    /**
     * Encuentra el LOOP_END correspondiente a un LOOP_START
     */
    findLoopEnd(moves, startIndex) {
        let loopCount = 0;
        for (let i = startIndex; i < moves.length; i++) {
            if (moves[i].type === 'LOOP_START') {
                loopCount++;
            } else if (moves[i].type === 'LOOP_END') {
                loopCount--;
                if (loopCount === 0) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    /**
     * Ejecuta un movimiento individual
     */
    async executeMove(moveType) {
        switch (moveType) {
            case 'FORWARD':
                await this.moveForward();
                break;
            case 'LEFT':
                this.turnLeft();
                break;
            case 'RIGHT':
                this.turnRight();
                break;
        }
    }
    
    /**
     * Mueve el robot hacia adelante
     */
    async moveForward() {
        let newX = this.robot.x;
        let newY = this.robot.y;
        
        switch (this.robot.direction) {
            case 'NORTH': newY--; break;
            case 'SOUTH': newY++; break;
            case 'EAST': newX++; break;
            case 'WEST': newX--; break;
        }
        
        console.log(`Movimiento: (${this.robot.x}, ${this.robot.y}) → (${newX}, ${newY}) [${this.robot.direction}]`);
        
        // Validar movimiento
        if (!this.isValidPosition(newX, newY)) {
            this.markErrorPosition(newX, newY);
            const error = new Error('Movimiento inválido: El robot no puede moverse a esa posición.');
            error.errorX = newX;
            error.errorY = newY;
            throw error;
        }
        
        // Actualizar posición
        this.robot.x = newX;
        this.robot.y = newY;
        this.visitedCells.add(`${newX},${newY}`);
        
        this.updateRobotPosition();
    }
    
    /**
     * Gira el robot a la izquierda
     */
    turnLeft() {
        const directions = ['NORTH', 'WEST', 'SOUTH', 'EAST'];
        const currentIndex = directions.indexOf(this.robot.direction);
        const oldDirection = this.robot.direction;
        this.robot.direction = directions[(currentIndex + 1) % 4];
        console.log(`Giro izquierda: ${oldDirection} → ${this.robot.direction}`);
        this.updateRobotPosition();
    }
    
    /**
     * Gira el robot a la derecha
     */
    turnRight() {
        const directions = ['NORTH', 'EAST', 'SOUTH', 'WEST'];
        const currentIndex = directions.indexOf(this.robot.direction);
        const oldDirection = this.robot.direction;
        this.robot.direction = directions[(currentIndex + 1) % 4];
        console.log(`Giro derecha: ${oldDirection} → ${this.robot.direction}`);
        this.updateRobotPosition();
    }
    
    /**
     * Valida si una posición es válida
     */
    isValidPosition(x, y) {
        // Verificar límites del grid
        if (x < 0 || x >= 5 || y < 0 || y >= 4) {
            return false;
        }
        
        // Verificar si es una celda de camino
        const gridConfig = JSON.parse(this.currentTrack.gridConfig);
        return gridConfig[y] && gridConfig[y][x] === 1;
    }
    
    /**
     * Marca una posición de error
     */
    markErrorPosition(x, y) {
        if (x >= 0 && x < 5 && y >= 0 && y < 4) {
            const cell = document.querySelector(`[data-x="${x}"][data-y="${y}"]`);
            if (cell) {
                cell.classList.add('error');
            }
        }
    }
    
    /**
     * Verifica la condición de victoria
     */
    async checkWinCondition() {
        const gridConfig = JSON.parse(this.currentTrack.gridConfig);
        let totalPathCells = 0;
        
        // Contar total de celdas de camino
        for (let row = 0; row < 4; row++) {
            for (let col = 0; col < 5; col++) {
                if (gridConfig[row] && gridConfig[row][col] === 1) {
                    totalPathCells++;
                }
            }
        }
        
        // Verificar si se visitaron todas las celdas
        if (this.visitedCells.size >= totalPathCells) {
            await this.updateGameSession('SUCCESS');
            this.showSuccessModal();
        }
    }
    
    /**
     * Actualiza la sesión de juego
     */
    async updateGameSession(status) {
        if (!this.gameSession) return;
        
        try {
            const executionTime = this.executionStartTime ? Date.now() - this.executionStartTime : 0;
            
            const response = await fetch(`/api/game/session/${this.gameSession.sessionId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    status: status,
                    movesCount: this.moves.length,
                    movesSequence: JSON.stringify(this.moves),
                    cellsVisited: this.visitedCells.size,
                    executionTimeMs: executionTime,
                    attemptsCount: this.attemptsCount
                })
            });
            
            if (!response.ok) {
                console.error('Error al actualizar sesión de juego');
            }
        } catch (error) {
            console.error('Error al actualizar sesión:', error);
        }
    }
    
    /**
     * Actualiza la sesión de juego con información de error
     */
    async updateGameSessionWithError(errorMessage, errorX, errorY) {
        if (!this.gameSession) return;
        
        try {
            const executionTime = this.executionStartTime ? Date.now() - this.executionStartTime : 0;
            
            const response = await fetch(`/api/game/session/${this.gameSession.sessionId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    status: 'FAILED',
                    movesCount: this.moves.length,
                    movesSequence: JSON.stringify(this.moves),
                    cellsVisited: this.visitedCells.size,
                    executionTimeMs: executionTime,
                    attemptsCount: this.attemptsCount,
                    errorMessage: errorMessage,
                    errorPositionX: errorX,
                    errorPositionY: errorY
                })
            });
            
            if (!response.ok) {
                console.error('Error al actualizar sesión de juego con error');
            }
        } catch (error) {
            console.error('Error al actualizar sesión con error:', error);
        }
    }
    
    /**
     * Destaca el movimiento actual durante la ejecución
     */
    highlightCurrentMove(index) {
        document.querySelectorAll('.move-item').forEach((item, i) => {
            item.classList.toggle('active', i === index);
        });
    }
    
    /**
     * Limpia la lista de movimientos
     */
    clearMoves() {
        if (this.isExecuting) return;
        
        this.moves = [];
        this.isInsideLoop = false;
        
        // Resetear el estado de ejecución anterior para permitir 
        // ejecutar cualquier cantidad de movimientos nuevos desde el inicio
        this.lastExecutionState = {
            x: null,
            y: null,
            direction: null,
            wasSuccessful: false,
            executedMovesCount: 0
        };
        
        // Resetear la posición del robot inmediatamente para que 
        // el usuario vea visualmente que el robot vuelve al inicio
        this.resetRobotPosition();
        
        this.updateMovesDisplay();
        this.updateUI();
    }
    
    /**
     * Reinicia el juego
     */
    resetGame() {
        if (this.isExecuting) return;
        
        this.clearMoves();
        this.resetRobotPosition();
        this.clearErrorCells();
        this.updateUI();
        this.closeModals();
        
        // Limpiar estado de ejecución anterior
        this.lastExecutionState = {
            x: null,
            y: null,
            direction: null,
            wasSuccessful: false,
            executedMovesCount: 0
        };
    }
    
    /**
     * Inicia un nuevo juego con una pista diferente
     */
    async newGame() {
        console.log('🎮 Iniciando nuevo juego...');
        this.closeModals();
        
        // Limpiar la pista específica para forzar carga aleatoria
        if (window.gameConfig) {
            window.gameConfig.track = null;
            console.log('🔄 Configuración de pista limpiada para carga aleatoria');
        }
        
        // Limpiar estado del juego actual
        this.moves = [];
        this.clearMoves();
        this.isExecuting = false;
        this.executionStartTime = null;
        this.visitedCells.clear();
        this.attemptsCount = 0; // Resetear intentos para nueva pista
        
        // Resetear estado de ejecución
        this.lastExecutionState = {
            x: null,
            y: null,
            direction: null,
            wasSuccessful: false,
            executedMovesCount: 0
        };
        
        console.log('🧹 Estado del juego limpiado, inicializando...');
        
        try {
            // Pasar true para excluir la pista actual y obtener una diferente
            await this.initializeGame(true);
            console.log('✅ Nueva pista cargada exitosamente');
        } catch (error) {
            console.error('❌ Error al cargar nueva pista:', error);
            this.showErrorModal('Error al cargar nueva pista. Intenta de nuevo.');
        }
    }
    
    /**
     * Limpia las celdas de error
     */
    clearErrorCells() {
        document.querySelectorAll('.grid-cell.error').forEach(cell => {
            cell.classList.remove('error');
        });
    }
    
    /**
     * Actualiza la UI
     */
    updateUI() {
        document.getElementById('totalMoves').textContent = this.moves.length;
        
        if (this.executionStartTime) {
            const elapsed = Math.floor((Date.now() - this.executionStartTime) / 1000);
            document.getElementById('executionTime').textContent = `${elapsed}s`;
        } else {
            document.getElementById('executionTime').textContent = '0s';
        }
    }
    
    /**
     * Habilita/deshabilita botones
     */
    toggleButtons(enabled) {
        const buttons = ['btnForward', 'btnLeft', 'btnRight', 'btnLoop', 'btnExecute', 'btnReset', 'btnClearMoves'];
        buttons.forEach(id => {
            const button = document.getElementById(id);
            if (button) {
                button.disabled = !enabled;
            }
        });
    }
    
    /**
     * Muestra el modal de éxito
     */
    showSuccessModal() {
        document.getElementById('successModal').style.display = 'flex';
    }
    
    /**
     * Muestra el modal de error
     */
    showErrorModal(message) {
        document.getElementById('errorMessage').textContent = message;
        document.getElementById('errorModal').style.display = 'flex';
    }
    
    /**
     * Muestra el modal de carga
     */
    showLoadingModal() {
        document.getElementById('loadingModal').style.display = 'flex';
    }
    
    /**
     * Cierra todos los modales
     */
    closeModals() {
        document.querySelectorAll('.game-modal').forEach(modal => {
            modal.style.display = 'none';
        });
    }
    
    /**
     * Función de utilidad para pausas
     */
    delay(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }
}

// Hacer GameManager disponible globalmente
window.GameManager = GameManager;