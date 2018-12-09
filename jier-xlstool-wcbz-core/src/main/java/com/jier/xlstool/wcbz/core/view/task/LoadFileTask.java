package com.jier.xlstool.wcbz.core.view.task;

import java.awt.Component;
import java.io.File;
import java.util.Objects;

import javax.swing.JOptionPane;

import com.dwarfeng.dutil.basic.cna.model.SyncReferenceModel;
import com.jier.xlstool.wcbz.core.control.ActionManager;
import com.jier.xlstool.wcbz.core.control.ModelManager;
import com.jier.xlstool.wcbz.core.model.enumeration.I18nKey;
import com.jier.xlstool.wcbz.core.util.Constants;

public class LoadFileTask extends AbstractViewTask {

	private final Component parentComponent;

	public LoadFileTask(ModelManager modelManager, ActionManager actionManager, Component parentComponent) {
		super(modelManager, actionManager);
		this.parentComponent = parentComponent;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void todo() throws Exception {
		SyncReferenceModel<File> file2LoadModel = modelManager.getFile2LoadModel();

		if (Objects.isNull(file2LoadModel.get())) {
			JOptionPane.showMessageDialog(parentComponent, i18nString(I18nKey.LABEL_12), i18nString(I18nKey.LABEL_13),
					JOptionPane.WARNING_MESSAGE);
		}

		actionManager.loadFile();
	}

	private final String i18nString(I18nKey key) {
		return modelManager.getI18nHandler().getStringOrDefault(key, Constants.MISSING_LABEL);
	}

}
