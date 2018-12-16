package com.jier.xlstool.wcbz.core.model.enumeration;

import com.dwarfeng.dutil.basic.str.DefaultName;
import com.dwarfeng.dutil.basic.str.Name;

public enum I18nKey implements Name {

	LOGGER_1(new DefaultName("logger.1")), //
	LOGGER_2(new DefaultName("logger.2")), //
	LOGGER_3(new DefaultName("logger.3")), //
	LOGGER_4(new DefaultName("logger.4")), //
	LOGGER_5(new DefaultName("logger.5")), //
	LOGGER_6(new DefaultName("logger.6")), //
	LOGGER_7(new DefaultName("logger.7")), //
	LOGGER_8(new DefaultName("logger.8")), //
	LOGGER_9(new DefaultName("logger.9")), //
	LOGGER_10(new DefaultName("logger.10")), //
	LOGGER_11(new DefaultName("logger.11")), //
	LOGGER_12(new DefaultName("logger.12")), //
	LOGGER_13(new DefaultName("logger.13")), //
	LOGGER_14(new DefaultName("logger.14")), //
	LOGGER_15(new DefaultName("logger.15")), //
	LOGGER_16(new DefaultName("logger.16")), //
	LOGGER_17(new DefaultName("logger.17")), //
	LOGGER_18(new DefaultName("logger.18")), //
	LOGGER_19(new DefaultName("logger.19")), //
	LOGGER_20(new DefaultName("logger.20")), //
	LOGGER_21(new DefaultName("logger.21")), //
	LOGGER_22(new DefaultName("logger.22")), //
	LOGGER_23(new DefaultName("logger.23")), //
	LOGGER_24(new DefaultName("logger.24")), //
	LOGGER_25(new DefaultName("logger.25")), //
	LOGGER_26(new DefaultName("logger.26")), //
	LOGGER_27(new DefaultName("logger.27")), //
	LOGGER_28(new DefaultName("logger.28")), //
	LOGGER_29(new DefaultName("logger.29")), //
	LOGGER_30(new DefaultName("logger.30")), //
	LOGGER_31(new DefaultName("logger.31")), //
	LOGGER_32(new DefaultName("logger.32")), //
	LOGGER_33(new DefaultName("logger.33")), //
	LOGGER_34(new DefaultName("logger.34")), //
	LOGGER_35(new DefaultName("logger.35")), //
	LOGGER_36(new DefaultName("logger.36")), //
	LOGGER_37(new DefaultName("logger.37")), //
	LOGGER_38(new DefaultName("logger.38")), //
	LOGGER_39(new DefaultName("logger.39")), //
	LOGGER_40(new DefaultName("logger.40")), //
	LOGGER_41(new DefaultName("logger.41")), //
	LOGGER_42(new DefaultName("logger.42")), //
	LOGGER_43(new DefaultName("logger.43")), //
	LOGGER_44(new DefaultName("logger.44")), //
	LOGGER_45(new DefaultName("logger.45")), //
	LOGGER_46(new DefaultName("logger.46")), //

	LABEL_1(new DefaultName("label.1")), //
	LABEL_2(new DefaultName("label.2")), //
	LABEL_3(new DefaultName("label.3")), //
	LABEL_4(new DefaultName("label.4")), //
	LABEL_5(new DefaultName("label.5")), //
	LABEL_6(new DefaultName("label.6")), //
	LABEL_7(new DefaultName("label.7")), //
	LABEL_8(new DefaultName("label.8")), //
	LABEL_9(new DefaultName("label.9")), //
	LABEL_10(new DefaultName("label.10")), //
	LABEL_11(new DefaultName("label.11")), //
	LABEL_12(new DefaultName("label.12")), //
	LABEL_13(new DefaultName("label.13")), //
	LABEL_14(new DefaultName("label.14")), //
	LABEL_15(new DefaultName("label.15")), //
	LABEL_16(new DefaultName("label.16")), //
	LABEL_17(new DefaultName("label.17")),//

	;

	private Name name;

	private I18nKey(Name name) {
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dwarfeng.dutil.basic.str.Name#getName()
	 */
	@Override
	public String getName() {
		return name.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
		return name.getName();
	}
}
