package com.jier.xlstool.wcbz.core.model.enumeration;

import com.dwarfeng.dutil.basic.str.Name;

/**
 * 资源枚举。
 * 
 * @author DwArFeng
 * @since 0.0.0-alpha
 */
public enum ResourceKey implements Name {

	/** 记录器处理器配置文件。 */
	LOGGER_SETTING("logger-setting"),
	/** 国际化处理器配置文件。 */
	I18N_SETTING("i18n-setting"),
	/** 程序配置文件。 */
	CONFIG("config"),

	;

	private final String name;

	private ResourceKey(String name) {
		this.name = name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return name;
	}

}
