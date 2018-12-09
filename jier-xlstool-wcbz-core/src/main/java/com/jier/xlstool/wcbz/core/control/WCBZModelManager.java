package com.jier.xlstool.wcbz.core.control;

import java.io.File;

import com.dwarfeng.dutil.basic.cna.AttributeComplex;
import com.dwarfeng.dutil.basic.cna.model.ListModel;
import com.dwarfeng.dutil.basic.cna.model.SyncReferenceModel;
import com.dwarfeng.dutil.develop.backgr.Background;
import com.dwarfeng.dutil.develop.i18n.SyncI18nHandler;
import com.dwarfeng.dutil.develop.logger.SyncLoggerHandler;
import com.dwarfeng.dutil.develop.setting.SyncSettingHandler;

class WCBZModelManager implements ModelManager {

	private final WCBZ wcbz;

	public WCBZModelManager(WCBZ wcbz) {
		this.wcbz = wcbz;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Background getBackground() {
		return wcbz.getBackground();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SyncSettingHandler getCoreSettingHandler() {
		return wcbz.getCoreSettingHandler();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SyncReferenceModel<File> getFile2ExportModel() {
		return wcbz.getFile2ExportModel();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SyncReferenceModel<File> getFile2LoadModel() {
		return wcbz.getFile2LoadModel();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SyncI18nHandler getI18nHandler() {
		return wcbz.getI18nHandler();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SyncLoggerHandler getLoggerHandler() {
		return wcbz.getLoggerHandler();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SyncSettingHandler getModalSettingHandler() {
		return wcbz.getModalSettingHandler();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ListModel<AttributeComplex> getStuffInfoModel() {
		return wcbz.getStuffInfoModel();
	}

}
