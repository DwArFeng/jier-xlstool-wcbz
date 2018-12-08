package com.jier.xlstool.wcbz.view.task;

import java.util.Optional;

import javax.swing.JFileChooser;

import com.dwarfeng.dutil.basic.cna.model.DefaultReferenceModel;
import com.dwarfeng.dutil.basic.cna.model.ReferenceModel;
import com.dwarfeng.dutil.basic.gui.swing.SwingUtil;
import com.jier.xlstool.wcbz.core.control.ActionManager;
import com.jier.xlstool.wcbz.core.control.ModelManager;

public class SelectLoadFileTask extends AbstractViewTask {

	private final String dialogTitle;

	public SelectLoadFileTask(ModelManager modelManager, ActionManager actionManager) {
		super(modelManager, actionManager);
		// TODO Auto-generated constructor stub
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void todo() throws Exception {
		ReferenceModel<JFileChooser> fileChooserRef = new DefaultReferenceModel<>();

		SwingUtil.invokeInEventQueue(() -> {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle(dialogTitle);

			// 如果输出文件模型中有文件，则将当前文件夹设置为输出文件的文件夹。
			Optional.ofNullable(modelManager.getFile2ExportModel().get())
					.ifPresent(file -> fileChooser.setCurrentDirectory(file.getParentFile()));

		});

	}

}
