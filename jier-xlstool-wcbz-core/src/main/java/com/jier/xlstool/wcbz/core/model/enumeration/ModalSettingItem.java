package com.jier.xlstool.wcbz.core.model.enumeration;

import com.dwarfeng.dutil.develop.setting.SettingEnumItem;
import com.dwarfeng.dutil.develop.setting.SettingInfo;
import com.dwarfeng.dutil.develop.setting.info.BooleanSettingInfo;
import com.dwarfeng.dutil.develop.setting.info.FileSettingInfo;

public enum ModalSettingItem implements SettingEnumItem {

	/** 是否有上次打开的文件。 */
	FLAG_LAST_LOADED_FILE_EXISTS("flag.last-loaded-file-exists", new BooleanSettingInfo("false")),
	/** 上次打开的文件位置。 */
	FILE_LAST_LOADED_FILE("path.last-loaded-file", new FileSettingInfo("attribute")),
	/** 是否有上次打开的文件。 */
	FLAG_LAST_EXPORTED_FILE_EXISTS("flag.last-exported-file-exists", new BooleanSettingInfo("false")),
	/** 上次打开的文件位置。 */
	FILE_LAST_EXPORTED_FILE("path.last-exported-file", new FileSettingInfo("attribute")),

	;

	private final String name;
	private final SettingInfo settingInfo;

	private ModalSettingItem(String name, SettingInfo settingInfo) {
		this.name = name;
		this.settingInfo = settingInfo;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SettingInfo getSettingInfo() {
		return settingInfo;
	}

}
