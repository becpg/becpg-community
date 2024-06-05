/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.repository.model;

import java.util.List;

/**
 * <p>ForecastValueDataItem interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface ForecastValueDataItem extends ManualDataItem, SimpleCharactDataItem, AspectAwareDataItem {
	
	/**
	 * <p>getForecastColumns.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	List<String> getForecastColumns();
	
	/**
	 * <p>setForecastValue.</p>
	 *
	 * @param forecastColumn a {@link java.lang.String} object
	 * @param value a {@link java.lang.Double} object
	 */
	void setForecastValue(String forecastColumn, Double value);
	
	/**
	 * <p>getForecastValue.</p>
	 *
	 * @param forecastColumn a {@link java.lang.String} object
	 * @return a {@link java.lang.Double} object
	 */
	Double getForecastValue(String forecastColumn);
	
	/**
	 * <p>getForecastAccessor.</p>
	 *
	 * @param forecastColumn a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	String getForecastAccessor(String forecastColumn);

	public static class ForecastContext<T> {
		private String forecastColumn;
		private String accessor;
		private ForecastValueGetter<T> valueGetter;
		private ForecastValueSetter<T> valueSetter;
		public ForecastContext(String forecastColumn, String accessor, ForecastValueSetter<T> valueSetter,
				ForecastValueGetter<T> valueGetter) {
			super();
			this.forecastColumn = forecastColumn;
			this.accessor = accessor;
			this.valueSetter = valueSetter;
			this.valueGetter = valueGetter;
		}
		public String getForecastColumn() {
			return forecastColumn;
		}
		
		public String getAccessor() {
			return accessor;
		}
		public void setValue(T forecastItem, Double value) {
			valueSetter.setValue(forecastItem, value);
		}
		public Double getValue(T forecastItem) {
			return valueGetter.getValue(forecastItem);
		}
		public interface ForecastValueGetter<T>{
			Double getValue(T forecastItem);
		}
		
		public interface ForecastValueSetter<T> {
			void setValue(T forecastItem, Double value);
		}
	}
}
