package com.jier.xlstool.wcbz.core.view.task;

import com.jier.xlstool.wcbz.core.control.ActionManager;
import com.jier.xlstool.wcbz.core.control.ModelManager;

public class ExitTask extends AbstractViewTask {

	public ExitTask(ModelManager modelManager, ActionManager actionManager) {
		super(modelManager, actionManager);
	}

	@Override
	protected void todo() throws Exception {
		actionManager.exit();
	}

}
