package com.jier.xlstool.wcbz.core.util;

public final class Constants {

	/** 默认的丢失文本字段。 */
	public final static String MISSING_LABEL = "！文本丢失";

	/** 资源列表所在的路径。 */
	public final static String JPATH_RESOURCE_LIST = "/com/jneagle/xlstool/zzxm/resources/resource-list.xml";
	/** Jar包内默认的国际化属性文件路径。 */
	public final static String JPATH_DEFAULT_I18N_PROP_FILE = "/com/jier/xlstool/wcbz/resources/i18n/zh_CN.properties";

	/** 代表强制重置配置文件的命令行参数。 */
	public static final String CLI_OPT_FLAG_CONFIG_FORCE_RESET = "r";

	private Constants() {
		// 禁止外部实例化。
	}

}
