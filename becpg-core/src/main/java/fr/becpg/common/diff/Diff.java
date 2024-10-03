/*
 * Diff Match and Patch
 * Copyright 2018 The diff-match-patch Authors.
 * https://github.com/google/diff-match-patch
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Modifications copyright (C) 2018 Author
 */

package fr.becpg.common.diff;

/**
 * Class representing one diff operation.
 *
 * @author matthieu
 */
public class Diff {
    /**
     * One of: INSERT, DELETE or EQUAL.
     */
    private Operation operation;
    /**
     * The dmp associated with this diff operation.
     */
    private String text;

    /**
     * Constructor.  Initializes the diff with the provided values.
     *
     * @param operation One of INSERT, DELETE or EQUAL.
     * @param text      The dmp being applied.
     */
    public Diff(Operation operation, String text) {
        // Construct a diff with the specified operation and dmp.
        this.operation = operation;
        this.text = text;
    }

    
    
    /**
     * <p>Getter for the field <code>operation</code>.</p>
     *
     * @return a {@link fr.becpg.common.diff.Operation} object
     */
    public Operation getOperation() {
		return operation;
	}



	/**
	 * <p>Setter for the field <code>operation</code>.</p>
	 *
	 * @param operation a {@link fr.becpg.common.diff.Operation} object
	 */
	public void setOperation(Operation operation) {
		this.operation = operation;
	}



	/**
	 * <p>Getter for the field <code>text</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getText() {
		return text;
	}



	/**
	 * <p>Setter for the field <code>text</code>.</p>
	 *
	 * @param text a {@link java.lang.String} object
	 */
	public void setText(String text) {
		this.text = text;
	}



    /**
     * Display a human-readable version of this Diff.
     *
     * @return dmp version.
     */
    public String toString() {
        String prettyText = this.text.replace('\n', '\u00b6');
        return "Diff(" + this.operation + ",\"" + prettyText + "\")";
    }

    
    
    /**
     * {@inheritDoc}
     *
     * Create a numeric hash value for a Diff.
     * This function is not used by DMP.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = (operation == null) ? 0 : operation.hashCode();
        result += prime * ((text == null) ? 0 : text.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * Is this Diff equivalent to another Diff?
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Diff other = (Diff) obj;
        if (operation != other.operation) {
            return false;
        }
        if (text == null) {
            if (other.text != null) {
                return false;
            }
        } else if (!text.equals(other.text)) {
            return false;
        }
        return true;
    }
}
