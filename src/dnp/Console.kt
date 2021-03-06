package dnp

import java.awt.*
import java.awt.datatransfer.DataFlavor
import java.awt.event.ActionEvent
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.image.BufferedImage
import java.awt.image.ImageObserver
import java.awt.print.Printable.PAGE_EXISTS
import java.awt.print.PrinterJob
import java.util.*
import java.util.Timer
import java.util.concurrent.ArrayBlockingQueue
import javax.swing.*
import kotlin.concurrent.timer

class Console {
    inner class Canvas : JPanel() {
        val image by lazy { BufferedImage(this@Console.width, this@Console.height, BufferedImage.TYPE_INT_ARGB) }
        val graphics: Graphics2D by lazy { image.createGraphics() }
        var buffered = false
        lateinit var main: ThreadGroup
        var show_cursor = false
        var cursor_visible = true

        init {
            for (thread in Thread.getAllStackTraces().keys) {
                if (thread.threadGroup.name == "main") {
                    main = thread.threadGroup
                    break
                }
            }
            // main = threads.filter { it.name.toLowerCase() == "main" }[0] // better for program, one line/easier, less correct
            // [0] can be replaced with .forEach { main = it }
            drawing_thread = timer(period = 1) {
                if (buffered) repaint()
            }
            main_check_thread = timer(period = 100) {
                /*var threads = main.activeCount() + 5
                val t = Array(threads) { Thread() }
                threads = main.enumerate(t)
                if (t.none { it.name == "main" || it.name.startsWith("Thread-") }) {
                    setState(State.FINISHED)
                    drawing_thread.cancel()
                    cursor_thread.cancel()
                    this.cancel()
                } else {
                    System.out.println(Arrays.toString(t))
                }*/
            }
            cursor_thread = timer(period = 300) {
                cursor_visible = !cursor_visible
                if (show_cursor) {
                    val pos = Point(current_col * font_width, current_row * font_height)
                    graphics_color = if (cursor_visible) Color.BLACK else Color.WHITE
                    fillRect(pos.x, pos.y, font_width, font_height)
                }
            }
        }

        @Synchronized
        fun setCursorPos(row: Int, col: Int) {
            synchronized(this) {
                if (cursor_visible) eraseChar()
                current_row = row
                current_col = col
            }
        }

        @Synchronized private fun draw(f: () -> Unit) {
            synchronized(this) {
                graphics.color = graphics_color
                f()
                buffered = true
            }
        }

        fun clearRect(x: Int, y: Int, width: Int, height: Int) = draw { graphics.clearRect(x, y, width, height) }
        fun copyArea(x: Int, y: Int, width: Int, height: Int, delta_x: Int, delta_y: Int) = draw { graphics.copyArea(x, y, width, height, delta_x, delta_y) }

        fun drawString(s: String, x: Int, y: Int) = draw { graphics.drawString(s, x, y) }
        fun drawRect(x: Int, y: Int, width: Int, height: Int) = draw { graphics.drawRect(x, y, width, height) }
        fun fillRect(x: Int, y: Int, width: Int, height: Int) = draw { graphics.fillRect(x, y, width, height) }
        fun drawOval(x: Int, y: Int, width: Int, height: Int) = draw { graphics.drawOval(x, y, width, height) }
        fun drawOval(pos: Point, v_radius: Int, h_radius: Int) = draw { graphics.drawOval(pos.x - h_radius, pos.y - v_radius, h_radius * 2, v_radius * 2) }
        fun fillOval(x: Int, y: Int, width: Int, height: Int) = draw { graphics.fillOval(x, y, width, height) }
        fun fillOval(pos: Point, v_radius: Int, h_radius: Int) = draw { graphics.fillOval(pos.x - h_radius, pos.y - v_radius, h_radius * 2, v_radius * 2) }
        fun draw3DRect(x: Int, y: Int, width: Int, height: Int, raised: Boolean) = draw { graphics.draw3DRect(x, y, width, height, raised) }
        fun drawArc(x: Int, y: Int, width: Int, height: Int, start_angle: Int, arc_angle: Int) = draw { graphics.drawArc(x, y, width, height, start_angle, arc_angle) }
        fun drawArc(pos: Point, v_radius: Int, h_radius: Int, start_angle: Int, arc_angle: Int) = draw { graphics.drawArc(pos.x - h_radius, pos.y - v_radius, h_radius * 2, v_radius * 2, start_angle, arc_angle) }
        fun fillArc(x: Int, y: Int, width: Int, height: Int, start_angle: Int, arc_angle: Int) = draw { graphics.fillArc(x, y, width, height, start_angle, arc_angle) }
        fun fillArc(pos: Point, v_radius: Int, h_radius: Int, start_angle: Int, arc_angle: Int) = draw { graphics.fillArc(pos.x - h_radius, pos.y - v_radius, h_radius * 2, v_radius * 2, start_angle, arc_angle) }
        fun drawImage(image: Image, x: Int, y: Int, observer: ImageObserver?) = draw { graphics.drawImage(image, x, y, observer) }
        fun drawLine(x1: Int, y1: Int, x2: Int, y2: Int) = draw { graphics.drawLine(x1, y1, x2, y2) }
        fun drawRoundRect(x: Int, y: Int, width: Int, height: Int, arc_width: Int, arc_height: Int) = draw { graphics.drawRoundRect(x, y, width, height, arc_width, arc_height) }
        fun fillRoundRect(x: Int, y: Int, width: Int, height: Int, arc_width: Int, arc_height: Int) = draw { graphics.fillRoundRect(x, y, width, height, arc_width, arc_height) }

        fun drawMapleLeaf(x: Int, y: Int, width: Int, height: Int) = draw {
            val xscale = width / 152
            val yscale = height / 140
            val middle = x + width / 2
            val points = Array(26) { Point() }
            points[0] = Point(middle - xscale * 5, y + height)
            points[1] = Point()
        }

        private val ratio = (1 + Math.sqrt(5.0)) / 2

        fun drawStar(x: Int, y: Int, width: Int, height: Int, fill: Boolean = false) = draw {
            val points = Array(10) { Point() }
            val width_ratio_1_d = width / (2 * ratio + 1)
            val width_ratio_golden = (width_ratio_1_d * ratio)/*.toInt()*/
            val height_ratio_1_d = height / (ratio + 1)
            val height_ratio_golden = (height_ratio_1_d * ratio).toInt()
            val width_ratio_1 = width_ratio_1_d.toInt()
            val height_ratio_1 = height_ratio_1_d.toInt()
            points[0] = Point(x + width / 2, y)
            points[1] = Point(x + width_ratio_golden.toInt(), y + height_ratio_1)
            points[2] = Point(x, y + height_ratio_1)
            //points[3] = Point(x + width / 2 - width_ratio_1 / 2 - width_ratio_1 * width_ratio_1 / (2 * width_ratio_golden.toInt()), y + height_ratio_1 + (width_ratio_1 * (Math.sqrt(4 * width_ratio_golden * width_ratio_golden - width_ratio_1 * width_ratio_1)).toInt()) / (2 * width_ratio_golden.toInt()))
            points[3] = Point(x + width / 2 - width_ratio_1 / 2 - width_ratio_1 * width_ratio_1 / (2 * width_ratio_golden.toInt()), (y + height - height * .381759).toInt())
            points[4] = Point(x + width_ratio_golden.toInt() / 2, y + height)
            points[5] = Point(x + width / 2, (y + height - height * .236068).toInt())
            //points[5] = Point(x + width / 2, y + height - (width_ratio_golden / 2 * Math.tan(Math.toRadians(36.0))).toInt())
            for (i in 6..9) {
                points[i].y = points[10 - i].y
                points[i].x = x + width - (points[10 - i].x - x)
            }
            if (fill) fillPolygon(points)
            else drawPolygon(points)
        }

        fun drawPolygon(x_points: IntArray, y_points: IntArray, points: Int) = draw { graphics.drawPolygon(x_points, y_points, points) }
        fun fillPolygon(x_points: IntArray, y_points: IntArray, points: Int) = draw { graphics.fillPolygon(x_points, y_points, points) }
        fun drawPolygon(points: Array<Point>) = draw {
            val x = IntArray(points.size)
            val y = IntArray(points.size)
            for (i in points.indices) {
                x[i] = points[i].x
                y[i] = points[i].y
            }
            drawPolygon(x, y, points.size)
        }

        fun fillPolygon(points: Array<Point>) = draw {
            val x = IntArray(points.size)
            val y = IntArray(points.size)
            for (i in points.indices) {
                x[i] = points[i].x
                y[i] = points[i].y
            }
            fillPolygon(x, y, points.size)
        }

        fun clearScreen(color: Color = background_color) = draw {
            graphics.color = color
            graphics.fillRect(0, 0, this@Console.width, this@Console.height)
        }

        fun eraseLine(line: Int = current_row) {
            val y = line * font_height
            graphics_color = background_color
            fillRect(0, y, this@Console.width, font_height)
        }

        override fun paintComponent(g: Graphics) {
            g.drawImage(image, 0, 0, null)
            buffered = false
        }

        fun drawText(row: Int = current_row, col: Int = current_col, text: String) {
            val pos = Point(col * font_width, row * font_height)
            graphics.color = background_color
            graphics.fillRect(pos.x, pos.y, text.length * font_width, font_height)
            graphics_color = text_color
            graphics.font = this@Console.font
            drawString(text, pos.x, pos.y + font_height - font_base)
        }

        fun eraseChar(row: Int = current_row, col: Int = current_col) {
            val pos = Point(col * font_width, row * font_height)
            graphics_color = background_color
            fillRect(pos.x, pos.y, font_width, font_height)
        }
    }

    lateinit var cursor_thread: Timer
    lateinit var drawing_thread: Timer
    lateinit var main_check_thread: Timer

    val graphics_canvas = Canvas()

    // The keyboard buffer
    val keyboard_buffer = ArrayBlockingQueue<Char>(4096)
    // The line buffer
    val line_buffer = LinkedList<Char>()
    // The JFrame and its components
    val frame: JFrame
    // Font variables
    var font = Font("monospaced", Font.PLAIN, DEFAULT_FONT_SIZE)
        @JvmName("placeholder") private set
    val font_height: Int
    val font_width: Int
    val font_base: Int
    // The base window title
    private val window_title: String
    // The height and width of the JFrame
    val height: Int
    val width: Int

    val cols: Int
    val rows: Int

    var current_col = 0
    var current_row = 0

    var text_color: Color = Color.BLACK
        @JvmName("setTextColor") set
        @JvmName("setTextColor") get
    var background_color: Color = Color.WHITE
        @JvmName("setTextBackgroundColor") set
        @JvmName("getTextBackgroundColor") get
    var graphics_color: Color = Color.BLACK
        @JvmName("setColor") set
        @JvmName("getColor") get

    fun setState(state: State) {
        frame.title = window_title + " - " + state.string
    }

    // static variables basically
    companion object {
        @JvmField var consoles = 0
        const val DEFAULT_ROWS = 25
        const val DEFAULT_COLS = 80
        const val DEFAULT_FONT_SIZE = 14
        const val DEFAULT_TITLE = "Console"
        @JvmStatic val whitespace = arrayOf('\n', '\t', ' ')
    }

    // TODO: this prints
    fun print(text: String?) {
        val text = text ?: "<null>"
        if (text == "\n") {
            if (current_row == rows - 1) {
                graphics_canvas.copyArea(0, 0, width, height, 0, -font_height)
                graphics_canvas.eraseLine()
                current_col = 0
            } else setCursor(current_row + 1, 0)
            return
        }
        if (text == "\t") {
            while (++current_col % 4 != 0);
            return
        }
        for (c in text) {
            if (c != '\n' && c != '\t') {
                if (current_col == cols) {
                    print("\n")
                }
                graphics_canvas.drawText(text = c.toString())
                current_col++
            } else print(c)
        }
        System.out.println("printed: $text")
        //System.out.println("printed: " + text)
    }

    fun print(text: Any, padding: Int) {
        when (text) {
            is Byte, Short, Int, Long -> print(String.format("%${padding}d", text))
            is Float, Double -> print(String.format("%-${padding}f", text))
            is String -> print(String.format("%-${padding}s", text))
            is Char -> print(String.format("%-${padding}c", text))
            else -> print(String.format("%-${padding}s", text.toString()))
        }
    }

    fun println(text: Any, padding: Int) {
        print(text, padding)
        println()
    }

    fun println(f: Float, padding: Int, places: Int) {
        print(f, padding, places)
        println()
    }

    fun println(d: Double, padding: Int, places: Int) {
        print(d, padding, places)
        println()
    }

    fun print(f: Float, padding: Int, places: Int) {
        print(String.format("%$padding.${places}f", f))
    }

    fun print(d: Double, padding: Int, places: Int) {
        print(String.format("%$padding.${places}f", d))
    }

    fun close() {
        cursor_thread.cancel()
        drawing_thread.cancel()
        main_check_thread.cancel()
        frame.dispose()
    }

    @JvmOverloads constructor(font_size: Int = DEFAULT_FONT_SIZE) : this(DEFAULT_ROWS, DEFAULT_COLS, font_size)

    @JvmOverloads constructor(rows: Int = DEFAULT_ROWS, columns: Int = DEFAULT_COLS, title: String) : this(rows, columns, DEFAULT_FONT_SIZE, title = title)

    @JvmOverloads constructor(rows: Int, columns: Int, font_size: Int, title: String = DEFAULT_TITLE) {
        consoles++
        window_title = if (title == DEFAULT_TITLE && consoles > 1) {
            title + " " + consoles
        } else title

        this.cols = columns
        this.rows = rows
        // Calculate the height and with of the font
        font = font.deriveFont(font_size.toFloat())
        val font_metrics = graphics_canvas.getFontMetrics(font)
        font_height = font_metrics.height + font_metrics.leading
        font_base = font_metrics.descent
        val chars = 32..127
        val char_widths = chars.map { font_metrics.charWidth(it) }
        font_width = char_widths.max() ?: 0
        // Calculate the height and width of the screen
        width = font_width * columns
        height = font_height * rows
        // Construct a JFrame
        frame = JFrame()
        frame.addKeyListener(object : KeyAdapter() {
            override fun keyTyped(e: KeyEvent?) {
                if (e?.keyChar == null) return
                val c = e.keyChar
                if (font.canDisplay(c) || c == '\b') keyboard_buffer.add(c)
            }
        })
        frame.contentPane.preferredSize = Dimension(width, height)
        frame.add(graphics_canvas)
        frame.isResizable = false
        frame.pack()
        setState(State.RUNNING)
        frame.setLocationRelativeTo(null)
        frame.background = Color.WHITE
        frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        frame.isVisible = true
        frame.requestFocus()
    }

    fun clear() {
        graphics_canvas.clearScreen()
        setCursor(0, 0)
    }

    fun clearRect(x: Int, y: Int, width: Int, height: Int) = graphics_canvas.clearRect(x, y, width, height)
    fun copyArea(x: Int, y: Int, width: Int, height: Int, delta_x: Int, delta_y: Int) = graphics_canvas.copyArea(x, y, width, height, delta_x, delta_y)
    fun draw3DRect(x: Int, y: Int, width: Int, height: Int, raised: Boolean) = graphics_canvas.draw3DRect(x, y, width, height, raised)
    fun drawArc(x: Int, y: Int, width: Int, height: Int, start_angle: Int, arc_angle: Int) = graphics_canvas.drawArc(x, y, width, height, start_angle, arc_angle)
    fun drawArc(pos: Point, v_radius: Int, h_radius: Int, start_angle: Int, arc_angle: Int) = graphics_canvas.drawArc(pos, v_radius, h_radius, start_angle, arc_angle)
    fun fillArc(x: Int, y: Int, width: Int, height: Int, start_angle: Int, arc_angle: Int) = graphics_canvas.fillArc(x, y, width, height, start_angle, arc_angle)
    fun fillArc(pos: Point, v_radius: Int, h_radius: Int, start_angle: Int, arc_angle: Int) = graphics_canvas.fillArc(pos, v_radius, h_radius, start_angle, arc_angle)
    @JvmOverloads
    fun drawImage(image: Image, x: Int, y: Int, observer: ImageObserver? = null) = graphics_canvas.drawImage(image, x, y, observer)

    fun drawLine(x1: Int, y1: Int, x2: Int, y2: Int) = graphics_canvas.drawLine(x1, y1, x2, y2)
    fun drawMapleLeaf(x: Int, y: Int, width: Int, height: Int) = graphics_canvas.drawMapleLeaf(x, y, width, height)
    fun drawOval(x: Int, y: Int, width: Int, height: Int) = graphics_canvas.drawOval(x, y, width, height)
    fun drawOval(pos: Point, v_radius: Int, h_radius: Int) = graphics_canvas.drawOval(pos, v_radius, h_radius)
    fun fillOval(x: Int, y: Int, width: Int, height: Int) = graphics_canvas.fillOval(x, y, width, height)
    fun fillOval(pos: Point, v_radius: Int, h_radius: Int) = graphics_canvas.fillOval(pos, v_radius, h_radius)

    fun drawPolygon(x_points: IntArray, y_points: IntArray, points: Int) = graphics_canvas.drawPolygon(x_points, y_points, points)
    fun drawPolygon(points: Array<Point>) = graphics_canvas.drawPolygon(points)
    fun fillPolygon(x_points: IntArray, y_points: IntArray, points: Int) = graphics_canvas.fillPolygon(x_points, y_points, points)
    fun fillPolygon(points: Array<Point>) = graphics_canvas.fillPolygon(points)
    fun drawRect(x: Int, y: Int, width: Int, height: Int) = graphics_canvas.drawRect(x, y, width, height)
    fun drawRoundRect(x: Int, y: Int, width: Int, height: Int, arc_width: Int, arc_height: Int) = graphics_canvas.drawRoundRect(x, y, width, height, arc_width, arc_height)
    fun fillRoundRect(x: Int, y: Int, width: Int, height: Int, arc_width: Int, arc_height: Int) = graphics_canvas.fillRoundRect(x, y, width, height, arc_width, arc_height)
    fun drawStar(x: Int, y: Int, width: Int, height: Int) = graphics_canvas.drawStar(x, y, width, height)
    fun fillStar(x: Int, y: Int, width: Int, height: Int) = graphics_canvas.drawStar(x, y, width, height, fill = true)
    fun drawString(s: String, x: Int, y: Int) = graphics_canvas.drawString(s, x, y)

    fun fillRect(x: Int, y: Int, width: Int, height: Int) = graphics_canvas.fillRect(x, y, width, height)

    init {
        // Get the input map for key bindings
        val input_map = graphics_canvas.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        // Set CTRL+Q to quit, and CTRL+V to paste
        input_map.put(KeyStroke.getKeyStroke("control Q"), "quit")
        input_map.put(KeyStroke.getKeyStroke("control V"), "paste")
        input_map.put(KeyStroke.getKeyStroke("control P"), "print")
        // Quit is just System.exit(0) but since AbstractAction is abstract, you can't use a lambda
        graphics_canvas.actionMap.put("quit", object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent?) {
                System.exit(0)
            }
        })
        // Paste by getting the clipboard contents, then checking which ones you can print, and add them to the keyboard buffer
        graphics_canvas.actionMap.put("paste", object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent?) {
                val clipboard = Toolkit.getDefaultToolkit().systemClipboard.getData(DataFlavor.stringFlavor) as String
                for (ch: Char in clipboard) {
                    if (font.canDisplay(ch)) keyboard_buffer.add(ch)
                }
            }
        })
        // Prints a screenshot of the program
        graphics_canvas.actionMap.put("print", object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent?) {
                val job = PrinterJob.getPrinterJob()
                job.setPrintable { graphics, pageFormat, _ ->
                    (graphics as Graphics2D).translate(pageFormat?.imageableX ?: .0, pageFormat?.imageableY ?: .0)
                    graphics.drawImage(graphics_canvas.image, 0, 0, null)
                    PAGE_EXISTS
                }
                if (job.printDialog()) {
                    job.print()
                }
            }
        })
    }

    // The extension at the top beside the console name
    enum class State(val string: String) {
        RUNNING("Running"),
        WAITING("Waiting for Input"),
        FINISHED("Finished Execution")
    }

    fun print(value: Any?) {
        print(value?.toString())
    }

    @JvmOverloads
    fun println(value: Any? = "") {
        print(value)
        print("\n")
    }

    fun setCursor(row: Int, column: Int) {
        graphics_canvas.setCursorPos(row, column)
    }

    fun readString(): String {
        val string = StringBuilder()
        var ch: Char
        // skip whitespace
        do {
            ch = readChar()
        } while (ch in whitespace) // Character.isWhitespace(ch)
        // read until whitespace and append to string
        while (ch !in whitespace) {
            string.append(ch)
            ch = readChar()
        }
        return string.toString()
    }

    fun readShort() = readString().toShort()
    fun readInt() = readString().toInt()
    fun readLong() = readString().toLong()
    fun readFloat() = readString().toFloat()
    fun readDouble() = readString().toDouble()
    fun readByte() = readString().toByte()
    fun readBoolean() = readString().toBoolean()

    fun readLine(): String {
        val string = StringBuilder()
        var ch: Char
        while (true) {
            ch = readChar()
            if (ch == '\n') break
            string.append(ch)
        }
        return string.toString()
    }

    fun erasePreviousChar() {
        var col = current_col
        var row = current_row
        if (col > 0) col--
        else if (row > 0) {
            row--
            col = 0
        }
        setCursor(row, col)
        graphics_canvas.eraseChar()
    }

    fun readChar(): Char {
        graphics_canvas.show_cursor = true
        if (line_buffer.isNotEmpty()) {
            return line_buffer.poll()
        }
        while (true) {
            val ch = getChar()
            if (ch == '\n') {
                print("\n")
                line_buffer.add('\n')
                break
            } else if (ch == '\b') {
                if (ch != '\t') {
                    erasePreviousChar()
                    line_buffer.removeLast()
                } else {
                    while (--current_col % 4 != 0) erasePreviousChar()
                }
            } else {
                print(ch)
                line_buffer.add(ch)
            }
        }
        graphics_canvas.show_cursor = false
        return line_buffer.poll()
    }

    fun getChar(): Char {
        setState(State.WAITING)
        val k = keyboard_buffer.take()
        setState(State.RUNNING)
        return k
    }

    fun maxx() = width - 1
    fun maxy() = height - 1
    fun getMaxRows() = rows
    fun getMaxCols() = cols
    fun getRow() = current_row
    fun getCol() = current_col
    fun maxrow() = rows
    fun maxcol() = cols
    fun setFont(f: Font) {
        graphics_canvas.graphics.font = f
    }

    fun setXORMode(c: Color) = graphics_canvas.graphics.setXORMode(c)
    fun setPaintMode() = graphics_canvas.graphics.setPaintMode()
}