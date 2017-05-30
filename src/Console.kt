import java.awt.Font
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.util.*
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.WindowConstants

class Console() : KeyListener {
    val keyboard_buffer = LinkedList<Char>()
    lateinit var frame: JFrame
    val panel = JPanel()
    lateinit var font: Font


    companion object {
        const val DEFAULT_ROWS = 25
        const val DEFAULT_COLS = 80
        const val DEFAULT_FONT_SIZE = 14
        const val DEFAULT_TITLE = "Console"
    }

    // TODO: this prints
    fun print(s: String) {

    }

    fun getChar() = keyboard_buffer.poll()


    override fun keyTyped(e: KeyEvent?) {
        if (e?.keyChar == null) return
        val c: Char = e.keyChar
        when (c) {
            21.toChar() -> System.exit(0) // Quit
            26.toChar() -> { // Paste
                println("paste")
                val clipboard: String = Toolkit.getDefaultToolkit().systemClipboard.getData(DataFlavor.stringFlavor) as String
                for (ch: Char in clipboard) {
                    if (font.canDisplay(ch)) keyboard_buffer.add(ch)
                }
            }
        }
        if (font.canDisplay(c)) {
            keyboard_buffer.add(c)
            (this as Object).notify()
        }
    }

    override fun keyPressed(e: KeyEvent?) {}
    override fun keyReleased(e: KeyEvent?) {}

    constructor(font_size: Int) : this(DEFAULT_ROWS, DEFAULT_COLS, font_size)

    @JvmOverloads constructor(rows: Int, columns: Int, font_size: Int = DEFAULT_FONT_SIZE, title: String = DEFAULT_TITLE) : this() {
        frame = JFrame(title)
    }

    init {
        frame.pack()
        frame.setLocationRelativeTo(null)
        frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        frame.isResizable = false
        frame.isVisible = true
    }
}