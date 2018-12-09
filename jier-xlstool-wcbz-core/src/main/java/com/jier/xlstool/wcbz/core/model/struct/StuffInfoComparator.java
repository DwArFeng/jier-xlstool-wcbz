package com.jier.xlstool.wcbz.core.model.struct;

import java.util.Comparator;

import com.dwarfeng.dutil.basic.cna.AttributeComplex;
import com.jier.xlstool.wcbz.core.util.Constants;

public class StuffInfoComparator implements Comparator<AttributeComplex> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compare(AttributeComplex o1, AttributeComplex o2) {
		String workNumber1 = o1.get(Constants.ATTRIBUTE_COMPLEX_MARK_WORK_NUMBER, String.class);
		String workNumber2 = o2.get(Constants.ATTRIBUTE_COMPLEX_MARK_WORK_NUMBER, String.class);
		return workNumber1.compareTo(workNumber2);
	}

}
