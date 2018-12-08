package com.jier.xlstool.wcbz.core.control;

import java.io.File;

import com.dwarfeng.dutil.basic.cna.AttributeComplex;
import com.dwarfeng.dutil.basic.cna.model.ListModel;
import com.dwarfeng.dutil.basic.cna.model.SyncReferenceModel;
import com.dwarfeng.dutil.develop.backgr.Background;
import com.dwarfeng.dutil.develop.i18n.SyncI18nHandler;
import com.dwarfeng.dutil.develop.logger.SyncLoggerHandler;
import com.dwarfeng.dutil.develop.setting.SyncSettingHandler;

/**
 * 
 * @author DwArFeng
 * @since 0.0.0-alpha
 */
public interface ModelManager {

	/**
	 * @return the background
	 */
	public Background getBackground();

	/**
	 * @return the coreSettingHandler
	 */
	public SyncSettingHandler getCoreSettingHandler();

	/**
	 * @return the file2ExportModel
	 */
	public SyncReferenceModel<File> getFile2ExportModel();

	/**
	 * @return the file2OpenModel
	 */
	public SyncReferenceModel<File> getFile2OpenModel();

	/**
	 * @return the i18nHandler
	 */
	public SyncI18nHandler getI18nHandler();

	/**
	 * @return the loggerHandler
	 */
	public SyncLoggerHandler getLoggerHandler();

	/**
	 * @return the modalSettingHandler
	 */
	public SyncSettingHandler getModalSettingHandler();

	/**
	 * @return the stuffInfoModel
	 */
	public ListModel<AttributeComplex> getStuffInfoModel();

}
