package com.jier.xlstool.wcbz.view.task;

import com.dwarfeng.dutil.develop.backgr.AbstractTask;
import com.jier.xlstool.wcbz.core.control.ActionManager;
import com.jier.xlstool.wcbz.core.control.ModelManager;

public abstract class AbstractViewTask extends AbstractTask {

	/** 模型管理器。 */
	protected final ModelManager modelManager;
	/** 动作管理器。 */
	protected final ActionManager actionManager;

	public AbstractViewTask(ModelManager modelManager, ActionManager actionManager) {
		this.modelManager = modelManager;
		this.actionManager = actionManager;
	}

}
