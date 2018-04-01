package dev.utils;

import java.awt.*;
import java.awt.datatransfer.*;

import javax.swing.*;

public class DualDnDListBox extends JFrame {

	DefaultListModel from = new DefaultListModel();
	DefaultListModel copy = new DefaultListModel();
	DefaultListModel move = new DefaultListModel();
	JList dragFrom;

	public DualDnDListBox() {
		super("ChooseDropActionDemo");

		for (int i = 15; i >= 0; i--) {
			from.add(0, "Source item " + i);
		}

		for (int i = 2; i >= 0; i--) {
			copy.add(0, "Target item " + i);
			move.add(0, "Target item " + i);
		}

		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		dragFrom = new JList(from);
		dragFrom.setTransferHandler(new FromTransferHandler());
		dragFrom.setPrototypeCellValue("List Item WWWWWW");
		dragFrom.setDragEnabled(true);
		dragFrom.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JLabel label = new JLabel("Drag from here:");
		label.setAlignmentX(0f);
		p.add(label);
		JScrollPane sp = new JScrollPane(dragFrom);
		sp.setAlignmentX(0f);
		p.add(sp);
		add(p, BorderLayout.WEST);

		JList moveTo = new JList(move);
		moveTo.setTransferHandler(new ToTransferHandler(TransferHandler.COPY));
		moveTo.setDropMode(DropMode.INSERT);
		JList copyTo = new JList(copy);
		copyTo.setTransferHandler(new ToTransferHandler(TransferHandler.MOVE));
		copyTo.setDropMode(DropMode.INSERT);

		p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		label = new JLabel("Drop to COPY to here:");
		label.setAlignmentX(0f);
		p.add(label);
		sp = new JScrollPane(moveTo);
		sp.setAlignmentX(0f);
		p.add(sp);
		label = new JLabel("Drop to MOVE to here:");
		label.setAlignmentX(0f);
		p.add(label);
		sp = new JScrollPane(copyTo);
		sp.setAlignmentX(0f);
		p.add(sp);
		p.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
		add(p, BorderLayout.CENTER);

		((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		getContentPane().setPreferredSize(new Dimension(320, 315));
	}

	private static void createAndShowGUI() {
		// Create and set up the window.
		DualDnDListBox test = new DualDnDListBox();
		test.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	
		// Display the window.
		test.pack();
		test.setLocationRelativeTo(null);
		test.setVisible(true);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// Turn off metal's use of bold fonts
				UIManager.put("swing.boldMetal", Boolean.FALSE);
				createAndShowGUI();
			}
		});
	}

	class FromTransferHandler extends TransferHandler {
		public int getSourceActions(JComponent comp) {
			return COPY_OR_MOVE;
		}

		private int index = 0;

		public Transferable createTransferable(JComponent comp) {
			index = dragFrom.getSelectedIndex();
			if (index < 0 || index >= from.getSize()) {
				return null;
			}

			return new StringSelection((String) dragFrom.getSelectedValue());
		}

		public void exportDone(JComponent comp, Transferable trans, int action) {
			if (action != MOVE) {
				return;
			}

			from.removeElementAt(index);
		}
	}

	class ToTransferHandler extends TransferHandler {
		int action;

		public ToTransferHandler(int action) {
			this.action = action;
		}

		public boolean canImport(TransferHandler.TransferSupport support) {
			// for the demo, we'll only support drops (not clipboard paste)
			if (!support.isDrop()) {
				return false;
			}

			// we only import Strings
			if (!support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				return false;
			}

			boolean actionSupported = (action & support.getSourceDropActions()) == action;
			if (actionSupported) {
				support.setDropAction(action);
				return true;
			}

			return false;
		}

		public boolean importData(TransferHandler.TransferSupport support) {
			// if we can't handle the import, say so
			if (!canImport(support)) {
				return false;
			}

			// fetch the drop location
			JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();

			int index = dl.getIndex();

			// fetch the data and bail if this fails
			String data;
			try {
				data = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
			} catch (UnsupportedFlavorException e) {
				return false;
			} catch (java.io.IOException e) {
				return false;
			}

			JList list = (JList) support.getComponent();
			DefaultListModel model = (DefaultListModel) list.getModel();
			model.insertElementAt(data, index);

			Rectangle rect = list.getCellBounds(index, index);
			list.scrollRectToVisible(rect);
			list.setSelectedIndex(index);
			list.requestFocusInWindow();

			return true;
		}
	}
}