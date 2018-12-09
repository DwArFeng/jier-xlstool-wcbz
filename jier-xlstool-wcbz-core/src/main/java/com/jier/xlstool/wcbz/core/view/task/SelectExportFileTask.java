package com.jier.xlstool.wcbz.core.view.task;

import java.awt.Component;
import java.io.File;
import java.util.Locale;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import com.jier.xlstool.wcbz.core.control.ActionManager;
import com.jier.xlstool.wcbz.core.control.ModelManager;

public class SelectExportFileTask extends AbstractViewTask {

	private final Locale locale;
	private final String dialogTitle;
	private final File refDirectory;
	private final FileFilter[] choosableFileFilters;
	private final boolean acceptAllFileFilterUsed;
	private final Component parentComponent;

	public SelectExportFileTask(ModelManager modelManager, ActionManager actionManager, Locale locale,
			String dialogTitle, File refDirectory, FileFilter[] choosableFileFilters, boolean acceptAllFileFilterUsed,
			Component parentComponent) {
		super(modelManager, actionManager);
		this.locale = locale;
		this.dialogTitle = dialogTitle;
		this.refDirectory = refDirectory;
		this.choosableFileFilters = choosableFileFilters;
		this.acceptAllFileFilterUsed = acceptAllFileFilterUsed;
		this.parentComponent = parentComponent;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void todo() throws Exception {
		JFileChooser jfc = new JFileChooser();
		jfc.setDialogTitle(dialogTitle);
		jfc.setCurrentDirectory(refDirectory);
		jfc.setLocale(locale);
		jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		for (FileFilter fileFilter : choosableFileFilters) {
			jfc.addChoosableFileFilter(fileFilter);
		}
		if (choosableFileFilters.length > 0) {
			jfc.setFileFilter(choosableFileFilters[0]);
		}
		jfc.setAcceptAllFileFilterUsed(acceptAllFileFilterUsed);
		// 打开对话框，让用户选择文件。
		int result = jfc.showSaveDialog(parentComponent);
		// 如果用户按下了确定键，则将文件模型设置为用户选择的文件。
		if (result == JFileChooser.APPROVE_OPTION) {
			File file = jfc.getSelectedFile();
			// TODO 此处处理不好，有空的时候在写点更好的方法。
			if (!file.getAbsolutePath().endsWith(".xls")) {
				file = new File(file.getAbsolutePath() + ".xls");
			}
			actionManager.setFile2Export(file);
		}
	}

}
