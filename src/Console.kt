import java.awt.*
import java.awt.datatransfer.DataFlavor
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.util.*
import javax.swing.*

class Console {

    class ConsoleGraphics : JPanel() {

    }


    // The keyboard buffer
    val keyboard_buffer = LinkedList<Char>()
    // The JFrame and its components
    val frame: JFrame
    lateinit var graphics: Graphics2D
    val panel = object : JPanel() {
        override fun paintComponent(g: Graphics) {
            this@Console.graphics = g as Graphics2D
            g.color = Color.RED
            g.drawRect(x, y, this@Console.width, this@Console.height)
        }
    }
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
    }

    // TODO: this prints
    fun print(text: String) {
        val length = text.length
        var index = 0
        var start_pos = current_col
        while (true) {
            if (index == length) return
            while (index < length && text[index] != '\n' && text[index] != '\t' && start_pos + index < cols) index++
            if (start_pos != index) {

            }
        }
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
        val font_metrics = panel.getFontMetrics(font)
        font_height = font_metrics.height + font_metrics.leading
        font_base = font_metrics.descent
        val chars = 0..256
        val char_widths = chars.map { font_metrics.charWidth(it) }
        font_width = char_widths.max() ?: 0
        println(font_width)
        // Calcualte the height adn width of the screen
        width = font_width * columns
        height = font_height * rows
        println("" + width + " " + height)
        // Construct a JFrame
        frame = JFrame()
        frame.addKeyListener(object : KeyListener {
            override fun keyTyped(e: KeyEvent?) {
                if (e?.keyChar == null) return
                val c = e.keyChar
                if (font.canDisplay(c) || c == '\b') keyboard_buffer.add(c)
                println(keyboard_buffer)
            }

            override fun keyPressed(e: KeyEvent?) {}
            override fun keyReleased(e: KeyEvent?) {}
        })
        frame.contentPane.preferredSize = Dimension(width, height)
        setState(State.RUNNING)
        frame.add(panel)
        frame.pack()
        frame.setLocationRelativeTo(null)
        frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        frame.isResizable = false
        frame.isVisible = true
        frame.requestFocus()
    }

    init {
        // Get the input map for key bindings
        val input_map = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        // Set CTRL+Q to quit, and CTRL+V to paste
        input_map.put(KeyStroke.getKeyStroke("control Q"), "quit")
        input_map.put(KeyStroke.getKeyStroke("control V"), "paste")
        input_map.put(KeyStroke.getKeyStroke("control P"), "print")
        // Quit is just System.exit(0) but since AbstractAction is abstract, you can't use a lambda
        panel.actionMap.put("quit", object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent?) {
                System.exit(0)
            }
        })
        // Paste by getting the clipboard contents, then checking which ones you can print, and add them to the keyboard buffer
        panel.actionMap.put("paste", object : AbstractAction() {
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
        FINISHED("Finished Excecution")
    }
}