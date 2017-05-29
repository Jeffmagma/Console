import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.WindowConstants

class Console() : KeyListener {
    val keyboard_buffer = listOf<Char>()
    var frame = JFrame()
    val panel = JPanel()


    fun print(s: String) {

    }

    override fun keyTyped(e: KeyEvent?) {
        // TODO check if int to char comparison works
        when (e?.keyChar) {
            21.toChar() -> System.exit(0) // Quit
            26.toChar() -> { // Paste
                println("paste")
                val clipboard: String = Toolkit.getDefaultToolkit().systemClipboard.getData(DataFlavor.stringFlavor) as String
                for (c: Char in clipboard) {
                    if (c in '!'..'_' || c == '\n') println(c)
                }
            }
            in '!'..'_' -> keyboard_buffer.last()
        }
    }

    override fun keyPressed(e: KeyEvent?) {}
    override fun keyReleased(e: KeyEvent?) {}

    constructor(font_size: Int) : this(font = font_size)

    constructor(rows: Int = 25, columns: Int = 80, font: Int = 24, title: String = "Console") : this() {
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