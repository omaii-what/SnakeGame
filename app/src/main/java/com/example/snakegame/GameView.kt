package com.example.snakegame

import kotlin.random.Random
import android.content.Context
import android.graphics.
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.random.Random

class GameView(context: Context, attrs: AttributeSet?):
        SurfaceView(context, attrs),
        Runnable{

    //================================================================
    // РАЗМЕРЫ ИГРОВОГО ПОЛЯ
    //================================================================
    private val GRID_SIZE = 20
    private val cellSize = 0f
    private val offsetX = 0f
    private val offsetY = 0f

    private var thread: Thread? = null
    private var isRunning = false

    init{
        post{
            val topMargin = 200f
            val bottomMargin = 100f

            val availableHeight = height - topMargin - bottomMargin
            val availableWidth = width.toFloat()

            val fieldSize = Math.min(availableWidth, availableHeight)
            cellSize = fieldSize / GRID_SIZE

            offsetX = (width - fieldSize) / 2f
            offsetY = topMargin
        }

        holder.addCallback(object: SurfaceHolder.Callback){
            override fun surfaceCreate(holder: SurfaceHolder){
                startGame()
            }
            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int){}

            override fun surfaceDestroyed(holder: SurfaceHolder){
                stopGame()
            }
        })
    }

    //================================================================
    // ИГРОВЫЕ ОБЪЕКТЫ
    //================================================================
    private val snake = mutableListOf<Pair<Int, Int>>()

    private var foodX = 0
    private var foodY = 0

    private var direction = Direction.RIGHT
    private var nextDirection = Direction.RIGHT


    val centerX = GRID_SIZE / 2
    val centerY = GRID_SIZE / 2
    snake.add(Pair(centerX, centerY))
    snake.add(Pair(centerX - 1, centerY))
    snake.add(Pair(centerX - 2, centerY))

    spawnFood()
    private fun startGame(){
        if (thread == null){
            isRunning = true
            thread = Thread(this)
            thread?.start()
        }
    }

    override fun stopGame(){
        isRunning = false
        thread?.join()
        thread = null
    }

    override fun run(){
        while(isRunning){
            draw()
            try{
                Thread.sleep(16)
            } catch (e: InterruptedException){
                e.printStackTrace()
            }
        }
    }

    private fun draw(){
        val holder = holder ?: return
        val canvas = holder.lockCanvas() ?: return

        canvas.drawColor(Color.parseColor("#0D1B2A"))

        val fieldRight = offsetX + GRID_SIZE * cellSize
        val fieldBottom = offsetY + GRID_SIZE * cellSize
        canvas.drawRect(offsetX, offsetY, fieldRight, fieldBottom, fieldPaint)

        canvas.drawRect(offsetX, offsetY, fieldRight, fieldBottom, borderPaint)

        holder.unlockCanvasAndPost(canvas)

        for (i in 0..GRID_SIZE){
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

        canvas.drawCircle(
            offsetX + foodX * cellSize + cellSize / 2,
            offsetY + foodY * cellSize + cellSize / 2,
            cellSize / 2 - 5f,
            foodPaint
            )
    }

    //================================================================
    // КИСТИ ДЛЯ РИСОВАНИЯ
    //================================================================
    private val fieldPaint = Paint().apply{
        color = Color.parseColor("#1B263B")
        style = Paint.Style.FILL
    }

    private val borderPaint = Paint().apply{
        color = Color.parseColor("#00E5FF")
        style = Paint.Style.STROKE
        strokeWidth = 6f
        isAntiAlias = true
    }

    private val gridPaint = Paint().apply{
        color = Color.parseColor("#415A77")
        style = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias - true
    }

    private fun SpawnFood(){
        do {
            foodX = Random.nextInt(GRID_SIZE)
            foodY = Random.nextInt(GRID_SIZE)
        } while (snake.contains(Pair(foodX, foodY)))
    }

    private val snakePaint = Paint().apply{
        color = Color.parseColor("#00C853")
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val headPaint = Paint().apply{
        color = Color.parseColor("#76FF03")
        style = Paint.Style.FILL
        isAntiAlias = true
}
    private val foodPaint = Paint().apply{
        color = Color.parseColor("#FF1744")
        style = Paint.Style.FILL
        isAntiAlias = true
}