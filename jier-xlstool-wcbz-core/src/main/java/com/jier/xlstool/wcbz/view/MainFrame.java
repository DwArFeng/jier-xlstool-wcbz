package com.jier.xlstool.wcbz.view;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.Objects;
import java.util.Optional;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.dwarfeng.dutil.basic.cna.model.obv.ReferenceAdapter;
import com.dwarfeng.dutil.basic.cna.model.obv.ReferenceObverser;
import com.dwarfeng.dutil.basic.gui.swing.SwingUtil;
import com.jier.xlstool.wcbz.core.control.ActionManager;
import com.jier.xlstool.wcbz.core.control.ModelManager;
import com.jier.xlstool.wcbz.core.model.enumeration.I18nKey;
import com.jier.xlstool.wcbz.core.util.Constants;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 主窗口文件。
 * 
 * @author DwArFeng
 * @since 0.0.0-alpha
 */
public class MainFrame extends JFrame {

	private final JPanel contentPane;
	private final JTextField tfFile2Open;
	private final JTextField tfFile2Export;
	private final JButton btnLoad;
	private final JButton btnExport;
	private final JTable table;
	private final JLabel lbFile2Open;
	private final JLabel lbFile2Export;

	private ModelManager modelManager;
	private ActionManager actionManager;

	private final ReferenceObverser<File> file2OpenObverser = new ReferenceAdapter<File>() {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void fireSet(File oldValue, File newValue) {
			SwingUtil.invokeInEventQueue(() -> {
				tfFile2Open.setText(newValue.getAbsolutePath());
			});
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void fireCleared() {
			SwingUtil.invokeInEventQueue(() -> {
				tfFile2Open.setText("");
			});
		}

	};

	private final ReferenceObverser<File> file2ExportObverser = new ReferenceAdapter<File>() {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void fireSet(File oldValue, File newValue) {
			SwingUtil.invokeInEventQueue(() -> {
				tfFile2Export.setText(newValue.getAbsolutePath());
			});
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void fireCleared() {
			SwingUtil.invokeInEventQueue(() -> {
				tfFile2Export.setText("");
			});
		}

	};

	public MainFrame() {
		this(null, null);
	}

	/**
	 * Create the frame.
	 */
	public MainFrame(ModelManager modelManager, ActionManager actionManager) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 509, 531);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.NORTH);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 55, 122, 90, 0 };
		gbl_panel.rowHeights = new int[] { 30, 30, 0 };
		gbl_panel.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		lbFile2Open = new JLabel();
		GridBagConstraints gbc_lbFile2Open = new GridBagConstraints();
		gbc_lbFile2Open.fill = GridBagConstraints.BOTH;
		gbc_lbFile2Open.insets = new Insets(0, 0, 5, 5);
		gbc_lbFile2Open.gridx = 0;
		gbc_lbFile2Open.gridy = 0;
		panel.add(lbFile2Open, gbc_lbFile2Open);

		tfFile2Open = new JTextField();
		tfFile2Open.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {

			}
		});
		tfFile2Open.setEditable(false);
		tfFile2Open.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		GridBagConstraints gbc_tfFile2Open = new GridBagConstraints();
		gbc_tfFile2Open.fill = GridBagConstraints.BOTH;
		gbc_tfFile2Open.insets = new Insets(0, 0, 5, 5);
		gbc_tfFile2Open.gridx = 1;
		gbc_tfFile2Open.gridy = 0;
		panel.add(tfFile2Open, gbc_tfFile2Open);
		tfFile2Open.setColumns(10);

		btnLoad = new JButton();
		GridBagConstraints gbc_btnLoad = new GridBagConstraints();
		gbc_btnLoad.fill = GridBagConstraints.BOTH;
		gbc_btnLoad.insets = new Insets(0, 0, 5, 0);
		gbc_btnLoad.gridx = 2;
		gbc_btnLoad.gridy = 0;
		panel.add(btnLoad, gbc_btnLoad);

		lbFile2Export = new JLabel();
		GridBagConstraints gbc_lbFile2Export = new GridBagConstraints();
		gbc_lbFile2Export.fill = GridBagConstraints.BOTH;
		gbc_lbFile2Export.insets = new Insets(0, 0, 0, 5);
		gbc_lbFile2Export.gridx = 0;
		gbc_lbFile2Export.gridy = 1;
		panel.add(lbFile2Export, gbc_lbFile2Export);

		tfFile2Export = new JTextField();
		tfFile2Export.setEditable(false);
		tfFile2Export.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		GridBagConstraints gbc_tfFile2Export = new GridBagConstraints();
		gbc_tfFile2Export.fill = GridBagConstraints.BOTH;
		gbc_tfFile2Export.insets = new Insets(0, 0, 0, 5);
		gbc_tfFile2Export.gridx = 1;
		gbc_tfFile2Export.gridy = 1;
		panel.add(tfFile2Export, gbc_tfFile2Export);
		tfFile2Export.setColumns(10);

		btnExport = new JButton();
		GridBagConstraints gbc_btnExport = new GridBagConstraints();
		gbc_btnExport.fill = GridBagConstraints.BOTH;
		gbc_btnExport.gridx = 2;
		gbc_btnExport.gridy = 1;
		panel.add(btnExport, gbc_btnExport);

		JPanel panel_1 = new JPanel();
		contentPane.add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		panel_1.add(scrollPane, BorderLayout.CENTER);

		table = new JTable();
		table.setFillsViewportHeight(true);
		scrollPane.setViewportView(table);

		JPanel panel_2 = new JPanel();
		contentPane.add(panel_2, BorderLayout.SOUTH);
		panel_2.setLayout(new BorderLayout(0, 0));

		JLabel lbState = new JLabel("New label");
		panel_2.add(lbState, BorderLayout.CENTER);

		this.modelManager = modelManager;
		this.actionManager = actionManager;

		Optional.ofNullable(this.modelManager).ifPresent(manager -> {
			manager.getFile2OpenModel().addObverser(file2OpenObverser);
			manager.getFile2ExportModel().addObverser(file2ExportObverser);
			// TODO
		});

		syncModel();
	}

	/**
	 * @return the modelManager
	 */
	public ModelManager getModelManager() {
		return modelManager;
	}

	/**
	 * @param modelManager the modelManager to set
	 */
	public void setModelManager(ModelManager modelManager) {
		Optional.ofNullable(this.modelManager).ifPresent(manager -> {
			manager.getFile2OpenModel().removeObverser(file2OpenObverser);
			manager.getFile2ExportModel().removeObverser(file2ExportObverser);
			// TODO Auto-generated method stub
		});
		this.modelManager = modelManager;
		Optional.ofNullable(this.modelManager).ifPresent(manager -> {
			manager.getFile2OpenModel().addObverser(file2OpenObverser);
			manager.getFile2ExportModel().addObverser(file2ExportObverser);
			// TODO Auto-generated method stub
		});
		syncModel();
	}

	/**
	 * @return the actionManager
	 */
	public ActionManager getActionManager() {
		return actionManager;
	}

	/**
	 * @param actionManager the actionManager to set
	 */
	public void setActionManager(ActionManager actionManager) {
		this.actionManager = actionManager;
	}

	private void syncModel() {
		lbFile2Open.setText(Constants.MISSING_LABEL);
		lbFile2Open.setText(Constants.MISSING_LABEL);
		btnLoad.setText(Constants.MISSING_LABEL);
		btnExport.setText(Constants.MISSING_LABEL);
		tfFile2Open.setToolTipText(Constants.MISSING_LABEL);
		tfFile2Export.setToolTipText(Constants.MISSING_LABEL);

		if (Objects.isNull(modelManager)) {
			return;
		}

		modelManager.getI18nHandler().getLock().readLock().lock();
		try {
			lbFile2Open.setText(i18nString(I18nKey.LABEL_1));
			lbFile2Export.setText(i18nString(I18nKey.LABEL_2));
			btnLoad.setText(i18nString(I18nKey.LABEL_3));
			btnExport.setText(i18nString(I18nKey.LABEL_4));
			tfFile2Open.setToolTipText(i18nString(I18nKey.LABEL_5));
			tfFile2Export.setToolTipText(i18nString(I18nKey.LABEL_5));
		} finally {
			modelManager.getI18nHandler().getLock().readLock().unlock();
		}

		// TODO Auto-generated method stub
	}

	private String i18nString(I18nKey i18nKey) {
		return Optional.ofNullable(modelManager)
				.map(manager -> manager.getI18nHandler().getStringOrDefault(i18nKey, Constants.MISSING_LABEL))
				.orElse("null");
	}

}
