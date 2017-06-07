import javax.swing.JComponent
import javax.swing.JOptionPane

class Message @JvmOverloads constructor(message: String, parent: JComponent? = null) {
    init {
        JOptionPane.showMessageDialog(parent, message)
    }
}