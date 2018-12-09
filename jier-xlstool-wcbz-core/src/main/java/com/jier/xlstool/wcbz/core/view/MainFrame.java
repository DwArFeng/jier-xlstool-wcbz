package com.jier.xlstool.wcbz.core.view;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Locale;
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
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import com.dwarfeng.dutil.basic.cna.AttributeComplex;
import com.dwarfeng.dutil.basic.cna.model.DefaultReferenceModel;
import com.dwarfeng.dutil.basic.cna.model.ReferenceModel;
import com.dwarfeng.dutil.basic.cna.model.SyncReferenceModel;
import com.dwarfeng.dutil.basic.cna.model.obv.ListAdapter;
import com.dwarfeng.dutil.basic.cna.model.obv.ListObverser;
import com.dwarfeng.dutil.basic.cna.model.obv.ReferenceAdapter;
import com.dwarfeng.dutil.basic.cna.model.obv.ReferenceObverser;
import com.dwarfeng.dutil.basic.gui.swing.SwingUtil;
import com.dwarfeng.dutil.develop.i18n.SyncI18nHandler;
import com.jier.xlstool.wcbz.core.control.ActionManager;
import com.jier.xlstool.wcbz.core.control.ModelManager;
import com.jier.xlstool.wcbz.core.control.WCBZ;
import com.jier.xlstool.wcbz.core.model.enumeration.CoreSettingItem;
import com.jier.xlstool.wcbz.core.model.enumeration.I18nKey;
import com.jier.xlstool.wcbz.core.util.Constants;
import com.jier.xlstool.wcbz.core.view.task.ExitTask;
import com.jier.xlstool.wcbz.core.view.task.ExportFileTask;
import com.jier.xlstool.wcbz.core.view.task.LoadFileTask;
import com.jier.xlstool.wcbz.core.view.task.SelectExportFileTask;
import com.jier.xlstool.wcbz.core.view.task.SelectLoadFileTask;

/**
 * 主窗口文件。
 * 
 * @author DwArFeng
 * @since 0.0.0-alpha
 */
public class MainFrame extends JFrame {

	private static final long serialVersionUID = -9062766495826841092L;

	private final JPanel contentPane;
	private final JTextField tfFile2Load;
	private final JTextField tfFile2Export;
	private final JButton btnLoad;
	private final JButton btnExport;
	private final JTable table;
	private final JLabel lbFile2Open;
	private final JLabel lbFile2Export;
	private final JLabel lbState;
	private final ReferenceModel<String> selectOpenFileDialogTitle = new DefaultReferenceModel<>();
	private final ReferenceModel<String> selectExportFileDialogTitle = new DefaultReferenceModel<>();
	private final ReferenceModel<String> xlsFileNameExtensionDescribe = new DefaultReferenceModel<>();

	private ModelManager modelManager;
	private ActionManager actionManager;

	private final DefaultTableModel tableModel = new DefaultTableModel() {

		private static final long serialVersionUID = -7567108920586376035L;

		@Override
		public int getColumnCount() {
			return 4;
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}

	};

	private final ReferenceObverser<File> file2LoadObverser = new ReferenceAdapter<File>() {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void fireSet(File oldValue, File newValue) {
			SwingUtil.invokeInEventQueue(() -> {
				tfFile2Load.setText(newValue.getAbsolutePath());
			});
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void fireCleared() {
			SwingUtil.invokeInEventQueue(() -> {
				tfFile2Load.setText("");
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
	private final ListObverser<AttributeComplex> stuffInfoObverser = new ListAdapter<AttributeComplex>() {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void fireAdded(int index, AttributeComplex attributeComplex) {
			SwingUtil.invokeInEventQueue(() -> {
				tableModel.insertRow(index,
						new Object[] { //
								attributeComplex.get(Constants.ATTRIBUTE_COMPLEX_MARK_DEPARTMENT), //
								attributeComplex.get(Constants.ATTRIBUTE_COMPLEX_MARK_WORK_NUMBER), //
								attributeComplex.get(Constants.ATTRIBUTE_COMPLEX_MARK_STUFF_NAME), //
								attributeComplex.get(Constants.ATTRIBUTE_COMPLEX_MARK_ABSENCE_COUNT),//
				});
			});
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void fireRemoved(int index, AttributeComplex element) {
			SwingUtil.invokeInEventQueue(() -> {
				tableModel.removeRow(index);
			});
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void fireChanged(int index, AttributeComplex oldElement, AttributeComplex newElement) {
			SwingUtil.invokeInEventQueue(() -> {
				tableModel.removeRow(index);
				tableModel.insertRow(index,
						new Object[] { //
								newElement.get(Constants.ATTRIBUTE_COMPLEX_MARK_DEPARTMENT), //
								newElement.get(Constants.ATTRIBUTE_COMPLEX_MARK_WORK_NUMBER), //
								newElement.get(Constants.ATTRIBUTE_COMPLEX_MARK_STUFF_NAME), //
								newElement.get(Constants.ATTRIBUTE_COMPLEX_MARK_ABSENCE_COUNT),//
				});
			});
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void fireCleared() {
			SwingUtil.invokeInEventQueue(() -> {
				while (tableModel.getRowCount() > 0) {
					tableModel.removeRow(0);
				}
			});
		}

	};
	private JLabel lblNewLabel;

	public MainFrame() {
		this(null, null);
	}

	/**
	 * Create the frame.
	 */
	public MainFrame(ModelManager modelManager, ActionManager actionManager) {
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 509, 531);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				checkManagerAndDo(() -> {
					MainFrame.this.actionManager
							.submit(new ExitTask(MainFrame.this.modelManager, MainFrame.this.actionManager));
				});
			}
		});

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

		tfFile2Load = new JTextField();
		tfFile2Load.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				checkManagerAndDo(() -> {
					Locale locale = MainFrame.this.modelManager.getCoreSettingHandler()
							.getParsedValidValue(CoreSettingItem.I18N_LOCALE, Locale.class);
					String dialogTitle = selectOpenFileDialogTitle.get();
					File refDirectory = Optional.ofNullable(MainFrame.this.modelManager.getFile2ExportModel().get())
							.orElse(null);
					FileFilter[] choosableFileFilters = new FileFilter[] { new FileNameExtensionFilter(
							xlsFileNameExtensionDescribe.get(), Constants.FILE_EXTENSION_EXCEL_97_03) };
					boolean acceptAllFileFilterUsed = false;

					MainFrame.this.actionManager.submit(new SelectLoadFileTask(MainFrame.this.modelManager,
							MainFrame.this.actionManager, locale, dialogTitle, refDirectory, choosableFileFilters,
							acceptAllFileFilterUsed, MainFrame.this));
				});
			}

		});
		tfFile2Load.setEditable(false);
		tfFile2Load.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		GridBagConstraints gbc_tfFile2Open = new GridBagConstraints();
		gbc_tfFile2Open.fill = GridBagConstraints.BOTH;
		gbc_tfFile2Open.insets = new Insets(0, 0, 5, 5);
		gbc_tfFile2Open.gridx = 1;
		gbc_tfFile2Open.gridy = 0;
		panel.add(tfFile2Load, gbc_tfFile2Open);
		tfFile2Load.setColumns(10);

		btnLoad = new JButton();
		btnLoad.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				checkManagerAndDo(() -> {
					MainFrame.this.actionManager.submit(new LoadFileTask(MainFrame.this.modelManager,
							MainFrame.this.actionManager, MainFrame.this));
				});
			}
		});
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
		tfFile2Export.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				checkManagerAndDo(() -> {
					Locale locale = MainFrame.this.modelManager.getCoreSettingHandler()
							.getParsedValidValue(CoreSettingItem.I18N_LOCALE, Locale.class);
					String dialogTitle = selectExportFileDialogTitle.get();
					File refDirectory = Optional.ofNullable(MainFrame.this.modelManager.getFile2LoadModel().get())
							.orElse(null);
					FileFilter[] choosableFileFilters = new FileFilter[] { new FileNameExtensionFilter(
							xlsFileNameExtensionDescribe.get(), Constants.FILE_EXTENSION_EXCEL_97_03) };
					boolean acceptAllFileFilterUsed = true;

					MainFrame.this.actionManager.submit(new SelectExportFileTask(MainFrame.this.modelManager,
							MainFrame.this.actionManager, locale, dialogTitle, refDirectory, choosableFileFilters,
							acceptAllFileFilterUsed, MainFrame.this));
				});
			}
		});
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
		btnExport.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				checkManagerAndDo(() -> {
					MainFrame.this.actionManager.submit(new ExportFileTask(MainFrame.this.modelManager,
							MainFrame.this.actionManager, MainFrame.this));
				});
			}
		});
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
		table.getTableHeader().setReorderingAllowed(false);
		table.setFillsViewportHeight(true);
		table.setModel(tableModel);
		scrollPane.setViewportView(table);

		JPanel panel_2 = new JPanel();
		contentPane.add(panel_2, BorderLayout.SOUTH);
		panel_2.setLayout(new BorderLayout(0, 0));

		lbState = new JLabel();
		panel_2.add(lbState, BorderLayout.CENTER);

		lblNewLabel = new JLabel(WCBZ.VERSION.toString());
		panel_2.add(lblNewLabel, BorderLayout.EAST);

		this.modelManager = modelManager;
		this.actionManager = actionManager;

		Optional.ofNullable(this.modelManager).ifPresent(manager -> {
			manager.getFile2LoadModel().addObverser(file2LoadObverser);
			manager.getFile2ExportModel().addObverser(file2ExportObverser);
			manager.getStuffInfoModel().addObverser(stuffInfoObverser);
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
	 * @param modelManager
	 *            the modelManager to set
	 */
	public void setModelManager(ModelManager modelManager) {
		Optional.ofNullable(this.modelManager).ifPresent(manager -> {
			manager.getFile2LoadModel().removeObverser(file2LoadObverser);
			manager.getFile2ExportModel().removeObverser(file2ExportObverser);
			manager.getStuffInfoModel().removeObverser(stuffInfoObverser);
		});
		this.modelManager = modelManager;
		Optional.ofNullable(this.modelManager).ifPresent(manager -> {
			manager.getFile2LoadModel().addObverser(file2LoadObverser);
			manager.getFile2ExportModel().addObverser(file2ExportObverser);
			manager.getStuffInfoModel().addObverser(stuffInfoObverser);
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
	 * @param actionManager
	 *            the actionManager to set
	 */
	public void setActionManager(ActionManager actionManager) {
		this.actionManager = actionManager;
	}

	private void syncModel() {
		SyncI18nHandler i18nHandler = modelManager.getI18nHandler();
		SyncReferenceModel<File> file2LoadModel = modelManager.getFile2LoadModel();
		SyncReferenceModel<File> file2ExportModel = modelManager.getFile2ExportModel();

		lbFile2Open.setText(Constants.MISSING_LABEL);
		lbFile2Open.setText(Constants.MISSING_LABEL);
		btnLoad.setText(Constants.MISSING_LABEL);
		btnExport.setText(Constants.MISSING_LABEL);
		tfFile2Load.setToolTipText(Constants.MISSING_LABEL);
		tfFile2Export.setToolTipText(Constants.MISSING_LABEL);
		selectOpenFileDialogTitle.set(Constants.MISSING_LABEL);
		selectExportFileDialogTitle.set(Constants.MISSING_LABEL);
		xlsFileNameExtensionDescribe.set(Constants.MISSING_LABEL);
		table.getColumnModel().getColumn(0).setHeaderValue(Constants.MISSING_LABEL);
		table.getColumnModel().getColumn(1).setHeaderValue(Constants.MISSING_LABEL);
		table.getColumnModel().getColumn(2).setHeaderValue(Constants.MISSING_LABEL);
		table.getColumnModel().getColumn(3).setHeaderValue(Constants.MISSING_LABEL);
		lbState.setText(Constants.MISSING_LABEL);
		this.setTitle(Constants.MISSING_LABEL);

		tfFile2Load.setText("");
		tfFile2Export.setText("");

		if (Objects.isNull(modelManager)) {
			return;
		}

		i18nHandler.getLock().readLock().lock();
		try {
			lbFile2Open.setText(i18nString(I18nKey.LABEL_1));
			lbFile2Export.setText(i18nString(I18nKey.LABEL_2));
			btnLoad.setText(i18nString(I18nKey.LABEL_3));
			btnExport.setText(i18nString(I18nKey.LABEL_4));
			tfFile2Load.setToolTipText(i18nString(I18nKey.LABEL_5));
			tfFile2Export.setToolTipText(i18nString(I18nKey.LABEL_5));
			selectOpenFileDialogTitle.set(i18nString(I18nKey.LABEL_6));
			selectExportFileDialogTitle.set(i18nString(I18nKey.LABEL_7));
			xlsFileNameExtensionDescribe.set(i18nString(I18nKey.LABEL_8));
			table.getColumnModel().getColumn(0).setHeaderValue(i18nString(I18nKey.LABEL_14));
			table.getColumnModel().getColumn(1).setHeaderValue(i18nString(I18nKey.LABEL_9));
			table.getColumnModel().getColumn(2).setHeaderValue(i18nString(I18nKey.LABEL_10));
			table.getColumnModel().getColumn(3).setHeaderValue(i18nString(I18nKey.LABEL_11));
			lbState.setText(i18nString(I18nKey.LABEL_16));
			this.setTitle(i18nString(I18nKey.LABEL_17));
		} finally {
			i18nHandler.getLock().readLock().unlock();
		}

		file2LoadModel.getLock().readLock().lock();
		try {
			tfFile2Load
					.setText(Optional.ofNullable(file2LoadModel.get()).map(file -> file.getAbsolutePath()).orElse(""));
		} finally {
			file2LoadModel.getLock().readLock().unlock();
		}

		file2ExportModel.getLock().readLock().lock();
		try {
			tfFile2Export.setText(
					Optional.ofNullable(file2ExportModel.get()).map(file -> file.getAbsolutePath()).orElse(""));
		} finally {
			file2ExportModel.getLock().readLock().unlock();
		}
	}

	private String i18nString(I18nKey i18nKey) {
		return Optional.ofNullable(modelManager)
				.map(manager -> manager.getI18nHandler().getStringOrDefault(i18nKey, Constants.MISSING_LABEL))
				.orElse("null");
	}

	private void checkManagerAndDo(Runnable runnable) {
		if (Objects.isNull(modelManager) || Objects.isNull(actionManager)) {
			return;
		}
		runnable.run();
	}

}
