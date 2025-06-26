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
package fr.becpg.repo.autocomplete;

import java.util.ArrayList;
import java.util.List;

import fr.becpg.repo.RepoConsts;

 /**
  * <p>AutoCompletePage class.</p>
  *
  * @author "Matthieu Laborie"
  * @version $Id: $Id
  */
 public class AutoCompletePage {
        
        private final List<AutoCompleteEntry> results;
        private final Integer pageSize;
        private final Integer page;
        private final Integer fullListSize;
        
        
        @SuppressWarnings("unchecked")
		/**
		 * <p>Constructor for AutoCompletePage.</p>
		 *
		 * @param fullList a {@link java.util.List} object.
		 * @param pageNum a {@link java.lang.Integer} object.
		 * @param pageSize a {@link java.lang.Integer} object.
		 * @param listValueExtractor a {@link fr.becpg.repo.autocomplete.AutoCompleteExtractor} object.
		 * @param characNameFormat a {@link java.util.String} object.
		 * @param <T> a T class
		 */
		public <T> AutoCompletePage(List<T> fullList, Integer pageNum, Integer pageSize,
				AutoCompleteExtractor<T> listValueExtractor, String characNameFormat) {
			if (pageNum == null || pageNum < 1) {
				pageNum = 1;
			}

			if (RepoConsts.MAX_RESULTS_UNLIMITED.equals(pageSize)) {
				this.page = 0;
				this.pageSize = fullList.size();
				this.fullListSize = fullList.size();
				this.results = listValueExtractor.extract(fullList/*, characNameFormat*/);
			} else {

				this.page = pageNum;
				this.pageSize = pageSize;
				this.fullListSize = fullList.size();

				int fromIndex = Math.max((page - 1) * pageSize, 0);
				int toIndex = Math.min(page * pageSize, fullListSize);

				if (!fullList.isEmpty() && toIndex >= fromIndex) {
					if (listValueExtractor == null) {
						results = (List<AutoCompleteEntry>) fullList.subList(fromIndex, toIndex);
					} else {
						results = listValueExtractor.extract(fullList.subList(fromIndex, toIndex)/*, characNameFormat*/);
					}
				} else {
					results = new ArrayList<>();
				}
			}
		}
     
        public <T> AutoCompletePage(List<T> fullList, Integer pageNum, Integer pageSize,
				AutoCompleteExtractor<T> listValueExtractor) {
        	this(fullList, pageNum, pageSize, listValueExtractor, null);
        }


		/**
		 * <p>Getter for the field <code>results</code>.</p>
		 *
		 * @return a {@link java.util.List} object.
		 */
		public List<AutoCompleteEntry> getResults() {
			return results;
		}

		/**
		 * <p>getPageNumber.</p>
		 *
		 * @return a {@link java.lang.Integer} object.
		 */
		public Integer getPageNumber() {
			return page;
		}

		/**
		 * <p>getObjectsPerPage.</p>
		 *
		 * @return a {@link java.lang.Integer} object.
		 */
		public Integer getObjectsPerPage() {
			return pageSize;
		}

		/**
		 * <p>Getter for the field <code>fullListSize</code>.</p>
		 *
		 * @return a {@link java.lang.Integer} object.
		 */
		public Integer getFullListSize() {
			return fullListSize;
		}
 }

