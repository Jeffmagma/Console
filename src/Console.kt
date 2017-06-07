import java.awt.*
import java.awt.datatransfer.DataFlavor
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.image.BufferedImage
import java.util.concurrent.ArrayBlockingQueue
import javax.swing.*
import kotlin.concurrent.timer

class Console {

    class Canvas : JPanel() {
        val image = BufferedImage(69, 420, BufferedImage.TYPE_4BYTE_ABGR)
        val graphics: Graphics2D = image.createGraphics()
        var buffered = false

        init {
            timer(period = 1) {
                if (buffered) repaint()
            }
        }

        fun drawString(s: String, x: Int, y: Int) {
            graphics.drawString(s, x, y)
            buffered = true
        }

        override fun paintComponent(g: Graphics) {
            System.out.println(buffered)
            g.drawImage(image, 0, 0, null)
            buffered = false
        }
    }

    val graphics_canvas = Canvas()

    // The keyboard buffer
    val keyboard_buffer = ArrayBlockingQueue<Char>(4096)
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

    var text_color = Color.BLACK
    var background_color = Color.WHITE

    fun setState(state: State) {
        frame.title = window_title + " - " + state.string
    }

    // static variables basically
    companion object {
        var consoles = 0
        const val DEFAULT_ROWS = 25
        const val DEFAULT_COLS = 80
        const val DEFAULT_FONT_SIZE = 14
        const val DEFAULT_TITLE = "Console"
        val whitespace = arrayOf('\n', '\t', ' ')
    }

    // TODO: this prints
    fun print(text: String) {
        System.out.println("printed: " + text)
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
        font = Font("monospaced", Font.PLAIN, font_size)
        val font_metrics = graphics_canvas.getFontMetrics(font)
        font_height = font_metrics.height + font_metrics.leading
        font_base = font_metrics.descent
        val chars = 0..256
        val char_widths = chars.map { font_metrics.charWidth(it) }
        font_width = char_widths.max() ?: 0
        // Calculate the height and width of the screen
        width = font_width * columns
        height = font_height * rows
        // Construct a JFrame
        frame = JFrame()
        frame.addKeyListener(object : KeyListener {
            override fun keyTyped(e: KeyEvent?) {
                if (e?.keyChar == null) return
                val c = e.keyChar
                if (font.canDisplay(c) || c == '\b') keyboard_buffer.add(c)
            }

            override fun keyPressed(e: KeyEvent?) {}
            override fun keyReleased(e: KeyEvent?) {}
        })
        frame.contentPane.preferredSize = Dimension(width, height)
        frame.add(graphics_canvas)
        frame.isResizable = false
        frame.pack()
        setState(State.RUNNING)
        frame.setLocationRelativeTo(null)
        frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        frame.isVisible = true
        frame.requestFocus()
    }

    fun drawString(s: String, x: Int, y: Int) {
        graphics_canvas.drawString(s, x, y)
    }

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
                println(clipboard)
                for (ch: Char in clipboard) {
                    if (font.canDisplay(ch)) keyboard_buffer.add(ch)
                }
            }
        })
        // TODO Implement printing
    }

    // The extension at the top beside the console name
    enum class State(val string: String) {
        RUNNING("Running"),
        WAITING("Waiting for Input"),
        FINISHED("Finished Execution")
    }

    fun print(value: Any) {
        print(value.toString())
    }

    fun println(value: Any) {
        print(value)
        print("\n")
    }

    fun setCursor(row: Int, column: Int) {
        current_row = row
        current_col = column
        // set inside canvas
    }

    fun readString(): String {
        val string = StringBuilder()
        var ch: Char
        // skip whitespace
        do {
            ch = readChar()
        } while (ch in whitespace)
        // read until whitespace and append to string
        while (ch !in whitespace) {
            string.append(ch)
            ch = readChar()
        }
        return string.toString()
    }

    // TODO make this read a char
    fun readChar() = getChar()

    fun getChar(): Char {
        val k = keyboard_buffer.take()
        keyboard_buffer.poll()
        return k
    }

    fun setBackgroundColor(color: Color) {
        background_color = color
    }

    /**
     * Sets the text color to [color]
     */
    fun setTextColor(color: Color) {
        text_color = color
    }
}