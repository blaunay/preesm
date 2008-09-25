package org.ietr.preesm.plugin.mapper.plot;

import java.text.FieldPosition;
import java.util.Date;

import org.jfree.chart.util.RelativeDateFormat;

/**
 * Time display format for Gantt charts
 * 
 * @author mpelcat
 */
public class VertexDateFormat extends RelativeDateFormat {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9164797353964241418L;

	@Override
	public StringBuffer format(Date date, StringBuffer toAppendTo,
			FieldPosition fieldPosition) {
		return new StringBuffer(String.format("%d cycles", date.getTime()));
	}

}