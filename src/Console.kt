import java.awt.Font
import java.awt.Graphics2D
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.util.*
import javax.swing.*

class Console : KeyListener {
    val keyboard_buffer = LinkedList<Char>()
    val frame: JFrame
    val panel = JPanel()
    lateinit var graphics: Graphics2D
    lateinit var font: Font
    val window_title: String

    fun setState(state: State) {
        frame.title = window_title + " - " + state.string
    }

    companion object {
        var consoles = 0
        const val DEFAULT_ROWS = 25
        const val DEFAULT_COLS = 80
        const val DEFAULT_FONT_SIZE = 14
        const val DEFAULT_TITLE = "Console"
    }

    // TODO: this prints
    fun print(s: String) {

    }

    override fun keyTyped(e: KeyEvent?) {
        if (e?.keyChar == null) return
        val c: Char = e.keyChar
        if (font.canDisplay(c) || c == '\b') keyboard_buffer.add(c)

    }

    override fun keyPressed(e: KeyEvent?) {}
    override fun keyReleased(e: KeyEvent?) {}

    @JvmOverloads constructor(font_size: Int = DEFAULT_FONT_SIZE) : this(DEFAULT_ROWS, DEFAULT_COLS, font_size)

    @JvmOverloads constructor(rows: Int, columns: Int, font_size: Int = DEFAULT_FONT_SIZE, title: String = DEFAULT_TITLE) {
        consoles++
        frame = JFrame()
        window_title = title
        setState(State.RUNNING)
        frame.add(panel)
        frame.pack()
        frame.setLocationRelativeTo(null)
        frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        frame.isResizable = false
        frame.isVisible = true
        frame.repaint()
    }

    init {
        val input_map = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        input_map.put(KeyStroke.getKeyStroke("CONTROL Q"), "quit")
        input_map.put(KeyStroke.getKeyStroke("CONTROL V"), "paste")
        panel.actionMap.put("quit", object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent?) {
                System.exit(0)
            }
        })
        panel.actionMap.put("paste", object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent?) {
                println("paste")
                val clipboard: String = Toolkit.getDefaultToolkit().systemClipboard.getData(DataFlavor.stringFlavor) as String
                for (ch: Char in clipboard) {
                    if (font.canDisplay(ch)) keyboard_buffer.add(ch)
                }
            }
        })
    }

    enum class State(val string: String) {
        RUNNING("Running"),
        WAITING("Waiting for Input"),
        FINISHED("Finished Excecution")
    }
}