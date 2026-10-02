package com.example.snakegame

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.math.abs
import kotlin.math.min
import kotlin.random.Random

class GameView(context: Context, attrs: AttributeSet?) :
    SurfaceView(context, attrs),
    Runnable {

    //================================================================
    // РАЗМЕРЫ ИГРОВОГО ПОЛЯ
    //================================================================
    private val GRID_SIZE = 20
    private var cellSize = 0f
    private var offsetX = 0f
    private var offsetY = 0f

    private var thread: Thread? = null
    @Volatile private var isRunning = false

    //================================================================
    // ИГРОВЫЕ ОБЪЕКТЫ
    //================================================================
    private val snake = mutableListOf<Pair<Int, Int>>()
    private var foodX = 0
    private var foodY = 0
    private var direction = Direction.RIGHT
    private var nextDirection = Direction.RIGHT

    //================================================================
    // ПАРАМЕТРЫ ИГРЫ
    //================================================================
    private var score = 0
    private var gameOver = false
    private var gameWin = false
    private var moveCounter = 0
    private val MOVE_DELAY = 10

    //================================================================
    // КИСТИ ДЛЯ РИСОВАНИЯ
    //================================================================
    private val fieldPaint = Paint().apply {
        color = Color.parseColor("#1B263B")
        style = Paint.Style.FILL
    }

    private val borderPaint = Paint().apply {
        color = Color.parseColor("#00E5FF")
        style = Paint.Style.STROKE
        strokeWidth = 6f
        isAntiAlias = true
    }

    private val gridPaint = Paint().apply {
        color = Color.parseColor("#415A77")
        style = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
    }

    private val snakePaint = Paint().apply {
        color = Color.parseColor("#00C853")
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val headPaint = Paint().apply {
        color = Color.parseColor("#76FF03")
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val foodPaint = Paint().apply {
        color = Color.parseColor("#FF1744")
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 60f
        isAntiAlias = true
        typeface = Typeface.DEFAULT_BOLD
        setShadowLayer(8f, 4f, 4f, Color.BLACK)
    }

    private val gameOverPaint = Paint().apply {
        color = Color.parseColor("#FF1744")
        textSize = 80f
        isAntiAlias = true
        typeface = Typeface.DEFAULT_BOLD
        setShadowLayer(10f, 5f, 5f, Color.BLACK)
    }

    //================================================================
    // ИНИЦИАЛИЗАЦИЯ
    //================================================================
    init {
        post {
            val topMargin = 200f
            val bottomMargin = 100f

            val availableHeight = height - topMargin - bottomMargin
            val availableWidth = width.toFloat()

            val fieldSize = min(availableWidth, availableHeight)
            cellSize = fieldSize / GRID_SIZE

            offsetX = (width - fieldSize) / 2f
            offsetY = topMargin

            val centerX = GRID_SIZE / 2
            val centerY = GRID_SIZE / 2
            snake.add(centerX to centerY)
            snake.add((centerX - 1) to centerY)
            snake.add((centerX - 2) to centerY)

            spawnFood()
        }

        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                startGame()
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                stopGame()
            }
        })
    }

    private fun startGame() {
        if (thread == null) {
            isRunning = true
            thread = Thread(this)
            thread?.start()
        }
    }

    private fun stopGame() {
        isRunning = false
        try {
            thread?.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        thread = null
    }

    //================================================================
    // ИГРОВОЙ ЦИКЛ
    //================================================================
    override fun run() {
        while (isRunning) {
            update()
            draw()
            try {
                Thread.sleep(16)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    private fun update() {
        if (gameOver || gameWin) return

        moveCounter++
        if (moveCounter < MOVE_DELAY) return
        moveCounter = 0

        direction = nextDirection

        val head = snake.first()
        val newHead = when (direction) {
            Direction.UP -> head.first to head.second - 1
            Direction.DOWN -> head.first to head.second + 1
            Direction.LEFT -> head.first - 1 to head.second
            Direction.RIGHT -> head.first + 1 to head.second
        }

        if (newHead.first < 0 || newHead.first >= GRID_SIZE ||
            newHead.second < 0 || newHead.second >= GRID_SIZE
        ) {
            gameOver = true
            return
        }

        if (snake.contains(newHead)) {
            gameOver = true
            return
        }

        snake.add(0, newHead)
        if (newHead.first == foodX && newHead.second == foodY) {
            score++
            spawnFood()
        } else {
            snake.removeAt(snake.size - 1)
        }
    }

    private fun spawnFood() {
        do {
            foodX = Random.nextInt(GRID_SIZE)
            foodY = Random.nextInt(GRID_SIZE)
        } while (snake.contains(foodX to foodY))
    }

    //================================================================
    // РИСОВАНИЕ
    //================================================================
    private fun draw() {
        val canvas = holder.lockCanvas() ?: return
        try {
            canvas.drawColor(Color.parseColor("#0D1B2A"))

            val fieldRight = offsetX + GRID_SIZE * cellSize
            val fieldBottom = offsetY + GRID_SIZE * cellSize
            canvas.drawRect(offsetX, offsetY, fieldRight, fieldBottom, fieldPaint)
            canvas.drawRect(offsetX, offsetY, fieldRight, fieldBottom, borderPaint)

            // Сетка
            for (i in 0..GRID_SIZE) {
                canvas.drawLine(
                    offsetX + i * cellSize, offsetY,
                    offsetX + i * cellSize, fieldBottom,
                    gridPaint
                )
                canvas.drawLine(
                    offsetX, offsetY + i * cellSize,
                    fieldRight, offsetY + i * cellSize,
                    gridPaint
                )
            }

            // Еда
            canvas.drawCircle(
                offsetX + foodX * cellSize + cellSize / 2,
                offsetY + foodY * cellSize + cellSize / 2,
                cellSize / 2 - 5f,
                foodPaint
            )

            // Змейка
            for ((index, segment) in snake.withIndex()) {
                val left = offsetX + segment.first * cellSize
                val top = offsetY + segment.second * cellSize
                val right = left + cellSize
                val bottom = top + cellSize

                if (index == 0) {
                    canvas.drawRoundRect(left + 2f, top + 2f, right - 2f, bottom - 2f, 10f, 10f, headPaint)
                } else {
                    canvas.drawRoundRect(left + 4f, top + 4f, right - 4f, bottom - 4f, 8f, 8f, snakePaint)
                }
            }

            // Счёт
            canvas.drawText("Счёт: $score", offsetX + 20f, offsetY - 40f, textPaint)

            // Game Over
            if (gameOver) {
                val centerX = offsetX + (GRID_SIZE * cellSize) / 2
                val centerY = offsetY + (GRID_SIZE * cellSize) / 2

                val overlayPaint = Paint().apply {
                    color = Color.parseColor("#CC000000")
                    style = Paint.Style.FILL
                }
                canvas.drawRect(centerX - 320f, centerY - 100f, centerX + 320f, centerY + 60f, overlayPaint)

                val overlayBorder = Paint().apply {
                    color = Color.parseColor("#FF1744")
                    style = Paint.Style.STROKE
                    strokeWidth = 4f
                }
                canvas.drawRect(centerX - 320f, centerY - 100f, centerX + 320f, centerY + 60f, overlayBorder)

                canvas.drawText("GAME OVER", centerX - 220f, centerY, gameOverPaint)
            }
        } finally {
            holder.unlockCanvasAndPost(canvas)
        }
    }

    //================================================================
    // ОБРАБОТКА КАСАНИЙ
    //================================================================
    private var touchStartX = 0f
    private var touchStartY = 0f

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStartX = event.x
                touchStartY = event.y
            }

            MotionEvent.ACTION_UP -> {
                val dx = event.x - touchStartX
                val dy = event.y - touchStartY

                if (abs(dx) > abs(dy)) {
                    if (dx > 0) {
                        if (direction != Direction.LEFT) nextDirection = Direction.RIGHT
                    } else {
                        if (direction != Direction.RIGHT) nextDirection = Direction.LEFT
                    }
                } else {
                    if (dy > 0) {
                        if (direction != Direction.UP) nextDirection = Direction.DOWN
                    } else {
                        if (direction != Direction.DOWN) nextDirection = Direction.UP
                    }
                }
            }
        }
        return true
    }
}