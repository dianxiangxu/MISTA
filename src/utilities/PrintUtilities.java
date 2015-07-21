package utilities;

import java.awt.*;
import javax.swing.*;
import java.awt.print.*;

/**
 * A simple utility class that lets you very simply print an arbitrary
 * component. Just pass the component to the PrintUtilities.printComponent. The
 * component you want to print doesn't need a print method and doesn't have to
 * implement any interface or do anything special at all.
 * <P>
 * If you are going to be printing many times, it is marginally more efficient
 * to first do the following:
 * 
 * <PRE>
 * 
 * PrintUtilities printHelper = new PrintUtilities(theComponent);
 * 
 * </PRE>
 * 
 * then later do printHelper.print(). But this is a very tiny difference, so in
 * most cases just do the simpler
 * PrintUtilities.printComponent(componentToBePrinted).
 * 
 * 7/99 Marty Hall, http://www.apl.jhu.edu/~hall/java/ May be freely used or
 * adapted.
 */

public class PrintUtilities implements Printable {
	private Component componentToBePrinted;

	public static void printComponent(Component c) {
		new PrintUtilities(c).print();
	}

	public PrintUtilities(Component componentToBePrinted) {
		this.componentToBePrinted = componentToBePrinted;
	}

	public void print() {
		PrinterJob printJob = PrinterJob.getPrinterJob();
		printJob.setPrintable(this);
		if (printJob.printDialog())
			try {
				printJob.print();
			} catch (PrinterException pe) {
				System.out.println("Error printing: " + pe);
			}
	}

	public int print(Graphics g, PageFormat pf, int pi) {
		if (pi >= 1) {
			return Printable.NO_SUCH_PAGE;
		}

		Graphics2D g2 = (Graphics2D) g;
//		Font f = Font.getFont("Courier");
//		double height = pf.getImageableHeight();
//		double width = pf.getImageableWidth();

		g2.translate(pf.getImageableX(), pf.getImageableY());
		g2.setColor(Color.black);
		//g2.drawString("NDSU Computer Science", (int) width / 2, (int) height
		//		- g2.getFontMetrics().getHeight());
		g2.translate(0f, 0f);
		g2.scale(0.46, 0.46);
		// g2.setClip(0,0,(int)width,
		// (int)(height-g2.getFontMetrics().getHeight()*2));
		componentToBePrinted.paint(g2);
		return Printable.PAGE_EXISTS;
	}

	/**
	 * The speed and quality of printing suffers dramatically if any of the
	 * containers have double buffering turned on. So this turns if off
	 * globally.
	 * 
	 * @see enableDoubleBuffering
	 */
	public static void disableDoubleBuffering(Component c) {
		RepaintManager currentManager = RepaintManager.currentManager(c);
		currentManager.setDoubleBufferingEnabled(false);
	}

	/** Re-enables double buffering globally. */

	public static void enableDoubleBuffering(Component c) {
		RepaintManager currentManager = RepaintManager.currentManager(c);
		currentManager.setDoubleBufferingEnabled(true);
	}
}
