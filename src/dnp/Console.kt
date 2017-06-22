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
import java.util.concurrent.ArrayBlockingQueue
import javax.swing.*
import kotlin.concurrent.timer

class Console {
    inner class Canvas : JPanel() {
        val image by lazy { BufferedImage(this@Console.width, this@Console.height, BufferedImage.TYPE_INT_ARGB) }
        val graphics: Graphics2D by lazy { image.createGraphics() }
        var buffered = false
        lateinit var main: Thread
        var show_cursor = false
        var cursor_visible = true
        var finished = false

        init {
            val threads = Thread.getAllStackTraces().keys
            for (thread: Thread in threads) {
                if (thread.name.toLowerCase() == "main") {
                    main = thread
                    break
                }
            }
            // main = threads.filter { it.name.toLowerCase() == "main" }[0] // better for program, one line/easier, less correct
            // [0] can be replaced with .forEach { main = it }
            timer(period = 1) {
                if (buffered) repaint()
            }
            timer(period = 100) {
                if (!main.isAlive) {
                    setState(State.FINISHED)
                    this.cancel()
                }
            }
            timer(period = 300) {
                cursor_visible = !cursor_visible
                if (show_cursor && cursor_visible) {
                    val pos = Point(current_col * font_width, current_row * font_height)
                    graphics_color = Color.BLACK
                    // graphics_color = if (cursor_visible) Color.BLACK else Color.WHITE
                    fillRect(pos.x, pos.y, font_width, font_height)
                }
            }
        }

        @Synchronized fun setCursorPos(row: Int, col: Int) {
            if (cursor_visible) eraseChar()
            current_row = row
            current_col = col
        }

        @Synchronized fun draw(f: () -> Unit) {
            graphics.color = graphics_color
            f()
            buffered = true
        }

        fun drawString(s: String, x: Int, y: Int) = draw { graphics.drawString(s, x, y) }
        fun drawRect(x: Int, y: Int, width: Int, height: Int) = draw { graphics.drawRect(x, y, width, height) }
        fun fillRect(x: Int, y: Int, width: Int, height: Int) = draw { graphics.fillRect(x, y, width, height) }
        fun drawOval(x: Int, y: Int, width: Int, height: Int) = draw { graphics.drawOval(x, y, width, height) }
        fun drawOval(pos: Point, v_radius: Int, h_radius: Int) = draw { graphics.drawOval(pos.x - h_radius, pos.y - v_radius, h_radius * 2, v_radius * 2) }
        fun clearRect(x: Int, y: Int, width: Int, height: Int) = draw { graphics.clearRect(x, y, width, height) }
        fun copyArea(x: Int, y: Int, width: Int, height: Int, delta_x: Int, delta_y: Int) = draw { graphics.copyArea(x, y, width, height, delta_x, delta_y) }
        fun draw3DRect(x: Int, y: Int, width: Int, height: Int, raised: Boolean) = draw { graphics.draw3DRect(x, y, width, height, raised) }
        fun drawArc(x: Int, y: Int, width: Int, height: Int, start_angle: Int, arc_angle: Int) = draw { graphics.drawArc(x, y, width, height, start_angle, arc_angle) }
        fun drawArc(pos: Point, v_radius: Int, h_radius: Int, start_angle: Int, arc_angle: Int) = draw { graphics.drawArc(pos.x - h_radius, pos.y - v_radius, h_radius * 2, v_radius * 2, start_angle, arc_angle) }
        fun drawImage(image: Image, x: Int, y: Int, observer: ImageObserver?) = draw { graphics.drawImage(image, x, y, observer) }
        fun drawLine(x1: Int, y1: Int, x2: Int, y2: Int) = draw { graphics.drawLine(x1, y1, x2, y2) }
        fun drawRoundRect(x: Int, y: Int, width: Int, height: Int, arc_width: Int, arc_height: Int) = draw { graphics.drawRoundRect(x, y, width, height, arc_width, arc_height) }

        fun drawMapleLeaf(x: Int, y: Int, width: Int, height: Int) = draw {
            TODO("Draw maple leaf")
        }

        fun drawStar(x: Int, y: Int, width: Int, height: Int) = draw { TODO("Draw Star") }

        fun drawPolygon(x_points: IntArray, y_points: IntArray, points: Int) = draw { graphics.drawPolygon(x_points, y_points, points) }
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

    val graphics_canvas = Canvas()

    // The keyboard buffer
    val keyboard_buffer = ArrayBlockingQueue<Char>(4096)
    // The line buffer
    val line_buffer = LinkedList<Char>()
    // The JFrame and its components
    val frame: JFrame
    // Font variables
    var font = Font("monospaced", Font.PLAIN, DEFAULT_FONT_SIZE)
    val font_height: Int
    val font_width: Int
    val font_base: Int
    // The base window title
    val window_title: String
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
        val text: String = text ?: "<null>"
        if (text == "\n") {
            setCursor(current_row + 1, 0)
            return
        }
        if (text == "\t") {
            while (++current_col % 4 != 0);
        }
        graphics_canvas.drawText(text = text)
        current_col += text.length
        System.out.println("printed: " + text)
    }

    fun print(number: Number, padding: Int) {
        val padded = StringBuilder()
        padded.append(number)
        // TODO: add spaces
        print(padded.toString())
    }

    @JvmOverloads constructor(font_size: Int = DEFAULT_FONT_SIZE) : this(DEFAULT_ROWS, DEFAULT_COLS, font_size)

    @JvmOverloads constructor(rows: Int, columns: Int, font_size: Int = DEFAULT_FONT_SIZE, title: String = DEFAULT_TITLE) {
        consoles++
        if (title == DEFAULT_TITLE && consoles > 1) {
            window_title = title + " " + consoles
        } else window_title = title

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

    fun drawString(s: String, x: Int, y: Int) = graphics_canvas.drawString(s, x, y)
    fun fillRect(x: Int, y: Int, width: Int, height: Int) = graphics_canvas.fillRect(x, y, width, height)
    fun drawRect(x: Int, y: Int, width: Int, height: Int) = graphics_canvas.drawRect(x, y, width, height)
    fun clearRect(x: Int, y: Int, width: Int, height: Int) = graphics_canvas.clearRect(x, y, width, height)
    fun drawPolygon(x_points: IntArray, y_points: IntArray, points: Int) = graphics_canvas.drawPolygon(x_points, y_points, points)

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

    fun println(value: Any?) {
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
        do {
            ch = readChar()
            string.append(ch)
        } while (ch != '\n')
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
    fun setColor(c: Color) = { graphics_color = c }
}