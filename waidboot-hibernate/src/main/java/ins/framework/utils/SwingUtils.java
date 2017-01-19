package ins.framework.utils;

/**
 * Swing工具类
 */
import java.awt.*;
import javax.swing.text.JTextComponent;

public class SwingUtils {
	/**
	 * 默认构造函数,禁止实例化
	 */
	private SwingUtils() {
	}

	/**
	 * Find the hosting frame
	 * @param c
	 *            component
	 * @return hosting frame
	 */
	public static Frame getFrame(Component c) {
		for (Container p = c.getParent(); p != null; p = p.getParent()) {
			if (p instanceof Frame) {
				return (Frame) p;
			}
		}
		return null;
	}

	/**
	 * Find the hosting dialog
	 * @param c
	 *            component
	 * @return hosting dialog
	 */
	public static Dialog getDialog(Component c) {
		for (Container p = c.getParent(); p != null; p = p.getParent()) {
			if (p instanceof Dialog) {
				return (Dialog) p;
			}
		}
		return null;
	}

	/**
	 * 使window定位于屏幕中间
	 * @param frame
	 *            window
	 */
	public static void center(Window frame) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = frame.getSize();
		frame.setLocation(screenSize.width / 2 - (frameSize.width / 2), screenSize.height / 2 - (frameSize.height / 2));
	}

	/**
	 * 是否没有内容
	 * @param textComponent
	 *            文本控件
	 * @return 如果文本控件内容为null或空，则返回true,否则返回false
	 */
	public static boolean isEmpty(JTextComponent textComponent) {
		if (textComponent.getText() == null || textComponent.getText().length() == 0) {
			return true;
		}
		return false;
	}
}