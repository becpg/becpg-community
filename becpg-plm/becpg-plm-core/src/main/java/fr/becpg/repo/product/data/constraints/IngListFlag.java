/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG. 
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
package fr.becpg.repo.product.data.constraints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Qualifier of an ingredient line, stored in the multi-valued bcpg:ingListFlags property.
 *
 * @author matthieu
 */
public enum IngListFlag {

    PROCESSING_AID,
    SUPPORT,
    IMPURITY,
    NANO,
    GMO,
    IONIZED;

    /**
     * Returns the flag codes without duplicates nor blanks, known flags first in declaration order, so that
     * two lines carrying the same flags compare equal whatever the order they were written in. A form saved
     * with no box ticked stores an empty code, which means no flag.
     *
     * @param codes the flag codes, may be null
     * @return a new list of distinct codes, empty when codes is null
     */
    public static List<String> normalize(Collection<String> codes) {
        List<String> normalized = new ArrayList<>();
        if (codes == null) {
            return normalized;
        }
        for (IngListFlag flag : values()) {
            if (codes.contains(flag.name())) {
                normalized.add(flag.name());
            }
        }
        for (String code : codes) {
            if ((code != null) && !code.isBlank() && !normalized.contains(code)) {
                normalized.add(code);
            }
        }
        return normalized;
    }
}
